package lk.kalindu.speechbridge.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.PropertyName;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Session implements Serializable {

    private String sessionId;
    private String therapistId;
    private String therapistName;
    private String patientId;
    private String type;

    @SerializedName("date") // match API field
    private com.google.firebase.Timestamp date;

    private String startTime;
    private String location;
    private com.google.firebase.firestore.GeoPoint pinnedLocation;
    private String meetingLink;

    @SerializedName("price") // match API field name
    private double price;

    private String status;

    private Double latitude;
    private Double longitude;

    // When pinnedLocation is set, update latitude/longitude automatically
    public void setPinnedLocation(GeoPoint pinnedLocation) {
        this.pinnedLocation = pinnedLocation;
        if (pinnedLocation != null) {
            this.latitude = pinnedLocation.getLatitude();
            this.longitude = pinnedLocation.getLongitude();
        } else {
            this.latitude = null;
            this.longitude = null;
        }
    }
}