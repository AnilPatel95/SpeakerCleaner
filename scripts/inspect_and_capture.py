import subprocess
import time
import os
import re

adb = r'C:\Users\Anil\AppData\Local\Android\Sdk\platform-tools\adb.exe'
subprocess.run([adb, 'connect', '192.168.100.25:46373'], capture_output=True, text=True)
time.sleep(1)
res = subprocess.run([adb, 'devices'], capture_output=True, text=True)
dev = [l.split()[0] for l in res.stdout.strip().splitlines()[1:] if 'device' in l][0]

subprocess.run([adb, '-s', dev, 'shell', 'am', 'start', '-n', 'com.shuttletechnologies.speakercleaner/.MainActivity'])
time.sleep(1.5)
subprocess.run([adb, '-s', dev, 'shell', 'input', 'tap', '967', '2157'])
time.sleep(1.0)

dump_local = r'D:\Anil\MyProduct\Android Project App realse data\Speaker Cleaner\settings_dump.xml'
subprocess.run([adb, '-s', dev, 'shell', 'uiautomator', 'dump', '/sdcard/settings_dump.xml'])
subprocess.run([adb, '-s', dev, 'pull', '/sdcard/settings_dump.xml', dump_local])

with open(dump_local, 'r', encoding='utf-8', errors='ignore') as f:
    xml = f.read()

pattern = r'text="([^"]+)"[^>]+bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"'
for text, x1, y1, x2, y2 in re.findall(pattern, xml):
    cx = (int(x1) + int(x2)) // 2
    cy = (int(y1) + int(y2)) // 2
    print(f'Text: "{text}" -> Bounds: [{x1},{y1}][{x2},{y2}], Center: ({cx}, {cy})')
