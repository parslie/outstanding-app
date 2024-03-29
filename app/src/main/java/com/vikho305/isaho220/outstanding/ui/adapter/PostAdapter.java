package com.vikho305.isaho220.outstanding.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.vikho305.isaho220.outstanding.R;
import com.vikho305.isaho220.outstanding.data.Post;
import com.vikho305.isaho220.outstanding.data.media.ImageMedia;

import java.util.ArrayList;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private List<Post> posts;
    private LayoutInflater inflater;

    public PostAdapter(Context context) {
        this.posts = new ArrayList<>();
        inflater = LayoutInflater.from(context);
    }

    public void addPosts(List<Post> newPosts) {
        posts.addAll(newPosts);
        notifyDataSetChanged();
    }

    public void setPosts(List<Post> newPosts) {
        posts = newPosts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindData(posts.get(position));
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void bindData(Post post) {
            TextView titleView = itemView.findViewById(R.id.postItemTitle);
            TextView contentView = itemView.findViewById(R.id.postItemContent);
            ShapeableImageView imageView = itemView.findViewById(R.id.postItemImage);
            TextView dateView = itemView.findViewById(R.id.postItemDate);
            titleView.setText(post.getTitle());
            contentView.setText(post.getText());
            dateView.setText(post.getDateCreated());

            if (post.getText().length() == 0)
                contentView.setVisibility(View.GONE);
            else
                contentView.setVisibility(View.VISIBLE);

            if (post.getMediaType().equals(Post.IMAGE_TYPE)) {
                imageView.setVisibility(View.VISIBLE);

                ImageMedia imageMedia = new ImageMedia();
                imageView.setImageBitmap(imageMedia.base64ToBitmap(post.getMedia()));
            }
            else {
                imageView.setVisibility(View.GONE);
            }
        }
    }
}
