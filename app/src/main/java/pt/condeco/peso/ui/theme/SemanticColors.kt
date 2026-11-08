package pt.condeco.peso.ui.theme

import androidx.compose.ui.graphics.Color

/** Colors with fixed meaning that must not change between light and dark themes. */
object SemanticColors {
    val Positive = Color(0xFF4CAF50)
    val Negative = Color(0xFFF44336)
    val Danger = Color(0xFFC62828)
    val Streak = Color(0xFFE65100)
    val Milestone = Color(0xFF009688)

    fun forChange(towardGoal: Boolean): Color = if (towardGoal) Positive else Negative
}
