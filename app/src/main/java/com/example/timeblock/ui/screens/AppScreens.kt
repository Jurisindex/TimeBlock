package com.example.timeblock.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.timeblock.ui.theme.BroccoliGreen
import com.example.timeblock.ui.theme.SteakRed
import com.example.timeblock.ui.theme.StepBlue
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.window.Dialog
import com.example.timeblock.data.entity.Entry
import com.example.timeblock.data.entity.User
import com.example.timeblock.ui.MainViewModel
import com.example.timeblock.ui.HistoryViewModel
import com.example.timeblock.ui.HistoryRange
import com.example.timeblock.util.proteinGoalForWeightString
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toKotlinInstant
import android.app.DatePickerDialog
import java.util.Calendar
import java.time.LocalDate
import kotlin.math.roundToInt

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun UserSetupScreen(onUserCreated: (String, String) -> Unit) {
    var displayName by remember { mutableStateOf("") }
    var weightValue by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedUnit by remember { mutableStateOf("kg") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to TimeBlock",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Please enter your name to get started",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it },
            label = { Text("Display Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = weightValue,
                onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) weightValue = it },
                label = { Text("Weight") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box {
                OutlinedButton(onClick = { expanded = true }) {
                    Text(selectedUnit)
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(text = { Text("kg") }, onClick = { selectedUnit = "kg"; expanded = false })
                    DropdownMenuItem(text = { Text("lbs") }, onClick = { selectedUnit = "lbs"; expanded = false })
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (displayName.isNotBlank()) {
                    val weightString = if (weightValue.isNotBlank() && weightValue.toDoubleOrNull() != null && weightValue.toDouble() > 0) {
                        "$weightValue $selectedUnit"
                    } else {
                        "0"
                    }
                    onUserCreated(displayName, weightString)
                }
            },
            enabled = displayName.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Continue")
        }
    }
}

@Composable
fun HomeScreen(
    user: User,
    trackingData: Entry?,
    currentEditMode: MainViewModel.EditMode?,
    onEditModeSelected: (MainViewModel.EditMode) -> Unit,
    onDismissDialog: () -> Unit,
    onUpdateValue: (Int, Boolean) -> Unit,
    onViewHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    showWeightPrompt: Boolean,
    onWeightSet: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with user info and history button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onOpenSettings) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
            }
            Text(
                text = "Hello, ${user.displayName}",
                style = MaterialTheme.typography.headlineMedium
            )
            val clipboard = LocalClipboardManager.current
            IconButton(onClick = {
                if (trackingData != null) {
                    val ldt = trackingData.timeCreated
                        .toKotlinInstant()
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                    val formattedDate = "%04d-%02d-%02d %02d:%02d".format(
                        ldt.year,
                        ldt.monthNumber,
                        ldt.dayOfMonth,
                        ldt.hour,
                        ldt.minute
                    )
                    val copyText = """
                        Date: $formattedDate
                        Protein: ${trackingData.proteinGrams}g
                        Veggies: ${trackingData.vegetableServings}
                        Steps: ${trackingData.steps}
                    """.trimIndent()
                    clipboard.setText(AnnotatedString(copyText))
                }
            }) {
                Icon(imageVector = Icons.Default.ContentPaste, contentDescription = "Copy")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Today's stats
        if (trackingData != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Today's Progress",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val goal = proteinGoalForWeightString(user.weight)
                        StatItem(
                            label = "Protein",
                            value = "${trackingData.proteinGrams}g/$goal g"
                        )
                        StatItem(
                            label = "Veggies",
                            value = "${trackingData.vegetableServings} serv/5"
                        )
                        StatItem(
                            label = "Steps",
                            value = "${trackingData.steps}/8500"
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onViewHistory, modifier = Modifier.align(Alignment.Start)) {
            Icon(imageVector = Icons.Default.ContentPaste, contentDescription = "View History")
            Spacer(modifier = Modifier.width(8.dp))
            Text("View History")
        }

        // Bottom third with 3 buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TrackingButton(
                label = "Protein",
                modifier = Modifier.weight(1f),
                onClick = { onEditModeSelected(MainViewModel.EditMode.PROTEIN) }
            )

            TrackingButton(
                label = "Veggies",
                modifier = Modifier.weight(1f),
                onClick = { onEditModeSelected(MainViewModel.EditMode.VEGETABLES) }
            )

            TrackingButton(
                label = "Steps",
                modifier = Modifier.weight(1f),
                onClick = { onEditModeSelected(MainViewModel.EditMode.STEPS) }
            )
        }
    }

    // Edit dialog
    if (currentEditMode != null && trackingData != null) {
        EditDialog(
            mode = currentEditMode,
            currentValue = when (currentEditMode) {
                MainViewModel.EditMode.PROTEIN -> trackingData.proteinGrams
                MainViewModel.EditMode.VEGETABLES -> trackingData.vegetableServings
                MainViewModel.EditMode.STEPS -> trackingData.steps
            },
            onDismiss = onDismissDialog,
            onUpdate = onUpdateValue,
            onViewHistory = onViewHistory
        )
    }

    if (showWeightPrompt) {
        WeightDialog(onDismiss = {}, onSet = onWeightSet)
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Visible
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TrackingButton(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Visible
        )
    }
}

