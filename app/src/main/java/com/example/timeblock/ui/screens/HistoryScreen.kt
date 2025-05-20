package com.example.timeblock.ui.screens

import android.app.Activity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.timeblock.data.entity.Entry
import com.example.timeblock.ui.HistoryViewModel
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HistoryApp(viewModelFactory: HistoryViewModel.HistoryViewModelFactory) {
    val viewModel: HistoryViewModel = viewModel(factory = viewModelFactory)
    val entriesState = viewModel.entries.collectAsState()
    HistoryScreen(entries = entriesState.value) {
        (LocalContext.current as? Activity)?.finish()
    }
}

@Composable
fun HistoryScreen(entries: List<Entry>, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "History",
                style = MaterialTheme.typography.headlineMedium
            )
            Button(onClick = onBack) { Text("Back") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LineChart(entries = entries)

        Spacer(modifier = Modifier.height(16.dp))

        HistoryList(entries = entries)
    }
}

@Composable
fun HistoryList(entries: List<Entry>) {
    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneId.systemDefault())
    LazyColumn {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Date", modifier = Modifier.weight(1f))
                Text("Protein", modifier = Modifier.weight(1f))
                Text("Vegetables", modifier = Modifier.weight(1f))
                Text("Steps", modifier = Modifier.weight(1f))
            }
        }
        items(entries) { entry ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formatter.format(entry.timeCreated), modifier = Modifier.weight(1f))
                Text("${entry.proteinGrams}", modifier = Modifier.weight(1f))
                Text("${entry.vegetableServings}", modifier = Modifier.weight(1f))
                Text("${entry.steps}", modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun LineChart(entries: List<Entry>) {
    if (entries.isEmpty()) return
    val proteins = entries.map { it.proteinGrams }
    val vegetables = entries.map { it.vegetableServings }
    val steps = entries.map { it.steps }
    val maxY = listOf(proteins.maxOrNull() ?: 0, vegetables.maxOrNull() ?: 0, steps.maxOrNull() ?: 0).maxOrNull() ?: 0
    if (maxY == 0) return
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        val widthStep = size.width / (entries.size - 1).coerceAtLeast(1)
        fun drawLine(values: List<Int>, color: Color) {
            val path = Path()
            values.forEachIndexed { index, value ->
                val x = index * widthStep
                val y = size.height - (value / maxY.toFloat()) * size.height
                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }
            drawPath(
                path,
                color = color,
                style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
        drawLine(proteins, Color.Red)
        drawLine(vegetables, Color.Green)
        drawLine(steps, Color.Blue)
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        LegendItem(color = Color.Red, label = "Protein")
        LegendItem(color = Color.Green, label = "Veg")
        LegendItem(color = Color.Blue, label = "Steps")
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(12.dp)) {
            drawRect(color = color)
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}