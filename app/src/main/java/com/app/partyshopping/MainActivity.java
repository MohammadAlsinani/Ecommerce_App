package com.app.partyshopping;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private Button btnLogin;
    private Button btnSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnLogin = findViewById(R.id.btn_nav_login);
        btnSignup = findViewById(R.id.btn_nav_signup);

        // Set initial button colors
        btnLogin.setSelected(true);
        btnSignup.setSelected(false);

        // Load the Login fragment by default
        loadFragment(new LoginFragment());

        // Handle navigation to Signup fragment with animation
        btnSignup.setOnClickListener(v -> {
            btnLogin.setSelected(false);
            btnSignup.setSelected(true);
            loadFragmentWithAnimation(new SignupFragment());
        });

        // Handle navigation to Login fragment with animation
        btnLogin.setOnClickListener(v -> {
            btnLogin.setSelected(true);
            btnSignup.setSelected(false);
            loadFragmentWithAnimation(new LoginFragment());
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    private void loadFragmentWithAnimation(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
}
