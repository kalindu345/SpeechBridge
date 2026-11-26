package lk.kalindu.speechbridge.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import lk.kalindu.speechbridge.databinding.FragmentPatientComplaintsBinding;

public class PatientComplaintsFragment extends Fragment {

    private FragmentPatientComplaintsBinding binding;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentPatientComplaintsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        handleSubmit();
    }

    private void handleSubmit() {

        binding.btnSubmitComplaint.setOnClickListener(v -> {

            String complaintText = binding.inputComplaint.getText().toString().trim();

            if (TextUtils.isEmpty(complaintText)) {
                Toast.makeText(getContext(), "Please enter your complaint", Toast.LENGTH_SHORT).show();
                return;
            }

            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            // Generate a new complaint document reference with an auto ID
            DocumentReference docRef = db.collection("complaints").document();

            // complaint data map
            Map<String, Object> complaint = new HashMap<>();
            complaint.put("complaintId", docRef.getId()); // Set complaintId here
            complaint.put("complaint", complaintText);
            complaint.put("userId", userId);

            // Save to Firestore
            docRef.set(complaint)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Complaint submitted successfully", Toast.LENGTH_SHORT).show();
                        binding.inputComplaint.setText(""); // clear input
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });
    }
}