    const PAGE_SIZE = 10;

    // Umumiy pagination controllerini chizadi
    function renderPagination(containerId, totalItems, currentPage, onPageChange) {
    const container = document.getElementById(containerId);
    const totalPages = Math.ceil(totalItems / PAGE_SIZE);

    if (totalPages <= 1) {
    container.style.display = 'none';
    return;
}

    container.style.display = 'flex';

    const start = (currentPage - 1) * PAGE_SIZE + 1;
    const end = Math.min(currentPage * PAGE_SIZE, totalItems);

    let html = '<div style="font-size:12px; color:var(--color-sub);">' +
    start + '–' + end + ' / ' + totalItems + ' ta</div>' +
    '<div style="display:flex; gap:4px; align-items:center;">';

    html += '<button class="page-btn" ' + (currentPage === 1 ? 'disabled' : '') +
    ' data-page="' + (currentPage - 1) + '">‹</button>';

    // Sahifa raqamlari (max 5 ta ko'rsatamiz)
    let startPage = Math.max(1, currentPage - 2);
    let endPage = Math.min(totalPages, startPage + 4);
    if (endPage - startPage < 4) startPage = Math.max(1, endPage - 4);

    if (startPage > 1) {
    html += '<button class="page-btn" data-page="1">1</button>';
    if (startPage > 2) html += '<span style="padding:0 4px;">…</span>';
}

    for (let p = startPage; p <= endPage; p++) {
    html += '<button class="page-btn ' + (p === currentPage ? 'active' : '') +
    '" data-page="' + p + '">' + p + '</button>';
}

    if (endPage < totalPages) {
    if (endPage < totalPages - 1) html += '<span style="padding:0 4px;">…</span>';
    html += '<button class="page-btn" data-page="' + totalPages + '">' + totalPages + '</button>';
}

    html += '<button class="page-btn" ' + (currentPage === totalPages ? 'disabled' : '') +
    ' data-page="' + (currentPage + 1) + '">›</button>';

    html += '</div>';
    container.innerHTML = html;

    container.querySelectorAll('.page-btn:not([disabled])').forEach(function(btn) {
    btn.addEventListener('click', function() {
    onPageChange(Number(this.getAttribute('data-page')));
});
});
}
    // Date
    const d = new Date();
    document.getElementById('currentDate').textContent =
    d.toLocaleDateString('uz-UZ', { year:'numeric', month:'long', day:'numeric' });

    // ===== To'lanmagan jadvallarni yuklash =====
    let unpaidFullData = [];
    let unpaidCurrentPage = 1;

    function loadUnpaid() {
    fetch('/admin/get-last-muddat', {
        method: 'GET',
        headers: { 'Accept': 'application/json' }
    })
        .then(function(res) {
            if (!res.ok) return Promise.reject(res.status);
            return res.json();
        })
        .then(function(data) {
            document.getElementById('unpaidLoading').style.display = 'none';

            if (!data || data.length === 0) {
                document.getElementById('unpaidEmpty').style.display = 'block';
                document.getElementById('unpaidSummaryCard').style.display = 'none';
                return;
            }

            unpaidFullData = data;

            var badge = document.getElementById('unpaidBadge');
            badge.textContent = data.length + ' ta';
            badge.style.display = 'inline-block';

            // ===== Summary — barcha ma'lumot bo'yicha hisoblanadi (sahifa emas) =====
            var totalSumma = 0, totalTulangan = 0, totalQoldiq = 0;
            data.forEach(function(item) {
                var s = Number(item.summa) || 0;
                var t = Number(item.tulangan) || 0;
                totalSumma += s;
                totalTulangan += t;
                totalQoldiq += (s - t);
            });

            var fmt = function(n) { return Math.round(n).toLocaleString('uz-UZ') + " so'm"; };
            document.getElementById('unpaidTotalSumma').textContent    = fmt(totalQoldiq);
            document.getElementById('unpaidTotalCount').textContent    = data.length + ' ta';
            document.getElementById('unpaidTotalSchedule').textContent = fmt(totalSumma);
            document.getElementById('unpaidTotalPaid').textContent     = fmt(totalTulangan);
            document.getElementById('unpaidSummaryCard').style.display = 'flex';

            document.getElementById('unpaidTableWrap').style.display = 'block';
            renderUnpaidPage(1);
        })
        .catch(function(err) {
            document.getElementById('unpaidLoading').style.display = 'none';
            document.getElementById('unpaidError').style.display = 'block';
            document.getElementById('unpaidSummaryCard').style.display = 'none';
            console.error('dashboard-data xatolik:', err);
        });
}

    function renderUnpaidPage(page) {
    unpaidCurrentPage = page;
    var start = (page - 1) * PAGE_SIZE;
    var pageData = unpaidFullData.slice(start, start + PAGE_SIZE);

    var oyNomlari = ['yan','fev','mar','apr','may','iyun','iyul','avg','sen','okt','noy','dek'];
    var fmt = function(n) { return Math.round(n).toLocaleString('uz-UZ') + " so'm"; };
    var fmtSana = function(iso) {
    var dt = new Date(iso);
    return dt.getDate() + '-' + oyNomlari[dt.getMonth()] + ', ' + dt.getFullYear();
};

    var rows = '';
    pageData.forEach(function(item, i) {
    var s = Number(item.summa) || 0;
    var t = Number(item.tulangan) || 0;
    var qoldiq = s - t;
    var holatClass = item.holat === "tulanmagan" ? 'badge-inactive' : 'badge-pending';

    rows += '<tr>' +
    '<td style="color:var(--color-sub);">' + (start + i + 1) + '</td>' +
    '<td>' +
    '<div class="d-flex align-items-center">' +
    '<div class="avatar-sm">' + (item.mijozFish ? item.mijozFish[0] : '?') + '</div>' +
    '<div style="font-weight:600;">' + (item.mijozFish || '—') + '</div>' +
    '</div>' +
    '</td>' +
    '<td>' + (item.mijozTel || '—') + '</td>' +
    '<td>' + fmtSana(item.sana) + '</td>' +
    '<td style="color:var(--color-sub);">' + fmt(item.shartnomaSumma) + ' / ' + item.shartnomaMuddat + '</td>' +
    '<td style="font-weight:600;">' + fmt(s) + '</td>' +
    '<td style="color:#1a9a5a;">' + fmt(t) + '</td>' +
    '<td style="color:#c0392b; font-weight:600;">' + fmt(qoldiq) + '</td>' +
    '<td><span class="badge-status ' + holatClass + '">' + item.holat + '</span></td>' +
    '<td><a href="/admin/shartnoma-detail?id=' + item.shartnomaId + '" style="color:#1a6fa8; font-size:12px; text-decoration:none; font-weight:500;">Ko\'rish →</a></td>' +
    '</tr>';
});

    document.getElementById('unpaidTableBody').innerHTML = rows;
    renderPagination('unpaidPagination', unpaidFullData.length, unpaidCurrentPage, renderUnpaidPage);
}
    document.addEventListener('DOMContentLoaded', loadUnpaid);
    document.addEventListener('DOMContentLoaded', loadOverdueDebts);

    // ===== Muddati o'tgan qarzdorlarni yuklash =====
    let debtFullData = [];
    let debtCurrentPage = 1;

    function loadOverdueDebts() {
    fetch('/admin/get-overdue-debts', {
        method: 'GET',
        headers: { 'Accept': 'application/json' }
    })
        .then(function(res) {
            if (!res.ok) return Promise.reject(res.status);
            return res.json();
        })
        .then(function(data) {
            document.getElementById('debtLoading').style.display = 'none';

            if (!data || data.length === 0) {
                document.getElementById('debtEmpty').style.display = 'block';
                document.getElementById('debtSummaryCard').style.display = 'none';
                return;
            }

            debtFullData = data;

            var badge = document.getElementById('debtBadge');
            badge.textContent = data.length + ' ta';
            badge.style.display = 'inline-block';

            var fmt = function(n) { return Math.round(n).toLocaleString('uz-UZ') + " so'm"; };
            var totalQarz = 0;
            data.forEach(function(item) { totalQarz += Number(item.qolgan) || 0; });

            document.getElementById('debtTotalSumma').textContent = fmt(totalQarz);
            document.getElementById('debtTotalCount').textContent = data.length + ' ta';
            document.getElementById('debtAvgSumma').textContent   = fmt(totalQarz / data.length);
            document.getElementById('debtSummaryCard').style.display = 'flex';

            document.getElementById('debtTableWrap').style.display = 'block';
            renderDebtPage(1);
        })
        .catch(function(err) {
            document.getElementById('debtLoading').style.display = 'none';
            document.getElementById('debtError').style.display = 'block';
            document.getElementById('debtSummaryCard').style.display = 'none';
            console.error('overdue-debts xatolik:', err);
        });
}

    function renderDebtPage(page) {
    debtCurrentPage = page;
    var start = (page - 1) * PAGE_SIZE;
    var pageData = debtFullData.slice(start, start + PAGE_SIZE);

    var fmt = function(n) { return Math.round(n).toLocaleString('uz-UZ') + " so'm"; };
    var fmtSana = function(iso) {
    if (!iso) return '—';
    var oyNomlari = ['yan','fev','mar','apr','may','iyun','iyul','avg','sen','okt','noy','dek'];
    var dt = new Date(iso);
    if (isNaN(dt.getTime())) return iso;
    return dt.getDate() + '-' + oyNomlari[dt.getMonth()] + ', ' + dt.getFullYear();
};

    var rows = '';
    pageData.forEach(function(item, i) {
    var qolgan = Number(item.qolgan) || 0;
    var kun = Number(item.kun) || 0;
    var cls = 'mild';
    if (kun >= 60) cls = 'critical';
    else if (kun >= 30) cls = 'bad';

    rows += '<tr>' +
    '<td style="color:var(--color-sub);">' + (start + i + 1) + '</td>' +
    '<td style="font-weight:600;">' + (item.mijoz || '—') + '</td>' +
    '<td style="color:var(--color-sub);">' + (item.tel || '—') + '</td>' +
    '<td style="font-weight:700;color:#c0392b;">' + fmt(qolgan) + '</td>' +
    '<td><span class="debt-days ' + cls + '" style="font-size:11px; padding:2px 8px; border-radius:20px; font-weight:700; white-space:nowrap;' +
    (cls === 'critical' ? 'background:#c0392b;color:#fff;' : cls === 'bad' ? 'background:#fde8e8;color:#c0392b;' : 'background:#fff0dc;color:#b07000;') +
    '">' + kun + ' kun</span></td>' +
    '<td style="color:var(--color-sub);">' + fmtSana(item.oxirgiTolov) + '</td>' +
    '<td><a href="/admin/shartnoma-detail?id=' + item.shartnomaId + '" style="color:#1a6fa8; font-size:12px; font-weight:600; text-decoration:none;">Ko\'rish →</a></td>' +
    '</tr>';
});

    document.getElementById('debtTableBody').innerHTML = rows;
    renderPagination('debtPagination', debtFullData.length, debtCurrentPage, renderDebtPage);
}
    // Theme
    const html = document.documentElement;
    const saved = localStorage.getItem('mb-theme') || 'light';
    applyTheme(saved);

    function applyTheme(theme) {
    html.setAttribute('data-theme', theme);
    localStorage.setItem('mb-theme', theme);
    document.getElementById('themeIcon').textContent  = theme === 'dark' ? '☀️' : '🌙';
    document.getElementById('themeLabel').textContent = theme === 'dark' ? 'Kun'  : 'Tun';
}
    function toggleTheme() {
    applyTheme(html.getAttribute('data-theme') === 'dark' ? 'light' : 'dark');
}

    /* ===== TOAST SYSTEM ===== */
    function showToast(title, msg, autoMs) {
    const container = document.getElementById('toastContainer');
    const toast = document.createElement('div');
    toast.className = 'toast-error';
    toast.style.position = 'relative';
    toast.innerHTML =
    '<div class="toast-icon">🔒</div>' +
    '<div style="flex:1; min-width:0;">' +
    '<p class="toast-title">' + title + '</p>' +
    '<p class="toast-msg">' + msg + '</p>' +
    '</div>' +
    '<button class="toast-close" onclick="closeToast(this.closest(\'.toast-error\'))">×</button>' +
    '<div class="toast-progress" style="animation-duration:' + (autoMs/1000) + 's"></div>';
    container.appendChild(toast);

    setTimeout(function() { closeToast(toast); }, autoMs);
}

    function closeToast(toast) {
    if (!toast || toast.classList.contains('hiding')) return;
    toast.classList.add('hiding');
    setTimeout(function() { toast.remove(); }, 300);
}

    /* URL parametrni tekshirish */
    const params = new URLSearchParams(window.location.search);
    if (params.get('error') === 'access_denied') {
    showToast('Ruxsat yo\'q! 🚫', 'Siz kirmoqchi bo\'lgan sahifaga kirishga ruxsat berilmagan.', 5000);
    /* URLdan parametrni tozalash (sahifa reload bo'lmaydi) */
    const cleanUrl = window.location.pathname;
    window.history.replaceState({}, document.title, cleanUrl);
}
    /* ===== END TOAST ===== */
    document.addEventListener('DOMContentLoaded', loadMissingInfo);

    // ===== Kamchiligi aniqlangan shartnomalarni yuklash =====
    let missingFullData = [];
    let missingCurrentPage = 1;

    function loadMissingInfo() {
        fetch('/admin/get-missing-info-shartnoma', {
            method: 'GET',
            headers: { 'Accept': 'application/json' }
        })
            .then(function(res) {
                if (!res.ok) return Promise.reject(res.status);
                return res.json();
            })
            .then(function(data) {
                document.getElementById('missingLoading').style.display = 'none';

                if (!data || data.length === 0) {
                    document.getElementById('missingEmpty').style.display = 'block';
                    return;
                }

                missingFullData = data;

                var badge = document.getElementById('missingBadge');
                badge.textContent = data.length + ' ta';
                badge.style.display = 'inline-block';

                document.getElementById('missingTableWrap').style.display = 'block';
                renderMissingPage(1);
            })
            .catch(function(err) {
                document.getElementById('missingLoading').style.display = 'none';
                document.getElementById('missingError').style.display = 'block';
                console.error('missing-info-shartnoma xatolik:', err);
            });
    }

    function renderMissingPage(page) {
        missingCurrentPage = page;
        var start = (page - 1) * PAGE_SIZE;
        var pageData = missingFullData.slice(start, start + PAGE_SIZE);

        var fmt = function(n) { return Math.round(n).toLocaleString('uz-UZ') + " so'm"; };

        var rows = '';
        pageData.forEach(function(item, i) {
            rows += '<tr>' +
                '<td style="color:var(--color-sub);">' + (start + i + 1) + '</td>' +
                '<td style="font-weight:600;">' + (item.fish || '—') + '</td>' +
                '<td style="color:var(--color-sub);">' + (item.tel || '—') + '</td>' +
                '<td style="font-weight:600;">' + fmt(item.summa) + '</td>' +
                '<td><span class="badge-status badge-pending" style="white-space:normal;">' + (item.sabab || '—') + '</span></td>' +
                '<td><a href="/admin/shartnoma-detail?id=' + item.id + '" style="color:#1a6fa8; font-size:12px; font-weight:600; text-decoration:none;">Batafsil →</a></td>' +
                '</tr>';
        });

        document.getElementById('missingTableBody').innerHTML = rows;
        renderPagination('missingPagination', missingFullData.length, missingCurrentPage, renderMissingPage);
    }