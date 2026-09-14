package pl.netbio.internetusageanalyzer.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.netbio.internetusageanalyzer.ui.theme.*

val GlassBackground = Color.White.copy(alpha = 0.06f)
val GlassBackgroundHover = Color.White.copy(alpha = 0.10f)
val GlassBorder = Brush.linearGradient(
    colors = listOf(
        Color.White.copy(alpha = 0.08f),
        Color.White.copy(alpha = 0.22f),
        Color.White.copy(alpha = 0.90f)
    ),
    start = Offset(0f, Float.POSITIVE_INFINITY),
    end = Offset(Float.POSITIVE_INFINITY, 0f)
)
val GlassBorderSubtle = Brush.linearGradient(
    colors = listOf(
        Color.White.copy(alpha = 0.05f),
        Color.White.copy(alpha = 0.15f),
        Color.White.copy(alpha = 0.60f)
    ),
    start = Offset(0f, Float.POSITIVE_INFINITY),
    end = Offset(Float.POSITIVE_INFINITY, 0f)
)
val GlassGradientFill = Brush.horizontalGradient(
    colors = listOf(AccentBlue, AccentPurple)
)
val GlassActiveGradient = Brush.linearGradient(
    colors = listOf(Color.White, Color.White.copy(alpha = 0.5f))
)

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    padding: Dp = 24.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Column(
        modifier = modifier
            .shadow(
                elevation = 16.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.35f),
                spotColor = Color.Black.copy(alpha = 0.35f)
            )
            .clip(shape)
            .then(
                if (onClick != null) Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ) else Modifier
            )
            .background(GlassBackground)
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
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = modifier
            .shadow(
                elevation = 12.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.30f),
                spotColor = Color.Black.copy(alpha = 0.30f)
            )
            .clip(shape)
            .then(
                if (onClick != null) Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ) else Modifier
            )
            .background(GlassBackground)
            .border(1.dp, GlassBorder, shape)
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )
        if (delta != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (deltaPositive) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = if (deltaPositive) SuccessGreen else ErrorRed,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = delta,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (deltaPositive) SuccessGreen else ErrorRed
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
    trackHeight: Dp = 12.dp,
    gradientBrush: Brush = GlassGradientFill
) {
    val shape = RoundedCornerShape(999.dp)
    Column(modifier = modifier) {
        if (label.isNotEmpty() || percentageText.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = percentageText,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight)
                .clip(shape)
                .background(GlassBackground)
                .border(1.dp, GlassBorderSubtle, shape)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = progress.coerceIn(0f, 1f))
                    .clip(shape)
                    .background(gradientBrush)
                    .shadow(
                        elevation = 8.dp,
                        shape = shape,
                        ambientColor = AccentBlue.copy(alpha = 0.4f),
                        spotColor = AccentBlue.copy(alpha = 0.4f)
                    )
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
    gradientBrush: Brush = GlassGradientFill
) {
    val shape = CircleShape
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxSize(),
            color = AccentBlue,
            trackColor = GlassBackground,
            strokeWidth = strokeWidth,
            strokeCap = StrokeCap.Round
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (value.isNotEmpty()) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            }
            if (label.isNotEmpty()) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
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
    val shape = RoundedCornerShape(28.dp)
    Box(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .shadow(
                elevation = 16.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.45f),
                spotColor = Color.Black.copy(alpha = 0.45f)
            )
            .clip(shape)
            .background(Color.Black.copy(alpha = 0.45f))
            .border(1.dp, GlassBorder, shape)
            .padding(horizontal = 8.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items.forEachIndexed { index, item ->
                GlassNavItemView(
                    item = item,
                    isSelected = selectedItem == index,
                    onClick = { onItemClick(index) }
                )
            }
        }
    }
}

@Composable
private fun GlassNavItemView(
    item: GlassNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color.Transparent,
        animationSpec = tween(200)
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF1A1A1A) else TextTertiary,
        animationSpec = tween(200)
    )
    val shape = RoundedCornerShape(18.dp)

    Column(
        modifier = Modifier
            .clip(shape)
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = contentColor
        )
    }
}

data class GlassNavItem(
    val icon: ImageVector,
    val label: String
)

@Composable
fun GlassTabBar(
    modifier: Modifier = Modifier,
    items: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    val shape = RoundedCornerShape(999.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(Color.Black.copy(alpha = 0.3f))
            .border(1.dp, GlassBorder, shape)
            .padding(5.dp)
    ) {
        Row {
            items.forEachIndexed { index, label ->
                val isSelected = selectedIndex == index
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White else Color.Transparent,
                    animationSpec = tween(200)
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) Color(0xFF1A1A1A) else TextTertiary,
                    animationSpec = tween(200)
                )
                val pillShape = RoundedCornerShape(999.dp)

                Box(
                    modifier = Modifier
                        .clip(pillShape)
                        .background(bgColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onSelect(index) }
                        )
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        color = textColor
                    )
                }
            }
        }
    }
}

