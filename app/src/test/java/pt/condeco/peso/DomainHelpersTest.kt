package pt.condeco.peso

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.condeco.peso.domain.BiologicalSex
import pt.condeco.peso.domain.BmiCategory
import pt.condeco.peso.domain.ThemeMode
import pt.condeco.peso.domain.UnitSystem
import pt.condeco.peso.domain.computeBmi
import pt.condeco.peso.domain.isTowardGoal
import pt.condeco.peso.ui.components.handleInput
import pt.condeco.peso.ui.components.step

class DomainHelpersTest {
    @Test fun towardGoalWhenAboveGoal() {
        assertTrue(isTowardGoal(80f, 70f, -0.5f))
        assertFalse(isTowardGoal(80f, 70f, 0.5f))
    }

    @Test fun towardGoalWhenBelowGoal() {
        assertTrue(isTowardGoal(60f, 70f, 0.5f))
        assertFalse(isTowardGoal(60f, 70f, -0.5f))
    }

    @Test fun anyChangeAtGoalIsGood() {
        assertTrue(isTowardGoal(70f, 70f, 1f))
    }

    @Test fun enumsFallBackToDefaults() {
        assertEquals(UnitSystem.Metric, UnitSystem.fromName(null))
        assertEquals(UnitSystem.Imperial, UnitSystem.fromName("Imperial"))
        assertEquals(ThemeMode.System, ThemeMode.fromName("bogus"))
        assertEquals(BiologicalSex.Female, BiologicalSex.fromName("Female"))
    }

    @Test fun bmiCategories() {
        assertEquals(BmiCategory.Underweight, BmiCategory.of(18.4f))
        assertEquals(BmiCategory.Normal, BmiCategory.of(22f))
        assertEquals(BmiCategory.Overweight, BmiCategory.of(27f))
        assertEquals(BmiCategory.Obese, BmiCategory.of(31f))
    }

    @Test fun bmiHandlesInvalidHeight() {
        assertEquals(0f, computeBmi(70f, 0), 0f)
        assertEquals(24.69f, computeBmi(80f, 180), 0.01f)
    }

    @Test fun keypadInput() {
        assertEquals("5", handleInput("0", "5"))
        assertEquals("75", handleInput("7", "5"))
        assertEquals("7.", handleInput("7", "."))
        assertEquals("7.5", handleInput("7.5", "."))
        assertEquals("0", handleInput("7", "del"))
        assertEquals("7", handleInput("75", "del"))
    }

    @Test fun stepClampsAtZero() {
        assertEquals("70.1", step("70.0", 0.1f))
        assertEquals("0.0", step("0.0", -0.1f))
    }
}
