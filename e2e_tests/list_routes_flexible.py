import re
with open('dist/assets/index-C-OAeCyq.js', 'r', encoding='utf-8', errors='ignore') as f:
    content = f.read()

# Let's search for "path:" in a case-sensitive or insensitive way and grab the surrounding characters
matches = []
for m in re.finditer(r'path\s*:', content):
    start = max(0, m.start() - 50)
    end = min(len(content), m.end() + 150)
    matches.append(content[start:end])

print(f"Found {len(matches)} occurrences of path:")
for i, m in enumerate(matches[:40]):
    print(f"\nMatch {i+1}:")
    print(repr(m))
