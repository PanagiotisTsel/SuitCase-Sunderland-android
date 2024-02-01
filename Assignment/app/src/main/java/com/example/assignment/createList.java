package com.example.assignment;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class createList extends Fragment {
    private SharedViewModel sharedViewModel;
    private EditText editTextTitle;
    private EditText editTextImageUrl;
    private EditText editTextPrice;
    private static final int REQUEST_IMAGE_PICK = 100;
    private ImageView imageView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        List<String> savedList = loadList(requireContext());
        sharedViewModel.setListItemData(savedList);
    }

    private List<String> loadList(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String json = preferences.getString("itemList", "");
        if (!json.isEmpty()) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<String>>() {
            }.getType();
            return gson.fromJson(json, type);
        } else {
            return new ArrayList<>();
        }
    }

    private void saveToCache(Bitmap bitmap, String fileName) {
        File cacheDir = getContext().getCacheDir();
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        File file = new File(cacheDir, fileName + ".jpg");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_list, container, false);
        Button submitButton = view.findViewById(R.id.buttonSubmit);
        editTextTitle = view.findViewById(R.id.editTextTitle);
        editTextImageUrl = view.findViewById(R.id.editTextImageUrl);
        editTextPrice = view.findViewById(R.id.editTextPrice);
        imageView = view.findViewById(R.id.img);


        submitButton.setOnClickListener(v -> {
            String title = editTextTitle.getText().toString();
            String price = editTextPrice.getText().toString();
            String imageUrl = editTextImageUrl.getText().toString();
            Drawable imgDrawable = imageView.getDrawable();

            if (!imageUrl.isEmpty() || (!title.isEmpty() && !price.isEmpty() && imgDrawable != null)) {
                if (imgDrawable instanceof BitmapDrawable) {
                    Bitmap bitmap = ((BitmapDrawable) imgDrawable).getBitmap();
                    String message = "Title: " + title + "\nPrice: " + price + "\nImage local: " + convertBit(bitmap);
                    dow_save_img(bitmap);
                    sharedViewModel.addListItem(message, convertBit(bitmap));
                    saveList(requireContext(), sharedViewModel.getListItemData().getValue());

                    Log.d("MyApp", "Before convertBit");
                    String encodedImage = convertBit(bitmap);
                    Log.d("MyApp", "After convertBit, encodedImage: " + encodedImage);

                    Toast.makeText(getContext(), "List added with stored image", Toast.LENGTH_SHORT).show();
                } else {
                    if (!imageUrl.isEmpty()) {
                        sharedViewModel.addListItem(title, imageUrl);
                        String message = "Title: " + title + "\nPrice: " + price + "\nImage URL: " + imageUrl;
                        dow_save_img(Uri.parse(imageUrl));
                        sharedViewModel.addListItem(message, imageUrl);
                        saveList(requireContext(), sharedViewModel.getListItemData().getValue());
                        Toast.makeText(getContext(), "List added with image URL", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Image URL should be provided", Toast.LENGTH_SHORT).show();
                    }
                }
            } else if (title.isEmpty() || price.isEmpty()) {
                Toast.makeText(getContext(), "Title and price need to be filled", Toast.LENGTH_SHORT).show();
            }
        });

        return view;

}


    private void imgUri(Uri imageUri) {
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(requireContext().getContentResolver().openInputStream(imageUri));
            saveToCache(bitmap, "placeholder_image");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("img", "no load");

        }
    }

    private void dow_save_img(Object image) {
        if (image instanceof Bitmap) {
            Bitmap bitmap = (Bitmap) image;
            saveToCache(bitmap, "placeholder_image");
        } else if (image instanceof Uri) {
            Uri imageUri = (Uri) image;
            imgUri(imageUri);
        } else {
            Log.e("img", "not existing: " + image.getClass().getName());
        }
    }

    private String convertBit(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
    private void galimg() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri selectedImageUri = data.getData();
                Picasso.get().load(selectedImageUri).into(imageView);

                Log.d("MyApp", "Loading image from URL: " + selectedImageUri);
                Picasso.get().load(selectedImageUri).into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.d("MyApp", "Image loaded successfully");
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("MyApp", "Error loading image: " + e.getMessage());
                    }
                });

            }
        }
    }


    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 1;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_SEND_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendMessage("1234567890");
            } else {
                Toast.makeText(getContext(), "Permission denied. Cannot send SMS.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendMessage(String phoneNumber) {
        String title = editTextTitle.getText().toString();
        String imageUrl = editTextImageUrl.getText().toString();
        String price = editTextPrice.getText().toString();
        String message = "Title: " + title + "\n\nPrice: " + price + "\n\nImage URL: " + imageUrl;
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        Toast.makeText(getContext(), "SMS sent!", Toast.LENGTH_SHORT).show();
    }

    private void saveList(Context context, List<String> itemList) {
        SharedPreferences preferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(itemList);
        editor.putString("itemList", json);
        editor.apply();
    }
}