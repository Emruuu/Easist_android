package com.example.easist;

import android.Manifest;
import android.content.ContentResolver;
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
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONObject;
import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.RecognitionListener;
import org.vosk.android.SpeechService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Scanner;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MainActivity extends AppCompatActivity implements RecognitionListener {

    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private EditText etPrompt;
    private Button btSave;
    private ImageButton ibtTalk;
    private TextView tv_view;
    private final String API_URL = "Your api URL";
    private final String API_KEY = "Your api KEY";
    private Model model;
    private SpeechService speechService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etPrompt = findViewById(R.id.et_prompt);
        btSave = findViewById(R.id.bt_save);
        ibtTalk = findViewById(R.id.ibt_talk);
        tv_view = findViewById(R.id.tv_view);

        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
        } else {
            initializeOrDownloadModel();
        }

        ibtTalk.setEnabled(false); // blokada zanim model gotowy

        btSave.setOnClickListener(v -> {
            String text = etPrompt.getText().toString();
            sendTextToApi(text);
        });

        ibtTalk.setOnClickListener(v -> {
            if (speechService != null) {
                speechService.stop();
                speechService = null;
                ibtTalk.setImageResource(android.R.drawable.ic_btn_speak_now); // ikonka mikrofonu
            } else {
                recognizeMicrophone();
                ibtTalk.setImageResource(android.R.drawable.ic_media_pause); // ikonka pauzy
            }
        });
    }
    private void initModel() {
        new Thread(() -> {
            try {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                String modelName = prefs.getString("vosk_model_name", null);

                if (modelName == null) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Brak zapisanej nazwy modelu!", Toast.LENGTH_LONG).show()
                    );
                    return;
                }

                File modelDir = new File(getExternalFilesDir(null), modelName);

                if (!modelDir.exists()) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Folder modelu nie istnieje!", Toast.LENGTH_LONG).show()
                    );
                    return;
                }

                Model model = new Model(modelDir.getAbsolutePath());

                runOnUiThread(() -> {
                    this.model = model;
                    Toast.makeText(this, "Model Vosk załadowany, możesz mówić.", Toast.LENGTH_LONG).show();
                    ibtTalk.setEnabled(true);
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Błąd ładowania modelu: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }
    private void initializeOrDownloadModel() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String modelName = prefs.getString("vosk_model_name", null);

        if (modelName == null) {
            showDownloadModelDialog();
        } else {
            initModel();
        }
    }

    private void showDownloadModelDialog() {
        String[] languages = {"Polski", "English"};
        String[] urls = {
                "https://alphacephei.com/vosk/models/vosk-model-small-pl-0.22.zip",
                "https://alphacephei.com/vosk/models/vosk-model-small-en-us-0.15.zip"
        };
        String[] modelNames = {"vosk-model-small-pl-0.22", "vosk-model-small-en-us-0.15"};

        new AlertDialog.Builder(this)
                .setTitle("Wybierz język modelu do pobrania")
                .setItems(languages, (dialog, which) -> {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                    prefs.edit().putString("vosk_model_name", modelNames[which]).apply();
                    downloadModel(urls[which], modelNames[which]);
                })
                .setCancelable(false)
                .show();
    }
    private void downloadModel(String urlStr, String modelName) {
        Toast.makeText(this, "Rozpoczynanie pobierania modelu...", Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            try {
                URL url = new URL(urlStr);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                File zipFile = new File(getExternalFilesDir(null), modelName + ".zip");
                InputStream input = connection.getInputStream();
                FileOutputStream output = new FileOutputStream(zipFile);

                byte[] buffer = new byte[4096];
                int len;
                while ((len = input.read(buffer)) != -1) {
                    output.write(buffer, 0, len);
                }
                output.close();
                input.close();

                runOnUiThread(() -> Toast.makeText(this, "Pobrano model, rozpoczynam rozpakowywanie...", Toast.LENGTH_SHORT).show());

                // ✅ Rozpakowujemy bezpośrednio do getExternalFilesDir(null)
                File targetDir = getExternalFilesDir(null);
                unzipModel(zipFile, targetDir);

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Błąd pobierania: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }
    private void unzipModel(File zipFile, File targetDir) {
        new Thread(() -> {
            try {
                byte[] buffer = new byte[4096];
                ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
                ZipEntry zipEntry = zis.getNextEntry();
                while (zipEntry != null) {
                    File newFile = new File(targetDir, zipEntry.getName());
                    if (zipEntry.isDirectory()) {
                        newFile.mkdirs();
                    } else {
                        newFile.getParentFile().mkdirs();
                        FileOutputStream fos = new FileOutputStream(newFile);
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                        fos.close();
                    }
                    zipEntry = zis.getNextEntry();
                }
                zis.closeEntry();
                zis.close();

                // === ZAPISUJEMY UUID W PRAWIDŁOWYM FOLDERZE ===
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                String modelFolderName = prefs.getString("vosk_model_name", null);

                if (modelFolderName != null) {
                    File modelDir = new File(getExternalFilesDir(null), modelFolderName);
                    if (!modelDir.exists()) {
                        modelDir.mkdirs();
                    }

                    String uuid = java.util.UUID.randomUUID().toString();
                    File uuidFile = new File(modelDir, "uuid");
                    try (FileOutputStream uuidOut = new FileOutputStream(uuidFile)) {
                        uuidOut.write(uuid.getBytes());
                        uuidOut.flush();
                    }
                }

                runOnUiThread(() -> Toast.makeText(this, "Model rozpakowany pomyślnie", Toast.LENGTH_LONG).show());

                // === USUWANIE ZIP PO ROZPAKOWANIU ===
                if (zipFile.delete()) {
                    runOnUiThread(() -> Toast.makeText(this, "Plik ZIP usunięty po rozpakowaniu", Toast.LENGTH_SHORT).show());
                    initModel();
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Nie udało się usunąć pliku ZIP", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Błąd rozpakowania: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }


    private void recognizeMicrophone() {
        try {
            Recognizer rec = new Recognizer(model, 16000.0f);
            speechService = new SpeechService(rec, 16000.0f);
            speechService.startListening(this);
            Toast.makeText(this, "Mów teraz...", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Błąd uruchamiania rozpoznawania: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeOrDownloadModel();
            } else {
                Toast.makeText(this, "Brak uprawnień do nagrywania, zamykanie aplikacji", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechService != null) {
            speechService.stop();
            speechService.shutdown();
        }
    }

    @Override
    public void onPartialResult(String hypothesis) {
        //sendTextToApi(hypothesis);
        //tv_view.setText(hypothesis);
    }

    @Override
    public void onResult(String hypothesis) {
        sendTextToApi(hypothesis);
        //tv_view.setText(hypothesis);
    }

    @Override
    public void onFinalResult(String hypothesis) {
        //sendTextToApi(hypothesis);
        //tv_view.setText(hypothesis);
        if (speechService != null) {
            speechService.stop();
            speechService = null;
            ibtTalk.setImageResource(android.R.drawable.ic_btn_speak_now);
        }
    }

    @Override
    public void onError(Exception e) {
        Toast.makeText(this, "Błąd rozpoznawania: " + e.getMessage(), Toast.LENGTH_LONG).show();
        if (speechService != null) {
            speechService.stop();
            speechService = null;
            ibtTalk.setImageResource(android.R.drawable.ic_btn_speak_now);
        }
    }

    @Override
    public void onTimeout() {
        if (speechService != null) {
            speechService.stop();
            speechService = null;
            ibtTalk.setImageResource(android.R.drawable.ic_btn_speak_now);
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

                String title = jsonResponse.getString("title");
                String date = jsonResponse.getString("date");
                String time = jsonResponse.getString("time");
                String type = jsonResponse.optString("type", "event");

                runOnUiThread(() -> handleEventType(type, title, date, time));

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, "Błąd: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }

    private void handleEventType(String type, String title, String date, String time) {
        switch (type) {
            case "event":
                addEventToCalendar(title, date, time);
                break;
            case "alarm":
                setAlarm(title, time);
                break;
            case "note":
                saveNote(title);
                break;
            default:
                Toast.makeText(this, "Nieznany typ: " + type, Toast.LENGTH_SHORT).show();
        }
    }

    private void addEventToCalendar(String title, String date, String time) {
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
            endTime.add(Calendar.HOUR_OF_DAY, 1); // domyślnie 1 godzina

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
                runOnUiThread(() ->
                        Toast.makeText(this, "Wydarzenie dodane do kalendarza!", Toast.LENGTH_SHORT).show()
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


    private void saveNote(String title) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, title);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, "Zapisz notatkę w aplikacji");
        startActivity(shareIntent);
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
}