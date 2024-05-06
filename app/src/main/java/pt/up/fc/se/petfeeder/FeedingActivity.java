package pt.up.fc.se.petfeeder;

import android.annotation.SuppressLint;
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

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;

public class FeedingActivity extends AppCompatActivity {

    TextView btnChangeDailyGoal;
    TextView txtBack;
    Button btnFeed;
    Button btnResetBowl;
    ServerRequests requests;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_feeding);

        Bundle extra = getIntent().getExtras();

        assert extra != null;
        String petName = extra.getString("bowlName");

        requests = new ServerRequests();

        BlockingQueue<Integer> blockingQueue;

        txtBack = findViewById(R.id.label_back);
        txtBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent I = new Intent(FeedingActivity.this, UserActivity.class);
                startActivity(I);
            }
        });

        TextView txtPetName = findViewById(R.id.text_pet_name);
        txtPetName.setText(petName);

        blockingQueue = requests.getFoodAmount(petName);
        TextView txtCurrentDosage = findViewById(R.id.text_current_dosage);
        try {
            txtCurrentDosage.setText(blockingQueue.take().toString());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // TODO
        //blockingQueue = requests.getDailyGoal(petName);
        btnChangeDailyGoal = findViewById(R.id.text_update_goal_dialog);
//        try {
//            btnChangeDailyGoal.setText(blockingQueue.take().toString());
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }

        btnChangeDailyGoal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUpdateGoalDialog(petName);
            }
        });

        BlockingQueue<LocalTime> localTimeBlockingQueue = requests.getLastFeedingTime(petName);
        TextView txtLastFeeding = findViewById(R.id.text_last_feeding_time);
        try {
            txtLastFeeding.setText(localTimeBlockingQueue.take().format(DateTimeFormatter.ISO_LOCAL_DATE));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //TODO: feeding action here
        btnFeed = findViewById(R.id.button_feed);
        btnFeed.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //TODO: update current dosage and last feeding time
                while(true) {
                    // get weight from the scale
                    // get feeding time
                    // update last feeding time?
                }
            }
        });

        //TODO: reset scale request
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

    @SuppressLint("SetTextI18n")
    void showUpdateGoalDialog(String petName) {
        final Dialog dialog = new Dialog(FeedingActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        Objects.requireNonNull(dialog.getWindow()).getDecorView().setBackgroundColor(Color.TRANSPARENT);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setContentView(R.layout.dialog_daily_goal);

        //BlockingQueue<Integer> blockingQueue = requests.getDailyGoal(petName);
        TextView txtCurrentGoal = dialog.findViewById(R.id.text_dialog_current_goal);
//        try {
//            txtCurrentGoal.setText(blockingQueue.take().toString());
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }

        final EditText txtDailyGoal = dialog.findViewById(R.id.edittext_dialog_update_daily_goal);
        Button btnUpdate = dialog.findViewById(R.id.button_dialog_update_daily_goal);

        btnUpdate.setOnClickListener((v) -> {
            String dailyGoal = txtDailyGoal.getText().toString();

            if(dailyGoal.isEmpty()) {
                txtDailyGoal.setError("Please insert a valid number");
                txtDailyGoal.requestFocus();
            } else {
                requests.postDailyGoal(petName, dailyGoal);
                dialog.dismiss();
            }
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