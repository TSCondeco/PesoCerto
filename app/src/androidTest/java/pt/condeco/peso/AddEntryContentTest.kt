package pt.condeco.peso

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import pt.condeco.peso.domain.UnitSystem
import pt.condeco.peso.ui.addentry.AddEntryContent
import pt.condeco.peso.ui.theme.PesoTheme

@RunWith(AndroidJUnit4::class)
class AddEntryContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun string(id: Int) =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id)

    @Test
    fun prefillsLastWeight_andStepperAdjusts() {
        composeRule.setContent {
            PesoTheme(darkTheme = false) {
                AddEntryContent(
                    editingEntry = null,
                    unitSystem = UnitSystem.Metric,
                    lastWeight = 80.0f,
                    onSave = { _, _, _, _ -> }
                )
            }
        }

        composeRule.onNodeWithText("80.0").assertExists()
        composeRule.onNodeWithContentDescription(string(R.string.weight_increase)).performClick()
        composeRule.onNodeWithText("80.1").assertExists()
        composeRule.onNodeWithContentDescription(string(R.string.weight_decrease)).performClick()
        composeRule.onNodeWithText("80.0").assertExists()
    }

    @Test
    fun saveReturnsEnteredNote() {
        var savedNote: String? = null
        var savedWeight = 0f
        composeRule.setContent {
            PesoTheme(darkTheme = false) {
                AddEntryContent(
                    editingEntry = null,
                    unitSystem = UnitSystem.Metric,
                    lastWeight = 72.5f,
                    onSave = { weight, _, _, note ->
                        savedWeight = weight
                        savedNote = note
                    }
                )
            }
        }

        composeRule.onNodeWithText(string(R.string.note_hint)).performTextInput("after run")
        composeRule.onNodeWithText(string(R.string.save_entry)).performClick()

        assertEquals(72.5f, savedWeight, 0.01f)
        assertEquals("after run", savedNote)
        assertNotNull(savedNote)
    }
}
