package lk.kalindu.speechbridge.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

import lk.kalindu.speechbridge.databinding.ActivityTherapistSignUpBinding;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class TherapistSignUpActivity extends AppCompatActivity {

    private ActivityTherapistSignUpBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private static final int PICK_PDF_REQUEST = 1;
    private Uri certificateUri;
    private ListenerRegistration approvalListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTherapistSignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();


        binding.therapistSignUpSignInBtn.setOnClickListener(view -> {
            startActivity(new Intent(this, TherapistSignInActivity.class));
            finish();
        });

        // Upload Certificate of therapist ( any proofed document)
        binding.therapistSignupUploadCertificate.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/pdf");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(intent, "Select Certificate PDF"), PICK_PDF_REQUEST);
        });


        binding.therapistSignupSignUpBtn.setOnClickListener(view -> signUpTherapist());
    }

    private void signUpTherapist() {
        String name = binding.therapistSignupInputName.getText().toString().trim();
        String mobile = binding.therapistSignupInputMobile.getText().toString().trim();
        String email = binding.therapistSignupInputEmail.getText().toString().trim();
        String password = binding.therapistSignupInputPassword.getText().toString().trim();
        String confirmPassword = binding.therapistSignupInputConfirmPassword.getText().toString().trim();


        if (name.isEmpty()) {
            binding.therapistSignupInputName.setError("Name is required");
            return;
        }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.therapistSignupInputEmail.setError("Enter valid Email");
            return;
        }
        if (mobile.isEmpty() || !mobile.matches("^(?:\\+94|0)?7[0-9]{8}$")) {
            binding.therapistSignupInputMobile.setError("Enter valid mobile");
            return;
        }

        if (password.isEmpty() || password.length() < 6) {
            binding.therapistSignupInputPassword.setError("Password must be at least 6 characters");
            return;
        }
        if (!password.equals(confirmPassword)) {
            binding.therapistSignupInputConfirmPassword.setError("Passwords do not match");
            return;
        }
        if (certificateUri == null) {
            Toast.makeText(this, "Please upload certificate", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button until admin approves from admin side
        binding.therapistSignupSignUpBtn.setEnabled(false);
        binding.therapistSignupSignUpBtn.setText("Signing Up...");

        // Create Firebase Auth User as Therapist
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        binding.therapistSignupSignUpBtn.setEnabled(true);
                        binding.therapistSignupSignUpBtn.setText("Sign Up");
                        Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }

                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user == null) return;
                    String uid = user.getUid();

                    // Prepare certificate filename
                    String cleanName = name.replaceAll(" ", "_");
                    String fileName = cleanName + "_" + uid + ".pdf";
                    StorageReference fileRef = storageReference.child("certificates/" + fileName);

                    // Upload PDF to Firebase Storage
                    fileRef.putFile(certificateUri)
                            .addOnSuccessListener(taskSnapshot ->
                                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {

                                        String certificateUrl = uri.toString();

                                        // Save therapist info in Firestore
                                        Map<String, Object> therapist = new HashMap<>();
                                        therapist.put("name", name);
                                        therapist.put("email", email);
                                        therapist.put("mobile", mobile);
                                        therapist.put("role", "therapist");
                                        therapist.put("certificateUrl", certificateUrl);
                                        therapist.put("approved", false); // default false
                                        therapist.put("profilePicUrl", null);
                                        therapist.put("uid", uid);

                                        DocumentReference docRef = firestore.collection("users").document(uid);
                                        docRef.set(therapist)
                                                .addOnSuccessListener(unused -> {
                                                    Toast.makeText(this, "Signup successful! Waiting for admin approval...", Toast.LENGTH_SHORT).show();

                                                    // Listen for approval status
                                                    listenForApproval(uid, docRef);

                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(this, "Firestore Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                                    resetSignUpButton();
                                                });

                                    })
                            )
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                resetSignUpButton();
                            });
                });
    }

    // Listen for admin approval
    private void listenForApproval(String uid, DocumentReference docRef) {
        approvalListener = docRef.addSnapshotListener((snapshot, error) -> {
            if (error != null || snapshot == null || !snapshot.exists()) return;

            Boolean approved = snapshot.getBoolean("approved");
            if (approved != null && approved) {
                // animation part
                binding.getRoot().animate().alpha(0f).setDuration(500).withEndAction(() -> {
                    Toast.makeText(this, "Approved! Redirecting to login...", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, TherapistSignInActivity.class));
                    finish();
                }).start();

                // Remove listener
                if (approvalListener != null) approvalListener.remove();
            }
        });
    }

    private void resetSignUpButton() {
        binding.therapistSignupSignUpBtn.setEnabled(true);
        binding.therapistSignupSignUpBtn.setText("Sign Up");
    }

    // Get Selected PDF
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_PDF_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            certificateUri = data.getData();
            binding.signupCertificateFileName.setText("Certificate Selected");
        }
    }
}
