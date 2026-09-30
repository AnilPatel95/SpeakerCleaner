# -*- coding: utf-8 -*-
"""
Shuttle Technologies — Automated Store Assets & Marketing Generator for Speaker Cleaner
Produces:
1. 8 Luxury Framed Marketing Screenshots (1080x2280 px, Play Store luxury chassis mockups)
2. Official 1080p Landscape Promo Video (1920x1080 @ 30 FPS, ~32s)
"""

import os
import sys
import time
import math
import subprocess
import shutil
from PIL import Image, ImageDraw, ImageFont, ImageFilter

RELEASE_DIR = r"D:\Anil\MyProduct\Android Project App realse data\Speaker Cleaner"
SCREENSHOTS_DIR = os.path.join(RELEASE_DIR, "screenshots")
PROJECT_DIR = r"d:\Anil\MyProduct\Native\Speaker Cleaner"
APP_ICON_PATH = os.path.join(RELEASE_DIR, "app_icon_512x512.png")

os.makedirs(RELEASE_DIR, exist_ok=True)
os.makedirs(SCREENSHOTS_DIR, exist_ok=True)

# Luxury Dark Studio Palette (100% Solid Opaque RGBA to prevent alpha blending artifacts)
DARK_BG = (10, 14, 23, 255)
DARK_SURFACE = (18, 24, 38, 255)
DARK_SURFACE_ELEVATED = (24, 34, 54, 255)
ACCENT_CYAN = (0, 229, 255, 255)
ACCENT_BLUE = (33, 150, 243, 255)
ACCENT_GREEN = (0, 230, 118, 255)
ACCENT_GOLD = (255, 179, 0, 255)
ACCENT_PURPLE = (171, 71, 188, 255)
ACCENT_RED = (255, 61, 0, 255)
TEXT_WHITE = (245, 248, 252, 255)
TEXT_SECONDARY = (160, 178, 200, 255)
TEXT_MUTED = (110, 126, 148, 255)
BORDER_COLOR = (42, 58, 86, 255)

# Solid Tinted Container Fills (Equivalent to alpha 0.18 over DARK_SURFACE)
TINTED_CYAN_BG = (16, 48, 68, 255)
TINTED_BLUE_BG = (18, 40, 72, 255)
TINTED_GREEN_BG = (16, 50, 38, 255)
TINTED_GOLD_BG = (48, 42, 18, 255)
TINTED_PURPLE_BG = (42, 24, 54, 255)

def get_font(size, bold=False):
    font_names = [
        "C:\\Windows\\Fonts\\segoeuib.ttf" if bold else "C:\\Windows\\Fonts\\segoeui.ttf",
        "C:\\Windows\\Fonts\\arialbd.ttf" if bold else "C:\\Windows\\Fonts\\arial.ttf"
    ]
    for fn in font_names:
        if os.path.exists(fn):
            try:
                return ImageFont.truetype(fn, size)
            except Exception:
                pass
    return ImageFont.load_default()

# 8 Category Screenshots Definition
SCREENSHOTS_METADATA = [
    {
        "id": "01_water_eject",
        "pill": "WATER & DUST EJECTOR",
        "pill_color": (0, 229, 255),
        "title": "Eject Water from Speakers",
        "subtitle": "Low-frequency acoustic pressure waves push out trapped water"
    },
    {
        "id": "02_frequency_generator",
        "pill": "PRECISION SOUND GENERATOR",
        "pill_color": (0, 180, 216),
        "title": "1 Hz – 25,000 Hz Tone Synth",
        "subtitle": "Sweep frequencies with sine, square, and triangle waveforms"
    },
    {
        "id": "03_earpiece_cleaning",
        "pill": "DUAL SPEAKER CLEANING",
        "pill_color": (33, 150, 243),
        "title": "Earpiece & Loudspeaker Modes",
        "subtitle": "Targeted acoustic resonance tuned for call speaker & bottom grill"
    },
    {
        "id": "04_stereo_audio_test",
        "pill": "STEREO AUDIO DIAGNOSTIC",
        "pill_color": (0, 230, 118),
        "title": "Stereo Left & Right Balance",
        "subtitle": "Verify acoustic clarity and speaker channel separation instantly"
    },
    {
        "id": "05_ultrasonic_clean",
        "pill": "ULTRASONIC DUST REMOVAL",
        "pill_color": (171, 71, 188),
        "title": "Silent Ultrasonic Micro-Sweep",
        "subtitle": "Dislodge stubborn dry dust particles at 18.5 kHz – 21.5 kHz"
    },
    {
        "id": "06_decibel_sound_meter",
        "pill": "REAL-TIME DB SOUND METER",
        "pill_color": (255, 179, 0),
        "title": "Measure Acoustic Decibels",
        "subtitle": "Track volume gain and sound clarity before & after cleaning"
    },
    {
        "id": "07_quick_settings_tile",
        "pill": "QUICK SETTINGS 1-TAP TILE",
        "pill_color": (0, 229, 255),
        "title": "Instant Notification Tile",
        "subtitle": "Start water ejection directly from your Android quick settings"
    },
    {
        "id": "08_settings_themes",
        "pill": "CURATED THEMES & 16 LANGUAGES",
        "pill_color": (0, 230, 118),
        "title": "Pitch Black AMOLED & Studio Dark",
        "subtitle": "100% offline-first architecture with 16 global translations"
    }
]

def draw_top_bar(draw, w, title="Speaker Cleaner", subtitle="Acoustic Water Ejector"):
    # Status bar
    f_time = get_font(26, bold=True)
    draw.text((45, 18), "09:00", fill=TEXT_WHITE, font=f_time)
    draw.text((w - 180, 22), "5G  100%", fill=TEXT_WHITE, font=get_font(20, bold=True))
    
    # App Bar Surface
    bar_y = 65
    bar_h = 100
    draw.rectangle([0, bar_y, w, bar_y + bar_h], fill=DARK_SURFACE)
    draw.line([(0, bar_y + bar_h), (w, bar_y + bar_h)], fill=BORDER_COLOR, width=1)
    
    # Speaker Icon Glyph
    cx, cy = 60, bar_y + 50
    draw.polygon([(cx - 15, cy - 10), (cx - 5, cy - 10), (cx + 8, cy - 22), (cx + 8, cy + 22), (cx - 5, cy + 10), (cx - 15, cy + 10)], fill=ACCENT_CYAN)
    draw.arc([cx + 12, cy - 14, cx + 24, cy + 14], -45, 45, fill=ACCENT_CYAN, width=3)
    draw.arc([cx + 16, cy - 22, cx + 32, cy + 22], -45, 45, fill=ACCENT_CYAN, width=3)
    
    # Title & Subtitle
    draw.text((115, bar_y + 20), title, fill=TEXT_WHITE, font=get_font(34, bold=True))
    draw.text((115, bar_y + 60), subtitle, fill=TEXT_MUTED, font=get_font(20))

