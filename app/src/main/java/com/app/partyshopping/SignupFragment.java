package com.app.partyshopping;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static android.app.Activity.RESULT_OK;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignupFragment extends Fragment {

    private static final int PERMISSION_REQUEST_CODE = 200;
    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText usernameEditText, emailEditText, passwordEditText;
    private Button signupButton, showPasswordButton;
    private ProgressBar progressBar;
    private boolean isPasswordVisible = false;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private CircleImageView circleImageView;
    private Uri imageUri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        usernameEditText = view.findViewById(R.id.edit_username);
        emailEditText = view.findViewById(R.id.edit_email_signup);
        passwordEditText = view.findViewById(R.id.edit_password_signup);
        progressBar = view.findViewById(R.id.progressBar);
        circleImageView = view.findViewById(R.id.circle_image);

        signupButton = view.findViewById(R.id.signup);
        showPasswordButton = view.findViewById(R.id.btn_show_password_signup);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpUser();
            }
        });

        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        showPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility();
            }
        });

        return view;
    }


    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
                circleImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void signUpUser() {
        final String email = emailEditText.getText().toString().trim();
        final String password = passwordEditText.getText().toString().trim();
        final String username = usernameEditText.getText().toString().trim();

        // Check if any field is empty
        if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
            Toast.makeText(getActivity(), "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Proceed with signup process
        progressBar.setVisibility(View.VISIBLE); // Show progress bar

        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("profile_images");

        // Check if imageUri is null, then use placeholder image from drawable
        if (imageUri == null) {
            // Get placeholder image from drawable
            Drawable drawable = getResources().getDrawable(R.drawable.your_image);
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();

            // Convert bitmap to Uri
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] data = baos.toByteArray();
            imageUri = Uri.fromFile(new File(getActivity().getCacheDir(), "placeholder_image"));
            try {
                OutputStream outputStream = new FileOutputStream(new File(imageUri.getPath()));
                outputStream.write(data);
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        final StorageReference imageRef = storageRef.child(email + "_profile_image");

        UploadTask uploadTask = imageRef.putFile(imageUri);
        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String imageUrl = uri.toString();
                            registerUserWithImage(email, password, username, imageUrl);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getActivity(), "Failed to get image URL", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(getActivity(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void registerUserWithImage(final String email, String password, final String username, final String imageUrl) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE); // Hide progress bar
                        if (task.isSuccessful()) {
                            // Sign up success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(username)
                                        .setPhotoUri(imageUri)
                                        .build();
                                user.updateProfile(profileUpdates);
                                saveUserDataToDatabase(user.getUid(), username, email, imageUrl);
                            }
                            startActivity(new Intent(getActivity(), MainActivity.class));
                            getActivity().finish();
                            Toast.makeText(getActivity(), "Signup successful.", Toast.LENGTH_SHORT).show();
                        } else {
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                // Email already exists
                                Toast.makeText(getActivity(), "Email already exists.", Toast.LENGTH_SHORT).show();
                            } else {
                                // Some other error occurred
                                Toast.makeText(getActivity(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void saveUserDataToDatabase(String userId, String username, String email, String imageUrl) {
        User userData = new User(username, email, imageUrl);
        usersRef.child(userId).setValue(userData);
    }


    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            passwordEditText.setTransformationMethod(new PasswordTransformationMethod());
            showPasswordButton.setText("Show Password");
        } else {
            passwordEditText.setTransformationMethod(null);
            showPasswordButton.setText("Hide Password");
        }
        isPasswordVisible = !isPasswordVisible;
    }
}
