package pt.up.fc.se.petfeeder;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Objects;

public class FeedingFragment extends Fragment {

    View view;
    TextView btnChangeDailyGoal;
    Button btnFeed;
    Button btnResetBowl;

    @SuppressLint("WrongViewCast")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_feeding, container, false);

        //TODO: if there is no selected bowl,
        // show error message that a bowl needs to be selected

        TextView petName = view.findViewById(R.id.text_pet_name);
        //TODO: fetch this from DB

        TextView currentDosage = view.findViewById(R.id.text_current_dosage);
        //TODO: fetch this from DB

        btnChangeDailyGoal = view.findViewById(R.id.text_update_goal_dialog);
        //TODO: fetch value from DB
        btnChangeDailyGoal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUpdateGoalDialog();
            }
        });

        TextView lastFeeding = view.findViewById(R.id.text_last_feeding_time);
        //TODO: fetch this from DB

        //TODO: feeding action here
        btnFeed = view.findViewById(R.id.button_feed);
        btnFeed.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //TODO: update current dosage and last feeding time
                return false;
            }
        });

        btnResetBowl = view.findViewById(R.id.button_reset_bowl_dialog);
        btnResetBowl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showResetBowlDialog();
            }
        });

        return view;
    }

    void showUpdateGoalDialog() {
        final Dialog dialog = new Dialog(requireActivity());
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
        final Dialog dialog = new Dialog(requireActivity());
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