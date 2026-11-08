package pt.condeco.peso

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import pt.condeco.peso.data.WeightDatabase

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        WeightDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate1To2_keepsExistingEntries() {
        helper.createDatabase(DB_NAME, 1).apply {
            execSQL("INSERT INTO weight_entries (weight, bodyFat, date, note) VALUES (80.5, 18.0, 1700000000000, 'hello')")
            close()
        }

        val db = helper.runMigrationsAndValidate(DB_NAME, 2, true, WeightDatabase.MIGRATION_1_2)

        db.query("SELECT weight, bodyFat, date, note FROM weight_entries").use { cursor ->
            assertEquals(1, cursor.count)
            cursor.moveToFirst()
            assertEquals(80.5, cursor.getDouble(0), 0.001)
            assertEquals(18.0, cursor.getDouble(1), 0.001)
            assertEquals(1700000000000L, cursor.getLong(2))
            assertEquals("hello", cursor.getString(3))
        }
    }

    private companion object {
        const val DB_NAME = "migration-test"
    }
}
