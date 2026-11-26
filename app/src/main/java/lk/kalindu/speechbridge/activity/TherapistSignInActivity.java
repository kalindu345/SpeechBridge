package lk.kalindu.speechbridge.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import lk.kalindu.speechbridge.R;
import lk.kalindu.speechbridge.databinding.ActivityTherapistSignInBinding;

public class TherapistSignInActivity extends AppCompatActivity {
    private ActivityTherapistSignInBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_therapist_sign_in);

        binding = ActivityTherapistSignInBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        binding.therapistSignInSignUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TherapistSignInActivity.this, TherapistSignUpActivity.class);
                startActivity(intent);
                finish();
            }
        });

        binding.therapistSignInSignInBtn.setOnClickListener(view -> {
            String email = binding.signinTherapistInputEmail.getText().toString().trim();
            String password = binding.signinTherapistInputPassword.getText().toString().trim();

            if (email.isEmpty()) {
                binding.signinTherapistInputEmail.setError("Email is required");
                binding.signinTherapistInputEmail.requestFocus();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.signinTherapistInputEmail.setError("Enter valid Email");
                binding.signinTherapistInputEmail.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                binding.signinTherapistInputPassword.setError("Password is required");
                binding.signinTherapistInputPassword.requestFocus();
                return;
            }

            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {

                            FirebaseUser user = firebaseAuth.getCurrentUser();

                            if (user != null) {
                                checkUserRole(user.getUid());
                            }

                        } else {
                            Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show();
                        }

                    });
        });



    }

    private void checkUserRole(String uid) {

        firestore.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (documentSnapshot.exists()) {

                        String role = documentSnapshot.getString("role");

                        if ("patient".equals(role)) {

                            startActivity(new Intent(this, PatientHomeActivity.class));
                            finish();

                        } else if ("therapist".equals(role)) {

                            startActivity(new Intent(this, TherapistHomeActivity.class));
                            finish();

                        } else {

                            Toast.makeText(this, "Unknown role", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
                    }

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}