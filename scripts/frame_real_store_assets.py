# -*- coding: utf-8 -*-
"""
Shuttle Technologies — Luxury Framing Engine & Promo Video Compositor using Real Device Captures
"""

import os
import sys
import time
import math
import subprocess
import shutil
from PIL import Image, ImageDraw, ImageFont, ImageFilter

RELEASE_DIR = r"D:\Anil\MyProduct\Android Project App realse data\Speaker Cleaner"
RAW_DIR = os.path.join(RELEASE_DIR, "raw_screens")
SCREENSHOTS_DIR = os.path.join(RELEASE_DIR, "screenshots")
APP_ICON_PATH = os.path.join(RELEASE_DIR, "app_icon_512x512.png")

os.makedirs(SCREENSHOTS_DIR, exist_ok=True)

# Luxury Dark Studio Palette
DARK_BG = (10, 14, 23, 255)
DARK_SURFACE = (18, 24, 38, 255)
DARK_SURFACE_ELEVATED = (24, 34, 54, 255)
TEXT_WHITE = (245, 248, 252, 255)
TEXT_SECONDARY = (160, 178, 200, 255)
TEXT_MUTED = (110, 126, 148, 255)
BORDER_COLOR = (42, 58, 86, 255)

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
        "id": "06_dust_blast",
        "pill": "RAPID DUST AGITATION",
        "pill_color": (255, 179, 0),
        "title": "High-Velocity Dust Blast",
        "subtitle": "Rapid kinetic sound pulses dislodge trapped dirt & lint"
    },
    {
        "id": "07_history_sessions",
        "pill": "SESSION LOGS & ACOUSTIC GUIDE",
        "pill_color": (0, 229, 255),
        "title": "Cleaning Records & Physics Guide",
        "subtitle": "Track cleaning cycles and learn acoustic ejection science"
    },
    {
        "id": "08_settings_themes",
        "pill": "CURATED THEMES & 16 LANGUAGES",
        "pill_color": (0, 230, 118),
        "title": "Pitch Black AMOLED & Studio Dark",
        "subtitle": "100% offline-first architecture with 16 global translations"
    }
]

def clean_real_screen(raw_path):
    """Crops 3-button system navigation bar at the bottom and replaces status bar with pristine 09:00/100%."""
    img = Image.open(raw_path).convert("RGBA")
    w, h = img.size
    
    # Crop off Android 3-button navigation bar at the bottom (y > 2200)
    cropped = img.crop((0, 0, w, 2200))
    cw, ch = cropped.size
    
    # Re-draw clean status bar over top 100px
    draw = ImageDraw.Draw(cropped)
    # Fill top status bar area with dark studio background color
    draw.rectangle([0, 0, cw, 100], fill=(10, 14, 23))
    
    f_time = get_font(34, bold=True)
    draw.text((60, 26), "09:00", fill=TEXT_WHITE, font=f_time)
    
    f_meta = get_font(28, bold=True)
    draw.text((cw - 240, 30), "5G  100%", fill=TEXT_WHITE, font=f_meta)
    
    return cropped

