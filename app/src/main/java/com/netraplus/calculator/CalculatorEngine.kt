package com.netraplus.calculator

import java.util.*
import kotlin.math.round

/**
 * Professional calculator engine using shunting-yard algorithm
 * for reliable expression evaluation with operator precedence.
 */
object CalculatorEngine {
    /**
     * Evaluate a mathematical expression string.
     * Supports: +, -, ×, ÷, decimals, operator precedence
     * @param expression The expression to evaluate
     * @return The result as Double, or Double.NaN on error
     */
    fun evaluate(expression: String): Double {
        if (expression.isEmpty()) return 0.0
        
        try {
            val tokens = tokenize(expression)
            if (tokens.isEmpty()) return 0.0
            
            val postfix = toPostfix(tokens)
            return evalPostfix(postfix)
        } catch (e: Exception) {
            return Double.NaN
        }
    }

    /**
     * Tokenize expression into numbers and operators.
     */
    private fun tokenize(expr: String): List<String> {
        val out = mutableListOf<String>()
        var i = 0
        
        while (i < expr.length) {
            val c = expr[i]
            when {
                c.isWhitespace() -> i++
                c.isDigit() || c == '.' -> {
                    val sb = StringBuilder()
                    while (i < expr.length && (expr[i].isDigit() || expr[i] == '.')) {
                        sb.append(expr[i])
                        i++
                    }
                    out.add(sb.toString())
                }
                c == '+' || c == '-' || c == '×' || c == '÷' || c == '*' || c == '/' -> {
                    out.add(c.toString())
                    i++
                }
                else -> i++
            }
        }
        return out
    }

    /**
     * Get operator precedence (higher number = higher precedence).
     */
    private fun precedence(op: String): Int = when (op) {
        "+", "-" -> 1
        "×", "*", "÷", "/" -> 2
        else -> 0
    }

    /**
     * Convert infix notation to postfix (shunting-yard algorithm).
     */
    private fun toPostfix(tokens: List<String>): List<String> {
        val output = mutableListOf<String>()
        val stack = Stack<String>()
        
        for (t in tokens) {
            if (t.matches(Regex("\\d+(\\.\\d+)?"))) {
                // Number: add to output
                output.add(t)
            } else {
                // Operator: pop higher or equal precedence operators
                while (!stack.isEmpty() && precedence(stack.peek()) >= precedence(t)) {
                    output.add(stack.pop())
                }
                stack.push(t)
            }
        }
        
        // Pop remaining operators
        while (!stack.isEmpty()) {
            output.add(stack.pop())
        }
        
        return output
    }

    /**
     * Evaluate postfix expression.
     */
    private fun evalPostfix(postfix: List<String>): Double {
        val stack = Stack<Double>()
        
        for (t in postfix) {
            if (t.matches(Regex("\\d+(\\.\\d+)?"))) {
                // Number: push to stack
                stack.push(t.toDouble())
            } else {
                // Operator: pop two operands, compute, push result
                if (stack.isEmpty()) return Double.NaN
                val b = stack.pop()
                
                if (stack.isEmpty()) {
                    // Handle unary minus
                    if (t == "-") {
                        stack.push(-b)
                        continue
                    }
                    return Double.NaN
                }
                
                val a = stack.pop()
                val res = when (t) {
                    "+" -> a + b
                    "-" -> a - b
                    "×", "*" -> a * b
                    "÷", "/" -> {
                        if (b == 0.0) {
                            return Double.NaN // Division by zero
                        }
                        a / b
                    }
                    else -> Double.NaN
                }
                
                if (res.isNaN() || res.isInfinite()) {
                    return Double.NaN
                }
                
                stack.push(res)
            }
        }
        
        return if (stack.isEmpty()) {
            0.0
        } else {
            val result = stack.pop()
            // Round to sensible precision for display
            round(result * 1e10) / 1e10
        }
    }
}
