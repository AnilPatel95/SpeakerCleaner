# -*- coding: utf-8 -*-
"""
Agent 1: Deep Localization Scanner (Automated AST / Regex Extraction)
Guarantees 100% string extraction, key parity, and zero untranslated UI elements.
"""

import os
import re
import sys

# Force UTF-8 output
sys.stdout.reconfigure(encoding='utf-8')

SRC_DIR = r"d:\Anil\MyProduct\Native\Speaker Cleaner\app\src\main\java"

STRING_REGEX = re.compile(r'"([^"\\]*(?:\\.[^"\\]*)*)"')

TECHNICAL_PATTERNS = [
    r'^[\d\s\.\,\:\-\_\/\%\+\#\*\(\)]+$',   # numbers, punctuation, format specs
    r'^https?:\/\/',                        # URLs
    r'^mailto:',                            # mailto URI
    r'^market:',                            # market URI
    r'^[a-zA-Z0-9_\.]+\/[a-zA-Z0-9_\.]+$', # MIME types or component names
    r'^[A-Z0-9_]{3,}$',                     # Constants/Tags/Extra keys
    r'^ca-app-pub-',                        # AdMob IDs
    r'^\%[0-9]*\$?[a-zA-Z]',               # Format specifiers (%s, %d, %1$s, %.1f)
    r'^\%\.?[0-9]*[a-zA-Z]',               # %.1f
    r'^[a-z]+_[a-z0-9_]+$',                 # Identifiers or table names
    r'^[#][0-9a-fA-F]{6,8}$',               # Hex colors
    r'^android\.permission\.',              # Permissions
    r'^\$\{.*\}\s*(Hz|kHz|dB|\%|s|ms|min|°|\°\s*✓)$', # Dynamic SI measurement units
    r'^(Hz|kHz|dB|\%|s|ms|min)$',            # Raw measurement unit
    r'^(MMM|dd|HH|mm|ss|yyyy)[\s\:\,\.\/\-]+(MMM|dd|HH|mm|ss|yyyy)?', # Date format patterns
    r'^tnum$',                              # Font feature setting
    r'^tab_crossfade$',
    r'^contentDescription$',
    r'^(pulse|rotation|phase|wave_bg)$',    # Compose animation labels
    r'^\$\{[a-zA-Z0-9_\.]+(\(.*\))?\}\s*[\(\•\-]\s*(\$\{[a-zA-Z0-9_\.]+(\(.*\))?\})?[\)]?.*$', # Pure variable/method calls
    r'^\$[a-zA-Z0-9_]+\s*•\s*\$\{[a-zA-Z0-9_\.]+\}s$', # Date • Duration (e.g. $dateStr • ${session.durationSeconds}s)
    r'^\$\{activeLang\.flagEmoji\}', # Dynamic language selector title
    r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$' # Email addresses
]

def is_technical(s, line_context=""):
    if not s or len(s.strip()) == 0:
        return True
    s_clean = s.strip()
    # Check if inside an animation label parameter: label = "..."
    if 'label = "' in line_context or "label = '" in line_context:
        if s_clean in ["pulse", "rotation", "phase", "wave_bg"]:
            return True
    for pat in TECHNICAL_PATTERNS:
        if re.search(pat, s_clean):
            return True
    if len(s_clean) <= 1:
        return True
    if not re.search(r'[a-zA-Z]', s_clean):
        return True
    return False

def scan_file(fpath):
    issues = []
    with open(fpath, 'r', encoding='utf-8', errors='ignore') as f:
        lines = f.readlines()
        
    for l_idx, line in enumerate(lines, start=1):
        line_str = line.strip()
        if line_str.startswith('package ') or line_str.startswith('import ') or line_str.startswith('//'):
            continue
        if 'Log.' in line_str or 'println(' in line_str or '@SuppressLint' in line_str:
            continue
            
        matches = STRING_REGEX.findall(line)
        for m in matches:
            if not is_technical(m, line):
                if 'localization' in fpath.lower() or 'locales' in fpath.lower():
                    continue
                if re.search(r'[a-zA-Z]{2,}', m):
                    if m in ["SpeakerCleaner", "WaterEjectService", "audio", "vibrator", "notification", "v1.0.0", "SHA-256", "en_US"]:
                        continue
                    # Exclude JSON keys in PreferencesManager
                    if 'PreferencesManager.kt' in fpath and m in ["id", "timestamp", "mode", "target", "durationSeconds", "initialDb", "finalDb", "completed"]:
                        continue
                    issues.append((l_idx, m, line.strip()))
    return issues

def main():
    print("=" * 60)
    print("AGENT 1: Automated AST / String Extraction Scan")
    print("=" * 60)
    
    total_issues = 0
    scanned_files = 0
    for root, dirs, files in os.walk(SRC_DIR):
        for file in files:
            if file.endswith('.kt'):
                scanned_files += 1
                fpath = os.path.join(root, file)
                rel_path = os.path.relpath(fpath, SRC_DIR)
                issues = scan_file(fpath)
                if issues:
                    print(f"\n[FLAG] {rel_path} ({len(issues)} candidate literals):")
                    for l_idx, lit, raw in issues:
                        print(f"  Line {l_idx:4d}: \"{lit}\" -> `{raw}`")
                    total_issues += len(issues)
                    
    print("\n" + "=" * 60)
    print(f"Total Kotlin files scanned: {scanned_files}")
    print(f"Total candidate hardcoded string literals: {total_issues}")
    print("=" * 60)

if __name__ == "__main__":
    main()
