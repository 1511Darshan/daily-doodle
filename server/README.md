# Local Demo Server (Daily Doodle Chain)

Quick start:
1. Install Node 18+ and npm.
2. Copy .env.example -> .env and edit if needed.
3. npm install
4. npm run start
5. Use ngrok to expose: ngrok http 3000

## Test upload (curl):

```bash
curl -X POST "http://localhost:3000/upload" -F "panel=@/path/to/your/image.png" -F "chainId=seed1" -F "authorId=test-user"
```

## PowerShell test:

```powershell
# Using Invoke-RestMethod
$form = @{
    panel = Get-Item -Path "C:\path\to\image.png"
    chainId = "seed1"
    authorId = "test-user"
}
Invoke-RestMethod -Uri "http://localhost:3000/upload" -Method Post -Form $form
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /upload | Upload an image panel |
| GET | /panels | List last 50 panels |
| GET | /uploads/:filename | Serve uploaded images |
| GET | /thumbs/:filename | Serve thumbnails |

## Notes:
- This is for prototyping only. Use HTTPS (ngrok) when exposing to devices.
- Back up ./data/uploads and ./data/db.sqlite regularly.

## Exposing with ngrok

```bash
ngrok http 3000
```

Then use the provided HTTPS URL (e.g., `https://abcd-1234.ngrok.io`) as your BASE_URL in .env and restart the server.
