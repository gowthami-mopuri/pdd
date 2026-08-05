package com.globaldental.pdd.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.globaldental.pdd.MainActivity;
import com.globaldental.pdd.R;
import com.globaldental.pdd.model.StaffAccount;
import com.globaldental.pdd.ui.auth.LandingFragment;
import com.globaldental.pdd.util.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MyProfileFragment extends Fragment {

    private TextView tvAvatar, tvUsername, tvRole, tvEmail, tvPhone,
            tvGender, tvDob, tvAddress, tvCreated, tvStatus;
    private TextInputEditText etNewPassword, etConfirmPassword;
    private Button btnSetPassword;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_profile, container, false);

        sessionManager = new SessionManager(requireContext());
        StaffAccount doctor = sessionManager.getDoctor();

        if (doctor == null) {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).replaceFragment(new LandingFragment());
            }
            return view;
        }

        // Show header with "My Profile" title
        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            activity.showAppHeader(true);
            activity.showBottomNavigation(true);
            activity.setHeaderTitle("My Profile");
        }

        // Bind views
        tvAvatar          = view.findViewById(R.id.tv_profile_avatar);
        tvUsername        = view.findViewById(R.id.tv_profile_username);
        tvRole            = view.findViewById(R.id.tv_profile_role);
        tvEmail           = view.findViewById(R.id.tv_profile_email);
        tvPhone           = view.findViewById(R.id.tv_profile_phone);
        tvGender          = view.findViewById(R.id.tv_profile_gender);
        tvDob             = view.findViewById(R.id.tv_profile_dob);
        tvAddress         = view.findViewById(R.id.tv_profile_address);
        tvCreated         = view.findViewById(R.id.tv_profile_created);
        tvStatus          = view.findViewById(R.id.tv_profile_status);
        etNewPassword     = view.findViewById(R.id.et_new_password);
        etConfirmPassword = view.findViewById(R.id.et_confirm_password);
        btnSetPassword    = view.findViewById(R.id.btn_set_password);

        // Populate data
        String name = doctor.getUsername() != null ? doctor.getUsername() : "N/A";
        String initials = name.length() >= 2
                ? name.substring(0, 2).toUpperCase()
                : name.toUpperCase();

        tvAvatar.setText(initials);
        tvUsername.setText(name);
        tvRole.setText("Clinical Staff Member");
        tvEmail.setText(doctor.getEmail() != null ? doctor.getEmail() : "—");
        tvPhone.setText(doctor.getPhone() != null ? doctor.getPhone() : "—");
        tvGender.setText(doctor.getGender() != null ? doctor.getGender() : "—");
        tvDob.setText(doctor.getDob() != null ? doctor.getDob() : "—");
        tvAddress.setText(doctor.getAddress() != null ? doctor.getAddress() : "—");
        tvCreated.setText(formatDate(doctor.getCreatedAt()));

        // Status badge
        String status = doctor.getStatus() != null ? doctor.getStatus() : "Active";
        tvStatus.setText("● " + capitalize(status));
        if (status.equalsIgnoreCase("Active")) {
            tvStatus.setTextColor(0xFF16A34A); // green
            tvStatus.setBackgroundResource(R.drawable.bg_status_badge);
        } else {
            tvStatus.setTextColor(0xFFDC2626); // red
            tvStatus.setBackgroundResource(R.drawable.bg_status_badge_red);
        }

        // Password change
        btnSetPassword.setOnClickListener(v -> {
            String newPass = etNewPassword.getText() != null
                    ? etNewPassword.getText().toString().trim() : "";
            String confirmPass = etConfirmPassword.getText() != null
                    ? etConfirmPassword.getText().toString().trim() : "";

            if (newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in both password fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPass.equals(confirmPass)) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newPass.length() < 6) {
                Toast.makeText(requireContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }
            // Save updated password locally
            doctor.setPassword(newPass);
            sessionManager.saveDoctor(doctor);
            Toast.makeText(requireContext(), "Password updated successfully", Toast.LENGTH_SHORT).show();
            etNewPassword.setText("");
            etConfirmPassword.setText("");
        });

        return view;
    }

    /** Parses an ISO-8601 timestamp and returns a readable date like "30 May 2026". */
    private String formatDate(String raw) {
        if (raw == null || raw.isEmpty()) return "—";
        // Try ISO with microseconds: 2026-05-30T20:54:49.185748+00:00
        String[] patterns = {
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd"
        };
        for (String pattern : patterns) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
                Date date = sdf.parse(raw);
                if (date != null) {
                    return new SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(date);
                }
            } catch (ParseException ignored) { }
        }
        // Fallback: trim the T part
        return raw.contains("T") ? raw.substring(0, raw.indexOf("T")) : raw;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase(Locale.getDefault()) + s.substring(1).toLowerCase(Locale.getDefault());
    }
}
