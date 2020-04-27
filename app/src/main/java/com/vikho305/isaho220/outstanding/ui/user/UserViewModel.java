package com.vikho305.isaho220.outstanding.ui.user;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import androidx.annotation.AttrRes;
import androidx.annotation.Size;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.vikho305.isaho220.outstanding.JsonParameterRequest;
import com.vikho305.isaho220.outstanding.R;
import com.vikho305.isaho220.outstanding.database.tables.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class UserViewModel extends ViewModel {

    private MutableLiveData<User> user;
    private MutableLiveData<float[]> userColor;
    private MutableLiveData<RoundedBitmapDrawable> userPicture;
    private MutableLiveData<String> userDescription;

    public UserViewModel() {
        user = new MutableLiveData<>();
        userColor = new MutableLiveData<>();
        userPicture = new MutableLiveData<>();
        userDescription = new MutableLiveData<>();
    }

    public void setUser(User user) {
        this.user.setValue(user);

        byte[] decodedPicture = Base64.decode(user.getPicture(), Base64.DEFAULT);
        Bitmap pictureBitmap = BitmapFactory.decodeByteArray(decodedPicture, 0, decodedPicture.length);
        RoundedBitmapDrawable roundedPicture = RoundedBitmapDrawableFactory.create(null, pictureBitmap);
        roundedPicture.setCircular(true);
        userPicture.setValue(roundedPicture);

        userColor.setValue(new float[] {
                360 * (float) user.getHue(),
                (float) user.getSaturation(),
                (float) user.getLightness()
        });

        userDescription.setValue(user.getDescription());
    }

    public LiveData<User> getUser() {
        return user;
    }

    public void setUserColor(float hue, float saturation, float lightness) {
        User user = this.user.getValue();
        user.setHue(hue);
        user.setSaturation(saturation);
        user.setLightness(lightness);

        this.userColor.setValue(new float[] {
                hue * 360,
                saturation,
                lightness
        });
    }

    public LiveData<float[]> getUserColor() {
        return userColor;
    }

    public void setUserPicture(Bitmap pictureBitmap) {
        int bitmapWidth = pictureBitmap.getWidth();
        int bitmapHeight = pictureBitmap.getHeight();
        int bitmapSize = Math.min(bitmapWidth, bitmapHeight);

        int croppedOffsetX = (bitmapWidth - bitmapSize) / 2;
        int croppedOffsetY = (bitmapHeight - bitmapSize) / 2;
        Bitmap croppedBitmap = Bitmap.createBitmap(pictureBitmap, croppedOffsetX, croppedOffsetY, bitmapSize, bitmapSize);
        RoundedBitmapDrawable roundedPicture = RoundedBitmapDrawableFactory.create(null, croppedBitmap);
        roundedPicture.setCircular(true);
        userPicture.setValue(roundedPicture);

        // Set user field
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] imageByteArray = stream.toByteArray();

        User user = this.user.getValue();
        user.setPicture(Base64.encodeToString(imageByteArray, Base64.DEFAULT));
    }

    public LiveData<RoundedBitmapDrawable> getUserPicture() {
        return userPicture;
    }

    public void setUserDescription(String description) {
        userDescription.setValue(description);

        User user = this.user.getValue();
        user.setDescription(description);
    }

    public LiveData<String> getUserDescription() {
        return userDescription;
    }

    // Server actions

    // TODO: check if moving authToken to constructor causes problems
    public void fetchUser(Context context, String userId, final String authToken) {
        String url = context.getResources().getString(R.string.get_user_url, userId);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Gson gson = new Gson();
                        User newUser = gson.fromJson(response.toString(), User.class);
                        setUser(newUser);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders()  {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + authToken);
                return headers;
            }
        };

        Volley.newRequestQueue(context).add(request);
    }

    public void saveUserProfile(Context context, final String authToken) throws JSONException {
        String url = context.getResources().getString(R.string.edit_user_url);
        User user = this.user.getValue();

        JSONObject parameters = new JSONObject();
        parameters.put("description", user.getDescription());
        parameters.put("picture", user.getPicture());
        parameters.put("hue", user.getHue());
        parameters.put("saturation", user.getSaturation());
        parameters.put("lightness", user.getLightness());

        JsonParameterRequest request = new JsonParameterRequest(
                Request.Method.POST,
                url,
                parameters,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders()  {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + authToken);
                return headers;
            }
        };

        Volley.newRequestQueue(context).add(request);
    }
}