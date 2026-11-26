package lk.kalindu.speechbridge.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import lk.kalindu.speechbridge.R;
import lk.kalindu.speechbridge.adapter.TherapistListViewAdapter;
import lk.kalindu.speechbridge.databinding.FragmentPatientTherapistsViewBinding;
import lk.kalindu.speechbridge.model.Therapist;

public class PatientTherapistsViewFragment extends Fragment {


    private FragmentPatientTherapistsViewBinding binding;

    private TherapistListViewAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding=FragmentPatientTherapistsViewBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.therapistRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .whereEqualTo("role", "therapist")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        List<Therapist> therapists = new ArrayList<>();

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Therapist therapist = doc.toObject(Therapist.class);
                            therapist.setUid(doc.getId());
                            therapists.add(therapist);
                        }

                        adapter = new TherapistListViewAdapter(therapists, therapist -> {

                            Bundle bundle = new Bundle();
                            bundle.putString("therapistId", therapist.getUid());

                            TherapistDetailsFragment fragment = new TherapistDetailsFragment();
                            fragment.setArguments(bundle);

                            getParentFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, fragment)
                                    .addToBackStack(null)
                                    .commit();
                        });

                        binding.therapistRecyclerView.setAdapter(adapter);

                    } else {
                        Log.e("Firestore", "Error: " + task.getException().getMessage());
                    }
                });

        binding.searchTherapist.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.filter(s.toString());
                }
            }

        });
    }
}