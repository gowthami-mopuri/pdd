package com.globaldental.pdd.ui.dashboard;

import android.graphics.Color;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.globaldental.pdd.R;
import com.globaldental.pdd.model.StaffAccount;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class StaffAdapter extends RecyclerView.Adapter<StaffAdapter.StaffViewHolder> {

    private final List<StaffAccount> staffList;
    private final OnStaffActionListener listener;

    public interface OnStaffActionListener {
        void onEdit(StaffAccount account);
        void onToggleSuspend(StaffAccount account);
        void onDelete(StaffAccount account);
    }

    public StaffAdapter(List<StaffAccount> staffList, OnStaffActionListener listener) {
        this.staffList = staffList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StaffViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_staff, parent, false);
        return new StaffViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StaffViewHolder holder, int position) {
        StaffAccount account = staffList.get(position);
        holder.bind(account, listener);
    }

    @Override
    public int getItemCount() {
        return staffList.size();
    }

    static class StaffViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvUsername;
        private final TextView tvGender;
        private final TextView tvEmail;
        private final TextView tvPhone;
        private final TextView tvStatus;
        private final MaterialButton btnEdit;
        private final MaterialButton btnSuspend;
        private final MaterialButton btnDelete;

        public StaffViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tv_staff_username);
            tvGender = itemView.findViewById(R.id.tv_staff_gender);
            tvEmail = itemView.findViewById(R.id.tv_staff_email);
            tvPhone = itemView.findViewById(R.id.tv_staff_phone);
            tvStatus = itemView.findViewById(R.id.tv_staff_status);
            btnEdit = itemView.findViewById(R.id.btn_edit_staff);
            btnSuspend = itemView.findViewById(R.id.btn_suspend_staff);
            btnDelete = itemView.findViewById(R.id.btn_delete_staff);
        }

        public void bind(StaffAccount account, OnStaffActionListener listener) {
            tvUsername.setText(account.getUsername());
            tvGender.setText(account.getGender() != null ? account.getGender() : "Male");
            tvEmail.setText(account.getEmail() != null ? account.getEmail() : "No Email");
            tvPhone.setText(account.getPhone() != null ? account.getPhone() : "No Phone");

            String status = account.getStatus() != null ? account.getStatus() : "active";
            tvStatus.setText(status.toUpperCase());

            if ("suspended".equalsIgnoreCase(status)) {
                tvStatus.setTextColor(Color.parseColor("#EA580C")); // Orange text
                tvStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFF7ED"))); // Orange tint bg
                btnSuspend.setText("Activate");
                btnSuspend.setIconResource(android.R.drawable.ic_media_play);
                btnSuspend.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#22C55E"))); // Green activate
            } else {
                tvStatus.setTextColor(Color.parseColor("#2563EB")); // Blue text
                tvStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#EFF6FF"))); // Blue tint bg
                btnSuspend.setText("Suspend");
                btnSuspend.setIconResource(android.R.drawable.ic_media_pause);
                btnSuspend.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F59E0B"))); // Orange suspend
            }

            btnEdit.setOnClickListener(v -> listener.onEdit(account));
            btnSuspend.setOnClickListener(v -> listener.onToggleSuspend(account));
            btnDelete.setOnClickListener(v -> listener.onDelete(account));
        }
    }
}
