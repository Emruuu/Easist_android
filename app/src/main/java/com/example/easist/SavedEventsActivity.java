package com.example.easist;

import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_events);

        recyclerView = findViewById(R.id.rv_saved_events);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        savedItems = loadSavedItems();

        adapter = new SavedEventsAdapter(savedItems, this::removeItem);
        recyclerView.setAdapter(adapter);

        Button btClearAll = findViewById(R.id.bt_clear_all);
        btClearAll.setOnClickListener(v -> clearAllEvents());
    }

    private List<SavedItem> loadSavedItems() {
        List<SavedItem> items = new ArrayList<>();
        String jsonString = prefs.getString("saved_items", "[]");

        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String type = obj.getString("type");
                String title = obj.getString("title");
                String date = obj.optString("date", "");
                String time = obj.optString("time", "");
                Long eventId = obj.has("eventId") && !obj.isNull("eventId") ? obj.getLong("eventId") : null;
                items.add(new SavedItem(type, title, date, time, eventId));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }

    private void saveItems() {
        try {
            JSONArray jsonArray = new JSONArray();
            for (SavedItem item : savedItems) {
                JSONObject obj = new JSONObject();
                obj.put("type", item.getType());
                obj.put("title", item.getTitle());
                obj.put("date", item.getDate());
                obj.put("time", item.getTime());
                Long eventId = item.getEventId();
                obj.put("eventId", eventId != null ? eventId : JSONObject.NULL);
                jsonArray.put(obj);
            }
            prefs.edit().putString("saved_items", jsonArray.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removeItem(int position) {
        SavedItem item = savedItems.get(position);

        // Usuwamy z kalendarza jeśli posiada eventId
        Long eventId = item.getEventId();
        if (eventId != null) {
            deleteCalendarEvent(eventId);
        }

        savedItems.remove(position);
        adapter.notifyItemRemoved(position);
        saveItems();
    }

    private void deleteCalendarEvent(long eventId) {
        try {
            ContentResolver cr = getContentResolver();
            Uri deleteUri = Uri.withAppendedPath(CalendarContract.Events.CONTENT_URI, String.valueOf(eventId));
            int rows = cr.delete(deleteUri, null, null);

            if (rows > 0) {
                Toast.makeText(this, "Wydarzenie usunięte", Toast.LENGTH_SHORT).show();
                Toast.makeText(this, "Usunięto również z kalendarza", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Nie znaleziono wydarzenia w kalendarzu", Toast.LENGTH_SHORT).show();
            }
        } catch (SecurityException se) {
            se.printStackTrace();
            Toast.makeText(this, "Brak uprawnień do usuwania z kalendarza", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Błąd przy usuwaniu z kalendarza: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void clearAllEvents() {
        savedItems.clear();
        adapter.notifyDataSetChanged();
        saveItems();
        Toast.makeText(this, "Wyczyszczono historię", Toast.LENGTH_SHORT).show();
    }
}
