package com.globaldental.pdd.ui.patients;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.globaldental.pdd.MainActivity;
import com.globaldental.pdd.R;
import com.globaldental.pdd.model.Patient;
import com.globaldental.pdd.model.Report;
import com.globaldental.pdd.network.SupabaseClient;
import com.globaldental.pdd.util.SessionManager;
import com.google.android.material.button.MaterialButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PatientProfileFragment extends Fragment {

    private TextView tvAvatar, tvProfileName, tvProfileBio, tvProfileVisit;
    private TextView tvSummaryImplant, tvHistoryDetails;
    private TextView tvPredictionStatus;
    private ProgressBar predictionProgress;
    private LinearLayout llPredictionResults;
    private TextView tvSurvivalScore, tvFailureScore, tvRiskCategory, tvRecsBullet;
    private MaterialButton btnRunPrediction, btnSyncReport;

    private String patientIdParam;
    private Patient patient;
    private SessionManager sessionManager;
    private double finalSurvivalScore = 95.0;
    private String calculatedRisk = "Low";
    private final List<String> actionItemsList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_profile, container, false);

        sessionManager = new SessionManager(requireContext());

        if (getArguments() != null) {
            patientIdParam = getArguments().getString("patient_id");
        }

        // Configure toolbar
        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            activity.showNavigationDrawer(true);
            activity.setToolbarTitle("Patient Profile");
        }

        tvAvatar = view.findViewById(R.id.tv_avatar);
        tvProfileName = view.findViewById(R.id.tv_profile_name);
        tvProfileBio = view.findViewById(R.id.tv_profile_bio);
        tvProfileVisit = view.findViewById(R.id.tv_profile_visit);

        tvSummaryImplant = view.findViewById(R.id.tv_summary_implant);
        tvHistoryDetails = view.findViewById(R.id.tv_history_details);

        tvPredictionStatus = view.findViewById(R.id.tv_prediction_status);
        predictionProgress = view.findViewById(R.id.prediction_progress);
        llPredictionResults = view.findViewById(R.id.ll_prediction_results);
        tvSurvivalScore = view.findViewById(R.id.tv_survival_score);
        tvFailureScore = view.findViewById(R.id.tv_failure_score);
        tvRiskCategory = view.findViewById(R.id.tv_risk_category);
        tvRecsBullet = view.findViewById(R.id.tv_recs_bullet);

        btnRunPrediction = view.findViewById(R.id.btn_run_prediction);
        btnSyncReport = view.findViewById(R.id.btn_sync_report);

        btnRunPrediction.setOnClickListener(v -> runPredictionSimulation());
        btnSyncReport.setOnClickListener(v -> syncReportToPatientPortal());

        fetchPatientData();

        return view;
    }

    private void fetchPatientData() {
        if (patientIdParam == null) return;

        SupabaseClient.getService().loginPatient("eq." + patientIdParam).enqueue(new Callback<List<Patient>>() {
            @Override
            public void onResponse(@NonNull Call<List<Patient>> call, @NonNull Response<List<Patient>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    patient = response.body().get(0);
                    bindProfileData();
                } else {
                    Toast.makeText(requireContext(), "Patient not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Patient>> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Failed to load patient: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindProfileData() {
        if (patient == null) return;

        tvAvatar.setText(patient.getName().substring(0, 1).toUpperCase());
        tvProfileName.setText(patient.getName());
        tvProfileBio.setText(patient.getPatientId() + " • " + patient.getAge() + " yrs, " + patient.getGender());
        tvProfileVisit.setText("Last Visit: " + patient.getLastVisit() + " • Status: " + patient.getStatus());

        Map<String, Object> clinical = patient.getClinicalData();
        if (clinical != null) {
            // Bind Treatment Overview
            String site = getMapString(clinical, "implantSite", "N/A");
            String pos = getMapString(clinical, "implantPosition", "N/A");
            String tooth = getMapString(clinical, "toothNumber", "N/A");
            String type = getMapString(clinical, "implantType", "N/A");
            String dia = getMapString(clinical, "implantDiameter", "N/A");
            String len = getMapString(clinical, "implantLength", "N/A");
            String boneClass = getMapString(clinical, "boneDensityClass", "Not Assessed");

            String summaryText = "• Target Site: " + site + " (Tooth " + tooth + " / Pos: " + pos + ")\n" +
                    "• Implant: " + type + " (" + dia + "x" + len + "mm)\n" +
                    "• Bone Quality: " + boneClass;
            tvSummaryImplant.setText(summaryText);

            // Bind Medical Details
            String smoking = getMapString(clinical, "smokingStatus", "Unknown");
            String packYears = getMapString(clinical, "packYears", "");
            String diabetes = getMapString(clinical, "diabetesStatus", "Unknown");
            String hba1c = getMapString(clinical, "hba1c", "");
            String fasting = getMapString(clinical, "fastingSugar", "");

            String historyText = "• Smoking Status: " + smoking + (packYears.isEmpty() ? "" : " (" + packYears + " Pack Years)") + "\n" +
                    "• Diabetes Status: " + diabetes + (hba1c.isEmpty() ? "" : " (HbA1c: " + hba1c + "%)") + (fasting.isEmpty() ? "" : " (Fasting Sugar: " + fasting + "mg/dL)");
            tvHistoryDetails.setText(historyText);
        }
    }

    private String getMapString(Map<String, Object> map, String key, String defaultValue) {
        Object val = map.get(key);
        if (val == null) return defaultValue;
        return val.toString();
    }

    private void runPredictionSimulation() {
        btnRunPrediction.setEnabled(false);
        tvPredictionStatus.setVisibility(View.VISIBLE);
        predictionProgress.setVisibility(View.VISIBLE);
        llPredictionResults.setVisibility(View.GONE);

        final String[] steps = {
                "Extracting clinical parameters...",
                "Analyzing bone density and site factors...",
                "Evaluating systemic risk indicators...",
                "Calculating survival probability matrix..."
        };

        final Handler handler = new Handler(Looper.getMainLooper());
        for (int i = 0; i < steps.length; i++) {
            final int index = i;
            handler.postDelayed(() -> {
                if (isAdded()) {
                    tvPredictionStatus.setText(steps[index]);
                    if (index == steps.length - 1) {
                        handler.postDelayed(this::displayCalculatedPrediction, 1000);
                    }
                }
            }, i * 1000);
        }
    }

    private void displayCalculatedPrediction() {
        if (patient == null) return;

        predictionProgress.setVisibility(View.GONE);
        tvPredictionStatus.setVisibility(View.GONE);

        double baseScore = 95.0;
        actionItemsList.clear();

        Map<String, Object> clinical = patient.getClinicalData();
        if (clinical != null) {
            String smoking = getMapString(clinical, "smokingStatus", "").toLowerCase();
            String diabetes = getMapString(clinical, "diabetesStatus", "").toLowerCase();
            String boneClass = getMapString(clinical, "boneDensityClass", "").toLowerCase();
            String hba1cStr = getMapString(clinical, "hba1c", "");

            // 1) Smoking deduction
            if (smoking.contains("heavy") || smoking.contains("active") || smoking.contains("smoker")) {
                baseScore -= 5.5;
                actionItemsList.add("Smoking cessation counseling support.");
            } else if (smoking.contains("former")) {
                baseScore -= 2.0;
                actionItemsList.add("Maintain smoke-free habits.");
            }

            // 2) Diabetes deduction
            if (diabetes.contains("controlled") && !diabetes.contains("uncontrolled")) {
                baseScore -= 3.0;
                actionItemsList.add("Glycemic monitoring before surgical intervention.");
            } else if (diabetes.contains("uncontrolled")) {
                baseScore -= 6.5;
                actionItemsList.add("Refer to PCP for diabetic control improvement (HbA1c < 7.0%).");
            }

            if (!hba1cStr.isEmpty()) {
                try {
                    double hba1c = Double.parseDouble(hba1cStr);
                    if (hba1c > 8.0) {
                        baseScore -= 3.0;
                    }
                } catch (Exception ignored) {}
            }

            // 3) Bone class adjustments
            if (boneClass.contains("type 1")) {
                baseScore += 2.0;
            } else if (boneClass.contains("type 4")) {
                baseScore -= 4.5;
                actionItemsList.add("Soft bone protocol: consider undersized osteotomy and delayed loading.");
            } else if (!boneClass.isEmpty()) {
                baseScore += 1.0;
            }
        }

        // Clamp
        finalSurvivalScore = Math.min(Math.max(baseScore, 40.0), 99.5);
        double failureRisk = 100.0 - finalSurvivalScore;

        if (finalSurvivalScore < 80.0) {
            calculatedRisk = "High";
            tvRiskCategory.setTextColor(getResources().getColor(R.color.colorDanger));
        } else if (finalSurvivalScore < 90.0) {
            calculatedRisk = "Medium";
            tvRiskCategory.setTextColor(getResources().getColor(R.color.colorWarning));
        } else {
            calculatedRisk = "Low";
            tvRiskCategory.setTextColor(getResources().getColor(R.color.colorSuccess));
        }

        if (actionItemsList.isEmpty()) {
            actionItemsList.add("Standard post-operative clinical hygiene protocol.");
            actionItemsList.add("Routine clinical follow-up at 3 and 6 months.");
        }

        tvSurvivalScore.setText(String.format(Locale.getDefault(), "%.1f%%", finalSurvivalScore));
        tvFailureScore.setText(String.format(Locale.getDefault(), "%.1f%%", failureRisk));
        tvRiskCategory.setText(calculatedRisk.toUpperCase() + " RISK");

        StringBuilder sb = new StringBuilder();
        for (String item : actionItemsList) {
            sb.append("• ").append(item).append("\n");
        }
        sb.setLength(sb.length() - 1);
        tvRecsBullet.setText(sb.toString());

        llPredictionResults.setVisibility(View.VISIBLE);
        btnRunPrediction.setEnabled(true);
    }

    private void syncReportToPatientPortal() {
        if (patient == null) return;

        // Save report locally so patient can fetch it in SharedPreferences
        Report report = new Report();
        report.setId(java.util.UUID.randomUUID().toString());
        report.setPatientId(patient.getPatientId());
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        report.setDate(sdf.format(new Date()));
        report.setRiskLevel(calculatedRisk.toUpperCase());
        report.setSurvivalProbability((int) finalSurvivalScore);
        report.setActionItems(actionItemsList);

        sessionManager.saveReport(report);

        // Update risk profile in Supabase
        Patient payload = new Patient();
        payload.setRisk(calculatedRisk);

        SupabaseClient.getService().updatePatient("eq." + patient.getId(), payload)
                .enqueue(new Callback<List<Patient>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Patient>> call, @NonNull Response<List<Patient>> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(), "Report published & synced to Patient Portal successfully!", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(requireContext(), "Report published locally, but database sync failed.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Patient>> call, @NonNull Throwable t) {
                        Toast.makeText(requireContext(), "Report published locally. Sync error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
