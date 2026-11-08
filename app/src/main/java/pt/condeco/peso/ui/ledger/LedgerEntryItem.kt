package pt.condeco.peso.ui.ledger

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import pt.condeco.peso.R
import pt.condeco.peso.data.WeightEntry
import pt.condeco.peso.domain.UnitSystem
import pt.condeco.peso.domain.Units
import pt.condeco.peso.domain.isTowardGoal
import pt.condeco.peso.ui.theme.SemanticColors
import pt.condeco.peso.ui.util.Formats

@Composable
fun LedgerEntryItem(entry: WeightEntry, prevEntry: WeightEntry?, goal: Float, unitSystem: UnitSystem, onDelete: () -> Unit, onEdit: () -> Unit) {
    var showOptions by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().clickable(onClickLabel = stringResource(R.string.edit)) { showOptions = !showOptions }) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.width(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(Formats.day(entry.date), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(Formats.monthShort(entry.date).uppercase(), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${Units.formatWeight(entry.weight, unitSystem)} ${Units.weightLabel(unitSystem)}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                entry.bodyFat?.let {
                    Text("${Formats.decimal(it)}% ${stringResource(R.string.body_fat)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                entry.note?.takeIf { it.isNotBlank() }?.let {
                    Text(it, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
            
            if (prevEntry != null) {
                val diff = entry.weight - prevEntry.weight
                val color = SemanticColors.forChange(isTowardGoal(entry.weight, goal, diff))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (diff <= 0) Icons.Default.ArrowDropDown else Icons.Default.ArrowDropUp,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(Units.formatWeight(abs(diff), unitSystem), color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            } else {
                Text("?", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            }
        }
        
        if (showOptions) {
            Row(
                modifier = Modifier.fillMaxWidth().height(48.dp).background(MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight().clickable(role = Role.Button) { 
                        onEdit()
                        showOptions = false
                    },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit), tint = MaterialTheme.colorScheme.secondary)
                }
                Box(
                    modifier = Modifier.width(64.dp).fillMaxHeight().background(SemanticColors.Danger).clickable(role = Role.Button) {
                        showOptions = false
                        onDelete()
                    },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete), tint = Color.White)
                }
            }
        }
    }
}