@Composable
fun FilterButton(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colors = if (selected) {
        ButtonDefaults.buttonColors()
    } else {
        ButtonDefaults.outlinedButtonColors()
    }
    Button(
        onClick = onClick,
        colors = colors,
        modifier = modifier
    ) {
        Text(label)
    }
}

@Composable
fun EditDialog(
    mode: MainViewModel.EditMode,
    currentValue: Int,
    onDismiss: () -> Unit,
    onUpdate: (Int, Boolean) -> Unit,
    onViewHistory: () -> Unit
) {
    var inputValue by remember(mode) { mutableStateOf("") }
    val title = when (mode) {
        MainViewModel.EditMode.PROTEIN -> "Update Protein"
        MainViewModel.EditMode.VEGETABLES -> "Update Vegetable Servings"
        MainViewModel.EditMode.STEPS -> "Update Steps"
    }

    val unit = when (mode) {
        MainViewModel.EditMode.PROTEIN -> "grams"
        MainViewModel.EditMode.VEGETABLES -> "servings"
        MainViewModel.EditMode.STEPS -> "steps"
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Current: $currentValue $unit",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = inputValue,
                    onValueChange = {
                        // Only allow numbers
                        if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                            inputValue = it
                        }
                    },
                    label = { Text("Value to update") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Add button
                    Button(
                        onClick = {
                            val value = inputValue.toIntOrNull() ?: 0
                            onUpdate(value, true)
                        },
                        modifier = Modifier.weight(1f).padding(end = 4.dp)
                    ) {
                        Text("Add")
                    }

                    // Set button
                    Button(
                        onClick = {
                            val value = inputValue.toIntOrNull() ?: 0
                            onUpdate(value, false)
                        },
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                    ) {
                        Text("Set")
                    }

                    // Cancel button
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp)
                    ) {
                        Text("Back")
                    }
                }
            }
        }
    }
}

@Composable
fun WeightDialog(onDismiss: () -> Unit, onSet: (String) -> Unit) {
    var weightValue by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var unit by remember { mutableStateOf("kg") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "Enter Weight", style = MaterialTheme.typography.headlineSmall)

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = weightValue,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) weightValue = it },
                        label = { Text("Weight") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Box {
                        OutlinedButton(onClick = { expanded = true }) { Text(unit) }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            DropdownMenuItem(text = { Text("kg") }, onClick = { unit = "kg"; expanded = false })
                            DropdownMenuItem(text = { Text("lbs") }, onClick = { unit = "lbs"; expanded = false })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = {
                        val w = if (weightValue.isNotBlank() && weightValue.toDoubleOrNull() != null && weightValue.toDouble() > 0) {
                            "$weightValue $unit"
                        } else {
                            "0"
                        }
                        onSet(w)
                    }) {
                        Text("Set")
                    }
                }
            }
        }
    }
}

