package com.example.easist;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.AlarmClock;
import android.provider.CalendarContract;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {
    protected static final int RESULT_SPEECH = 1;

    private final String API_URL = "Your api URL";
    private final String API_KEY = "Your api KEY";
    private Button bt_save, btHistory;
    private ImageButton ibt_talk;
    private EditText et_prompt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bt_save = findViewById(R.id.bt_save);
        ibt_talk = findViewById(R.id.ibt_talk);
        et_prompt = findViewById(R.id.et_prompt);

        if (!hasPermissions()) {
            requestNecessaryPermissions();
        } else {
            ibt_talk.setOnClickListener(v -> startSpeechRecognition());

            bt_save.setOnClickListener(v -> {
                String prompt = et_prompt.getText().toString();
                sendTextToApi(prompt);
            });
            btHistory = findViewById(R.id.bt_history);
            btHistory.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, SavedEventsActivity.class);
                startActivity(intent);
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_SPEECH && resultCode == RESULT_OK && data != null) {
            List<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String recognizedText = result.get(0);
                sendTextToApi(recognizedText);
            }
        }
    }

    private boolean hasPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestNecessaryPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_CALENDAR,
                        Manifest.permission.READ_CALENDAR
                },
                123
        );
    }

    private void startSpeechRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pl-PL");

        try {
            startActivityForResult(intent, RESULT_SPEECH);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getApplicationContext(), "Twoje urządzenie nie wspiera rozpoznawania mowy", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    private void sendTextToApi(String text) {
        new Thread(() -> {
            try {
                String promptDate = "Dzisiejsza data: " + LocalDate.now().toString() +
                        " Dzisiejsza godzina: " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) +
                        ". ";
                String finalText = promptDate + text;

                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("accept", "application/json");
                conn.setRequestProperty("x-api-key", API_KEY);
                conn.setDoOutput(true);

                JSONObject jsonRequest = new JSONObject();
                jsonRequest.put("text", finalText);

                OutputStream os = conn.getOutputStream();
                os.write(jsonRequest.toString().getBytes("UTF-8"));
                os.close();

                Scanner inStream = new Scanner(conn.getInputStream());
                StringBuilder response = new StringBuilder();
                while (inStream.hasNextLine()) {
                    response.append(inStream.nextLine());
                }
                inStream.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                Log.d("API_RESPONSE", jsonResponse.toString());

                String action = jsonResponse.getString("action");
                JSONArray keywordsArray = jsonResponse.getJSONArray("title_keywords");
                List<String> titleKeywords = new ArrayList<>();
                for (int i = 0; i < keywordsArray.length(); i++) {
                    titleKeywords.add(keywordsArray.getString(i));
                }
                String title = jsonResponse.getString("title");
                String description = jsonResponse.getString("description");
                String date = jsonResponse.getString("date");
                String time = jsonResponse.getString("time");
                String type = jsonResponse.optString("type", "event");
                JSONArray remindersJson = jsonResponse.optJSONArray("reminders");
                List<Integer> reminders = new ArrayList<>();
                if (remindersJson != null) {
                    for (int i = 0; i < remindersJson.length(); i++) {
                        reminders.add(remindersJson.getInt(i));
                    }
                } else {
                    reminders.add(60);
                }

                runOnUiThread(() -> handleEventType(action, titleKeywords, type, title, description, date, time, reminders));

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, "Błąd: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }

    private void handleEventType(String action,List<String> titleKeywords, String type, String title,String description, String date, String time, List<Integer> reminders) {
        if ("create".equals(action)) {
            switch (type) {
                case "event":
                    addEventToCalendar(title, date, time, reminders);
                    break;
                case "alarm":
                    setAlarm(title, time);
                    break;
                case "note":
                    saveNote(type, title, description);
                    break;
                default:
                    Toast.makeText(this, "Nieznany typ: " + type, Toast.LENGTH_SHORT).show();
            }
        } else if ("edit".equals(action)) {
            switch (type) {
                case "event":
                    editEventInCalendar(titleKeywords, title, date, time, reminders);
                    break;
                case "note":
                    editNote(titleKeywords, title ,description);
                    break;
                case "alarm":
                    Toast.makeText(this, "Edycja alarmów nie jest obsługiwana", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(this, "Nieznany typ: " + type, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Nieznana akcja: " + action, Toast.LENGTH_SHORT).show();
        }
    }

    private void addEventToCalendar(String title, String date, String time, List<Integer> reminders) {
        try {
            String[] dateParts = date.split("-");
            String[] timeParts = time.split(":");

            Calendar beginTime = Calendar.getInstance();
            beginTime.set(
                    Integer.parseInt(dateParts[0]),
                    Integer.parseInt(dateParts[1]) - 1,
                    Integer.parseInt(dateParts[2]),
                    Integer.parseInt(timeParts[0]),
                    Integer.parseInt(timeParts[1])
            );

            Calendar endTime = (Calendar) beginTime.clone();
            endTime.add(Calendar.HOUR_OF_DAY, 1);

            long startMillis = beginTime.getTimeInMillis();
            long endMillis = endTime.getTimeInMillis();

            long calendarId = getPrimaryCalendarId();
            if (calendarId == -1) {
                runOnUiThread(() -> Toast.makeText(this, "Brak dostępnego kalendarza!", Toast.LENGTH_LONG).show());
                return;
            }

            ContentResolver cr = getContentResolver();
            ContentValues values = new ContentValues();
            values.put(CalendarContract.Events.DTSTART, startMillis);
            values.put(CalendarContract.Events.DTEND, endMillis);
            values.put(CalendarContract.Events.TITLE, title);
            values.put(CalendarContract.Events.DESCRIPTION, "Dodane przez Easist");
            values.put(CalendarContract.Events.CALENDAR_ID, calendarId);
            values.put(CalendarContract.Events.EVENT_TIMEZONE, "Europe/Warsaw");

            Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);

            if (uri != null) {
                long eventId = Long.parseLong(uri.getLastPathSegment());

                // Ustawiamy wszystkie przypomnienia
                for (int minutesBefore : reminders) {
                    ContentValues reminderValues = new ContentValues();
                    reminderValues.put(CalendarContract.Reminders.EVENT_ID, eventId);
                    reminderValues.put(CalendarContract.Reminders.MINUTES, minutesBefore);
                    reminderValues.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
                    cr.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues);
                }

                saveItem("event", title, null, date, time, eventId);
                runOnUiThread(() ->
                        Toast.makeText(this, "Wydarzenie i przypomnienia dodane do kalendarza!", Toast.LENGTH_SHORT).show()
                );
            } else {
                runOnUiThread(() ->
                        Toast.makeText(this, "Błąd przy dodawaniu wydarzenia!", Toast.LENGTH_SHORT).show()
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() ->
                    Toast.makeText(this, "Błąd przy tworzeniu wydarzenia: " + e.getMessage(), Toast.LENGTH_LONG).show()
            );
        }
    }

    private void editEventInCalendar(List<String> titleKeywords, String newTitle, String newDate, String newTime, List<Integer> newReminders) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String jsonString = prefs.getString("saved_items", "[]");

        List<SavedItem> eventItems = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String type = obj.getString("type");
                if (!type.equals("event")) continue; // pomijamy notatki itp

                String title = obj.getString("title");
                String date = obj.optString("date", "");
                String time = obj.optString("time", "");
                Long eventId = obj.has("eventId") && !obj.isNull("eventId") ? obj.getLong("eventId") : null;

                if (eventId != null) {
                    eventItems.add(new SavedItem(type, title, date, time, eventId));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
            boolean found = false;

            for (SavedItem item : eventItems) {
                if (!"event".equals(item.getType())) continue;

                String existingTitle = item.getTitle().toLowerCase();
                for (String keyword : titleKeywords) {
                    if (existingTitle.contains(keyword.toLowerCase())) {
                        Long eventId = item.getEventId();
                        if (eventId != null) {

                            // Sprawdzamy, co faktycznie chcemy zmienić
                            String today = LocalDate.now().toString();
                            String updatedDate;
                            if (!newDate.isEmpty() && !newDate.equals(item.getDate()) && !newDate.equals(today)) {
                                updatedDate = newDate;
                            } else {
                                updatedDate = item.getDate();
                            }
                            String updatedTitle = (!newTitle.isEmpty() && !newTitle.equalsIgnoreCase(item.getTitle())) ? newTitle : item.getTitle();
                            String updatedTime = (!newTime.isEmpty() && !newTime.equals(item.getTime())) ? newTime : item.getTime();
                            List<Integer> updatedReminders = (newReminders != null && !newReminders.equals(Collections.singletonList(1440))) ? newReminders : null;

                            updateCalendarEvent(eventId, updatedTitle, updatedDate, updatedTime, updatedReminders);

                            // Aktualizacja lokalnej historii
                            item.setTitle(updatedTitle);
                            item.setDate(updatedDate);
                            item.setTime(updatedTime);

                            updateItemInHistory(item);

                            runOnUiThread(() ->
                                    Toast.makeText(this, "Wydarzenie zaktualizowane", Toast.LENGTH_SHORT).show()
                            );

                            found = true;
                            break;
                        }
                    }
                }
                if (found) break;
            }

            if (!found) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Nie znaleziono wydarzenia do edycji", Toast.LENGTH_SHORT).show()
                );
            }
        }
    private void updateCalendarEvent(long eventId, String newTitle, String newDate, String newTime, List<Integer> newReminders) {
        try {
            String[] dateParts = newDate.split("-");
            String[] timeParts = newTime.split(":");

            Calendar beginTime = Calendar.getInstance();
            beginTime.set(
                    Integer.parseInt(dateParts[0]),
                    Integer.parseInt(dateParts[1]) - 1,
                    Integer.parseInt(dateParts[2]),
                    Integer.parseInt(timeParts[0]),
                    Integer.parseInt(timeParts[1])
            );

            Calendar endTime = (Calendar) beginTime.clone();
            endTime.add(Calendar.HOUR_OF_DAY, 1);

            long startMillis = beginTime.getTimeInMillis();
            long endMillis = endTime.getTimeInMillis();

            ContentResolver cr = getContentResolver();
            ContentValues values = new ContentValues();
            values.put(CalendarContract.Events.TITLE, newTitle);
            values.put(CalendarContract.Events.DTSTART, startMillis);
            values.put(CalendarContract.Events.DTEND, endMillis);

            Uri updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId);
            int rows = cr.update(updateUri, values, null, null);

            // najpierw usuń stare reminders
            Uri remindersUri = CalendarContract.Reminders.CONTENT_URI;
            cr.delete(remindersUri, CalendarContract.Reminders.EVENT_ID + " = ?", new String[]{String.valueOf(eventId)});

            // dodaj nowe reminders
            for (Integer minutes : newReminders) {
                ContentValues reminderValues = new ContentValues();
                reminderValues.put(CalendarContract.Reminders.MINUTES, minutes);
                reminderValues.put(CalendarContract.Reminders.EVENT_ID, eventId);
                reminderValues.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
                cr.insert(remindersUri, reminderValues);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void updateItemInHistory(SavedItem updatedItem) {
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String jsonString = prefs.getString("saved_items", "[]");
            JSONArray jsonArray = new JSONArray(jsonString);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                Long eventId = obj.has("eventId") && !obj.isNull("eventId") ? obj.getLong("eventId") : null;

                if (eventId != null && eventId.equals(updatedItem.getEventId())) {
                    obj.put("title", updatedItem.getTitle());
                    obj.put("date", updatedItem.getDate());
                    obj.put("time", updatedItem.getTime());
                    break;
                }
            }

            prefs.edit().putString("saved_items", jsonArray.toString()).apply();
            Log.d("updateItemInHistory", "Zaktualizowano w historii: " + updatedItem.getTitle());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void setAlarm(String title, String time) {
        try {
            if (time == null || time.isEmpty() || !time.contains(":")) {
                Toast.makeText(this, "Brak godziny dla budzika.", Toast.LENGTH_SHORT).show();
                return;
            }

            String[] timeParts = time.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            Log.d("Easist", "Ustawiam budzik: " + hour + ":" + minute + " - " + title);

            saveItem("alarm", title,null ,null , time, null);

            Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM)
                    .putExtra(AlarmClock.EXTRA_HOUR, hour)
                    .putExtra(AlarmClock.EXTRA_MINUTES, minute)
                    .putExtra(AlarmClock.EXTRA_MESSAGE, title);

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "Brak aplikacji do obsługi alarmu", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Błąd przy ustawianiu alarmu: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    private void saveNote(String type, String title, String description) {
        saveItem("note", title, description, null, null, null);
        runOnUiThread(() ->
                Toast.makeText(this, "Dodano notatkę", Toast.LENGTH_SHORT).show()
        );
    }
    private void editNote(List<String> titleKeywords, String newTitle, String newDescription) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String jsonString = prefs.getString("saved_items", "[]");

        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            boolean found = false;
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                if ("note".equals(obj.getString("type"))) {
                    String existingTitle = obj.getString("title").toLowerCase();
                    for (String keyword : titleKeywords) {
                        if (existingTitle.contains(keyword.toLowerCase())) {

                            // aktualizujemy tylko jeśli nowe wartości są sensowne
                            if (newDescription != null && !newDescription.isEmpty()) {
                                obj.put("description", newDescription);
                            }

                            found = true;
                            break;
                        }
                    }
                }
                if (found) break;
            }

            if (found) {
                prefs.edit().putString("saved_items", jsonArray.toString()).apply();
                runOnUiThread(() ->
                        Toast.makeText(this, "Notatka zaktualizowana", Toast.LENGTH_SHORT).show()
                );
            } else {
                runOnUiThread(() ->
                        Toast.makeText(this, "Nie znaleziono notatki do edycji", Toast.LENGTH_SHORT).show()
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() ->
                    Toast.makeText(this, "Błąd przy edycji notatki: " + e.getMessage(), Toast.LENGTH_LONG).show()
            );
        }
    }
    private long getPrimaryCalendarId() {
        String[] projection = new String[]{
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.IS_PRIMARY
        };

        Cursor cursor = getContentResolver().query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                null,
                null,
                null
        );

        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(0);
                    int isPrimary = cursor.getInt(1);
                    if (isPrimary == 1) {
                        return id;
                    }
                }
                cursor.moveToFirst();
                return cursor.getLong(0);
            } finally {
                cursor.close();
            }
        }
        return -1;
    }
    private void saveItem(String type, String title,String description , String date, String time, Long eventId) {
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String jsonString = prefs.getString("saved_items", "[]");
            JSONArray jsonArray = new JSONArray(jsonString);

            JSONObject obj = new JSONObject();
            obj.put("type", type);
            obj.put("title", title);
            obj.put("description", description != null ? description : JSONObject.NULL);
            obj.put("date", date);
            obj.put("time", time);
            obj.put("eventId", eventId != null ? eventId : JSONObject.NULL);

            jsonArray.put(obj);

            prefs.edit().putString("saved_items", jsonArray.toString()).apply();

            Log.d("SavedItem", "Zapisano: " + obj.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
