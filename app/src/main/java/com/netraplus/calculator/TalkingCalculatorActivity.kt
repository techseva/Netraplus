package com.netraplus.calculator

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.netraplus.R
import com.netraplus.utils.SpeechHelper

/**
 * Enhanced Talking Calculator Activity with voice recognition and learning mode.
 * Features:
 * - Shunting-yard algorithm for reliable calculations
 * - Voice recognition for hands-free input
 * - Learning mode with step-by-step explanations
 * - TTS feedback on focus and button press
 * - Haptic feedback on button press
 * - Full TalkBack support
 * - Calculation history
 */
class TalkingCalculatorActivity : AppCompatActivity() {

    private lateinit var display: TextView
    private lateinit var vibrator: Vibrator
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var settingsManager: SettingsManager
    
    // Calculator state
    private var currentExpression = ""
    private var currentNumber = ""
    private var expression = "" // For compatibility with existing code
    private var hapticEnabled = true
    private var learningMode = true
    private var isListening = false
    private var lastExpressionBeforeEquals = ""
    private var justCalculated = false
    
    // Voice recognition
    private var speechRecognizer: SpeechRecognizer? = null

    companion object {
        private const val PERMISSION_REQUEST_CODE = 123
        private const val TAG = "TalkingCalculator"
        private const val PREF_LEARNING_MODE = "learning_mode"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // UI inflation and view binding — only these should trigger safe mode
        try {
            setContentView(R.layout.activity_talking_calculator)
            initializeVibrator()
            initializePreferences()
            initializeViews()
            setupButtonListeners()
            setupDisplayListener()
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Calculator UI error: ${e.javaClass.simpleName}: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            val tv = TextView(this).apply {
                text = "UI error: ${e.javaClass.simpleName}: ${e.message}\nPlease share this message."
                textSize = 18f
                setPadding(24, 24, 24, 24)
            }
            setContentView(tv)
            return
        }

        // TTS init — failures here should not replace the UI
        try {
            SpeechHelper.init(this) {
                SpeechHelper.speak("Talking calculator ready. Use swipe or arrow keys to move. Double-tap to activate.")
            }
        } catch (e: Exception) {
            Toast.makeText(this, "TTS init error: ${e.javaClass.simpleName}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initializeVibrator() {
        vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    private fun initializePreferences() {
        sharedPrefs = getSharedPreferences("calc_voice_prefs", Context.MODE_PRIVATE)
        hapticEnabled = sharedPrefs.getBoolean("haptic_enabled", true)
        learningMode = sharedPrefs.getBoolean(PREF_LEARNING_MODE, true)
        settingsManager = SettingsManager(this)
    }

    private fun initializeViews() {
        val res = resources
        val pkg = packageName
        val resultId = res.getIdentifier("tvResult", "id", pkg)
        val displayId = res.getIdentifier("tvDisplay", "id", pkg)
        val resultView: TextView? = if (resultId != 0) findViewById(resultId) else null
        val displayView: TextView? = if (displayId != 0) findViewById(displayId) else null
        display = resultView ?: displayView ?: throw IllegalStateException("Calculator display view not found")
        display.text = "0"
    }

    private fun setupButtonListeners() {
        // Map of button ID names to their symbols (works across layouts)
        val nameToSymbol = listOf(
            "btn0" to "0", "btn1" to "1", "btn2" to "2", "btn3" to "3",
            "btn4" to "4", "btn5" to "5", "btn6" to "6", "btn7" to "7",
            "btn8" to "8", "btn9" to "9",
            // decimal
            "btnDot" to ".", "btnDecimal" to ".",
            // divide
            "btnDiv" to "÷", "btnDivide" to "÷",
            // multiply
            "btnMul" to "×", "btnMultiply" to "×",
            // minus
            "btnSub" to "−", "btnMinus" to "−",
            // plus, equals, clear
            "btnPlus" to "+", "btnEq" to "=", "btnEquals" to "=", "btnClear" to "C",
            // extras
            "btnBackspace" to "BACKSPACE", "btnPercent" to "%", "btnPlusMinus" to "SIGN"
        )

        val res = resources
        val pkg = packageName

        for ((name, symbol) in nameToSymbol) {
            val id = res.getIdentifier(name, "id", pkg)
            if (id == 0) continue
            val view = findViewById<View>(id) ?: continue
            // Focus listener — speak cursor prompt when focused
            view.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    val desc = v.contentDescription?.toString()
                    if (!desc.isNullOrEmpty()) {
                        SpeechHelper.speak(desc)
                    }
                }
            }
            // Click listener — handle button press
            view.setOnClickListener {
                vibrateShort()
                handleSymbol(symbol)
            }
        }
    }

    private fun setupDisplayListener() {
        display.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                SpeechHelper.speak(getString(R.string.cursor_display))
            }
        }
    }

    // Button click handlers
    private fun onNumberClick(number: String) {
        currentNumber += number
        currentExpression += number
        expression = currentExpression
        updateDisplay()
        speakText(number)
    }

    private fun onOperatorClick(operator: String) {
        if (currentExpression.isNotEmpty() && !currentExpression.endsWith(" ")) {
            val opSymbol = when (operator) {
                "+" -> "+"
                "-" -> "−"
                "*" -> "×"
                "/" -> "÷"
                else -> operator
            }
            currentExpression += " $opSymbol "
            expression = currentExpression
            currentNumber = ""
            updateDisplay()
            speakText(getOperatorName(operator))
        }
    }

    private fun onDecimalClick() {
        if (!currentNumber.contains(".")) {
            currentNumber += "."
            currentExpression += "."
            expression = currentExpression
            updateDisplay()
            speakText("decimal")
        }
    }

    private fun onEqualsClick() {
        if (currentExpression.isNotEmpty()) {
            calculateResult()
        }
    }

    private fun onClearClick() {
        currentExpression = ""
        currentNumber = ""
        expression = ""
        display.text = "0"
        speakText("cleared")
    }

    private fun onBackspaceClick() {
        // If a calculation just happened, restore the prior expression to edit
        if (justCalculated) {
            if (lastExpressionBeforeEquals.isNotEmpty()) {
                currentExpression = lastExpressionBeforeEquals
                expression = currentExpression
                currentNumber = ""
                justCalculated = false
                updateDisplay()
                speakText("restored")
                return
            } else {
                justCalculated = false
            }
        }

        if (currentExpression.isNotEmpty()) {
            // Remove trailing spaces first
            var expr = currentExpression
            while (expr.isNotEmpty() && expr.last() == ' ') {
                expr = expr.dropLast(1)
            }

            var deletedSpoken = ""
            // If ends with operator, remove whole operator block
            if (expr.endsWith("+") || expr.endsWith("−") || expr.endsWith("×") || expr.endsWith("÷")) {
                val op = expr.last()
                deletedSpoken = getCharName(op)
                expr = expr.dropLast(1)
                // Remove any preceding space
                while (expr.isNotEmpty() && expr.last() == ' ') {
                    expr = expr.dropLast(1)
                }
            } else {
                // Remove last digit/decimal point
                val ch = expr.last()
                deletedSpoken = getCharName(ch)
                expr = expr.dropLast(1)
            }

            currentExpression = expr
            expression = currentExpression
            // Adjust currentNumber
            currentNumber = if (currentNumber.isNotEmpty()) currentNumber.dropLast(1) else ""
            updateDisplay()
            
            if (deletedSpoken.isNotBlank()) {
                speakText("deleted $deletedSpoken")
            } else {
                speakText("deleted")
            }
        }
    }

    private fun onVoiceClick() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) 
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                arrayOf(Manifest.permission.RECORD_AUDIO), PERMISSION_REQUEST_CODE)
            return
        }
        startVoiceRecognition()
    }

    // Settings button click handler
    fun onSettingsButtonClick(view: View) {
        val intent = Intent(this, CalculatorVoiceSettingsActivity::class.java)
        startActivity(intent)
    }

    // Voice button click handler (for XML onClick)
    fun onVoiceButtonClick(view: View) {
        onVoiceClick()
    }

    /**
     * Handle button symbol press (unified handler).
     */
    private fun handleSymbol(s: String) {
        when (s) {
            "=" -> onEqualsClick()
            "C" -> onClearClick()
            "." -> onDecimalClick()
            "÷" -> onOperatorClick("/")
            "×" -> onOperatorClick("*")
            "−" -> onOperatorClick("-")
            "+" -> onOperatorClick("+")
            "%" -> onPercentClick()
            "BACKSPACE" -> onBackspaceClick()
            "SIGN" -> onToggleSignClick()
            else -> {
                if (s.matches(Regex("\\d"))) {
                    onNumberClick(s)
                }
            }
        }
    }

    // XML onClick entry point to support layouts that use android:onClick
    fun onButtonClick(view: View) {
        val name = try { resources.getResourceEntryName(view.id) } catch (_: Exception) { "" }
        val symbol = when (name) {
            "btn0" -> "0"
            "btn1" -> "1"
            "btn2" -> "2"
            "btn3" -> "3"
            "btn4" -> "4"
            "btn5" -> "5"
            "btn6" -> "6"
            "btn7" -> "7"
            "btn8" -> "8"
            "btn9" -> "9"
            // decimal variants
            "btnDot" -> "."
            "btnDecimal" -> "."
            // divide variants
            "btnDiv" -> "÷"
            "btnDivide" -> "÷"
            // multiply variants
            "btnMul" -> "×"
            "btnMultiply" -> "×"
            // minus variants
            "btnSub" -> "−"
            "btnMinus" -> "−"
            // plus
            "btnPlus" -> "+"
            // equals variants
            "btnEq" -> "="
            "btnEquals" -> "="
            // clear
            "btnClear" -> "C"
            // extras
            "btnBackspace" -> "BACKSPACE"
            "btnPercent" -> "%"
            "btnPlusMinus" -> "SIGN"
            else -> ""
        }
        if (symbol.isNotEmpty()) {
            vibrateShort()
            handleSymbol(symbol)
        }
    }

    private fun onPercentClick() {
        if (currentNumber.isNotEmpty()) {
            val value = currentNumber.toDoubleOrNull()
            if (value != null) {
                val percent = value / 100.0
                // replace the last number occurrence in expression with its percent
                val idx = currentExpression.lastIndexOf(currentNumber)
                if (idx >= 0) {
                    currentExpression = currentExpression.substring(0, idx) + formatResult(percent) +
                        currentExpression.substring(idx + currentNumber.length)
                    expression = currentExpression
                    currentNumber = formatResult(percent)
                    updateDisplay()
                    speakText("percent")
                }
            }
        }
    }

    private fun onToggleSignClick() {
        if (currentNumber.isEmpty()) return
        val isNegative = currentNumber.startsWith("-")
        val newNumber = if (isNegative) currentNumber.removePrefix("-") else "-" + currentNumber
        val idx = currentExpression.lastIndexOf(currentNumber)
        if (idx >= 0) {
            currentExpression = currentExpression.substring(0, idx) + newNumber +
                currentExpression.substring(idx + currentNumber.length)
            expression = currentExpression
            currentNumber = newNumber
            updateDisplay()
            speakText(if (isNegative) "positive" else "negative")
        }
    }

    private fun updateDisplay() {
        display.text = if (currentExpression.isEmpty()) "0" else currentExpression
    }

    private fun getOperatorName(operator: String): String {
        return when (operator) {
            "+" -> "plus"
            "-" -> "minus"
            "*" -> "multiply"
            "/" -> "divide"
            else -> operator
        }
    }

    private fun formatResult(value: Double): String {
        return if (value % 1.0 == 0.0) {
            value.toLong().toString()
        } else {
            String.format("%.10f", value).trimEnd('0').trimEnd('.')
        }
    }

    private fun getCharName(ch: Char): String {
        return when (ch) {
            '0' -> "zero"
            '1' -> "one"
            '2' -> "two"
            '3' -> "three"
            '4' -> "four"
            '5' -> "five"
            '6' -> "six"
            '7' -> "seven"
            '8' -> "eight"
            '9' -> "nine"
            '.' -> "decimal point"
            '+' -> "plus"
            '−' -> "minus"
            '×' -> "multiply"
            '÷' -> "divide"
            else -> ch.toString()
        }
    }

    private fun calculateResult() {
        try {
            // Save the current expression so delete can restore it
            lastExpressionBeforeEquals = currentExpression

            // Convert display symbols to calculation symbols
            val expressionForCalc = currentExpression
                .replace("×", "*")
                .replace("÷", "/")
                .replace("−", "-")
                .replace(" ", "")

            val result = CalculatorEngine.evaluate(expressionForCalc)

            if (result.isNaN() || result.isInfinite()) {
                display.text = "Error"
                speakText("Invalid expression")
                return
            }

            if (learningMode) {
                explainCalculation(expressionForCalc, result)
            } else {
                val formatted = formatResult(result)
                display.text = formatted
                speakText("Result is $formatted")
            }

            val formattedForState = formatResult(result)
            currentExpression = formattedForState
            expression = currentExpression
            currentNumber = formattedForState
            justCalculated = true

            // Save to history
            val originalExpression = lastExpressionBeforeEquals.ifBlank { expressionForCalc }
            val historyEntry = "$originalExpression = $formattedForState"
            try {
                settingsManager.addHistoryEntry(historyEntry)
            } catch (e: Exception) {
                Log.e(TAG, "Error saving history", e)
            }

        } catch (e: Exception) {
            display.text = "Error"
            speakText("Invalid expression")
            Log.e(TAG, "Calculation error", e)
        }
    }

    private fun explainCalculation(expression: String, result: Double) {
        val formatted = formatResult(result)
        val explanation = "Let me solve $expression step by step. The result is $formatted"
        display.text = formatted
        speakText(explanation)
    }

    private fun startVoiceRecognition() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val spokenText = matches[0]
                    processVoiceInput(spokenText)
                }
                stopListening()
            }

            override fun onError(error: Int) {
                val errorMsg = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
                    else -> "Speech recognition error"
                }
                Toast.makeText(this@TalkingCalculatorActivity, errorMsg, Toast.LENGTH_SHORT).show()
                stopListening()
            }

            override fun onReadyForSpeech(params: Bundle?) {
                isListening = true
                Toast.makeText(this@TalkingCalculatorActivity, "Listening...", Toast.LENGTH_SHORT).show()
            }

            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer?.startListening(intent)
    }

    private fun stopListening() {
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
        isListening = false
    }

    private fun processVoiceInput(spokenText: String) {
        val normalizedExpression = convertSpeechToMath(spokenText)
        currentExpression = normalizedExpression
        expression = currentExpression
        updateDisplay()
        Toast.makeText(this, "Heard: $spokenText", Toast.LENGTH_SHORT).show()
        // Auto-evaluate and speak the result after voice input
        if (currentExpression.isNotBlank()) {
            calculateResult()
        }
    }

    private fun convertSpeechToMath(spokenText: String): String {
        var normalized = spokenText.lowercase().trim()

        // Convert number words to digits
        val numberMap = mapOf(
            "zero" to "0", "one" to "1", "two" to "2", "three" to "3", "four" to "4",
            "five" to "5", "six" to "6", "seven" to "7", "eight" to "8", "nine" to "9",
            "ten" to "10", "eleven" to "11", "twelve" to "12", "thirteen" to "13",
            "fourteen" to "14", "fifteen" to "15", "sixteen" to "16", "seventeen" to "17",
            "eighteen" to "18", "nineteen" to "19", "twenty" to "20"
        )

        numberMap.forEach { (word, digit) ->
            normalized = normalized.replace("\\b$word\\b".toRegex(), digit)
        }

        // Convert operators
        normalized = normalized
            .replace(Regex("\\bplus\\b|\\badd\\b"), " + ")
            .replace(Regex("\\bminus\\b|\\bsubtract\\b"), " - ")
            .replace(Regex("\\btimes\\b|\\bmultiply\\b|\\binto\\b"), " * ")
            .replace(Regex("\\bdivide\\b|\\bdivided by\\b|\\bover\\b"), " / ")
            .replace(Regex("\\bpoint\\b|\\bdot\\b"), ".")

        return normalized.trim()
    }

    /**
     * Vibrate for 50ms on button press.
     */
    private fun vibrateShort() {
        if (!hapticEnabled) return

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(50)
            }
        } catch (e: Exception) {
            // Ignore on devices without vibrator
        }
    }

    private fun speakText(text: String) {
        SpeechHelper.speak(text)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startVoiceRecognition()
            } else {
                Toast.makeText(this, "Microphone permission required for voice input", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        SpeechHelper.stop()
        speechRecognizer?.destroy()
    }
}
