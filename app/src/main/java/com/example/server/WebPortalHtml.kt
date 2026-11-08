package com.example.server

object WebPortalHtml {

    fun getHtml(): String {
        return """
<!DOCTYPE html>
<html lang="ar" dir="rtl">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SwiftShare - نقل البيانات فائق السرعة</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <style>
        :root {
            --bg-primary: #070f1e;
            --bg-card: #0d1b2a;
            --bg-card-hover: #132238;
            --border-color: #1b2a47;
            --accent-cyan: #00d2ff;
            --accent-blue: #0284c7;
            --accent-green: #10b981;
            --accent-red: #ef4444;
            --text-primary: #f8fafc;
            --text-secondary: #94a3b8;
            --radius-lg: 16px;
            --radius-md: 12px;
            --radius-sm: 8px;
            --font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
        }

        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
            font-family: var(--font-family);
        }

        body {
            background-color: var(--bg-primary);
            color: var(--text-primary);
            min-height: 100vh;
            display: flex;
            flex-direction: column;
            overflow-x: hidden;
        }

        /* Top Bar */
        header {
            background: rgba(13, 27, 42, 0.85);
            backdrop-filter: blur(12px);
            border-bottom: 1px solid var(--border-color);
            position: sticky;
            top: 0;
            z-index: 100;
            padding: 16px 24px;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }

        .brand-container {
            display: flex;
            align-items: center;
            gap: 12px;
        }

        .brand-logo {
            width: 40px;
            height: 40px;
            background: linear-gradient(135deg, var(--accent-cyan), var(--accent-blue));
            border-radius: 10px;
            display: flex;
            align-items: center;
            justify-content: center;
            box-shadow: 0 4px 14px rgba(0, 210, 255, 0.35);
        }

        .brand-logo svg {
            width: 24px;
            height: 24px;
            fill: #000;
        }

        .brand-title {
            font-size: 20px;
            font-weight: 700;
            background: linear-gradient(90deg, #fff, var(--accent-cyan));
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
        }

        .brand-subtitle {
            font-size: 12px;
            color: var(--text-secondary);
        }

        .header-actions {
            display: flex;
            align-items: center;
            gap: 12px;
        }

        .status-pill {
            display: flex;
            align-items: center;
            gap: 8px;
            background: rgba(16, 185, 129, 0.15);
            border: 1px solid rgba(16, 185, 129, 0.3);
            color: var(--accent-green);
            padding: 6px 14px;
            border-radius: 50px;
            font-size: 13px;
            font-weight: 600;
        }

        .pulse-dot {
            width: 8px;
            height: 8px;
            background-color: var(--accent-green);
            border-radius: 50%;
            animation: pulse 1.5s infinite;
        }

        @keyframes pulse {
            0% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.7); }
            70% { transform: scale(1); box-shadow: 0 0 0 8px rgba(16, 185, 129, 0); }
            100% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(16, 185, 129, 0); }
        }

        .lang-btn {
            background: var(--bg-card);
            border: 1px solid var(--border-color);
            color: var(--text-primary);
            padding: 6px 12px;
            border-radius: var(--radius-sm);
            cursor: pointer;
            font-size: 13px;
            transition: all 0.2s;
        }

        .lang-btn:hover {
            border-color: var(--accent-cyan);
        }

        /* Container & Navigation */
        main {
            flex: 1;
            max-width: 1080px;
            width: 100%;
            margin: 0 auto;
            padding: 24px;
        }

        .nav-tabs {
            display: flex;
            gap: 10px;
            background: var(--bg-card);
            padding: 6px;
            border-radius: var(--radius-md);
            border: 1px solid var(--border-color);
            margin-bottom: 24px;
        }

        .nav-tab {
            flex: 1;
            padding: 12px;
            border: none;
            background: transparent;
            color: var(--text-secondary);
            font-size: 15px;
            font-weight: 600;
            border-radius: var(--radius-sm);
            cursor: pointer;
            transition: all 0.2s;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
        }

        .nav-tab.active {
            background: linear-gradient(135deg, rgba(0, 210, 255, 0.15), rgba(2, 132, 199, 0.2));
            color: var(--accent-cyan);
            border: 1px solid rgba(0, 210, 255, 0.3);
        }

        /* Sections */
        .tab-content {
            display: none;
        }

        .tab-content.active {
            display: block;
        }

        /* Upload Zone */
        .dropzone {
            border: 2px dashed rgba(0, 210, 255, 0.4);
            background: rgba(13, 27, 42, 0.6);
            border-radius: var(--radius-lg);
            padding: 48px 24px;
            text-align: center;
            cursor: pointer;
            transition: all 0.3s;
            position: relative;
        }

        .dropzone:hover, .dropzone.dragover {
            border-color: var(--accent-cyan);
            background: rgba(0, 210, 255, 0.05);
            transform: translateY(-2px);
            box-shadow: 0 10px 30px rgba(0, 210, 255, 0.1);
        }

        .dropzone input[type="file"] {
            position: absolute;
            inset: 0;
            opacity: 0;
            cursor: pointer;
            width: 100%;
            height: 100%;
        }

        .drop-icon {
            width: 64px;
            height: 64px;
            margin: 0 auto 16px;
            background: rgba(0, 210, 255, 0.1);
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            color: var(--accent-cyan);
        }

        .drop-icon svg {
            width: 36px;
            height: 36px;
            stroke: currentColor;
            stroke-width: 2;
            fill: none;
        }

        .drop-title {
            font-size: 18px;
            font-weight: 700;
            margin-bottom: 8px;
        }

        .drop-subtitle {
            font-size: 14px;
            color: var(--text-secondary);
        }

        /* Upload Queue List */
        .queue-container {
            margin-top: 24px;
            display: flex;
            flex-direction: column;
            gap: 12px;
        }

        .queue-item {
            background: var(--bg-card);
            border: 1px solid var(--border-color);
            border-radius: var(--radius-md);
            padding: 16px;
            display: flex;
            flex-direction: column;
            gap: 10px;
        }

        .queue-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
        }

        .file-info {
            display: flex;
            align-items: center;
            gap: 12px;
        }

        .file-badge {
            width: 36px;
            height: 36px;
            border-radius: 8px;
            background: rgba(2, 132, 199, 0.2);
            color: var(--accent-cyan);
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: 700;
            font-size: 11px;
            text-transform: uppercase;
        }

        .file-name {
            font-weight: 600;
            font-size: 14px;
            max-width: 300px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }

        .file-size {
            font-size: 12px;
            color: var(--text-secondary);
        }

        .progress-bar-container {
            background: rgba(255, 255, 255, 0.08);
            height: 6px;
            border-radius: 4px;
            overflow: hidden;
        }

        .progress-bar {
            height: 100%;
            background: linear-gradient(90deg, var(--accent-blue), var(--accent-cyan));
            width: 0%;
            transition: width 0.2s;
        }

        /* Download Section */
        .section-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 16px;
        }

        .section-title {
            font-size: 18px;
            font-weight: 700;
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .btn-primary {
            background: linear-gradient(135deg, var(--accent-cyan), var(--accent-blue));
            color: #000;
            border: none;
            padding: 10px 20px;
            border-radius: var(--radius-sm);
            font-weight: 700;
            cursor: pointer;
            display: flex;
            align-items: center;
            gap: 8px;
            transition: all 0.2s;
            text-decoration: none;
        }

        .btn-primary:hover {
            opacity: 0.9;
            transform: translateY(-1px);
            box-shadow: 0 4px 16px rgba(0, 210, 255, 0.3);
        }

        .file-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
            gap: 16px;
        }

        .file-card {
            background: var(--bg-card);
            border: 1px solid var(--border-color);
            border-radius: var(--radius-md);
            padding: 16px;
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            gap: 12px;
            transition: all 0.2s;
        }

        .file-card:hover {
            border-color: var(--accent-cyan);
            background: var(--bg-card-hover);
        }

        .file-card-top {
            display: flex;
            align-items: flex-start;
            gap: 12px;
        }

        .file-download-btn {
            background: rgba(0, 210, 255, 0.1);
            color: var(--accent-cyan);
            border: 1px solid rgba(0, 210, 255, 0.3);
            border-radius: var(--radius-sm);
            padding: 8px 14px;
            font-weight: 600;
            font-size: 13px;
            text-decoration: none;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 6px;
            cursor: pointer;
            transition: all 0.2s;
        }

        .file-download-btn:hover {
            background: var(--accent-cyan);
            color: #000;
        }

        /* Clipboard Card */
        .clipboard-box {
            background: var(--bg-card);
            border: 1px solid var(--border-color);
            border-radius: var(--radius-md);
            padding: 20px;
            display: flex;
            flex-direction: column;
            gap: 16px;
        }

        .clipboard-textarea {
            width: 100%;
            height: 140px;
            background: rgba(7, 15, 30, 0.8);
            border: 1px solid var(--border-color);
            border-radius: var(--radius-sm);
            color: var(--text-primary);
            padding: 12px;
            font-size: 14px;
            resize: vertical;
        }

        .clipboard-textarea:focus {
            outline: none;
            border-color: var(--accent-cyan);
        }

        /* Privacy Banner */
        .privacy-box {
            margin-top: 32px;
            background: rgba(13, 27, 42, 0.4);
            border: 1px solid rgba(27, 42, 71, 0.6);
            border-radius: var(--radius-md);
            padding: 16px 20px;
            display: flex;
            align-items: center;
            gap: 16px;
            font-size: 13px;
            color: var(--text-secondary);
        }

        .privacy-icon {
            color: var(--accent-green);
            flex-shrink: 0;
        }

        /* Empty state */
        .empty-placeholder {
            text-align: center;
            padding: 48px 16px;
            color: var(--text-secondary);
        }

        /* PIN Modal */
        .pin-modal-overlay {
            position: fixed;
            inset: 0;
            background: rgba(7, 15, 30, 0.85);
            backdrop-filter: blur(8px);
            display: none;
            align-items: center;
            justify-content: center;
            z-index: 1000;
        }

        .pin-modal {
            background: var(--bg-card);
            border: 1px solid var(--border-color);
            border-radius: var(--radius-lg);
            padding: 32px;
            width: 90%;
            max-width: 400px;
            text-align: center;
            display: flex;
            flex-direction: column;
            gap: 20px;
        }

        .pin-input {
            width: 160px;
            margin: 0 auto;
            text-align: center;
            font-size: 28px;
            letter-spacing: 8px;
            padding: 8px 12px;
            background: rgba(7, 15, 30, 0.8);
            border: 2px solid var(--border-color);
            border-radius: var(--radius-sm);
            color: var(--accent-cyan);
        }

        .pin-input:focus {
            outline: none;
            border-color: var(--accent-cyan);
        }
    </style>
</head>
<body>

    <header>
        <div class="brand-container">
            <div class="brand-logo">
                <svg viewBox="0 0 24 24"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 14.5v-9l6 4.5-6 4.5z"/></svg>
            </div>
            <div>
                <h1 class="brand-title">SwiftShare</h1>
                <p class="brand-subtitle" id="subtitle-text">نقل لاسلكي مباشر فائق السرعة</p>
            </div>
        </div>

        <div class="header-actions">
            <div class="status-pill">
                <span class="pulse-dot"></span>
                <span id="conn-status-text">متصل بالهاتف (شبكة محلية)</span>
            </div>
            <button class="lang-btn" onclick="toggleLanguage()" id="lang-btn">English</button>
        </div>
    </header>

    <main>
        <nav class="nav-tabs">
            <button class="nav-tab active" onclick="switchTab('download')" id="tab-btn-download">
                📥 <span id="tab-text-download">تنزيل من الهاتف</span> (<span id="download-count">0</span>)
            </button>
            <button class="nav-tab" onclick="switchTab('upload')" id="tab-btn-upload">
                📤 <span id="tab-text-upload">إرسال للهاتف</span>
            </button>
            <button class="nav-tab" onclick="switchTab('clipboard')" id="tab-btn-clipboard">
                📋 <span id="tab-text-clipboard">الحافظة المشتركة</span>
            </button>
        </nav>

        <!-- Tab 1: Download from Phone -->
        <section id="tab-download" class="tab-content active">
            <div class="section-header">
                <h2 class="section-title">
                    <span id="down-title">الملفات الجاهزة للتنزيل من هاتفك</span>
                </h2>
                <a href="/download-all" id="download-all-btn" class="btn-primary" style="display:none;">
                    📦 <span id="btn-text-download-all">تنزيل الكل (ملف مضغوط ZIP)</span>
                </a>
            </div>

            <div id="file-grid-container" class="file-grid"></div>
            <div id="no-files-placeholder" class="empty-placeholder">
                <p id="empty-down-msg">لم يتم تحديد ملفات في الهاتف بعد. قم باختيار الصور أو الملفات في تطبيق SwiftShare على هاتفك لتظهر هنا فوراً!</p>
            </div>
        </section>

        <!-- Tab 2: Send to Phone -->
        <section id="tab-upload" class="tab-content">
            <div class="dropzone" id="dropzone">
                <input type="file" id="file-picker" multiple>
                <div class="drop-icon">
                    <svg viewBox="0 0 24 24"><path d="M12 4v12m0-12l-4 4m4-4l4 4m-8 12h8"/></svg>
                </div>
                <h3 class="drop-title" id="drop-main-text">اسحب وأفلت الملفات هنا للإرسال المباشر</h3>
                <p class="drop-subtitle" id="drop-sub-text">أو اضغط لاختيار صور، فيديوهات، أو أي مستندات من حاسوبك</p>
            </div>

            <div class="queue-container" id="upload-queue"></div>
        </section>

        <!-- Tab 3: Clipboard -->
        <section id="tab-clipboard" class="tab-content">
            <div class="clipboard-box">
                <h3 style="font-size: 16px; font-weight: 600;" id="clip-title">مزامنة النصوص والحافظة والروابط</h3>
                <textarea id="clipboard-input" class="clipboard-textarea" placeholder="اكتب أو الصق نصاً أو رابطاً هنا لإرساله إلى حافظة هاتفك فوراً..."></textarea>
                <div style="display: flex; gap: 12px; justify-content: flex-end;">
                    <button class="btn-primary" onclick="sendClipboard()" id="clip-send-btn">
                        🚀 <span id="btn-text-send-clip">إرسال لهاتفي</span>
                    </button>
                    <button class="lang-btn" onclick="fetchClipboard()" id="clip-refresh-btn">
                        🔄 <span id="btn-text-fetch-clip">جلب من الهاتف</span>
                    </button>
                </div>
            </div>
        </section>

        <div class="privacy-box">
            <div class="privacy-icon">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
                </svg>
            </div>
            <p id="privacy-text">
                🔒 خصوصية تامة وأمان مطلق: يتم نقل البيانات لاسلكياً وبشكل مباشر بين هاتفك وحاسوبك عبر الشبكة المحلية (Local Wi-Fi) دون المرور بأي خادم سحابي أو اتصال بالإنترنت.
            </p>
        </div>
    </main>

    <!-- PIN Code Modal -->
    <div id="pin-modal-overlay" class="pin-modal-overlay">
        <div class="pin-modal">
            <h2 id="pin-modal-title">رمز الحماية مطلوب</h2>
            <p id="pin-modal-desc" style="color: var(--text-secondary); font-size: 14px;">أدخل رمز الأمان (PIN) الموضح على شاشة هاتفك للاتصال:</p>
            <input type="password" id="pin-input" class="pin-input" maxlength="6" autofocus placeholder="****">
            <button class="btn-primary" style="justify-content: center;" onclick="verifyPin()" id="pin-btn">تأكيد الاتصال</button>
            <p id="pin-error" style="color: var(--accent-red); font-size: 13px; display: none;">رمز الحماية غير صحيح!</p>
        </div>
    </div>

    <script>
        let currentLang = 'ar';
        let isPinRequired = false;
        let authToken = sessionStorage.getItem('swift_pin') || '';

        const translations = {
            ar: {
                subtitle: 'نقل لاسلكي مباشر فائق السرعة',
                connStatus: 'متصل بالهاتف (شبكة محلية)',
                tabDownload: 'تنزيل من الهاتف',
                tabUpload: 'إرسال للهاتف',
                tabClipboard: 'الحافظة المشتركة',
                downTitle: 'الملفات الجاهزة للتنزيل من هاتفك',
                downAll: 'تنزيل الكل (ملف مضغوط ZIP)',
                emptyDown: 'لم يتم تحديد ملفات في الهاتف بعد. قم باختيار الصور أو الملفات في تطبيق SwiftShare على هاتفك لتظهر هنا فوراً!',
                dropMain: 'اسحب وأفلت الملفات هنا للإرسال المباشر',
                dropSub: 'أو اضغط لاختيار صور، فيديوهات، أو أي مستندات من حاسوبك',
                clipTitle: 'مزامنة النصوص والحافظة والروابط',
                clipPlaceholder: 'اكتب أو الصق نصاً أو رابطاً هنا لإرساله إلى حافظة هاتفك فوراً...',
                clipSend: 'إرسال لهاتفي',
                clipRefresh: 'جلب من الهاتف',
                privacy: '🔒 خصوصية تامة وأمان مطلق: يتم نقل البيانات لاسلكياً وبشكل مباشر بين هاتفك وحاسوبك عبر الشبكة المحلية (Local Wi-Fi) دون المرور بأي خادم سحابي أو اتصال بالإنترنت.',
                langBtn: 'English'
            },
            en: {
                subtitle: 'Ultra-Fast Direct Wireless Transfer',
                connStatus: 'Connected to Phone (Local LAN)',
                tabDownload: 'Download from Phone',
                tabUpload: 'Send to Phone',
                tabClipboard: 'Shared Clipboard',
                downTitle: 'Files ready to download from your phone',
                downAll: 'Download All (ZIP Archive)',
                emptyDown: 'No files staged on phone yet. Select photos or documents inside the SwiftShare app on your phone to see them here!',
                dropMain: 'Drag and drop files here for instant transfer',
                dropSub: 'Or click to select photos, videos, or documents from your PC',
                clipTitle: 'Text, Links & Clipboard Synchronization',
                clipPlaceholder: 'Type or paste text or link here to instantly send to phone clipboard...',
                clipSend: 'Send to Phone',
                clipRefresh: 'Fetch from Phone',
                privacy: '🔒 100% Private & Secure: Transfers occur directly over your local Wi-Fi without passing through any external servers or the internet.',
                langBtn: 'العربية'
            }
        };

        function toggleLanguage() {
            currentLang = currentLang === 'ar' ? 'en' : 'ar';
            document.documentElement.dir = currentLang === 'ar' ? 'rtl' : 'ltr';
            document.documentElement.lang = currentLang;
            const t = translations[currentLang];
            document.getElementById('subtitle-text').textContent = t.subtitle;
            document.getElementById('conn-status-text').textContent = t.connStatus;
            document.getElementById('tab-text-download').textContent = t.tabDownload;
            document.getElementById('tab-text-upload').textContent = t.tabUpload;
            document.getElementById('tab-text-clipboard').textContent = t.tabClipboard;
            document.getElementById('down-title').textContent = t.downTitle;
            document.getElementById('btn-text-download-all').textContent = t.downAll;
            document.getElementById('empty-down-msg').textContent = t.emptyDown;
            document.getElementById('drop-main-text').textContent = t.dropMain;
            document.getElementById('drop-sub-text').textContent = t.dropSub;
            document.getElementById('clip-title').textContent = t.clipTitle;
            document.getElementById('clipboard-input').placeholder = t.clipPlaceholder;
            document.getElementById('btn-text-send-clip').textContent = t.clipSend;
            document.getElementById('btn-text-fetch-clip').textContent = t.clipRefresh;
            document.getElementById('privacy-text').textContent = t.privacy;
            document.getElementById('lang-btn').textContent = t.langBtn;
        }

        function switchTab(tab) {
            document.querySelectorAll('.nav-tab').forEach(b => b.classList.remove('active'));
            document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
            document.getElementById('tab-btn-' + tab).classList.add('active');
            document.getElementById('tab-' + tab).classList.add('active');
            if (tab === 'clipboard') fetchClipboard();
        }

        // Fetch shared files
        async function fetchFiles() {
            try {
                const res = await fetch('/api/files', {
                    headers: { 'X-Auth-Token': authToken }
                });
                if (res.status === 401) {
                    showPinModal();
                    return;
                }
                const data = await res.json();
                renderFiles(data.files || []);
            } catch (err) {
                console.error('Fetch files failed:', err);
            }
        }

        function renderFiles(files) {
            const container = document.getElementById('file-grid-container');
            const placeholder = document.getElementById('no-files-placeholder');
            const countSpan = document.getElementById('download-count');
            const downloadAllBtn = document.getElementById('download-all-btn');

            countSpan.textContent = files.length;
            if (files.length === 0) {
                container.innerHTML = '';
                placeholder.style.display = 'block';
                downloadAllBtn.style.display = 'none';
                return;
            }

            placeholder.style.display = 'none';
            downloadAllBtn.style.display = 'flex';

            container.innerHTML = '';
            files.forEach(f => {
                const isImg = f.mimeType && f.mimeType.startsWith('image/');
                const iconOrThumb = isImg 
                    ? '<img src="/api/download?id=' + f.id + '&thumb=true" style="width: 44px; height: 44px; object-fit: cover; border-radius: 8px;">'
                    : '<div class="file-badge">' + ((f.name.split('.').pop() || 'FILE').slice(0, 4)) + '</div>';

                const card = document.createElement('div');
                card.className = 'file-card';
                card.innerHTML = 
                    '<div class="file-card-top">' + iconOrThumb +
                    '<div style="flex: 1; min-width: 0;">' +
                    '<div class="file-name" title="' + f.name + '">' + f.name + '</div>' +
                    '<div class="file-size">' + f.formattedSize + '</div>' +
                    '</div></div>' +
                    '<a href="/api/download?id=' + f.id + '" class="file-download-btn" download="' + f.name + '">' +
                    '⬇️ <span>' + (currentLang === 'ar' ? 'تنزيل الملف' : 'Download') + '</span></a>';
                container.appendChild(card);
            });
        }

        // Uploading files with XMLHttpRequest for progress tracking
        const dropzone = document.getElementById('dropzone');
        const filePicker = document.getElementById('file-picker');
        const uploadQueue = document.getElementById('upload-queue');

        dropzone.addEventListener('dragover', (e) => { e.preventDefault(); dropzone.classList.add('dragover'); });
        dropzone.addEventListener('dragleave', () => dropzone.classList.remove('dragover'));
        dropzone.addEventListener('drop', (e) => {
            e.preventDefault();
            dropzone.classList.remove('dragover');
            if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
                handleUploadFiles(e.dataTransfer.files);
            }
        });

        filePicker.addEventListener('change', (e) => {
            if (e.target.files && e.target.files.length > 0) {
                handleUploadFiles(e.target.files);
                filePicker.value = '';
            }
        });

        function handleUploadFiles(files) {
            for (let i = 0; i < files.length; i++) {
                uploadSingleFile(files[i]);
            }
        }

        function formatSize(bytes) {
            if (bytes === 0) return '0 B';
            const k = 1024;
            const sizes = ['B', 'KB', 'MB', 'GB'];
            const i = Math.floor(Math.log(bytes) / Math.log(k));
            return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
        }

        function uploadSingleFile(file) {
            const fileId = 'file_' + Math.random().toString(36).substring(2, 9);
            const item = document.createElement('div');
            item.className = 'queue-item';
            item.id = fileId;

            item.innerHTML = 
                '<div class="queue-header">' +
                '<div class="file-info">' +
                '<div class="file-badge">UP</div>' +
                '<div>' +
                '<div class="file-name">' + file.name + '</div>' +
                '<div class="file-size">' + formatSize(file.size) + ' • <span class="speed-label">0 MB/s</span></div>' +
                '</div></div>' +
                '<span class="status-label" style="font-size: 13px; font-weight: 600; color: var(--accent-cyan);">0%</span>' +
                '</div>' +
                '<div class="progress-bar-container">' +
                '<div class="progress-bar"></div></div>';
            uploadQueue.prepend(item);

            const progressBar = item.querySelector('.progress-bar');
            const statusLabel = item.querySelector('.status-label');
            const speedLabel = item.querySelector('.speed-label');

            const xhr = new XMLHttpRequest();
            const startTime = Date.now();
            let lastLoaded = 0;
            let lastTime = startTime;

            xhr.upload.addEventListener('progress', (e) => {
                if (e.lengthComputable) {
                    const percent = Math.round((e.loaded / e.total) * 100);
                    progressBar.style.width = percent + '%';
                    statusLabel.textContent = percent + '%';

                    const now = Date.now();
                    const diffTime = (now - lastTime) / 1000;
                    if (diffTime > 0.5) {
                        const diffBytes = e.loaded - lastLoaded;
                        const speedMBps = (diffBytes / diffTime / (1024 * 1024)).toFixed(1);
                        speedLabel.textContent = speedMBps + ' MB/s';
                        lastLoaded = e.loaded;
                        lastTime = now;
                    }
                }
            });

            xhr.addEventListener('load', () => {
                if (xhr.status >= 200 && xhr.status < 300) {
                    progressBar.style.width = '100%';
                    progressBar.style.background = 'var(--accent-green)';
                    statusLabel.innerHTML = '✅ ' + (currentLang === 'ar' ? 'تم النقل بنجاح' : 'Done');
                    statusLabel.style.color = 'var(--accent-green)';
                    speedLabel.textContent = (currentLang === 'ar' ? 'اكتمل' : 'Completed');
                } else {
                    statusLabel.textContent = '❌ ' + (currentLang === 'ar' ? 'فشل' : 'Failed');
                    statusLabel.style.color = 'var(--accent-red)';
                }
            });

            xhr.addEventListener('error', () => {
                statusLabel.textContent = '❌ ' + (currentLang === 'ar' ? 'خطأ في النقل' : 'Error');
                statusLabel.style.color = 'var(--accent-red)';
            });

            xhr.open('POST', '/api/upload?filename=' + encodeURIComponent(file.name));
            if (authToken) xhr.setRequestHeader('X-Auth-Token', authToken);
            xhr.send(file);
        }

        // Clipboard
        async function fetchClipboard() {
            try {
                const res = await fetch('/api/clipboard', {
                    headers: { 'X-Auth-Token': authToken }
                });
                if (res.ok) {
                    const text = await res.text();
                    document.getElementById('clipboard-input').value = text;
                }
            } catch (e) {
                console.error(e);
            }
        }

        async function sendClipboard() {
            const text = document.getElementById('clipboard-input').value;
            const btn = document.getElementById('clip-send-btn');
            try {
                await fetch('/api/clipboard', {
                    method: 'POST',
                    headers: { 'X-Auth-Token': authToken, 'Content-Type': 'text/plain; charset=UTF-8' },
                    body: text
                });
                btn.style.background = 'var(--accent-green)';
                setTimeout(() => {
                    btn.style.background = '';
                }, 1200);
            } catch (e) {
                alert('Failed to send text');
            }
        }

        // PIN Security
        function showPinModal() {
            document.getElementById('pin-modal-overlay').style.display = 'flex';
        }

        async function verifyPin() {
            const pin = document.getElementById('pin-input').value.trim();
            const res = await fetch('/api/verify-pin?pin=' + encodeURIComponent(pin));
            if (res.ok) {
                const data = await res.json();
                authToken = data.token;
                sessionStorage.setItem('swift_pin', authToken);
                document.getElementById('pin-modal-overlay').style.display = 'none';
                fetchFiles();
            } else {
                document.getElementById('pin-error').style.display = 'block';
            }
        }

        // Periodic poll for file list sync
        fetchFiles();
        setInterval(fetchFiles, 3000);
    </script>
</body>
</html>
        """.trimIndent()
    }
}
