📅 Easist – Twój asystent do szybkiego zapisywania wydarzeń głosowych 📅🎤
Aplikacja Android (Java), która umożliwia:
✅ rozpoznawanie mowy (Speech-to-Text)
✅ wysyłanie tekstu do backendu FastAPI
✅ automatyczne zapisywanie wydarzeń do lokalnego kalendarza
✅ zapisywanie notatek w telefonie
✅ ustawianie budzików poleceniami głosowymi.

🚀 Funkcje
🎤 Klikasz mikrofon ➔ mówisz np. „Dentysta jutro o 15”
✅ Aplikacja rozpoznaje mowę lub wpisany tekst
✅ Wysyła go do backendu, który zwraca dane wydarzenia
✅ Tworzy wydarzenie w Twoim lokalnym kalendarzu Android
✅ Może ustawić budzik na wskazaną godzinę
✅ Może zapisać notatkę do pliku lub udostępnić do wybranej aplikacji
✅ Brak integracji z Google Calendar – pełna prywatność offline
✅ Przydatne dla streamerów, studentów, freelancerów.

🛠️ Technologie
Java (Android Studio)

SpeechRecognizer

Lokalny kalendarz Android

Backend FastAPI do parsowania tekstu

OpenAI API

🔐 Bezpieczeństwo kluczy API
Z uwagi na bezpieczeństwo, API_KEY oraz API_URL są usuwane przed commitem do repozytorium.

Przed uruchomieniem:

1️⃣ Otwórz MainActivity.java
2️⃣ Uzupełnij:

java
Kopiuj
Edytuj
private final String API_URL = "https://twoj-backend-url";
private final String API_KEY = "sk_live_twoj_klucz";
📅 Backend – Asystent głosowy z FastAPI
Backend aplikacji Android do rozpoznawania komend głosowych i zamieniania ich na dane wydarzenia kalendarza, alarmy lub notatki.

🔧 Technologie
Python 3

FastAPI

Uvicorn

OpenAI API

systemd

Nginx (reverse proxy + SSL)

Ubuntu VPS

🧱 Struktura projektu
bash
Kopiuj
Edytuj
fastapi-assistant/
├── main.py
├── .env
└── venv/
🛠️ Instalacja krok po kroku
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
4️⃣ Plik main.py
Zawiera endpoint /parse-event, który odbiera tekst i zwraca JSON z title, date, time, type.

5️⃣ Uruchamianie lokalne
bash
Kopiuj
Edytuj
uvicorn main:app --host 127.0.0.1 --port 8000
🌐 Konfiguracja serwera
Nginx (/etc/nginx/sites-available/assistant)
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
Certyfikat SSL
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
Uruchomienie:
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
✅ 1. Przejście z Google SpeechRecognizer na lokalny rozpoznawacz mowy (np. Vosk)
• uniezależnienie od internetu i Google
• zwiększenie prywatności i działania offline

✅ 2. Usuwanie wydarzeń z kalendarza
• możliwość listy wydarzeń dodanych przez Easist
• możliwość ich usunięcia jednym kliknięciem
