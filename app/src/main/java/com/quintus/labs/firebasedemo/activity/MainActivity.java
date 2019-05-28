package com.quintus.labs.firebasedemo.activity;


import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;



import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.quintus.labs.firebasedemo.R;
import com.quintus.labs.firebasedemo.adapter.PostAdapter;
import com.quintus.labs.firebasedemo.model.Post;
import com.quintus.labs.firebasedemo.utils.Utility;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView mRV;
    private PostAdapter mAdapter;
    private int mTotalItemCount = 0;
    private int mLastVisibleItemPosition;
    private boolean isLoading = false;
    private boolean isMaxData = false;
    private int ITEM_LOAD_COUNT = 5;
    ProgressBar progressBar;
    FirebaseAuth mAuth;

    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        progressBar = (ProgressBar)findViewById(R.id.progressbar);

        mRV = findViewById(R.id.recyclerView);

        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRV.setLayoutManager(mLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRV.getContext(),
                mLayoutManager.getOrientation());
        mRV.addItemDecoration(dividerItemDecoration);

        mAdapter = new PostAdapter();
        mRV.setAdapter(mAdapter);


        getPosts(null);
        mRV.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                mTotalItemCount = mLayoutManager.getItemCount();
                mLastVisibleItemPosition = mLayoutManager.findLastVisibleItemPosition();

                if (!isLoading && mTotalItemCount <= (mLastVisibleItemPosition + ITEM_LOAD_COUNT)) {
                    getPosts(mAdapter.getLastItemId());
                    isLoading = true;
                }
            }
        });

    }




    private void getPosts(String nodeId) {
        Query query;
        progressBar.setVisibility(View.VISIBLE);

        if (nodeId == null)
            query = FirebaseDatabase.getInstance().getReference()
                    .child("posts")
                    .orderByKey()
                    .limitToFirst(ITEM_LOAD_COUNT);
        else
            query = FirebaseDatabase.getInstance().getReference()
                    .child("posts")
                    .orderByKey()
                    .startAt(nodeId)
                    .limitToFirst(ITEM_LOAD_COUNT);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChildren()){
                    Post post;
                    List<Post> postList = new ArrayList<>();
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        post = postSnapshot.getValue(Post.class);
                        String postId = postSnapshot.getKey();
                        post.setId(postId);
                        postList.add(post);
                    }

                    mAdapter.addAll(postList);
                    progressBar.setVisibility(View.GONE);
                }else{
                    isLoading=false;
                    progressBar.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                isLoading = false;
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
    }



}