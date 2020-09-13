package com.vikho305.isaho220.outstanding.ui.view;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.CornerTreatment;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.vikho305.isaho220.outstanding.R;
import com.vikho305.isaho220.outstanding.data.User;
import com.vikho305.isaho220.outstanding.ui.viewmodel.ContextualViewModelFactory;
import com.vikho305.isaho220.outstanding.ui.viewmodel.LoginViewModel;
import com.vikho305.isaho220.outstanding.ui.viewmodel.ProfileViewModel;
import com.vikho305.isaho220.outstanding.util.Resource;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {
    private static final String USER_ID_ARG = "user_id";

    private ShapeableImageView profilePicture;
    private TextView username, description;
    private TextView followerCount, followingCount;
    private Button followButton;

    private String userId;
    private ProfileViewModel viewModel;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance(String userId) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(USER_ID_ARG, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        if (getArguments() != null) {
            userId = getArguments().getString(USER_ID_ARG);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profilePicture = view.findViewById(R.id.profilePicture);
        username = view.findViewById(R.id.profileName);
        description = view.findViewById(R.id.profileDescription);
        followerCount = view.findViewById(R.id.profileFollowerCount);
        followingCount = view.findViewById(R.id.profileFollowingCount);
        followButton = view.findViewById(R.id.profileFollowBtn);

        initViewModel();

        return view;
    }

    private void initViewModel() {
        ContextualViewModelFactory contextualViewModelFactory = new ContextualViewModelFactory(requireContext());
        viewModel = new ViewModelProvider(requireActivity(), contextualViewModelFactory).get(ProfileViewModel.class);

        viewModel.getUser().observe(requireActivity(), new Observer<Resource<User>>() {
            @Override
            public void onChanged(Resource<User> userResource) {
                switch (userResource.getStatus()) {
                    case SUCCESS:
                        User user = userResource.getData();
                        username.setText(user.getUsername());
                        description.setText(user.getDescription());
                        followerCount.setText(getString(R.string.follower_count, user.getFollowerCount()));
                        followingCount.setText(getString(R.string.following_count, user.getFollowingCount()));
                        break;
                }
            }
        });

        viewModel.fetchUser(userId);
        viewModel.fetchPosts(userId);
    }
}