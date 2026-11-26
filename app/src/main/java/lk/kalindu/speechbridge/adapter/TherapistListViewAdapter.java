package lk.kalindu.speechbridge.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import lk.kalindu.speechbridge.R;
import lk.kalindu.speechbridge.model.Therapist;

public class TherapistListViewAdapter extends RecyclerView.Adapter<TherapistListViewAdapter.ViewHolder> {
    private List<Therapist> therapists;
    private List<Therapist> therapistsFull; // for filtering part
    private OnTherapistClickListener listener;


    public TherapistListViewAdapter(List<Therapist> therapists, OnTherapistClickListener listener) {
        this.therapists = therapists;
        this.listener = listener;
        this.therapistsFull = new ArrayList<>(therapists);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_therapist_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Therapist therapist = therapists.get(position);
        holder.therapistName.setText(therapist.getName());
        holder.therapistEmail.setText(therapist.getEmail());
        holder.therapistMobile.setText(therapist.getMobile());
        holder.viewDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.OnTherapistClick(therapist);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return therapists.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView therapistName;
        TextView therapistEmail;
        TextView therapistMobile;

        MaterialButton viewDetails;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            therapistName = itemView.findViewById(R.id.therapistName);
            therapistEmail = itemView.findViewById(R.id.therapistEmail);
            therapistMobile = itemView.findViewById(R.id.therapistMobile);
            viewDetails = itemView.findViewById(R.id.viewDetails);

        }


    }

    public interface OnTherapistClickListener {
        void OnTherapistClick(Therapist therapist);
    }

    public void filter(String text) {
        therapists.clear();

        if (text.isEmpty()) {
            therapists.addAll(therapistsFull);
        } else {
            text = text.toLowerCase();

            for (Therapist therapist : therapistsFull) {
                if (therapist.getName().toLowerCase().contains(text)) {
                    therapists.add(therapist);
                }
            }
        }

        notifyDataSetChanged();
    }


}
