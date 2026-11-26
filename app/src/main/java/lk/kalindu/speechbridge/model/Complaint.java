package lk.kalindu.speechbridge.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Complaint {

    private String complaintId;
    private String userId;
    private String complaint;

}
