package com.example.easist;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SavedEventsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SavedEventsAdapter adapter;
    private List<SavedItem> savedItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_events);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        savedItems = loadSavedItems();
        adapter = new SavedEventsAdapter(savedItems);
        recyclerView.setAdapter(adapter);
    }

    private List<SavedItem> loadSavedItems() {
        List<SavedItem> items = new ArrayList<>();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String jsonString = prefs.getString("saved_items", "[]");

        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String type = obj.getString("type");
                String title = obj.getString("title");
                String date = obj.optString("date", "");
                String time = obj.optString("time", "");
                items.add(new SavedItem(type, title, date, time));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }
}
