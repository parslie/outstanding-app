package com.vikho305.isaho220.outstanding.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.fragment.app.Fragment;

import com.vikho305.isaho220.outstanding.OnClickCallback;
import com.vikho305.isaho220.outstanding.R;
import com.vikho305.isaho220.outstanding.activity.PostActivity;
import com.vikho305.isaho220.outstanding.database.Post;
import com.vikho305.isaho220.outstanding.database.User;

import java.util.Objects;

public class PostFragment extends Fragment implements View.OnClickListener {

    public static final String AUTHOR_CLICK_KEY = "author", LIKE_CLICK_KEY = "like";
    public static final String DISLIKE_CLICK_KEY = "dislike";

    private TextView titleView, textView;
    private TextView likeCountView, dislikeCountView;

    private ImageView imageView;
    private ImageView authorPicture;
    private TextView authorUsername;

    private Button likeButton;
    private Button dislikeButton;

    private OnClickCallback onClickCallback;

    public void setOnClickCallback(OnClickCallback onClickCallback) {
        this.onClickCallback = onClickCallback;
    }

    public PostFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post, container, false);

        // Get views
        titleView = view.findViewById(R.id.postFrag_title);
        textView = view.findViewById(R.id.postFrag_text);

        likeCountView = view.findViewById(R.id.postFrag_likeCount);
        dislikeCountView = view.findViewById(R.id.postFrag_dislikeCount);

        imageView = view.findViewById(R.id.postFrag_image);
        authorPicture = view.findViewById(R.id.postFrag_authorPicture);
        authorUsername = view.findViewById(R.id.postFrag_author);

        likeButton = view.findViewById(R.id.postFrag_like);
        dislikeButton = view.findViewById(R.id.postFrag_dislike);

        // Init listeners
        authorPicture.setOnClickListener(this);
        authorUsername.setOnClickListener(this);

        likeButton.setOnClickListener(this);
        dislikeButton.setOnClickListener(this);

        return view;
    }

    public void updateDetails(Post post) {
        titleView.setText(post.getTitle());
        textView.setText(post.getText());

        // TODO: disable/enable title and text based on null value

        // Media
        switch (post.getMediaType()) {
            case Post.TEXT_TYPE:
                imageView.setVisibility(View.GONE);
                break;
            case Post.IMAGE_TYPE:
                byte[] decodedPicture = Base64.decode(post.getMedia(), Base64.DEFAULT);
                Bitmap pictureBitmap = BitmapFactory.decodeByteArray(decodedPicture, 0, decodedPicture.length);
                imageView.setImageBitmap(pictureBitmap);
                break;
            default:
                // TODO: add error case
                break;
        }

        // Author
        User author = post.getAuthor();
        authorUsername.setText(author.getUsername());

        Bitmap pictureBitmap;
        if (author.getProfile().getPicture() == null) {
            pictureBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_pfp);
        }
        else {
            byte[] decodedPicture = Base64.decode(author.getProfile().getPicture(), Base64.DEFAULT);
            pictureBitmap = BitmapFactory.decodeByteArray(decodedPicture, 0, decodedPicture.length);
        }

        RoundedBitmapDrawable authorDrawable = RoundedBitmapDrawableFactory.create(getResources(), pictureBitmap);
        authorDrawable.setCircular(true);
        authorPicture.setImageDrawable(authorDrawable);

        // Ratings
        likeCountView.setText(String.valueOf(post.getLikeCount()));
        dislikeCountView.setText(String.valueOf(post.getDislikeCount()));

        // TODO: set rating buttons' icons
    }

    @Override
    public void onClick(View v) {
        if (v == authorUsername || v == authorPicture) {
            onClickCallback.onClickCallback(AUTHOR_CLICK_KEY);
        }
        else if (v == likeButton) {
            onClickCallback.onClickCallback(LIKE_CLICK_KEY);
        }
        else if (v == dislikeButton) {
            onClickCallback.onClickCallback(DISLIKE_CLICK_KEY);
        }
    }

}
