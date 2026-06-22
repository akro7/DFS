const { Client, LocalAuth } = require('whatsapp-web.js');
const express = require('express');
const { WebSocketServer } = require('ws');
const QRCode = require('qrcode');
const http = require('http');

const app = express();
app.use(express.json());

const server = http.createServer(app);
const wss = new WebSocketServer({ server, path: '/ws' });

const PORT = 3000;

// ─── WhatsApp Client ───────────────────────────────────────────────
const client = new Client({
    authStrategy: new LocalAuth({ clientId: 'akrowats' }),
    puppeteer: {
        headless: true,
        args: ['--no-sandbox', '--disable-setuid-sandbox']
    }
});

// ─── Connected WebSocket clients ──────────────────────────────────
const wsClients = new Set();

wss.on('connection', (ws) => {
    wsClients.add(ws);
    console.log('App connected via WebSocket');

    ws.on('close', () => wsClients.delete(ws));
});

function broadcast(data) {
    const msg = JSON.stringify(data);
    wsClients.forEach(ws => {
        if (ws.readyState === 1) ws.send(msg);
    });
}

// ─── WhatsApp Events ───────────────────────────────────────────────
client.on('qr', async (qr) => {
    console.log('QR received, sending to app...');
    const base64 = await QRCode.toDataURL(qr);
    // Remove "data:image/png;base64," prefix
    const raw = base64.replace('data:image/png;base64,', '');
    broadcast({ type: 'qr', data: raw });
});

client.on('ready', () => {
    const number = client.info.wid.user;
    console.log('WhatsApp ready! Number:', number);
    broadcast({ type: 'ready', number });
});

client.on('message', async (msg) => {
    if (msg.fromMe) return;
    console.log('Message from:', msg.from, '-', msg.body);
    broadcast({
        type: 'message',
        from: msg.from,
        body: msg.body,
        timestamp: msg.timestamp
    });
});

client.on('auth_failure', (msg) => {
    broadcast({ type: 'error', message: 'Auth failed: ' + msg });
});

client.on('disconnected', (reason) => {
    broadcast({ type: 'error', message: 'Disconnected: ' + reason });
});

// ─── REST API ──────────────────────────────────────────────────────

// Send message
app.post('/send', async (req, res) => {
    const { to, body } = req.body;
    if (!to || !body) return res.status(400).json({ error: 'Missing to or body' });

    try {
        // Auto-add @c.us if missing
        const chatId = to.includes('@') ? to : to + '@c.us';
        await client.sendMessage(chatId, body);
        res.json({ success: true });
    } catch (e) {
        res.status(500).json({ error: e.message });
    }
});

// Get chats list
app.get('/chats', async (req, res) => {
    try {
        const chats = await client.getChats();
        const result = chats.slice(0, 50).map(chat => ({
            id: chat.id._serialized,
            name: chat.name,
            lastMessage: chat.lastMessage?.body || '',
            time: chat.lastMessage ? new Date(chat.lastMessage.timestamp * 1000).toLocaleTimeString() : '',
            unread: chat.unreadCount
        }));
        res.json(result);
    } catch (e) {
        res.status(500).json({ error: e.message });
    }
});

// Get messages for a chat
app.get('/messages/:chatId', async (req, res) => {
    try {
        const chatId = decodeURIComponent(req.params.chatId);
        const chat = await client.getChatById(chatId);
        const msgs = await chat.fetchMessages({ limit: 50 });
        const result = msgs.map(m => ({
            id: m.id._serialized,
            body: m.body,
            timestamp: m.timestamp,
            fromMe: m.fromMe
        }));
        res.json(result);
    } catch (e) {
        res.status(500).json({ error: e.message });
    }
});

// Health check
app.get('/status', (req, res) => {
    res.json({ status: 'running', ready: client.info ? true : false });
});

// ─── Start ─────────────────────────────────────────────────────────
server.listen(PORT, '0.0.0.0', () => {
    console.log(`Akrowats server running on port ${PORT}`);
    console.log(`Your phone IP: check with 'ip addr show' or 'ipconfig'`);
});

client.initialize();
