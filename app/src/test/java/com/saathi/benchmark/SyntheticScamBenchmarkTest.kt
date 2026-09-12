package com.saathi.benchmark

import com.saathi.engine.AlertCooldownManager
import com.saathi.engine.ScamDetectionEngine
import com.saathi.model.RiskLevel
import com.saathi.model.ScamEvaluationResult
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Rigorous Synthetic Fraud Benchmark Suite.
 * Executes all 26 synthetic fixtures across English, Devanagari, homoglyphs, leetspeak,
 * zero-width evasion attacks, and benign negative controls.
 *
 * Verifies:
 * 1. 100% True Positive detection across all adversarial variations
 * 2. 0% False Positive rate on benign messages
 * 3. Execution latency under 15ms performance budget
 */
class SyntheticScamBenchmarkTest {

    private lateinit var engine: ScamDetectionEngine
    private lateinit var cooldownManager: AlertCooldownManager

    @Before
    fun setUp() {
        cooldownManager = AlertCooldownManager()
        engine = ScamDetectionEngine(cooldownManager)
    }

    @Test
    fun runFullBenchmarkSuite() {
        var positiveTotal = 0
        var positiveDetected = 0
        var negativeTotal = 0
        var falsePositives = 0
        val latencies = mutableListOf<Double>()

        for (fixture in ScamFixtures.FIXTURES) {
            // Clear cooldown before each fixture to test independent detection capability
            cooldownManager.clear()

            val startNano = System.nanoTime()
            val result = engine.evaluateText(fixture.text, fixture.packageName)
            val elapsedMs = (System.nanoTime() - startNano) / 1_000_000.0
            latencies.add(elapsedMs)

            if (fixture.isScam) {
                positiveTotal++
                assertTrue(
                    "Fixture ${fixture.id} [${fixture.description}] must be detected as a threat",
                    result is ScamEvaluationResult.ThreatDetected
                )
                val threat = result as ScamEvaluationResult.ThreatDetected
                positiveDetected++

                // Verify category if specified
                if (fixture.expectedCategory != null) {
                    assertEquals(
                        "Fixture ${fixture.id} category mismatch",
                        fixture.expectedCategory,
                        threat.category
                    )
                }

                // Verify risk level
                assertEquals(
                    "Fixture ${fixture.id} risk level mismatch",
                    fixture.expectedRiskLevel,
                    threat.riskLevel
                )
            } else {
                negativeTotal++
                if (result !is ScamEvaluationResult.Safe) {
                    falsePositives++
                    fail("Fixture ${fixture.id} [${fixture.description}] produced false positive: $result")
                }
            }
        }

        val accuracy = (positiveDetected.toDouble() / positiveTotal) * 100.0
        val falsePositiveRate = (falsePositives.toDouble() / negativeTotal) * 100.0
        val avgLatency = latencies.average()

        println("==================================================")
        println("SAATHI SYNTHETIC BENCHMARK RESULTS")
        println("==================================================")
        println("Total Test Fixtures   : ${ScamFixtures.FIXTURES.size}")
        println("Scam Positives Tested : $positiveTotal")
        println("Scams Detected        : $positiveDetected ($accuracy%)")
        println("Negative Controls     : $negativeTotal")
        println("False Positives       : $falsePositives ($falsePositiveRate%)")
        println("Average Latency       : ${String.format("%.3f", avgLatency)} ms (Budget: <15.0ms)")
        println("Max Latency           : ${String.format("%.3f", latencies.maxOrNull() ?: 0.0)} ms")
        println("==================================================")

        assertEquals("Scam detection accuracy must be 100%", 100.0, accuracy, 0.001)
        assertEquals("False positive rate must be 0%", 0.0, falsePositiveRate, 0.001)
        assertTrue("Average latency must be within 15ms budget", avgLatency < 15.0)
    }
}
