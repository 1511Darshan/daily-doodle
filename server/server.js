const express = require('express');
const multer = require('multer');
const sharp = require('sharp');
const path = require('path');
const fs = require('fs');
const sqlite3 = require('sqlite3').verbose();
const { v4: uuidv4 } = require('uuid');
const cors = require('cors');
const { v2: cloudinary } = require('cloudinary');

// Load .env from the same directory as server.js
require('dotenv').config({ path: path.join(__dirname, '.env') });

// Configure Cloudinary
cloudinary.config({
  cloud_name: process.env.CLOUDINARY_CLOUD_NAME || 'dq8watps5',
  api_key: process.env.CLOUDINARY_API_KEY || '284249892731925',
  api_secret: process.env.CLOUDINARY_API_SECRET || 'Zr8F4OvGBG_CEHbWcFsgMVOrzZc'
});

// Keep process alive and handle errors
process.on('uncaughtException', (err) => {
  console.error('Uncaught Exception:', err);
});
process.on('unhandledRejection', (reason, promise) => {
  console.error('Unhandled Rejection at:', promise, 'reason:', reason);
});

const PORT = process.env.PORT || 3000;
const UPLOAD_DIR = path.join(__dirname, process.env.UPLOAD_DIR || './data/uploads');
const THUMB_DIR = path.join(__dirname, process.env.THUMB_DIR || './data/thumbs');
const DB_PATH = path.join(__dirname, './data/db.sqlite');
const MAX_UPLOAD_MB = Number(process.env.MAX_UPLOAD_MB || 5);

[UPLOAD_DIR, THUMB_DIR].forEach(dir => { if (!fs.existsSync(dir)) fs.mkdirSync(dir, { recursive: true }); });

// Simple SQLite DB
const db = new sqlite3.Database(DB_PATH);
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

/**
 * Get base URL from incoming request (works with ngrok, proxies, etc.)
 */
function getBaseUrl(req) {
  const proto = req.headers['x-forwarded-proto'] || req.protocol || 'http';
  const host = req.headers['x-forwarded-host'] || req.get('host');
  return `${proto}://${host}`;
}

app.post('/upload', upload.single('panel'), async (req, res) => {
  try {
    if (!req.file) return res.status(400).json({ error: 'No file' });

    const id = uuidv4();
    const chainId = req.body.chainId || 'unknown';
    const authorId = req.body.authorId || 'anonymous';
    const now = Date.now();

    // Process image with sharp (convert to webp)
    const processedBuffer = await sharp(req.file.buffer)
      .resize({ width: 1080 })
      .webp({ quality: 75 })
      .toBuffer();

    // Create thumbnail
    const thumbBuffer = await sharp(req.file.buffer)
      .resize({ width: 400 })
      .webp({ quality: 60 })
      .toBuffer();

    // Upload to Cloudinary
    const uploadToCloudinary = (buffer, publicId, folder) => {
      return new Promise((resolve, reject) => {
        const uploadStream = cloudinary.uploader.upload_stream(
          {
            public_id: publicId,
            folder: folder,
            resource_type: 'image',
            format: 'webp'
          },
          (error, result) => {
            if (error) reject(error);
            else resolve(result);
          }
        );
        uploadStream.end(buffer);
      });
    };

    // Upload main image and thumbnail to Cloudinary
    const [imageResult, thumbResult] = await Promise.all([
      uploadToCloudinary(processedBuffer, `panel_${id}`, 'dailydoodle/panels'),
      uploadToCloudinary(thumbBuffer, `thumb_${id}`, 'dailydoodle/thumbs')
    ]);

    const imageUrl = imageResult.secure_url;
    const thumbUrl = thumbResult.secure_url;

    // Insert metadata into local DB (for reference)
    db.run(
      `INSERT INTO panels (id, chainId, authorId, imagePath, thumbPath, createdAt) VALUES (?, ?, ?, ?, ?, ?)`,
      [id, chainId, authorId, imageUrl, thumbUrl, now]
    );

    console.log(`✅ Uploaded panel ${id} to Cloudinary`);
    console.log(`   Chain: ${chainId}, Author: ${authorId}`);
    console.log(`   Image: ${imageUrl}`);
    console.log(`   Thumb: ${thumbUrl}`);

    return res.json({
      id,
      imageUrl,
      thumbUrl
    });
  } catch (err) {
    console.error('❌ Upload error:', err);
    return res.status(500).json({ error: 'Upload failed', details: err.message });
  }
});

// List panels with Cloudinary URLs (already stored as full URLs now)
app.get('/panels', (req, res) => {
  db.all(`SELECT id, chainId, authorId, imagePath, thumbPath, createdAt FROM panels ORDER BY createdAt DESC LIMIT 100`, [], (err, rows) => {
    if (err) return res.status(500).json({ error: err.message });
    
    // URLs are now stored as full Cloudinary URLs
    const mapped = rows.map(r => ({
      id: r.id,
      chainId: r.chainId,
      authorId: r.authorId,
      imageUrl: r.imagePath,
      thumbUrl: r.thumbPath,
      createdAt: r.createdAt
    }));
    
    console.log(`📋 Returning ${mapped.length} panels`);
    res.json(mapped);
  });
});

// Health check endpoint
app.get('/health', (req, res) => {
  res.json({ status: 'ok', timestamp: Date.now() });
});

app.listen(PORT, '0.0.0.0', () => {
  console.log(`🚀 Server listening on http://0.0.0.0:${PORT}`);
  console.log(`📁 Uploads: ${path.resolve(UPLOAD_DIR)}`);
  console.log(`📁 Thumbs: ${path.resolve(THUMB_DIR)}`);
  console.log(`☁️  Cloudinary: ${process.env.CLOUDINARY_CLOUD_NAME || 'dq8watps5'}`);
});
