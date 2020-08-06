package com.arijit.socialnetwork;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private EditText UserName, FullName, CountryName;
    private Button SaveInformationButton;
    private CircleImageView UserProfileImage;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;
    private StorageReference UserProfileImageRef;

    String currentUserId;

    final static int gallery_pick = 1;

    private ProgressDialog LoadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        UserName = (EditText)findViewById(R.id.setup_username);
        FullName = (EditText)findViewById(R.id.setup_fullname);
        CountryName = (EditText)findViewById(R.id.setup_country);
        SaveInformationButton = (Button)findViewById(R.id.setup_information_button);
        UserProfileImage = (CircleImageView)findViewById(R.id.setup_profile_image);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        UserProfileImageRef= FirebaseStorage.getInstance().getReference().child("Profile Images");

        LoadingBar = new ProgressDialog(this);

        SaveInformationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveAccountSetupInformation();
            }
        });

        UserProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(SetupActivity.this);
            }
        });

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){

                    if(snapshot.hasChild("profile_image")){
                        String image = snapshot.child("profile_image").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(UserProfileImage);
                    }
                    else{
                        Toast.makeText(SetupActivity.this, "Please select a profile image first...", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK){
                LoadingBar.setTitle("Profile Image");
                LoadingBar.setMessage("Please wait, while we are updating your profile image.");
                LoadingBar.setCanceledOnTouchOutside(true);
                LoadingBar.show();

                Uri resultUri = result.getUri();

                final StorageReference filePath = UserProfileImageRef.child(currentUserId + ".jpg");
                filePath.putFile(resultUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        final String downloadUrl = uri.toString();
                                        UsersRef.child("profile_image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    Toast.makeText(SetupActivity.this, "Profile Image saves to Firebase Database successfully!", Toast.LENGTH_SHORT).show();
                                                    Intent selfIntent = new Intent(SetupActivity.this, SetupActivity.class);
                                                    startActivity(selfIntent);
                                                    LoadingBar.dismiss();
                                                }
                                                else{
                                                    String message = task.getException().getMessage();
                                                    Toast.makeText(SetupActivity.this, "Error Occurred: " + message, Toast.LENGTH_LONG).show();
                                                    LoadingBar.dismiss();
                                                }
                                            }
                                        });
                                    }
                                });
                            }
                        });
            }
            else{
                Toast.makeText(SetupActivity.this, "Error Occurred: Image can't be cropped. Try Again!", Toast.LENGTH_LONG).show();
                LoadingBar.dismiss();
            }
        }
    }

    private void SaveAccountSetupInformation() {
        String username = UserName.getText().toString();
        String fullname = FullName.getText().toString();
        String country = CountryName.getText().toString();

        if(TextUtils.isEmpty(username)){
            Toast.makeText(SetupActivity.this, "Please write your username...", Toast.LENGTH_LONG).show();
        }
        else if(TextUtils.isEmpty(fullname)){
            Toast.makeText(SetupActivity.this, "Please write your fullname...", Toast.LENGTH_LONG).show();
        }
        else if(TextUtils.isEmpty(country)){
            Toast.makeText(SetupActivity.this, "Please write your country...", Toast.LENGTH_LONG).show();
        }
        else{
            LoadingBar.setTitle("Saving Information");
            LoadingBar.setMessage("Please wait, while we are creating your new account.");
            LoadingBar.setCanceledOnTouchOutside(true);
            LoadingBar.show();

            HashMap UserMap = new HashMap();
            UserMap.put("username", username);
            UserMap.put("fullname", fullname);
            UserMap.put("country", country);
            UserMap.put("status", "Hey there I am using AZTalks");
            UserMap.put("gender", "None");
            UserMap.put("dob", "None");
            UserMap.put("relationship_status", "None");

            UsersRef.updateChildren(UserMap)
                    .addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if(task.isSuccessful()){
                                Toast.makeText(SetupActivity.this, "Your account has been created successfully!", Toast.LENGTH_SHORT).show();
                                SendUserToMainActivity();
                                LoadingBar.dismiss();
                            }
                            else{
                                String message = task.getException().getMessage();
                                Toast.makeText(SetupActivity.this, "Error Occurred: " + message, Toast.LENGTH_LONG).show();
                                LoadingBar.dismiss();
                            }
                        }
                    });
        }
    }

    private void SendUserToMainActivity() {
        Intent MainIntent  = new Intent(SetupActivity.this, MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();
    }
}