package pt.condeco.peso.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [WeightEntry::class], version = 2, exportSchema = true)
abstract class WeightDatabase : RoomDatabase() {
    abstract fun weightDao(): WeightDao

    companion object {
        @Volatile
        private var Instance: WeightDatabase? = null

        // Schema is unchanged between v1 and v2, so existing data is kept.
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {}
        }

        fun getDatabase(context: Context): WeightDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context,
                    WeightDatabase::class.java,
                    "weight_database"
                )
                .addMigrations(MIGRATION_1_2)
                .build()
                .also { Instance = it }
            }
        }
    }
}
