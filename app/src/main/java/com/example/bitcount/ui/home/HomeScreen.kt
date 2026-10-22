package com.example.bitcount.ui.home

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitcount.analysis.BitCountFormatter
import com.example.bitcount.model.BitWidthMode
import com.example.bitcount.model.InputBase
import com.example.bitcount.ui.components.BitRowView
import com.example.bitcount.ui.components.CheckBitSection
import com.example.bitcount.ui.components.DetailsExpandableSection
import com.example.bitcount.ui.components.RepresentationRow
import com.example.bitcount.ui.components.StatRow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: BitCountViewModel,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var showCustomWidthDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbarMessage()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "BitCount",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToHistory,
                        modifier = Modifier.semantics { contentDescription = "View calculation history" }
                    ) {
                        Icon(imageVector = Icons.Default.History, contentDescription = null)
                    }
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.semantics { contentDescription = "Open app settings" }
                    ) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = null)
                    }
                    IconButton(
                        onClick = onNavigateToAbout,
                        modifier = Modifier.semantics { contentDescription = "About BitCount" }
                    ) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Header & Subtitle
            Text(
                text = "Analyze the bits inside an integer.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(18.dp))

            // INPUT BASE SELECTOR
            Text(
                text = "INPUT BASE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Select input number base." },
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ElevatedFilterChip(
                    selected = state.inputBase == InputBase.DECIMAL,
                    onClick = { viewModel.onInputBaseChange(InputBase.DECIMAL) },
                    label = {
                        Text(
                            text = "DECIMAL",
                            fontWeight = if (state.inputBase == InputBase.DECIMAL) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.elevatedFilterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )

                ElevatedFilterChip(
                    selected = state.inputBase == InputBase.BINARY,
                    onClick = { viewModel.onInputBaseChange(InputBase.BINARY) },
                    label = {
                        Text(
                            text = "BINARY",
                            fontWeight = if (state.inputBase == InputBase.BINARY) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.elevatedFilterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // VALUE INPUT
            Text(
                text = "VALUE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = state.input,
                onValueChange = { viewModel.onInputChange(it) },
                placeholder = {
                    Text(
                        if (state.inputBase == InputBase.DECIMAL) "e.g. 255, -45, 1024" else "e.g. 11111111, 0b1010"
                    )
                },
                singleLine = false,
                maxLines = 4,
                trailingIcon = {
                    if (state.input.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.onInputChange("") },
                            modifier = Modifier.semantics { contentDescription = "Clear input text" }
                        ) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = null)
                        }
                    }
                },
                isError = state.errorMessage != null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (state.inputBase == InputBase.DECIMAL) KeyboardType.Text else KeyboardType.Ascii,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { viewModel.analyze() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Integer to analyze." }
            )

            if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // BIT WIDTH SELECTOR
            Text(
                text = "BIT WIDTH",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .semantics { contentDescription = "Select analysis bit width." },
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ElevatedFilterChip(
                    selected = state.bitWidthMode is BitWidthMode.Natural,
                    onClick = {
                        showCustomWidthDialog = false
                        viewModel.onBitWidthModeChange(BitWidthMode.Natural)
                    },
                    label = { Text("Natural") },
                    colors = FilterChipDefaults.elevatedFilterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )

                listOf(4, 8, 16, 32, 64).forEach { width ->
                    val isSelected = state.bitWidthMode is BitWidthMode.Fixed && (state.bitWidthMode as BitWidthMode.Fixed).width == width
                    ElevatedFilterChip(
                        selected = isSelected,
                        onClick = {
                            showCustomWidthDialog = false
                            viewModel.onBitWidthModeChange(BitWidthMode.Fixed(width))
                        },
                        label = { Text("$width") },
                        colors = FilterChipDefaults.elevatedFilterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }

                val isCustomSelected = state.bitWidthMode is BitWidthMode.Fixed &&
                    !listOf(4, 8, 16, 32, 64).contains((state.bitWidthMode as BitWidthMode.Fixed).width)

                ElevatedFilterChip(
                    selected = isCustomSelected || showCustomWidthDialog,
                    onClick = { showCustomWidthDialog = true },
                    label = {
                        Text(
                            if (isCustomSelected) "${(state.bitWidthMode as BitWidthMode.Fixed).width} (Custom)" else "Custom"
                        )
                    },
                    colors = FilterChipDefaults.elevatedFilterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }

            // Custom width input row if expanded
            if (showCustomWidthDialog) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = state.customWidthInput,
                        onValueChange = { viewModel.onCustomWidthInputChange(it) },
                        label = { Text("Width (1–2000)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            viewModel.applyCustomWidth()
                            showCustomWidthDialog = false
                        }),
                        modifier = Modifier
                            .weight(1f)
                            .semantics { contentDescription = "Custom bit width input" }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            viewModel.applyCustomWidth()
                            showCustomWidthDialog = false
                        }
                    ) {
                        Text("Apply")
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ANALYZE BUTTON
            Button(
                onClick = { viewModel.analyze() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .semantics { contentDescription = "Analyze binary bits." },
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "ANALYZE BITS",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            // RESULTS SECTION
            val result = state.result
            if (result != null) {
                Spacer(modifier = Modifier.height(24.dp))

                // PRIMARY RESULT CARD: BINARY VALUE
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "BINARY VALUE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.primary
                            )

                            OutlinedButton(
                                onClick = {
                                    val copyText = BitCountFormatter.formatBinaryCopy(
                                        result.binary,
                                        state.settings.binaryPrefixInCopy
                                    )
                                    clipboardManager.setText(AnnotatedString(copyText))
                                    viewModel.triggerHapticClick()
                                    scope.launch { snackbarHostState.showSnackbar("Copied") }
                                },
                                contentPadding = ButtonDefaults.TextButtonContentPadding,
                                modifier = Modifier.semantics { contentDescription = "Copy binary representation" }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                Text("COPY", fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val formattedBinary = BitCountFormatter.formatBinary(
                            result.binary,
                            state.settings.groupBinaryBits
                        )

                        Text(
                            text = formattedBinary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.semantics { contentDescription = "Binary representation." }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${result.analyzedBitLength} BITS",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SECOND CARD: BIT ANALYSIS
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "BIT ANALYSIS",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        if (result.isNegative) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Bit statistics shown for absolute value.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (result.bitWidthMode is BitWidthMode.Fixed) {
                            StatRow(
                                label = "Width",
                                value = "${result.analyzedBitLength} bits",
                                accessibilityDescription = "Selected bit width is ${result.analyzedBitLength} bits"
                            )
                        }

                        StatRow(
                            label = "Binary Length",
                            value = "${result.analyzedBitLength} bits",
                            accessibilityDescription = "Binary length is ${result.analyzedBitLength} bits"
                        )

                        StatRow(
                            label = "1-Bits",
                            value = "${result.oneBitCount}",
                            accessibilityDescription = "Number of set bits is ${result.oneBitCount}"
                        )

                        StatRow(
                            label = "0-Bits",
                            value = "${result.zeroBitCount}",
                            accessibilityDescription = "Number of clear bits is ${result.zeroBitCount}"
                        )

                        StatRow(
                            label = "Highest Set Bit",
                            value = result.highestSetBit?.let { "Bit $it" } ?: "None",
                            accessibilityDescription = "Highest set bit position is ${result.highestSetBit?.let { "Bit $it" } ?: "None"}"
                        )

                        StatRow(
                            label = "Lowest Set Bit",
                            value = result.lowestSetBit?.let { "Bit $it" } ?: "None",
                            accessibilityDescription = "Lowest set bit position is ${result.lowestSetBit?.let { "Bit $it" } ?: "None"}"
                        )

                        StatRow(
                            label = "Bit Density",
                            value = BitCountFormatter.formatDensity(result.bitDensityPercent),
                            accessibilityDescription = "Percentage of bits that are set is ${BitCountFormatter.formatDensity(result.bitDensityPercent)}"
                        )

                        if (result.bitWidthMode is BitWidthMode.Fixed) {
                            StatRow(
                                label = "Natural Length",
                                value = "${result.naturalBitLength} bits",
                                accessibilityDescription = "Natural binary length is ${result.naturalBitLength} bits"
                            )
                            StatRow(
                                label = "Unused Leading Bits",
                                value = "${result.analyzedBitLength - result.naturalBitLength}",
                                accessibilityDescription = "Unused leading bits count is ${result.analyzedBitLength - result.naturalBitLength}"
                            )
                        }
                    }
                }

                // BIT POSITIONS VISUALIZATION
                if (state.settings.showBitPositions) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "BIT POSITIONS",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            BitRowView(
                                magnitudeBinary = result.magnitudeBinary,
                                analyzedBitLength = result.analyzedBitLength,
                                showPositions = true,
                                grouping = state.settings.groupBinaryBits
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // NUMBER REPRESENTATIONS CARD
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "NUMBER REPRESENTATIONS",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        RepresentationRow(
                            title = "Decimal",
                            value = result.normalizedDecimal,
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(result.normalizedDecimal))
                                viewModel.triggerHapticClick()
                                scope.launch { snackbarHostState.showSnackbar("Copied Decimal") }
                            }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        RepresentationRow(
                            title = "Binary",
                            value = result.binary,
                            onCopy = {
                                val copyText = BitCountFormatter.formatBinaryCopy(
                                    result.binary,
                                    state.settings.binaryPrefixInCopy
                                )
                                clipboardManager.setText(AnnotatedString(copyText))
                                viewModel.triggerHapticClick()
                                scope.launch { snackbarHostState.showSnackbar("Copied Binary") }
                            }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        RepresentationRow(
                            title = "Octal",
                            value = result.octal,
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(result.octal))
                                viewModel.triggerHapticClick()
                                scope.launch { snackbarHostState.showSnackbar("Copied Octal") }
                            }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        RepresentationRow(
                            title = "Hexadecimal",
                            value = result.hexadecimal,
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(result.hexadecimal))
                                viewModel.triggerHapticClick()
                                scope.launch { snackbarHostState.showSnackbar("Copied Hexadecimal") }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ACTION ROW: [ COPY ALL ] [ SHARE ] [ SAVE ] [ CLEAR ]
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val copyAll = BitCountFormatter.formatCopyAll(result)
                            clipboardManager.setText(AnnotatedString(copyAll))
                            viewModel.triggerHapticClick()
                            scope.launch { snackbarHostState.showSnackbar("Copied all statistics") }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                        Text("COPY ALL", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            val shareReport = BitCountFormatter.formatShare(result)
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareReport)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Share BitCount Analysis")
                            context.startActivity(shareIntent)
                            viewModel.triggerHapticClick()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                        Text("SHARE", fontSize = 12.sp)
                    }

                    ElevatedButton(
                        onClick = { viewModel.saveCurrentAnalysis() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                        Text("SAVE", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { viewModel.clear() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                        Text("CLEAR", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SHOW DETAILS
                DetailsExpandableSection(result = result)

                Spacer(modifier = Modifier.height(16.dp))

                // CHECK BIT
                CheckBitSection(
                    bitPositionInput = state.bitPositionInput,
                    bitPositionResult = state.bitPositionResult,
                    bitPositionError = state.bitPositionError,
                    onBitPositionChange = { viewModel.onBitPositionInputChange(it) },
                    onCheck = { viewModel.checkBitPosition() }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
