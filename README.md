# Easist – Twój asystent do szybkiego zapisywania wydarzeń głosowych 📅🎤

Aplikacja **Android (Java)** umożliwiająca:
✅ rozpoznawanie mowy (Speech-to-Text)  
✅ wysyłanie tekstu do endpointu (`/parse-event`)  
✅ automatyczne zapisywanie wydarzeń do **lokalnego kalendarza**.

---

## 🚀 Funkcje
- Klikasz 🎤 ➔ mówisz „Dentysta jutro o 15”
- Aplikacja rozpoznaje mowę i zamienia ją na dane wydarzenia
- Tworzy wydarzenie w Twoim **lokalnym kalendarzu Android**
- **Brak użycia zewnętrznego Google Calendar (lokalna prywatność)**
- Przydatne dla streamerów, studentów, freelancerów

---

## 🛠️ Technologie
- **Java (Android Studio)**
- SpeechRecognizer
- Lokalny kalendarz Android
- Backend FastAPI do parsowania tekstu

---

## 🔐 Bezpieczeństwo kluczy API

Z uwagi na bezpieczeństwo,
**klucz `API_KEY` oraz `API_URL` są usuwane przed commitem do repozytorium.**

Przed uruchomieniem:
1️⃣ Otwórz `MainActivity.java`  
2️⃣ Uzupełnij:
```java
private final String API_URL = "https://twoj-url";
private final String API_KEY = "sk_live_twoj_klucz";
```

🚧 Plany rozwoju
✅ 1. Przejście z Google SpeechRecognizer na lokalny rozpoznawacz mowy (np. Vosk)

aby uniezależnić aplikację od internetu i usług Google

zwiększyć prywatność i szybkość działania offline

✅ 2. Usuwanie wydarzeń z kalendarza

możliwość wyświetlenia listy wydarzeń zapisanych przez Easist

usunięcie ich jednym kliknięciem w aplikacji
