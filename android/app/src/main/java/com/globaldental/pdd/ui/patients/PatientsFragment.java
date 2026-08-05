package com.globaldental.pdd.ui.patients;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import com.globaldental.pdd.network.SupabaseClient;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PatientsFragment extends Fragment implements PatientAdapter.OnPatientClickListener {

    private EditText etSearch;
    private RecyclerView rvPatients;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private List<Patient> fullPatientList;
    private List<Patient> filteredPatientList;
    private PatientAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patients, container, false);

        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            activity.showNavigationDrawer(true);
            activity.setToolbarTitle("Patients");
        }

        etSearch = view.findViewById(R.id.et_search);
        rvPatients = view.findViewById(R.id.rv_patients_list);
        progressBar = view.findViewById(R.id.patients_progress);
        tvEmpty = view.findViewById(R.id.tv_patients_empty);

        rvPatients.setLayoutManager(new LinearLayoutManager(requireContext()));
        fullPatientList = new ArrayList<>();
        filteredPatientList = new ArrayList<>();
        adapter = new PatientAdapter(filteredPatientList, this);
        rvPatients.setAdapter(adapter);

        view.findViewById(R.id.fab_add_patient).setOnClickListener(v -> {
            navigateTo(new AddPatientFragment());
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        fetchPatients();

        return view;
    }

    private void fetchPatients() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        SupabaseClient.getService().getPatients().enqueue(new Callback<List<Patient>>() {
            @Override
            public void onResponse(@NonNull Call<List<Patient>> call, @NonNull Response<List<Patient>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    fullPatientList.clear();
                    fullPatientList.addAll(response.body());
                    filterList(etSearch.getText().toString());
                } else {
                    tvEmpty.setText("Failed to retrieve patients from database.");
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

    private void filterList(String query) {
        filteredPatientList.clear();
        String q = query.trim().toLowerCase();

        if (q.isEmpty()) {
            filteredPatientList.addAll(fullPatientList);
        } else {
            for (Patient p : fullPatientList) {
                boolean matchesName = p.getName() != null && p.getName().toLowerCase().contains(q);
                boolean matchesId = p.getPatientId() != null && p.getPatientId().toLowerCase().contains(q);
                if (matchesName || matchesId) {
                    filteredPatientList.add(p);
                }
            }
        }

        adapter.notifyDataSetChanged();

        if (filteredPatientList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPatientClick(Patient patient) {
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
