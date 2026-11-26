package lk.kalindu.speechbridge.model;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Patient {
    private String name;
    private String email;
    private String mobile;
    private String role;
    private String profilePicUrl;
    private String uid;
}
