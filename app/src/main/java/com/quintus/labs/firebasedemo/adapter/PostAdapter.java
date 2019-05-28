package com.quintus.labs.firebasedemo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.quintus.labs.firebasedemo.R;
import com.quintus.labs.firebasedemo.model.Post;

import java.util.ArrayList;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.MyViewHolder> {

    private List<Post> postList;
    Context context;

    public PostAdapter(Context context) {
        this.postList = new ArrayList<>();
        this.context = context;
    }

    public PostAdapter() {
        this.postList = new ArrayList<>();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, description;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            description = (TextView) view.findViewById(R.id.description);

        }
    }



    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.post_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.title.setText(post.getTitle());
        holder.description.setText(post.getBody());

    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public void addAll(List<Post> newList) {
        int initialSize = postList.size();
        postList.addAll(newList);
        notifyItemRangeInserted(initialSize, newList.size());
    }

    public String getLastItemId() {
        return postList.get(postList.size() - 1).getId();
    }
}