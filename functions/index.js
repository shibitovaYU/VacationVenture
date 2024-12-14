const functions = require("firebase-functions");
const nodemailer = require("nodemailer");

const gmailEmail = functions.config().gmail.email;
const gmailPassword = functions.config().gmail.password;

const transporter = nodemailer.createTransport({
  service: "gmail",
  auth: {
    user: gmailEmail,
    pass: gmailPassword,
  },
});

exports.sendSupportEmail = functions.https.onCall((data, context) => {
  const mailOptions = {
    from: gmailEmail,
    to: "support@example.com", // Почта службы поддержки
    subject: `Запрос от пользователя: ${data.userEmail}`,
    text: data.message,
  };

  return transporter.sendMail(mailOptions)
      .then(() => {
        return {
          success: true,
          message: "Email отправлен",
        };
      })
      .catch((error) => {
        console.error("Ошибка отправки email:", error);
        throw new functions.https.HttpsError(
            "internal",
            "Ошибка отправки email",
        );
      });
});
