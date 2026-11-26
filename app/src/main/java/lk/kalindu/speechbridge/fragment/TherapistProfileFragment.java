package lk.kalindu.speechbridge.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.Map;

import lk.kalindu.speechbridge.databinding.FragmentTherapistProfileBinding;
import lk.kalindu.speechbridge.model.Therapist;

public class TherapistProfileFragment extends Fragment {

    private FragmentTherapistProfileBinding binding;

    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private static final int PICK_IMAGE_REQUEST = 101;

    private Uri imageUri;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentTherapistProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        loadProfile();

        // Click profile image → pick image
        binding.imgProfile.setOnClickListener(v ->
                openImagePicker()
        );

        // View certificate
        binding.btnViewCertificate.setOnClickListener(v ->
                openCertificate()
        );
    }

    // Load therapist data
    private void loadProfile() {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {

                    if (doc.exists()) {

                        Therapist therapist = doc.toObject(Therapist.class);

                        binding.txtName.setText(therapist.getName());
                        binding.txtEmail.setText(therapist.getEmail());
                        binding.txtMobile.setText(therapist.getMobile());

                        // Load profile image
                        if (therapist.getProfilePicUrl() != null) {
                            Glide.with(requireContext())
                                    .load(therapist.getProfilePicUrl())
                                    .into(binding.imgProfile);
                        }



                    }

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Open image picker
    private void openImagePicker() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // Handle image selected
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == Activity.RESULT_OK
                && data != null
                && data.getData() != null) {

            imageUri = data.getData();

            uploadProfileImage();
        }
    }

    // Upload image to Firebase Storage
    private void uploadProfileImage() {

        if (imageUri == null) return;

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        StorageReference ref = storage.getReference()
                .child("profile_images/" + uid + ".jpg");

        ref.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl()
                        .addOnSuccessListener(uri -> {

                            String imageUrl = uri.toString();

                            // Save URL to Firestore
                            Map<String, Object> update = new HashMap<>();
                            update.put("profilePicUrl", imageUrl);

                            db.collection("users")
                                    .document(uid)
                                    .update(update)
                                    .addOnSuccessListener(unused -> {

                                        Glide.with(requireContext())
                                                .load(imageUrl)
                                                .into(binding.imgProfile);

                                        Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show();

                                    });

                        }))
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Open certificate PDF or licence pdf
    private void openCertificate() {

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {

                    if (doc.exists()) {

                        String url = doc.getString("certificateUrl");

                        if (url != null && !url.isEmpty()) {

                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(url));
                            startActivity(intent);

                        } else {
                            Toast.makeText(getContext(), "No certificate uploaded", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}