@Composable
fun EditEntryDialog(
    entry: Entry,
    onDismiss: () -> Unit,
    onSave: (protein: Int, vegetables: Int, steps: Int) -> Unit,
    onDelete: () -> Unit
) {
    var proteinValue by remember { mutableStateOf(entry.proteinGrams.toString()) }
    var vegValue by remember { mutableStateOf(entry.vegetableServings.toString()) }
    var stepsValue by remember { mutableStateOf(entry.steps.toString()) }

    var showConfirm by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "Edit Entry", style = MaterialTheme.typography.headlineSmall)

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = proteinValue,
                    onValueChange = { if (it.all { c -> c.isDigit() }) proteinValue = it },
                    label = { Text("Protein (g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = vegValue,
                    onValueChange = { if (it.all { c -> c.isDigit() }) vegValue = it },
                    label = { Text("Veggies") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = stepsValue,
                    onValueChange = { if (it.all { c -> c.isDigit() }) stepsValue = it },
                    label = { Text("Steps") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(
                        onClick = { showConfirm = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text("Delete")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(onClick = {
                        onSave(
                            proteinValue.toIntOrNull() ?: entry.proteinGrams,
                            vegValue.toIntOrNull() ?: entry.vegetableServings,
                            stepsValue.toIntOrNull() ?: entry.steps
                        )
                    }) {
                        Text("OK")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )) {
                        Text("Cancel")
                    }
                }
            }
        }
    }

    if (showConfirm) {
        Dialog(onDismissRequest = { showConfirm = false }) {
            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Are you sure you want to delete this entry?")
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Button(onClick = { showConfirm = false }) { Text("No") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            showConfirm = false
                            onDelete()
                        }, colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )) { Text("Yes") }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(user: User, onSave: (String, String) -> Unit, onBack: () -> Unit) {
    var name by remember { mutableStateOf(user.displayName) }
    var weightVal by remember { mutableStateOf(user.weight.takeWhile { it.isDigit() || it == '.' }) }
    var expanded by remember { mutableStateOf(false) }
    var unit by remember { mutableStateOf(if (user.weight.endsWith("lbs")) "lbs" else "kg") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Settings", style = MaterialTheme.typography.headlineMedium)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Display Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = weightVal,
                onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) weightVal = it },
                label = { Text("Weight") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box {
                OutlinedButton(onClick = { expanded = true }) { Text(unit) }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(text = { Text("kg") }, onClick = { unit = "kg"; expanded = false })
                    DropdownMenuItem(text = { Text("lbs") }, onClick = { unit = "lbs"; expanded = false })
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val weightString = if (weightVal.isNotBlank() && weightVal.toDoubleOrNull() != null && weightVal.toDouble() > 0) {
                    "$weightVal $unit"
                } else {
                    "0"
                }
                onSave(name, weightString)
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Save") }
    }
}

@Composable
fun HistoryChart(entries: List<Entry>, modifier: Modifier = Modifier) {
    val maxValue = entries.maxOfOrNull { it.proteinGrams } ?: 0
    val density = LocalDensity.current
    val barWidthPx = with(density) { 16.dp.toPx() }
    val spacePx = with(density) { 4.dp.toPx() }
    val barColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier.height(150.dp).fillMaxWidth()) {
        val maxHeight = size.height
        val totalWidth = entries.size * barWidthPx + (entries.size - 1) * spacePx
        val startX = (size.width - totalWidth).coerceAtLeast(0f) / 2f

        entries.forEachIndexed { index, entry ->
            val barHeight = if (maxValue == 0) 0f else (entry.proteinGrams / maxValue.toFloat()) * maxHeight
            val x = startX + index * (barWidthPx + spacePx)
            drawRect(
                color = barColor,
                topLeft = Offset(x, size.height - barHeight),
                size = Size(barWidthPx, barHeight)
            )
        }
    }
}

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    weight: String,
    onBack: () -> Unit,
    onShowGraphs: () -> Unit
) {
    val entries by viewModel.entries.collectAsState()
    // Always show the full history regardless of previous graph range
    LaunchedEffect(Unit) { viewModel.loadEntries(HistoryRange.MAX, updateCurrentRange = false) }
    var showPicker by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<Entry?>(null) }
    val context = LocalContext.current

    if (showPicker) {
        val calendar = remember { Calendar.getInstance() }
        val dialog = remember {
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    viewModel.addEntry(LocalDate.of(year, month + 1, dayOfMonth))
                    showPicker = false
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).apply { setOnCancelListener { showPicker = false } }
        }
        DisposableEffect(Unit) {
            dialog.show()
            onDispose { }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "History",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val listState = rememberLazyListState()
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(entries) { entry ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Protein: ${entry.proteinGrams}g, Veggies: ${entry.vegetableServings}, Steps: ${entry.steps}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { editingEntry = entry }) {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            val ldt = entry.timeCreated
                                .toKotlinInstant()
                                .toLocalDateTime(TimeZone.currentSystemDefault())
                            val formattedDate = "%04d-%02d-%02d %02d:%02d".format(
                                ldt.year,
                                ldt.monthNumber,
                                ldt.dayOfMonth,
                                ldt.hour,
                                ldt.minute
                            )
                            val clipboard = LocalClipboardManager.current
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Date: $formattedDate",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = weight,
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = {
                                    val copyText = """
                                    Date: $formattedDate
                                    Protein: ${entry.proteinGrams}g
                                    Veggies: ${entry.vegetableServings}
                                    Steps: ${entry.steps}
                                """.trimIndent()
                                    clipboard.setText(AnnotatedString(copyText))
                                }) {
                                    Icon(imageVector = Icons.Default.ContentPaste, contentDescription = "Copy")
                                }
                            }
                        }
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = {
                viewModel.loadEntries(viewModel.currentRange.value, updateCurrentRange = false)
                onShowGraphs()
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(text = "\uD83D\uDCC8 Line Graphs")
        }

        FloatingActionButton(
            onClick = { showPicker = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
        }

        if (editingEntry != null) {
            EditEntryDialog(
                entry = editingEntry!!,
                onDismiss = { editingEntry = null },
                onSave = { p, v, s ->
                    viewModel.updateEntry(editingEntry!!, p, v, s)
                    editingEntry = null
                },
                onDelete = {
                    viewModel.deleteEntry(editingEntry!!)
                    editingEntry = null
                }
            )
        }
    }
}

