# 📅 Easist – Twój prywatny asystent głosowy 📱

Aplikacja **Android (Java)** umożliwiająca:

✅ rozpoznawanie mowy (Speech-to-Text)  
✅ wysyłanie tekstu do endpointu (`/parse-event`)  
✅ automatyczne zapisywanie wydarzeń do **lokalnego kalendarza Android**  
✅ ustawianie **alarmów głosem**  
✅ zapisywanie **notatek głosem**

---

## 🚀 Funkcje

- Klikasz 🎤 ➔ mówisz np. „Dentysta jutro o 15”
- Aplikacja rozpoznaje mowę i zamienia ją na dane wydarzenia
- Tworzy wydarzenie w **lokalnym kalendarzu Android** (bez Google Calendar)
- Ustawia **alarmy/budziki głosem**
- Zapisuje **notatki głosem**
- Pełna prywatność – brak wysyłki danych do Google
- Przydatne dla streamerów, studentów, freelancerów

---

## 🛠️ Technologie

- **Java (Android Studio)**
- `SpeechRecognizer`
- Lokalny kalendarz Android
- `AlarmManager` do budzików
- Zapisywanie notatek lokalnie
- Backend **FastAPI + OpenAI** do parsowania tekstu

---

## 🔐 Bezpieczeństwo kluczy API

Z uwagi na bezpieczeństwo:

**Klucz `API_KEY` oraz `API_URL` są usuwane przed commitem do repozytorium.**

### ⚠️ Przed uruchomieniem:
1️⃣ Otwórz `MainActivity.java`  
2️⃣ Uzupełnij:
```java
private final String API_URL = "https://twoj-url";
private final String API_KEY = "sk_live_twoj_klucz";
```
💡 **Wskazówka:** Trzymaj klucz w `secrets.txt` lokalnie, aby łatwo go wklejać przed wrzutką na GitHub.

---

# 🖥️ Backend – FastAPI Asystent

Backend do rozpoznawania komend głosowych i zamiany ich na dane **wydarzenia, alarmy lub notatki**.

---

## 🔧 Technologie backendu

- Python 3
- FastAPI
- Uvicorn
- OpenAI API
- systemd
- Nginx (SSL)
- Ubuntu VPS

---

## 🧱 Struktura projektu

```
fastapi-assistant/
├── main.py
├── .env
└── venv/
```

---

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

### 3️⃣ Plik `.env`
```env
OPENAI_API_KEY=sk-...twoj_klucz...
```

### 4️⃣ Plik `main.py`
```python
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from openai import OpenAI
import os
from dotenv import load_dotenv

load_dotenv()
client = OpenAI(api_key=os.getenv("OPENAI_API_KEY"))

app = FastAPI()

class ParseRequest(BaseModel):
    text: str

@app.post("/parse-event")
def parse_event(req: ParseRequest):
    prompt = f'''Zamień na dane wydarzenia w JSON:
"{req.text}"

Format:
{{
  "title": "...",
  "date": "RRRR-MM-DD",
  "time": "GG:MM",
  "type": "event/note/alarm"
}}'''
    try:
        response = client.chat.completions.create(
            model="gpt-4o-mini",
            messages=[{"role": "user", "content": prompt}],
            temperature=0.2
        )
        raw = response.choices[0].message.content
        return eval(raw)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
```

### 5️⃣ Uruchamianie lokalnie
```bash
uvicorn main:app --host 127.0.0.1 --port 8000
```

---

## 🌐 Konfiguracja serwera

### Nginx (`/etc/nginx/sites-available/assistant`)
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

## 🚀 Uruchamianie jako usługa (`systemd`)

### Plik `/etc/systemd/system/fastapi.service`
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
```

### Uruchomienie:
```bash
sudo systemctl daemon-reexec
sudo systemctl daemon-reload
sudo systemctl enable --now fastapi
```

---

## ✅ Testowanie

### Przeglądarka:
`http://yourserver.pl/docs`

### CURL:
```bash
curl -X POST -H "Content-Type: application/json" \
  -d '{"text":"Spotkanie z Jackiem jutro o 15:00"}' \
  http://yourserver.pl/parse-event
```

---

## 🚧 Plany rozwoju

✅ Przejście z Google `SpeechRecognizer` na **lokalny rozpoznawacz mowy (np. Vosk)**  
✅ Usuwanie wydarzeń z kalendarza  
✅ Lista wydarzeń zapisanych przez Easist  

---

## 📜 Licencja

Projekt **Easist** rozwijany prywatnie.  
W razie pytań co do użycia – zapraszam na Discord / Twitter / Email.
