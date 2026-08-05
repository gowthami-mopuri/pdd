package com.globaldental.pdd.network;

import com.globaldental.pdd.model.Patient;
import com.globaldental.pdd.model.StaffAccount;
import androidx.annotation.NonNull;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;
import java.io.IOException;
import java.util.List;

public class SupabaseClient {
    private static final String BASE_URL = "https://gamfhzsvticwvzybchae.supabase.co/rest/v1/";
    private static final String ANON_KEY = "sb_publishable_CNsi7ICGKmNcqLkGOW7RdQ_xJEHgk7N";

    private static SupabaseService service;

    public interface SupabaseService {
        @GET("staff_accounts")
        Call<List<StaffAccount>> loginDoctor(
                @Query("username") String usernameFilter, // pass "eq.username"
                @Query("password") String passwordFilter  // pass "eq.password"
        );

        @GET("patients")
        Call<List<Patient>> loginPatient(
                @Query("patient_id") String patientIdFilter // pass "eq.PT-xxxx-xxx"
        );

        @GET("patients")
        Call<List<Patient>> getPatients();

        @POST("patients")
        @Headers("Prefer: return=representation")
        Call<List<Patient>> addPatient(@Body Patient patient);

        @PATCH("patients")
        @Headers("Prefer: return=representation")
        Call<List<Patient>> updatePatient(
                @Query("id") String idFilter, // pass "eq.uuid"
                @Body Patient patient
        );

        @GET("staff_accounts")
        Call<List<StaffAccount>> getStaffAccounts();

        @POST("staff_accounts")
        @Headers("Prefer: return=representation")
        Call<List<StaffAccount>> createStaffAccount(@Body StaffAccount account);

        @PATCH("staff_accounts")
        @Headers("Prefer: return=representation")
        Call<List<StaffAccount>> updateStaffAccount(
                @Query("id") String idFilter, // pass "eq.uuid"
                @Body StaffAccount account
        );

        @retrofit2.http.DELETE("staff_accounts")
        Call<Void> deleteStaffAccount(
                @Query("id") String idFilter // pass "eq.uuid"
        );
    }

    public static synchronized SupabaseService getService() {
        if (service == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(new Interceptor() {
                        @NonNull
                        @Override
                        public Response intercept(@NonNull Chain chain) throws IOException {
                            Request original = chain.request();
                            Request.Builder requestBuilder = original.newBuilder()
                                    .header("apikey", ANON_KEY)
                                    .header("Authorization", "Bearer " + ANON_KEY)
                                    .header("Content-Type", "application/json")
                                    .method(original.method(), original.body());
                            return chain.proceed(requestBuilder.build());
                        }
                    })
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();

            service = retrofit.create(SupabaseService.class);
        }
        return service;
    }
}
