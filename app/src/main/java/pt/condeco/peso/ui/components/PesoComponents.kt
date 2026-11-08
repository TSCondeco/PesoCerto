package pt.condeco.peso.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.condeco.peso.ui.theme.SemanticColors

/** The app's standard surface card. Use [outlined] for flat cards with a hairline border instead of elevation. */
@Composable
fun PesoCard(
    modifier: Modifier = Modifier,
    outlined: Boolean = false,
    contentPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (outlined) 0.dp else 2.dp),
        shape = RoundedCornerShape(12.dp),
        border = if (outlined) BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant) else null
    ) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}

/** Small muted label, typically an upper-case card heading. */
@Composable
fun CaptionText(text: String, modifier: Modifier = Modifier, fontSize: TextUnit = 12.sp, bold: Boolean = false) {
    Text(
        text = text,
        modifier = modifier,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = fontSize,
        fontWeight = if (bold) FontWeight.Bold else null
    )
}

/** Pill showing a weight change, tinted green when it moves toward the goal and red otherwise. */
@Composable
fun ChangeChip(text: String, towardGoal: Boolean, isDecrease: Boolean, caption: String? = null) {
    val tint: Color = SemanticColors.forChange(towardGoal)
    Surface(color = tint.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (isDecrease) Icons.AutoMirrored.Filled.TrendingDown else Icons.AutoMirrored.Filled.TrendingUp,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text, color = tint, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            if (caption != null) {
                Spacer(modifier = Modifier.width(4.dp))
                CaptionText(caption)
            }
        }
    }
}
