package pt.up.fc.se.petfeeder;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
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
    TextView txtDosageWarning;
    Button btnFeed;
    Button btnResetBowl;
    ServerRequests requests;
    String bowlName;

    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_feeding);

        Bundle extra = getIntent().getExtras();

        assert extra != null;
        bowlName = extra.getString("bowlName");

        requests = new ServerRequests();

        txtBack = findViewById(R.id.label_back);
        txtBack.setOnClickListener(v -> {
            Intent I = new Intent(FeedingActivity.this, UserActivity.class);
            startActivity(I);
        });

        TextView txtPetName = findViewById(R.id.text_pet_name);
        txtPetName.setText(bowlName);

        TextView txtCurrentDosage = findViewById(R.id.text_current_dosage);
        btnChangeDailyGoal = findViewById(R.id.text_update_goal_dialog);

        try {
            int current = requests.getFoodAmount(bowlName).take();
            int goal = requests.getDailyGoal(bowlName).take();

            if(current >= goal * 0.75) {
                txtDosageWarning = findViewById(R.id.text_dosage_warning);
                txtDosageWarning.setVisibility(View.VISIBLE);
            }
            txtCurrentDosage.setText(String.valueOf(current));

            btnChangeDailyGoal.setText(String.valueOf(goal));

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        btnChangeDailyGoal.setOnClickListener(v -> showUpdateGoalDialog(bowlName));

        BlockingQueue<String> feedingTimeBlockingQueue = requests.getLastFeedingTime(bowlName);
        TextView txtLastFeeding = findViewById(R.id.text_last_feeding_time);
        try {
            txtLastFeeding.setText(feedingTimeBlockingQueue.take());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        btnFeed = findViewById(R.id.button_feed);
        btnFeed.setOnTouchListener(new View.OnTouchListener() {
            private Handler handler;

            @Override public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (handler != null) return true;
                        requests.changeMotorState(bowlName, "on");
                        handler = new Handler();
                        handler.postDelayed(action, 200);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (handler == null) return true;
                        handler.removeCallbacks(action);
                        handler = null;
                        requests.changeMotorState(bowlName, "off");
                        break;
                }
                return false;
            }

            final Runnable action = new Runnable() {
                final int goal;
                final int current;

                {
                    try {
                        goal = requests.getDailyGoal(bowlName).take();
                        current = requests.getFoodAmount(bowlName).take();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override public void run() {
                    try {
                        int weight = requests.getFoodPoured(bowlName).take();
                        int total = weight + current;

                        txtCurrentDosage.setText(Integer.toString(total));
                        requests.postLastFeedingTime(bowlName, LocalTime.now().format(DateTimeFormatter.ofPattern("H:m")));
                        txtLastFeeding.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("H:m")));

                        if(total >= goal * 0.75) {
                            txtDosageWarning = findViewById(R.id.text_dosage_warning);
                            txtDosageWarning.setVisibility(View.VISIBLE);
                        }

                        System.out.println(weight+current);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    handler.postDelayed(this, 200);
                }
            };
        });

        btnResetBowl = findViewById(R.id.button_reset_bowl_dialog);
        btnResetBowl.setOnClickListener(v -> showResetBowlDialog());

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

        BlockingQueue<Integer> blockingQueue = requests.getDailyGoal(petName);
        TextView txtCurrentGoal = dialog.findViewById(R.id.text_dialog_current_goal);
        try {
            txtCurrentGoal.setText(blockingQueue.take().toString());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

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

            //TODO: make user wait time
            requests.resetBowl(bowlName);

            dialog.dismiss();
        });

        dialog.show();
    }
}