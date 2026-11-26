package lk.kalindu.speechbridge.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import lk.kalindu.speechbridge.R;
import lk.kalindu.speechbridge.adapter.PatientSessionAdapter;
import lk.kalindu.speechbridge.databinding.FragmentPatientSessionViewBinding;
import lk.kalindu.speechbridge.model.Session;

public class PatientSessionViewFragment extends Fragment {

    private FragmentPatientSessionViewBinding binding;
    private PatientSessionAdapter adapter;
    private List<Session> sessionList = new ArrayList<>();
    private List<Session> filteredList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPatientSessionViewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        binding.sessionRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new PatientSessionAdapter(filteredList, new PatientSessionAdapter.OnSessionClickListener() {
            @Override
            public void onLocationClick(Session session) {
                if (session == null) return;

                String location = session.getLocation() != null ? session.getLocation() : "Unknown";
                Double latitude = session.getLatitude() != null ? session.getLatitude() : null;
                Double longitude = session.getLongitude() != null ? session.getLongitude() : null;

                // Open map fragment
                TherapistLocationFragment mapFragment = TherapistLocationFragment.newInstance(location, latitude, longitude);

                // Add fragment with backstack
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, mapFragment)
                        .addToBackStack("map")
                        .commit();
            }

            @Override
            public void onOnlineClick(Session session) {
                if (session == null) return;
                String link = session.getMeetingLink();
                if (link != null && !link.isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), "Meeting link not available", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.sessionRecyclerView.setAdapter(adapter);

        loadSessions();
        setupSearch();
    }

    private void loadSessions() {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) {
            binding.emptyView.setVisibility(View.VISIBLE);
            return;
        }

        db.collection("session")
                .whereEqualTo("patientId", uid)
                .whereEqualTo("status", "booked")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    sessionList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Session session = doc.toObject(Session.class);
                        sessionList.add(session);
                    }

                    filteredList.clear();
                    filteredList.addAll(sessionList);
                    adapter.notifyDataSetChanged();

                    binding.emptyView.setVisibility(sessionList.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> binding.emptyView.setVisibility(View.VISIBLE));
    }

    private void setupSearch() {
        binding.searchSession.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filteredList.clear();
                String query = s.toString().toLowerCase();

                for (Session session : sessionList) {
                    if (session.getTherapistName() != null &&
                            session.getTherapistName().toLowerCase().contains(query)) {
                        filteredList.add(session);
                    }
                }
                adapter.notifyDataSetChanged();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
}