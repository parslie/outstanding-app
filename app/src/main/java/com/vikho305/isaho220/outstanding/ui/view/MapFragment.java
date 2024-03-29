package com.vikho305.isaho220.outstanding.ui.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolClickListener;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.utils.BitmapUtils;
import com.vikho305.isaho220.outstanding.R;
import com.vikho305.isaho220.outstanding.data.Post;
import com.vikho305.isaho220.outstanding.ui.viewmodel.ContextualViewModelFactory;
import com.vikho305.isaho220.outstanding.ui.viewmodel.MapViewModel;
import com.vikho305.isaho220.outstanding.util.Resource;
import com.vikho305.isaho220.outstanding.util.Status;

import java.util.List;
import java.util.Objects;

public class MapFragment extends Fragment implements OnMapReadyCallback, View.OnClickListener {

    private static final String IMAGE_ICON = "image", TEXT_ICON = "text";
    private static final float ICON_SIZE = 1.5f;

    private FloatingActionButton fab;
    private MapView mapView;
    private MapboxMap mapboxMap;
    private SymbolManager postManager;
    private LocationComponent locationComponent;

    private MapViewModel viewModel;

    public MapFragment() {
        // Required empty public constructor
    }

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        setRetainInstance(true);

        fab = view.findViewById(R.id.mapFab);
        fab.setOnClickListener(this);
        mapView = view.findViewById(R.id.mapMap);
        mapView.getMapAsync(this);

        initViewModel();
        return view;
    }

    private void initViewModel() {
        ContextualViewModelFactory viewModelFactory = new ContextualViewModelFactory(requireContext());
        viewModel = new ViewModelProvider(requireActivity(), viewModelFactory).get(MapViewModel.class);

        viewModel.getPosts().observe(requireActivity(), new Observer<Resource<List<Post>>>() {
            @Override
            public void onChanged(Resource<List<Post>> listResource) {
                if (listResource.getStatus() == Status.SUCCESS) {
                    postManager.deleteAll();
                    if (postManager != null) {
                        for (Post post : listResource.getData()) {
                            LatLng postLatLng = new LatLng(post.getLatitude(), post.getLongitude());
                            SymbolOptions postSymbol = new SymbolOptions()
                                    .withLatLng(postLatLng)
                                    .withIconSize(ICON_SIZE);

                            switch (post.getMediaType()) {
                                case Post.IMAGE_TYPE:
                                    postSymbol.withIconImage(IMAGE_ICON);
                                    break;
                                case Post.TEXT_TYPE:
                                    postSymbol.withIconImage(TEXT_ICON);
                                    break;
                            }

                            postManager.create(postSymbol);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v == fab) {
            Intent intent = new Intent(requireContext(), PostCreationActivity.class);
            startActivity(intent);
        }
    }

    // Map methods

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;

        mapboxMap.setStyle(Style.LIGHT, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                // Init symbol icons
                Bitmap textIcon = Objects.requireNonNull(BitmapUtils.getBitmapFromDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_text_24dp)));
                Bitmap imageIcon = Objects.requireNonNull(BitmapUtils.getBitmapFromDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_image_24dp)));
                style.addImage(TEXT_ICON, textIcon, true);
                style.addImage(IMAGE_ICON, imageIcon, true);

                // Init symbol managers
                postManager = new SymbolManager(mapView, mapboxMap, style);
                postManager.setIconAllowOverlap(true);
                postManager.setTextAllowOverlap(true);

                // Init symbol click listeners
                postManager.addClickListener(new OnSymbolClickListener() {
                    @Override
                    public void onAnnotationClick(Symbol symbol) {
                        LatLng latLng = symbol.getLatLng();
                        double lat = latLng.getLatitude();
                        double lng = latLng.getLongitude();

                        // TODO: request going to activity of post at coordinates
                    }
                });

                viewModel.fetchPosts(100);
                enableLocationComponent(style);
            }
        });
    }

    private void enableLocationComponent(@NonNull Style style) {
        LocationComponentOptions customLocationComponentOptions = LocationComponentOptions.builder(requireContext())
                .elevation(5)
                .accuracyAlpha(.25f)
                .accuracyColor(Color.RED)
                .build();

        LocationComponentActivationOptions locationComponentActivationOptions =
                LocationComponentActivationOptions.builder(requireContext(), style)
                        .locationComponentOptions(customLocationComponentOptions)
                        .build();

        // Permission check
        if ((ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) | ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
            return;
        }

        // Set up location component
        locationComponent = mapboxMap.getLocationComponent();
        locationComponent.activateLocationComponent(locationComponentActivationOptions);
        locationComponent.setLocationComponentEnabled(true);
        locationComponent.setCameraMode(CameraMode.TRACKING);
        locationComponent.setRenderMode(RenderMode.COMPASS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int i = 0; i < permissions.length; i++) {
            String permission = permissions[i];
            int result = grantResults[i];

            if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION) && result == PackageManager.PERMISSION_GRANTED ||
                    permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION) && result == PackageManager.PERMISSION_GRANTED) {
                mapboxMap.getStyle(new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        enableLocationComponent(style);
                    }
                });
            }
            else {
                return; // TODO: add more extensive handling
            }
        }
    }

    // Essential lifecycle methods

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
    }
}