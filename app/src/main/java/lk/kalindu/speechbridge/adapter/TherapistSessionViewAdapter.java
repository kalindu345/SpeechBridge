package lk.kalindu.speechbridge.adapter;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import lk.kalindu.speechbridge.R;
import lk.kalindu.speechbridge.model.Session;

public class TherapistSessionViewAdapter extends RecyclerView.Adapter<TherapistSessionViewAdapter.ViewHolder> {

    private final List<Session> sessions;
    private final OnSessionClickListener listener;

    public TherapistSessionViewAdapter(List<Session> sessions, OnSessionClickListener listener) {
        this.sessions = sessions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_session_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Session session = sessions.get(position);
        Context context = holder.itemView.getContext();

        // Date
        Date date = null;
        try {
            if (session.getDate() != null) {
                date = session.getDate().toDate();
            }
        } catch (Exception ignored) {}

        if (date != null) {
            SimpleDateFormat sdfDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            SimpleDateFormat sdfTime = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            holder.sessionDate.setText(sdfDate.format(date));
            holder.sessionStartTime.setText(sdfTime.format(date));
        } else {
            holder.sessionDate.setText("No Date");
            holder.sessionStartTime.setText("N/A");
        }

        // Price
        holder.sessionPrice.setText("Rs. " + session.getPrice());

        // Status
        holder.sessionStatus.setText(session.getStatus() != null ? session.getStatus() : "Unknown");

        // Session Type
        if ("online".equalsIgnoreCase(session.getType())) {

            holder.sessionLink.setVisibility(View.VISIBLE);
            holder.sessionLocation.setVisibility(View.GONE);

            holder.sessionLink.setText("Join Meeting");

            holder.sessionLink.setOnClickListener(v -> {
                if (session.getMeetingLink() != null && !session.getMeetingLink().isEmpty()) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(session.getMeetingLink()));
                        context.startActivity(intent); // Zoom opens
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(context, "No app found", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        } else {

            holder.sessionLocation.setVisibility(View.VISIBLE);
            holder.sessionLink.setVisibility(View.GONE);

            holder.sessionLocation.setText(session.getLocation() != null ? session.getLocation() : "No Location");

            holder.sessionLocation.setOnClickListener(v -> {
                if (listener != null) listener.OnLocationClick(session);
            });
        }

        // session Booking Button
        holder.sessionBookingBtn.setOnClickListener(v -> {
            if (listener != null) listener.OnSessionClick(session);
        });
    }

    @Override
    public int getItemCount() {
        return sessions != null ? sessions.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView sessionDate, sessionPrice, sessionStartTime;
        TextView sessionLocation, sessionLink, sessionStatus;
        MaterialButton sessionBookingBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            sessionDate = itemView.findViewById(R.id.sessionDate);
            sessionPrice = itemView.findViewById(R.id.sessionPrice);
            sessionStartTime = itemView.findViewById(R.id.sessionStartTime);
            sessionLocation = itemView.findViewById(R.id.sessionLocation);
            sessionLink = itemView.findViewById(R.id.sessionLink);
            sessionStatus = itemView.findViewById(R.id.sessionStatus);
            sessionBookingBtn = itemView.findViewById(R.id.sessionBookingBtn);
        }
    }

    public interface OnSessionClickListener {
        void OnSessionClick(Session session);
        void OnLocationClick(Session session);
    }
}