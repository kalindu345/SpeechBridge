package lk.kalindu.speechbridge.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import lk.kalindu.speechbridge.databinding.ActivityPatientSignInBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;


public class PatientSignInActivity extends AppCompatActivity {

    private ActivityPatientSignInBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPatientSignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        binding.signInSignUpBtn.setOnClickListener(view -> {
            Intent intent = new Intent(PatientSignInActivity.this, PatientSignUpActivity.class);
            startActivity(intent);
            finish();
        });

        binding.signInSignInBtn.setOnClickListener(view -> {

            String email = binding.signinInputEmail.getText().toString().trim();
            String password = binding.signinInputPassword.getText().toString().trim();

            if (email.isEmpty()) {
                binding.signinInputEmail.setError("Email is required");
                binding.signinInputEmail.requestFocus();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.signinInputEmail.setError("Enter valid Email");
                binding.signinInputEmail.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                binding.signinInputPassword.setError("Password is required");
                binding.signinInputPassword.requestFocus();
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

    // Check whether the role is Patient or Therapist from Firebase Firestore
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

