"""Extract login page component and staff_accounts SQL from bundle."""
import sys, re
sys.stdout.reconfigure(encoding='utf-8')

with open(r'dist/assets/index-C-OAeCyq.js', 'r', encoding='utf-8', errors='ignore') as f:
    content = f.read()

# 1. Find the staff_accounts SQL DDL
idx = content.find('staff_accounts')
while idx != -1:
    print(f'\n=== staff_accounts at {idx} ===')
    print(content[max(0, idx-200):idx+800])
    print('---')
    idx = content.find('staff_accounts', idx+1)

# 2. Find the login-page component (handles form submission)
idx = content.find('login-page')
if idx != -1:
    # Go back to find the function definition
    region = content[max(0, idx-8000):idx+2000]
    print(f'\n=== login-page COMPONENT ===')
    print(region)

# 3. Find localStorage.setItem doctorUser
for kw in ['setItem(`doctorUser', "setItem('doctorUser", 'setItem("doctorUser']:
    idx2 = content.find(kw)
    if idx2 != -1:
        print(f'\n=== {kw} at {idx2} ===')
        print(content[max(0, idx2-2000):idx2+1000])
