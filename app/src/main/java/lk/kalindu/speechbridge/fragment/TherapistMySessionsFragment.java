package lk.kalindu.speechbridge.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lk.kalindu.speechbridge.adapter.TherapistSessionAdapter;
import lk.kalindu.speechbridge.databinding.FragmentTherapistMySessionsBinding;
import lk.kalindu.speechbridge.model.Session;

public class TherapistMySessionsFragment extends Fragment {

    private FragmentTherapistMySessionsBinding binding;
    private FirebaseFirestore db;

    private TherapistSessionAdapter adapter;
    private List<Session> sessionList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentTherapistMySessionsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String therapistId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        binding.recyclerTherapistSessions.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new TherapistSessionAdapter(sessionList);
        binding.recyclerTherapistSessions.setAdapter(adapter);

        loadTherapistSessions(therapistId);
    }

    private void loadTherapistSessions(String therapistId) {

        db.collection("session")
                .whereEqualTo("therapistId", therapistId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    sessionList.clear();

                    for (var doc : queryDocumentSnapshots) {

                        try {
                            Session session = doc.toObject(Session.class);


                            Object dateObj = doc.get("date");

                            if (dateObj instanceof Long) {
                                session.setDate(
                                        new com.google.firebase.Timestamp(
                                                new Date((Long) dateObj)
                                        )
                                );
                            } else if (dateObj instanceof com.google.firebase.Timestamp) {
                                session.setDate((com.google.firebase.Timestamp) dateObj);
                            }

                            sessionList.add(session);

                        } catch (Exception e) {
                            Log.e("Firestore", "Parsing error: " + e.getMessage());
                        }
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error: " + e.getMessage());
                });
    }
}