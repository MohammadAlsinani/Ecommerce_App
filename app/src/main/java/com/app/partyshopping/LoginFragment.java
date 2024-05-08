package com.app.partyshopping;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class LoginFragment extends Fragment {

    private EditText emailEditText, passwordEditText;
    private Button loginButton, showPasswordButton, forgetPasswordButton;
    private ProgressBar progressBar;
    private boolean isPasswordVisible = false;

    private FirebaseAuth mAuth;
    private SharedPreferences sharedPref;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        mAuth = FirebaseAuth.getInstance();
        sharedPref = requireActivity().getSharedPreferences("admin_login_status", Context.MODE_PRIVATE);

        emailEditText = view.findViewById(R.id.EmailEditText);
        passwordEditText = view.findViewById(R.id.PasswordEditText);
        progressBar = view.findViewById(R.id.progressBar);

        loginButton = view.findViewById(R.id.login);
        showPasswordButton = view.findViewById(R.id.btn_show_password);
        forgetPasswordButton = view.findViewById(R.id.btn_forget_password);


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        showPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility();
            }
        });
        forgetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showForgetPasswordDialog();
            }
        });

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is already logged in, navigate to HomeActivity
            startActivity(new Intent(getActivity(), HomeActivity.class));
            getActivity().finish(); // Finish the current activity
        }

        // Check if admin is already authenticated and logged in
        if (sharedPref.getBoolean("isAdminLoggedIn", false)) {
            // Admin is already logged in, navigate to AdminActivity
            startActivity(new Intent(getActivity(), AdminActivity.class));
            getActivity().finish(); // Finish the current activity
        }

        return view;
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Check if email and password fields are empty
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getActivity(), "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE); // Show progress bar

        // Check if the email and password match the admin credentials
        if (email.equals("Admin@gmail.com") && password.equals("admin@123")) {
            // Save admin login status
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("isAdminLoggedIn", true);
            editor.apply();

            // Navigate to AdminActivity
            startActivity(new Intent(getActivity(), AdminActivity.class));
            getActivity().finish(); // Finish the current activity
            return;
        }

        // If not admin credentials, proceed with Firebase authentication
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE); // Hide progress bar
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            // Handle successful login
                            Toast.makeText(getActivity(), "Login successful.", Toast.LENGTH_SHORT).show();
                            // Navigate to HomeActivity
                            startActivity(new Intent(getActivity(), HomeActivity.class));
                            getActivity().finish(); // Finish the current activity
                        } else {
                            // If sign in fails, display a message to the user.
                            if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                                // User not found or disabled
                                Toast.makeText(getActivity(), "Invalid email or password.", Toast.LENGTH_SHORT).show();
                            } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // Invalid password
                                Toast.makeText(getActivity(), "Invalid email or password.", Toast.LENGTH_SHORT).show();
                            } else {
                                // Other errors
                                Toast.makeText(getActivity(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
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

    private void showForgetPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Reset Password");
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_reset_password, null);
        final TextInputLayout textInputLayoutEmail = view.findViewById(R.id.textInputLayoutEmail);
        final TextInputEditText emailEditText = view.findViewById(R.id.editTextEmail);
        builder.setView(view);

        builder.setPositiveButton("Reset", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String email = emailEditText.getText().toString().trim();
                if (!TextUtils.isEmpty(email)) {
                    resetPassword(email);
                } else {
                    textInputLayoutEmail.setError("Please enter your email.");
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void resetPassword(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(requireContext(), "Password reset email sent.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Failed to send password reset email.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}
