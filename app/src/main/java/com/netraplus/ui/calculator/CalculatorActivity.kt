package com.netraplus.ui.calculator

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.netraplus.R
import com.netraplus.utils.TTSManager
import com.netraplus.utils.Prefs
import com.netraplus.utils.VibrationHelper

class CalculatorActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var tts: TTSManager
    private lateinit var display: TextView

    private var current = StringBuilder()
    private var operand: Double? = null
    private var operator: Char? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculator)

        tts = TTSManager(this)
        tts.init(onReady = {
            tts.setPitch(Prefs.getTtsPitch(this))
            tts.setSpeechRate(Prefs.getTtsSpeed(this))
            tts.speak(getString(R.string.calc_title))
        })

        findViewById<MaterialToolbar>(R.id.topAppBar).apply {
            title = getString(R.string.calc_title)
            setNavigationIcon(R.drawable.ic_back)
            navigationContentDescription = getString(R.string.action_back)
            setNavigationOnClickListener { finish() }
        }

        display = findViewById(R.id.tvDisplay)

        val btnIds = intArrayOf(
            R.id.btn1, R.id.btnEq, R.id.btnClear,
            R.id.btn0, R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
            R.id.btnAdd, R.id.btnSub, R.id.btnMul, R.id.btnDiv, R.id.btnDot
        )
        btnIds.forEach { id ->
            val v = findViewById<View?>(id)
            v?.setOnClickListener(this)
        }
    }

    override fun onClick(v: View?) {
        if (v == null) return
        VibrationHelper.vibrate(this)
        val text = (v as? TextView)?.text?.toString() ?: ""
        when (v.id) {
            R.id.btnClear -> {
                current.clear(); operand = null; operator = null
                display.text = "0"
                tts.speak(getString(R.string.calc_cleared))
            }
            R.id.btnAdd, R.id.btnSub, R.id.btnMul, R.id.btnDiv -> {
                commitNumber()
                operator = when (v.id) {
                    R.id.btnAdd -> '+'
                    R.id.btnSub -> '-'
                    R.id.btnMul -> '*'
                    else -> '/'
                }
                tts.speak(
                    when (operator) {
                        '+' -> getString(R.string.calc_plus)
                        '-' -> getString(R.string.calc_minus)
                        '*' -> getString(R.string.calc_multiply)
                        else -> getString(R.string.calc_divide)
                    }
                )
            }
            R.id.btnEq -> {
                val res = calculate()
                display.text = format(res)
                tts.speak(getString(R.string.calc_equals_to, display.text))
                operand = res
                operator = null
                current.clear()
            }
            else -> {
                // number or dot
                if (v.id == R.id.btnDot && current.contains('.')) return
                current.append(text)
                display.text = current.toString()
                tts.speak(v.contentDescription?.toString() ?: text)
            }
        }
    }

    private fun commitNumber() {
        val num = current.toString().toDoubleOrNull()
        if (num != null) {
            if (operand == null) operand = num else operand = applyOp(operand!!, operator, num)
        }
        current.clear()
    }

    private fun calculate(): Double {
        val num = current.toString().toDoubleOrNull()
        val res = if (operand == null && num != null) num
        else if (operand != null && num != null) applyOp(operand!!, operator, num)
        else operand ?: 0.0
        return res
    }

    private fun applyOp(a: Double, op: Char?, b: Double): Double {
        return when (op) {
            '+' -> a + b
            '-' -> a - b
            '*' -> a * b
            '/' -> if (b == 0.0) Double.NaN else a / b
            else -> b
        }
    }

    private fun format(v: Double): String {
        val asLong = v.toLong()
        return if (v.isNaN()) getString(R.string.calc_nan)
        else if (v == asLong.toDouble()) asLong.toString() else String.format("%s", v)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::tts.isInitialized) tts.shutdown()
    }
}
