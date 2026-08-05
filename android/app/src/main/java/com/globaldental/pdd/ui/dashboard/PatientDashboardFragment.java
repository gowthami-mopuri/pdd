package com.globaldental.pdd.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.globaldental.pdd.MainActivity;
import com.globaldental.pdd.R;
import com.globaldental.pdd.model.Patient;
import com.globaldental.pdd.model.Report;
import com.globaldental.pdd.ui.ai.ChatbotDialog;
import com.globaldental.pdd.ui.auth.LandingFragment;
import com.globaldental.pdd.ui.patients.ReportAdapter;
import com.globaldental.pdd.util.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

public class PatientDashboardFragment extends Fragment {

    private TextView tvWelcome;
    private TextView tvId;
    private TextView tvAge;
    private TextView tvGender;
    private TextView tvDimensions;
    private TextView tvStatus;
    private TextView tvNoReports;
    private RecyclerView rvReports;
    private SessionManager sessionManager;
    private Patient patient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_dashboard, container, false);

        sessionManager = new SessionManager(requireContext());
        patient = sessionManager.getPatient();

        // Redirect to login if session is empty
        if (patient == null) {
            navigateBack();
            return view;
        }

        // Configure toolbar (hide drawer toggle for patients, set title)
        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            activity.showNavigationDrawer(false);
            activity.setToolbarTitle("Patient Portal");
        }

        tvWelcome = view.findViewById(R.id.tv_patient_welcome);
        tvId = view.findViewById(R.id.tv_profile_id);
        tvAge = view.findViewById(R.id.tv_profile_age);
        tvGender = view.findViewById(R.id.tv_profile_gender);
        tvDimensions = view.findViewById(R.id.tv_profile_dimensions);
        tvStatus = view.findViewById(R.id.tv_profile_status);
        tvNoReports = view.findViewById(R.id.tv_no_reports);
        rvReports = view.findViewById(R.id.rv_saved_reports);

        // Bind patient profile data
        tvWelcome.setText("Welcome back, " + patient.getName());
        tvId.setText(patient.getPatientId());
        tvAge.setText(patient.getAge() != null ? patient.getAge() + " years" : "N/A");
        tvGender.setText(patient.getGender() != null ? patient.getGender() : "N/A");
        tvDimensions.setText(
                (patient.getWeight() != null ? patient.getWeight() + "kg" : "N/A") + " / " +
                (patient.getHeight() != null ? patient.getHeight() + "cm" : "N/A")
        );
        tvStatus.setText(patient.getStatus() != null ? patient.getStatus() : "Consultation");

        // Load reports list
        rvReports.setLayoutManager(new LinearLayoutManager(requireContext()));
        List<Report> reports = sessionManager.getReportsForPatient(patient.getPatientId());
        if (reports.isEmpty()) {
            tvNoReports.setVisibility(View.VISIBLE);
            rvReports.setVisibility(View.GONE);
        } else {
            tvNoReports.setVisibility(View.GONE);
            rvReports.setVisibility(View.VISIBLE);
            ReportAdapter adapter = new ReportAdapter(reports, report -> openFullReport(report));
            rvReports.setAdapter(adapter);
        }

        view.findViewById(R.id.btn_sign_out).setOnClickListener(v -> performLogout());

        // Chatbot floating action button
        FloatingActionButton fab = view.findViewById(R.id.fab_chatbot);
        fab.setOnClickListener(v -> {
            ChatbotDialog dialog = ChatbotDialog.newInstance(patient);
            dialog.show(getParentFragmentManager(), "ChatbotDialog");
        });

        return view;
    }

    private void performLogout() {
        sessionManager.logout();
        navigateBack();
    }

    private void openFullReport(Report report) {
        if (getActivity() instanceof MainActivity) {
            com.globaldental.pdd.ui.ai.ImplantSurvivalFragment fragment = new com.globaldental.pdd.ui.ai.ImplantSurvivalFragment();
            Bundle args = new Bundle();
            args.putString("patientJson", new com.google.gson.Gson().toJson(patient));
            args.putString("reportJson", new com.google.gson.Gson().toJson(report));
            args.putString("fromSource", "patient_dashboard");
            fragment.setArguments(args);
            ((MainActivity) getActivity()).replaceFragment(fragment);
        }
    }

    private void navigateBack() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).replaceFragment(new LandingFragment());
        }
    }
}
