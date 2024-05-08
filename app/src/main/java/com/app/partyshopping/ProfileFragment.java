package com.app.partyshopping;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private CircleImageView profileImageView;
    private TextView emailTextView, usernameTextView, totalOrdersTextView;
    private DatabaseReference usersRef,ordersRef;
    private Button logoutButton;


    private TextView changePasswordButton,changeprofile;
    private FirebaseAuth mAuth;

    private FirebaseFirestore db;

    private static final String COLLECTION_COMPLAINTS = "Complaints";

    private static final int PICK_IMAGE_REQUEST = 1;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileImageView = view.findViewById(R.id.profile_image);
        emailTextView = view.findViewById(R.id.email_text);
        usernameTextView = view.findViewById(R.id.username_text);
        totalOrdersTextView = view.findViewById(R.id.total_orders_text);
        logoutButton = view.findViewById(R.id.logout);
        changePasswordButton = view.findViewById(R.id.change_password);
        changeprofile = view.findViewById(R.id.changeprofile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        TextView supportTextView = view.findViewById(R.id.support);
        supportTextView.setOnClickListener(v -> showComplaintDialog());
        TextView responce = view.findViewById(R.id.responce);
        responce.setOnClickListener(v -> showComplaintDialogResponce());


        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

            emailTextView.setText(user.getEmail());
            ordersRef = FirebaseDatabase.getInstance().getReference().child("Orders");

            Query query = ordersRef.orderByChild("userEmail").equalTo(user.getEmail());
            query.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                int totalOrders = (int) snapshot.getChildrenCount();
                                                totalOrdersTextView.setText(String.valueOf(totalOrders));
                                            }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Failed to fetch user data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            usersRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String username = snapshot.child("username").getValue(String.class);
                        usernameTextView.setText(username);
                        String imageUrl = snapshot.child("imageUrl").getValue(String.class);
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(getContext()).load(imageUrl).into(profileImageView);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Failed to fetch user data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            logoutButton.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getActivity(), MainActivity.class));
                getActivity().finish();
            });

            changePasswordButton.setOnClickListener(v -> showChangePasswordDialog());

            changeprofile.setOnClickListener(v -> openGallery());
        }

        return view;
    }

    private void showComplaintDialogResponce() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();

            db.collection("ComplaintsResponse")
                    .document(userEmail)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String userMessage = documentSnapshot.getString("userMessage");
                            String adminResponse = documentSnapshot.getString("response");

                            if (userMessage != null && adminResponse != null) {
                                // Display the user's message and admin response in the popup dialog
                                showComplaintPopup(userMessage, adminResponse);
                            } else {
                                Toast.makeText(getContext(), "Data not available", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getContext(), "Data not available", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to fetch data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }
    private void showComplaintPopup(String userMessage, String adminResponse) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.popup_complaint_response, null);

        TextView userMessageTextView = dialogView.findViewById(R.id.user_message_text);
        TextView adminResponseTextView = dialogView.findViewById(R.id.admin_response_text);

        userMessageTextView.setText(userMessage);
        adminResponseTextView.setText(adminResponse);

        builder.setView(dialogView);
        builder.setPositiveButton("Close", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    // Inside onActivityResult method
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK
                && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                // Use Glide to load the selected image into CircleImageView
                Glide.with(this)
                        .load(imageUri)
                        .into(profileImageView);

                // Update profile image in Firebase Storage and Realtime Database
                updateProfileImage(imageUri);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateProfileImage(Uri imageUri) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            StorageReference profileImageRef = FirebaseStorage.getInstance().getReference()
                    .child("profile_images")
                    .child(user.getEmail() + "_profile_image");

            profileImageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Get the download URL for the uploaded image
                        profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            // Update the profile image URL in Firebase Realtime Database
                            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference()
                                    .child("Users")
                                    .child(user.getUid());

                            usersRef.child("imageUrl").setValue(uri.toString())
                                    .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Profile image updated successfully", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update profile image", Toast.LENGTH_SHORT).show());
                        });
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to upload profile image", Toast.LENGTH_SHORT).show());
        }
    }
    private void showChangePasswordDialog() {
        // Create a custom dialog layout
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_change_password, null);

        TextInputLayout currentPasswordLayout = dialogView.findViewById(R.id.current_password_layout);
        TextInputLayout newPasswordLayout = dialogView.findViewById(R.id.new_password_layout);

        EditText currentPasswordEditText = currentPasswordLayout.getEditText();
        EditText newPasswordEditText = newPasswordLayout.getEditText();

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setView(dialogView);

        // Set the title with black text and center horizontally
        TextView title = new TextView(getContext());
        title.setText("Change Password");
        title.setTextSize(20);
        title.setTextColor(Color.BLACK);
        title.setGravity(Gravity.CENTER);
        dialogBuilder.setCustomTitle(title);

        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (currentPasswordEditText != null && newPasswordEditText != null) {
                            String currentPassword = currentPasswordEditText.getText().toString().trim();
                            String newPassword = newPasswordEditText.getText().toString().trim();

                            if (TextUtils.isEmpty(currentPassword) || TextUtils.isEmpty(newPassword)) {
                                Toast.makeText(getContext(), "Please enter both current and new passwords", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            // Get the current user
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if (user != null) {
                                // Re-authenticate the user to change password
                                user.reauthenticate(EmailAuthProvider.getCredential(user.getEmail(), currentPassword))
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                // Change password
                                                user.updatePassword(newPassword)
                                                        .addOnCompleteListener(task1 -> {
                                                            if (task1.isSuccessful()) {
                                                                Toast.makeText(getContext(), "Password updated successfully", Toast.LENGTH_SHORT).show();
                                                                FirebaseAuth.getInstance().signOut(); // Log out after password change
                                                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                                                startActivity(intent);
                                                                getActivity().finish();
                                                            } else {
                                                                Toast.makeText(getContext(), "Failed to update password", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            } else {
                                                Toast.makeText(getContext(), "Authentication failed. Please check your current password", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(getContext(), "Error accessing text input fields", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null);

        AlertDialog alertDialog = dialogBuilder.create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                // Set background color to white
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));

                // Set title text color and alignment
                TextView dialogTitle = alertDialog.findViewById(R.id.title);
                if (dialogTitle != null) {
                    dialogTitle.setTextColor(Color.BLACK);
                    dialogTitle.setGravity(Gravity.CENTER);
                }
            }
        });

        alertDialog.show();
    }

    private void showComplaintDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Explain Your Issue");

        // Set up the input
        final EditText input = new EditText(getContext());
        input.setHint("Enter your issue here...");
        input.setTextColor(Color.BLACK); // Set text color to black
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String complaint = input.getText().toString().trim();
                if (!TextUtils.isEmpty(complaint)) {
                    // Save complaint to Firebase Firestore
                    saveComplaint(complaint);
                } else {
                    Toast.makeText(getContext(), "Please enter your issue", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();

        // Show the dialog
        dialog.show();

        // Set background color to white
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));

        // Set title text color and alignment
        TextView titleTextView = dialog.findViewById(android.R.id.title);
        if (titleTextView != null) {
            titleTextView.setTextColor(Color.BLACK);
            titleTextView.setGravity(Gravity.CENTER);
        }

        // Set hint text color
        input.setHintTextColor(Color.BLACK);
    }

    private void saveComplaint(String complaint) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String username = user.getDisplayName();
            String userEmail = user.getEmail();

            // Create a new document in the "Complaints" collection
            db.collection(COLLECTION_COMPLAINTS)
                    .add(new Complaint(username, userEmail, complaint))
                    .addOnSuccessListener(documentReference -> Toast.makeText(getContext(), "Complaint submitted successfully", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to submit complaint", Toast.LENGTH_SHORT).show());
        }
    }
}
