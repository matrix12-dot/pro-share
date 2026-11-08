# SwiftShare ⚡
> تطبيق أندرويد لنقل البيانات والملفات والصور بين الهاتف والحاسوب لاسلكياً وبسرعة فائقة وخصوصية تامة عبر الشبكة المحلية (Local Wi-Fi).

---

## 🌟 المميزات الرئيسية (Key Features)

- **⚡ نقل لاسلكي فائق السرعة:** نقل مباشر بسرعة الشبكة المحلية (تصل إلى 100+ ميجابايت/ثانية على 5GHz) بدون وسيط وبدون استهلاك رصيد الإنترنت.
- **🔒 خصوصية تامة وأمان 100%:** الاتصال مباشر ومحلي (Peer-to-Peer) دون المرور بأي خوادم سحابية.
- **🛡️ رمز أمان اختياري (PIN Code):** قفل الاتصال برمز أمان لمنع أي متطفل على شبكة الـ Wi-Fi من الدخول.
- **💻 بدون تثبيت برامج على الكمبيوتر:** يعمل فوراً عبر أي متصفح (Chrome, Edge, Firefox, Safari) بواسطة بوابة الويب المدمجة.
- **📁 دعم شامل للملفات والصور:**
  - منتقي صور وفيديوهات آمن وبكامل الدقة الأصلية بدون أي ضغط.
  - دعم جميع صيغ الملفات والمستندات (PDF, ZIP, APK, Docs).
  - إمكانية تنزيل جميع الملفات دفعة واحدة في ملف مضغوط (ZIP) بنقرة واحدة.
  - دعم السحب والإفلات (Drag & Drop) من الحاسوب مع مؤشر تقدم وسرعة حية.
- **📋 مزامنة الحافظة (Clipboard Sync):** مشاركة الروابط والنصوص المنسوخة بين الهاتف والحاسوب في الاتجاهين فوراً.
- **📱 نقطة اتصال الهواتف (Hotspot Support):** إمكانية النقل حتى في حال عدم وجود راوتر عبر تشغيل نقطة الاتصال في الهاتف وربط الحاسوب بها.

---

## 🛠️ التقنيات المستخدمة (Tech Stack)

- **Language:** Kotlin
- **UI Toolkit:** Jetpack Compose (Material Design 3)
- **Architecture:** MVVM (Model-View-ViewModel) + Kotlin Coroutines & StateFlow
- **Image Loading:** Coil Compose
- **Server Engine:** Built-in multi-threaded HTTP/1.1 Engine (`ServerSocket` with port-recycling & candidate fallbacks)
- **QR Code:** Pure Kotlin QR Matrix Encoder
- **Min SDK:** 24 (Android 7.0+)
- **Target SDK:** 36 (Android 16 Ready)

---

## 🚀 كيفية تشغيل المشروع وتجربته محلياً

1. استنسخ المستودع (Clone):
   ```bash
   git clone https://github.com/YOUR_USERNAME/SwiftShare.git
   ```
2. افتح المشروع في **Android Studio**.
3. قم بعمل مزامنة لـ Gradle (Sync Project with Gradle Files).
4. شغّل التطبيق على هاتفك أو على المحاكي عبر زر **Run** (أو عبر أمر `gradle assembleDebug`).

---

## 📄 الترخيص (License)
هذا المشروع متاح للاستخدام الحر.
