package com.shuttletechnologies.speakercleaner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shuttletechnologies.speakercleaner.localization.appStrings
import com.shuttletechnologies.speakercleaner.theme.LocalAppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    currentTab: Int,
    scrollBehavior: TopAppBarScrollBehavior,
    isCleaningActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val strings = appStrings()

    val (title, subtitle) = when (currentTab) {
        0 -> strings.appName to (if (isCleaningActive) strings.statusCleaning else strings.subClean)
        1 -> strings.tabGenerator to strings.subGenerator
        2 -> strings.tabDiagnostics to strings.subDiagnostics
        3 -> strings.tabHistory to strings.subHistory
        else -> strings.tabSettings to strings.subSettings
    }

    TopAppBar(
        modifier = modifier.fillMaxWidth(),
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colors.surfaceElevated,
            scrolledContainerColor = colors.surfaceElevated,
            titleContentColor = colors.textPrimary,
            actionIconContentColor = colors.textPrimary
        ),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 2.dp)
            ) {
                AppLogo(size = 32.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isCleaningActive && currentTab == 0) colors.accent else colors.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        actions = {
            if (isCleaningActive) {
                Box(
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.accent.copy(alpha = 0.22f))
                        .border(
                            width = 1.dp,
                            color = colors.accent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = strings.badgeActive,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.accent,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    )
}
