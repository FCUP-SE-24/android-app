package pt.up.fc.se.petfeeder;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

import java.util.Objects;

import pt.up.fc.se.petfeeder.databinding.ActivityMainBinding;
import pt.up.fc.se.petfeeder.databinding.ActivityUserBinding;

public class UserActivity extends AppCompatActivity {

    ActivityUserBinding binding;
    ImageView menu_user_show;
    @SuppressLint("RestrictedApi")
    MenuBuilder menuBuilder;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    private FirebaseAuth.AuthStateListener authStateListener;
    public int selectedBowl;

    @SuppressLint({"NonConstantResourceId", "RestrictedApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new BowlsFragment());

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        if(user == null) {
            Intent I = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(I);
            finish();
        }

        menu_user_show = findViewById(R.id.image_user_menu);
        menuBuilder = new MenuBuilder(this);
        MenuInflater menuInflater = new MenuInflater(this);
        menuInflater.inflate(R.menu.user_menu, menuBuilder);
        String userEmail = Objects.requireNonNull(user.getEmail()).substring(0, user.getEmail().indexOf("@"));
        menuBuilder.getItem(0).setTitle(userEmail);
        menu_user_show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MenuPopupHelper menuPopupHelper = new MenuPopupHelper(UserActivity.this, menuBuilder, v);
                menuPopupHelper.setForceShowIcon(true);

                menuBuilder.getItem(0).setEnabled(false);
                menuBuilder.setCallback(new MenuBuilder.Callback() {
                    @Override
                    public boolean onMenuItemSelected(@NonNull MenuBuilder menu, @NonNull MenuItem item) {
                        if(item.getItemId() == R.id.menu_logout) {
                            FirebaseAuth.getInstance().signOut();
                            Intent I = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(I);
                            finish();
                            return true;
                        }
                        return false;
                    }

                    @Override
                    public void onMenuModeChange(@NonNull MenuBuilder menu) {
                        //empty
                    }
                });
                menuPopupHelper.show();
            }
        });


        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if(id == R.id.bottom_navbar_menu_bowls) {
                replaceFragment(new BowlsFragment());
            } else if(id == R.id.bottom_navbar_menu_feeding) {
                replaceFragment(new FeedingFragment());
            }
            return true;
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
//        firebaseAuth.addAuthStateListener(authStateListener);
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        if(user == null) {
            Intent I = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(I);
            finish();
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}