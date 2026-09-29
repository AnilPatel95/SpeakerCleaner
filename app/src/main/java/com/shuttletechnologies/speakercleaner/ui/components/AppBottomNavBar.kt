package com.shuttletechnologies.speakercleaner.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shuttletechnologies.speakercleaner.localization.appStrings
import com.shuttletechnologies.speakercleaner.theme.LocalAppColors

data class BottomNavTabItem(
    val index: Int,
    val title: String,
    val icon: ImageVector
)

@Composable
fun AppBottomNavBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val strings = appStrings()
    val view = LocalView.current

    val tabs = listOf(
        BottomNavTabItem(0, strings.tabClean, Icons.Rounded.WaterDrop),
        BottomNavTabItem(1, strings.tabGenerator, Icons.Rounded.GraphicEq),
        BottomNavTabItem(2, strings.tabDiagnostics, Icons.Rounded.Speed),
        BottomNavTabItem(3, strings.tabHistory, Icons.Rounded.History),
        BottomNavTabItem(4, strings.tabSettings, Icons.Rounded.Settings)
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(260, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(260)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(260, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(260)),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            color = colors.surface,
            tonalElevation = 6.dp,
            border = BorderStroke(1.dp, colors.border.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 2.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEach { item ->
                    val isSelected = selectedTab == item.index
                    val interactionSource = remember { MutableInteractionSource() }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                onTabSelected(item.index)
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isSelected) colors.accent.copy(alpha = 0.20f)
                                    else Color.Transparent
                                )
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                modifier = Modifier.size(22.dp),
                                tint = if (isSelected) colors.accent else colors.textMuted
                            )
                        }

                        Text(
                            text = item.title,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) colors.accent else colors.textMuted,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
