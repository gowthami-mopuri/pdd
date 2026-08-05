package com.globaldental.pdd.ui.patients;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.globaldental.pdd.MainActivity;
import com.globaldental.pdd.R;
import com.globaldental.pdd.model.Patient;
import com.globaldental.pdd.network.SupabaseClient;
import com.google.android.material.textfield.TextInputEditText;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddPatientFragment extends Fragment {

    private TextInputEditText etPatientId, etPatientName, etAge, etGender, etHeight, etWeight;
    private TextInputEditText etSmokingStatus, etPackYears, etDiabetesStatus, etHba1c, etFastingSugar;
    private TextInputEditText etImplantSite, etToothNumber, etImplantType, etImplantDiameter, etImplantLength, etBoneDensityClass, etBoneDensityScore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_patient, container, false);

        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            activity.showNavigationDrawer(true);
            activity.setToolbarTitle("Add Patient");
        }

        // Demographics
        etPatientId = view.findViewById(R.id.et_patient_id);
        etPatientName = view.findViewById(R.id.et_patient_name);
        etAge = view.findViewById(R.id.et_age);
        etGender = view.findViewById(R.id.et_gender);
        etHeight = view.findViewById(R.id.et_height);
        etWeight = view.findViewById(R.id.et_weight);

        // Medical Risks
        etSmokingStatus = view.findViewById(R.id.et_smoking_status);
        etPackYears = view.findViewById(R.id.et_pack_years);
        etDiabetesStatus = view.findViewById(R.id.et_diabetes_status);
        etHba1c = view.findViewById(R.id.et_hba1c);
        etFastingSugar = view.findViewById(R.id.et_fasting_sugar);

        // Site details
        etImplantSite = view.findViewById(R.id.et_implant_site);
        etToothNumber = view.findViewById(R.id.et_tooth_number);
        etImplantType = view.findViewById(R.id.et_implant_type);
        etImplantDiameter = view.findViewById(R.id.et_implant_diameter);
        etImplantLength = view.findViewById(R.id.et_implant_length);
        etBoneDensityClass = view.findViewById(R.id.et_bone_density_class);
        etBoneDensityScore = view.findViewById(R.id.et_bone_density_score);

        view.findViewById(R.id.btn_cancel).setOnClickListener(v -> navigateBack());
        view.findViewById(R.id.btn_save).setOnClickListener(v -> savePatientProfile());

        return view;
    }

    private void savePatientProfile() {
        String patientId = etPatientId.getText().toString().trim();
        String name = etPatientName.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        String gender = etGender.getText().toString().trim();
        String heightStr = etHeight.getText().toString().trim();
        String weightStr = etWeight.getText().toString().trim();

        if (patientId.isEmpty()) {
            Toast.makeText(requireContext(), "Patient ID is required.", Toast.LENGTH_SHORT).show();
            return;
        }

        Patient patient = new Patient();
        patient.setPatientId(patientId);
        patient.setName(name.isEmpty() ? "Unknown Patient" : name);
        patient.setAge(ageStr.isEmpty() ? null : Integer.parseInt(ageStr));
        patient.setGender(gender.isEmpty() ? null : gender);
        patient.setHeight(heightStr.isEmpty() ? null : Integer.parseInt(heightStr));
        patient.setWeight(weightStr.isEmpty() ? null : Integer.parseInt(weightStr));
        patient.setStatus("Consultation");
        patient.setRisk("Pending");

        // Compile all form data into the clinical_data map
        Map<String, Object> clinicalData = new HashMap<>();
        clinicalData.put("patientId", patientId);
        clinicalData.put("patientName", name);
        clinicalData.put("age", ageStr);
        clinicalData.put("gender", gender);
        clinicalData.put("height", heightStr);
        clinicalData.put("weight", weightStr);

        clinicalData.put("smokingStatus", etSmokingStatus.getText().toString().trim());
        clinicalData.put("packYears", etPackYears.getText().toString().trim());
        clinicalData.put("diabetesStatus", etDiabetesStatus.getText().toString().trim());
        clinicalData.put("hba1c", etHba1c.getText().toString().trim());
        clinicalData.put("fastingSugar", etFastingSugar.getText().toString().trim());

        clinicalData.put("implantSite", etImplantSite.getText().toString().trim());
        clinicalData.put("toothNumber", etToothNumber.getText().toString().trim());
        clinicalData.put("implantType", etImplantType.getText().toString().trim());
        clinicalData.put("implantDiameter", etImplantDiameter.getText().toString().trim());
        clinicalData.put("implantLength", etImplantLength.getText().toString().trim());
        clinicalData.put("boneDensityClass", etBoneDensityClass.getText().toString().trim());
        clinicalData.put("boneDensityScore", etBoneDensityScore.getText().toString().trim());

        patient.setClinicalData(clinicalData);

        SupabaseClient.getService().addPatient(patient).enqueue(new Callback<List<Patient>>() {
            @Override
            public void onResponse(@NonNull Call<List<Patient>> call, @NonNull Response<List<Patient>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Patient Profile Created successfully!", Toast.LENGTH_SHORT).show();
                    navigateBack();
                } else {
                    Toast.makeText(requireContext(), "Failed to save patient profile.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Patient>> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Database error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateBack() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).replaceFragment(new PatientsFragment());
        }
    }
}
