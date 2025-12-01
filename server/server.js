const express = require('express');
const multer = require('multer');
const sharp = require('sharp');
const path = require('path');
const fs = require('fs');
const sqlite3 = require('sqlite3').verbose();
const { v4: uuidv4 } = require('uuid');
const cors = require('cors');
require('dotenv').config();

// Keep process alive and handle errors
process.on('uncaughtException', (err) => {
  console.error('Uncaught Exception:', err);
});
process.on('unhandledRejection', (reason, promise) => {
  console.error('Unhandled Rejection at:', promise, 'reason:', reason);
});

const PORT = process.env.PORT || 3000;
const UPLOAD_DIR = process.env.UPLOAD_DIR || './data/uploads';
const THUMB_DIR = process.env.THUMB_DIR || './data/thumbs';
const MAX_UPLOAD_MB = Number(process.env.MAX_UPLOAD_MB || 5);
const BASE_URL = process.env.BASE_URL || `http://localhost:${PORT}`;

[UPLOAD_DIR, THUMB_DIR].forEach(dir => { if (!fs.existsSync(dir)) fs.mkdirSync(dir, { recursive: true }); });

// Simple SQLite DB
const db = new sqlite3.Database('./data/db.sqlite');
db.serialize(() => {
  db.run(`CREATE TABLE IF NOT EXISTS panels (
    id TEXT PRIMARY KEY,
    chainId TEXT,
    authorId TEXT,
    imagePath TEXT,
    thumbPath TEXT,
    createdAt INTEGER
  )`);
});

const storage = multer.memoryStorage();
const upload = multer({
  storage,
  limits: { fileSize: MAX_UPLOAD_MB * 1024 * 1024 },
  fileFilter: (req, file, cb) => {
    if (!file.mimetype.startsWith('image/')) return cb(new Error('Only images allowed'), false);
    cb(null, true);
  }
});

const app = express();
app.use(cors());
app.use(express.json());

// Serve static files (no directory listing)
app.use('/uploads', express.static(path.resolve(UPLOAD_DIR)));
app.use('/thumbs', express.static(path.resolve(THUMB_DIR)));

app.post('/upload', upload.single('panel'), async (req, res) => {
  try {
    if (!req.file) return res.status(400).json({ error: 'No file' });

    const id = uuidv4();
    const fileExt = 'webp';
    const filename = `${id}.${fileExt}`;
    const thumbName = `${id}_thumb.${fileExt}`;

    // Convert to webp and save
    const imagePath = path.join(UPLOAD_DIR, filename);
    await sharp(req.file.buffer)
      .resize({ width: 1080 }) // scale for consistent max dimension
      .webp({ quality: 75 })
      .toFile(imagePath);

    // Create a small thumbnail
    const thumbPath = path.join(THUMB_DIR, thumbName);
    await sharp(req.file.buffer)
      .resize({ width: 400 })
      .webp({ quality: 60 })
      .toFile(thumbPath);

    const now = Date.now();
    const chainId = req.body.chainId || 'unknown';
    const authorId = req.body.authorId || 'anonymous';

    // Insert metadata
    db.run(
      `INSERT INTO panels (id, chainId, authorId, imagePath, thumbPath, createdAt) VALUES (?, ?, ?, ?, ?, ?)`,
      [id, chainId, authorId, `/uploads/${filename}`, `/thumbs/${thumbName}`, now]
    );

    // Return publicly-accessible URLs (through this server)
    return res.json({
      id,
      imageUrl: `${BASE_URL}/uploads/${filename}`,
      thumbUrl: `${BASE_URL}/thumbs/${thumbName}`
    });
  } catch (err) {
    console.error('Upload error:', err);
    return res.status(500).json({ error: 'Upload failed', details: err.message });
  }
});

// Example: list last N panels (for quick testing)
app.get('/panels', (req, res) => {
  db.all(`SELECT * FROM panels ORDER BY createdAt DESC LIMIT 50`, [], (err, rows) => {
    if (err) return res.status(500).json({ error: err.message });
    res.json(rows);
  });
});

app.listen(PORT, () => {
  console.log(`Server listening on http://localhost:${PORT}`);
});
