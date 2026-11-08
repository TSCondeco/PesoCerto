package pt.condeco.peso

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.condeco.peso.domain.UnitSystem
import pt.condeco.peso.domain.Units
import pt.condeco.peso.ui.analytics.nearestIndex
import pt.condeco.peso.ui.analytics.pointX

class ChartGeometryTest {
    @Test fun nearestIndexSnapsToClosestPoint() {
        assertEquals(0, nearestIndex(0f, 100f, 5))
        assertEquals(1, nearestIndex(30f, 100f, 5))
        assertEquals(2, nearestIndex(50f, 100f, 5))
        assertEquals(4, nearestIndex(100f, 100f, 5))
    }

    @Test fun nearestIndexClampsOutOfBoundsTouches() {
        assertEquals(0, nearestIndex(-20f, 100f, 5))
        assertEquals(4, nearestIndex(250f, 100f, 5))
    }

    @Test fun nearestIndexHandlesDegenerateInput() {
        assertEquals(0, nearestIndex(10f, 100f, 1))
        assertEquals(0, nearestIndex(10f, 0f, 5))
    }

    @Test fun pointXMatchesNearestIndex() {
        repeat(7) { i -> assertEquals(i, nearestIndex(pointX(i, 7, 300f), 300f, 7)) }
        assertEquals(50f, pointX(0, 1, 100f), 0f)
    }

    @Test fun inputRangesContainTypicalValues() {
        assertTrue(175f in Units.heightInputRange(UnitSystem.Metric))
        assertTrue(69f in Units.heightInputRange(UnitSystem.Imperial))
        assertTrue(70f in Units.goalWeightRange(UnitSystem.Metric))
        assertTrue(154f in Units.goalWeightRange(UnitSystem.Imperial))
    }
}
