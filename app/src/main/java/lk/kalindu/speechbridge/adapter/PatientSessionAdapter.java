package lk.kalindu.speechbridge.adapter;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import lk.kalindu.speechbridge.databinding.ItemPatientSessionCardBinding;
import lk.kalindu.speechbridge.model.Session;

public class PatientSessionAdapter extends RecyclerView.Adapter<PatientSessionAdapter.ViewHolder> {

    private final List<Session> sessions;
    private final OnSessionClickListener listener;

    public interface OnSessionClickListener {
        void onLocationClick(Session session);
        void onOnlineClick(Session session);
    }

    public PatientSessionAdapter(List<Session> sessions, OnSessionClickListener listener) {
        this.sessions = sessions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPatientSessionCardBinding binding = ItemPatientSessionCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Session session = sessions.get(position);

        // Therapist Name
        holder.binding.txtTherapistName.setText(session.getTherapistName() != null ? session.getTherapistName() : "N/A");

        // Date
        if (session.getDate() != null) {
            Date date = session.getDate().toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            holder.binding.txtDate.setText(sdf.format(date));
        } else {
            holder.binding.txtDate.setText("N/A");
        }

        // Time
        holder.binding.txtTime.setText(session.getStartTime() != null ? session.getStartTime() : "N/A");

        // Price
        holder.binding.txtPrice.setText("Rs. " + session.getPrice());

        // Status
        holder.binding.txtStatus.setText(session.getStatus() != null ? session.getStatus() : "N/A");

        // Session Type
        String type = session.getType() != null ? session.getType() : "Unknown";
        holder.binding.txtType.setText(type.equalsIgnoreCase("physical") ? "Physical" : "Online");

        // Handle visibility of meelink link and the location
        if ("physical".equalsIgnoreCase(type)) {
            holder.binding.txtLocation.setVisibility(View.VISIBLE);
            holder.binding.txtLink.setVisibility(View.GONE);
            holder.binding.txtLocation.setText(session.getLocation() != null ? "📍 " + session.getLocation() : "📍 N/A");

            holder.binding.txtLocation.setOnClickListener(v -> {
                if (listener != null) listener.onLocationClick(session);
            });

        } else if ("online".equalsIgnoreCase(type)) {
            holder.binding.txtLink.setVisibility(View.VISIBLE);
            holder.binding.txtLocation.setVisibility(View.GONE);
            holder.binding.txtLink.setText("Join Meeting");

            holder.binding.txtLink.setOnClickListener(v -> {
                if (listener != null) listener.onOnlineClick(session);
            });

        } else {
            holder.binding.txtLink.setVisibility(View.GONE);
            holder.binding.txtLocation.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemPatientSessionCardBinding binding;

        public ViewHolder(@NonNull ItemPatientSessionCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}