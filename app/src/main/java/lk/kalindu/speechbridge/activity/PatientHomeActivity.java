package lk.kalindu.speechbridge.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import lk.kalindu.speechbridge.R;
import lk.kalindu.speechbridge.databinding.ActivityMainBinding;
import lk.kalindu.speechbridge.databinding.ActivityPatientHomeBinding;
import lk.kalindu.speechbridge.databinding.SideNavHeaderBinding;
import lk.kalindu.speechbridge.fragment.PatientComplaintsFragment;
import lk.kalindu.speechbridge.fragment.PatientHomeFragment;
import lk.kalindu.speechbridge.fragment.PatientProfileFragment;
import lk.kalindu.speechbridge.fragment.PatientSessionViewFragment;
import lk.kalindu.speechbridge.fragment.PatientTherapistsViewFragment;
import lk.kalindu.speechbridge.model.Patient;

public class PatientHomeActivity extends AppCompatActivity {

    private ActivityPatientHomeBinding binding;
    private SideNavHeaderBinding sideNavHeaderBinding;
    private DrawerLayout drawerLayout;
    private MaterialToolbar toolbar;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPatientHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        View headerView = binding.sideNavigationView.getHeaderView(0);
        sideNavHeaderBinding = SideNavHeaderBinding.bind(headerView);

        drawerLayout = binding.drawerLayout;
        toolbar = binding.toolbar;
        navigationView = binding.sideNavigationView;
        bottomNavigationView = binding.bottomNavigationView;

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(toggle);

        toggle.syncState();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    finish();
                }
            }
        });


        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        /// load Patient details
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            firebaseFirestore.collection("users").document(currentUser.getUid()).get().
                    addOnSuccessListener(ds -> {
                        if (ds.exists()) {

                            Patient patient = ds.toObject(Patient.class);
                            sideNavHeaderBinding.headerUserName.setText(patient.getName());
                            sideNavHeaderBinding.headerUserEmail.setText(patient.getEmail());

                            if (patient.getProfilePicUrl() != null && !patient.getProfilePicUrl().isEmpty()) {

                                Glide.with(this)
                                        .load(patient.getProfilePicUrl())
                                        .placeholder(R.drawable.profile) // default image
                                        .error(R.drawable.profile) // if failed
                                        .into(sideNavHeaderBinding.headerProfilePic);

                            }
                        } else {
                            Log.e("Firestore", "Document does not exist");
                        }
                    }).addOnFailureListener(e -> {
                        Log.e("Firestore", "Error: " + e.getMessage());
                    });
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new PatientHomeFragment())
                .commit();

        bottomNavigationView.setOnItemSelectedListener(item -> {

            if (item.getItemId() == R.id.bottom_nav_home) {
                loadFragment(new PatientHomeFragment());
                return true;

            } else if (item.getItemId() == R.id.bottom_nav_therapists) {
                loadFragment(new PatientTherapistsViewFragment());
                return true;

            } else if (item.getItemId() == R.id.bottom_nav_mySessions) {
                loadFragment(new PatientSessionViewFragment());
                return true;

            } else if (item.getItemId() == R.id.bottom_nav_profile) {
                loadFragment(new PatientProfileFragment());
                return true;
            }

            return false;
        });

        navigationView.setNavigationItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.side_nav_therapists) {
                loadFragment(new PatientTherapistsViewFragment());
                bottomNavigationView.setSelectedItemId(R.id.bottom_nav_therapists);

            } else if (id == R.id.side_nav_mySessions) {
                loadFragment(new PatientSessionViewFragment());
                bottomNavigationView.setSelectedItemId(R.id.bottom_nav_mySessions);

            } else if (id == R.id.side_nav_complaints) {
                loadFragment(new PatientComplaintsFragment());

                bottomNavigationView.getMenu().setGroupCheckable(0, true, false);
                for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
                    bottomNavigationView.getMenu().getItem(i).setChecked(false);
                }
            } else if (id == R.id.side_nav_logout) {
                firebaseAuth.signOut();

                Intent intent = new Intent(PatientHomeActivity.this, RoleSelectionActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

                finish();
            }


            drawerLayout.closeDrawer(GravityCompat.START);

            return true;
        });

    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }


}