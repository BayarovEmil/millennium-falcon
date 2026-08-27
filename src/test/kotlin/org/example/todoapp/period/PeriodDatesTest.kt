package org.example.todoapp.period

import java.time.LocalDate
import java.time.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals

class PeriodDatesTest {

    @Test
    fun `weekStart returns the same Monday regardless of which day in the week is passed`() {
        val monday = LocalDate.of(2026, 9, 14)
        val wednesday = LocalDate.of(2026, 9, 16)
        val sunday = LocalDate.of(2026, 9, 20)

        assertEquals(monday, PeriodDates.weekStart(monday))
        assertEquals(monday, PeriodDates.weekStart(wednesday))
        assertEquals(monday, PeriodDates.weekStart(sunday))
    }

    @Test
    fun `weekEnd is always the Sunday six days after weekStart`() {
        val wednesday = LocalDate.of(2026, 9, 16)
        assertEquals(LocalDate.of(2026, 9, 20), PeriodDates.weekEnd(wednesday))
    }

    @Test
    fun `weekRange returns Monday to Sunday inclusive`() {
        val (start, end) = PeriodDates.weekRange(LocalDate.of(2026, 9, 16))
        assertEquals(LocalDate.of(2026, 9, 14), start)
        assertEquals(LocalDate.of(2026, 9, 20), end)
    }

    @Test
    fun `week entirely inside one month belongs to that month`() {
        // Mon 2026-09-14 .. Sun 2026-09-20, no boundary crossing.
        assertEquals(YearMonth.of(2026, 9), PeriodDates.monthOfWeek(LocalDate.of(2026, 9, 14)))
        assertEquals(YearMonth.of(2026, 9), PeriodDates.monthOfWeek(LocalDate.of(2026, 9, 20)))
    }

    @Test
    fun `year boundary week belongs to January of the following year via its Thursday`() {
        // Mon 2025-12-29 .. Sun 2026-01-04. Thursday is 2026-01-01, so the whole
        // week (even though most of it - Mon/Tue/Wed - sits in December) is owned by January 2026.
        val monday = LocalDate.of(2025, 12, 29)
        val sunday = LocalDate.of(2026, 1, 4)

        assertEquals(LocalDate.of(2026, 1, 1), PeriodDates.thursdayOfWeek(monday))
        assertEquals(YearMonth.of(2026, 1), PeriodDates.monthOfWeek(monday))
        assertEquals(YearMonth.of(2026, 1), PeriodDates.monthOfWeek(sunday))
    }

    @Test
    fun `leap year week spanning February into March belongs to March`() {
        // Mon 2028-02-28 .. Sun 2028-03-05 (2028 is a leap year, so the week
        // contains the leap day 2028-02-29). Thursday is 2028-03-02 -> March.
        val monday = LocalDate.of(2028, 2, 28)
        val leapDay = LocalDate.of(2028, 2, 29)
        val sunday = LocalDate.of(2028, 3, 5)

        assertEquals(LocalDate.of(2028, 3, 2), PeriodDates.thursdayOfWeek(monday))
        assertEquals(YearMonth.of(2028, 3), PeriodDates.monthOfWeek(monday))
        assertEquals(YearMonth.of(2028, 3), PeriodDates.monthOfWeek(leapDay))
        assertEquals(YearMonth.of(2028, 3), PeriodDates.monthOfWeek(sunday))
    }

    @Test
    fun `non-leap year February week ending on the 28th stays in February`() {
        // Mon 2027-02-22 .. Sun 2027-02-28 (2027 is not a leap year, so Feb 28
        // is the last day of the month and also the last day of this week).
        val monday = LocalDate.of(2027, 2, 22)
        val sunday = LocalDate.of(2027, 2, 28)

        assertEquals(LocalDate.of(2027, 2, 25), PeriodDates.thursdayOfWeek(monday))
        assertEquals(YearMonth.of(2027, 2), PeriodDates.monthOfWeek(monday))
        assertEquals(YearMonth.of(2027, 2), PeriodDates.monthOfWeek(sunday))
    }

    @Test
    fun `monthRange returns first and last calendar day of the month`() {
        val (start, end) = PeriodDates.monthRange(YearMonth.of(2026, 2))
        assertEquals(LocalDate.of(2026, 2, 1), start)
        assertEquals(LocalDate.of(2026, 2, 28), end)

        val (leapStart, leapEnd) = PeriodDates.monthRange(YearMonth.of(2028, 2))
        assertEquals(LocalDate.of(2028, 2, 1), leapStart)
        assertEquals(LocalDate.of(2028, 2, 29), leapEnd)
    }

    @Test
    fun `dayRange returns the same date twice`() {
        val date = LocalDate.of(2026, 9, 14)
        assertEquals(date to date, PeriodDates.dayRange(date))
    }
}
