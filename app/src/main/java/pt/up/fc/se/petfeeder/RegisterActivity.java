package pt.up.fc.se.petfeeder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    public EditText etEmail, etPassword, etConfirm;
    Button btnRegister;
    TextView txtLoginRedirect;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();
        etEmail = findViewById(R.id.edittext_register_email);
        etPassword = findViewById(R.id.edittext_register_password);
        etConfirm = findViewById(R.id.edittext_register_confirm_pwd);
        btnRegister = findViewById(R.id.button_register);
        txtLoginRedirect = findViewById(R.id.text_has_account);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString();
                String pwd = etPassword.getText().toString();
                String confirm_pwd = etConfirm.getText().toString();

                if(email.isEmpty() && pwd.isEmpty() && confirm_pwd.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "All fields are mandatory", Toast.LENGTH_SHORT).show();
                } else if(email.isEmpty()) {
                    etEmail.setError("Provide an email first");
                    etEmail.requestFocus();
                } else if(pwd.isEmpty()) {
                    etPassword.setError("Enter a password");
                    etPassword.requestFocus();
                } else if(confirm_pwd.isEmpty()) {
                    etConfirm.setError("Please confirm your password");
                    etConfirm.requestFocus();
                } else {
                    if(pwd.equals(confirm_pwd)) {
                        firebaseAuth.createUserWithEmailAndPassword(email, pwd).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(!task.isSuccessful()) {
                                    Toast.makeText(RegisterActivity.this.getApplicationContext(), "Registration was unsuccessful: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                } else {
                                    startActivity(new Intent(RegisterActivity.this, UserActivity.class));
                                }
                            }
                        });
                    } else {
                        etConfirm.setError("Passwords are different");
                        etConfirm.requestFocus();
                    }

                }
            }
        });

        txtLoginRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent I = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(I);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}