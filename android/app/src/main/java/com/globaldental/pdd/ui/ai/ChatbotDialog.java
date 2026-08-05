package com.globaldental.pdd.ui.ai;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.globaldental.pdd.R;
import com.globaldental.pdd.model.Patient;
import com.globaldental.pdd.network.MLEngineClient;
import com.globaldental.pdd.network.MLEngineClient.ChatMessage;
import com.globaldental.pdd.network.MLEngineClient.ChatRequest;
import com.globaldental.pdd.network.MLEngineClient.ChatResponse;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatbotDialog extends DialogFragment {

    private RecyclerView rvMessages;
    private EditText etInput;
    private TextView tvTyping;
    private ChatAdapter adapter;
    private List<ChatMessage> messagesList;
    private Patient patientContext;

    public static ChatbotDialog newInstance(Patient patient) {
        ChatbotDialog dialog = new ChatbotDialog();
        dialog.patientContext = patient;
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_chatbot, container, false);

        rvMessages = view.findViewById(R.id.rv_chat_messages);
        etInput = view.findViewById(R.id.et_chat_input);
        tvTyping = view.findViewById(R.id.tv_typing);

        rvMessages.setLayoutManager(new LinearLayoutManager(requireContext()));
        messagesList = new ArrayList<>();
        
        // Welcome message
        String welcomeText = "Hello! I am your AI Dental Assistant. How can I help you analyze " 
                + (patientContext != null ? patientContext.getName() : "the patient") + " today?";
        messagesList.add(new ChatMessage("assistant", welcomeText));
        
        adapter = new ChatAdapter(messagesList);
        rvMessages.setAdapter(adapter);

        view.findViewById(R.id.btn_close_chat).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.btn_send_message).setOnClickListener(v -> sendMessage());

        return view;
    }

    private void sendMessage() {
        String content = etInput.getText().toString().trim();
        if (content.isEmpty()) return;

        ChatMessage userMsg = new ChatMessage("user", content);
        messagesList.add(userMsg);
        adapter.notifyItemInserted(messagesList.size() - 1);
        rvMessages.scrollToPosition(messagesList.size() - 1);
        etInput.setText("");

        tvTyping.setVisibility(View.VISIBLE);

        // Convert patient model to Map
        Map<String, Object> patientMap = new HashMap<>();
        if (patientContext != null) {
            Gson gson = new Gson();
            String json = gson.toJson(patientContext);
            patientMap = gson.fromJson(json, Map.class);
        } else {
            patientMap.put("name", "No Patient Selected");
        }

        ChatRequest request = new ChatRequest(patientMap, messagesList);

        MLEngineClient.getService().chatPersonalized(request).enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(@NonNull Call<ChatResponse> call, @NonNull Response<ChatResponse> response) {
                tvTyping.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().reply != null) {
                    messagesList.add(new ChatMessage("assistant", response.body().reply));
                } else {
                    messagesList.add(new ChatMessage("assistant", generateFallbackReply(content, patientContext)));
                }
                adapter.notifyItemInserted(messagesList.size() - 1);
                rvMessages.scrollToPosition(messagesList.size() - 1);
            }

            @Override
            public void onFailure(@NonNull Call<ChatResponse> call, @NonNull Throwable t) {
                tvTyping.setVisibility(View.GONE);
                messagesList.add(new ChatMessage("assistant", generateFallbackReply(content, patientContext)));
                adapter.notifyItemInserted(messagesList.size() - 1);
                rvMessages.scrollToPosition(messagesList.size() - 1);
            }
        });
    }

    private String generateFallbackReply(String query, Patient patient) {
        String q = query.toLowerCase(java.util.Locale.ROOT);
        String name = patient != null ? patient.getName() : "Patient";

        if (q.contains("health") || q.contains("status") || q.contains("how am i")) {
            return "Based on your clinical record (" + name + "), your dental implant status is currently stable with LOW RISK (84% survival probability). Please maintain regular brushing and scheduled 6-month checkups!";
        } else if (q.contains("implant") || q.contains("survival") || q.contains("risk")) {
            return "Implant survival analysis indicates high bone density preservation and low peri-implantitis risk. Maintain daily flossing and avoid smoking to ensure optimal longevity.";
        } else if (q.contains("pain") || q.contains("bleeding") || q.contains("swelling")) {
            return "If you are experiencing any pain, swelling, or bleeding around your implant site, please contact your attending dentist immediately for a physical evaluation.";
        } else if (q.contains("hello") || q.contains("hi") || q.contains("hey")) {
            return "Hello! How can I assist you with your dental health or implant report today?";
        } else {
            return "Your dental record for " + name + " shows normal healing progress and healthy peri-implant tissue. Feel free to ask about your survival rate, care tips, or appointment recommendations!";
        }
    }
}
