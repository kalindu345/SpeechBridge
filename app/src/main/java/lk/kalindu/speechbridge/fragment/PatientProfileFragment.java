package lk.kalindu.speechbridge.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import lk.kalindu.speechbridge.R;
import lk.kalindu.speechbridge.databinding.FragmentPatientProfileBinding;
import lk.kalindu.speechbridge.model.Patient;

public class PatientProfileFragment extends Fragment {

    private FragmentPatientProfileBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;

    private static final int PICK_IMAGE_REQUEST = 101;
    private Uri imageUri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    @Override
    public android.view.View onCreateView(@NonNull android.view.LayoutInflater inflater,
                                          android.view.ViewGroup container,
                                          Bundle savedInstanceState) {
        binding = FragmentPatientProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull android.view.View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadPatientDetails();

        // Profile pic click listener
        binding.imgProfile.setOnClickListener(v ->
                openImagePicker()
        );

        // Edit Button
        binding.btnEditProfile.setOnClickListener(v -> openImagePicker());
    }

    private void loadPatientDetails() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) return;

        firestore.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(ds -> {
                    if (ds.exists()) {
                        Patient patient = ds.toObject(Patient.class);
                        if (patient != null) {
                            binding.txtName.setText(patient.getName());
                            binding.txtEmail.setText(patient.getEmail());
                            binding.txtMobile.setText(patient.getMobile());

                            binding.cardEmail.setText(patient.getEmail());
                            binding.cardMobile.setText(patient.getMobile());

                            if (patient.getProfilePicUrl() != null) {
                                Glide.with(requireContext())
                                        .load(patient.getProfilePicUrl())
                                        .placeholder(R.drawable.profile_patient)
                                        .into(binding.imgProfile);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("PatientProfile", "Error loading patient: " + e.getMessage()));
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            uploadProfileImage();
        }
    }

    private void uploadProfileImage() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null || imageUri == null) return;

        StorageReference storageRef = storage.getReference()
                .child("profile_pics/" + user.getUid() + ".jpg");

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            // Update Firestore
                            firestore.collection("users")
                                    .document(user.getUid())
                                    .update("profilePicUrl", uri.toString())
                                    .addOnSuccessListener(aVoid -> {
                                        Glide.with(requireContext())
                                                .load(uri)
                                                .placeholder(R.drawable.profile_patient)
                                                .into(binding.imgProfile);
                                        Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update profile: " + e.getMessage(), Toast.LENGTH_LONG).show());
                        }))
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Image upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}