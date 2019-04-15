package com.example.barterapp;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener{

    private ImageView profile;
    private Uri imgUri;
    private EditText nameInput;
    private EditText emailInput;
    private EditText passwordInput;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        profile = findViewById(R.id.profile_image);
        nameInput = findViewById(R.id.name_field);
        emailInput = findViewById(R.id.sign_up_email_field);
        passwordInput = findViewById(R.id.sign_up_password_field);

        findViewById(R.id.sign_up_button_in_activity_sign_up).setOnClickListener(this);
        findViewById(R.id.sign_in_button_in_activity_sign_up).setOnClickListener(this);
        profile.setOnClickListener(this);

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
                    Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                    intent.putExtra("user", user);
                    startActivity(intent);
                } else {
                    Toast.makeText(SignUpActivity.this, R.string.failed_to_sign_up, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



    private boolean isValid() {
        boolean isValid = true;

        String email = emailInput.getText().toString();
        if (email.isEmpty()) {
            emailInput.setError("This field cannot be blank.");
            isValid = false;
        } else if (!email.endsWith("@pace.edu")) {
            emailInput.setError("Please sign up with your pace email.");
            isValid = false;
        } else {
            emailInput.setError(null);
        }

        String password = passwordInput.getText().toString();
        if (password.isEmpty()) {
            passwordInput.setError("This field cannot be blank.");
            isValid = false;
        } else if (password.length() < 10) {
            passwordInput.setError("Your password must be at least 10 characters.");
            isValid = false;
        } else {
            passwordInput.setError(null);
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
        } else if (i == R.id.profile_image) {
            selectImage();
        }
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, 5);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 5 && resultCode == RESULT_OK) {
            profile.setImageURI(data.getData());
            imgUri = data.getData();
        }
    }
}
