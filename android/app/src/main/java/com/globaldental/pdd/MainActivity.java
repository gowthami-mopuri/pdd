package com.globaldental.pdd;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private View appHeaderInclude;
    private View headerDivider;
    private TextView headerTitle;
    private TextView headerUserName;
    private TextView headerUserRole;
    private TextView headerAvatar;

    private final com.google.android.material.navigation.NavigationBarView.OnItemSelectedListener bottomNavListener =
        new com.google.android.material.navigation.NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                Fragment fragment = null;
                String title = "Dashboard Analytics";

                if (id == R.id.nav_dashboard) {
                    fragment = new com.globaldental.pdd.ui.dashboard.DashboardFragment();
                    title = "Dashboard Analytics";
                } else if (id == R.id.nav_patients) {
                    fragment = new com.globaldental.pdd.ui.patients.PatientsFragment();
                    title = "Patient Records";
                } else if (id == R.id.nav_ai_analysis) {
                    fragment = new com.globaldental.pdd.ui.ai.AIAnalysisFragment();
                    title = "AI Analysis";
                } else if (id == R.id.nav_implant_survival) {
                    fragment = new com.globaldental.pdd.ui.reports.ReportsFragment();
                    title = "Medical Reports";
                }

                if (fragment != null) {
                    loadFragment(fragment);
                    setHeaderTitle(title);
                }
                return true;
            }
        };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        com.globaldental.pdd.util.SessionManager sm = new com.globaldental.pdd.util.SessionManager(this);
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
            sm.isDarkMode() ? androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES : androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);
        // No title bar / action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_main);

        appHeaderInclude = findViewById(R.id.app_header_include);
        headerDivider    = findViewById(R.id.header_divider);
        headerTitle      = findViewById(R.id.header_title);
        headerUserName   = findViewById(R.id.header_user_name);
        headerUserRole   = findViewById(R.id.header_user_role);
        headerAvatar     = findViewById(R.id.header_avatar);

        bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(bottomNavListener);

        // Settings icon click
        View settingsIcon = findViewById(R.id.header_settings);
        if (settingsIcon != null) {
            settingsIcon.setOnClickListener(v ->
                loadFragment(new com.globaldental.pdd.ui.settings.SettingsPageFragment()));
        }

        // Profile chip click → open My Profile
        View profileChip = findViewById(R.id.header_profile_chip);
        if (profileChip != null) {
            profileChip.setOnClickListener(v ->
                loadFragment(new com.globaldental.pdd.ui.profile.MyProfileFragment()));
        }

        // Load Landing page on startup
        if (savedInstanceState == null) {
            loadFragment(new com.globaldental.pdd.ui.auth.LandingFragment());
        }
    }

    public void replaceFragment(Fragment fragment) {
        loadFragment(fragment);
    }

    public void setHeaderTitle(String title) {
        if (headerTitle != null) {
            headerTitle.setText(title);
        }
    }

    /**
     * Update the user info shown in the header chip.
     * Call this after a successful login with the logged-in user's name and role.
     */
    public void updateHeaderUser(String name, String role) {
        if (headerUserName != null) headerUserName.setText(name);
        if (headerUserRole != null) headerUserRole.setText(role);
        if (headerAvatar != null && name != null && name.length() >= 2) {
            headerAvatar.setText(name.substring(0, 2).toUpperCase());
        }
    }

    // ---- Compatibility stubs (kept so existing fragments compile) ----
    /** @deprecated Use showAppHeader() instead */
    public void showNavigationDrawer(boolean show) {
        // No-op: drawer has been replaced by the custom white header
        showAppHeader(show);
        showBottomNavigation(show);
    }

    /** @deprecated Use setHeaderTitle() instead */
    public void setToolbarTitle(String title) {
        setHeaderTitle(title);
    }
    // ------------------------------------------------------------------

    public void showAppHeader(boolean show) {
        if (appHeaderInclude != null) {
            appHeaderInclude.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (headerDivider != null) {
            headerDivider.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    public void showBottomNavigation(boolean show) {
        if (bottomNav != null) {
            bottomNav.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void loadFragment(Fragment fragment) {
        boolean isAuthFlow = fragment instanceof com.globaldental.pdd.ui.auth.LandingFragment
                || fragment instanceof com.globaldental.pdd.ui.auth.AdminLoginFragment
                || fragment instanceof com.globaldental.pdd.ui.auth.DoctorLoginFragment
                || fragment instanceof com.globaldental.pdd.ui.auth.PatientLoginFragment
                || fragment instanceof com.globaldental.pdd.ui.dashboard.AdminDashboardFragment;

        showAppHeader(!isAuthFlow);
        showBottomNavigation(!isAuthFlow);

        if (!isAuthFlow && bottomNav != null) {
            int checkedId = -1;
            if (fragment instanceof com.globaldental.pdd.ui.dashboard.DashboardFragment) {
                checkedId = R.id.nav_dashboard;
                setHeaderTitle("Dashboard Analytics");
            } else if (fragment instanceof com.globaldental.pdd.ui.patients.PatientsFragment
                    || fragment instanceof com.globaldental.pdd.ui.patients.AddPatientFragment) {
                checkedId = R.id.nav_patients;
                setHeaderTitle("Patient Records");
            } else if (fragment instanceof com.globaldental.pdd.ui.ai.AIAnalysisFragment) {
                checkedId = R.id.nav_ai_analysis;
                setHeaderTitle("AI Analysis");
            } else if (fragment instanceof com.globaldental.pdd.ui.reports.ReportsFragment) {
                checkedId = R.id.nav_implant_survival;
                setHeaderTitle("Medical Reports");
            } else if (fragment instanceof com.globaldental.pdd.ui.ai.ImplantSurvivalFragment) {
                checkedId = R.id.nav_implant_survival;
                setHeaderTitle("Implant Survival Report");
            } else if (fragment instanceof com.globaldental.pdd.ui.settings.SettingsPageFragment) {
                setHeaderTitle("Settings");
            } else if (fragment instanceof com.globaldental.pdd.ui.profile.MyProfileFragment) {
                setHeaderTitle("My Profile");
            }

            if (checkedId != -1 && bottomNav.getSelectedItemId() != checkedId) {
                bottomNav.setOnItemSelectedListener(null);
                bottomNav.setSelectedItemId(checkedId);
                bottomNav.setOnItemSelectedListener(bottomNavListener);
            }
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_content_frame, fragment);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
