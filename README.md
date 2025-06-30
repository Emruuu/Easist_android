# 📅 Easist – Twój prywatny asystent głosowy 📱


## 📸 Screenshots

| 🇵🇱 Widok główny |  Historia wydarzeń |
|---|---|
|  <img src="screenshots/main.jpg" width="200"/>   | <img src="screenshots/history.jpg" width="200"/> |

## ✨ Co potrafi Easist?

✅ Rozpoznawanie mowy (Speech-to-Text)  
✅ Wysyłanie tekstu do endpointu (`/parse-event`)  
✅ Automatyczne zapisywanie wydarzeń do **lokalnego kalendarza Android**  
✅ Ustawianie **alarmów głosem**  
✅ Zapisywanie **notatek głosem**  
✅ **Historia zapisanych wydarzeń z możliwością usuwania pojedynczych lub wszystkich**

---

## 🚀 Jak działa?

🎤 **Klikasz mikrofon ➔ mówisz np. „Dentysta jutro o 15”**  
🧠 Easist rozpoznaje mowę i wysyła ją do backendu (FastAPI + OpenAI)  
📅 Automatycznie tworzy wydarzenie w **lokalnym kalendarzu Android**  
⏰ Ustawia budzik/alarm jeśli wykryje intencję  
📝 Zapisuje notatki głosem  
📜 Dodaje wydarzenia do **historii zapisanych wydarzeń** w aplikacji
=======
Aplikacja **Android (Java)** umożliwiająca:
✅ rozpoznawanie mowy (Speech-to-Text)  
✅ wysyłanie tekstu do endpointu (`/parse-event`)  
✅ automatyczne zapisywanie wydarzeń do **lokalnego kalendarza Android**.


---

## 🛠️ Technologie

- **Java (Android Studio)**
- `SpeechRecognizer` (online STT)
- Lokalny kalendarz Android
- `AlarmManager` do budzików
- Zapisywanie notatek
- **RecyclerView** do historii
- **FastAPI + OpenAI** do parsowania tekstu

---

## 🗂️ Historia zapisanych wydarzeń

- Zapisuje **typ, tytuł, datę, godzinę** każdego wydarzenia
- Wyświetla w czytelnej liście w aplikacji
- Długie kliknięcie ➔ usuwa pojedynczy wpis
- Przycisk w menu ➔ usuwa całą historię jednym kliknięciem

---

## 🛡️ Bezpieczeństwo kluczy API

Klucze API NIE są trzymane publicznie w repo.  
Przed uruchomieniem aplikacji uzupełnij w `MainActivity.java`:

```java
private final String API_URL = "https://twoj-url";
private final String API_KEY = "sk_live_twoj_klucz";
```

💡 Trzymaj w local.properties lub secrets.txt lokalnie.
=======

📦 Instalacja
1️⃣ Sklonuj repo:

```bash
git clone https://github.com/TwojUser/Easist_android.git
```
2️⃣ Otwórz w Android Studio
3️⃣ Podłącz telefon lub użyj emulatora
4️⃣ Uruchom aplikację 🚀

⚙️ Backend (FastAPI)
Do działania wymagany jest endpoint /parse-event, który:

✅ Przyjmuje tekst użytkownika
✅ Rozpoznaje intencję (event/alarm/note)
✅ Zwraca JSON z:

```json
{
  "title": "Dentysta",
  "date": "2024-06-28",
  "time": "15:00",
  "type": "event"
}
```
Backend bazuje na FastAPI + OpenAI i działa lokalnie lub na VPS.

## ⚙️ Instalacja backendu krok po kroku

### 1️⃣ Zależności systemowe (Ubuntu)
```bash
sudo apt update && sudo apt upgrade -y
sudo apt install python3 python3-pip python3-venv nginx curl certbot python3-certbot-nginx -y
```

