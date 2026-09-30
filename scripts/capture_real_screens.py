# -*- coding: utf-8 -*-
"""
Captures 8 authentic real screens from the active device in Dark Studio mode with pristine Demo Mode.
"""

import os
import sys
import time
import subprocess

adb = r'C:\Users\Anil\AppData\Local\Android\Sdk\platform-tools\adb.exe'
RELEASE_DIR = r"D:\Anil\MyProduct\Android Project App realse data\Speaker Cleaner"
RAW_DIR = os.path.join(RELEASE_DIR, "raw_screens")
os.makedirs(RAW_DIR, exist_ok=True)

def get_device():
    for _ in range(5):
        res = subprocess.run([adb, 'devices'], capture_output=True, text=True)
        for line in res.stdout.strip().splitlines()[1:]:
            parts = line.split()
            if len(parts) >= 2 and parts[1] == 'device':
                return parts[0]
        time.sleep(1)
    # Try connect if not found
    subprocess.run([adb, 'connect', '192.168.100.25:46373'], capture_output=True, text=True)
    time.sleep(1.5)
    res = subprocess.run([adb, 'devices'], capture_output=True, text=True)
    for line in res.stdout.strip().splitlines()[1:]:
        parts = line.split()
        if len(parts) >= 2 and parts[1] == 'device':
            return parts[0]
    return None

dev = get_device()
if not dev:
    print("Error: No device connected!")
    sys.exit(1)

print("Target device:", dev)

def run_adb(args):
    return subprocess.run([adb, '-s', dev] + args, capture_output=True, text=True)

def setup_demo_mode():
    print("Setting up Android Demo Mode (09:00, 100% battery)...")
    run_adb(['shell', 'settings', 'put', 'global', 'sysui_demo_allowed', '1'])
    run_adb(['shell', 'am', 'broadcast', '-a', 'com.android.systemui.demo', '-e', 'command', 'enter'])
    run_adb(['shell', 'am', 'broadcast', '-a', 'com.android.systemui.demo', '-e', 'command', 'clock', '-e', 'hhmm', '0900'])
    run_adb(['shell', 'am', 'broadcast', '-a', 'com.android.systemui.demo', '-e', 'command', 'battery', '-e', 'plugged', 'false', '-e', 'level', '100'])
    run_adb(['shell', 'am', 'broadcast', '-a', 'com.android.systemui.demo', '-e', 'command', 'network', '-e', 'wifi', 'show', '-e', 'level', '4'])
    run_adb(['shell', 'am', 'broadcast', '-a', 'com.android.systemui.demo', '-e', 'command', 'notifications', '-e', 'visible', 'false'])

def exit_demo_mode():
    run_adb(['shell', 'am', 'broadcast', '-a', 'com.android.systemui.demo', '-e', 'command', 'exit'])

def capture(name):
    temp = f"/sdcard/{name}.png"
    out = os.path.join(RAW_DIR, f"{name}.png")
    run_adb(['shell', 'screencap', '-p', temp])
    run_adb(['pull', temp, out])
    run_adb(['shell', 'rm', temp])
    print(f"Captured: {out} ({os.path.getsize(out)} bytes)")
    return out

def main():
    setup_demo_mode()
    
    # Bring app to front
    run_adb(['shell', 'am', 'start', '-n', 'com.shuttletechnologies.speakercleaner/.MainActivity'])
    time.sleep(1.5)

    # 1. Switch to Dark Studio theme
    print("Ensuring Dark Studio theme...")
    run_adb(['shell', 'input', 'tap', '967', '2157']) # Settings tab
    time.sleep(0.8)
    run_adb(['shell', 'input', 'tap', '414', '513'])  # Dark Studio chip
    time.sleep(0.8)

    # Screen 1: Tab 0 (Clean) - Default Water Eject
    print("Capturing Screen 1: Water Eject...")
    run_adb(['shell', 'input', 'tap', '112', '2157']) # Clean tab
    time.sleep(0.6)
    run_adb(['shell', 'input', 'tap', '325', '461'])  # Loudspeaker
    time.sleep(0.4)
    run_adb(['shell', 'input', 'tap', '202', '762'])  # Water Eject mode
    time.sleep(0.8)
    capture("01_water_eject")

    # Screen 2: Tab 1 (Generator) - Frequency Synthesizer
    print("Capturing Screen 2: Frequency Generator...")
    run_adb(['shell', 'input', 'tap', '325', '2157']) # Generator tab
    time.sleep(1.0)
    capture("02_frequency_generator")

    # Screen 3: Tab 0 (Clean) - Ear Receiver Mode
    print("Capturing Screen 3: Earpiece Cleaning...")
    run_adb(['shell', 'input', 'tap', '112', '2157']) # Clean tab
    time.sleep(0.6)
    run_adb(['shell', 'input', 'tap', '834', '461'])  # Ear Receiver
    time.sleep(0.8)
    capture("03_earpiece_cleaning")

    # Screen 4: Tab 2 (Diagnostics) - Stereo Channels & Audio Tests
    print("Capturing Screen 4: Audio Diagnostics...")
    run_adb(['shell', 'input', 'tap', '539', '2157']) # Diagnostics tab
    time.sleep(1.0)
    capture("04_stereo_audio_test")

    # Screen 5: Tab 0 (Clean) - Ultrasonic Mode
    print("Capturing Screen 5: Ultrasonic Mode...")
    run_adb(['shell', 'input', 'tap', '112', '2157']) # Clean tab
    time.sleep(0.6)
    run_adb(['shell', 'input', 'tap', '325', '461'])  # Loudspeaker
    time.sleep(0.4)
    run_adb(['shell', 'input', 'tap', '793', '988'])  # Ultrasonic mode
    time.sleep(0.8)
    capture("05_ultrasonic_clean")

    # Screen 6: Tab 0 (Clean) - Dust Blast Mode
    print("Capturing Screen 6: Dust Blast Mode...")
    run_adb(['shell', 'input', 'tap', '112', '2157']) # Clean tab
    time.sleep(0.6)
    run_adb(['shell', 'input', 'tap', '539', '762'])  # Dust Blast mode
    time.sleep(0.8)
    capture("06_dust_blast")

    # Screen 7: Tab 3 (History) - Cleaning Records
    print("Capturing Screen 7: Cleaning History...")
    run_adb(['shell', 'input', 'tap', '753', '2157']) # History tab
    time.sleep(1.0)
    capture("07_history_sessions")

    # Screen 8: Tab 4 (Settings) - Settings & Appearance
    print("Capturing Screen 8: Settings & Appearance...")
    run_adb(['shell', 'input', 'tap', '967', '2157']) # Settings tab
    time.sleep(1.0)
    capture("08_settings_themes")

    exit_demo_mode()
    print("\n[SUCCESS] All 8 real device screenshots captured in:", RAW_DIR)

if __name__ == "__main__":
    main()
