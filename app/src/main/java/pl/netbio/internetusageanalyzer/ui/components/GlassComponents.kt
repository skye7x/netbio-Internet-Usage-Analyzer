package pl.netbio.internetusageanalyzer.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.minimumInteractiveComponentSize
import pl.netbio.internetusageanalyzer.ui.theme.*

val GlassCardShape = RoundedCornerShape(20.dp)
val PillShape = RoundedCornerShape(999.dp)

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    padding: Dp = 20.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Column(
        modifier = modifier
            .shadow(elevation = 8.dp, shape = shape, ambientColor = Color.Black.copy(alpha = 0.5f))
            .clip(shape)
            .then(
                if (onClick != null) Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ) else Modifier
            )
            .background(GlassMid)
            .border(1.dp, GlassBorder, shape)
            .padding(padding),
        content = content
    )
}

@Composable
fun GlassStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    delta: String? = null,
    deltaPositive: Boolean = true,
    icon: ImageVector? = null,
    iconTint: Color = AccentBlue,
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .shadow(elevation = 4.dp, shape = shape, ambientColor = Color.Black.copy(alpha = 0.4f))
            .clip(shape)
            .then(
                if (onClick != null) Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ) else Modifier
            )
            .background(GlassMid)
            .border(1.dp, GlassBorder, shape)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextTertiary)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )
        if (delta != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (deltaPositive) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = if (deltaPositive) ErrorRed else SuccessGreen,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = delta,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (deltaPositive) ErrorRed else SuccessGreen
                )
            }
        }
    }
}

@Composable
fun GlassProgressBar(
    modifier: Modifier = Modifier,
    progress: Float,
    label: String = "",
    percentageText: String = "",
    trackHeight: Dp = 10.dp,
    fillColor: Color = AccentBlue
) {
    val shape = PillShape
    Column(modifier = modifier) {
        if (label.isNotEmpty() || percentageText.isNotEmpty()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Text(text = percentageText, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        Box(
            modifier = Modifier.fillMaxWidth().height(trackHeight).clip(shape)
                .background(GlassLow).border(1.dp, GlassBorder, shape)
        ) {
            Box(
                modifier = Modifier.fillMaxHeight().fillMaxWidth(fraction = progress.coerceIn(0f, 1f))
                    .clip(shape).background(fillColor)
            )
        }
    }
}

@Composable
fun GlassCircularProgress(
    modifier: Modifier = Modifier,
    progress: Float,
    size: Dp = 120.dp,
    strokeWidth: Dp = 10.dp,
    label: String = "",
    value: String = "",
    fillColor: Color = AccentBlue
) {
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxSize(),
            color = fillColor,
            trackColor = GlassLow,
            strokeWidth = strokeWidth,
            strokeCap = StrokeCap.Round
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (value.isNotEmpty()) {
                Text(text = value, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
            }
            if (label.isNotEmpty()) {
                Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
            }
        }
    }
}

@Composable
fun GlassBottomNavigation(
    modifier: Modifier = Modifier,
    items: List<GlassNavItem>,
    selectedItem: Int,
    onItemClick: (Int) -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            .shadow(elevation = 12.dp, shape = shape, ambientColor = Color.Black.copy(alpha = 0.6f))
            .clip(shape).background(DarkCard.copy(alpha = 0.95f))
            .border(1.dp, GlassBorder, shape)
            .padding(horizontal = 6.dp, vertical = 8.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            items.forEachIndexed { index, item ->
                GlassNavItemView(item = item, isSelected = selectedItem == index, onClick = { onItemClick(index) })
            }
        }
    }
}

