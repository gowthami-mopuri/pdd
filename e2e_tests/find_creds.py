"""Search the JS bundle for hardcoded login credentials."""
import sys, re, os
sys.stdout.reconfigure(encoding='utf-8')

dist_assets = r'dist\assets'
js_files = [f for f in os.listdir(dist_assets) if f.endswith('.js')]
print('JS files:', js_files)

for jf in js_files:
    with open(os.path.join(dist_assets, jf), 'r', encoding='utf-8', errors='ignore') as f:
        content = f.read()

    print(f'\n=== {jf} ({len(content):,} chars) ===')

    # Search for credential-related keywords
    keywords = [
        'validateCredentials', 'hardcoded', 'VALID_', 'const users',
        'USERS', 'credentials', 'checkLogin', 'verifyLogin',
        'username:', 'password:', 'correctPassword', 'correctUsername',
        'HARDCODED', 'adminUser', 'doctorUser', 'staffUser',
    ]
    for kw in keywords:
        idx = content.find(kw)
        if idx != -1:
            print(f'\nKEYWORD: [{kw}] at pos {idx}')
            print(repr(content[max(0, idx-150):idx+400]))
            print('---')

    # Pattern: anything that looks like user/pass assignment
    matches = re.findall(r'["\'](?:username|user|login|pass|password)["\']:?\s*["\']([^"\']{2,30})["\']', content, re.I)
    if matches:
        print(f'\nUser/pass values: {list(set(matches))}')

    # Look for === comparisons with literals (hardcoded auth checks)
    eq_checks = re.findall(r'===\s*["\']([^"\']{2,30})["\']', content)
    eq_checks += re.findall(r'["\']([^"\']{2,30})["\'\s]*===', content)
    creds = [v for v in eq_checks if len(v) > 2 and len(v) < 25
             and not v.startswith('/') and not v.startswith('http')]
    if creds:
        print(f'\nHardcoded string comparisons (potential creds): {list(set(creds))[:30]}')

    # Supabase auth sign in calls
    supabase_auth = re.findall(r'signIn.{0,200}', content)
    if supabase_auth:
        print(f'\nSupabase signIn calls:')
        for m in supabase_auth[:5]:
            print(f'  {repr(m[:200])}')
