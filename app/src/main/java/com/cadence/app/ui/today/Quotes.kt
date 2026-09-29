package com.cadence.app.ui.today

import java.time.LocalDate

object Quotes {
    private val daily = listOf(
        "Small steps every day add up to big change.",
        "You don't have to be extreme, just consistent.",
        "Done is better than perfect.",
        "Motivation gets you started. Habit keeps you going.",
        "The best time to start was yesterday. The next best time is now.",
        "Focus on the step in front of you, not the whole staircase.",
        "A little progress each day is still progress.",
        "Discipline is choosing what you want most over what you want now.",
        "Your future is built by what you do today.",
        "Make today so awesome that yesterday gets jealous.",
        "Success is the sum of small efforts, repeated.",
        "Don't count the days. Make the days count.",
        "Start where you are. Use what you have. Do what you can.",
        "It always seems impossible until it's done.",
        "Energy flows where attention goes.",
        "One day or day one. You decide.",
        "Great things are done by a series of small things brought together.",
        "Be stubborn about your goals, flexible about your methods.",
        "What you do every day matters more than what you do once in a while.",
        "The secret of getting ahead is getting started.",
        "Slow progress is better than no progress.",
        "Win the morning, win the day.",
        "You are one decision away from a different life.",
        "Keep going. Everything you need will come at the perfect time.",
        "Action is the foundational key to all success.",
        "Either you run the day, or the day runs you.",
        "Dream big. Start small. Act now.",
        "Consistency compounds.",
        "Show up for yourself today.",
        "Rome wasn't built in a day, but they were laying bricks every hour.",
        "First we make our habits, then our habits make us.",
    )

    fun forToday(): String = daily[LocalDate.now().dayOfYear % daily.size]
}