@Composable
private fun GlassNavItemView(item: GlassNavItem, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor by animateColorAsState(targetValue = if (isSelected) AccentBlue.copy(alpha = 0.15f) else Color.Transparent, animationSpec = tween(200))
    val contentColor by animateColorAsState(targetValue = if (isSelected) AccentBlue else TextTertiary, animationSpec = tween(200))
    val shape = RoundedCornerShape(14.dp)

    Column(
        modifier = Modifier.clip(shape).background(bgColor)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(imageVector = item.icon, contentDescription = item.label, tint = contentColor, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = item.label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold), color = contentColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

data class GlassNavItem(val icon: ImageVector, val label: String)

@Composable
fun GlassTabBar(
    modifier: Modifier = Modifier,
    items: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    val shape = PillShape
    Box(
        modifier = modifier.clip(shape).background(GlassLow).border(1.dp, GlassBorder, shape).padding(3.dp)
    ) {
        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            items.forEachIndexed { index, label ->
                val isSelected = selectedIndex == index
                val bgColor by animateColorAsState(targetValue = if (isSelected) AccentBlue.copy(alpha = 0.2f) else Color.Transparent, animationSpec = tween(200))
                val textColor by animateColorAsState(targetValue = if (isSelected) AccentBlue else TextTertiary, animationSpec = tween(200))
                Box(
                    modifier = Modifier.clip(PillShape).background(bgColor)
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = { onSelect(index) })
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = label, style = MaterialTheme.typography.labelMedium, color = textColor, maxLines = 1)
                }
            }
        }
    }
}

@Composable
fun GlassToggle(modifier: Modifier = Modifier, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val trackColor by animateColorAsState(targetValue = if (checked) AccentBlue else GlassHigh, animationSpec = tween(250))
    val knobOffset by animateFloatAsState(targetValue = if (checked) 28f else 0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))

    Box(
        modifier = modifier.size(width = 56.dp, height = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.size(width = 56.dp, height = 32.dp).clip(PillShape).background(trackColor)
                .border(1.dp, GlassBorder, PillShape)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = { onCheckedChange(!checked) }),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier.offset(x = (3 + knobOffset).dp).size(26.dp).clip(CircleShape)
                    .background(TextPrimary).shadow(4.dp, CircleShape)
            )
        }
    }
}

@Composable
fun GlassButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    color: Color = AccentBlue
) {
    val shape = PillShape
    val alpha = if (enabled) 1f else 0.5f

    Box(
        modifier = modifier.graphicsLayer(alpha = alpha)
            .shadow(6.dp, shape, ambientColor = color.copy(alpha = 0.2f))
            .clip(shape).background(color)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, enabled = enabled, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(text = text, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold), color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun GlassChip(
    modifier: Modifier = Modifier,
    text: String,
    selected: Boolean = false,
    onClick: () -> Unit = {},
    icon: ImageVector? = null,
    tint: Color = AccentBlue
) {
    val bgColor by animateColorAsState(targetValue = if (selected) tint.copy(alpha = 0.15f) else GlassLow, animationSpec = tween(200))
    val textColor by animateColorAsState(targetValue = if (selected) tint else TextSecondary, animationSpec = tween(200))
    val borderColor by animateColorAsState(targetValue = if (selected) tint.copy(alpha = 0.3f) else GlassBorder, animationSpec = tween(200))

    Box(
        modifier = modifier.clip(PillShape).background(bgColor).border(1.dp, borderColor, PillShape)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, tint = textColor, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(text = text, style = MaterialTheme.typography.labelMedium, color = textColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun GlassInput(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = modifier.fillMaxWidth().clip(shape).background(GlassLow)
            .border(1.dp, GlassBorder, shape).padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            Icon(imageVector = leadingIcon, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(10.dp))
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
            cursorBrush = SolidColor(AccentBlue),
            singleLine = true,
            decorationBox = { innerTextField ->
                if (value.isEmpty() && placeholder.isNotEmpty()) {
                    Text(text = placeholder, style = MaterialTheme.typography.bodyMedium, color = TextDisabled)
                }
                innerTextField()
            }
        )
        if (trailingIcon != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = trailingIcon, contentDescription = null, tint = TextTertiary,
                modifier = Modifier.size(18.dp).then(
                    if (onTrailingClick != null) Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onTrailingClick) else Modifier
                )
            )
        }
    }
}

@Composable
fun GlassAlert(
    modifier: Modifier = Modifier,
    title: String,
    message: String,
    type: GlassAlertType = GlassAlertType.Info,
    icon: ImageVector? = null,
    onDismiss: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(14.dp)
    val accentColor = when (type) {
        GlassAlertType.Success -> SuccessGreen
        GlassAlertType.Warning -> WarningOrange
        GlassAlertType.Error -> ErrorRed
        GlassAlertType.Info -> InfoBlue
    }
    val bgColor = accentColor.copy(alpha = 0.08f)

    Column(
        modifier = modifier.fillMaxWidth().clip(shape).background(bgColor)
            .border(1.dp, accentColor.copy(alpha = 0.2f), shape).padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(10.dp))
            }
            Text(text = title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary, modifier = Modifier.weight(1f))
            if (onDismiss != null) {
                IconButton(onClick = onDismiss, modifier = Modifier.minimumInteractiveComponentSize()) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Dismiss", tint = TextTertiary, modifier = Modifier.size(14.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = message, style = MaterialTheme.typography.bodySmall, color = TextSecondary, modifier = Modifier.padding(start = 28.dp))
    }
}

