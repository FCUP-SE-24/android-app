package pt.up.fc.se.petfeeder;

import android.content.Intent;
import android.os.Bundle;
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

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    public EditText etEmail, etPassword;
    Button btnLogin;
    TextView txtRegisterRedirect;
    FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        etEmail = findViewById(R.id.edittext_login_email);
        etPassword = findViewById(R.id.edittext_login_password);
        btnLogin = findViewById(R.id.button_login);
        txtRegisterRedirect = findViewById(R.id.text_no_account);

        authStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if(user != null) {
                Toast.makeText(MainActivity.this, "User logged in ", Toast.LENGTH_SHORT).show();
                Intent I = new Intent(MainActivity.this, UserActivity.class);
                startActivity(I);
            } else {
                Toast.makeText(MainActivity.this, "Login to continue", Toast.LENGTH_SHORT).show();
            }
        };

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString();
            String pwd = etPassword.getText().toString();
            if(email.isEmpty() && pwd.isEmpty()) {
                Toast.makeText(MainActivity.this, "Fields are empty", Toast.LENGTH_SHORT).show();
            } else if(email.isEmpty()) {
                etEmail.setError("Provide an email first");
                etEmail.requestFocus();
            } else if(pwd.isEmpty()) {
                etPassword.setError("Password can't be empty");
                etPassword.requestFocus();
            } else {
                firebaseAuth.signInWithEmailAndPassword(email, pwd).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Login failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                        } else {
                            startActivity(new Intent(MainActivity.this, UserActivity.class));
                        }
                    }
                });
            }
        });

        txtRegisterRedirect.setOnClickListener(v -> {
            Intent I = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(I);
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
        firebaseAuth.addAuthStateListener(authStateListener);
    }
}