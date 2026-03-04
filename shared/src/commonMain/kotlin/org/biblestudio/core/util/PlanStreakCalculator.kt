package org.biblestudio.core.util

/**
 * Computes the current reading streak — consecutive completed days ending at
 * the highest day number.
 *
 * @param completedDays day numbers that have been completed (need not be sorted).
 * @return the length of the streak, or 0 if [completedDays] is empty.
 */
fun calculateStreak(completedDays: List<Int>): Int {
    if (completedDays.isEmpty()) return 0
    val sorted = completedDays.sorted()
    var streak = 1
    for (i in sorted.lastIndex downTo 1) {
        if (sorted[i] - sorted[i - 1] == 1) streak++ else break
    }
    return streak
}
