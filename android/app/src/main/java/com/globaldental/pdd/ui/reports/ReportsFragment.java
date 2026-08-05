package com.globaldental.pdd.ui.reports;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.globaldental.pdd.MainActivity;
import com.globaldental.pdd.R;
import com.globaldental.pdd.model.Report;
import com.globaldental.pdd.ui.ai.ImplantSurvivalFragment;
import com.globaldental.pdd.ui.patients.ReportAdapter;
import com.globaldental.pdd.util.SessionManager;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;

public class ReportsFragment extends Fragment {

    private View cardEmptyState;
    private RecyclerView rvReportsList;
    private SessionManager sessionManager;
    private ReportAdapter adapter;
    private List<Report> reportsList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reports, container, false);

        sessionManager = new SessionManager(requireContext());
        cardEmptyState = view.findViewById(R.id.card_empty_state);
        rvReportsList  = view.findViewById(R.id.rv_reports_list);

        rvReportsList.setLayoutManager(new LinearLayoutManager(requireContext()));

        loadReports();
        return view;
    }

    private void loadReports() {
        if (!isAdded()) return;
        reportsList = sessionManager.getAllReports();

        if (reportsList == null || reportsList.isEmpty()) {
            cardEmptyState.setVisibility(View.VISIBLE);
            rvReportsList.setVisibility(View.GONE);
        } else {
            cardEmptyState.setVisibility(View.GONE);
            rvReportsList.setVisibility(View.VISIBLE);

            adapter = new ReportAdapter(reportsList,
                report -> openFullReport(report),
                report -> deleteReport(report)
            );
            rvReportsList.setAdapter(adapter);
        }
    }

    private void openFullReport(Report report) {
        if (getActivity() instanceof MainActivity) {
            ImplantSurvivalFragment fragment = new ImplantSurvivalFragment();
            Bundle args = new Bundle();
            args.putString("reportJson", new Gson().toJson(report));
            args.putString("fromSource", "reports");
            fragment.setArguments(args);
            ((MainActivity) getActivity()).replaceFragment(fragment);
        }
    }

    private void deleteReport(Report report) {
        if (!isAdded() || report == null) return;
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete Report")
                .setMessage("Are you sure you want to delete this saved report?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    sessionManager.deleteReport(report.getId());
                    Toast.makeText(requireContext(), "Report deleted.", Toast.LENGTH_SHORT).show();
                    loadReports();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
