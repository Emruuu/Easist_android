# Easist – Twój asystent do szybkiego zapisywania wydarzeń głosowych 📅🎤

Aplikacja **Android (Java)** umożliwiająca:
✅ rozpoznawanie mowy (Speech-to-Text)  
✅ wysyłanie tekstu do endpointu (`/parse-event`)  
✅ automatyczne zapisywanie wydarzeń do **lokalnego kalendarza Android**.

---

## 🚀 Funkcje
- Klikasz 🎤 ➔ mówisz „Dentysta jutro o 15”
- Aplikacja rozpoznaje mowę i zamienia ją na dane wydarzenia
- Tworzy wydarzenie w Twoim **lokalnym kalendarzu Android**
- **Brak użycia Google Calendar – pełna prywatność**
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
Wskazówka:
Trzymaj swój klucz w .txt lokalnie, aby łatwo go wklejać przed wrzuceniem na repo, jeśli wprowadzisz zmiany.

# 📅 Asystent głosowy z FastAPI – backend

Backend aplikacji Androidowej do rozpoznawania komend głosowych i zamieniania ich na dane wydarzenia kalendarza.

## 🔧 Technologie
- Python 3
- FastAPI
- Uvicorn
- OpenAI API
- systemd
- Nginx (reverse proxy + SSL)
- Ubuntu VPS

## 🧱 Struktura projektu
```
fastapi-assistant/
├── main.py
├── .env
└── venv/
```

## 🛠️ Instalacja krok po kroku

### 1. Zależności systemowe (Ubuntu)
```bash
sudo apt update && sudo apt upgrade -y
sudo apt install python3 python3-pip python3-venv nginx curl certbot python3-certbot-nginx -y
```

### 2. Projekt FastAPI
```bash
mkdir -p ~/fastapi-assistant
cd ~/fastapi-assistant
python3 -m venv venv
source venv/bin/activate
pip install fastapi uvicorn openai python-dotenv
```

### 3. Plik `.env`
```env
OPENAI_API_KEY=sk-...twój_klucz...
```

### 4. Plik `main.py`
```python
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import openai
import os
from dotenv import load_dotenv

load_dotenv()
openai.api_key = os.getenv("OPENAI_API_KEY")

app = FastAPI()

class ParseRequest(BaseModel):
    text: str

@app.post("/parse-event")
def parse_event(req: ParseRequest):
    prompt = f'''Zamień na dane wydarzenia w JSON:\n\"{req.text}\"\n\nFormat:\n{{\n  "title": "...",\n  "date": "RRRR-MM-DD",\n  "time": "GG:MM"\n}}'''

    try:
        response = openai.ChatCompletion.create(
            model="gpt-4",
            messages=[{"role": "user", "content": prompt}],
            temperature=0.2
        )
        raw = response['choices'][0]['message']['content']
        return eval(raw)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
```

### 5. Uruchamianie lokalne
```bash
uvicorn main:app --host 127.0.0.1 --port 8000
```

---

## 🌐 Konfiguracja serwera

### Nginx `/etc/nginx/sites-available/assistant`
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

## 🚀 systemd – uruchamianie jako usługa

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

### Uruchomienie
```bash
sudo systemctl daemon-reexec
sudo systemctl daemon-reload
sudo systemctl enable --now fastapi
```

---

## ✅ Testowanie

### Przeglądarka:
`https://api.url/docs`

### CURL:
```bash
curl -X POST -H "Content-Type: application/json" \
  -d '{"text":"Spotkanie z Jackiem 5 lipca o 15:00"}' \
  https://api.url/parse-event
```

---

## 📦 Gotowe do integracji z aplikacją Android.


🚧 Plany rozwoju
✅ 1. Przejście z Google SpeechRecognizer na lokalny rozpoznawacz mowy (np. Vosk)

aby uniezależnić aplikację od internetu i usług Google

zwiększyć prywatność i szybkość działania offline

✅ 2. Usuwanie wydarzeń z kalendarza

możliwość wyświetlenia listy wydarzeń zapisanych przez Easist

usunięcie ich jednym kliknięciem w aplikacji

📜 Licencja
Projekt Easist jest rozwijany prywatnie. W razie pytań co do użycia – napisz na Discord/Twitter/Email.
