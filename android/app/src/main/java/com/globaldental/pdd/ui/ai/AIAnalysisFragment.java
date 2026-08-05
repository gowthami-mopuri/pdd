package com.globaldental.pdd.ui.ai;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.globaldental.pdd.MainActivity;
import com.globaldental.pdd.R;
import com.globaldental.pdd.model.Patient;
import com.globaldental.pdd.network.SupabaseClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AIAnalysisFragment extends Fragment {

    private Spinner spSelectPatient;
    private MaterialButton btnSelectImage, btnChangeImage, btnRunDetection;
    private MaterialCardView cardUploadZone, cardPreviewZone;
    private ImageView ivScanPreview;

    private List<Patient> patientsList = new ArrayList<>();
    private Patient selectedPatient;
    private Uri selectedImageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ai_analysis, container, false);

        if (getActivity() instanceof MainActivity) {
            MainActivity a = (MainActivity) getActivity();
            a.showAppHeader(true);
            a.showBottomNavigation(true);
            a.setHeaderTitle("AI Analysis");
        }

        spSelectPatient   = view.findViewById(R.id.sp_select_patient);
        btnSelectImage    = view.findViewById(R.id.btn_select_image);
        btnChangeImage    = view.findViewById(R.id.btn_change_image);
        btnRunDetection   = view.findViewById(R.id.btn_run_detection);
        cardUploadZone    = view.findViewById(R.id.card_upload_zone);
        cardPreviewZone   = view.findViewById(R.id.card_preview_zone);
        ivScanPreview     = view.findViewById(R.id.iv_scan_preview);

        // Image picker
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        loadSelectedImage();
                    }
                }
        );

        cardUploadZone.setOnClickListener(v -> pickImage());
        btnSelectImage.setOnClickListener(v -> pickImage());

        btnChangeImage.setOnClickListener(v -> {
            selectedImageUri = null;
            cardUploadZone.setVisibility(View.VISIBLE);
            cardPreviewZone.setVisibility(View.GONE);
            pickImage();
        });

        btnRunDetection.setOnClickListener(v -> runAnalysis());

        fetchPatients();
        return view;
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void loadSelectedImage() {
        if (selectedImageUri == null) return;
        try {
            InputStream is = requireContext().getContentResolver().openInputStream(selectedImageUri);
            Bitmap bmp = BitmapFactory.decodeStream(is);
            if (bmp != null) {
                ivScanPreview.setImageBitmap(bmp);
                cardUploadZone.setVisibility(View.GONE);
                cardPreviewZone.setVisibility(View.VISIBLE);

                if (isAppScreenshot(bmp)) {
                    showScreenshotWarning();
                }
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Failed to load image.", Toast.LENGTH_SHORT).show();
        }
    }

    private void runAnalysis() {
        if (selectedImageUri == null) {
            Toast.makeText(requireContext(), "Please upload a scan first.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check image screenshot status
        try {
            InputStream is = requireContext().getContentResolver().openInputStream(selectedImageUri);
            Bitmap bmp = BitmapFactory.decodeStream(is);
            if (bmp != null && isAppScreenshot(bmp)) {
                showScreenshotWarning();
                return;
            }
        } catch (Exception ignored) {}

        if (selectedPatient == null) {
            Toast.makeText(requireContext(), "Please select a patient first.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Navigate to ImplantSurvivalFragment with patient + image URI
        ImplantSurvivalFragment fragment = new ImplantSurvivalFragment();
        Bundle args = new Bundle();
        args.putString("patientJson", new Gson().toJson(selectedPatient));
        args.putString("imageUriString", selectedImageUri.toString());
        args.putBoolean("isNewAnalysis", true);
        args.putString("fromSource", "analysis");
        fragment.setArguments(args);

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).replaceFragment(fragment);
        }
    }

    private void showScreenshotWarning() {
        if (!isAdded()) return;
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("⚠️ Invalid Medical Scan")
                .setMessage("The selected file is a mobile app UI screenshot instead of a dental X-Ray scan.\n\nPlease tap 'CHANGE IMAGE' and select a genuine CBCT or Panoramic Dental X-Ray scan.")
                .setPositiveButton("Change Image", (dialog, which) -> {
                    selectedImageUri = null;
                    cardUploadZone.setVisibility(View.VISIBLE);
                    cardPreviewZone.setVisibility(View.GONE);
                    pickImage();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private boolean isAppScreenshot(Bitmap bmp) {
        if (bmp == null) return false;
        int w = bmp.getWidth();
        int h = bmp.getHeight();

        int blueHeaderPixels = 0;
        int pureWhitePixels = 0;

        for (int y = 0; y < h; y += Math.max(1, h / 60)) {
            for (int x = 0; x < w; x += Math.max(1, w / 60)) {
                int pixel = bmp.getPixel(x, y);
                int r = (pixel >> 16) & 0xff;
                int g = (pixel >> 8) & 0xff;
                int b = pixel & 0xff;

                // Check for primary blue UI headers (#1E40AF / #2563EB / #1D4ED8)
                if (b > 130 && r < 70 && g < 130) {
                    blueHeaderPixels++;
                }

                // Check for solid pure white UI backgrounds
                if (r > 245 && g > 245 && b > 245) {
                    pureWhitePixels++;
                }
            }
        }

        return (blueHeaderPixels >= 5) || (pureWhitePixels > 1200);
    }

    private void fetchPatients() {
        SupabaseClient.getService().getPatients().enqueue(new Callback<List<Patient>>() {
            @Override
            public void onResponse(@NonNull Call<List<Patient>> call,
                                   @NonNull Response<List<Patient>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    patientsList = response.body();
                    populatePatientsSpinner();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Patient>> call, @NonNull Throwable t) {
                // Silent — offline mode
            }
        });
    }

    private void populatePatientsSpinner() {
        if (patientsList == null || patientsList.isEmpty()) return;

        List<String> names = new ArrayList<>();
        names.add("-- Select a Patient --");
        for (Patient p : patientsList) {
            names.add(p.getName() + " (" + p.getPatientId() + ")");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, names);
        spSelectPatient.setAdapter(adapter);

        spSelectPatient.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPatient = position == 0 ? null : patientsList.get(position - 1);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
}
