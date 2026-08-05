import re
with open('dist/assets/index-C-OAeCyq.js', 'r', encoding='utf-8', errors='ignore') as f:
    content = f.read()

# Look for patterns like path:"/something" or path: "/something" or path:'/something'
paths = re.findall(r'path\s*:\s*["\']([^"\']+)["\']', content)
print("Found paths in bundle:")
for p in sorted(set(paths)):
    print("  ", p)
