package com.globaldental.pdd.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.globaldental.pdd.MainActivity;
import com.globaldental.pdd.R;

public class LandingFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_landing, container, false);

        // Hide drawer/toolbar on Landing Page
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showNavigationDrawer(false);
        }

        View.OnClickListener doctorClick = v -> navigateTo(new DoctorLoginFragment());
        View cardDoctor = view.findViewById(R.id.card_doctor_portal);
        View btnDoctor = view.findViewById(R.id.btn_doctor_login);
        if (cardDoctor != null) cardDoctor.setOnClickListener(doctorClick);
        if (btnDoctor != null) btnDoctor.setOnClickListener(doctorClick);

        View.OnClickListener patientClick = v -> navigateTo(new PatientLoginFragment());
        View cardPatient = view.findViewById(R.id.card_patient_portal);
        View btnPatient = view.findViewById(R.id.btn_patient_login);
        if (cardPatient != null) cardPatient.setOnClickListener(patientClick);
        if (btnPatient != null) btnPatient.setOnClickListener(patientClick);

        View.OnClickListener adminClick = v -> navigateTo(new AdminLoginFragment());
        View cardAdmin = view.findViewById(R.id.card_admin_portal);
        View btnAdmin = view.findViewById(R.id.btn_admin_login);
        if (cardAdmin != null) cardAdmin.setOnClickListener(adminClick);
        if (btnAdmin != null) btnAdmin.setOnClickListener(adminClick);

        return view;
    }

    private void navigateTo(Fragment fragment) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).replaceFragment(fragment);
        }
    }
}
