package com.globaldental.pdd.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import com.globaldental.pdd.MainActivity;
import com.globaldental.pdd.R;
import com.globaldental.pdd.ui.auth.LandingFragment;
import com.globaldental.pdd.util.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsPageFragment extends Fragment {

    private SwitchMaterial switchDarkMode;
    private MaterialButton btnLogout;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings_page, container, false);

        sessionManager = new SessionManager(requireContext());
        switchDarkMode = view.findViewById(R.id.switch_dark_mode);
        btnLogout      = view.findViewById(R.id.btn_logout);

        // Configure toolbar
        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            activity.showNavigationDrawer(true);
            activity.setToolbarTitle("Settings");
        }

        // Dark mode state from SessionManager
        boolean isDark = sessionManager.isDarkMode();
        switchDarkMode.setChecked(isDark);

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (sessionManager.isDarkMode() != isChecked) {
                sessionManager.setDarkMode(isChecked);
                AppCompatDelegate.setDefaultNightMode(isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
                Toast.makeText(requireContext(), isChecked ? "Dark Mode Enabled" : "Light Mode Enabled", Toast.LENGTH_SHORT).show();
                if (getActivity() != null) {
                    getActivity().recreate();
                }
            }
        });

        btnLogout.setOnClickListener(v -> performLogout());

        return view;
    }

    private void performLogout() {
        if (!isAdded()) return;
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    sessionManager.logout();
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).replaceFragment(new LandingFragment());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
