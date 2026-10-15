# CREAZZY SPACE

واجهة عربية RTL لإدارة مساحات عمل وملفات تعريف مستقلة للتطبيقات والروابط، قابلة للتشغيل على Android عبر Termux وNode.js.

## ما الذي يفعله الإصدار الحالي؟
- واجهة responsive للهاتف والكمبيوتر.
- إنشاء/تعديل/إيقاف/حذف مساحات متعددة، مع تصنيف وبحث وفرز.
- حفظ مستمر محلي في `data/instances.json` عند تشغيل الخادم.
- وضع بديل يحفظ في `localStorage` عند فتح الواجهة دون الخادم.
- تصدير واستيراد نسخة احتياطية JSON.
- API محلي: `GET /healthz`, `GET /api/instances`, `PUT /api/instances`.

## تنبيه تقني مهم
هذا المشروع **ليس** بديلاً أصلياً عن Parallel Space ولا يستنسخ APKs أو ينشئ Android users/containers. صفحة ويب وخادم Node.js لا يمكنهما عزل عمليات Android أو توفير Google Play Services لكل نسخة. تشغيل تطبيقات Android بنسخ متزامنة مستقلة يتطلب مكوّناً أصلياً/بيئة افتراضية على الجهاز، مع توافق خدمات Google ومتطلبات المطور وسياسات التطبيقات. لا تدّعِ أن هذه اللوحة تحقق ذلك؛ إنها لوحة إدارة أولية قابلة للتوسعة.

## التشغيل على Termux
يتطلب Node.js 18 أو أحدث، ولا يحتاج إلى npm packages.

```sh
pkg update
pkg install nodejs git
git clone https://github.com/c83491307-gif/Crazy-ai.git
cd Crazy-ai
```

إذا كان المستودع يحتوي مشاريع أخرى، شغّل المشروع من مجلده الفرعي الذي يحتوي `package.json`. أو احفظ ملفات هذا المشروع في مجلد منفصل كما يلي:

```sh
mkdir -p ~/creazzy-space/public
# ضع index.html داخل ~/creazzy-space/public/
# ضع server.mjs و package.json داخل ~/creazzy-space/
cd ~/creazzy-space
npm start
```

افتح في متصفح الهاتف: `http://127.0.0.1:8787`

لإيقاف الخادم: اضغط `Ctrl+C`.

## الشبكة والأمان
الخادم يستمع افتراضياً على `127.0.0.1` فقط، فلا تعرضه على الإنترنت أو شبكة Wi-Fi. لتغيير المنفذ: `PORT=9090 npm start`. لا تخزن كلمات مرور أو مفاتيح API داخل بيانات المساحات. قبل أي استخدام متعدد المستخدمين، أضف المصادقة والحماية من CSRF وحدود المعدل.

## الملفات
- `public/index.html` — واجهة التطبيق.
- `server.mjs` — خادم Node.js وتخزين JSON.
- `package.json` — أوامر التشغيل.
- `.gitignore` — يستثني البيانات المحلية.
