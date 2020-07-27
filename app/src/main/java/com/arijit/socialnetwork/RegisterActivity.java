package com.arijit.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private EditText UserEmail, UserPassword, UserConfirmPassword;
    private Button CreateAccountButton;

    private ProgressDialog LoadingBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        UserEmail = (EditText)findViewById(R.id.register_email);
        UserPassword = (EditText)findViewById(R.id.register_password);
        UserConfirmPassword = (EditText)findViewById(R.id.register_confirm_password);
        CreateAccountButton = (Button)findViewById(R.id.register_create_account);

        LoadingBar = new ProgressDialog(this);

        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAccount();
            }
        });
    }

    @Override
    protected void onStart() {

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null){
            SendUserToMainActivity();
        }

        super.onStart();
    }

    private void SendUserToMainActivity() {
        Intent MainIntent  = new Intent(RegisterActivity.this, MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();
    }

    private void CreateNewAccount() {
        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();
        String confirm_password = UserConfirmPassword.getText().toString();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please enter your email...", Toast.LENGTH_LONG).show();
        }
        else if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please enter your password...", Toast.LENGTH_LONG).show();
        }
        else if(TextUtils.isEmpty(confirm_password)){
            Toast.makeText(this, "Please confirm your password...", Toast.LENGTH_LONG).show();
        }
        else if(!password.equals(confirm_password)){
            Toast.makeText(this, "Your password does not match with your confirm password...", Toast.LENGTH_LONG).show();
        }
        else{
            LoadingBar.setTitle("Creating New Account");
            LoadingBar.setMessage("Please wait, while we are creating your new account.");
            LoadingBar.show();
            LoadingBar.setCanceledOnTouchOutside(true);

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(RegisterActivity.this, "You were authenticated successfully!", Toast.LENGTH_SHORT).show();
                                LoadingBar.dismiss();
                                SendUserToSetupActivity();
                            }
                            else{
                                String message = task.getException().getMessage();
                                Toast.makeText(RegisterActivity.this, "Error Occurred: " + message, Toast.LENGTH_LONG).show();
                                LoadingBar.dismiss();
                            }
                        }
                    });
        }
    }

    private void SendUserToSetupActivity() {
        Intent setupIntent = new Intent(RegisterActivity.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }
}