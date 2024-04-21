package pt.up.fc.se.petfeeder;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Objects;

public class FeedingActivity extends AppCompatActivity {

    TextView btnChangeDailyGoal;
    TextView txtBack;
    Button btnFeed;
    Button btnResetBowl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_feeding);

        Bundle extra = getIntent().getExtras();

        //TODO: using extra, fetch from DB info about bowl

        txtBack = findViewById(R.id.label_back);
        txtBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent I = new Intent(FeedingActivity.this, UserActivity.class);
                startActivity(I);
            }
        });

        TextView txtPetName = findViewById(R.id.text_pet_name);
        if(extra != null) txtPetName.setText(extra.getString("bowlName"));

        TextView txtCurrentDosage = findViewById(R.id.text_current_dosage);
        //TODO: fetch this from DB

        btnChangeDailyGoal = findViewById(R.id.text_update_goal_dialog);
        //TODO: fetch value from DB
        btnChangeDailyGoal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUpdateGoalDialog();
            }
        });

        TextView txtLastFeeding = findViewById(R.id.text_last_feeding_time);
        //TODO: fetch this from DB

        //TODO: feeding action here
        btnFeed = findViewById(R.id.button_feed);
        btnFeed.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //TODO: update current dosage and last feeding time
                return false;
            }
        });

        btnResetBowl = findViewById(R.id.button_reset_bowl_dialog);
        btnResetBowl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showResetBowlDialog();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    void showUpdateGoalDialog() {
        final Dialog dialog = new Dialog(FeedingActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        Objects.requireNonNull(dialog.getWindow()).getDecorView().setBackgroundColor(Color.TRANSPARENT);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setContentView(R.layout.dialog_daily_goal);

        TextView currentGoal = dialog.findViewById(R.id.text_dialog_current_goal);
        //TODO: fetch from DB this value

        final EditText txtDailyGoal = dialog.findViewById(R.id.edittext_dialog_update_daily_goal);
        Button btnUpdate = dialog.findViewById(R.id.button_dialog_update_daily_goal);

        btnUpdate.setOnClickListener((v) -> {
            String dailyGoal = txtDailyGoal.getText().toString();

            //TODO: send this info to database

            dialog.dismiss();
        });

        dialog.show();
    }

    void showResetBowlDialog() {
        final Dialog dialog = new Dialog(FeedingActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        Objects.requireNonNull(dialog.getWindow()).getDecorView().setBackgroundColor(Color.TRANSPARENT);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setContentView(R.layout.dialog_reset_bowl);

        Button btnReset = dialog.findViewById(R.id.button_dialog_reset_bowl);

        btnReset.setOnClickListener((v) -> {

            //TODO: process reset bowl request

            dialog.dismiss();
        });

        dialog.show();
    }
}