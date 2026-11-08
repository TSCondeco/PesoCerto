package pt.condeco.peso.domain

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import pt.condeco.peso.data.WeightEntry

/** CSV import/export; weights are always in kg with a decimal comma, dates as d/M/yyyy. */
object WeightCsv {
    private val splitRegex = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex()

    fun export(entries: List<WeightEntry>, locale: Locale = Locale.getDefault()): String {
        val dateFormat = SimpleDateFormat("d/M/yyyy", locale)
        val builder = StringBuilder("Data,Peso (kg)\n")
        entries.sortedBy { it.date }.forEach { entry ->
            val weight = "%.1f".format(Locale.ROOT, entry.weight).replace(".", ",")
            builder.append("${dateFormat.format(Date(entry.date))},\"$weight\"\n")
        }
        return builder.toString()
    }

    /** Skips the header and any line that can't be parsed. */
    fun parse(csvContent: String, locale: Locale = Locale.getDefault()): List<WeightEntry> {
        val dateFormat = SimpleDateFormat("d/M/yyyy", locale)
        return csvContent.lines().drop(1).mapNotNull { line ->
            if (line.isBlank()) return@mapNotNull null
            val parts = if (line.contains("\t")) line.split("\t") else line.split(splitRegex)
            if (parts.size < 2) return@mapNotNull null
            try {
                val date = dateFormat.parse(parts[0].trim().removeSurrounding("\""))?.time
                    ?: return@mapNotNull null
                val weight = parts[1].trim().removeSurrounding("\"").replace(",", ".").toFloat()
                WeightEntry(weight = weight, date = date)
            } catch (e: Exception) {
                null
            }
        }
    }
}
