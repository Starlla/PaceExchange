package com.example.pace_exchange;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText firstNameInput;
    private EditText lastNameInput;
    private EditText emailInput;
    private EditText passwordInput;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        firstNameInput = findViewById(R.id.sign_up_first_name_field);
        lastNameInput = findViewById(R.id.sign_up_last_name_field);
        emailInput = findViewById(R.id.sign_up_email_field);
        passwordInput = findViewById(R.id.sign_up_password_field);

        findViewById(R.id.sign_up_button_in_activity_sign_up).setOnClickListener(this);
        findViewById(R.id.sign_in_button_in_activity_sign_up).setOnClickListener(this);

        auth = FirebaseAuth.getInstance();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void createAccount(String email, String password) {
        if (!isValid()) {
            return;
        }

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = auth.getCurrentUser();
                    String user_id = user.getUid();
                    String email = user.getEmail();
                    String first_name = firstNameInput.getText().toString();
                    String last_name = lastNameInput.getText().toString();

                    DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child(getString(R.string.node_users)).child(user_id);

                    Map<String, String> newPost = new HashMap<>();
                    newPost.put(getString(R.string.field_first_name), first_name);
                    newPost.put(getString(R.string.field_last_name), last_name);
                    newPost.put(getString(R.string.field_email),email);
                    current_user_db.setValue(newPost);

                    Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                    intent.putExtra(getString(R.string.extra_user), user);
                    startActivity(intent);
                } else {
                    Toast.makeText(SignUpActivity.this, R.string.failed_to_sign_up, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean isValid() {
        boolean isValid = true;
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_error_24dp);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

        String firstName = firstNameInput.getText().toString();
        if (firstName.isEmpty()) {
            firstNameInput.requestFocus();
            firstNameInput.setError(this.getString(R.string.this_field_cannot_be_blank), drawable);
            isValid = false;
        }

        String lastName = lastNameInput.getText().toString();
        if (lastName.isEmpty()) {
            lastNameInput.requestFocus();
            lastNameInput.setError(this.getString(R.string.this_field_cannot_be_blank), drawable);
            isValid = false;
        }

        String email = emailInput.getText().toString();
        if (email.isEmpty()) {
            emailInput.requestFocus();
            emailInput.setError(getString(R.string.this_field_cannot_be_blank), drawable);
            isValid = false;
        } else if (!email.endsWith(getString(R.string.email_suffix))) {
            emailInput.requestFocus();
            emailInput.setError(getString(R.string.please_sign_up_with_your_pace_email), drawable);
            isValid = false;
        }

        String password = passwordInput.getText().toString();
        if (password.isEmpty()) {
            passwordInput.requestFocus();
            passwordInput.setError(getString(R.string.this_field_cannot_be_blank), drawable);
            isValid = false;
        } else if (password.length() < 10) {
            passwordInput.requestFocus();
            passwordInput.setError(getString(R.string.your_password_must_be_at_least_10_characters), drawable);
            isValid = false;
        }

        return isValid;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.sign_up_button_in_activity_sign_up) {
            createAccount(emailInput.getText().toString(), passwordInput.getText().toString());
        } else if (i == R.id.sign_in_button_in_activity_sign_up) {
            Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
            startActivity(intent);
        }
    }
}