def draw_bottom_nav(draw, w, h, selected_idx=0):
    nav_h = 135
    nav_y = h - nav_h
    draw.rounded_rectangle([0, nav_y, w, h], radius=24, fill=DARK_SURFACE, outline=BORDER_COLOR, width=1)
    
    tabs = ["Clean", "Generator", "Diagnostics", "History", "Settings"]
    tab_w = w / len(tabs)
    
    for i, tab in enumerate(tabs):
        tx = int(i * tab_w + tab_w / 2)
        is_sel = (i == selected_idx)
        col = ACCENT_CYAN if is_sel else TEXT_MUTED
        
        if is_sel:
            draw.rounded_rectangle([tx - 52, nav_y + 14, tx + 52, nav_y + 64], radius=18, fill=TINTED_CYAN_BG, outline=ACCENT_CYAN, width=1)
        
        # Crisp icon placeholder
        draw.ellipse([tx - 12, nav_y + 24, tx + 12, nav_y + 48], outline=col, width=3)
        draw.text((tx - len(tab) * 7, nav_y + 76), tab, fill=col, font=get_font(21, bold=is_sel))

def draw_checkmark(draw, cx, cy, col=ACCENT_GREEN, size=16):
    draw.line([(cx - size, cy), (cx - size // 3, cy + size * 2 // 3)], fill=col, width=4)
    draw.line([(cx - size // 3, cy + size * 2 // 3), (cx + size, cy - size * 2 // 3)], fill=col, width=4)

def create_raw_ui_screen(screen_id):
    """Composes an authentic, high-res 828x1740 UI screenshot for Speaker Cleaner."""
    W, H = 828, 1740
    img = Image.new("RGBA", (W, H), DARK_BG)
    draw = ImageDraw.Draw(img)
    
    if screen_id == "01_water_eject":
        draw_top_bar(draw, W, "Speaker Cleaner", "Acoustic Water Ejector")
        
        # Speaker Selector (Loudspeaker / Earpiece)
        sw_y = 190
        draw.rounded_rectangle([40, sw_y, W - 40, sw_y + 68], radius=20, fill=DARK_SURFACE, outline=BORDER_COLOR, width=1)
        draw.rounded_rectangle([44, sw_y + 4, W // 2 - 4, sw_y + 64], radius=16, fill=TINTED_CYAN_BG, outline=ACCENT_CYAN, width=2)
        draw.text((110, sw_y + 20), "Loudspeaker (Main)", fill=ACCENT_CYAN, font=get_font(22, bold=True))
        draw.text((W // 2 + 75, sw_y + 20), "Earpiece (Call)", fill=TEXT_MUTED, font=get_font(22))
        
        # Mode Chips (Water Eject, Dust Agitate, Deep Clean, Ultrasonic)
        chips_y = 280
        chips = [("Water Eject", True), ("Dust Agitate", False), ("Deep Clean", False), ("Ultrasonic", False)]
        cx = 40
        for name, active in chips:
            cw = 175
            col = ACCENT_CYAN if active else TEXT_MUTED
            bg = TINTED_CYAN_BG if active else DARK_SURFACE
            border = ACCENT_CYAN if active else BORDER_COLOR
            draw.rounded_rectangle([cx, chips_y, cx + cw, chips_y + 54], radius=16, fill=bg, outline=border, width=2 if active else 1)
            draw.text((cx + 20, chips_y + 14), name, fill=col, font=get_font(19, bold=active))
            cx += cw + 14
            
        # Large Central Acoustic Sweep Gauge
        gcx, gcy = W // 2, 600
        gr = 210
        # Background arc
        draw.arc([gcx - gr, gcy - gr, gcx + gr, gcy + gr], 135, 405, fill=(35, 48, 72), width=18)
        # Active glowing arc
        draw.arc([gcx - gr, gcy - gr, gcx + gr, gcy + gr], 135, 330, fill=ACCENT_CYAN, width=18)
        
        # Inner Circle
        draw.ellipse([gcx - 170, gcy - 170, gcx + 170, gcy + 170], fill=DARK_SURFACE, outline=BORDER_COLOR, width=2)
        
        # Speaker Cone Graphic in Center
        draw.polygon([(gcx - 45, gcy - 25), (gcx - 15, gcy - 25), (gcx + 25, gcy - 55), (gcx + 25, gcy + 55), (gcx - 15, gcy + 25), (gcx - 45, gcy + 25)], fill=ACCENT_CYAN)
        draw.arc([gcx + 35, gcy - 35, gcx + 65, gcy + 35], -50, 50, fill=ACCENT_CYAN, width=6)
        draw.arc([gcx + 45, gcy - 55, gcx + 85, gcy + 55], -50, 50, fill=ACCENT_CYAN, width=6)
        
        draw.text((gcx - 65, gcy + 75), "165 Hz", fill=TEXT_WHITE, font=get_font(38, bold=True))
        draw.text((gcx - 90, gcy + 120), "RESONANT FREQUENCY", fill=ACCENT_CYAN, font=get_font(17, bold=True))
        
        # 45° Gravity Drainage Sensor Card
        card_y = 860
        draw.rounded_rectangle([40, card_y, W - 40, card_y + 115], radius=20, fill=DARK_SURFACE, outline=BORDER_COLOR, width=1)
        draw.ellipse([70, card_y + 35, 115, card_y + 80], fill=TINTED_GREEN_BG, outline=ACCENT_GREEN, width=2)
        draw.text((86, card_y + 44), "45°", fill=ACCENT_GREEN, font=get_font(18, bold=True))
        draw.text((135, card_y + 26), "Optimal Drainage Position Detected", fill=TEXT_WHITE, font=get_font(23, bold=True))
        draw.text((135, card_y + 64), "Hold phone facing downwards to allow gravity drainage", fill=TEXT_SECONDARY, font=get_font(18))
        
        # Volume Maximizer Card
        vol_y = 1000
        draw.rounded_rectangle([40, vol_y, W - 40, vol_y + 90], radius=20, fill=DARK_SURFACE, outline=BORDER_COLOR, width=1)
        draw.text((70, vol_y + 20), "Acoustic Volume: 100% (Auto-Maximized)", fill=ACCENT_GOLD, font=get_font(22, bold=True))
        draw.text((70, vol_y + 54), "Maximum kinetic acoustic displacement active", fill=TEXT_MUTED, font=get_font(18))
        
        # Giant Start Cleaning Button
        btn_y = 1120
        draw.rounded_rectangle([40, btn_y, W - 40, btn_y + 110], radius=28, fill=ACCENT_CYAN)
        draw.text((W // 2 - 130, btn_y + 34), "START CLEANING", fill=(10, 14, 23), font=get_font(32, bold=True))
        
        draw.text((W // 2 - 190, btn_y + 135), "Estimated cleaning cycle: 60 seconds", fill=TEXT_MUTED, font=get_font(20))
        draw_bottom_nav(draw, W, H, selected_idx=0)
        
    elif screen_id == "02_frequency_generator":
        draw_top_bar(draw, W, "Frequency Generator", "Manual Acoustic Tone Synthesizer")
        
        # Waveform selector chips
        wf_y = 190
        waveforms = [("Sine Wave", True), ("Square Wave", False), ("Triangle Wave", False)]
        cx = 40
        for name, active in waveforms:
            cw = 236
            col = ACCENT_CYAN if active else TEXT_MUTED
            bg = TINTED_CYAN_BG if active else DARK_SURFACE
            border = ACCENT_CYAN if active else BORDER_COLOR
            draw.rounded_rectangle([cx, wf_y, cx + cw, wf_y + 56], radius=16, fill=bg, outline=border, width=2 if active else 1)
            draw.text((cx + 50, wf_y + 16), name, fill=col, font=get_font(21, bold=active))
            cx += cw + 20
            
        # Frequency Dial Display Card
        fcard_y = 280
        draw.rounded_rectangle([40, fcard_y, W - 40, fcard_y + 320], radius=24, fill=DARK_SURFACE, outline=BORDER_COLOR, width=1)
        draw.text((W // 2 - 120, fcard_y + 35), "CURRENT FREQUENCY", fill=TEXT_MUTED, font=get_font(19, bold=True))
        draw.text((W // 2 - 130, fcard_y + 80), "440 Hz", fill=ACCENT_CYAN, font=get_font(68, bold=True))
        draw.text((W // 2 - 110, fcard_y + 175), "Concert Pitch A4", fill=TEXT_SECONDARY, font=get_font(23))
        
        # Slider track
        sy = fcard_y + 245
        draw.rounded_rectangle([70, sy, W - 70, sy + 14], radius=7, fill=(35, 48, 72))
        draw.rounded_rectangle([70, sy, W // 2 + 50, sy + 14], radius=7, fill=ACCENT_CYAN)
        draw.ellipse([W // 2 + 35, sy - 14, W // 2 + 75, sy + 28], fill=TEXT_WHITE, outline=ACCENT_CYAN, width=3)
        draw.text((70, sy + 28), "1 Hz", fill=TEXT_MUTED, font=get_font(18))
        draw.text((W - 165, sy + 28), "25,000 Hz", fill=TEXT_MUTED, font=get_font(18))
        
        # Real-time Sine Wave Oscilloscope
        osc_y = 630
        draw.rounded_rectangle([40, osc_y, W - 40, osc_y + 220], radius=24, fill=DARK_SURFACE, outline=BORDER_COLOR, width=1)
        draw.text((70, osc_y + 25), "Live Acoustic Oscilloscope", fill=TEXT_WHITE, font=get_font(22, bold=True))
        
        points = []
        for x in range(70, W - 70, 4):
            val = math.sin((x - 70) * 0.05) * 45
            points.append((x, osc_y + 130 + val))
        for k in range(len(points) - 1):
            draw.line([points[k], points[k + 1]], fill=ACCENT_CYAN, width=3)
            
        # Fast Preset Chips
        pr_y = 880
        draw.text((45, pr_y), "Acoustic Tuning Presets", fill=TEXT_WHITE, font=get_font(24, bold=True))
        presets = [("165 Hz (Water Eject)", ACCENT_CYAN), ("440 Hz (Standard A)", TEXT_WHITE), ("1000 Hz (Reference)", TEXT_WHITE), ("12 kHz (Treble Clean)", TEXT_WHITE)]
        py = pr_y + 40
        for name, col in presets:
            draw.rounded_rectangle([40, py, W - 40, py + 64], radius=16, fill=DARK_SURFACE, outline=BORDER_COLOR, width=1)
            draw.text((70, py + 18), name, fill=col, font=get_font(21, bold=(col == ACCENT_CYAN)))
            draw.text((W - 120, py + 18), "Select", fill=ACCENT_CYAN, font=get_font(19, bold=True))
            py += 78
            
        pbtn_y = 1230
        draw.rounded_rectangle([40, pbtn_y, W - 40, pbtn_y + 100], radius=24, fill=(0, 180, 216))
        draw.text((W // 2 - 80, pbtn_y + 32), "PLAY TONE", fill=(10, 14, 23), font=get_font(30, bold=True))
        
        draw_bottom_nav(draw, W, H, selected_idx=1)
        
    elif screen_id == "03_earpiece_cleaning":
        draw_top_bar(draw, W, "Earpiece Cleaning", "Top Call Speaker Acoustic Sweep")
        
        sw_y = 190
        draw.rounded_rectangle([40, sw_y, W - 40, sw_y + 68], radius=20, fill=DARK_SURFACE, outline=BORDER_COLOR, width=1)
        draw.text((110, sw_y + 20), "Loudspeaker (Main)", fill=TEXT_MUTED, font=get_font(22))
        draw.rounded_rectangle([W // 2 + 4, sw_y + 4, W - 44, sw_y + 64], radius=16, fill=TINTED_BLUE_BG, outline=ACCENT_BLUE, width=2)
        draw.text((W // 2 + 75, sw_y + 20), "Earpiece (Call)", fill=ACCENT_BLUE, font=get_font(22, bold=True))
        
        pcx, pcy = W // 2, 540
        pw, ph = 260, 480
        draw.rounded_rectangle([pcx - pw // 2, pcy - ph // 2, pcx + pw // 2, pcy + ph // 2], radius=42, fill=DARK_SURFACE, outline=BORDER_COLOR, width=3)
        draw.rounded_rectangle([pcx - pw // 2 + 14, pcy - ph // 2 + 45, pcx + pw // 2 - 14, pcy + ph // 2 - 25], radius=28, fill=(12, 16, 26))
        draw.rounded_rectangle([pcx - 35, pcy - ph // 2 + 20, pcx + 35, pcy - ph // 2 + 28], radius=4, fill=ACCENT_BLUE)
        for rad in [45, 80, 115, 150]:
            draw.arc([pcx - rad, pcy - ph // 2 - rad + 24, pcx + rad, pcy - ph // 2 + rad + 24], -140, -40, fill=ACCENT_BLUE, width=4)
            
        draw.text((pcx - 110, pcy + ph // 2 + 35), "Target: Top Ear Speaker", fill=TEXT_WHITE, font=get_font(24, bold=True))
        draw.text((pcx - 135, pcy + ph // 2 + 70), "Calibrated for delicate call voice coils", fill=TEXT_MUTED, font=get_font(19))
        
        rc_y = 920
        draw.rounded_rectangle([40, rc_y, W - 40, rc_y + 110], radius=20, fill=DARK_SURFACE, outline=BORDER_COLOR, width=1)
        draw.text((70, rc_y + 24), "High-Resonance Pulse: 320 Hz – 1,200 Hz", fill=ACCENT_BLUE, font=get_font(22, bold=True))
        draw.text((70, rc_y + 62), "Prevents voice coil strain while expelling moisture", fill=TEXT_SECONDARY, font=get_font(18))
        
        sbtn_y = 1070
        draw.rounded_rectangle([40, sbtn_y, W - 40, sbtn_y + 105], radius=26, fill=ACCENT_BLUE)
        draw.text((W // 2 - 150, sbtn_y + 34), "CLEAN EARPIECE NOW", fill=TEXT_WHITE, font=get_font(28, bold=True))
        
        draw_bottom_nav(draw, W, H, selected_idx=0)
        
    elif screen_id == "04_stereo_audio_test":
        draw_top_bar(draw, W, "Audio Diagnostics", "Stereo Channels & Sound Clarity")
        
        cy = 200
        # Left channel card
        draw.rounded_rectangle([40, cy, W // 2 - 15, cy + 300], radius=24, fill=DARK_SURFACE, outline=ACCENT_GREEN, width=2)
        draw.ellipse([W // 4 - 35, cy + 35, W // 4 + 35, cy + 105], fill=TINTED_GREEN_BG, outline=ACCENT_GREEN, width=3)
        draw.text((W // 4 - 10, cy + 50), "L", fill=ACCENT_GREEN, font=get_font(36, bold=True))
        draw.text((W // 4 - 55, cy + 125), "Left Channel", fill=TEXT_WHITE, font=get_font(24, bold=True))
        draw.text((W // 4 - 70, cy + 165), "Volume Output: 100%", fill=TEXT_SECONDARY, font=get_font(18))
        draw.rounded_rectangle([65, cy + 215, W // 2 - 40, cy + 270], radius=16, fill=ACCENT_GREEN)
        draw.text((W // 4 - 35, cy + 230), "TEST L", fill=(10, 14, 23), font=get_font(21, bold=True))
        
        # Right channel card
        draw.rounded_rectangle([W // 2 + 15, cy, W - 40, cy + 300], radius=24, fill=DARK_SURFACE, outline=BORDER_COLOR, width=1)
        draw.ellipse([3 * W // 4 - 35, cy + 35, 3 * W // 4 + 35, cy + 105], fill=(35, 48, 72), outline=BORDER_COLOR, width=3)
        draw.text((3 * W // 4 - 10, cy + 50), "R", fill=TEXT_MUTED, font=get_font(36, bold=True))
        draw.text((3 * W // 4 - 60, cy + 125), "Right Channel", fill=TEXT_WHITE, font=get_font(24, bold=True))
        draw.text((3 * W // 4 - 70, cy + 165), "Volume Output: 100%", fill=TEXT_SECONDARY, font=get_font(18))
        draw.rounded_rectangle([W // 2 + 40, cy + 215, W - 65, cy + 270], radius=16, fill=DARK_SURFACE_ELEVATED, outline=BORDER_COLOR, width=1)
        draw.text((3 * W // 4 - 35, cy + 230), "TEST R", fill=TEXT_WHITE, font=get_font(21, bold=True))
        
        fbtn_y = 530
        draw.rounded_rectangle([40, fbtn_y, W - 40, fbtn_y + 80], radius=20, fill=DARK_SURFACE_ELEVATED, outline=ACCENT_GREEN, width=1)
        draw.text((W // 2 - 120, fbtn_y + 24), "PLAY DUAL STEREO SWEEP", fill=ACCENT_GREEN, font=get_font(22, bold=True))
        
        tbl_y = 650
        draw.text((45, tbl_y), "Acoustic Clarity Diagnostic", fill=TEXT_WHITE, font=get_font(24, bold=True))
        tests = [
            ("Low Bass Resonance (60 Hz - 250 Hz)", "PASS"),
            ("Midrange Clarity (250 Hz - 4,000 Hz)", "PASS"),
            ("High Treble Sparkle (4,000 Hz - 16 kHz)", "PASS"),
            ("Microphone Noise Cancellation", "PASS")
        ]
        ty = tbl_y + 45
        for name, status in tests:
            draw.rounded_rectangle([40, ty, W - 40, ty + 70], radius=16, fill=DARK_SURFACE, outline=BORDER_COLOR, width=1)
            draw.text((70, ty + 22), name, fill=TEXT_WHITE, font=get_font(20))
            draw_checkmark(draw, W - 140, ty + 35, col=ACCENT_GREEN, size=10)
            draw.text((W - 120, ty + 22), status, fill=ACCENT_GREEN, font=get_font(20, bold=True))
            ty += 84
            
        hc_y = 1040
        draw.rounded_rectangle([40, hc_y, W - 40, hc_y + 110], radius=22, fill=DARK_SURFACE, outline=ACCENT_GREEN, width=2)
        draw.text((70, hc_y + 24), "Acoustic Clarity Score: 98% (Optimal)", fill=ACCENT_GREEN, font=get_font(23, bold=True))
        draw.text((70, hc_y + 64), "No distortion or mechanical rattles detected", fill=TEXT_SECONDARY, font=get_font(18))
        
        draw_bottom_nav(draw, W, H, selected_idx=2)
        
    elif screen_id == "05_ultrasonic_clean":
        draw_top_bar(draw, W, "Ultrasonic Cleaning", "Silent Micro-Dust Vibrations")
        
        chips_y = 190
        chips = [("Water Eject", False), ("Dust Agitate", False), ("Deep Clean", False), ("Ultrasonic", True)]
        cx = 40
        for name, active in chips:
            cw = 175
            col = ACCENT_PURPLE if active else TEXT_MUTED
            bg = TINTED_PURPLE_BG if active else DARK_SURFACE
            border = ACCENT_PURPLE if active else BORDER_COLOR
            draw.rounded_rectangle([cx, chips_y, cx + cw, chips_y + 54], radius=16, fill=bg, outline=border, width=2 if active else 1)
            draw.text((cx + 20, chips_y + 14), name, fill=col, font=get_font(19, bold=active))
            cx += cw + 14
            
        gcx, gcy = W // 2, 530
        gr = 200
        draw.arc([gcx - gr, gcy - gr, gcx + gr, gcy + gr], 135, 405, fill=(35, 48, 72), width=18)
        draw.arc([gcx - gr, gcy - gr, gcx + gr, gcy + gr], 135, 360, fill=ACCENT_PURPLE, width=18)
        
        draw.ellipse([gcx - 160, gcy - 160, gcx + 160, gcy + 160], fill=DARK_SURFACE, outline=BORDER_COLOR, width=2)
        draw.text((gcx - 100, gcy - 50), "20,500 Hz", fill=ACCENT_PURPLE, font=get_font(46, bold=True))
        draw.text((gcx - 85, gcy + 15), "ULTRASONIC SWEEP", fill=TEXT_SECONDARY, font=get_font(18, bold=True))
        draw.text((gcx - 110, gcy + 55), "Inaudible Human Spectrum", fill=TEXT_MUTED, font=get_font(17))
        
        sc_y = 800
        draw.rounded_rectangle([40, sc_y, W - 40, sc_y + 120], radius=20, fill=DARK_SURFACE, outline=BORDER_COLOR, width=1)
        draw.text((70, sc_y + 26), "Quiet & Discrete Operation", fill=TEXT_WHITE, font=get_font(23, bold=True))
        draw.text((70, sc_y + 66), "Safe to run in public, meetings, or quiet offices without noise", fill=TEXT_SECONDARY, font=get_font(18))
        
        mc_y = 950
        draw.rounded_rectangle([40, mc_y, W - 40, mc_y + 120], radius=20, fill=DARK_SURFACE, outline=BORDER_COLOR, width=1)
        draw.text((70, mc_y + 26), "Micro-Dust & Lint Dispersion", fill=ACCENT_PURPLE, font=get_font(23, bold=True))
        draw.text((70, mc_y + 66), "Breaks electrostatic adhesion of dry pocket lint on speaker mesh", fill=TEXT_SECONDARY, font=get_font(18))
        
        btn_y = 1110
        draw.rounded_rectangle([40, btn_y, W - 40, btn_y + 105], radius=28, fill=ACCENT_PURPLE)
        draw.text((W // 2 - 150, btn_y + 32), "START ULTRASONIC", fill=TEXT_WHITE, font=get_font(30, bold=True))
        
        draw_bottom_nav(draw, W, H, selected_idx=0)
        
    elif screen_id == "06_decibel_sound_meter":
        draw_top_bar(draw, W, "Decibel Sound Meter", "Real-Time Acoustic SPL Monitor")
        
        db_y = 200
        draw.rounded_rectangle([40, db_y, W - 40, db_y + 360], radius=24, fill=DARK_SURFACE, outline=BORDER_COLOR, width=1)
        draw.text((W // 2 - 70, db_y + 30), "SOUND LEVEL", fill=TEXT_MUTED, font=get_font(19, bold=True))
        draw.text((W // 2 - 130, db_y + 75), "78.4", fill=ACCENT_GOLD, font=get_font(76, bold=True))
        draw.text((W // 2 + 50, db_y + 120), "dB", fill=TEXT_SECONDARY, font=get_font(28, bold=True))
        
        draw.rounded_rectangle([W // 2 - 120, db_y + 195, W // 2 + 120, db_y + 245], radius=16, fill=TINTED_GOLD_BG, outline=ACCENT_GOLD, width=1)
        draw.text((W // 2 - 95, db_y + 210), "LOUD & CLEAR (OPTIMAL)", fill=ACCENT_GOLD, font=get_font(16, bold=True))
        
        draw.line([(40, db_y + 270), (W - 40, db_y + 270)], fill=BORDER_COLOR, width=1)
        draw.text((80, db_y + 290), "MIN: 42.1 dB", fill=TEXT_SECONDARY, font=get_font(19))
        draw.text((W // 2 - 50, db_y + 290), "AVG: 68.5 dB", fill=TEXT_SECONDARY, font=get_font(19))
        draw.text((W - 190, db_y + 290), "MAX: 84.2 dB", fill=TEXT_SECONDARY, font=get_font(19))
        
        gr_y = 590
        draw.rounded_rectangle([40, gr_y, W - 40, gr_y + 260], radius=24, fill=DARK_SURFACE, outline=BORDER_COLOR, width=1)
        draw.text((70, gr_y + 25), "Live SPL Waveform (dB vs Time)", fill=TEXT_WHITE, font=get_font(22, bold=True))
        
        bx = 70
        vals = [45, 52, 60, 58, 65, 72, 78, 82, 80, 75, 71, 74, 79, 83, 78, 76, 81, 84, 78]
        for v in vals:
            bh = int((v - 30) * 2.2)
            draw.rounded_rectangle([bx, gr_y + 220 - bh, bx + 28, gr_y + 220], radius=6, fill=ACCENT_GOLD)
            bx += 36
            
        cmp_y = 880
        draw.rounded_rectangle([40, cmp_y, W - 40, cmp_y + 160], radius=22, fill=DARK_SURFACE_ELEVATED, outline=ACCENT_GOLD, width=1)
        draw.text((70, cmp_y + 24), "Cleaning Volume Restoration", fill=TEXT_WHITE, font=get_font(23, bold=True))
        draw.text((70, cmp_y + 64), "• Pre-Clean Output:  61.2 dB (Muffled)", fill=TEXT_MUTED, font=get_font(19))
        draw.text((70, cmp_y + 104), "• Post-Clean Output: 78.4 dB (+17.2 dB Gain) [RESTORED]", fill=ACCENT_GREEN, font=get_font(20, bold=True))
        
        draw_bottom_nav(draw, W, H, selected_idx=2)
        
    elif screen_id == "07_quick_settings_tile":
        draw.rectangle([0, 0, W, H], fill=(12, 16, 26))
        
        draw.text((50, 60), "09:00", fill=TEXT_WHITE, font=get_font(60, bold=True))
        draw.text((52, 135), "Wednesday, September 30", fill=TEXT_MUTED, font=get_font(22))
        
        qs_y = 200
        tiles = [
            ("Internet", "Wi-Fi Connected", False),
            ("Bluetooth", "On", False),
            ("Do Not Disturb", "Off", False),
            ("Flashlight", "Off", False),
            ("Speaker Cleaner", "1-Tap Water Eject", True),
            ("Auto-Rotate", "On", False)
        ]
        
        for idx, (tname, tsub, is_active) in enumerate(tiles):
            row = idx // 2
            col = idx % 2
            tx = 40 if col == 0 else W // 2 + 10
            ty = qs_y + row * 120
            tw = W // 2 - 50
            th = 100
            
            bg = (0, 229, 255) if is_active else DARK_SURFACE
            border = ACCENT_CYAN if is_active else BORDER_COLOR
            txt_col = (10, 14, 23) if is_active else TEXT_WHITE
            sub_col = (20, 30, 45) if is_active else TEXT_MUTED
            
            draw.rounded_rectangle([tx, ty, tx + tw, ty + th], radius=24, fill=bg, outline=border, width=2 if is_active else 1)
            draw.ellipse([tx + 20, ty + 25, tx + 70, ty + 75], fill=(255, 255, 255, 60) if is_active else DARK_SURFACE_ELEVATED)
            draw.text((tx + 85, ty + 24), tname, fill=txt_col, font=get_font(20, bold=True))
            draw.text((tx + 85, ty + 56), tsub, fill=sub_col, font=get_font(16))
            
        notif_y = 600
        draw.rounded_rectangle([40, notif_y, W - 40, notif_y + 190], radius=24, fill=DARK_SURFACE, outline=ACCENT_CYAN, width=2)
        draw.text((70, notif_y + 26), "Speaker Cleaner • Quick Tile Active", fill=ACCENT_CYAN, font=get_font(19, bold=True))
        draw.text((70, notif_y + 65), "Water Ejection Ready", fill=TEXT_WHITE, font=get_font(26, bold=True))
        draw.text((70, notif_y + 105), "Tap tile in notification bar anytime for instant water expulsion", fill=TEXT_SECONDARY, font=get_font(18))
        draw.text((70, notif_y + 145), "Background tile listener registered via Android TileService", fill=TEXT_MUTED, font=get_font(16))
        
        draw.rounded_rectangle([40, 830, W - 40, 1550], radius=32, fill=DARK_SURFACE_ELEVATED, outline=BORDER_COLOR, width=1)
        draw.text((W // 2 - 160, 870), "Instant System Access", fill=TEXT_WHITE, font=get_font(28, bold=True))
        draw.text((W // 2 - 200, 920), "No need to open the app after water exposure", fill=TEXT_SECONDARY, font=get_font(20))
        
        dcx, dcy = W // 2, 1180
        draw.ellipse([dcx - 120, dcy - 120, dcx + 120, dcy + 120], fill=TINTED_CYAN_BG, outline=ACCENT_CYAN, width=4)
        
        # Water drop vector shape
        draw.polygon([(dcx, dcy - 60), (dcx - 30, dcy - 10), (dcx + 30, dcy - 10)], fill=ACCENT_CYAN)
        draw.ellipse([dcx - 30, dcy - 25, dcx + 30, dcy + 35], fill=ACCENT_CYAN)
        draw.text((dcx - 55, dcy + 45), "165 Hz Sweep", fill=TEXT_WHITE, font=get_font(20, bold=True))
        draw.text((dcx - 70, dcy + 75), "TAP TO EJECT", fill=ACCENT_CYAN, font=get_font(22, bold=True))
        
    elif screen_id == "08_settings_themes":
        draw_top_bar(draw, W, "Settings", "Preferences & Themes")
        
        th_y = 200
        draw.text((45, th_y), "App Appearance", fill=TEXT_WHITE, font=get_font(24, bold=True))
        themes = [("System", False), ("Dark Studio", True), ("AMOLED", False), ("Light", False)]
        tx = 40
        for name, active in themes:
            tw = 175
            col = ACCENT_CYAN if active else TEXT_MUTED
            bg = TINTED_CYAN_BG if active else DARK_SURFACE
            border = ACCENT_CYAN if active else BORDER_COLOR
            draw.rounded_rectangle([tx, th_y + 40, tx + tw, th_y + 100], radius=16, fill=bg, outline=border, width=2 if active else 1)
            draw.text((tx + 25, th_y + 58), name, fill=col, font=get_font(20, bold=active))
            tx += tw + 14
            
        sh_y = 350
        draw.rounded_rectangle([20, sh_y, W - 20, H - 20], radius=36, fill=DARK_SURFACE_ELEVATED, outline=ACCENT_CYAN, width=2)
        draw.rounded_rectangle([W // 2 - 35, sh_y + 16, W // 2 + 35, sh_y + 24], radius=4, fill=BORDER_COLOR)
        
        draw.text((50, sh_y + 50), "Select Language (16 Supported)", fill=TEXT_WHITE, font=get_font(26, bold=True))
        draw.text((50, sh_y + 90), "100% Offline Multi-Language Architecture", fill=ACCENT_CYAN, font=get_font(18))
        
        langs = [
            ("English [EN]", "English (United States) - Base Default", True),
            ("Español [ES]", "Spanish (Spain & Latin America)", False),
            ("Français [FR]", "French (France)", False),
            ("Deutsch [DE]", "German (Germany)", False),
            ("Arabic [AR]", "Arabic (Full RTL Mirrored Layout)", False),
            ("Hindi [HI]", "Hindi (India)", False),
            ("Português [PT]", "Portuguese (Brazil & Portugal)", False),
            ("Japanese [JA]", "Japanese (Japan)", False),
            ("Korean [KO]", "Korean (South Korea)", False),
            ("Russian [RU]", "Russian (Russia)", False)
        ]
        
        ly = sh_y + 140
        for native, sub, is_sel in langs:
            bg_l = TINTED_CYAN_BG if is_sel else DARK_SURFACE
            border_l = ACCENT_CYAN if is_sel else BORDER_COLOR
            col_l = ACCENT_CYAN if is_sel else TEXT_WHITE
            
            draw.rounded_rectangle([45, ly, W - 45, ly + 80], radius=16, fill=bg_l, outline=border_l, width=2 if is_sel else 1)
            draw.text((75, ly + 16), native, fill=col_l, font=get_font(22, bold=is_sel))
            draw.text((75, ly + 46), sub, fill=TEXT_MUTED, font=get_font(16))
            if is_sel:
                draw_checkmark(draw, W - 140, ly + 40, col=ACCENT_CYAN, size=10)
                draw.text((W - 120, ly + 26), "Active", fill=ACCENT_CYAN, font=get_font(18, bold=True))
            ly += 94
            if ly > H - 100:
                break
                
    return img

def frame_screenshot(raw_img, meta, idx):
    canvas_w, canvas_h = 1080, 2280
    canvas = Image.new("RGBA", (canvas_w, canvas_h), (10, 14, 23, 255))
    draw = ImageDraw.Draw(canvas)

    pill_col = meta["pill_color"]
    glow = Image.new("RGBA", (canvas_w, canvas_h), (0, 0, 0, 0))
    gdraw = ImageDraw.Draw(glow)
    for rad in range(580, 100, -50):
        alpha = int(35 * (1.0 - rad / 580.0))
        gdraw.ellipse([canvas_w // 2 - rad, 1200 - rad, canvas_w // 2 + rad, 1200 + rad], fill=(*pill_col[:3], alpha))
    canvas.paste(glow, (0, 0), glow)

    # 1. Category Pill Badge
    pill_text = meta["pill"]
    f_pill = get_font(24, bold=True)
    pill_w = draw.textlength(pill_text, font=f_pill) + 48
    pill_x1 = (canvas_w - pill_w) // 2
    pill_y1 = 120
    draw.rounded_rectangle([pill_x1, pill_y1, pill_x1 + pill_w, pill_y1 + 54], radius=27, fill=DARK_SURFACE_ELEVATED, outline=pill_col, width=2)
    draw.text((pill_x1 + 24, pill_y1 + 12), pill_text, fill=pill_col, font=f_pill)

    # 2. Bold Headline
    f_title = get_font(52, bold=True)
    title_text = meta["title"]
    title_w = draw.textlength(title_text, font=f_title)
    draw.text(((canvas_w - title_w) // 2, 205), title_text, fill=TEXT_WHITE, font=f_title)

    # 3. Subtitle
    f_sub = get_font(27)
    sub_text = meta["subtitle"]
    sub_w = draw.textlength(sub_text, font=f_sub)
    draw.text(((canvas_w - sub_w) // 2, 280), sub_text, fill=TEXT_SECONDARY, font=f_sub)

    # 4. Smartphone Chassis Frame
    phone_w, phone_h = 860, 1780
    phone_x = (canvas_w - phone_w) // 2
    phone_y = 360
    bezel_radius = 48

    shadow = Image.new("RGBA", (canvas_w, canvas_h), (0, 0, 0, 0))
    sdraw = ImageDraw.Draw(shadow)
    sdraw.rounded_rectangle([phone_x - 8, phone_y + 8, phone_x + phone_w + 8, phone_y + phone_h + 24], radius=bezel_radius + 4, fill=(0, 0, 0, 160))
    shadow = shadow.filter(ImageFilter.GaussianBlur(18))
    canvas.paste(shadow, (0, 0), shadow)

    draw.rounded_rectangle([phone_x, phone_y, phone_x + phone_w, phone_y + phone_h], radius=bezel_radius, fill=(28, 36, 52), outline=(55, 72, 100), width=4)
    draw.rounded_rectangle([phone_x + 6, phone_y + 6, phone_x + phone_w - 6, phone_y + phone_h - 6], radius=bezel_radius - 4, fill=(12, 16, 24), outline=(18, 24, 34), width=3)

    screen_rect = [phone_x + 16, phone_y + 16, phone_x + phone_w - 16, phone_y + phone_h - 16]
    screen_w = screen_rect[2] - screen_rect[0]
    screen_h = screen_rect[3] - screen_rect[1]

    if raw_img is not None:
        scaled_screen = raw_img.resize((screen_w, screen_h), Image.Resampling.LANCZOS)
        mask = Image.new("L", (screen_w, screen_h), 0)
        mdraw = ImageDraw.Draw(mask)
        mdraw.rounded_rectangle([0, 0, screen_w, screen_h], radius=bezel_radius - 12, fill=255)
        canvas.paste(scaled_screen, (screen_rect[0], screen_rect[1]), mask)
    else:
        draw.rounded_rectangle(screen_rect, radius=bezel_radius - 12, fill=DARK_SURFACE)

    cam_x, cam_y = canvas_w // 2, phone_y + 36
    draw.ellipse([cam_x - 8, cam_y - 8, cam_x + 8, cam_y + 8], fill=(6, 10, 18), outline=(35, 48, 70), width=1)

    out_file = os.path.join(SCREENSHOTS_DIR, f"{meta['id']}.png")
    canvas.convert("RGB").save(out_file, "PNG")
    print(f"Saved Screenshot [{idx}/8]: {out_file}")
    return scaled_screen

def generate_all_screenshots():
    print("Generating 8 Luxury Framed Screenshots (Autonomous mode)...")
    screens_dict = {}
    for idx, meta in enumerate(SCREENSHOTS_METADATA, start=1):
        raw_screen = create_raw_ui_screen(meta["id"])
        scaled = frame_screenshot(raw_screen, meta, idx)
        screens_dict[meta["id"]] = scaled
    return screens_dict

def render_promo_video():
    """Renders 16:9 Landscape Full HD Promo Video using the official store-assets-generator compositor."""
    script_path = r"C:\Users\Anil\.gemini\config\skills\store-assets-generator\scripts\generate_official_promo_video.py"
    if not os.path.exists(script_path):
        print(f"Error: Promo video script not found at {script_path}")
        return

    raw_s1 = create_raw_ui_screen("01_water_eject")
    raw_s2 = create_raw_ui_screen("02_frequency_generator")
    raw_s3 = create_raw_ui_screen("03_earpiece_cleaning")
    raw_s4 = create_raw_ui_screen("04_stereo_audio_test")
    raw_s5 = create_raw_ui_screen("06_decibel_sound_meter")
    
    screenshots_dict = {
        "screen_eject": raw_s1,
        "screen_gen": raw_s2,
        "screen_ear": raw_s3,
        "screen_diag": raw_s4,
        "screen_meter": raw_s5
    }

    scenes_data = [
        {
            "screen": "screen_eject",
            "badge": "ACOUSTIC WATER EXPULSION",
            "badge_col": (0, 229, 255),
            "title": "Clear Water & Dust Instantly",
            "sub": "Tuned 165 Hz acoustic pressure waves safely push out trapped moisture",
            "cards": [
                ("1", (0, 229, 255), "Automatic Frequency Sweep", "Calibrated sound wave cycle expels droplets from speaker grills"),
                ("2", (0, 230, 118), "45° Downward Drainage Guide", "Built-in gravity sensor guides phone angle for maximum expulsion"),
                ("3", (255, 179, 0), "Volume Safety Restoration", "Auto-maximizes acoustic amplitude, then safely restores user volume")
            ],
            "stat": "PROVEN ACOUSTIC WAVE EJECTION • ZERO HARDWARE DAMAGE"
        },
        {
            "screen": "screen_gen",
            "badge": "TONE SYNTHESIZER",
            "badge_col": (0, 180, 216),
            "title": "1 Hz to 25,000 Hz Manual Synth",
            "sub": "Precision frequency control across sine, triangle, and square waveforms",
            "cards": [
                ("1", (0, 180, 216), "Dynamic Waveform Selector", "Switch between pure sine, rich square, and linear triangle waves"),
                ("2", (0, 229, 255), "Real-time Oscilloscope", "Live visual waveform feedback rendered at 60 FPS"),
                ("3", (171, 71, 188), "Quick Tuning Presets", "165 Hz, 440 Hz standard A, 1 kHz reference, and 12 kHz treble")
            ],
            "stat": "PRECISION 0.1 HZ STEP RESOLUTION • PURE NATIVE AUDIO PCM"
        },
        {
            "screen": "screen_ear",
            "badge": "DUAL SPEAKER TARGETING",
            "badge_col": (33, 150, 243),
            "title": "Earpiece & Loudspeaker Modes",
            "sub": "Targeted acoustic resonance tuned for delicate voice calls and main drivers",
            "cards": [
                ("1", (33, 150, 243), "Top Call Speaker Cleaning", "Gentle resonant acoustics safely clear top earpiece moisture"),
                ("2", (0, 229, 255), "Bottom Loudspeaker Mode", "High-displacement acoustic bursts clean bottom audio grills"),
                ("3", (0, 230, 118), "Independent Resonance Profiles", "Custom frequency algorithms prevent delicate voice coil fatigue")
            ],
            "stat": "DUAL-SPEAKER INDEPENDENT FREQUENCY TUNING"
        },
        {
            "screen": "screen_diag",
            "badge": "STEREO AUDIO DIAGNOSTICS",
            "badge_col": (0, 230, 118),
            "title": "Verify Acoustic Health & Balance",
            "sub": "Comprehensive diagnostic suite tests channel balance and sound clarity",
            "cards": [
                ("1", (0, 230, 118), "Stereo Left / Right Separation", "Isolate each speaker channel to detect blown or muffled cones"),
                ("2", (255, 179, 0), "Acoustic Frequency Sweep", "Full spectrum 20 Hz – 20 kHz clarity and distortion verification"),
                ("3", (0, 229, 255), "Instant Health Rating", "Get real-time audio quality and clarity pass/fail ratings")
            ],
            "stat": "FULL AUDIO SPECTRUM DIAGNOSTIC • 100% OFFLINE"
        },
        {
            "screen": "screen_meter",
            "badge": "DECIBEL SOUND METER",
            "badge_col": (255, 179, 0),
            "title": "Real-time dB Output Monitor",
            "sub": "Track volume gain and acoustic sound pressure before and after cleaning",
            "cards": [
                ("1", (255, 179, 0), "Live Sound Pressure Level (SPL)", "Precision decibel measurement with rolling waveform chart"),
                ("2", (0, 230, 118), "Before & After Gain Comparison", "Measure exact loudness gains after water expulsion"),
                ("3", (0, 229, 255), "Min, Average & Peak SPL", "Accurate acoustic telemetry with zero background data collection")
            ],
            "stat": "100% PRIVATE • ZERO INTERNET PERMISSION REQUIRED"
        }
    ]

    sys.path.insert(0, os.path.dirname(script_path))
    import generate_official_promo_video as compositor

    intro_pills = ["165 Hz Water Eject", "1 Hz – 25 kHz Synth", "Dual Speaker", "dB Sound Meter"]
    
    print("\n==================================================")
    print("Launching Official Promo Video Rendering Pipeline...")
    print("==================================================")
    
    video_path = compositor.render_promo_video(
        app_name="Speaker Cleaner",
        app_subtitle="Water Ejector & Acoustic Diagnostics",
        app_icon_path=APP_ICON_PATH,
        screenshots_dict=screenshots_dict,
        scenes_data=scenes_data,
        output_dir=RELEASE_DIR,
        duration=32.0,
        fps=30,
        intro_pills=intro_pills
    )
    
    # Enforce STRICT RULE: Exactly ONE video in root release folder
    dup_promo = os.path.join(RELEASE_DIR, "store_promo.mp4")
    if os.path.exists(dup_promo):
        os.remove(dup_promo)
        print(f"[STRICT RULE ENFORCED] Removed duplicate {dup_promo}. Exactly ONE video remains: {video_path}")

    return video_path

if __name__ == "__main__":
    print("==================================================")
    print("Speaker Cleaner — Store Assets Generation Pipeline")
    print("==================================================")
    generate_all_screenshots()
    render_promo_video()
    print("\n[COMPLETE] All Store Assets Successfully Generated in:")
    print(RELEASE_DIR)
