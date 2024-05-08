package com.app.partyshopping;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    private AdminHomeFragment homeFragment;
    private AdminOrdersFragment ordersFragment;
    private AdminProfileFragment profileFragment;
    private AdminAllItemsFragment adminAllItemsFragment;
    private AdminHistoryFragment adminHistoryFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Initialize fragments
        homeFragment = new AdminHomeFragment();
        ordersFragment = new AdminOrdersFragment();
        profileFragment = new AdminProfileFragment();
        adminAllItemsFragment = new AdminAllItemsFragment();
        adminHistoryFragment = new AdminHistoryFragment()
        ;

        // Set HomeFragment as default fragment
        setCurrentFragment(homeFragment);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.menu_home) {
                    setCurrentFragment(homeFragment);
                    return true;
                } else if (itemId == R.id.menu_orders) {
                    setCurrentFragment(ordersFragment);
                    return true;
                }
                else if (itemId == R.id.menu_profile) {
                    setCurrentFragment(profileFragment);
                    return true;
                }
                else if (itemId == R.id.all) {
                    setCurrentFragment(adminAllItemsFragment);
                    return true;
                }
                else if (itemId == R.id.history) {
                    setCurrentFragment(adminHistoryFragment);
                    return true;
                }
                return false;
            }
        });
    }

    private void setCurrentFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
    }
}
