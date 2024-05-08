package com.app.partyshopping;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ComplaintAdapter extends RecyclerView.Adapter<ComplaintAdapter.ComplaintViewHolder> {

    private List<Complaint> complaintList;
    private OnItemClickListener listener;
    private SharedPreferences sharedPreferences;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public ComplaintAdapter(List<Complaint> complaintList, Context context) {
        this.complaintList = complaintList;
        this.sharedPreferences = context.getSharedPreferences("complaint_prefs", Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public ComplaintViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.complaint_item, parent, false);
        return new ComplaintViewHolder(itemView);
    }
    public void updateResponseReceived(String userEmail) {
        for (Complaint complaint : complaintList) {
            if (complaint.getUserEmail().equals(userEmail)) {
                complaint.setResponseReceived(true);
                notifyDataSetChanged();
                break;
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ComplaintViewHolder holder, int position) {
        Complaint complaint = complaintList.get(position);
        holder.bind(complaint);

        // Check if response is received and update UI accordingly
        boolean responseReceived = sharedPreferences.getBoolean(complaint.getUserEmail(), false);
        if (responseReceived) {
            holder.tickIcon.setVisibility(View.VISIBLE);
        } else {
            holder.tickIcon.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return complaintList.size();
    }


    public class ComplaintViewHolder extends RecyclerView.ViewHolder {

        private TextView usernameTextView;
        private TextView emailTextView;
        private ImageView tickIcon;

        public ComplaintViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.username_text);
            emailTextView = itemView.findViewById(R.id.email_text);
            tickIcon = itemView.findViewById(R.id.tick_icon);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }

        public void bind(Complaint complaint) {
            usernameTextView.setText(complaint.getUsername());
            emailTextView.setText(complaint.getUserEmail());
        }
    }
}

