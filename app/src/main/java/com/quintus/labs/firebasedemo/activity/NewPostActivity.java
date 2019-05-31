package com.quintus.labs.firebasedemo.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.quintus.labs.firebasedemo.R;
import com.quintus.labs.firebasedemo.model.Post;
import com.quintus.labs.firebasedemo.model.User;
import com.quintus.labs.firebasedemo.utils.LocalStorage;
import com.quintus.labs.firebasedemo.utils.Utility;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class NewPostActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private EditText mTitleField, mBodyField;
    private Button mSubmitButton;
    private static final int CHOOSE_IMAGE = 102;

    ImageView imageView;
    Uri uriPostImage;
    ProgressDialog progressBar;

    String postImageUrl;
    String title,body,userId,userJson;
    User user;
    LocalStorage localStorage;
    Gson gson =new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);
        mTitleField = findViewById(R.id.field_title);
        mBodyField = findViewById(R.id.field_body);
        imageView = findViewById(R.id.imageView);
        mSubmitButton = findViewById(R.id.fab_submit_post);

        localStorage = new LocalStorage(getApplicationContext());
        userJson = localStorage.getUserLogin();
        user = gson.fromJson(userJson,User.class);

        progressBar =new ProgressDialog(NewPostActivity.this);
        progressBar.setMessage("Please Wait...");

        mDatabase = FirebaseDatabase.getInstance().getReference();

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitPost();
            }
        });
    }

    private boolean validateForm(String title, String body) {
        if (TextUtils.isEmpty(title)) {
            mTitleField.setError(getString(R.string.required));
            return false;
        } else if (TextUtils.isEmpty(body)) {
            mBodyField.setError(getString(R.string.required));
            return false;
        } else {
            mTitleField.setError(null);
            mBodyField.setError(null);
            return true;
        }
    }

    private void submitPost() {
         title = mTitleField.getText().toString().trim();
         body = mBodyField.getText().toString().trim();
        userId = getUserId();

        if (validateForm(title, body)) {
            // Disable button so there are no multi-posts
            setEditingEnabled(false);
            uploadImageToFirebaseStorage();

        }
    }

    private String getUserId() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        return uid;
    }

    @SuppressLint("RestrictedApi")
    private void setEditingEnabled(boolean enabled) {
        mTitleField.setEnabled(enabled);
        mBodyField.setEnabled(enabled);
        if (enabled) {
            mSubmitButton.setVisibility(View.VISIBLE);
        } else {
            mSubmitButton.setVisibility(View.GONE);
        }
    }

    private void writeNewPost(String userId, String username, String title, String body,String image) {
        // Create new post at /user-posts/$userid/$postid
        // and at /posts/$postid simultaneously
        String key = mDatabase.child("posts").push().getKey();
        Post post = new Post(userId, username, title, body,image);
        Map<String, Object> postValues = post.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/posts/" + key, postValues);
        childUpdates.put("/user-posts/" + userId + "/" + key, postValues);

        mDatabase.updateChildren(childUpdates);
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
        finish();
    }

    public void onImageClicked(View view) {

        boolean result= Utility.checkPermission(NewPostActivity.this);
        if(result){
            showImageChooser();
        }
    }

    private void showImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), CHOOSE_IMAGE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CHOOSE_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uriPostImage = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uriPostImage);
                imageView.setImageBitmap(bitmap);



            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImageToFirebaseStorage() {

        final StorageReference profileImageRef =
                FirebaseStorage.getInstance().getReference("postImage/" + System.currentTimeMillis() + ".jpg");

        if (uriPostImage != null) {
           progressBar.show();
            profileImageRef.putFile(uriPostImage)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                           progressBar.dismiss();

                            profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    postImageUrl = uri.toString();
                                    writeNewPost(userId, user.name, title, body,postImageUrl);
                                    Log.d("IMAGE",postImageUrl);
                                }
                            });

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            setEditingEnabled(true);
                           progressBar.dismiss();
                            Toast.makeText(NewPostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}