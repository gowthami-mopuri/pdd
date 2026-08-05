package com.globaldental.pdd.ui.auth;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.globaldental.pdd.MainActivity;
import com.globaldental.pdd.R;
import com.globaldental.pdd.util.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

public class AdminLoginFragment extends Fragment {

    private TextInputEditText etUsername;
    private TextInputEditText etPassword;
    private TextView tvError;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_login, container, false);

        sessionManager = new SessionManager(requireContext());

        etUsername = view.findViewById(R.id.et_admin_id);
        etPassword = view.findViewById(R.id.et_admin_password);
        tvError = view.findViewById(R.id.tv_error);

        view.findViewById(R.id.btn_back).setOnClickListener(v -> navigateBack());
        view.findViewById(R.id.btn_login).setOnClickListener(v -> performLogin());

        return view;
    }

    private void performLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        tvError.setVisibility(View.GONE);

        // Standard delay mimicking auth network response
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if ("admin".equals(username) && "admin123".equals(password)) {
                sessionManager.saveAdmin(true);
                loginSuccess();
            } else {
                showError("Invalid admin credentials. Access denied.");
            }
        }, 600);
    }

    private void loginSuccess() {
        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            activity.showNavigationDrawer(false); // Hide the standard sidebar drawer
            activity.setToolbarTitle("Admin Workspace");
            activity.replaceFragment(new com.globaldental.pdd.ui.dashboard.AdminDashboardFragment());
        }
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    private void navigateBack() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).replaceFragment(new LandingFragment());
        }
    }
}
