package pt.up.fc.se.petfeeder;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;

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
    JSONArray bowlsArray;

    @SuppressLint({"NonConstantResourceId", "RestrictedApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user);

        firebaseAuth = FirebaseAuth.getInstance();

        requests = new ServerRequests();

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
        menu_user_show.setOnClickListener(v -> {
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
        });

        btnAddBowl = findViewById(R.id.button_add_bowl_dialog);
        layout = findViewById(R.id.layout_container);

        if(isNetworkConnected()) {
            btnAddBowl.setOnClickListener(v -> {
                try {
                    showAddBowlDialog();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });

            BlockingQueue<JSONArray> blockingQueue = requests.getBowlsList();
            try {
                bowlsArray = blockingQueue.take();
                for (int i = 0; i < bowlsArray.length(); i++) {
                    if(!bowlsArray.getString(i).contains("undefined")) {
                        addCard(bowlsArray.getString(i));
                    }
                }
            } catch (InterruptedException | JSONException e) {
                throw new RuntimeException(e);
            }
        } else {
            Toast.makeText(UserActivity.this, "Error connecting to network:\nUnable to fetch data", Toast.LENGTH_SHORT).show();
            btnAddBowl.setOnClickListener(v -> {
                Toast.makeText(UserActivity.this, "Error connecting to network:\nCannot add bowl", Toast.LENGTH_SHORT).show();
            });
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        if(user == null) {
            Intent I = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(I);
            finish();
        }
    }

    void showAddBowlDialog() throws InterruptedException {
        final Dialog dialog = new Dialog(UserActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        Objects.requireNonNull(dialog.getWindow()).getDecorView().setBackgroundColor(Color.TRANSPARENT);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setContentView(R.layout.dialog_add_bowl);

        final TextView txtUndefinedBowls = dialog.findViewById(R.id.text_undefined_bowls);
        final EditText txtAddPetName = dialog.findViewById(R.id.edittext_dialog_add_pet_name);
        final EditText txtAddDailyGoal = dialog.findViewById(R.id.edittext_dialog_add_daily_goal);
        Button btnAdd = dialog.findViewById(R.id.button_dialog_add_bowl);

        BlockingQueue<Integer> undefinedBowlsBlockingQueue = requests.getUndefinedBowlsCount();
        int count = undefinedBowlsBlockingQueue.take();

        if(count > 0) {
            String warning = "There are " + count + " available bowls!";
            txtUndefinedBowls.setText(warning);

            btnAdd.setOnClickListener((v) -> {
                String petName = txtAddPetName.getText().toString();
                String dailyGoal = txtAddDailyGoal.getText().toString();

                if(petName.isBlank() || petName.toLowerCase().contains("undefined") || petName.toLowerCase().contains("to_be_def")) {
                    txtAddPetName.setError("Please insert a valid name");
                    txtAddPetName.requestFocus();
                } else if(dailyGoal.isEmpty()) {
                    txtAddDailyGoal.setError("Please insert a valid number");
                    txtAddDailyGoal.requestFocus();
                } else {
                    String error = "";
                    for (int i = 0; i < this.bowlsArray.length(); i++) {
                        try {
                            if(bowlsArray.getString(i).equals(petName))
                               error = "The name " + petName + " already exists";
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    if(error.isEmpty()) {
                        requests.postAddBowl(petName, dailyGoal, user.getUid());
                        requests.resetBowl(petName);
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
            });
        } else {
            String warning = "There are no available bowls!";
            txtUndefinedBowls.setText(warning);
            txtUndefinedBowls.setTextColor(getResources().getColor(com.google.android.material.R.color.design_default_color_error));
            btnAdd.setAlpha(.5f);
            btnAdd.setEnabled(false);
        }

        dialog.show();
    }

    @SuppressLint("SetTextI18n")
    private void addCard(String petName) throws InterruptedException {
        View cardView = getLayoutInflater().inflate(R.layout.card_bowl, null);
        BlockingQueue<Integer> blockingQueue;

        TextView txtBowlName = cardView.findViewById(R.id.text_bowl_name);
        txtBowlName.setText(petName);

        blockingQueue = requests.getFoodAmount(petName);
        String foodAmount = blockingQueue.take().toString();
        TextView txtCurrentDosage = cardView.findViewById(R.id.text_bowl_current_dosage);
        txtCurrentDosage.setText(foodAmount);

        blockingQueue = requests.getDailyGoal(petName);
        String dailyGoal = blockingQueue.take().toString();
        TextView txtDailyGoal = cardView.findViewById(R.id.text_daily_goal);
        txtDailyGoal.setText(dailyGoal);

        Button btnSelect = cardView.findViewById(R.id.button_select_bowl);

        btnSelect.setOnClickListener(v -> {
            Intent I = new Intent(UserActivity.this, FeedingActivity.class);
            Bundle extras = new Bundle();
            extras.putString("bowlName", petName);
            extras.putString("daily_goal", dailyGoal);
            extras.putString("food_amount", foodAmount);
            I.putExtras(extras);
            startActivity(I);
        });

        layout.addView(cardView);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager connManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connManager.getActiveNetworkInfo();
        if (info != null)
            return info.isConnectedOrConnecting(); // WIFI connected
        else
            return false; // no info object implies no connectivity
    }
}