@Composable
fun GlassToggle(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val shape = RoundedCornerShape(999.dp)
    val trackColor by animateColorAsState(
        targetValue = if (checked) AccentGreen.copy(alpha = 0.45f) else Color.Black.copy(alpha = 0.35f),
        animationSpec = tween(250)
    )
    val knobOffset by animateFloatAsState(
        targetValue = if (checked) 28f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Box(
        modifier = modifier
            .size(width = 64.dp, height = 36.dp)
            .clip(shape)
            .background(trackColor)
            .border(1.dp, GlassBorder, shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onCheckedChange(!checked) }
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = (3 + knobOffset).dp)
                .size(30.dp)
                .clip(CircleShape)
                .background(Color.White)
                .shadow(4.dp, CircleShape, ambientColor = Color.Black.copy(alpha = 0.4f))
        )
    }
}

@Composable
fun GlassButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    gradient: Brush = GlassGradientFill,
    enabled: Boolean = true
) {
    val shape = RoundedCornerShape(999.dp)
    val alpha = if (enabled) 1f else 0.5f

    Box(
        modifier = modifier
            .graphicsLayer(alpha = alpha)
            .shadow(8.dp, shape, ambientColor = AccentBlue.copy(alpha = 0.3f))
            .clip(shape)
            .background(gradient)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF1A1A1A),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = Color(0xFF1A1A1A)
            )
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
    val shape = RoundedCornerShape(999.dp)
    val bgColor by animateColorAsState(
        targetValue = if (selected) tint.copy(alpha = 0.2f) else GlassBackground,
        animationSpec = tween(200)
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) tint else TextSecondary,
        animationSpec = tween(200)
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(bgColor)
            .border(1.dp, if (selected) SolidColor(tint.copy(alpha = 0.3f)) else GlassBorderSubtle, shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = textColor
            )
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
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(GlassBackground)
            .border(1.dp, GlassBorderSubtle, shape)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            textStyle = TextStyle(
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            ),
            cursorBrush = SolidColor(AccentBlue),
            singleLine = true,
            decorationBox = { innerTextField ->
                if (value.isEmpty() && placeholder.isNotEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextDisabled
                    )
                }
                innerTextField()
            }
        )
        if (trailingIcon != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier
                    .size(20.dp)
                    .then(
                        if (onTrailingClick != null) Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onTrailingClick
                        ) else Modifier
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
    val shape = RoundedCornerShape(20.dp)
    val (accentColor, bgColor) = when (type) {
        GlassAlertType.Success -> SuccessGreen to SuccessGreen.copy(alpha = 0.1f)
        GlassAlertType.Warning -> WarningOrange to WarningOrange.copy(alpha = 0.1f)
        GlassAlertType.Error -> ErrorRed to ErrorRed.copy(alpha = 0.1f)
        GlassAlertType.Info -> InfoBlue to InfoBlue.copy(alpha = 0.1f)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(bgColor)
            .border(1.dp, accentColor.copy(alpha = 0.3f), shape)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon ?: when (type) {
                    GlassAlertType.Success -> Icons.Default.CheckCircle
                    GlassAlertType.Warning -> Icons.Default.Warning
                    GlassAlertType.Error -> Icons.Default.Error
                    GlassAlertType.Info -> Icons.Default.Info
                },
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            if (onDismiss != null) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = TextTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.padding(start = 30.dp)
        )
    }
}

enum class GlassAlertType {
    Success, Warning, Error, Info
}

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
            containerColor = Color.Transparent,
            scrimColor = Color.Black.copy(alpha = 0.6f),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .size(width = 36.dp, height = 4.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(GlassWhite40)
                )
            },
            modifier = modifier
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(DarkCard)
                    .border(1.dp, GlassBorder, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .padding(24.dp),
                content = content
            )
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
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            }
        }
        if (action != null) {
            action()
        }
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
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconTint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            }
        }
        if (trailing != null) {
            trailing()
        }
    }
}

@Composable
fun GlassGlowBackground(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-50).dp, y = (-50).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            GradientStart.copy(alpha = 0.6f),
                            Color.Transparent
                        ),
                        radius = 300f
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(250.dp)
                .offset(x = 200.dp, y = 400.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            AccentPurple.copy(alpha = 0.3f),
                            Color.Transparent
                        ),
                        radius = 250f
                    )
                )
        )
    }
}
