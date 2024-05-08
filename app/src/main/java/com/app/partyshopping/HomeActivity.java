package com.app.partyshopping;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FrameLayout fragmentContainer;

    private HomeFragment homeFragment;
    private CartFragment cartFragment;
    private OrdersFragment ordersFragment;
    private ProfileFragment profileFragment;
    private HistoryFragment historyFragment;

    private TextView usernameTextView;
    private CircleImageView profileImageView;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fragmentContainer = findViewById(R.id.fragment_container);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        // Initialize fragments
        homeFragment = new HomeFragment();
        cartFragment = new CartFragment();
        ordersFragment = new OrdersFragment();
        profileFragment = new ProfileFragment();
        historyFragment = new HistoryFragment();


        // Set HomeFragment as default fragment
        setCurrentFragment(homeFragment);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    if (!isNetworkAvailable()) {
                        Toast.makeText(HomeActivity.this, "Connect Internet", Toast.LENGTH_SHORT).show();
                        return false; // Prevent navigation when there is no internet
                    }
                if (item.getItemId() == R.id.menu_home) {
                    setCurrentFragment(homeFragment);
                    return true;
                } else if (item.getItemId() == R.id.menu_cart) {
                    setCurrentFragment(cartFragment);
                    return true;
                } else if (item.getItemId() == R.id.menu_orders) {
                    setCurrentFragment(ordersFragment);
                    return true;
                } else if (item.getItemId() == R.id.menu_profile) {
                    setCurrentFragment(profileFragment);
                    return true;
                } else if (item.getItemId() == R.id.history) {
                    setCurrentFragment(historyFragment);
                    return true;
                }
                return false;
            }
        });

        // Fetch and display username and profile image
        usernameTextView = findViewById(R.id.username);
        profileImageView = findViewById(R.id.profile);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            String username = user.getUsername();
                            String imageUrl = user.getImageUrl();

                            // Set username
                            usernameTextView.setText(username);

                            // Load profile image using Picasso library into CircleImageView
                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                Picasso.get().load(imageUrl).into(profileImageView);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle database error
                }
            });
        }
    }

    private void setCurrentFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }
}
