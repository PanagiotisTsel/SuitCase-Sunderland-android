package com.example.assignment;

import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;


public class urldetailsdefiner extends AsyncTask<String, Void, Void> {
    public urldetailsdefiner(myList myList) {
    }
    @Override
    protected Void doInBackground(String... params) {
        String url = params[0];
        try {
            Document document = Jsoup.connect(url).get();
            String title = document.title();
            String imageUrl = document.select("meta[property=og:image]").attr("content");
            String description = document.select("meta[property=og:description]").attr("content");
            String price = document.select("meta[property=product:price:amount]").attr("content");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
