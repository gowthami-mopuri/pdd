package com.globaldental.pdd.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.globaldental.pdd.MainActivity;
import com.globaldental.pdd.R;
import com.globaldental.pdd.model.StaffAccount;
import com.globaldental.pdd.network.SupabaseClient;
import com.globaldental.pdd.util.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DoctorLoginFragment extends Fragment {

    private TextInputEditText etUsername;
    private TextInputEditText etPassword;
    private TextView tvError;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_doctor_login, container, false);

        sessionManager = new SessionManager(requireContext());

        etUsername = view.findViewById(R.id.et_username);
        etPassword = view.findViewById(R.id.et_password);
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

        // Fetch staff_accounts from Supabase
        SupabaseClient.getService().loginDoctor("eq." + username, "eq." + password)
                .enqueue(new Callback<List<StaffAccount>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<StaffAccount>> call, @NonNull Response<List<StaffAccount>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            StaffAccount doctor = response.body().get(0);
                            if ("suspended".equalsIgnoreCase(doctor.getStatus())) {
                                showError("This account has been suspended by the administrator.");
                            } else {
                                // Save session and redirect to doctor dashboard
                                sessionManager.saveDoctor(doctor);
                                loginSuccess();
                            }
                        } else {
                            showError("Invalid username or password.");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<StaffAccount>> call, @NonNull Throwable t) {
                        showError("Connection failed: " + t.getMessage());
                    }
                });
    }

    private void loginSuccess() {
        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            com.globaldental.pdd.util.SessionManager sm =
                new com.globaldental.pdd.util.SessionManager(requireContext());
            com.globaldental.pdd.model.StaffAccount doc = sm.getDoctor();
            if (doc != null) {
                activity.updateHeaderUser(doc.getUsername(), "Clinical Staff");
            }
            activity.replaceFragment(new com.globaldental.pdd.ui.dashboard.DashboardFragment());
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
