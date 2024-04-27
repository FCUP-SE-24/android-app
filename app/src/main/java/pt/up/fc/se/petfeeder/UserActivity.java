package pt.up.fc.se.petfeeder;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import pt.up.fc.se.petfeeder.databinding.ActivityMainBinding;
import pt.up.fc.se.petfeeder.databinding.ActivityUserBinding;

public class UserActivity extends AppCompatActivity {

    Button btnAddBowl;
    LinearLayout layout;
    ImageView menu_user_show;
    @SuppressLint("RestrictedApi")
    MenuBuilder menuBuilder;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    private FirebaseAuth.AuthStateListener authStateListener;

    //Temporary
    public List<String> bowls;

    @SuppressLint({"NonConstantResourceId", "RestrictedApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        bowls = new LinkedList<String>();

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

        btnAddBowl = findViewById(R.id.button_add_bowl_dialog);
        layout = findViewById(R.id.layout_container);

        //TODO: warning of how much bowls are free
        // free bowls = arduinos without name
        // if no more free bowls, disable button and warn
        btnAddBowl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddBowlDialog();
            }
        });

        //TODO: get all bowls from DB
        // for loop that uses the method addCard


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

        OkHttpClient client = new OkHttpClient();
        String url = "http://46.101.71.117:5000/get_bowls_list";

        Request request = new Request.Builder().url(url).build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                assert response.body() != null;
                String responseBody = response.body().string();
                System.out.println(responseBody);
                Log.d("RESPONSE RECEIVED", responseBody);
            }

            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                System.out.println("FAILURE");
                e.printStackTrace();
            }
        });
    }

    void showAddBowlDialog() {
        final Dialog dialog = new Dialog(UserActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        Objects.requireNonNull(dialog.getWindow()).getDecorView().setBackgroundColor(Color.TRANSPARENT);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setContentView(R.layout.dialog_add_bowl);

        //TODO: list with existent bowls names
        // petName is unique
        // or search in db if that petName already exists

        final EditText txtAddPetName = dialog.findViewById(R.id.edittext_dialog_add_pet_name);
        final EditText txtAddDailyGoal = dialog.findViewById(R.id.edittext_dialog_add_daily_goal);
        Button btnAdd = dialog.findViewById(R.id.button_dialog_add_bowl);

        btnAdd.setOnClickListener((v) -> {
            String petName = txtAddPetName.getText().toString();
            String dailyGoal = txtAddDailyGoal.getText().toString();

            addCard(petName, dailyGoal);

            //TODO: send this info to database
            //TODO: get bowl weight and 'reset' it

            dialog.dismiss();
        });

        dialog.show();
    }

    private void addCard(String petName, String dailyGoal) {
        View cardView = getLayoutInflater().inflate(R.layout.card_bowl, null);

        TextView txtBowlName = cardView.findViewById(R.id.text_bowl_name);
        txtBowlName.setText(petName);

        TextView txtCurrentDosage = cardView.findViewById(R.id.text_bowl_current_dosage);
        //TODO: get value from DB?
        txtCurrentDosage.setText("0");

        TextView txtDailyGoal = cardView.findViewById(R.id.text_daily_goal);
        txtDailyGoal.setText(dailyGoal);

        Button btnSelect = cardView.findViewById(R.id.button_select_bowl);

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent I = new Intent(UserActivity.this, FeedingActivity.class);
                I.putExtra("bowlName", petName);
                startActivity(I);
            }
        });

        layout.addView(cardView);
    }
}