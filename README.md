📅 Easist – Twój prywatny asystent głosowy 📱
Aplikacja Android (Java) umożliwiająca:

✅ rozpoznawanie mowy (Speech-to-Text)
✅ wysyłanie tekstu do backendu FastAPI
✅ automatyczne zapisywanie wydarzeń do lokalnego kalendarza Android
✅ ustawianie alarmów głosem
✅ zapisywanie notatek lokalnie lub do wybranej aplikacji

Brak integracji z Google Calendar – pełna prywatność i lokalne działanie.

🚀 Funkcje
🎤 Mówisz: „Dentysta jutro o 15”
✅ Aplikacja rozpoznaje mowę lub wpisany tekst
✅ Wysyła go do backendu, który zwraca dane wydarzenia
✅ Automatycznie zapisuje w kalendarzu Android
✅ Może ustawić alarm na wskazaną godzinę
✅ Może zapisać notatkę lokalnie lub udostępnić do wybranej aplikacji

📈 Idealne dla streamerów, studentów, freelancerów i zapracowanych osób.

🛠️ Technologie
Java (Android Studio)

SpeechRecognizer (Android)

Lokalny kalendarz Android

Backend FastAPI + OpenAI API do parsowania tekstu

🔐 Bezpieczeństwo kluczy API
Klucze API_URL oraz API_KEY nie są umieszczane w repozytorium.

Przed uruchomieniem:

1️⃣ Otwórz MainActivity.java
2️⃣ Uzupełnij:

java
Kopiuj
Edytuj
private final String API_URL = "https://twoj-backend-url";
private final String API_KEY = "sk_live_twoj_klucz";
🖥️ Backend – FastAPI
Backend do rozpoznawania komend i zamiany na:
✅ wydarzenia kalendarza
✅ alarmy
✅ notatki

🔧 Technologie backendu
Python 3

FastAPI + Uvicorn

OpenAI API

Nginx (reverse proxy + SSL)

systemd

Ubuntu VPS

📂 Struktura projektu
bash
Kopiuj
Edytuj
fastapi-assistant/
├── main.py
├── .env
└── venv/
⚡ Instalacja krok po kroku
1️⃣ Zależności systemowe (Ubuntu)
bash
Kopiuj
Edytuj
sudo apt update && sudo apt upgrade -y
sudo apt install python3 python3-pip python3-venv nginx curl certbot python3-certbot-nginx -y
2️⃣ Projekt FastAPI
bash
Kopiuj
Edytuj
mkdir -p ~/fastapi-assistant
cd ~/fastapi-assistant
python3 -m venv venv
source venv/bin/activate
pip install fastapi uvicorn openai python-dotenv
3️⃣ Plik .env
ini
Kopiuj
Edytuj
OPENAI_API_KEY=sk-...twoj_klucz...
4️⃣ Uruchomienie lokalne
bash
Kopiuj
Edytuj
uvicorn main:app --host 127.0.0.1 --port 8000
🌐 Konfiguracja Nginx
/etc/nginx/sites-available/assistant
nginx
Kopiuj
Edytuj
server {
    listen 80;
    server_name twoj-backend-url;

    location / {
        proxy_pass http://127.0.0.1:8000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
SSL
bash
Kopiuj
Edytuj
sudo ln -s /etc/nginx/sites-available/assistant /etc/nginx/sites-enabled/
sudo nginx -t && sudo systemctl reload nginx
sudo certbot --nginx -d twoj-backend-url
🚀 systemd – uruchamianie jako usługa
/etc/systemd/system/fastapi.service
ini
Kopiuj
Edytuj
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
Uruchomienie
bash
Kopiuj
Edytuj
sudo systemctl daemon-reexec
sudo systemctl daemon-reload
sudo systemctl enable --now fastapi
✅ Testowanie
CURL:
bash
Kopiuj
Edytuj
curl -X POST -H "Content-Type: application/json" \
  -d '{"text":"Spotkanie z Jackiem 5 lipca o 15:00"}' \
  https://twoj-backend-url/parse-event
🚧 Plany rozwoju
✅ Przejście na lokalny rozpoznawacz mowy (Vosk)

Uniezależnienie od internetu

Większa prywatność i offline

✅ Usuwanie wydarzeń z kalendarza

Lista wydarzeń zapisanych przez Easist

Możliwość usunięcia jednym kliknięciem
