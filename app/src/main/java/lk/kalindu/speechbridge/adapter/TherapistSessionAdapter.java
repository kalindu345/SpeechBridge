package lk.kalindu.speechbridge.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.text.format.DateFormat;
import android.graphics.Color;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.List;

import lk.kalindu.speechbridge.R;
import lk.kalindu.speechbridge.model.Session;

public class TherapistSessionAdapter extends RecyclerView.Adapter<TherapistSessionAdapter.ViewHolder> {

    private List<Session> sessionList;
    private FirebaseFirestore db;

    public TherapistSessionAdapter(List<Session> sessionList) {
        this.sessionList = sessionList;
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mysession, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Session session = sessionList.get(position);

        // show the patient's name if It is booked
        if ("booked".equalsIgnoreCase(session.getStatus()) && session.getPatientId() != null && !session.getPatientId().isEmpty()) {

            db.collection("users")
                    .document(session.getPatientId())
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String patientName = doc.getString("name");
                            holder.txtTherapistName.setText("👤 " + patientName);
                        }
                    });

        } else {
            holder.txtTherapistName.setText("No patient yet");
        }

        // Date
        if (session.getDate() != null) {
            Date date = session.getDate().toDate();
            holder.txtDate.setText(DateFormat.format("dd MMM yyyy", date));
        }

        holder.txtTime.setText(session.getStartTime());
        holder.txtPrice.setText("Rs. " + (int) session.getPrice());

        // Status
        String status = session.getStatus() != null ? session.getStatus() : "unknown";
        holder.txtStatus.setText(status);

        if ("available".equalsIgnoreCase(status)) {
            holder.txtStatus.setTextColor(Color.parseColor("#4CAF50"));
        } else if ("booked".equalsIgnoreCase(status)) {
            holder.txtStatus.setTextColor(Color.parseColor("#FF9800"));
        } else {
            holder.txtStatus.setTextColor(Color.GRAY);
        }

        // Type
        String type = session.getType() != null ? session.getType() : "";

        if ("physical".equalsIgnoreCase(type)) {
            holder.txtLocation.setVisibility(View.VISIBLE);
            holder.txtLocation.setText("📍 " + session.getLocation());
            holder.txtLink.setVisibility(View.GONE);

        } else if ("online".equalsIgnoreCase(type)) {
            holder.txtLink.setVisibility(View.VISIBLE);
            holder.txtLink.setText("🌐 Join Meeting");
            holder.txtLocation.setVisibility(View.GONE);
        } else {
            holder.txtLocation.setVisibility(View.GONE);
            holder.txtLink.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return sessionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtTherapistName, txtDate, txtTime, txtLocation, txtLink, txtPrice, txtStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtTherapistName = itemView.findViewById(R.id.txtTherapistName);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtLocation = itemView.findViewById(R.id.txtLocation);
            txtLink = itemView.findViewById(R.id.txtLink);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            txtStatus = itemView.findViewById(R.id.txtStatus);
        }
    }
}