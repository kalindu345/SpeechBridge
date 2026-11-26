package lk.kalindu.speechbridge.model;

import com.google.firebase.Timestamp;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Invoice implements java.io.Serializable {

    private String invoiceId;

    private String sessionId;

    private String therapistId;
    private String therapistName;

    private String patientId;
    private String patientName;

    private Timestamp sessionDate;
    private double amount;

    private String status;

    private Timestamp createdAt;


}