@Composable
fun LineGraphScreen(viewModel: HistoryViewModel, onBack: () -> Unit) {
    val entries by viewModel.entries.collectAsState()
    val range by viewModel.currentRange.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Line Graphs",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterButton("5d", range == HistoryRange.DAYS_5, modifier = Modifier.weight(1f)) {
                viewModel.loadEntries(HistoryRange.DAYS_5)
            }
            FilterButton("30d", range == HistoryRange.DAYS_30, modifier = Modifier.weight(1f)) {
                viewModel.loadEntries(HistoryRange.DAYS_30)
            }
            FilterButton("MAX", range == HistoryRange.MAX, modifier = Modifier.weight(1f)) {
                viewModel.loadEntries(HistoryRange.MAX)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        MetricLineGraph(
            label = "Protein",
            entries = entries,
            valueSelector = { it.proteinGrams },
            color = SteakRed,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        MetricLineGraph(
            label = "Veggies",
            entries = entries,
            valueSelector = { it.vegetableServings },
            color = BroccoliGreen,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        MetricLineGraph(
            label = "Steps",
            entries = entries,
            valueSelector = { it.steps },
            color = StepBlue,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        )
    }
}

@Composable
fun MetricLineGraph(
    label: String,
    entries: List<Entry>,
    valueSelector: (Entry) -> Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(label, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        SingleLineGraph(entries = entries, valueSelector = valueSelector, color = color, modifier = Modifier.fillMaxWidth().height(100.dp))
    }
}

@Composable
fun SingleLineGraph(
    entries: List<Entry>,
    valueSelector: (Entry) -> Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    val sorted = entries.sortedBy { it.timeCreated }
    val maxValue = sorted.maxOfOrNull { valueSelector(it) }?.coerceAtLeast(1) ?: 1
    val minValue = sorted.minOfOrNull { valueSelector(it) } ?: 0
    val range = (maxValue - minValue).coerceAtLeast(1)

    Column(modifier = modifier) {
        Row(modifier = Modifier.weight(1f)) {
            val step = range / 4f
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(end = 4.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                for (i in 4 downTo 0) {
                    val value = (minValue + step * i).roundToInt()
                    Text(text = "$value", style = MaterialTheme.typography.labelSmall)
                }
            }

            val strokeWidth = with(LocalDensity.current) { 1.dp.toPx() }

            Canvas(modifier = Modifier
                .weight(1f)
                .fillMaxHeight()) {
            if (sorted.isEmpty()) return@Canvas
            val xStep = if (sorted.size > 1) size.width / (sorted.size - 1) else 0f
            val path = Path()

            // Draw horizontal grid lines
            for (i in 0..4) {
                val value = minValue + step * i
                val yLine = size.height - ((value - minValue) / range.toFloat()) * size.height
                drawLine(
                    color = Color.LightGray,
                    start = Offset(0f, yLine),
                    end = Offset(size.width, yLine),
                    strokeWidth = strokeWidth
                )
            }

            sorted.forEachIndexed { index, entry ->
                val x = index * xStep
                val y = size.height - ((valueSelector(entry) - minValue).toFloat() / range.toFloat()) * size.height
                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }

            drawPath(path, color = color, style = Stroke(width = 6f))
        }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            sorted.forEach { entry ->
                val ldt = entry.timeCreated.toKotlinInstant().toLocalDateTime(TimeZone.currentSystemDefault())
                val label = "%02d/%02d".format(ldt.monthNumber, ldt.dayOfMonth)
                Text(label, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
