package pt.up.fc.se.petfeeder;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
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
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;

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
    ServerRequests requests;
    int nAvailableBowls;
    JSONArray bowlsArray;

    @SuppressLint({"NonConstantResourceId", "RestrictedApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user);

        firebaseAuth = FirebaseAuth.getInstance();

        requests = new ServerRequests();

        nAvailableBowls = 0;

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

        btnAddBowl = findViewById(R.id.button_add_bowl_dialog);
        layout = findViewById(R.id.layout_container);

        btnAddBowl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddBowlDialog();
            }
        });

        BlockingQueue<JSONArray> blockingQueue = requests.getBowlsList();
        try {
            bowlsArray = blockingQueue.take();
            for (int i = 0; i < bowlsArray.length(); i++) {
                if(bowlsArray.getString(i).contains("undefined"))
                    nAvailableBowls += 1;
                else addCard(bowlsArray.getString(i));
            }
        } catch (InterruptedException | JSONException e) {
            throw new RuntimeException(e);
        }

        //TODO: warning of how much bowls are free
        // free bowls = arduinos without name
        // if no more free bowls, disable button and warn

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

    void showAddBowlDialog() {
        final Dialog dialog = new Dialog(UserActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        Objects.requireNonNull(dialog.getWindow()).getDecorView().setBackgroundColor(Color.TRANSPARENT);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setContentView(R.layout.dialog_add_bowl);

        final EditText txtAddPetName = dialog.findViewById(R.id.edittext_dialog_add_pet_name);
        final EditText txtAddDailyGoal = dialog.findViewById(R.id.edittext_dialog_add_daily_goal);
        Button btnAdd = dialog.findViewById(R.id.button_dialog_add_bowl);

        btnAdd.setOnClickListener((v) -> {
            String petName = txtAddPetName.getText().toString();
            String dailyGoal = txtAddDailyGoal.getText().toString();

            //TODO: check if this works
            if(petName.isBlank()) {
                txtAddPetName.setError("Please insert a valid name");
                txtAddPetName.requestFocus();
            } else if(dailyGoal.isEmpty()) {
                txtAddDailyGoal.setError("Please insert a valid number");
                txtAddDailyGoal.requestFocus();
            } else {
                String error = "";
                for (int i = 0; i < this.bowlsArray.length(); i++) {
                try {
                    if(bowlsArray.getString(i).equals(petName)) error = "This name it's already in use";
                    if(bowlsArray.getString(i).contains("undefined")) error = "Please insert a valid name: (undefined) is not valid!";
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
              }

                if(error.isEmpty()) {
                    requests.postAddBowl(petName);
                    requests.postDailyGoal(petName, dailyGoal);
                    try {
                        addCard(petName);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    dialog.dismiss();
                } else {
                    txtAddPetName.setError(error);
                    txtAddPetName.requestFocus();
                }
            }

            //TODO: request may execute out of order BIG PROBLEM
            // postAddBowl needs to receive dailyGoal

            //TODO: get bowl weight and 'reset' it
        });

        dialog.show();
    }

    @SuppressLint("SetTextI18n")
    private void addCard(String petName) throws InterruptedException {
        View cardView = getLayoutInflater().inflate(R.layout.card_bowl, null);
        BlockingQueue<Integer> blockingQueue;

        TextView txtBowlName = cardView.findViewById(R.id.text_bowl_name);
        txtBowlName.setText(petName);

        blockingQueue = requests.getFoodAmount(petName);
        TextView txtCurrentDosage = cardView.findViewById(R.id.text_bowl_current_dosage);
        txtCurrentDosage.setText(blockingQueue.take().toString());

        // TODO
        blockingQueue = requests.getDailyGoal(petName);
        TextView txtDailyGoal = cardView.findViewById(R.id.text_daily_goal);
        txtDailyGoal.setText(blockingQueue.take().toString());
        txtDailyGoal.setText("250");

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