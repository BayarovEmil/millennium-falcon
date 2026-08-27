package org.example.todoapp.period

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters

/**
 * Calendar math shared by period lookup and lazy-creation logic.
 *
 * Weeks run Monday-Sunday (ISO-8601). A week that straddles two months belongs
 * to whichever month contains its Thursday (the ISO "week owner" convention) -
 * equivalently, whichever month holds 4+ of the week's 7 days.
 */
object PeriodDates {
    fun weekStart(date: LocalDate): LocalDate = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    fun weekEnd(date: LocalDate): LocalDate = weekStart(date).plusDays(6)

    fun weekRange(date: LocalDate): Pair<LocalDate, LocalDate> = weekStart(date) to weekEnd(date)

    /** The Thursday of the ISO week that [date] falls in - determines month ownership. */
    fun thursdayOfWeek(date: LocalDate): LocalDate = weekStart(date).plusDays(3)

    /** The month that owns the week containing [date], per the Thursday-ownership rule. */
    fun monthOfWeek(date: LocalDate): YearMonth = YearMonth.from(thursdayOfWeek(date))

    fun monthRange(month: YearMonth): Pair<LocalDate, LocalDate> = month.atDay(1) to month.atEndOfMonth()

    fun dayRange(date: LocalDate): Pair<LocalDate, LocalDate> = date to date
}
