package com.globaldental.pdd.ui.dashboard;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
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
import com.globaldental.pdd.model.StaffAccount;
import com.globaldental.pdd.network.SupabaseClient;
import com.globaldental.pdd.ui.auth.LandingFragment;
import com.globaldental.pdd.util.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDashboardFragment extends Fragment implements StaffAdapter.OnStaffActionListener {

    private TextInputEditText etUsername, etEmail, etPhone, etGender, etDob, etAddress, etPassword, etConfirmPassword;
    private TextView tvFormTitle, tvMessage, tvEmpty;
    private ProgressBar progressBar;
    private RecyclerView rvStaff;
    private List<StaffAccount> staffList;
    private StaffAdapter adapter;
    private SessionManager sessionManager;
    private String editingUserId = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_dashboard, container, false);

        sessionManager = new SessionManager(requireContext());

        // Configure toolbar
        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            activity.showNavigationDrawer(false);
            activity.setToolbarTitle("Admin Workspace");
        }

        etUsername = view.findViewById(R.id.et_username);
        etEmail = view.findViewById(R.id.et_email);
        etPhone = view.findViewById(R.id.et_phone);
        etGender = view.findViewById(R.id.et_gender);
        etDob = view.findViewById(R.id.et_dob);
        etAddress = view.findViewById(R.id.et_address);
        etPassword = view.findViewById(R.id.et_password);
        etConfirmPassword = view.findViewById(R.id.et_confirm_password);

        tvFormTitle = view.findViewById(R.id.tv_form_title);
        tvMessage = view.findViewById(R.id.tv_admin_message);
        tvEmpty = view.findViewById(R.id.tv_admin_empty);
        progressBar = view.findViewById(R.id.admin_progress);
        rvStaff = view.findViewById(R.id.rv_staff_accounts);

        etDob.setOnClickListener(v -> showDatePicker());
        etGender.setOnClickListener(v -> showGenderPicker());
        
        View btnBackToRoles = view.findViewById(R.id.btn_back_to_roles);
        if (btnBackToRoles != null) {
            btnBackToRoles.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).replaceFragment(new LandingFragment());
                }
            });
        }

        view.findViewById(R.id.btn_reset).setOnClickListener(v -> resetForm());
        view.findViewById(R.id.btn_save_user).setOnClickListener(v -> saveUser());
        view.findViewById(R.id.btn_sign_out).setOnClickListener(v -> signOut());

        rvStaff.setLayoutManager(new LinearLayoutManager(requireContext()));
        staffList = new ArrayList<>();
        adapter = new StaffAdapter(staffList, this);
        rvStaff.setAdapter(adapter);

        fetchUsers();

        return view;
    }

    private void showGenderPicker() {
        String[] genders = {"Male", "Female", "Other"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Select Gender")
                .setItems(genders, (dialog, which) -> {
                    etGender.setText(genders[which]);
                })
                .show();
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog picker = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            String date = year + "-" + String.format("%02d", month + 1) + "-" + String.format("%02d", dayOfMonth);
            etDob.setText(date);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        picker.show();
    }

    private void fetchUsers() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        SupabaseClient.getService().getStaffAccounts().enqueue(new Callback<List<StaffAccount>>() {
            @Override
            public void onResponse(@NonNull Call<List<StaffAccount>> call, @NonNull Response<List<StaffAccount>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    staffList.clear();
                    staffList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    if (staffList.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                    }
                } else {
                    showMessage("error", "Failed to fetch staff accounts.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<StaffAccount>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                showMessage("error", "Database connection error: " + t.getMessage());
            }
        });
    }

    private void saveUser() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String gender = etGender.getText().toString().trim();
        String dob = etDob.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (username.isEmpty() || email.isEmpty()) {
            showMessage("error", "Username and Email are required fields.");
            return;
        }

        if (editingUserId == null && (password.isEmpty() || confirmPassword.isEmpty())) {
            showMessage("error", "Password is required for new accounts.");
            return;
        }

        if (!password.isEmpty() && !password.equals(confirmPassword)) {
            showMessage("error", "Passwords do not match.");
            return;
        }

        StaffAccount payload = new StaffAccount();
        payload.setUsername(username);
        payload.setEmail(email);
        payload.setPhone(phone);
        payload.setGender(gender);
        payload.setDob(dob.isEmpty() ? null : dob);
        payload.setAddress(address);
        if (!password.isEmpty()) {
            payload.setPassword(password);
        }

        if (editingUserId != null) {
            // Update
            SupabaseClient.getService().updateStaffAccount("eq." + editingUserId, payload)
                    .enqueue(new Callback<List<StaffAccount>>() {
                        @Override
                        public void onResponse(@NonNull Call<List<StaffAccount>> call, @NonNull Response<List<StaffAccount>> response) {
                            if (response.isSuccessful()) {
                                showMessage("success", "User profile updated successfully!");
                                resetForm();
                                fetchUsers();
                            } else {
                                showMessage("error", "Failed to update profile.");
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<List<StaffAccount>> call, @NonNull Throwable t) {
                            showMessage("error", "Connection error: " + t.getMessage());
                        }
                    });
        } else {
            // Create
            payload.setStatus("active");
            SupabaseClient.getService().createStaffAccount(payload)
                    .enqueue(new Callback<List<StaffAccount>>() {
                        @Override
                        public void onResponse(@NonNull Call<List<StaffAccount>> call, @NonNull Response<List<StaffAccount>> response) {
                            if (response.isSuccessful()) {
                                showMessage("success", "User profile created successfully!");
                                resetForm();
                                fetchUsers();
                            } else {
                                showMessage("error", "Failed to create profile.");
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<List<StaffAccount>> call, @NonNull Throwable t) {
                            showMessage("error", "Connection error: " + t.getMessage());
                        }
                    });
        }
    }

    private void resetForm() {
        editingUserId = null;
        tvFormTitle.setText("Create User Profile");
        etUsername.setText("");
        etEmail.setText("");
        etPhone.setText("");
        etGender.setText("");
        etDob.setText("");
        etAddress.setText("");
        etPassword.setText("");
        etConfirmPassword.setText("");
    }

    private void showMessage(String type, String text) {
        tvMessage.setText(text);
        if ("error".equals(type)) {
            tvMessage.setBackgroundResource(R.color.colorDanger);
        } else {
            tvMessage.setBackgroundResource(R.color.colorSuccess);
        }
        tvMessage.setVisibility(View.VISIBLE);
    }

    private void signOut() {
        sessionManager.saveAdmin(false);
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).replaceFragment(new LandingFragment());
        }
    }

    @Override
    public void onEdit(StaffAccount account) {
        editingUserId = account.getId();
        tvFormTitle.setText("Edit User Profile: " + account.getUsername());
        etUsername.setText(account.getUsername() != null ? account.getUsername() : "");
        etEmail.setText(account.getEmail() != null ? account.getEmail() : "");
        etPhone.setText(account.getPhone() != null ? account.getPhone() : "");
        etGender.setText(account.getGender() != null ? account.getGender() : "");
        etDob.setText(account.getDob() != null ? account.getDob() : "");
        etAddress.setText(account.getAddress() != null ? account.getAddress() : "");
        etPassword.setText("");
        etConfirmPassword.setText("");
    }

    @Override
    public void onToggleSuspend(StaffAccount account) {
        String newStatus = "suspended".equalsIgnoreCase(account.getStatus()) ? "active" : "suspended";
        StaffAccount payload = new StaffAccount();
        payload.setStatus(newStatus);

        SupabaseClient.getService().updateStaffAccount("eq." + account.getId(), payload)
                .enqueue(new Callback<List<StaffAccount>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<StaffAccount>> call, @NonNull Response<List<StaffAccount>> response) {
                        if (response.isSuccessful()) {
                            showMessage("success", "Account status updated!");
                            fetchUsers();
                        } else {
                            showMessage("error", "Failed to update account status.");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<StaffAccount>> call, @NonNull Throwable t) {
                        showMessage("error", "Connection error: " + t.getMessage());
                    }
                });
    }

    @Override
    public void onDelete(StaffAccount account) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete " + account.getUsername() + "? This cannot be undone.")
                .setPositiveButton("Yes, Delete", (dialog, which) -> {
                    SupabaseClient.getService().deleteStaffAccount("eq." + account.getId())
                            .enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                                    if (response.isSuccessful() || response.code() == 204) {
                                        showMessage("success", "Account deleted successfully.");
                                        if (account.getId().equals(editingUserId)) resetForm();
                                        fetchUsers();
                                    } else {
                                        showMessage("error", "Failed to delete account.");
                                    }
                                }

                                @Override
                                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                                    showMessage("error", "Connection error: " + t.getMessage());
                                }
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
