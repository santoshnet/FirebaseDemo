package com.quintus.labs.firebasedemo.activity;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.quintus.labs.firebasedemo.R;
import com.quintus.labs.firebasedemo.model.Post;

public class PostViewHolder extends RecyclerView.ViewHolder {
    TextView textViewTitle;
    TextView textViewBody;

    public PostViewHolder(@NonNull View itemView) {
        super(itemView);
        textViewTitle = itemView.findViewById(R.id.title);
        textViewBody = itemView.findViewById(R.id.description);
    }

    public void setItem(Post post){
        textViewTitle.setText(post.title);
        textViewBody.setText(post.body);
    }
}