# Akrowats 🟢

WhatsApp client app بتصميم واتساب الرسمي — Java Android + Node.js Bridge

---

## 🗂 هيكل المشروع

```
Akrowats/
├── app/                    ← Android App (Java)
│   └── src/main/
│       ├── java/com/akro/akrowats/
│       │   ├── SplashActivity.java
│       │   ├── QrActivity.java      ← أدخل URL وامسح QR
│       │   ├── MainActivity.java    ← قائمة المحادثات
│       │   ├── ChatActivity.java    ← شاشة المحادثة
│       │   ├── ApiClient.java       ← HTTP + WebSocket
│       │   ├── ChatItem.java
│       │   ├── MessageItem.java
│       │   ├── ChatAdapter.java
│       │   └── MessageAdapter.java
│       └── res/
└── server/                 ← Node.js Bridge
    ├── index.js
    └── package.json
```

---

## 🚀 طريقة التشغيل

### 1. Server (على جهازك أو VPS)
```bash
cd server
npm install
node index.js
```

### 2. امسح QR
- افتح التطبيق
- أدخل IP الجهاز اللي شغال عليه السيرفر مثلاً: `http://192.168.1.5:3000`
- اضغط Connect
- هيظهر QR — امسحه بواتساب الرسمي

### 3. استخدام
- بعد المسح التطبيق هيفتح قائمة المحادثات
- اضغط على أي محادثة للدردشة
- أو اضغط ⋮ → New Chat وأدخل الرقم

---

## ⚠️ ملاحظة
- `whatsapp-web.js` مش رسمي من Meta
- ممكن الحساب يتبان لو استخدامه كتير
- للاستخدام الشخصي فقط
