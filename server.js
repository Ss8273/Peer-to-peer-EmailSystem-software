// server.js
const express = require('express');
const http = require('http');
const socketIo = require('socket.io');
const sqlite3 = require('sqlite3').verbose();

const app = express();
const server = http.createServer(app);
const io = socketIo(server, { transports: ['websocket', 'polling'] });

// SQLite
const db = new sqlite3.Database('./users.db', (err) => {
    if (err) console.error('Database connection error:', err);
    else {
        console.log('Connected to SQLite database');
        db.run(`CREATE TABLE IF NOT EXISTS users (username TEXT PRIMARY KEY, password TEXT)`);
        db.run(`CREATE TABLE IF NOT EXISTS user_connections (username TEXT PRIMARY KEY, socket_id TEXT, ip_address TEXT, last_connected TIMESTAMP DEFAULT CURRENT_TIMESTAMP)`);
        db.run(`CREATE TABLE IF NOT EXISTS emails (id INTEGER PRIMARY KEY AUTOINCREMENT, sender TEXT, receiver TEXT, subject TEXT, body TEXT, send_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)`);
        db.run(`CREATE TABLE IF NOT EXISTS message (id INTEGER PRIMARY KEY AUTOINCREMENT, sender TEXT, receiver TEXT, subject TEXT, time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)`);
        db.run(`CREATE TABLE IF NOT EXISTS response (id INTEGER PRIMARY KEY AUTOINCREMENT, sender TEXT, receiver TEXT, subject TEXT, time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)`);
    }
});

const clients = {}; // { username: socket }

io.on('connection', (socket) => {
    console.log(`Client connected: ${socket.id}`);

    socket.on('register', (data) => handleRegister(socket, data.username, data.password));
    socket.on('login', (data, callback) => handleLogin(socket, data.username, data.password, callback));
    socket.on('reconnect', (username) => handleReconnect(socket, username));
    socket.on('disconnect', () => handleDisconnect(socket));

    socket.on('send_message', (data) => handleSendMessage(socket, data));
    socket.on('message_action', (data) => handleMessageAction(socket, data));
    socket.on('SdpInfo', (data) => handleSdpInfo(socket, data));
    socket.on('IceInfo', (data) => handleIceInfo(socket, data));
});

server.listen(8080, () => {
    console.log('Server running on port 8080');
});

// ------------------ Handlers ------------------
function handleRegister(socket, username, password) {
    db.get('SELECT username FROM users WHERE username = ?', [username], (err, row) => {
        if (err) return socket.emit("register_response", { status: 'error', message: 'Database error' });
        if (row) return socket.emit("register_response", { status: 'error', message: 'Username already exists.' });
        db.run('INSERT INTO users (username, password) VALUES (?, ?)', [username, password], (err) => {
            if (err) return socket.emit("register_response", { status: 'error', message: 'Error saving user.' });
            socket.emit("register_response", { status: 'success', message: 'Registration successful!' });
        });
    });
}

function handleLogin(socket, username, password, callback) {
    db.get('SELECT username, password FROM users WHERE username = ?', [username], (err, row) => {
        if (err) return callback({ status: 'error', message: 'Database error' });
        if (!row || row.password !== password) return callback({ status: 'error', message: 'Invalid username or password.' });

        if (clients[username]) {
            console.log(`[Login] Overwriting existing socket for ${username}`);
        }
        clients[username] = socket;
        db.run('INSERT OR REPLACE INTO user_connections (username, socket_id, ip_address) VALUES (?, ?, ?)', [username, socket.id, socket.handshake.address]);
        callback({ status: 'success', message: 'Login successful!' });
    });
}

function handleReconnect(socket, username) {
    clients[username] = socket;
    db.run('UPDATE user_connections SET socket_id = ?, last_connected = CURRENT_TIMESTAMP WHERE username = ?', [socket.id, username]);
    socket.emit("reconnect_response", { status: 'success', message: 'Reconnected successfully!' });
}

function handleDisconnect(socket) {
    for (let username in clients) {
        if (clients[username] === socket) {
            delete clients[username];
            db.run('DELETE FROM user_connections WHERE username = ?', [username]);
            console.log(`User ${username} disconnected.`);
            break;
        }
    }
}

function handleSendMessage(socket, data) {
    const { to, from, message, timeslot } = data;
    checkUserExists(to).then(userExists => {
        if (!userExists) return socket.emit('send_message_response', { status: 'error', message: 'User does not exist.' });

        const targetClient = clients[to];
        if (targetClient) {
            targetClient.emit('message', { sender: from, subject: message, time: timeslot });
            socket.emit('send_message_response', { status: 'success', message: 'Message sent successfully!' });
        } else {
            db.run('INSERT INTO message (sender, receiver, subject, time) VALUES (?, ?, ?, ?)', [from, to, message, timeslot]);
            socket.emit('send_message_response', { status: 'error', message: 'User not online.' });
        }
    });
}

function handleMessageAction(socket, data) {
    const { sender, action, username, subject, time } = data;
    if (!sender || !action || !username || !subject || !time) {
        return socket.emit('message_action_response', { status: 'error', message: 'Invalid data format' });
    }

    const targetClient = clients[sender];
    if (targetClient) {
        targetClient.emit('message_action', { sender: username, subject, time, action });
        socket.emit('message_action_response', { status: 'success', message: 'Message action sent successfully!' });
    } else {
        socket.emit('message_action_response', { status: 'error', message: 'Sender not online.' });
    }
}

function handleSdpInfo(socket, data) {
    const { source, destination, type, description } = data;
    checkUserExists(destination).then(userExists => {
        if (!userExists) return socket.emit('sdp_response', { status: 'error', message: 'User does not exist.' });

        const targetClient = clients[destination];
        if (targetClient) {
            targetClient.emit('SdpInfo', { source, destination, type, description });
            socket.emit('sdp_response', { status: 'success', message: 'SDP sent successfully!' });
        } else {
            socket.emit('sdp_response', { status: 'error', message: 'User not online.' });
        }
    });
}

function handleIceInfo(socket, data) {
    const { source, destination, id, label, candidate } = data;
    checkUserExists(destination).then(userExists => {
        if (!userExists) return socket.emit('ice_response', { status: 'error', message: 'User does not exist.' });

        const targetClient = clients[destination];
        if (targetClient) {
            targetClient.emit('IceInfo', { source, destination, id, label, candidate });
            socket.emit('ice_response', { status: 'success', message: 'ICE sent successfully!' });
        } else {
            socket.emit('ice_response', { status: 'error', message: 'User not online.' });
        }
    });
}

function checkUserExists(username) {
    return new Promise((resolve, reject) => {
        db.get('SELECT username FROM users WHERE username = ?', [username], (err, row) => {
            if (err) reject(err);
            else resolve(!!row);
        });
    });
}
