package com.shuttletechnologies.speakercleaner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.Hearing
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shuttletechnologies.speakercleaner.data.CleanMode
import com.shuttletechnologies.speakercleaner.data.CleaningSession
import com.shuttletechnologies.speakercleaner.data.PreferencesManager
import com.shuttletechnologies.speakercleaner.data.SpeakerTarget
import com.shuttletechnologies.speakercleaner.localization.appStrings
import com.shuttletechnologies.speakercleaner.theme.LocalAppColors
import com.shuttletechnologies.speakercleaner.ui.components.SmallNativeAdView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    prefs: PreferencesManager,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val strings = appStrings()
    val scrollState = rememberScrollState()

    val sessions by prefs.sessions.collectAsState()
    val totalCleans = prefs.getTotalCleans()
    val waterCleans = prefs.getWaterCleans()
    val dustCleans = prefs.getDustCleans()
    val lastCleanTime = prefs.getLastCleanTime()

    val lastCleanFormatted = if (lastCleanTime > 0) {
        SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(lastCleanTime))
    } else {
        strings.never
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Quick Stats Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                title = strings.statTotalCleans,
                value = totalCleans.toString(),
                icon = Icons.Rounded.CleaningServices,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = strings.statWaterCleans,
                value = waterCleans.toString(),
                icon = Icons.Rounded.WaterDrop,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = strings.statDustCleans,
                value = dustCleans.toString(),
                icon = Icons.Rounded.Air,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Recent Sessions Section
        Text(
            text = strings.historyTitle,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        if (sessions.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surfaceElevated)
                    .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Rounded.History,
                    contentDescription = null,
                    tint = colors.textMuted,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = strings.historyEmpty,
                    fontSize = 12.sp,
                    color = colors.textMuted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            sessions.take(10).forEach { session ->
                SessionItemCard(session = session)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Educational Guides & Maintenance Tips
        GuideCard(
            title = strings.tipAcousticTitle,
            description = strings.tipAcousticDesc,
            icon = Icons.Rounded.Info
        )

        Spacer(modifier = Modifier.height(12.dp))

        GuideCard(
            title = strings.tipPositionTitle,
            description = strings.tipPositionDesc,
            icon = Icons.Rounded.CheckCircle
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Small Native Ad at Bottom
        SmallNativeAdView()

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surfaceElevated)
            .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.accent,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary
        )
        Text(
            text = title,
            fontSize = 9.sp,
            color = colors.textMuted,
            maxLines = 1
        )
    }
}

@Composable
private fun SessionItemCard(session: CleaningSession) {
    val colors = LocalAppColors.current
    val strings = appStrings()
    val dateStr = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(session.timestamp))

    val icon = when (session.mode) {
        CleanMode.WATER_EJECT, CleanMode.DEEP_CLEAN -> Icons.Rounded.WaterDrop
        CleanMode.DUST_BLAST -> Icons.Rounded.Air
        else -> Icons.Rounded.CleaningServices
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceElevated)
            .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(colors.accent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${session.mode.getLocalizedName(strings)} (${session.target.getLocalizedName(strings)})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Text(
                text = "$dateStr • ${session.durationSeconds}s",
                fontSize = 10.sp,
                color = colors.textMuted
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(colors.success.copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = strings.statusOk,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = colors.success
            )
        }
    }
}

@Composable
private fun GuideCard(
    title: String,
    description: String,
    icon: ImageVector
) {
    val colors = LocalAppColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surfaceElevated)
            .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = description,
            fontSize = 11.sp,
            color = colors.textSecondary,
            lineHeight = 16.sp
        )
    }
}
