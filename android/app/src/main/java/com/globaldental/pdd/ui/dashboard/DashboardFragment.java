package com.globaldental.pdd.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.globaldental.pdd.MainActivity;
import com.globaldental.pdd.R;
import com.globaldental.pdd.model.Patient;
import com.globaldental.pdd.model.StaffAccount;
import com.globaldental.pdd.network.SupabaseClient;
import com.globaldental.pdd.ui.patients.AddPatientFragment;
import com.globaldental.pdd.ui.patients.PatientAdapter;
import com.globaldental.pdd.ui.patients.PatientProfileFragment;
import com.globaldental.pdd.util.SessionManager;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardFragment extends Fragment implements PatientAdapter.OnPatientClickListener {

    private TextView tvWelcome;
    private TextView tvTotalPatients;
    private TextView tvActiveConsultations;
    private TextView tvHighRisk;
    private TextView tvPending;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private RecyclerView rvRecentPatients;
    private List<Patient> patientList;
    private PatientAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Enable toolbar drawer icon
        if (getActivity() instanceof MainActivity) {
            MainActivity mainAct = (MainActivity) getActivity();
            mainAct.showNavigationDrawer(true);
            mainAct.setToolbarTitle("Dashboard");
        }

        SessionManager sessionManager = new SessionManager(requireContext());
        StaffAccount doctor = sessionManager.getDoctor();

        tvWelcome = view.findViewById(R.id.tv_welcome);
        if (doctor != null) {
            tvWelcome.setText("Welcome back, Dr. " + doctor.getUsername());
        }

        tvTotalPatients = view.findViewById(R.id.tv_stat_total_patients);
        tvActiveConsultations = view.findViewById(R.id.tv_stat_active_consultations);
        tvHighRisk = view.findViewById(R.id.tv_stat_high_risk);
        tvPending = view.findViewById(R.id.tv_stat_pending);
        progressBar = view.findViewById(R.id.progress_bar);
        tvEmpty = view.findViewById(R.id.tv_empty);
        rvRecentPatients = view.findViewById(R.id.rv_recent_patients);

        rvRecentPatients.setLayoutManager(new LinearLayoutManager(requireContext()));
        patientList = new ArrayList<>();
        adapter = new PatientAdapter(patientList, this);
        rvRecentPatients.setAdapter(adapter);

        view.findViewById(R.id.btn_add_patient).setOnClickListener(v -> {
            navigateTo(new AddPatientFragment());
        });

        View fabChatbot = view.findViewById(R.id.fab_chatbot);
        if (fabChatbot != null) {
            fabChatbot.setOnClickListener(v -> {
                com.globaldental.pdd.ui.ai.ChatbotDialog dialog = com.globaldental.pdd.ui.ai.ChatbotDialog.newInstance(null);
                dialog.show(getParentFragmentManager(), "ChatbotDialog");
            });
        }

        fetchDashboardData();

        return view;
    }

    private void fetchDashboardData() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        SupabaseClient.getService().getPatients().enqueue(new Callback<List<Patient>>() {
            @Override
            public void onResponse(@NonNull Call<List<Patient>> call, @NonNull Response<List<Patient>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<Patient> allPatients = response.body();
                    updateStats(allPatients);
                    
                    patientList.clear();
                    // Load top 5 recent patients
                    int limit = Math.min(5, allPatients.size());
                    for (int i = 0; i < limit; i++) {
                        patientList.add(allPatients.get(i));
                    }
                    adapter.notifyDataSetChanged();

                    if (patientList.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                    }
                } else {
                    tvEmpty.setText("Failed to parse database records.");
                    tvEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Patient>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                tvEmpty.setText("Database connection error: " + t.getMessage());
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    private void updateStats(List<Patient> list) {
        int total = list.size();
        int consultations = 0;
        int highRisk = 0;
        int pendingRisk = 0;

        for (Patient p : list) {
            if ("Consultation".equalsIgnoreCase(p.getStatus())) {
                consultations++;
            }
            if ("High".equalsIgnoreCase(p.getRisk())) {
                highRisk++;
            }
            if ("Pending".equalsIgnoreCase(p.getRisk()) || p.getRisk() == null || p.getRisk().isEmpty()) {
                pendingRisk++;
            }
        }

        tvTotalPatients.setText(String.valueOf(total));
        tvActiveConsultations.setText(String.valueOf(consultations));
        tvHighRisk.setText(String.valueOf(highRisk));
        tvPending.setText(String.valueOf(pendingRisk));
    }

    @Override
    public void onPatientClick(Patient patient) {
        // Open patient profile fragment passing details
        PatientProfileFragment fragment = new PatientProfileFragment();
        Bundle bundle = new Bundle();
        bundle.putString("patient_id", patient.getPatientId());
        fragment.setArguments(bundle);
        navigateTo(fragment);
    }

    private void navigateTo(Fragment fragment) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).replaceFragment(fragment);
        }
    }
}
