import xml.etree.ElementTree as ET
import sys
from pathlib import Path

dump_path = Path(sys.argv[1]) if len(sys.argv) > 1 else Path(__file__).with_name("window_dump.xml")
tree = ET.parse(dump_path)
root = tree.getroot()

for node in root.iter('node'):
    text = node.attrib.get('text', '')
    desc = node.attrib.get('content-desc', '')
    bounds = node.attrib.get('bounds', '')
    if text or desc:
        print(f"TEXT: {text} | DESC: {desc} | BOUNDS: {bounds}")
