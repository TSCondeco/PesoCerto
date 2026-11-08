package pt.condeco.peso.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import pt.condeco.peso.R
import pt.condeco.peso.domain.BmiCategory
import pt.condeco.peso.ui.components.CaptionText
import pt.condeco.peso.ui.components.PesoCard
import pt.condeco.peso.ui.util.label

private const val BMI_SCALE_MIN = 15f
private const val BMI_SCALE_MAX = 35f

@Composable
fun BmiCard(bmi: Float) {
    val category = BmiCategory.of(bmi)
    val color = category.color()

    PesoCard(modifier = Modifier.fillMaxWidth()) {
        CaptionText(stringResource(R.string.bmi_context))
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(String.format(Locale.getDefault(), "%.1f", bmi), fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                Text(
                    category.label(),
                    color = color,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    fontSize = 12.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
        ) {
            val progress = (bmi - BMI_SCALE_MIN) / (BMI_SCALE_MAX - BMI_SCALE_MIN)
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(color, CircleShape)
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            CaptionText(BmiCategory.UNDERWEIGHT_BELOW.toString(), fontSize = 10.sp)
            CaptionText(BmiCategory.OVERWEIGHT_FROM.toString(), fontSize = 10.sp)
        }
    }
}

@Composable
private fun BmiCategory.label(): String = stringResource(
    when (this) {
        BmiCategory.Underweight -> R.string.underweight
        BmiCategory.Normal -> R.string.normal
        BmiCategory.Overweight -> R.string.overweight
        BmiCategory.Obese -> R.string.obese
    }
)

private fun BmiCategory.color(): Color = when (this) {
    BmiCategory.Underweight -> Color(0xFF2196F3)
    BmiCategory.Normal -> Color(0xFF009688)
    BmiCategory.Overweight -> Color(0xFFFF9800)
    BmiCategory.Obese -> Color(0xFFF44336)
}
