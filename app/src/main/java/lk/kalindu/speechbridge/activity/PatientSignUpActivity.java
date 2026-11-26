package lk.kalindu.speechbridge.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import lk.kalindu.speechbridge.databinding.ActivityPatientSignUpBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class PatientSignUpActivity extends AppCompatActivity {

    private ActivityPatientSignUpBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPatientSignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        binding.signUpSignInBtn.setOnClickListener(view -> {
            Intent intent = new Intent(PatientSignUpActivity.this, PatientSignInActivity.class);
            startActivity(intent);
            finish();
        });

        binding.signupSignUpBtn.setOnClickListener(view -> {

            String name = binding.signupInputName.getText().toString().trim();
            String mobile = binding.signupInputMobile.getText().toString().trim();
            String email = binding.signupInputEmail.getText().toString().trim();
            String password = binding.signupInputPassword.getText().toString().trim();
            String confirmPassword = binding.signupInputConfirmPassword.getText().toString().trim();

            if (name.isEmpty()) {
                binding.signupInputName.setError("Name is required");
                binding.signupInputName.requestFocus();
                return;
            }

            if (email.isEmpty()) {
                binding.signupInputEmail.setError("Email is required");
                binding.signupInputEmail.requestFocus();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.signupInputEmail.setError("Enter valid Email");
                binding.signupInputEmail.requestFocus();
                return;
            }

            if (mobile.isEmpty()) {
                binding.signupInputMobile.setError("Mobile is required");
                binding.signupInputMobile.requestFocus();
                return;
            }

            if (!mobile.matches("^(?:\\+94|0)?7[0-9]{8}$")) {
                binding.signupInputMobile.setError("Enter a valid Sri Lankan mobile number");
                binding.signupInputMobile.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                binding.signupInputPassword.setError("Password is required");
                binding.signupInputPassword.requestFocus();
                return;
            }

            if (password.length() < 6) {
                binding.signupInputPassword.setError("Password must be at least 6 characters");
                binding.signupInputPassword.requestFocus();
                return;
            }

            if (!confirmPassword.equals(password)) {
                binding.signupInputConfirmPassword.setError("Passwords do not match");
                binding.signupInputConfirmPassword.requestFocus();
                return;
            }

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {

                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            String uid = user.getUid();

                            Map<String, Object> patient = new HashMap<>();
                            patient.put("name", name);
                            patient.put("email", email);
                            patient.put("mobile", mobile);
                            patient.put("role", "patient");
                            patient.put("profilePicUrl",null);
                            patient.put("uid",uid);

                            firestore.collection("users")
                                    .document(uid)
                                    .set(patient)
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(PatientSignUpActivity.this, "Signup successful", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(PatientSignUpActivity.this, PatientSignInActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        e.printStackTrace();
                                        Toast.makeText(PatientSignUpActivity.this, "Error saving user: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    });

                        } else {
                            binding.signupInputEmail.setError(task.getException().getMessage());
                        }

                    });

        });

    }
}