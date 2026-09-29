package com.shuttletechnologies.speakercleaner.ui.components

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.shuttletechnologies.speakercleaner.BuildConfig
import com.shuttletechnologies.speakercleaner.R
import com.shuttletechnologies.speakercleaner.theme.AppColorScheme
import com.shuttletechnologies.speakercleaner.theme.LocalAppColors

@Composable
fun SmallNativeAdView(
    modifier: Modifier = Modifier
) {
    if (LocalInspectionMode.current) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .background(LocalAppColors.current.surfaceElevated, RoundedCornerShape(16.dp))
        )
        return
    }

    val context = LocalContext.current
    val colors = LocalAppColors.current

    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }
    var loadFailed by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val adLoader = AdLoader.Builder(context, BuildConfig.NATIVE_AD_UNIT_ID)
            .forNativeAd { loadedAd ->
                nativeAd = loadedAd
                loadFailed = false
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    loadFailed = true
                }
            })
            .build()

        adLoader.loadAd(AdRequest.Builder().build())

        onDispose {
            nativeAd?.destroy()
            nativeAd = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceElevated)
            .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        val currentAd = nativeAd
        if (currentAd != null) {
            AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = { ctx ->
                    val inflater = LayoutInflater.from(ctx)
                    val adView = inflater.inflate(R.layout.layout_small_native_ad, null) as NativeAdView
                    populateAndStyleNativeAd(adView, currentAd, colors)
                    adView
                },
                update = { adView ->
                    populateAndStyleNativeAd(adView, currentAd, colors)
                }
            )
        } else {
            // Adaptive Banner Fallback
            AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = { ctx ->
                    AdView(ctx).apply {
                        setAdSize(AdSize.BANNER)
                        adUnitId = BuildConfig.BANNER_AD_UNIT_ID
                        loadAd(AdRequest.Builder().build())
                    }
                }
            )
        }
    }
}

private fun populateAndStyleNativeAd(
    adView: NativeAdView,
    nativeAd: NativeAd,
    colors: AppColorScheme
) {
    // Style Container Background
    val density = adView.context.resources.displayMetrics.density
    val bg = GradientDrawable().apply {
        cornerRadius = 16f * density
        setColor(colors.surfaceElevated.toArgb())
        setStroke((1f * density).toInt(), colors.border.copy(alpha = 0.6f).toArgb())
    }
    adView.background = bg

    // App Icon
    val iconView = adView.findViewById<ImageView>(R.id.ad_app_icon)
    if (nativeAd.icon != null) {
        iconView.setImageDrawable(nativeAd.icon?.drawable)
        iconView.clipToOutline = true
        adView.iconView = iconView
    }

    // Headline
    val headlineView = adView.findViewById<TextView>(R.id.ad_headline)
    headlineView.text = nativeAd.headline ?: "Sponsor"
    headlineView.setTextColor(colors.textPrimary.toArgb())
    adView.headlineView = headlineView

    // Secondary / Body
    val bodyView = adView.findViewById<TextView>(R.id.ad_body)
    bodyView.text = nativeAd.body ?: nativeAd.advertiser ?: "Promoted by Google"
    bodyView.setTextColor(colors.textMuted.toArgb())
    adView.bodyView = bodyView

    // Call To Action Button
    val ctaView = adView.findViewById<Button>(R.id.ad_call_to_action)
    ctaView.text = nativeAd.callToAction ?: "LEARN MORE"
    val ctaBg = GradientDrawable().apply {
        cornerRadius = 18f * density
        setColor(colors.accent.toArgb())
    }
    ctaView.background = ctaBg
    val ctaTextColor = if (colors.isDark) android.graphics.Color.BLACK else android.graphics.Color.WHITE
    ctaView.setTextColor(ctaTextColor)
    adView.callToActionView = ctaView

    adView.setNativeAd(nativeAd)
}
