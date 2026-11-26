package lk.kalindu.speechbridge.network;

import android.util.Log;

import com.google.firebase.firestore.GeoPoint;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import lk.kalindu.speechbridge.model.Session;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiClient {

    private static final String BASE_URL = "http://192.168.1.100:8080/api/sessions";
    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // Create a session and send pinned location if available
    public static void createSession(Session session, ApiCallback callback) {
        try {
            // Extract latitude & longitude from GeoPoint
            Double latitude = null;
            Double longitude = null;

            GeoPoint geo = session.getPinnedLocation();
            if (geo != null) {
                latitude = geo.getLatitude();
                longitude = geo.getLongitude();
            }

            // Prepare payload
            SessionPayload payload = new SessionPayload(
                    session.getSessionId(),
                    session.getTherapistId(),
                    session.getTherapistName(),
                    session.getPatientId(),
                    session.getType(),
                    session.getDate() != null ? session.getDate().toDate().getTime() : System.currentTimeMillis(),
                    session.getStartTime(),
                    session.getLocation(),
                    session.getMeetingLink(),
                    session.getPrice(),
                    session.getStatus(),
                    latitude,
                    longitude
            );

            String json = gson.toJson(payload);

            RequestBody body = RequestBody.create(
                    json,
                    MediaType.get("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(BASE_URL)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("ApiClient", "API call failed", e);
                    callback.onFailure(e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {

                        String resp = response.body().string();
                        Log.d("API_RESPONSE", resp);


                        callback.onSuccess(null);

                    } else {
                        callback.onFailure("Error: " + response.code());
                    }
                }
            });

        } catch (Exception e) {
            callback.onFailure(e.getMessage());
        }
    }

   //Callback Interface
    public interface ApiCallback {
        void onSuccess(Session session);
        void onFailure(String errorMessage);
    }

    // Payload class matches with API
    private static class SessionPayload {
        private String sessionId;
        private String therapistId;
        private String therapistName;
        private String patientId;
        private String type;
        private long date;
        private String startTime;
        private String location;
        private String meetingLink;
        private double price;
        private String status;
        private Double latitude;
        private Double longitude;

        public SessionPayload(String sessionId, String therapistId, String therapistName,
                              String patientId, String type, long date, String startTime,
                              String location, String meetingLink, double price,
                              String status, Double latitude, Double longitude) {
            this.sessionId = sessionId;
            this.therapistId = therapistId;
            this.therapistName = therapistName;
            this.patientId = patientId;
            this.type = type;
            this.date = date;
            this.startTime = startTime;
            this.location = location;
            this.meetingLink = meetingLink;
            this.price = price;
            this.status = status;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}