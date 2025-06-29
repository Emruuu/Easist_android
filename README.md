# Easist (Branch: vosk)

Lokalny asystent głosowy działający bez googla, integrujący rozpoznawanie mowy z funkcjami kalendarza, budzika oraz wysyłką do backendu.

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

### Pobieranie i rozpakowanie:

Rozpoznawanie mowy:

Wysyłka do backendu:


## 🎯 TODO
 Obsługa usuwania lub zmiany modelu

 Konfigurowalne endpointy API w UI

 Wyświetlanie historii rozpoznanych poleceń
