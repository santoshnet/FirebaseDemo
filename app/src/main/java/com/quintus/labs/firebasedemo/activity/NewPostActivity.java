package com.quintus.labs.firebasedemo.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.quintus.labs.firebasedemo.R;
import com.quintus.labs.firebasedemo.model.Post;
import com.quintus.labs.firebasedemo.model.User;

import java.util.HashMap;
import java.util.Map;

public class NewPostActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private EditText mTitleField, mBodyField;
    private Button mSubmitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);
        mTitleField = findViewById(R.id.field_title);
        mBodyField = findViewById(R.id.field_body);
        mSubmitButton = findViewById(R.id.fab_submit_post);

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
        final String title = mTitleField.getText().toString().trim();
        final String body = mBodyField.getText().toString().trim();
        final String userId = getUserId();

        if (validateForm(title, body)) {
            // Disable button so there are no multi-posts
            setEditingEnabled(false);
            mDatabase.child("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user == null) {
                        Toast.makeText(NewPostActivity.this, "Error: could not fetch user.", Toast.LENGTH_LONG).show();
                    } else {
                        writeNewPost(userId, user.name, title, body);
                    }
                    setEditingEnabled(true);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    setEditingEnabled(true);
                    Toast.makeText(NewPostActivity.this, "onCancelled: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
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

    private void writeNewPost(String userId, String username, String title, String body) {
        // Create new post at /user-posts/$userid/$postid
        // and at /posts/$postid simultaneously
        String key = mDatabase.child("posts").push().getKey();
        Post post = new Post(userId, username, title, body);
        Map<String, Object> postValues = post.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/posts/" + key, postValues);
        childUpdates.put("/user-posts/" + userId + "/" + key, postValues);

        mDatabase.updateChildren(childUpdates);
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
        finish();
    }
}