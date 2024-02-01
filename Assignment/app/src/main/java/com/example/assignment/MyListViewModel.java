package com.example.assignment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class MyListViewModel extends ViewModel {
    private MutableLiveData<List<String>> listItems = new MutableLiveData<>();

    public LiveData<List<String>> getListItems() {
        return listItems;
    }

    public void setListItems(List<String> items) {
        listItems.setValue(items);
    }

    // Add other methods as needed
}
