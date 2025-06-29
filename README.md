Easist (Branch: vosk)
Lokalny asystent głosowy bez potrzeby internetu dzięki integracji Vosk Speech Recognition.

🚀 Co robi ten branch?
✅ Pobiera wybrany model Vosk (PL/EN)
✅ Rozpakowuje model lokalnie i generuje uuid
✅ Zapisuje nazwę modelu w SharedPreferences do ponownego użycia
✅ Inicjalizuje Vosk z lokalnego modelu na dysku, nie z assets
✅ Gotowy pod dalsze przetwarzanie rozpoznanego tekstu (np. wysyłanie do backendu)

🛠️ Wymagania
Android Studio Giraffe+

Min SDK 28

Vosk Android (com.alphacephei:vosk-android:0.3.32)

📂 Struktura modeli
Modele są pobierane jako .zip i rozpakowywane do:

swift
Kopiuj
Edytuj
/Android/data/com.example.easist/files/{nazwa_modelu}/
W folderze modelu generowany jest plik uuid wymagany przez Vosk.

Nazwa modelu zapisywana jest w SharedPreferences jako vosk_model_name.

⚙️ Główne funkcje
Pobieranie i rozpakowywanie
Użytkownik wybiera język (PL/EN) ➔ pobierany jest odpowiedni model ➔ automatyczne rozpakowanie ➔ generowanie uuid ➔ usunięcie .zip po rozpakowaniu.

Inicjalizacja modelu
java
Kopiuj
Edytuj
private void initModel() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    StorageService.unpack(this, prefs.getString("vosk_model_name", null), "model",
        (model) -> {
            this.model = model;
            Toast.makeText(this, "Model Vosk gotowy, możesz mówić.", Toast.LENGTH_SHORT).show();
            ibtTalk.setEnabled(true);
        },
        (IOException exception) -> Toast.makeText(this, "Błąd ładowania modelu: " + exception.getMessage(), Toast.LENGTH_LONG).show());
}
Użycie
Przyciskiem mikrofonu uruchamiasz rozpoznawanie mowy offline.

Wypowiedz polecenie ➔ tekst trafia do pola prompt ➔ możesz przesłać go do swojego API.
