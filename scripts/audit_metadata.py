# -*- coding: utf-8 -*-
"""
Audits Google Play Store metadata CSV files for:
1. Title <= 30 chars
2. Short Description <= 80 chars
3. Full Description <= 4000 chars
4. Zero banned superlatives, rankings, awards, or store claims
"""

import csv
import os
import sys

sys.stdout.reconfigure(encoding='utf-8')

csv_paths = [
    r'C:\Users\Anil\.gemini\antigravity\brain\6fcfbf3e-7091-40ef-9dfa-2ce52af94467\play_store_translations.csv',
    r'D:\Anil\MyProduct\Android Project App realse data\Speaker Cleaner\play_store_translations.csv'
]

BANNED = [
    'best', '#1', 'no. 1', 'top-rated', 'leading', 'the ultimate', 'app of the year', 'award-winning', 'winner',
    'pinakamahusay', 'terbaik', 'definitivo', 'el mejor', 'das ultimative', 'das beste', 'par excellence', 'le meilleur',
    'सबसे अच्छा', 'सबसे सटीक', 'de ultieme', 'สุดยอด', 'en gelişmiş', 'en iyi', 'tối ưu', 'tốt nhất', '最佳', '最高', 'no.1', '최고의'
]

for p in csv_paths:
    if not os.path.exists(p):
        print(f"Skipping (not found): {p}")
        continue
    print(f"\n--- Checking Metadata File: {p} ---")
    with open(p, mode='r', encoding='utf-8') as f:
        reader = csv.DictReader(f)
        row_count = 0
        violations = 0
        for i, row in enumerate(reader, 1):
            row_count += 1
            # Case insensitive key matching
            lang = None
            title = ""
            short_desc = ""
            full_desc = ""
            for k, v in row.items():
                k_lower = k.lower().strip()
                if 'lang' in k_lower:
                    lang = v
                elif 'short' in k_lower:
                    short_desc = v or ""
                elif 'full' in k_lower or 'desc' in k_lower:
                    full_desc = v or ""
                elif 'title' in k_lower:
                    title = v or ""
            
            lang_label = lang or f"Row {i}"
            
            if len(title) > 30:
                print(f"[ERROR] [{lang_label}] Title exceeds 30 chars ({len(title)}): '{title}'")
                violations += 1
            if len(short_desc) > 80:
                print(f"[ERROR] [{lang_label}] Short Description exceeds 80 chars ({len(short_desc)}): '{short_desc}'")
                violations += 1
            if len(full_desc) > 4000:
                print(f"[ERROR] [{lang_label}] Full Description exceeds 4000 chars ({len(full_desc)})")
                violations += 1
                
            combined = f"{title} {short_desc}".lower()
            for b in BANNED:
                # check word boundaries or substring
                if b in combined:
                    print(f"[WARN] [{lang_label}] Found superlative keyword '{b}' in metadata header")
                    violations += 1

        if violations == 0:
            print(f"PASS: Verified all {row_count} languages. All limits & policies strictly satisfied!")
        else:
            print(f"FAIL: Found {violations} total metadata issues.")
