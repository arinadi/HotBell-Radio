package com.hotbell.radio.ui.wakeup

import kotlin.random.Random

data class MathChallenge(
    val question: String,
    val options: List<Int>,
    val correctIndex: Int
)

object MathChallengeGenerator {
    fun generate(): MathChallenge {
        val type = Random.nextInt(3) // 0: add, 1: sub, 2: mul
        
        val a: Int
        val b: Int
        val answer: Int
        val symbol: String

        when (type) {
            0 -> { // Addition
                a = Random.nextInt(10, 100)
                b = Random.nextInt(10, 100)
                answer = a + b
                symbol = "+"
            }
            1 -> { // Subtraction
                a = Random.nextInt(30, 100)
                b = Random.nextInt(10, a) // Ensure positive result
                answer = a - b
                symbol = "-"
            }
            else -> { // Multiplication
                a = Random.nextInt(3, 12)
                b = Random.nextInt(3, 12)
                answer = a * b
                symbol = "×"
            }
        }

        val question = "$a $symbol $b"
        
        // Generate wrong answers that look plausible
        val wrongAnswers = mutableSetOf<Int>()
        while (wrongAnswers.size < 3) {
            val offset = Random.nextInt(-10, 11)
            val wrong = answer + offset
            if (offset != 0 && wrong > 0) {
                wrongAnswers.add(wrong)
            }
        }

        val allOptions = wrongAnswers.toList() + answer
        val shuffledOptions = allOptions.shuffled()
        val correctIndex = shuffledOptions.indexOf(answer)

        return MathChallenge(question, shuffledOptions, correctIndex)
    }
}
