package lk.kalindu.speechbridge.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lk.kalindu.speechbridge.R;
import lk.kalindu.speechbridge.adapter.TherapistSessionViewAdapter;
import lk.kalindu.speechbridge.databinding.FragmentTherapistDetailsBinding;
import lk.kalindu.speechbridge.model.Invoice;
import lk.kalindu.speechbridge.model.Session;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.model.InitRequest;

public class TherapistDetailsFragment extends Fragment {

    private FragmentTherapistDetailsBinding binding;
    private TherapistSessionViewAdapter adapter;
    private List<Session> sessionList = new ArrayList<>();
    private FirebaseFirestore db;
    private String therapistId;
    private Session selectedSession;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTherapistDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            therapistId = getArguments().getString("therapistId");
        }

        if (therapistId == null || therapistId.isEmpty()) {
            Toast.makeText(getContext(), "Therapist ID missing!", Toast.LENGTH_LONG).show();
            return;
        }

        // RecyclerView
        binding.sessionRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TherapistSessionViewAdapter(sessionList, new TherapistSessionViewAdapter.OnSessionClickListener() {
            @Override
            public void OnSessionClick(Session session) {
                selectedSession = session;
                fetchPatientAndLaunchPayHere(session);
            }

            @Override
            public void OnLocationClick(Session session) {
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, TherapistLocationFragment.newInstance(
                                session.getLocation(),
                                session.getLatitude(),
                                session.getLongitude()
                        ))
                        .addToBackStack(null)
                        .commit();
            }
        });
        binding.sessionRecyclerView.setAdapter(adapter);

        loadSessions();
    }

    private void loadSessions() {
        db.collection("session")
                .whereEqualTo("therapistId", therapistId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    sessionList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {


                        Object rawDate = doc.get("date");
                        if (rawDate instanceof Long) {
                            doc.getReference().update("date",
                                    new Timestamp(new Date((Long) rawDate)));
                        }

                        try {
                            Session session = doc.toObject(Session.class);
                            session.setSessionId(doc.getId());

                            if (session.getType() == null) session.setType("online");

                            if (!"booked".equalsIgnoreCase(session.getStatus())) {
                                sessionList.add(session);
                            }

                        } catch (Exception e) {
                            Log.e("SESSION_LOAD_ERROR",
                                    "Failed to parse session: " + doc.getId(), e);
                        }
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("SESSION_ERROR", e.getMessage(), e);
                    Toast.makeText(getContext(),
                            "Failed to load sessions", Toast.LENGTH_SHORT).show();
                });
    }

    // PayHere payment launcher
    private final ActivityResultLauncher<Intent> payhereLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == getActivity().RESULT_OK) {
                            Toast.makeText(getContext(), "Payment Success", Toast.LENGTH_SHORT).show();
                            createInvoiceAndUpdateSession(selectedSession);
                        } else if (result.getResultCode() == getActivity().RESULT_CANCELED) {
                            Toast.makeText(getContext(), "Payment Cancelled", Toast.LENGTH_SHORT).show();
                        }
                    });

    private void fetchPatientAndLaunchPayHere(Session session) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(getContext(), "Patient info not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String fullName = doc.getString("name");
                    String email = doc.getString("email");
                    String mobile = doc.getString("mobile");

                    String firstName = fullName;
                    String lastName = "";
                    if (fullName != null && fullName.contains(" ")) {
                        int idx = fullName.lastIndexOf(" ");
                        firstName = fullName.substring(0, idx);
                        lastName = fullName.substring(idx + 1);
                    }

                    launchPayHere(session, firstName, lastName, email, mobile);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to fetch patient info", Toast.LENGTH_SHORT).show();
                    Log.e("PATIENT_FETCH_ERROR", e.getMessage(), e);
                });
    }

    private void launchPayHere(Session session, String firstName, String lastName,
                               String email, String mobile) {

        double total = session.getPrice();
        InitRequest req = new InitRequest();
        req.setSandBox(true);
        req.setMerchantId("1222314");
        req.setMerchantSecret("MjIyNDQ1NDM2OTE3NjYyNzA5OTM3MTEzMTc3NzgyMzMwODc4MDQ1");
        req.setCurrency("LKR");
        req.setAmount(Math.round(total * 100.0) / 100.0);
        req.setOrderId("SESSION-" + session.getSessionId());
        req.setItemsDescription("Therapy Session");

        req.getCustomer().setFirstName(firstName);
        req.getCustomer().setLastName(lastName);
        req.getCustomer().setEmail(email);
        req.getCustomer().setPhone(mobile);
        req.getCustomer().getAddress().setAddress("No.1, Galle Road");
        req.getCustomer().getAddress().setCity("Colombo");
        req.getCustomer().getAddress().setCountry("Sri Lanka");

        req.setNotifyUrl("https://speechbridge.requestcatcher.com//");

        Intent intent = new Intent(getActivity(), PHMainActivity.class);
        intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
        payhereLauncher.launch(intent);
    }

    private void createInvoiceAndUpdateSession(Session session) {
        if (session == null) return;

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Get Patient Information
        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(patientDoc -> {
                    if (!patientDoc.exists()) return;

                    String patientName = patientDoc.getString("name");
                    String patientEmail = patientDoc.getString("email");
                    String patientMobile = patientDoc.getString("mobile");

                    // Get Therapist Information
                    db.collection("users").document(session.getTherapistId())
                            .get()
                            .addOnSuccessListener(therapistDoc -> {
                                if (!therapistDoc.exists()) return;

                                String therapistName = therapistDoc.getString("name");

                                // Update session document
                                Map<String, Object> sessionUpdates = new HashMap<>();
                                sessionUpdates.put("status", "booked");
                                sessionUpdates.put("patientId", currentUserId);
                                sessionUpdates.put("patientName", patientName);
                                sessionUpdates.put("patientEmail", patientEmail);
                                sessionUpdates.put("patientMobile", patientMobile);
                                sessionUpdates.put("therapistName", therapistName);

                                db.collection("session").document(session.getSessionId())
                                        .update(sessionUpdates)
                                        .addOnSuccessListener(unused -> {
                                            // Create invoice with real details
                                            Map<String, Object> invoiceData = new HashMap<>();
                                            String invoiceId = db.collection("invoice").document().getId();
                                            invoiceData.put("invoiceId", invoiceId);
                                            invoiceData.put("sessionId", session.getSessionId());
                                            invoiceData.put("therapistId", session.getTherapistId());
                                            invoiceData.put("therapistName", therapistName);
                                            invoiceData.put("patientId", currentUserId);
                                            invoiceData.put("patientName", patientName);
                                            invoiceData.put("sessionDate", session.getDate());
                                            invoiceData.put("amount", session.getPrice());
                                            invoiceData.put("status", "completed");
                                            invoiceData.put("createdAt", FieldValue.serverTimestamp());

                                            db.collection("invoice")
                                                    .document(invoiceId)
                                                    .set(invoiceData)
                                                    .addOnSuccessListener(docRef -> {
                                                        adapter.notifyDataSetChanged();

                                                        // Navigate to InvoiceFragment
                                                        Invoice invoice = new Invoice();
                                                        invoice.setInvoiceId(invoiceId);
                                                        invoice.setSessionId(session.getSessionId());
                                                        invoice.setTherapistId(session.getTherapistId());
                                                        invoice.setTherapistName(therapistName);
                                                        invoice.setPatientId(currentUserId);
                                                        invoice.setPatientName(patientName);
                                                        invoice.setSessionDate(session.getDate());
                                                        invoice.setAmount(session.getPrice());
                                                        invoice.setStatus("completed");

                                                        getParentFragmentManager().beginTransaction()
                                                                .replace(R.id.fragment_container,
                                                                        InvoiceFragment.newInstance(invoiceId))
                                                                .addToBackStack(null)
                                                                .commit();
                                                    })
                                                    .addOnFailureListener(e -> Log.e("INVOICE_ERROR", "Failed to create invoice", e));
                                        })
                                        .addOnFailureListener(e -> Log.e("SESSION_UPDATE_ERROR", "Failed to update session", e));
                            })
                            .addOnFailureListener(e -> Log.e("THERAPIST_FETCH_ERROR", "Failed to fetch therapist info", e));
                })
                .addOnFailureListener(e -> Log.e("PATIENT_FETCH_ERROR", "Failed to fetch patient info", e));
    }
}