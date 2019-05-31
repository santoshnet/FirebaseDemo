package com.quintus.labs.firebasedemo.view_holder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.quintus.labs.firebasedemo.R;
import com.quintus.labs.firebasedemo.model.Post;
import com.squareup.picasso.Picasso;

public class PostViewHolder extends RecyclerView.ViewHolder {
    TextView textViewTitle;
    TextView textViewBody;
    ImageView imageView;

    public PostViewHolder(@NonNull View itemView) {
        super(itemView);
        textViewTitle = itemView.findViewById(R.id.title);
        textViewBody = itemView.findViewById(R.id.description);
        imageView = itemView.findViewById(R.id.post_imageView);
    }

    public void setItem(Post post){
        textViewTitle.setText(post.title);
        textViewBody.setText(post.body);
        if(post.image!=null){
            imageView.setVisibility(View.VISIBLE);
            Picasso.get().load(post.image).into(imageView);
            Log.d("Post Image==>",post.image);
        }else {
            imageView.setVisibility(View.GONE);
        }


    }
}