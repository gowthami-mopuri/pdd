package com.globaldental.pdd.ui.auth;

import android.app.DatePickerDialog;
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
import com.globaldental.pdd.model.Patient;
import com.globaldental.pdd.network.SupabaseClient;
import com.globaldental.pdd.util.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Calendar;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PatientLoginFragment extends Fragment {

    private TextInputEditText etPatientId;
    private TextInputEditText etDob;
    private TextView tvError;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_login, container, false);

        sessionManager = new SessionManager(requireContext());

        etPatientId = view.findViewById(R.id.et_patient_id);
        etDob = view.findViewById(R.id.et_dob);
        tvError = view.findViewById(R.id.tv_error);

        etDob.setOnClickListener(v -> showDatePicker());
        view.findViewById(R.id.btn_back).setOnClickListener(v -> navigateBack());
        view.findViewById(R.id.btn_login).setOnClickListener(v -> performLogin());

        return view;
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog picker = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            String date = year + "-" + String.format("%02d", month + 1) + "-" + String.format("%02d", dayOfMonth);
            etDob.setText(date);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        picker.show();
    }

    private void performLogin() {
        String patientId = etPatientId.getText().toString().trim();

        if (patientId.isEmpty()) {
            showError("Please enter your Patient ID.");
            return;
        }

        tvError.setVisibility(View.GONE);

        // Fetch patient by ID from Supabase
        SupabaseClient.getService().loginPatient("eq." + patientId)
                .enqueue(new Callback<List<Patient>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Patient>> call, @NonNull Response<List<Patient>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            Patient patient = response.body().get(0);
                            sessionManager.savePatient(patient);
                            loginSuccess();
                        } else {
                            showError("Invalid Patient ID. Please check your records.");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Patient>> call, @NonNull Throwable t) {
                        showError("Connection failed: " + t.getMessage());
                    }
                });
    }

    private void loginSuccess() {
        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            activity.showNavigationDrawer(false); // Hide the doctor's sidebar drawer
            activity.setToolbarTitle("Patient Portal");
            activity.replaceFragment(new com.globaldental.pdd.ui.dashboard.PatientDashboardFragment());
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
