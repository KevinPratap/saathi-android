package com.saathi.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

@OptIn(ExperimentalCoroutinesApi::class)
class EventDebouncerTest {

    @Test
    fun testEventCollapsing_MultipleRapidCallsTriggerOnlyOnce() = runBlocking {
        val debouncer = EventDebouncer(debounceDelayMs = 100L)
        val executionCount = AtomicInteger(0)

        // Rapid fire 5 events within 20ms of each other (well within 100ms window)
        for (i in 1..5) {
            debouncer.debounce("test_key") {
                executionCount.incrementAndGet()
            }
            delay(10)
        }

        // Wait for debounce delay to fully settle
        delay(150)

        assertEquals("Only the last collapsed event should execute", 1, executionCount.get())
        debouncer.cancelAll()
    }

    @Test
    fun testDistinctKeys_ExecuteIndependently() = runBlocking {
        val debouncer = EventDebouncer(debounceDelayMs = 80L)
        val countKey1 = AtomicInteger(0)
        val countKey2 = AtomicInteger(0)

        debouncer.debounce("key_1") {
            countKey1.incrementAndGet()
        }
        debouncer.debounce("key_2") {
            countKey2.incrementAndGet()
        }

        delay(130)

        assertEquals(1, countKey1.get())
        assertEquals(1, countKey2.get())
        debouncer.cancelAll()
    }

    @Test
    fun testExplicitCancellation_PreventsExecution() = runBlocking {
        val debouncer = EventDebouncer(debounceDelayMs = 100L)
        val executed = AtomicInteger(0)

        debouncer.debounce("cancel_key") {
            executed.incrementAndGet()
        }

        // Cancel before 100ms
        delay(30)
        debouncer.cancel("cancel_key")

        delay(120)
        assertEquals("Cancelled event must not execute", 0, executed.get())
        debouncer.cancelAll()
    }
}
