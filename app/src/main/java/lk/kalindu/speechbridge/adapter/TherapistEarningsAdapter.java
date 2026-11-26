package lk.kalindu.speechbridge.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.text.format.DateFormat;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Date;
import java.util.List;

import lk.kalindu.speechbridge.R;
import lk.kalindu.speechbridge.model.Session;

public class TherapistEarningsAdapter extends RecyclerView.Adapter<TherapistEarningsAdapter.ViewHolder> {

    private List<Session> sessionList;

    public TherapistEarningsAdapter(List<Session> sessionList) {
        this.sessionList = sessionList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_earning, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Session session = sessionList.get(position);


        holder.txtSessionTitle.setText("Session");


        if (session.getDate() != null) {
            Date date = session.getDate().toDate();
            holder.txtDate.setText(DateFormat.format("dd MMM yyyy", date));
        }


        holder.txtPrice.setText("Rs. " + (int) session.getPrice());
    }

    @Override
    public int getItemCount() {
        return sessionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtSessionTitle, txtDate, txtPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtSessionTitle = itemView.findViewById(R.id.txtSessionTitle);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtPrice = itemView.findViewById(R.id.txtPrice);
        }
    }
}
