package pt.condeco.peso.ui.util

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import java.io.File

fun shareText(context: Context, text: String, mimeType: String = "text/plain") {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        putExtra(Intent.EXTRA_TEXT, text)
        type = mimeType
    }
    context.startActivity(Intent.createChooser(sendIntent, null))
}

/** Shares [image] as a PNG together with [text]. The file lives in the cache dir and is exposed via FileProvider. */
fun shareImage(context: Context, image: Bitmap, text: String) {
    val dir = File(context.cacheDir, "shared").apply { mkdirs() }
    val file = File(dir, "peso_chart.png")
    file.outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, text)
        clipData = ClipData.newRawUri(null, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(sendIntent, null))
}
