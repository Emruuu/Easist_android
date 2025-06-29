# Easist (Branch: vosk)

Lokalny asystent głosowy działający bez googla, integrujący rozpoznawanie mowy z funkcjami kalendarza, budzika oraz wysyłką do backendu.
Model vosk nie jest idealny szczególnie przy godzinach. Możliwe że poszukam alternatywy.
---

## 🚀 Funkcje w tym branchu

✅ Pobieranie i rozpakowywanie modelu Vosk (.zip) (PL/EN)  
✅ Generowanie pliku `uuid` wymaganego przez Vosk wewnątrz rozpakowanego folderu  
✅ Zapisywanie ścieżki modelu w `SharedPreferences` (`vosk_model_name`)  
✅ Inicjalizacja **Vosk** z lokalnej pamięci (`getExternalFilesDir`)  
✅ Rozpoznawanie mowy offline i wysyłka tekstu do backendu  
✅ Obsługa poleceń do kalendarza, budzika i zapisu notatek

---

## ⚙️ Wymagania

- Android Studio Giraffe+
- Min SDK 28
- `implementation 'com.alphacephei:vosk-android:0.3.32'`

---

## 📂 Struktura modeli

1️⃣ Model pobierany jako `.zip`, rozpakowywany do:
/storage/emulated/0/Android/data/com.example.easist/files/{nazwa_modelu}/

2️⃣ Wewnątrz folderu generowany jest plik `uuid`.

3️⃣ `.zip` jest usuwany po rozpakowaniu.

---

## 🛠️ Przebieg działania

### 1️⃣ Uruchomienie aplikacji
- Sprawdzane są uprawnienia mikrofonu.
- Sprawdzane jest, czy model jest już pobrany.
- Jeśli nie ➔ **wybór języka ➔ pobranie ➔ rozpakowanie ➔ generowanie `uuid` ➔ inicjalizacja.**

### 2️⃣ Przycisk mikrofonu
- Rozpoczyna rozpoznawanie mowy offline.
- Zatrzymanie/pauza działa na tym samym przycisku.

### 3️⃣ Wynik rozpoznania
- Przesyłany do zdefiniowanego **API backendu** (np. FastAPI) wraz z datą i godziną.
- Backend zwraca obiekt JSON zawierający:
  - `title`
  - `date`
  - `time`
  - `type` (`event`, `alarm`, `note`)

- Na tej podstawie aplikacja:
  - Dodaje wydarzenie do kalendarza
  - Ustawia budzik
  - Otwiera notatkę do zapisu

---

## 🗂️ Kluczowe funkcje w kodzie
### Inicjalizacja w pobranym folderze:
```java
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
```
### Pobieranie i rozpakowanie:
```java
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
```
```java
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

```
Rozpoznawanie mowy:
```java
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
```

## 🎯 TODO
 Obsługa usuwania lub zmiany modelu

 Konfigurowalne endpointy API w UI

 Wyświetlanie historii rozpoznanych poleceń
