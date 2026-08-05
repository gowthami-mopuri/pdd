"""Deep search for login validation logic in the JS bundle."""
import sys, re
sys.stdout.reconfigure(encoding='utf-8')

with open(r'dist/assets/index-C-OAeCyq.js', 'r', encoding='utf-8', errors='ignore') as f:
    content = f.read()

# 1. Find the login/auth component around 'doctorUser' localStorage key
idx = content.find('doctorUser')
if idx != -1:
    # Search back 3000 chars for the login form / handleLogin / handleSubmit
    region = content[max(0, idx-5000):idx+5000]
    print('=== REGION AROUND doctorUser ===')
    print(region)
    print('\n\n')

# 2. Find handleLogin / handleSubmit / onSubmit patterns
for kw in ['handleLogin', 'handleSubmit', 'onSubmit', 'signIn', 'login(', 'checkPassword']:
    idxs = [m.start() for m in re.finditer(re.escape(kw), content)]
    for ix in idxs[:3]:
        ctx = content[max(0, ix-100):ix+500]
        if any(c in ctx for c in ['username', 'password', 'localStorage', 'doctor']):
            print(f'\n=== {kw} at {ix} ===')
            print(ctx)
            print('---')

# 3. Find the login page component
login_idx = content.find('login-page')
if login_idx != -1:
    print(f'\n=== login-page component (pos {login_idx}) ===')
    print(content[max(0, login_idx-3000):login_idx+3000])

# 4. Look for Supabase staff/doctor table queries
for kw in ['staff', 'doctor', 'clinical', 'select', 'from', 'where']:
    matches = re.findall(f'supabase.*?{kw}.*?\.{{0,200}}', content, re.I | re.S)
    if matches:
        print(f'\n=== Supabase + {kw} ===')
        for m in matches[:3]:
            print(repr(m[:300]))

# 5. Check if there's a registered_staff or staff table
for kw in ['registered_staff', 'staff_table', 'from("staff")', "from('staff')", 'staff_members']:
    idx2 = content.find(kw)
    if idx2 != -1:
        print(f'\n=== TABLE: {kw} at {idx2} ===')
        print(content[max(0, idx2-200):idx2+500])
