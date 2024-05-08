package com.app.partyshopping;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ComplaintActivity extends AppCompatActivity {

    private ComplaintAdapter adapter;
    private List<Complaint> complaintList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference complaintsRef = db.collection("Complaints");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint);

        setUpRecyclerView();
        loadComplaints();
    }

    private void setUpRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ComplaintAdapter(complaintList, this);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new ComplaintAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Complaint complaint = complaintList.get(position);
                if (complaint != null) {
                    showComplaintPopup(complaint.getComplaint(), complaint.getUserEmail());
                }
            }
        });
    }

    private void loadComplaints() {
        complaintsRef.orderBy("username", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            Complaint complaint = documentSnapshot.toObject(Complaint.class);
                            if (complaint != null) {
                                complaintList.add(complaint);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ComplaintActivity.this, "Failed to load complaints: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showComplaintPopup(String complaintMessage, String userEmail) {
        View popupView = getLayoutInflater().inflate(R.layout.popup_complaint, null);

        TextView complaintTextView = popupView.findViewById(R.id.complaint_text);
        complaintTextView.setText(complaintMessage);

        EditText responseEditText = popupView.findViewById(R.id.response_edit_text);
        Button sendButton = popupView.findViewById(R.id.send_button);

        // Declare the dialog variable as final and initialize it to null
        final androidx.appcompat.app.AlertDialog dialog;

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setView(popupView);

        dialog = builder.create(); // Assign the created dialog to the variable

        // Handle Send button click
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String response = responseEditText.getText().toString().trim();
                if (!response.isEmpty()) {
                    // Store the response under ComplaintsResponse with userEmail as the key
                    storeResponse(userEmail, complaintMessage, response);
                    // Dismiss the popup if dialog is not null
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                } else {
                    Toast.makeText(ComplaintActivity.this, "Please enter a response", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.show();
    }

    private void storeResponse(String userEmail, String userMessage, String response) {
        // Store response under ComplaintsResponse with userEmail as the key
        db.collection("ComplaintsResponse")
                .document(userEmail)
                .set(new ComplaintResponse(userMessage, response))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Update SharedPreferences
                        SharedPreferences.Editor editor = getSharedPreferences("complaint_prefs", Context.MODE_PRIVATE).edit();
                        editor.putBoolean(userEmail, true); // Set the corresponding key to true
                        editor.apply();
                        adapter.updateResponseReceived(userEmail);
                        Toast.makeText(ComplaintActivity.this, "Response stored successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ComplaintActivity.this, "Failed to store response: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
