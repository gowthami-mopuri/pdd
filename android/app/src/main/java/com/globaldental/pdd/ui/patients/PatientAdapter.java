package com.globaldental.pdd.ui.patients;

import android.graphics.Color;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.globaldental.pdd.R;
import com.globaldental.pdd.model.Patient;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.PatientViewHolder> {

    private final List<Patient> patients;
    private final OnPatientClickListener listener;

    public interface OnPatientClickListener {
        void onPatientClick(Patient patient);
    }

    public PatientAdapter(List<Patient> patients, OnPatientClickListener listener) {
        this.patients = patients;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_patient, parent, false);
        return new PatientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        Patient patient = patients.get(position);
        holder.bind(patient, listener);
    }

    @Override
    public int getItemCount() {
        return patients.size();
    }

    static class PatientViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvId;
        private final TextView tvDate;
        private final TextView tvRisk;
        private final TextView tvStatus;
        private final MaterialButton btnView;

        public PatientViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_patient_name);
            tvId = itemView.findViewById(R.id.tv_patient_id);
            tvDate = itemView.findViewById(R.id.tv_patient_date);
            tvRisk = itemView.findViewById(R.id.tv_patient_risk);
            tvStatus = itemView.findViewById(R.id.tv_patient_status);
            btnView = itemView.findViewById(R.id.btn_view_profile);
        }

        public void bind(Patient patient, OnPatientClickListener listener) {
            tvName.setText(patient.getName());
            tvId.setText(patient.getPatientId() != null ? patient.getPatientId() : "No ID");
            tvDate.setText(patient.getLastVisit() != null ? patient.getLastVisit() : "N/A");
            tvStatus.setText(patient.getStatus() != null ? patient.getStatus() : "Consultation");

            String risk = patient.getRisk() != null ? patient.getRisk() : "Pending";
            tvRisk.setText(risk);

            // Style Risk Badge dynamically using custom status colors matching mockup
            if ("High".equalsIgnoreCase(risk)) {
                tvRisk.setTextColor(Color.parseColor("#B91C1C")); // Dark red
                tvRisk.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FEF2F2"))); // Light red
            } else if ("Medium".equalsIgnoreCase(risk)) {
                tvRisk.setTextColor(Color.parseColor("#C2410C")); // Dark orange
                tvRisk.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFF7ED"))); // Light orange
            } else if ("Low".equalsIgnoreCase(risk)) {
                tvRisk.setTextColor(Color.parseColor("#047857")); // Dark green
                tvRisk.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ECFDF5"))); // Light green
            } else {
                tvRisk.setTextColor(Color.parseColor("#475569")); // Dark gray
                tvRisk.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F1F5F9"))); // Light gray
            }

            btnView.setOnClickListener(v -> listener.onPatientClick(patient));
        }
    }
}
