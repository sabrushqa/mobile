import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import sgMail from "@sendgrid/mail";

admin.initializeApp();

const SENDGRID_API_KEY = functions.config().sendgrid.key;
sgMail.setApiKey(SENDGRID_API_KEY);

export const sendOrderStatusEmail = functions.firestore
  .document('orders/{orderId}')
  .onUpdate(async (change, context) => {
    const before = change.before.data();
    const after = change.after.data();

    if (before.status === after.status) {
      return null;
    }

    const email = after.email;
    const orderId = context.params.orderId;
    const status = after.status;

    const msg = {
      to: email,
      from: 'ton-email-verifie@ton-domaine.com', // Email validé SendGrid
      subject: `Mise à jour de votre commande #${orderId}`,
      text: `Bonjour,\n\nVotre commande n°${orderId} est maintenant : ${status}.\n\nMerci pour votre confiance.`,
    };

    try {
      await sgMail.send(msg);
      console.log("✅ Email envoyé à :", email);
    } catch (error) {
      console.error("❌ Erreur lors de l'envoi de l'email :", error.toString());
    }

    return null;
  });