enum class GlassAlertType { Success, Warning, Error, Info }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassBottomSheet(
    modifier: Modifier = Modifier,
    visible: Boolean,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    if (visible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            containerColor = DarkCard,
            scrimColor = Color.Black.copy(alpha = 0.6f),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = {
                Box(modifier = Modifier.padding(top = 12.dp).size(width = 36.dp, height = 4.dp).clip(PillShape).background(GlassHigh))
            },
            modifier = modifier
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp), content = content)
        }
    }
}

@Composable
fun GlassSectionHeader(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    action: (@Composable () -> Unit)? = null
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = TextTertiary)
            }
        }
        if (action != null) { action() }
    }
}

@Composable
fun GlassListItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color = AccentBlue,
    title: String,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier.fillMaxWidth()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(1.dp))
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = TextTertiary)
            }
        }
        if (trailing != null) { trailing() }
    }
}

@Composable
fun GlassGlowBackground(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.size(250.dp).align(Alignment.TopStart).offset(x = (-60).dp, y = (-60).dp)
                .background(Color(0xFF3B82F6).copy(alpha = 0.04f), CircleShape)
        )
        Box(
            modifier = Modifier.size(200.dp).align(Alignment.BottomEnd).offset(x = 60.dp, y = 60.dp)
                .background(Color(0xFFA855F7).copy(alpha = 0.03f), CircleShape)
        )
    }
}

@Composable
fun GlassMetricRow(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: ImageVector? = null,
    iconTint: Color = AccentBlue
) {
    Row(modifier = modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(10.dp))
        }
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary, modifier = Modifier.weight(1f))
        Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun UsageBarChart(
    modifier: Modifier = Modifier,
    data: List<Pair<String, Float>>,
    maxValue: Float,
    barColor: Color = AccentBlue,
    labelColor: Color = TextTertiary
) {
    val shape = RoundedCornerShape(4.dp)
    Column(modifier = modifier) {
        data.forEach { (label, value) ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(text = label, style = MaterialTheme.typography.labelSmall, color = labelColor, modifier = Modifier.widthIn(min = 32.dp, max = 50.dp))
                Box(modifier = Modifier.weight(1f).height(14.dp).clip(shape).background(GlassLow)) {
                    Box(
                        modifier = Modifier.fillMaxHeight().fillMaxWidth(fraction = if (maxValue > 0) (value / maxValue).coerceIn(0f, 1f) else 0f)
                            .clip(shape).background(barColor)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = String.format("%.1f", value), style = MaterialTheme.typography.labelSmall, color = TextSecondary, modifier = Modifier.widthIn(min = 36.dp, max = 60.dp), textAlign = TextAlign.End)
            }
        }
    }
}

@Composable
fun GlassPieChart(
    modifier: Modifier = Modifier,
    segments: List<PieSegment>,
    size: Dp = 120.dp
) {
    Canvas(modifier = modifier.size(size)) {
        var startAngle = -90f
        segments.forEach { segment ->
            val sweep = (segment.value / segments.sumOf { it.value.toDouble() }).toFloat() * 360f
            drawArc(color = segment.color, startAngle = startAngle, sweepAngle = sweep, useCenter = true, topLeft = Offset.Zero, size = Size(this.size.width, this.size.height))
            startAngle += sweep
        }
    }
}

data class PieSegment(val value: Float, val color: Color, val label: String = "")
