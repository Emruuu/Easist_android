package com.example.easist;

import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
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
    private List<SavedItem> allItems;
    private boolean suppressToastsDuringBulkDelete = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_events);

        recyclerView = findViewById(R.id.rv_saved_events);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        allItems = loadSavedItems();
        savedItems = new ArrayList<>(allItems);

        adapter = new SavedEventsAdapter(savedItems, this::removeItem);
        recyclerView.setAdapter(adapter);

        Button btClearAll = findViewById(R.id.bt_clear_all);
        btClearAll.setOnClickListener(v -> clearAllEvents());

        Spinner spinnerFilter = findViewById(R.id.spinner_filter);
        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                applyFilter(selected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
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
                String description = obj.has("description") && !obj.isNull("description") ? obj.getString("description") : "";
                String date = obj.optString("date", "");
                String time = obj.optString("time", "");
                Long eventId = obj.has("eventId") && !obj.isNull("eventId") ? obj.getLong("eventId") : null;

                items.add(new SavedItem(type, title, description, date, time, eventId));
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
                obj.put("description", item.getDescription() != null ? item.getDescription() : JSONObject.NULL);
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

            if (!suppressToastsDuringBulkDelete) {
                if (rows > 0) {
                    Toast.makeText(this, "Wydarzenie usunięte z kalendarza", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Nie znaleziono wydarzenia w kalendarzu", Toast.LENGTH_SHORT).show();
                }
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
        suppressToastsDuringBulkDelete = true;

        for (SavedItem item : new ArrayList<>(savedItems)) {
            Long eventId = item.getEventId();
            if (eventId != null) {
                deleteCalendarEvent(eventId);
            }
        }

        suppressToastsDuringBulkDelete = false;

        savedItems.clear();
        adapter.notifyDataSetChanged();
        saveItems();
        Toast.makeText(this, "Wyczyszczono historię i powiązane wydarzenia z kalendarza", Toast.LENGTH_SHORT).show();
    }
    private void applyFilter(String filter) {
        List<SavedItem> filteredList = new ArrayList<>();

        for (SavedItem item : allItems) { // <-- używamy pełnej listy, a nie savedItems
            switch (filter) {
                case "Wszystkie":
                    filteredList.add(item);
                    break;
                case "Notatki":
                    if (item.getType().equalsIgnoreCase("note")) {
                        filteredList.add(item);
                    }
                    break;
                case "Wydarzenia":
                    if (item.getType().equalsIgnoreCase("event")) {
                        filteredList.add(item);
                    }
                    break;
                case "Alarmy":
                    if (item.getType().equalsIgnoreCase("alarm")) {
                        filteredList.add(item);
                    }
                    break;
            }
        }

        adapter.updateList(filteredList);
    }
}
