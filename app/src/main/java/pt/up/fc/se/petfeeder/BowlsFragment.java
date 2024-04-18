package pt.up.fc.se.petfeeder;

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

import java.util.Objects;

public class BowlsFragment extends Fragment {

    View view;
    Button btnAddBowl;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_bowls, container, false);

        btnAddBowl = view.findViewById(R.id.button_add_bowl_dialog);

        btnAddBowl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddBowlDialog();
            }
        });

        //TODO: get all bowls from DB
        // make dynamic list of bowls

        //TODO: action of selecting bowl
        // if no bowl is selected, select
        // if a bowl is already selected, unselect that bowl, select new bowl

        //TODO: when a bowl is selected, redirect view to feeding fragment

        return view;
    }

    void showAddBowlDialog() {
        final Dialog dialog = new Dialog(requireActivity());
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

            //TODO: send this info to database
            //TODO: get bowl weight and 'reset' it

            dialog.dismiss();
        });

        dialog.show();
    }
}