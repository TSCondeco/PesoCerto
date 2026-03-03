package pt.condeco.peso.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weight_entries")
data class WeightEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val weight: Float,
    val bodyFat: Float? = null,
    val date: Long = System.currentTimeMillis(),
    val note: String? = null
)
