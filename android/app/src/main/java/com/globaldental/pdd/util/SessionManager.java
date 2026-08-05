package com.globaldental.pdd.util;

import android.content.Context;
import android.content.SharedPreferences;
import com.globaldental.pdd.model.Patient;
import com.globaldental.pdd.model.StaffAccount;
import com.globaldental.pdd.model.Report;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;

public class SessionManager {
    private static final String PREF_NAME = "ImplantAIPrefs";
    private static final String KEY_DOCTOR = "doctorUser";
    private static final String KEY_PATIENT = "patientUser";
    private static final String KEY_ADMIN = "adminAuthenticated";

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;
    private final Gson gson;

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
        gson = new Gson();
    }

    public void saveDoctor(StaffAccount doctor) {
        editor.putString(KEY_DOCTOR, gson.toJson(doctor));
        editor.apply();
    }

    public StaffAccount getDoctor() {
        String json = pref.getString(KEY_DOCTOR, null);
        if (json == null) return null;
        return gson.fromJson(json, StaffAccount.class);
    }

    public void savePatient(Patient patient) {
        editor.putString(KEY_PATIENT, gson.toJson(patient));
        editor.apply();
    }

    public Patient getPatient() {
        String json = pref.getString(KEY_PATIENT, null);
        if (json == null) return null;
        return gson.fromJson(json, Patient.class);
    }

    public void saveAdmin(boolean authenticated) {
        editor.putBoolean(KEY_ADMIN, authenticated);
        editor.apply();
    }

    public boolean isAdminAuthenticated() {
        return pref.getBoolean(KEY_ADMIN, false);
    }

    public void logout() {
        String savedReports = pref.getString("savedReports", "[]");
        boolean darkMode = pref.getBoolean("darkModeEnabled", false);
        editor.clear();
        editor.putString("savedReports", savedReports);
        editor.putBoolean("darkModeEnabled", darkMode);
        editor.apply();
    }

    public List<Report> getReportsForPatient(String patientId) {
        List<Report> allReports = getAllReports();
        List<Report> filtered = new ArrayList<>();
        String targetClean = cleanId(patientId);
        for (Report r : allReports) {
            String rClean = cleanId(r.getPatientId());
            if (!targetClean.isEmpty() && (targetClean.equals(rClean) || rClean.contains(targetClean) || targetClean.contains(rClean))) {
                filtered.add(r);
            }
        }
        return filtered;
    }

    private String cleanId(String raw) {
        if (raw == null) return "";
        return raw.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
    }

    public void saveReport(Report report) {
        List<Report> allReports = getAllReports();
        allReports.add(0, report);
        editor.putString("savedReports", gson.toJson(allReports));
        editor.apply();
    }

    public void setDarkMode(boolean enabled) {
        editor.putBoolean("darkModeEnabled", enabled);
        editor.apply();
    }

    public boolean isDarkMode() {
        return pref.getBoolean("darkModeEnabled", false);
    }

    public List<Report> getAllReports() {
        String json = pref.getString("savedReports", null);
        if (json == null || json.equals("[]")) {
            List<Report> defaults = new ArrayList<>();
            Report sample = new Report();
            sample.setId("REP-2026-001");
            sample.setPatientName("Sravanthi");
            sample.setPatientId("PT-2024");
            sample.setRiskLevel("LOW RISK");
            sample.setSurvivalProbability(84);
            sample.setDate("2026-07-26");

            List<String> actions = new ArrayList<>();
            actions.add("Routine maintenance every 6 months");
            actions.add("Maintain good oral hygiene");
            sample.setActionItems(actions);

            List<String> factors = new ArrayList<>();
            factors.add("Non-smoker");
            factors.add("Adequate bone density");
            sample.setRiskFactors(factors);

            List<String> narr = new ArrayList<>();
            narr.add("The patient shows strong peri-implant bone preservation with low risk of failure.");
            sample.setNarrative(narr);

            defaults.add(sample);
            editor.putString("savedReports", gson.toJson(defaults));
            editor.apply();
            return defaults;
        }
        java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<List<Report>>(){}.getType();
        List<Report> allReports = gson.fromJson(json, listType);
        return allReports != null ? allReports : new ArrayList<>();
    }

    public void deleteReport(String reportId) {
        if (reportId == null) return;
        List<Report> all = getAllReports();
        List<Report> updated = new ArrayList<>();
        for (Report r : all) {
            if (!reportId.equals(r.getId())) {
                updated.add(r);
            }
        }
        editor.putString("savedReports", gson.toJson(updated));
        editor.apply();
    }
}
