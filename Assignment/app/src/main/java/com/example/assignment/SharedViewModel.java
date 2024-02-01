package com.example.assignment;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SharedViewModel extends ViewModel {
    private MutableLiveData<List<String>> listItemData = new MutableLiveData<>(new ArrayList<>());
    private MutableLiveData<Map<Integer, String>> imageUrlMap = new MutableLiveData<>(new HashMap<>());
    private MutableLiveData<Map<Integer, Boolean>> purchasedStatusMap = new MutableLiveData<>(new HashMap<>());

    public SharedViewModel() {
        listItemData.setValue(new ArrayList<>());
        imageUrlMap.setValue(new HashMap<>());
    }

    public LiveData<List<String>> getListItemData() {
        return listItemData;
    }

    public LiveData<Map<Integer, String>> getImageUrlMap() {
        return imageUrlMap;
    }
    public boolean getPurchasedStatus(int index) {
        Map<Integer, Boolean> statusMap = purchasedStatusMap.getValue();
        return statusMap != null && statusMap.containsKey(index) && statusMap.get(index);
    }
    public void addListItem(String data, String imageUrl) {
        List<String> currentList = listItemData.getValue();
        if (currentList == null) {
            currentList = new ArrayList<>();
        }
        currentList.add(data);
        listItemData.setValue(currentList);

        Map<Integer, String> currentImageUrlMap = imageUrlMap.getValue();
        if (currentImageUrlMap == null) {
            currentImageUrlMap = new HashMap<>();
        }
        currentImageUrlMap.put(currentList.size() - 1, imageUrl);
        imageUrlMap.setValue(currentImageUrlMap);
    }


    public void setListItemData(List<String> data) {
        listItemData.setValue(data);
    }


    public void removeListItemAndUpdate(int index) {
        List<String> currentList = listItemData.getValue();
        if (currentList != null && index >= 0 && index < currentList.size()) {
            int originalSize = currentList.size();
            currentList.remove(index);
            listItemData.postValue(new ArrayList<>(currentList));

            Map<Integer, String> currentImageUrlMap = imageUrlMap.getValue();
            if (currentImageUrlMap != null) {
                currentImageUrlMap.remove(index);
                imageUrlMap.postValue(new HashMap<>(currentImageUrlMap));
            }

            int newSize = currentList.size();
            Log.d("RemoveListItem", "Size before: " + originalSize + ", Size after: " + newSize);
        }
    }
    public List<String> getListItems() {
        return listItemData.getValue();
    }

    public void clearListData() {
        listItemData.setValue(new ArrayList<>());
    }
    public void editListItem(int index, String editedItem) {
        List<String> currentList = listItemData.getValue();
        if (currentList != null && index >= 0 && index < currentList.size()) {
            currentList.set(index, editedItem);
            listItemData.setValue(currentList);
        }
    }


    public void setPurchasedStatusMap(Map<Integer, Boolean> map) {
        purchasedStatusMap.postValue(map);
    }

    public void markItemAsPurchased(int index) {
        List<String> items = listItemData.getValue();
        Map<Integer, Boolean> statusMap = purchasedStatusMap.getValue();

        if (items != null && statusMap != null && index >= 0 && index < items.size()) {
            String item = items.get(index);
            String updatedItem = markAsPurchased(item, true);
            items.set(index, updatedItem);

            statusMap.put(index, true);

            listItemData.setValue(items);
            purchasedStatusMap.postValue(statusMap);
        }
    }

    public void markItemAsUnpurchased(int index) {
        List<String> currentList = listItemData.getValue();
        Map<Integer, Boolean> statusMap = purchasedStatusMap.getValue();

        if (currentList != null && statusMap != null && index >= 0 && index < currentList.size()) {
            String item = currentList.get(index);
            String updatedItem = markAsPurchased(item, false);
            currentList.set(index, updatedItem);

            statusMap.put(index, false);

            listItemData.setValue(currentList);
            purchasedStatusMap.postValue(statusMap);
        }
    }

    private String markAsPurchased(String item, boolean purchased) {
        String[] parts = item.split("\n");

        StringBuilder updatedItemBuilder = new StringBuilder();
        for (int i = 0; i < Math.min(parts.length, 2); i++) {
            updatedItemBuilder.append(parts[i]).append("\n");
        }
        updatedItemBuilder.append(purchased ? "Purchased: true" : "Not Purchased");
        for (int i = 2; i < parts.length; i++) {
            updatedItemBuilder.append("\n").append(parts[i]);
        }

        return updatedItemBuilder.toString();
    }

    public void addImageUrl(int index, String imageUrl) {
        Map<Integer, String> currentImageUrlMap = imageUrlMap.getValue();
        if (currentImageUrlMap != null) {
            currentImageUrlMap.put(index, imageUrl);
            imageUrlMap.setValue(currentImageUrlMap);
        }
    }

}
