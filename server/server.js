require('dotenv').config();

const express = require('express');
const cors = require('cors');

const app = express();

const PORT = process.env.PORT || 3000;
const GEMINI_API_KEY = process.env.GEMINI_API_KEY;
const GEMINI_MODEL = process.env.GEMINI_MODEL || 'gemini-2.5-flash';

app.use(cors());
app.use(express.json());

app.get('/', (req, res) => {
  res.json({
    status: 'running',
    message: 'Gemini backend is running. Send POST /chat with { "message": "Hello" }'
  });
});

app.post('/chat', async (req, res) => {
  try {
    const userMessage = (req.body.message || '').trim();

    if (!userMessage) {
      return res.status(400).json({
        error: 'Message is required.'
      });
    }

    if (!GEMINI_API_KEY) {
      return res.status(500).json({
        error: 'GEMINI_API_KEY is missing. Add it to server/.env before starting the backend.'
      });
    }

    const geminiUrl =
      `https://generativelanguage.googleapis.com/v1beta/models/${GEMINI_MODEL}:generateContent?key=${GEMINI_API_KEY}`;

    const geminiResponse = await fetch(geminiUrl, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        systemInstruction: {
          parts: [
            {
              text: 'Keep answers clear, simple, and concise.'
            }
          ]
        },
        contents: [
          {
            role: 'user',
            parts: [
              {
                text: userMessage
              }
            ]
          }
        ]
      })
    });

    const data = await geminiResponse.json();

    if (!geminiResponse.ok) {
      return res.status(geminiResponse.status).json({
        error: 'Gemini API error',
        details: data
      });
    }

    const reply =
      data?.candidates?.[0]?.content?.parts
        ?.map((part) => part.text || '')
        .join('')
        .trim() || 'Gemini returned an empty response.';

    return res.json({
      reply: reply
    });

  } catch (error) {
    return res.status(500).json({
      error: 'Server error',
      details: error.message
    });
  }
});

app.listen(PORT, () => {
  console.log(`Gemini backend running at http://localhost:${PORT}`);
});