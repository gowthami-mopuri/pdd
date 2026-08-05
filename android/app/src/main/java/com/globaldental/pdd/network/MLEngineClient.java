package com.globaldental.pdd.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public class MLEngineClient {
    private static final String BASE_URL = "http://192.168.31.10:8000/";

    private static MLService service;

    // Chat models
    public static class ChatMessage {
        @SerializedName("role")
        public String role;
        @SerializedName("content")
        public String content;

        public ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    public static class ChatRequest {
        @SerializedName("patient_data")
        public Map<String, Object> patientData;
        @SerializedName("messages")
        public List<ChatMessage> messages;

        public ChatRequest(Map<String, Object> patientData, List<ChatMessage> messages) {
            this.patientData = patientData;
            this.messages = messages;
        }
    }

    public static class ChatResponse {
        @SerializedName("status")
        public String status;
        @SerializedName("reply")
        public String reply;
    }

    // Detection model
    public static class Detection {
        @SerializedName("class")
        public String className;
        @SerializedName("confidence")
        public double confidence;
        @SerializedName("bbox")
        public List<Double> bbox;
    }

    public static class AnalysisResponse {
        @SerializedName("status")
        public String status;
        @SerializedName("detections")
        public List<Detection> detections;
    }

    // Survival Prediction models
    public static class Factor {
        @SerializedName("label")
        public String label;
        @SerializedName("factor")
        public String factor;
        @SerializedName("risk")
        public String risk;
        @SerializedName("impact")
        public String impact;
        @SerializedName("level")
        public String level;
        @SerializedName("color")
        public String color;
        @SerializedName("pos")
        public Boolean pos;
    }

    public static class ActionItem {
        @SerializedName("text")
        public String text;
        @SerializedName("level")
        public String level;
        @SerializedName("type")
        public String type;
    }

    public static class SurvivalData {
        @SerializedName("survival_probability")
        public int survivalProbability;
        @SerializedName("failure_risk")
        public int failureRisk;
        @SerializedName("confidence")
        public int confidence;
        @SerializedName("risk_factors")
        public List<Factor> riskFactors;
        @SerializedName("success_factors")
        public List<Factor> successFactors;
        @SerializedName("action_items")
        public List<ActionItem> actionItems;
        @SerializedName("narrative")
        public List<String> narrative;
    }

    public static class SurvivalResponse {
        @SerializedName("status")
        public String status;
        @SerializedName("data")
        public SurvivalData data;
    }

    public interface MLService {
        @Multipart
        @POST("analyze/panoramic")
        Call<AnalysisResponse> analyzePanoramic(@Part MultipartBody.Part file);

        @Multipart
        @POST("analyze/implant")
        Call<AnalysisResponse> analyzeImplant(@Part MultipartBody.Part file);

        @Multipart
        @POST("analyze/mandibular")
        Call<AnalysisResponse> analyzeMandibular(@Part MultipartBody.Part file);

        @Multipart
        @POST("analyze/sinus")
        Call<AnalysisResponse> analyzeSinus(@Part MultipartBody.Part file);

        @Multipart
        @POST("analyze/gemini-survival")
        Call<SurvivalResponse> analyzeGeminiSurvival(
                @Part MultipartBody.Part file,
                @Part("patient_data") RequestBody patientData
        );

        @POST("chat/personalized")
        Call<ChatResponse> chatPersonalized(@Body ChatRequest request);
    }

    public static synchronized MLService getService() {
        if (service == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();

            service = retrofit.create(MLService.class);
        }
        return service;
    }
}