def frame_screenshot(real_img, meta, idx):
    canvas_w, canvas_h = 1080, 2280
    canvas = Image.new("RGBA", (canvas_w, canvas_h), (10, 14, 23, 255))
    draw = ImageDraw.Draw(canvas)

    # Ambient radial glow behind phone chassis (y = 1250)
    pill_col = meta["pill_color"]
    glow = Image.new("RGBA", (canvas_w, canvas_h), (0, 0, 0, 0))
    gdraw = ImageDraw.Draw(glow)
    for rad in range(580, 100, -50):
        alpha = int(35 * (1.0 - rad / 580.0))
        gdraw.ellipse([canvas_w // 2 - rad, 1250 - rad, canvas_w // 2 + rad, 1250 + rad], fill=(*pill_col[:3], alpha))
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

    # Chassis Drop Shadow
    shadow = Image.new("RGBA", (canvas_w, canvas_h), (0, 0, 0, 0))
    sdraw = ImageDraw.Draw(shadow)
    sdraw.rounded_rectangle([phone_x - 8, phone_y + 8, phone_x + phone_w + 8, phone_y + phone_h + 24], radius=bezel_radius + 4, fill=(0, 0, 0, 160))
    shadow = shadow.filter(ImageFilter.GaussianBlur(18))
    canvas.paste(shadow, (0, 0), shadow)

    # Metallic Outer Chassis Bezel
    draw.rounded_rectangle([phone_x, phone_y, phone_x + phone_w, phone_y + phone_h], radius=bezel_radius, fill=(28, 36, 52), outline=(55, 72, 100), width=4)
    # Inner Bezel Ring
    draw.rounded_rectangle([phone_x + 6, phone_y + 6, phone_x + phone_w - 6, phone_y + phone_h - 6], radius=bezel_radius - 4, fill=(12, 16, 24), outline=(18, 24, 34), width=3)

    # Screen Display Area
    screen_rect = [phone_x + 16, phone_y + 16, phone_x + phone_w - 16, phone_y + phone_h - 16]
    screen_w = screen_rect[2] - screen_rect[0]
    screen_h = screen_rect[3] - screen_rect[1]

    if real_img is not None:
        scaled_screen = real_img.resize((screen_w, screen_h), Image.Resampling.LANCZOS)
        mask = Image.new("L", (screen_w, screen_h), 0)
        mdraw = ImageDraw.Draw(mask)
        mdraw.rounded_rectangle([0, 0, screen_w, screen_h], radius=bezel_radius - 12, fill=255)
        canvas.paste(scaled_screen, (screen_rect[0], screen_rect[1]), mask)
    else:
        draw.rounded_rectangle(screen_rect, radius=bezel_radius - 12, fill=DARK_SURFACE)

    # Top Camera Punch Hole
    cam_x, cam_y = canvas_w // 2, phone_y + 36
    draw.ellipse([cam_x - 8, cam_y - 8, cam_x + 8, cam_y + 8], fill=(6, 10, 18), outline=(35, 48, 70), width=1)

    out_file = os.path.join(SCREENSHOTS_DIR, f"{meta['id']}.png")
    canvas.convert("RGB").save(out_file, "PNG")
    print(f"Saved Real Framed Screenshot [{idx}/8]: {out_file}")
    return real_img

def generate_all_real_framed_screenshots():
    print("Framing 8 Real Device Screenshots...")
    screens_dict = {}
    for idx, meta in enumerate(SCREENSHOTS_METADATA, start=1):
        raw_file = os.path.join(RAW_DIR, f"{meta['id']}.png")
        if not os.path.exists(raw_file):
            print(f"Warning: {raw_file} not found!")
            continue
        cleaned = clean_real_screen(raw_file)
        frame_screenshot(cleaned, meta, idx)
        screens_dict[meta["id"]] = cleaned
    return screens_dict

def render_promo_video(screens_dict):
    """Renders 16:9 Landscape Full HD Promo Video using real device captures."""
    script_path = r"C:\Users\Anil\.gemini\config\skills\store-assets-generator\scripts\generate_official_promo_video.py"
    if not os.path.exists(script_path):
        print(f"Error: Promo video script not found at {script_path}")
        return

    screenshots_for_video = {
        "screen_eject": screens_dict.get("01_water_eject"),
        "screen_gen": screens_dict.get("02_frequency_generator"),
        "screen_ear": screens_dict.get("03_earpiece_cleaning"),
        "screen_diag": screens_dict.get("04_stereo_audio_test"),
        "screen_meter": screens_dict.get("05_ultrasonic_clean")
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
            "badge": "ULTRASONIC AGITATION",
            "badge_col": (171, 71, 188),
            "title": "Silent Micro-Dust Vibrations",
            "sub": "Inaudible 18.5 kHz – 21.5 kHz frequencies break electrostatic dirt adhesion",
            "cards": [
                ("1", (171, 71, 188), "Quiet Operation", "Safe to run in public, meetings, or quiet offices without noise"),
                ("2", (0, 230, 118), "Dry Dust & Lint Dispersion", "Dislodges pocket lint from speaker mesh fibers safely"),
                ("3", (0, 229, 255), "Dual Driver Support", "Selectable for both top earpiece and bottom loudspeaker")
            ],
            "stat": "100% PRIVATE • ZERO INTERNET PERMISSION REQUIRED"
        }
    ]

    sys.path.insert(0, os.path.dirname(script_path))
    import generate_official_promo_video as compositor

    intro_pills = ["165 Hz Water Eject", "1 Hz – 25 kHz Synth", "Dual Speaker", "Audio Diagnostics"]
    
    print("\n==================================================")
    print("Launching Official Promo Video Rendering with Real Captures...")
    print("==================================================")
    
    video_path = compositor.render_promo_video(
        app_name="Speaker Cleaner",
        app_subtitle="Water Ejector & Acoustic Diagnostics",
        app_icon_path=APP_ICON_PATH,
        screenshots_dict=screenshots_for_video,
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
    print("Speaker Cleaner — Real Device Framing & Promo Video")
    print("==================================================")
    screens_dict = generate_all_real_framed_screenshots()
    render_promo_video(screens_dict)
    print("\n[COMPLETE] Real Store Assets & Video Successfully Generated in:")
    print(RELEASE_DIR)
