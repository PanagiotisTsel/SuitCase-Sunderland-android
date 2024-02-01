package com.example.assignment;
import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.provider.MediaStore;
import android.telephony.SmsManager;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import androidx.recyclerview.widget.RecyclerView;


//upload a downloaded img


public class myList extends Fragment {
    private LinearLayout linearLayout;
    private SharedViewModel sharedViewModel;
    private boolean isDataLoaded = false;
    private static final int SMS_PERMISSION_REQUEST_CODE = 100;
    private static final int DURATION_OF_ANIMATION = 300;
    private Uri selectedImageUri;
//btnImage

    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup con, Bundle savedInstanceState) {
        View view = inf.inflate(R.layout.fragment_my_list, con, false);
        Button addButton = view.findViewById(R.id.buttonAdd);
        EditText urlText = view.findViewById(R.id.editTextURL);
        Button btnAddNewItemGallery = view.findViewById(R.id.btnImage);
        btnAddNewItemGallery.setOnClickListener(v -> openAddNewItemDialog());
        addButton.setOnClickListener(v -> {
            String url = urlText.getText().toString().trim();
            if (!TextUtils.isEmpty(url)) {
                new URLDetailsDefiner(this).execute(url);
            } else {
                Toast.makeText(getContext(), "Please enter a valid URL", Toast.LENGTH_SHORT).show();
            }
        });
        linearLayout = view.findViewById(R.id.linearLayout);
        if (!isDataLoaded) {
            sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
            List<String> savedList = loadList(requireContext());
            sharedViewModel.setListItemData(savedList);
            isDataLoaded = true;
        }
        sharedViewModel.getListItemData().observe(getViewLifecycleOwner(), itemList -> {
            UI(itemList);
        });
        return view;
    }
    private void addNewItem(String newItem) {
        String[] parts = newItem.split("\n");

        if (parts.length >= 3) {
            String title = parts[0].trim();
            String price = parts[1].trim();
            String imageUrl = parts[2].replace("Image URL: ", "").trim();

            List<String> currentList = sharedViewModel.getListItemData().getValue();
            if (currentList != null) {
                currentList.add(newItem);
                sharedViewModel.setListItemData(currentList);
                UI(currentList);
                saveList(requireContext(), currentList);
                sharedViewModel.addImageUrl(currentList.size() - 1, imageUrl);
                Toast.makeText(getContext(), "Item added successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Unable to add item", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Invalid item format", Toast.LENGTH_SHORT).show();
        }
    }
    private void openAddNewItemDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add New Item");
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_item, null);
        builder.setView(dialogView);

        EditText titleET = dialogView.findViewById(R.id.edtTitlegalery);
        EditText priceET = dialogView.findViewById(R.id.edtPricegalery);
        Button selectImageBtn = dialogView.findViewById(R.id.btnSelectImagegalery);
        ImageView imageView = dialogView.findViewById(R.id.imageViewSelectedImage);
        selectImageBtn.setOnClickListener(v -> openGalleryForImageSelection());

        builder.setPositiveButton("Save", (dialog, which) -> {
            String title = titleET.getText().toString().trim();
            String price = priceET.getText().toString().trim();
            if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(price)) {
                String newItem = title + "\n" + price + "\nImage URL: " + selectedImageUri;
                addNewItem(newItem);
                handleImageSelection(requireContext(), selectedImageUri, imageView);
                Toast.makeText(getContext(), "Item added successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Please enter title and price", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }
    private static final int GALLERY_REQUEST_CODE = 123;

    private void openGalleryForImageSelection() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data.getData();

            if (selectedImageUri != null) {
            }
        }
    }


    private void displayItem(String item, int index) {
        String title = extractTitle(item);
        String price = extractPrice(item);
        String imageUrl = extractImageUrl(item);
        boolean isNotPurchased = !sharedViewModel.getPurchasedStatus(index);

        LinearLayout itemLayout = new LinearLayout(requireContext());
        itemLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(itemLayout);

        TextView titleTextView = new TextView(requireContext());
        titleTextView.setText("Title: " + title);
        titleTextView.setTextColor(isNotPurchased ? Color.GREEN : Color.WHITE);
        itemLayout.addView(titleTextView);

        TextView priceTextView = new TextView(requireContext());
        priceTextView.setText("Price: " + price);
        Log.d("DisplayItem", "Extracted Price: " + price);
        priceTextView.setTextColor(isNotPurchased ? Color.GREEN : Color.WHITE);
        itemLayout.addView(priceTextView);

        ImageView imageView = new ImageView(requireContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(10, 0, 0, 0);
        imageView.setLayoutParams(layoutParams);
        itemLayout.addView(imageView);

        titleTextView.setTextColor(isNotPurchased ? Color.WHITE : Color.GREEN);
        priceTextView.setTextColor(isNotPurchased ? Color.WHITE : Color.GREEN);


        loadImage(imageView, imageUrl);

        Button markAsPurchasedButton = createButton("Purchased", v -> {
            sharedViewModel.markItemAsPurchased(index);
            UI(sharedViewModel.getListItemData().getValue());
            saveList(requireContext(), sharedViewModel.getListItemData().getValue());
            titleTextView.setTextColor(Color.GREEN);
            priceTextView.setTextColor(Color.GREEN);
            Toast.makeText(getContext(), "Product Purchased", Toast.LENGTH_SHORT).show();
        });

        Button markItemAsUnpurchased = createButton("Not Purchased", v -> {
            sharedViewModel.markItemAsUnpurchased(index);
            UI(sharedViewModel.getListItemData().getValue());
            saveList(requireContext(), sharedViewModel.getListItemData().getValue());
            titleTextView.setTextColor(Color.WHITE);
            priceTextView.setTextColor(Color.WHITE);
            Toast.makeText(getContext(), "Product Not Purchased", Toast.LENGTH_SHORT).show();
        });

        Button sendSMSButton = createButton("Send SMS", v -> showPhone(item));
        if (!isNotPurchased) {
            itemLayout.addView(markItemAsUnpurchased);
            itemLayout.addView(sendSMSButton);
        } else {
            itemLayout.addView(markAsPurchasedButton);
            itemLayout.addView(sendSMSButton);
        }

        Button editButton = createButton("Edit", v -> openEditDialog(item, index));
        itemLayout.addView(editButton);

        Button deleteButton = createButton("Delete", v -> confirmDeleteDialog(itemLayout, index));
        itemLayout.addView(deleteButton);

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SlideAnimation(itemLayout, index);
            }
        });
    }

    private void UI(List<String> itemList) {
        linearLayout.removeAllViews();
        for (int i = 0; i < itemList.size(); i++) {
            try {
                Log.d("ui updated", "show the saved items" + i);
                displayItem(itemList.get(i), i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void loadImage(ImageView imageView, String imageUrl) {
        Log.d("ImageLoading", "Loading image: " + imageUrl);

        if (imageUrl != null) {
            if (imageUrl.startsWith("http") || imageUrl.startsWith("https") || imageUrl.startsWith("content")) {
                Picasso.get().load(imageUrl).into(imageView);
            } else if (imageUrl.startsWith("data:image")) {
                String base64Image = imageUrl.substring(imageUrl.indexOf(",") + 1);
                byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
                imageView.setImageBitmap(BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length));
            } else {
                Log.e("ImageLoading", "Error: Unsupported URI scheme - " + imageUrl);
            }
        } else {
            Log.e("ImageLoading", "Error: Image URL is null");
        }
    }




    private void handleImageSelection(Context context, Uri selectedImageUri, ImageView imageView) {
        if (selectedImageUri != null) {
            Log.d("ImageLoading", "Loading image from URI: " + selectedImageUri.toString());

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), selectedImageUri);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                Log.e("ImageLoading", "Error loading image: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            Log.e("ImageLoading", "Error: Image URI is null");
            imageView.setImageResource(R.drawable.placeholder_image);
        }
    }


    private String getPathFromUri(Context context, Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String imagePath = cursor.getString(columnIndex);
            cursor.close();
            return imagePath;
        }

        return uri.getPath();
    }

    private void SlideAnimation(final View view, final int position) {
        ValueAnimator animator = ValueAnimator.ofInt(view.getLeft(), view.getRight());
        animator.setDuration(300);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int value = (Integer) valueAnimator.getAnimatedValue();
                view.setTranslationX(value);
            }
        });
        animator.start();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                removeListItem(position);
                Toast.makeText(getContext(), "Item Deleted", Toast.LENGTH_SHORT).show();
            }
        }, 300);
    }

    private void addNewItem(String url, String title, String price, String imageUrl) {
        String newItem = " " + title + "\n " + price + "\n" + imageUrl;
        List<String> currentList = sharedViewModel.getListItemData().getValue();
        if (currentList != null) {
            currentList.add(newItem);
            sharedViewModel.setListItemData(currentList);
            UI(currentList);
            saveList(requireContext(), currentList);
            sharedViewModel.addImageUrl(currentList.size() - 1, imageUrl);
            Toast.makeText(getContext(), "Item added successfully", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getContext(), "Unable to add item", Toast.LENGTH_SHORT).show();

        }
    }
    private static class URLDetailsDefiner extends AsyncTask<String, Void, Void> {
        private WeakReference<myList> myListReference;
        URLDetailsDefiner(myList myList) {
            myListReference = new WeakReference<>(myList);
        }

        @Override
        protected Void doInBackground(String... params) {
            String url = params[0];
            try {
                Document document = Jsoup.connect(url).get();
                myList myList = myListReference.get();
                if (myList != null) {
                    myList.getActivity().runOnUiThread(() -> {
                        String title = document.title();
                        String imageUrl = document.select("meta[property=og:image]").attr("content");
                        String price = document.select("meta[property=product:price:amount]").attr("content");
                        myList.addNewItem(url, title, price, imageUrl);
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
    private void showPhone(String item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Receivers phone Number");
        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_PHONE);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String phoneNumber = input.getText().toString().trim();
            if (!phoneNumber.isEmpty()) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST_CODE);
                } else {
                    sendSMS(phoneNumber, item);
                }
            } else {
                Toast.makeText(getContext(), "Fill the phone number", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void sendSMS(String phoneNumber, String message) {
        Log.d("SMS", "Sending SMS to: " + phoneNumber);
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        Log.d("SMS", "SMS sent!");
        Toast.makeText(getContext(), "SMS sent!", Toast.LENGTH_SHORT).show();
    }

    private List<String> loadList(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String json = preferences.getString("itemList", null);

        if (json != null) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<String>>() {
            }.getType();
            return gson.fromJson(json, listType);
        } else {
            return Collections.emptyList();
        }
    }

    private void saveList(Context context, List<String> itemList) {
        SharedPreferences preferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(itemList);
        editor.putString("itemList", json);
        editor.apply();
    }

    private String extractPrice(String item) {
        String[] parts = item.split("\n");

        for (int i = 0; i < parts.length; i++) {
            Log.d("Extracted Parts", "Part " + i + ": " + parts[i]);
        }
        if (parts.length > 1) {
            String potentialPrice = parts[1].trim();

            if (!potentialPrice.isEmpty()) {
                return potentialPrice;
            }
        }
        return "Price not available";
    }
    private String extractTitle(String item) {
        String[] parts = item.split("\n");
        return parts[0];
    }
    private Button createButton(String text, View.OnClickListener onClickListener) {
        Button button = new Button(requireContext());
        button.setText(text);
        button.setOnClickListener(onClickListener);
        return button;
    }
    private void confirmDeleteDialog(final LinearLayout itemLayout, final int index) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Confirm Delete");
        builder.setMessage("Are you sure you want to delete this item?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            createFadeOutAnimation(itemLayout, index);
            itemLayout.postDelayed(() -> {
                linearLayout.removeView(itemLayout);
            }, DURATION_OF_ANIMATION);
        });
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void createFadeOutAnimation(final View view, final int index) {
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        fadeOut.setDuration(DURATION_OF_ANIMATION);

        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
            }
        });

        fadeOut.start();
    }

    private void removeListItem(int index) {
        sharedViewModel.removeListItemAndUpdate(index);
        saveList(requireContext(), sharedViewModel.getListItemData().getValue());
    }

    private void openEditDialog(String listItem, int index) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Edit Item");
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.edit_item_dialog, null);
        builder.setView(dialogView);
        EditText titleET = dialogView.findViewById(R.id.editTextTitle);
        EditText priceET = dialogView.findViewById(R.id.editTextPrice);
        EditText imageurlText = dialogView.findViewById(R.id.editTextImageUrl);
        String[] parts = listItem.split("\n");
        titleET.setText(extractTitle(listItem));
        priceET.setText(parts.length > 1 ? parts[1] : "");
        imageurlText.setText(extractImageUrl(listItem));
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                String editedTitle = titleET.getText().toString().trim();
                String editedPrice = priceET.getText().toString().trim();
                String editedImageUrl = imageurlText.getText().toString().trim();
                String originalUrl = extractImageUrl(listItem);

                String editedItem = editedTitle + "\n" + editedPrice + "\nImage URL: " + editedImageUrl;
                sharedViewModel.editListItem(index, editedItem);
                saveList(requireContext(), sharedViewModel.getListItemData().getValue());

                String title = editedTitle;
                String imageUrl = editedImageUrl;
                String price = editedPrice;
                String message = "" + title + "\n" + price + "\n" + imageUrl;
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    private String extractImageUrl(String item) {
        String[] parts = item.split("\n");
        return parts[parts.length - 1].replace("Image URL: ", "");
    }
}