package com.app.partyshopping;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminProfileFragment extends Fragment {

    private TextView totalOrdersTextView;
    private TextView totalFlowersTextView;
    private TextView totalCakesTextView;
    private TextView totalStagesTextView;
    private Button logoutButton;

    private DatabaseReference ordersRef;
    private DatabaseReference itemsRef;

    private SharedPreferences sharedPref;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_profile, container, false);

        totalOrdersTextView = view.findViewById(R.id.total_orders_text_view);
        totalFlowersTextView = view.findViewById(R.id.total_flowers_text_view);
        totalCakesTextView = view.findViewById(R.id.total_cakes_text_view);
        totalStagesTextView = view.findViewById(R.id.total_stages_text_view);
        logoutButton = view.findViewById(R.id.logout_button);
        sharedPref = requireActivity().getSharedPreferences("admin_login_status", Context.MODE_PRIVATE);

        CardView cardView = view.findViewById(R.id.profile_card_view);

        // Firebase references
        ordersRef = FirebaseDatabase.getInstance().getReference().child("Orders");
        itemsRef = FirebaseDatabase.getInstance().getReference().child("Items");

        // Fetch total orders
        fetchTotalOrders();

        // Fetch and display total items in each category
        fetchTotalItems("Apple Products", totalFlowersTextView);
        fetchTotalItems("Fashion", totalCakesTextView);
        fetchTotalItems("Household Items", totalStagesTextView);

        logoutButton.setOnClickListener(v -> {
            logoutAdmin();
        });
        TextView complaintsButton = view.findViewById(R.id.complains);

        // Set onClickListener for the button
        complaintsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start ComplaintActivity
                startActivity(new Intent(getActivity(), ComplaintActivity.class));
            }
        });

        return view;
    }

    public void logoutAdmin() {
        // Clear admin login status from SharedPreferences
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("isAdminLoggedIn", false);
        editor.apply();
        // Redirect to the login screen
        startActivity(new Intent(getActivity(), MainActivity.class));
        getActivity().finish(); // Finish the current activity
    }


    private void fetchTotalOrders() {
        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long totalOrders = snapshot.getChildrenCount();
                totalOrdersTextView.setText(String.valueOf(totalOrders));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Failed to fetch orders", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchTotalItems(String category, TextView textView) {
        itemsRef.child(category).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long totalItems = snapshot.getChildrenCount();
                textView.setText(String.valueOf(totalItems));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Failed to fetch items", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
