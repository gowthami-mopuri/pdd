package com.globaldental.pdd.ui.patients;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.globaldental.pdd.R;
import com.globaldental.pdd.model.Report;
import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    public interface OnReportClickListener {
        void onReportClick(Report report);
    }

    public interface OnDeleteReportListener {
        void onDeleteReport(Report report);
    }

    private final List<Report> reports;
    private final OnReportClickListener clickListener;
    private final OnDeleteReportListener deleteListener;

    public ReportAdapter(List<Report> reports) {
        this(reports, null, null);
    }

    public ReportAdapter(List<Report> reports, OnReportClickListener clickListener) {
        this(reports, clickListener, null);
    }

    public ReportAdapter(List<Report> reports, OnReportClickListener clickListener, OnDeleteReportListener deleteListener) {
        this.reports = reports;
        this.clickListener = clickListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Report report = reports.get(position);
        holder.bind(report, deleteListener);
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onReportClick(report);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvPatientName;
        private final TextView tvPatientId;
        private final TextView tvDate;
        private final TextView tvRisk;
        private final TextView tvSurvival;
        private final TextView tvRecommendations;
        private final TextView tvDeleteReport;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPatientName = itemView.findViewById(R.id.tv_report_patient_name);
            tvPatientId = itemView.findViewById(R.id.tv_report_patient_id);
            tvDate = itemView.findViewById(R.id.tv_report_date);
            tvRisk = itemView.findViewById(R.id.tv_report_risk);
            tvSurvival = itemView.findViewById(R.id.tv_report_survival);
            tvRecommendations = itemView.findViewById(R.id.tv_report_recommendations);
            tvDeleteReport = itemView.findViewById(R.id.tv_delete_report);
        }

        public void bind(Report report, OnDeleteReportListener deleteListener) {
            if (tvPatientName != null) {
                String pName = report.getPatientName() != null ? report.getPatientName() : "Patient";
                tvPatientName.setText(pName);
            }
            if (tvPatientId != null) {
                String pId = report.getPatientId() != null ? report.getPatientId() : "PT-2024";
                tvPatientId.setText("ID: " + pId);
            }

            if (tvDate != null) {
                tvDate.setText(report.getDate() != null ? report.getDate() : "Recent");
            }
            
            String risk = report.getRiskLevel() != null ? report.getRiskLevel() : "LOW";
            if (tvRisk != null) {
                tvRisk.setText(risk + " RISK");
                GradientDrawable riskBg = new GradientDrawable();
                riskBg.setCornerRadius(12);
                if ("HIGH".equalsIgnoreCase(risk)) {
                    riskBg.setColor(Color.parseColor("#FEE2E2")); // Light red
                    tvRisk.setTextColor(Color.parseColor("#991B1B")); // Dark red
                } else if ("MEDIUM".equalsIgnoreCase(risk)) {
                    riskBg.setColor(Color.parseColor("#FEF3C7")); // Light yellow
                    tvRisk.setTextColor(Color.parseColor("#92400E")); // Dark yellow
                } else {
                    riskBg.setColor(Color.parseColor("#DCFCE7")); // Light green
                    tvRisk.setTextColor(Color.parseColor("#166534")); // Dark green
                }
                tvRisk.setBackground(riskBg);
            }

            Integer survival = report.getSurvivalProbability() != null ? report.getSurvivalProbability() : 84;
            if (tvSurvival != null) {
                tvSurvival.setText(survival + "%");
                if (survival >= 80) {
                    tvSurvival.setTextColor(Color.parseColor("#16A34A"));
                } else {
                    tvSurvival.setTextColor(Color.parseColor("#D97706"));
                }
            }

            if (tvRecommendations != null) {
                StringBuilder sb = new StringBuilder();
                if (report.getActionItems() != null && !report.getActionItems().isEmpty()) {
                    int max = Math.min(report.getActionItems().size(), 2);
                    for (int i = 0; i < max; i++) {
                        sb.append("→ ").append(report.getActionItems().get(i)).append("\n");
                    }
                    if (report.getActionItems().size() > 2) {
                        sb.append("+ ").append(report.getActionItems().size() - 2).append(" additional recommendations");
                    }
                } else {
                    sb.append("→ Smoking Cessation");
                }
                tvRecommendations.setText(sb.toString().trim());
            }

            if (tvDeleteReport != null) {
                tvDeleteReport.setOnClickListener(v -> {
                    if (deleteListener != null) {
                        deleteListener.onDeleteReport(report);
                    }
                });
            }
        }
    }
}
