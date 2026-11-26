package lk.kalindu.speechbridge.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import lk.kalindu.speechbridge.adapter.TherapistEarningsAdapter;
import lk.kalindu.speechbridge.databinding.FragmentTherapistEarningsBinding;
import lk.kalindu.speechbridge.model.Session;

public class TherapistEarningsFragment extends Fragment {

    private FragmentTherapistEarningsBinding binding;
    private FirebaseFirestore db;

    private TherapistEarningsAdapter adapter;
    private List<Session> sessionList = new ArrayList<>();

    private double totalEarnings = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentTherapistEarningsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        binding.recyclerEarnings.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TherapistEarningsAdapter(sessionList);
        binding.recyclerEarnings.setAdapter(adapter);

        loadEarnings();
    }

    private void loadEarnings() {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String therapistId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("session")
                .whereEqualTo("therapistId", therapistId)
                .whereEqualTo("status", "booked")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    sessionList.clear();
                    totalEarnings = 0;

                    for (var doc : queryDocumentSnapshots) {

                        Session session = doc.toObject(Session.class);
                        sessionList.add(session);

                        // Sum earnings
                        totalEarnings += session.getPrice();
                    }

                    // Update fragment UI
                    binding.txtTotalEarnings.setText("Rs. " + (int) totalEarnings);
                    binding.txtTotalSessions.setText(String.valueOf(sessionList.size()));

                    // Avg calculation of earnings
                    if (sessionList.size() > 0) {
                        double avg = totalEarnings / sessionList.size();
                        binding.txtAvgPrice.setText("Rs. " + (int) avg);
                    } else {
                        binding.txtAvgPrice.setText("Rs. 0");
                    }

                    adapter.notifyDataSetChanged();

                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error: " + e.getMessage());
                });
    }
}