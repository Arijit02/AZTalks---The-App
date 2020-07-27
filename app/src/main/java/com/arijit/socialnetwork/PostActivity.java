package com.arijit.socialnetwork;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private ImageButton selectPostImage;
    private Button updatePostButton;
    private EditText postDescription;

    private Uri ImageUri;
    private String description;

    private String downloadUrl;

    private DatabaseReference PostsRef, UserRef;
    private FirebaseAuth mAuth;

    private String currentUserId;

    private StorageReference postImageRef;

    private String saveCurrentDate, saveCurrentTime, postRandomName;

    private ProgressDialog LoadingBar;

    final static int gallery_pick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        selectPostImage = (ImageButton) findViewById(R.id.post_image);
        updatePostButton = (Button) findViewById(R.id.update_post_button);
        postDescription = (EditText) findViewById(R.id.post_description);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        postImageRef = FirebaseStorage.getInstance().getReference();

        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        LoadingBar = new ProgressDialog(this);

        mToolbar = (Toolbar) findViewById(R.id.update_post_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Update Post");

        selectPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenGallery();
            }
        });

        updatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidatePostInfo();
            }
        });
    }

    private void ValidatePostInfo() {
        description = postDescription.getText().toString();

        if(ImageUri == null){
            Toast.makeText(PostActivity.this, "Please select post image...", Toast.LENGTH_LONG).show();
        }
        else if(TextUtils.isEmpty(description)){
            Toast.makeText(PostActivity.this, "Please say something about your image...", Toast.LENGTH_LONG).show();
        }
        else{
            LoadingBar.setTitle("Add New Post");
            LoadingBar.setMessage("Please wait, while we are updating your post.");
            LoadingBar.setCanceledOnTouchOutside(true);
            LoadingBar.show();
            StoringImageToFirebaseStorage();
        }
    }

    private void StoringImageToFirebaseStorage() {
        Calendar callForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(callForDate.getTime());

        Calendar callForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(callForTime.getTime());

        postRandomName = saveCurrentDate + saveCurrentTime;

        final StorageReference filePath = postImageRef.child("Post Images").child(ImageUri.getLastPathSegment() + postRandomName + ".jpg");
        filePath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    Toast.makeText(PostActivity.this, "Image uploaded to storage successfully!", Toast.LENGTH_LONG).show();
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            downloadUrl = uri.toString();
                            SavingPostInformationToDatabase();
                        }
                    });
                }
                else{
                    LoadingBar.dismiss();
                    String message = task.getException().getMessage();
                    Toast.makeText(PostActivity.this, "Error Occurred: " + message, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void SavingPostInformationToDatabase() {
        UserRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String userfullname = snapshot.child("fullname").getValue().toString();
                    String userprofileimage = snapshot.child("profile_image").getValue().toString();

                    final HashMap PostsMap = new HashMap();
                    PostsMap.put("uid", currentUserId);
                    PostsMap.put("date", saveCurrentDate);
                    PostsMap.put("time", saveCurrentTime);
                    PostsMap.put("description", description);
                    PostsMap.put("post_image", downloadUrl);
                    PostsMap.put("profile_image", userprofileimage);
                    PostsMap.put("fullname", userfullname);

                    PostsRef.child(postRandomName + currentUserId).updateChildren(PostsMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if(task.isSuccessful()){
                                LoadingBar.dismiss();
                                Toast.makeText(PostActivity.this, "New post is updated successfully!", Toast.LENGTH_SHORT).show();
                                SendUserToMainActivity();
                            }
                            else{
                                LoadingBar.dismiss();
                                Toast.makeText(PostActivity.this, "Error occurred while updating your post, Try again!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void OpenGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, gallery_pick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == gallery_pick && resultCode == RESULT_OK && data != null){
            ImageUri = data.getData();
            selectPostImage.setImageURI(ImageUri);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        if(id == android.R.id.home){
            SendUserToMainActivity();
        }

        return super.onOptionsItemSelected(item);
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(PostActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}