### 2️⃣ Projekt FastAPI
```bash
mkdir -p ~/fastapi-assistant
cd ~/fastapi-assistant
python3 -m venv venv
source venv/bin/activate
pip install fastapi uvicorn openai python-dotenv
```

### 3️⃣ Plik .env
```env
OPENAI_API_KEY=sk-...twoj_klucz...
```

### 4️⃣ Plik main.py
```python
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from openai import OpenAI
from dotenv import load_dotenv
import os
import json

load_dotenv()
client = OpenAI(api_key=os.getenv("OPENAI_API_KEY"))

app = FastAPI()

class ParseRequest(BaseModel):
    text: str

@app.post("/parse-event")
def parse_event(req: ParseRequest):
    prompt = f"""
Twoim zadaniem jest zwrócić dane wydarzenia w formacie JSON do użycia w aplikacji asystenta.

Na podstawie tekstu użytkownika zidentyfikuj intencję:
- "event" jeśli chodzi o wydarzenie do kalendarza,
- "alarm" jeśli użytkownik chce ustawić budzik,
- "note" jeśli użytkownik chce zapisać notatkę.

Dodatkowo oblicz i zwróć datę oraz godzinę w formacie:
{{
  "title": "...",
  "date": "YYYY-MM-DD",
  "time": "HH:MM",
  "type": "event | alarm | note"
}}

Tekst użytkownika:
\"{req.text}\"

Zwróć wyłącznie czysty JSON bez komentarzy.
"""

    try:
        response = client.chat.completions.create(
            model="gpt-4o-mini",
            messages=[
                {"role": "system", "content": "Jesteś asystentem konwertującym tekst użytkownika na dane wydarzenia w JSON."},
                {"role": "user", "content": prompt}
            ],
            temperature=0.2
        )

        raw_content = response.choices[0].message.content.strip()

        # Wymuszenie poprawnego JSON
        data = json.loads(raw_content)

        return data

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
```

### 5️⃣ Uruchamianie lokalnie
```bash
uvicorn main:app --host 127.0.0.1 --port 8000
```


## 🌐 Konfiguracja serwera

### Nginx (/etc/nginx/sites-available/assistant)
```nginx
server {
    listen 80;
    server_name yourserver.pl;

    location / {
        proxy_pass http://127.0.0.1:8000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### Certyfikat SSL
```bash
sudo ln -s /etc/nginx/sites-available/assistant /etc/nginx/sites-enabled/
sudo nginx -t && sudo systemctl reload nginx
sudo certbot --nginx -d yourserver.pl
```

---

## 🚀 Uruchamianie jako usługa (systemd)

### Plik /etc/systemd/system/fastapi.service
```ini
[Unit]
Description=FastAPI Assistant
After=network.target

[Service]
User=ubuntu
WorkingDirectory=/home/ubuntu/fastapi-assistant
ExecStart=/home/ubuntu/fastapi-assistant/venv/bin/python3 -m uvicorn main:app --host 127.0.0.1 --port 8000
Restart=always

[Install]
WantedBy=multi-user.target


### Uruchomienie:
bash
sudo systemctl daemon-reexec
sudo systemctl daemon-reload
sudo systemctl enable --now fastapi
```

---

## ✅ Testowanie

### Przeglądarka:
=======
`https://api.url/docs`
>>>>>>> master

### CURL:
```bash
curl -X POST -H "Content-Type: application/json" \
  -d '{"text":"Spotkanie z Jackiem jutro o 15:00"}' \
  http://yourserver.pl/parse-event
```

📜 Licencja
Projekt Easist rozwijany prywatnie.
Masz pytania? Napisz na Discord / Twitter / Email.


##🚧 Plany rozwoju

✅ 1. Edycja wydarzeń z historii
✅ 2. Sortowanie
✅ 3. Przejście z Google SpeechRecognizer na lokalny rozpoznawacz mowy (np. Vosk)
aby uniezależnić aplikację od internetu i usług Google
zwiększyć prywatność i szybkość działania offline
