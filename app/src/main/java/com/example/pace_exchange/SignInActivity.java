package com.example.pace_exchange;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText emailInput;
    private EditText passwordInput;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        emailInput = findViewById(R.id.sign_in_email_field);
        passwordInput = findViewById(R.id.sign_in_password_field);
        findViewById(R.id.sign_in_button_in_activity_sign_in).setOnClickListener(this);
        findViewById(R.id.sign_up_button_in_activity_sign_in).setOnClickListener(this);
        auth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if the user has signed in, if so then update.
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(SignInActivity.this, MainActivity.class);
            intent.putExtra(getString(R.string.extra_user), currentUser);
            startActivity(intent);
        }
    }

    private void signIn(String email, String password) {
        if (!isValid()) {
            return;
        }

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Successfully sign in, update.
                    FirebaseUser user = auth.getCurrentUser();
                    Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                    intent.putExtra(getString(R.string.extra_user), user);
                    startActivity(intent);
                } else {
                    // Fail to sign in, display a message to the user.
                    Toast.makeText(SignInActivity.this, R.string.unable_to_sign_in, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean isValid() {
        boolean isValid = true;
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_error_24dp);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

        String email = emailInput.getText().toString();
        if (email.isEmpty()) {
            emailInput.requestFocus();
            emailInput.setError(getString(R.string.this_field_cannot_be_blank), drawable);
            isValid = false;
        } else if (!email.endsWith(getString(R.string.email_suffix))) {
            emailInput.requestFocus();
            emailInput.setError(getString(R.string.please_sign_in_with_your_pace_email), drawable);
            isValid = false;
        }

        String password = passwordInput.getText().toString();
        if (password.isEmpty()) {
            passwordInput.requestFocus();
            passwordInput.setError(getString(R.string.this_field_cannot_be_blank), drawable);
            isValid = false;
        }
        return isValid;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.sign_in_button_in_activity_sign_in) {
            signIn(emailInput.getText().toString(), passwordInput.getText().toString());
        } else if (i == R.id.sign_up_button_in_activity_sign_in) {
            Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
            startActivity(intent);
        }
    }
}
