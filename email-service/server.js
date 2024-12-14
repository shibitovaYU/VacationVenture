// server.js
const express = require('express');
const nodemailer = require('nodemailer');
require('dotenv').config();

const app = express();
app.use(express.json());

app.post('/sendSupportEmail', async (req, res) => {
    const { userEmail, message } = req.body;
    const supportEmail = 'support@example.com'; // Email службы поддержки

    const transporter = nodemailer.createTransport({
        service: 'gmail',
        auth: {
            user: process.env.GMAIL_EMAIL,   // ваш email
            pass: process.env.GMAIL_PASSWORD // пароль приложения
        }
    });

    const mailOptions = {
        from: process.env.GMAIL_EMAIL,
        to: supportEmail,
        subject: `Запрос от пользователя: ${userEmail}`,
        text: message
    };

    try {
        await transporter.sendMail(mailOptions);
        res.status(200).send({ success: true, message: 'Email отправлен' });
    } catch (error) {
        console.error('Ошибка отправки email:', error);
        res.status(500).send({ success: false, message: 'Ошибка отправки email' });
    }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, "0.0.0.0", () => {
    console.log(`Сервер запущен на порту ${PORT}`);
});
