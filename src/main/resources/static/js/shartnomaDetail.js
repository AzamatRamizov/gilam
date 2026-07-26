// ── Theme ──
const html = document.documentElement;
applyTheme(localStorage.getItem('mb-theme') || 'light');
function applyTheme(t) {
    html.setAttribute('data-theme', t);
    localStorage.setItem('mb-theme', t);
    const icon  = document.getElementById('themeIcon');
    const label = document.getElementById('themeLabel');
    if (icon)  icon.textContent  = t === 'dark' ? '☀️' : '🌙';
    if (label) label.textContent = t === 'dark' ? 'Kun' : 'Tun';
}
function toggleTheme() { applyTheme(html.getAttribute('data-theme') === 'dark' ? 'light' : 'dark'); }

const params      = new URLSearchParams(window.location.search);
const shartnomaId = params.get('id');

// ── Shartnoma tab menyu ──
function switchShartnomaTab(tab) {
    document.querySelectorAll('.shartnoma-tab').forEach(function(b){
        b.classList.toggle('active', b.getAttribute('data-tab')===tab);
    });
    document.querySelectorAll('.shartnoma-tab-panel').forEach(function(p){
        p.classList.toggle('active', p.id==='tabPanel-'+tab);
    });
}

let _currentGrafik  = [];
let _magazinlar     = [];
let _magazinLoaded  = false;
let _currentMijozId = null;
let _currentShartnomaId = null;
let _nsFiles        = {};

// Shartnomaning umumiy summasi / to'langan / qolgan qiymatlari (umumiy to'lov modali uchun)
let _currentMuddat       = 0;
let _currentTotalSumma   = 0;
let _currentTotalTulandi = 0;
let _currentTotalQolgan  = 0;

// ══════ LIGHTBOX ══════
let _lbImages = [], _lbIndex = 0;
function openLightbox(images, startIndex) {
    _lbImages = images; _lbIndex = startIndex || 0;
    document.getElementById('lightbox').classList.add('show');
    document.body.style.overflow = 'hidden';
    lbRender();
}
function closeLightbox() {
    document.getElementById('lightbox').classList.remove('show');
    document.body.style.overflow = '';
}
function lbRender() {
    const img = _lbImages[_lbIndex], total = _lbImages.length;
    document.getElementById('lbImg').src = img.url;
    document.getElementById('lbTitle').textContent = img.title + (total > 1 ? ' ('+((_lbIndex+1))+'/'+total+')' : '');
    document.getElementById('lbDownload').href = img.url;
    document.getElementById('lbDownload').setAttribute('download', img.title.replace(/\s+/g,'_')+'.jpg');
    document.getElementById('lbOpen').href = img.url;
    document.getElementById('lbPrev').classList.toggle('hidden', _lbIndex === 0);
    document.getElementById('lbNext').classList.toggle('hidden', _lbIndex === total-1);
    const counter = document.getElementById('lbCounter');
    counter.innerHTML = total > 1 ? _lbImages.map(function(_,i){
        return '<div class="lb-dot'+(i===_lbIndex?' active':'')+'" onclick="lbGoTo('+i+')"></div>';
    }).join('') : '';
}
function lbNavigate(dir) { const n = _lbIndex+dir; if(n>=0&&n<_lbImages.length){_lbIndex=n;lbRender();} }
function lbGoTo(i) { _lbIndex=i; lbRender(); }
document.getElementById('lightbox').addEventListener('click', function(e){ if(e.target===this) closeLightbox(); });
document.addEventListener('keydown', function(e){
    if (!document.getElementById('lightbox').classList.contains('show')) return;
    if (e.key==='ArrowLeft') lbNavigate(-1);
    if (e.key==='ArrowRight') lbNavigate(1);
    if (e.key==='Escape') closeLightbox();
});

// ══════ PHOTO RENDERING (read-only groups, e.g. kafolat) ══════
function photoUrl(src) {
    if (!src) return null;
    if (typeof src==='object' && src.id) return '/admin/fayl/'+src.id;
    if (typeof src==='number') return '/admin/fayl/'+src;
    if (typeof src==='string') return src;
    return null;
}
function renderPhotoGroup(wrapId, photos) {
    const wrap  = document.getElementById(wrapId);
    const valid = photos.filter(function(p){ return !!p.url; });
    const noSvg = '<svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" fill="currentColor" viewBox="0 0 16 16"><path d="M6.002 5.5a1.5 1.5 0 1 1-3 0 1.5 1.5 0 0 1 3 0z"/><path d="M2.002 1a2 2 0 0 0-2 2v10a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V3a2 2 0 0 0-2-2h-12zm12 1a1 1 0 0 1 1 1v6.5l-3.777-1.947a.5.5 0 0 0-.577.093l-3.71 3.71-2.66-1.772a.5.5 0 0 0-.63.062L1.002 12V3a1 1 0 0 1 1-1h12z"/></svg>';
    wrap.innerHTML = photos.map(function(p){
        if (!p.url) return '<div class="col-md-6"><span class="photo-label">'+p.title+'</span><div class="photo-box"><div class="no-photo">'+noSvg+'<div>Rasm mavjud emas</div></div></div></div>';
        const vi = valid.findIndex(function(v){ return v.url===p.url; });
        return '<div class="col-md-6"><span class="photo-label">'+p.title+'</span>'+
            '<div class="photo-box"><img src="'+p.url+'" alt="'+p.title+'" onclick="openLightbox('+JSON.stringify(valid)+','+vi+')"></div>'+
            '<div class="photo-actions">'+
            '<a class="btn-photo-action" href="'+p.url+'" download="'+p.title.replace(/\s+/g,'_')+'.jpg">'+
            '<svg width="13" height="13" fill="currentColor" viewBox="0 0 16 16"><path d="M.5 9.9a.5.5 0 0 1 .5.5v2.5a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1v-2.5a.5.5 0 0 1 1 0v2.5a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2v-2.5a.5.5 0 0 1 .5-.5z"/><path d="M7.646 11.854a.5.5 0 0 0 .708 0l3-3a.5.5 0 0 0-.708-.708L8.5 10.293V1.5a.5.5 0 0 0-1 0v8.793L5.354 8.146a.5.5 0 1 0-.708.708l3 3z"/></svg>Yuklab olish</a>'+
            '<a class="btn-photo-action" href="'+p.url+'" target="_blank">'+
            '<svg width="13" height="13" fill="currentColor" viewBox="0 0 16 16"><path fill-rule="evenodd" d="M8.636 3.5a.5.5 0 0 0-.5-.5H1.5A1.5 1.5 0 0 0 0 4.5v10A1.5 1.5 0 0 0 1.5 16h10a1.5 1.5 0 0 0 1.5-1.5V7.864a.5.5 0 0 0-1 0V14.5a.5.5 0 0 1-.5.5h-10a.5.5 0 0 1-.5-.5v-10a.5.5 0 0 1 .5-.5h6.636a.5.5 0 0 0 .5-.5z"/><path fill-rule="evenodd" d="M16 .5a.5.5 0 0 0-.5-.5h-5a.5.5 0 0 0 0 1h3.793L6.146 9.146a.5.5 0 1 0 .708.708L15 1.707V5.5a.5.5 0 0 0 1 0v-5z"/></svg>Yangi tab</a>'+
            '</div></div>';
    }).join('');
}

// ══════ PASSPORT PHOTOS (editable) ══════
let _mijozFiles = {};          // { passport1: File, passport2: File } — tanlangan lekin hali saqlanmagan fayllar
let _lastPassportPhotos = [];  // renderni qayta chizish uchun oxirgi ma'lumot

function renderEditablePhotoGroup(wrapId, photos) {
    _lastPassportPhotos = photos;
    const wrap = document.getElementById(wrapId);
    const noSvg = '<svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" fill="currentColor" viewBox="0 0 16 16"><path d="M6.002 5.5a1.5 1.5 0 1 1-3 0 1.5 1.5 0 0 1 3 0z"/><path d="M2.002 1a2 2 0 0 0-2 2v10a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V3a2 2 0 0 0-2-2h-12zm12 1a1 1 0 0 1 1 1v6.5l-3.777-1.947a.5.5 0 0 0-.577.093l-3.71 3.71-2.66-1.772a.5.5 0 0 0-.63.062L1.002 12V3a1 1 0 0 1 1-1h12z"/></svg>';

    wrap.innerHTML = photos.map(function(p){
        const pending  = _mijozFiles[p.key];
        const previewSrc = pending ? pending._previewUrl : p.url;

        const inner = previewSrc
            ? '<img src="'+previewSrc+'" alt="'+p.title+'">'
            : '<div class="no-photo">'+noSvg+'<div>Rasm mavjud emas</div></div>';

        let overlayHtml = '';
        if (pending) {
            overlayHtml =
                '<span class="photo-new-badge">&#9989; Yangi</span>'+
                '<button class="photo-cancel-new" onclick="cancelPassportPhoto(\''+p.key+'\',event)" title="Bekor qilish">&#10005;</button>';
        } else {
            overlayHtml =
                (p.url ? '<button class="photo-view-btn" onclick="viewPassportPhoto(event,\''+p.url+'\',\''+p.title.replace(/'/g,"\\'")+'\')" title="Kattalashtirib ko\'rish">&#128065;</button>' : '')+
                '<span class="photo-change-overlay">&#128260; Almashtirish</span>'+
                '<input type="file" class="photo-change-input" accept="image/*" onchange="onPassportPhotoChange(this,\''+p.key+'\')" title="Yangi rasm yuklash">';
        }

        return '<div class="col-md-6">'+
            '<span class="photo-label">'+p.title+'</span>'+
            '<div class="photo-edit-wrap'+(pending?' has-new-file':'')+'">'+
            '<div class="photo-box">'+inner+overlayHtml+'</div>'+
            '</div>'+
            '</div>';
    }).join('');

    const anyPending = photos.some(function(p){ return !!_mijozFiles[p.key]; });
    document.getElementById('passportPendingNote').style.display = anyPending ? 'flex' : 'none';
}

function onPassportPhotoChange(input, key) {
    const file = input.files[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = function(e){
        file._previewUrl = e.target.result;
        _mijozFiles[key] = file;
        renderEditablePhotoGroup('passportPhotosWrap', _lastPassportPhotos);
    };
    reader.readAsDataURL(file);
}
function cancelPassportPhoto(key, ev) {
    if (ev) ev.stopPropagation();
    delete _mijozFiles[key];
    renderEditablePhotoGroup('passportPhotosWrap', _lastPassportPhotos);
}
function viewPassportPhoto(ev, url, title) {
    if (ev) ev.stopPropagation();
    if (!url) return;
    openLightbox([{url:url,title:title}], 0);
}

// ══════ KAFOLAT XATI PHOTOS (editable) ══════
let _kafolatFiles      = {};   // { kafolat1: File, kafolat2: File } — tanlangan lekin hali saqlanmagan fayllar
let _lastKafolatPhotos = [];   // renderni qayta chizish uchun oxirgi ma'lumot

function renderEditableKafolatGroup(photos) {
    _lastKafolatPhotos = photos;
    const wrap = document.getElementById('kafolatPhotosWrap');
    const noSvg = '<svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" fill="currentColor" viewBox="0 0 16 16"><path d="M6.002 5.5a1.5 1.5 0 1 1-3 0 1.5 1.5 0 0 1 3 0z"/><path d="M2.002 1a2 2 0 0 0-2 2v10a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V3a2 2 0 0 0-2-2h-12zm12 1a1 1 0 0 1 1 1v6.5l-3.777-1.947a.5.5 0 0 0-.577.093l-3.71 3.71-2.66-1.772a.5.5 0 0 0-.63.062L1.002 12V3a1 1 0 0 1 1-1h12z"/></svg>';

    wrap.innerHTML = photos.map(function(p){
        const pending  = _kafolatFiles[p.key];
        const previewSrc = pending ? pending._previewUrl : p.url;

        const inner = previewSrc
            ? '<img src="'+previewSrc+'" alt="'+p.title+'">'
            : '<div class="no-photo">'+noSvg+'<div>Rasm mavjud emas</div></div>';

        let overlayHtml = '';
        if (pending) {
            overlayHtml =
                '<span class="photo-new-badge">&#9989; Yangi</span>'+
                '<button class="photo-cancel-new" onclick="cancelKafolatPhoto(\''+p.key+'\',event)" title="Bekor qilish">&#10005;</button>';
        } else {
            overlayHtml =
                (p.url ? '<button class="photo-view-btn" onclick="viewPassportPhoto(event,\''+p.url+'\',\''+p.title.replace(/'/g,"\\'")+'\')" title="Kattalashtirib ko\'rish">&#128065;</button>' : '')+
                '<span class="photo-change-overlay">&#128260; Almashtirish</span>'+
                '<input type="file" class="photo-change-input" accept="image/*" onchange="onKafolatPhotoChange(this,\''+p.key+'\')" title="Yangi rasm yuklash">';
        }

        return '<div class="col-md-6">'+
            '<span class="photo-label">'+p.title+'</span>'+
            '<div class="photo-edit-wrap'+(pending?' has-new-file':'')+'">'+
            '<div class="photo-box">'+inner+overlayHtml+'</div>'+
            '</div>'+
            '</div>';
    }).join('');

    const anyPending = photos.some(function(p){ return !!_kafolatFiles[p.key]; });
    document.getElementById('kafolatPendingNote').style.display = anyPending ? 'flex' : 'none';
}

function onKafolatPhotoChange(input, key) {
    const file = input.files[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = function(e){
        file._previewUrl = e.target.result;
        _kafolatFiles[key] = file;
        renderEditableKafolatGroup(_lastKafolatPhotos);
    };
    reader.readAsDataURL(file);
}
function cancelKafolatPhoto(key, ev) {
    if (ev) ev.stopPropagation();
    delete _kafolatFiles[key];
    renderEditableKafolatGroup(_lastKafolatPhotos);
}
function saveKafolat() {
    const btn   = document.getElementById('kafolatSaveBtn');
    const succ  = document.getElementById('kafolatSaveSuccess');
    const errEl = document.getElementById('kafolatSaveError');
    succ.style.display='none'; errEl.style.display='none';

    const newK1 = _kafolatFiles.kafolat1;
    const newK2 = _kafolatFiles.kafolat2;
    if (!newK1 && !newK2) { errEl.textContent='⚠️ Avval yangi rasm tanlang'; errEl.style.display='block'; return; }
    if (!_currentShartnomaId) { errEl.textContent='⚠️ Shartnoma topilmadi'; errEl.style.display='block'; return; }

    btn.disabled=true; btn.textContent='⏳ Saqlanmoqda...';

    const fd = new FormData();
    fd.append('shartnomaId', _currentShartnomaId);
    if (newK1) fd.append('kafolat1', newK1);
    if (newK2) fd.append('kafolat2', newK2);

    fetch('/admin/update-shartnoma-kafolat', {method:'POST', body:fd})
        .then(function(r){ return r.json().then(function(b){ if(!r.ok) throw (b.message||'Xatolik: '+r.status); return b; }); })
        .then(function(){ _kafolatFiles={}; succ.style.display='block'; setTimeout(function(){succ.style.display='none';},3000); loadData(); })
        .catch(function(msg){ errEl.textContent='⚠️ '+msg; errEl.style.display='block'; })
        .finally(function(){ btn.disabled=false; btn.innerHTML='💾 Saqlash'; });
}

// ── Helpers ──
function showError(msg) {
    document.getElementById('errorBannerText').textContent = msg;
    document.getElementById('errorBanner').style.display = 'flex';
}
function badgeInfo(s) {
    if (s==='Faol') return {cls:'badge-active',label:'Faol'};
    if (s==='Yopilgan') return {cls:'badge-closed',label:'Yopilgan'};
    if (s==="Muddati o'tgan") return {cls:'badge-overdue',label:"Muddati o'tgan"};
    return {cls:'badge-closed',label:s||'—'};
}
function fmtSumma(n) {
    if (n===undefined||n===null||n==='') return '—';
    return (Number(n)||0).toLocaleString('uz-UZ')+" so'm";
}
function fmtSummaShort(n) {
    return (Number(n)||0).toLocaleString('uz-UZ')+" so'm";
}
function payStatusInfo(h) {
    if (h==='tulangan') return {cls:'paid',label:"To'langan"};
    if (h==="Muddati o'tgan") return {cls:'overdue',label:"Muddati o'tgan"};
    return {cls:'pending',label:'Kutilmoqda'};
}
// Faqat sana (jadval jadvali uchun)
function fmtDate(sana) {
    if (!sana) return '—';
    const d=new Date(sana);
    if (isNaN(d.getTime())) return sana;
    const oy=['yan','fev','mar','apr','may','iyun','iyul','avg','sen','okt','noy','dek'];
    return d.getDate()+'-'+oy[d.getMonth()]+', '+d.getFullYear();
}
// Sana + soat:daqiqa (to'lov tarixi uchun)
function fmtDateTime(sana) {
    if (!sana) return '—';
    const d=new Date(sana);
    if (isNaN(d.getTime())) return sana;
    const oy=['yan','fev','mar','apr','may','iyun','iyul','avg','sen','okt','noy','dek'];
    const hh=String(d.getHours()).padStart(2,'0');
    const mi=String(d.getMinutes()).padStart(2,'0');
    return d.getDate()+'-'+oy[d.getMonth()]+', '+d.getFullYear()+' &#8226; '+hh+':'+mi;
}
function nowLocalISO() {
    const now=new Date();
    return new Date(now.getTime()-now.getTimezoneOffset()*60000).toISOString().slice(0,16);
}
function localISOwithMonthOffset(months) {
    const d=new Date();
    d.setMonth(d.getMonth()+months);
    return new Date(d.getTime()-d.getTimezoneOffset()*60000).toISOString().slice(0,16);
}

// ── Magazinlar ──
function loadMagazinlar(selId, wrapId) {
    if (_magazinLoaded) { buildDokonSelect(selId); return Promise.resolve(); }
    const wrap = document.getElementById(wrapId);
    if (wrap) wrap.classList.add('loading');
    return fetch('/admin/get-magazinlar')
        .then(function(r){ return r.ok ? r.json() : Promise.reject(r.status); })
        .then(function(data){ _magazinlar=Array.isArray(data)?data:[]; _magazinLoaded=true; buildDokonSelect(selId); })
        .catch(function(){ _magazinlar=[]; _magazinLoaded=true; buildDokonSelect(selId); })
        .finally(function(){ if(wrap) wrap.classList.remove('loading'); });
}
function buildDokonSelect(selId) {
    const sel = document.getElementById(selId);
    if (!sel) return;
    sel.innerHTML = '<option value="" disabled selected>Do\'konni tanlang...</option>';
    _magazinlar.forEach(function(m){
        const opt=document.createElement('option');
        opt.value=m.id; opt.textContent=m.nomi; sel.appendChild(opt);
    });
}

// ── Mijoz fill & save ──
function fillMijoz(m) {
    m=m||{};
    _currentMijozId=m.id||null;
    ['ism','familiya','sharif','tel1','tel2','tel3','viloyat','tuman','manzil','muljal','passport'].forEach(function(k){
        const el=document.getElementById('f_'+k);
        if (el) el.value=m[k]||'';
    });
    const fish=[m.familiya,m.ism,m.sharif].filter(Boolean).join(' ');
    document.getElementById('pageTitle').textContent  = fish||"Shartnoma ma'lumotlari";
    document.getElementById('avatarIcon').textContent = (((m.ism||'')[0]||'')+((m.familiya||'')[0]||'')).toUpperCase()||'—';

    // Saqlashdan so'ng (yoki dastlabki yuklashda) tanlangan fayllar tozalanadi
    _mijozFiles = {};
    renderEditablePhotoGroup('passportPhotosWrap',[
        {url:photoUrl(m.passportRasm||m.passportRasm1), title:'Passport rasmi (1-bet)', key:'passport1'},
        {url:photoUrl(m.passportRasm2||m.katm),          title:'Passport rasmi (2-bet)', key:'passport2'}
    ]);
}
function saveMijoz() {
    const btn=document.getElementById('mijozSaveBtn');
    const succ=document.getElementById('mijozSaveSuccess');
    const errEl=document.getElementById('mijozSaveError');
    succ.style.display='none'; errEl.style.display='none';

    const payload={mijozId:_currentMijozId};
    ['ism','familiya','sharif','tel1','tel2','tel3','viloyat','tuman','manzil','muljal','passport'].forEach(function(k){
        payload[k]=document.getElementById('f_'+k).value.trim();
    });
    if (!payload.ism||!payload.familiya){errEl.textContent='⚠️ Ism va familiya majburiy';errEl.style.display='block';return;}

    btn.disabled=true; btn.textContent='⏳ Saqlanmoqda...';

    const newP1 = _mijozFiles.passport1;
    const newP2 = _mijozFiles.passport2;

    const fd = new FormData();
    fd.append('mijoz', new Blob([JSON.stringify(payload)], {type:'application/json'}));
    if (newP1) fd.append('rasm', newP1);
    if (newP2) fd.append('rasm2', newP2);

    fetch('/admin/update-mijoz', {method:'POST', body:fd})
        .then(function(r){ return r.json().then(function(b){ if(!r.ok) throw b.message||'Xatolik: '+r.status; return b; }); })
        .then(function(data){ if(data&&data.ism) fillMijoz(data); succ.style.display='block'; setTimeout(function(){succ.style.display='none';},3000); })
        .catch(function(msg){ errEl.textContent='⚠️ '+msg; errEl.style.display='block'; })
        .finally(function(){ btn.disabled=false; btn.innerHTML='💾 Saqlash'; });
}

// ── Mahsulot fill & save (2.2) ──
function fillMahsulot(c) {
    c = c || {};
    const m = c.mahsulot || {};
    document.getElementById('pr_nomi').value = m.nomi || c.about || '';
    document.getElementById('pr_sana').value = m.sana ? String(m.sana).slice(0,10) : '';
    document.getElementById('pr_narx').value = (m.narx !== undefined && m.narx !== null) ? m.narx : '';
    document.getElementById('pr_lokatsiya').value = m.lokatsiya || '';
    document.getElementById('pr_izoh').value = (m.izoh !== undefined && m.izoh !== null) ? m.izoh : (c.izoh || '');
    document.getElementById('pr_oldindan').value = (m.oldindanTulov !== undefined && m.oldindanTulov !== null) ? m.oldindanTulov : '';
    prFillDokon(m.dokonId);
    prUpdateLokatsiyaLink();
}
// "Sotilgan do'kon" select'ini to'ldiradi va joriy qiymatni tanlaydi.
// Magazinlar ro'yxati loadMagazinlar keshidan olinadi (qayta so'rov ketmaydi).
function prFillDokon(selectedId) {
    function build() {
        const sel = document.getElementById('pr_dokon');
        if (!sel) return;
        sel.innerHTML = '<option value="">Belgilanmagan</option>';
        _magazinlar.forEach(function(mg){
            const opt = document.createElement('option');
            opt.value = mg.id; opt.textContent = mg.nomi;
            sel.appendChild(opt);
        });
        sel.value = (selectedId !== null && selectedId !== undefined) ? String(selectedId) : '';
        if (sel.selectedIndex < 0) sel.value = ''; // do'kon o'chirib yuborilgan bo'lsa
    }
    if (_magazinLoaded) { build(); return; }
    loadMagazinlar('pr_dokon').then(build);
}
function prUpdateLokatsiyaLink() {
    const val = document.getElementById('pr_lokatsiya').value.trim();
    const link = document.getElementById('pr_lokatsiya_link');
    if (val && /^https?:\/\//i.test(val)) {
        link.href = val;
        link.style.display = 'inline-flex';
    } else {
        link.style.display = 'none';
    }
}
document.getElementById('pr_lokatsiya').addEventListener('input', prUpdateLokatsiyaLink);
function saveMahsulot() {
    const btn  = document.getElementById('mahsulotSaveBtn');
    const succ = document.getElementById('mahsulotSaveSuccess');
    const errEl= document.getElementById('mahsulotSaveError');
    succ.style.display='none'; errEl.style.display='none';
    const payload = {
        shartnomaId: _currentShartnomaId,
        nomi: document.getElementById('pr_nomi').value.trim(),
        sana: document.getElementById('pr_sana').value,
        narx: String(document.getElementById('pr_narx').value||'0'),
        lokatsiya: document.getElementById('pr_lokatsiya').value.trim(),
        izoh: document.getElementById('pr_izoh').value.trim(),
        oldindanTulov: document.getElementById('pr_oldindan').value
            ? Number(document.getElementById('pr_oldindan').value)
            : null,
        dokonId: document.getElementById('pr_dokon').value
            ? Number(document.getElementById('pr_dokon').value)
            : null
    };
    if (!payload.nomi) { errEl.textContent='⚠️ Mahsulot nomini kiriting'; errEl.style.display='block'; return; }
    btn.disabled=true; btn.textContent='⏳ Saqlanmoqda...';
    fetch('/admin/update-mahsulot',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(payload)})
        .then(function(r){ return r.text().then(function(t){ if(!r.ok) throw t||('Xatolik: '+r.status); return t; }); })
        .then(function(){ succ.style.display='block'; setTimeout(function(){succ.style.display='none';},3000); })
        .catch(function(msg){ errEl.textContent='⚠️ '+msg; errEl.style.display='block'; })
        .finally(function(){ btn.disabled=false; btn.innerHTML='💾 Saqlash'; });
}

// ── To'lov tarixi (2.3) ──
// NOTE: backend hali ulanmagan. Kutilayotgan maydon: c.tulovTarixi = [{ sana, summa, turi, dokon }]
function renderTulovTarixi(c) {
    c = c || {};
    const wrap = document.getElementById('tulovTarixiWrap');
    const list = Array.isArray(c.tulovTarixi) ? c.tulovTarixi.slice() : [];

    // Sanaga qarab o'sish tartibida (eng eski to'lov birinchi)
    list.sort(function(a, b) {
        const da = a.sana ? new Date(a.sana).getTime() : 0;
        const db = b.sana ? new Date(b.sana).getTime() : 0;
        return da - db;
    });

    if (!list.length) {
        wrap.innerHTML = '<div class="schedule-empty">To\'lov tarixi mavjud emas</div>';
        return;
    }
    let rows = '';
    list.forEach(function(t, i){
        rows += '<tr>'+
            '<td style="color:var(--color-sub);font-weight:600;">'+(i+1)+'</td>'+
            '<td>'+fmtDateTime(t.sana)+'</td>'+
            '<td class="tulangan-col">'+fmtSumma(t.summa)+'</td>'+
            '<td>'+(t.turi||'—')+'</td>'+
            '<td>'+(t.dokon||'—')+'</td>'+
            '</tr>';
    });
    wrap.innerHTML = '<div style="overflow-x:auto;"><table class="schedule-table">'+
        '<thead><tr><th>#</th><th>To\'lov sanasi</th><th>Summa</th><th>To\'lov turi</th><th>Do\'kon</th></tr></thead>'+
        '<tbody>'+rows+'</tbody></table></div>';
}

// ── Shartnoma render ──
function renderShartnoma(c) {
    c=c||{};
    _currentShartnomaId = c.id || null;
    document.getElementById('v_contractNo').textContent='№'+(c.id??'—');
    const b=badgeInfo(c.status);
    const be=document.getElementById('v_statusBadge');
    be.className='badge-status '+b.cls;
    document.getElementById('v_statusLabel').textContent=b.label;

    const grafik=(c.jadvalList||[]).slice().sort(function(a, b){
        const da = a.sana ? new Date(a.sana).getTime() : 0;
        const db = b.sana ? new Date(b.sana).getTime() : 0;
        return da - db;   // ← eng eski sana birinchi
    });
    _currentGrafik=grafik;
    const totalSumma=Number(c.summa)||0;
    const totalTulandi=grafik.reduce(function(s,g){return s+(Number(g.tulangan)||0);},0);
    const totalQolgan=Math.max(0,totalSumma-totalTulandi);

    // Umumiy to'lov modali uchun saqlab qo'yamiz
    _currentMuddat       = Number(c.muddat) || 0;
    _currentTotalSumma   = totalSumma;
    _currentTotalTulandi = totalTulandi;
    _currentTotalQolgan  = totalQolgan;

    document.getElementById('summaryBar').innerHTML=
        '<div class="summary-item"><span>Umumiy summa</span><span>'+fmtSummaShort(totalSumma)+'</span></div>'+
        '<div class="summary-item"><span>Muddat</span><span>'+(c.muddat?c.muddat+' oy':'—')+
        ' <button type="button" class="muddat-edit-btn" title="Muddatni o\'zgartirish" onclick="openMuddatModal()">&#9998;</button></span></div>'+
        '<div class="summary-item"><span>To\'langan</span><span style="color:#1a7a50">'+fmtSummaShort(totalTulandi)+'</span></div>'+
        '<div class="summary-item"><span>Qolgan</span><span style="color:'+(totalQolgan>0?'#c0392b':'#1a7a50')+'">'+fmtSummaShort(totalQolgan)+'</span></div>';

    // Umumiy "To'lov qilish" tugmasi — faqat qolgan qarz bo'lsa yoqilgan
    const generalPayBtn=document.getElementById('generalPayBtn');
    if (generalPayBtn) generalPayBtn.disabled = totalQolgan<=0;

    const wrap=document.getElementById('scheduleWrap');
    if (grafik.length) {
        let rows='';
        grafik.forEach(function(g,i){
            const ps=payStatusInfo(g.holat);
            const summa=Number(g.summa)||0;
            const tulangan=Number(g.tulangan)||0;
            const qolgan=Math.max(0,summa-tulangan);
            // to'liq to'langan qatorda sanani tahrirlash tugmasi ko'rsatilmaydi
            const tolandi = (qolgan<=0) || (g.holat==='tulangan');
            rows+='<tr>'+
                '<td style="color:var(--color-sub);font-weight:600;">'+(i+1)+'</td>'+
                '<td><span class="jadval-sana-text">'+fmtDate(g.sana)+'</span>'+
                (tolandi ? '' : '<button type="button" class="jadval-sana-edit-btn" title="Sanani o\'zgartirish" onclick="openJadvalSanaModal('+g.id+')">&#9998;</button>')+'</td>'+
                '<td>'+fmtSumma(summa)+'</td>'+
                '<td class="tulangan-col">'+fmtSumma(tulangan)+'</td>'+
                '<td style="font-weight:600;color:'+(qolgan>0?'#c0392b':'var(--color-text)')+'">'+fmtSumma(qolgan)+'</td>'+
                '<td><span class="pay-status '+ps.cls+'">'+ps.label+'</span></td>'+
                '</tr>';
        });
        wrap.innerHTML='<div style="overflow-x:auto;"><table class="schedule-table">'+
            '<thead><tr><th>#</th><th>To\'lov sanasi</th><th>Summasi</th><th>To\'langan</th><th>Qolgan</th><th>Holati</th></tr></thead>'+
            '<tbody>'+rows+'</tbody></table></div>';
    } else {
        wrap.innerHTML='<div class="schedule-empty">To\'lov grafigi mavjud emas</div>';
    }
    _kafolatFiles = {};
    renderEditableKafolatGroup([
        {url:photoUrl(c.kafolat),  title:'Kafolat xati (1-bet)', key:'kafolat1'},
        {url:photoUrl(c.kafolat2), title:'Kafolat xati (2-bet)', key:'kafolat2'}
    ]);
    fillMahsulot(c);
    renderTulovTarixi(c);
}

// ══════ YANGI SHARTNOMA MODAL ══════
function openNewShartnoma() {
    // Reset
    ['ns_sana','ns_tulov_sana','ns_about','ns_izoh','ns_tannarx','ns_oldindan','ns_joylashuv','ns_summa','ns_foiz','ns_muddat'].forEach(function(id){
        const el=document.getElementById(id);
        if(el) el.value='';
    });
    const nsLokLink=document.getElementById('ns_joylashuv_link');
    if(nsLokLink) nsLokLink.style.display='none';
    document.getElementById('ns_sana').value       = nowLocalISO();
    document.getElementById('ns_tulov_sana').value = localISOwithMonthOffset(1);
    document.getElementById('ns_sana').addEventListener('change', function(){
        if (!this.value) return;
        const d=new Date(this.value);
        d.setMonth(d.getMonth()+1);
        document.getElementById('ns_tulov_sana').value=new Date(d.getTime()-d.getTimezoneOffset()*60000).toISOString().slice(0,16);
    }, {once:true});

    document.getElementById('nsCalcBox').style.display='none';
    document.getElementById('nsScheduleBody').innerHTML='';
    nsRemoveFile('ns_kaf1'); nsRemoveFile('ns_kaf2');
    _nsFiles={};
    const msg=document.getElementById('nsMsg');
    msg.className='form-msg'; msg.textContent='';
    const btn=document.getElementById('nsSaveBtn');
    btn.disabled=false; btn.textContent='💾 Shartnoma yaratish';

    loadMagazinlar('ns_dokon','nsDokonWrap');
    document.getElementById('newShartnomaModal').classList.add('show');
}
function closeNewShartnoma() {
    document.getElementById('newShartnomaModal').classList.remove('show');
}
document.getElementById('newShartnomaModal').addEventListener('click', function(e){ if(e.target===this) closeNewShartnoma(); });

// Calc preview
function nsUpdateCalc() {
    const summa  = parseFloat(document.getElementById('ns_summa').value)||0;
    const foiz   = parseFloat(document.getElementById('ns_foiz').value)||0;
    const muddat = parseInt(document.getElementById('ns_muddat').value)||0;
    const box    = document.getElementById('nsCalcBox');
    if (summa<=0||muddat<=0) { box.style.display='none'; return; }
    box.style.display='block';
    const foizSumma=summa*(foiz/100);
    const jami=summa+foizSumma;
    const oylik=jami/muddat;
    const fmt=function(n){ return Math.round(n).toLocaleString('uz-UZ')+" so'm"; };
    document.getElementById('ns_c_summa').textContent=fmt(summa);
    document.getElementById('ns_c_foiz').textContent=fmt(foizSumma);
    document.getElementById('ns_c_jami').textContent=fmt(jami);
    document.getElementById('ns_c_oylik').textContent=fmt(oylik);
    // Schedule rows
    const sanaVal=document.getElementById('ns_tulov_sana').value;
    const baseDate=sanaVal ? new Date(sanaVal) : new Date();
    const oy=['yanvar','fevral','mart','aprel','may','iyun','iyul','avgust','sentyabr','oktyabr','noyabr','dekabr'];
    let rows=''; let remaining=jami;
    for(let i=1;i<=muddat;i++){
        const pd=new Date(baseDate.getFullYear(),baseDate.getMonth()+(i-1),baseDate.getDate());
        const sana=pd.getDate()+'-'+oy[pd.getMonth()]+', '+pd.getFullYear();
        const isLast=i===muddat;
        const amount=isLast?remaining:oylik;
        remaining-=oylik;
        rows+='<tr><td>'+i+'</td><td>'+sana+'</td><td>'+fmt(amount)+'</td></tr>';
    }
    document.getElementById('nsScheduleBody').innerHTML=rows;
}
document.getElementById('ns_tulov_sana').addEventListener('change', nsUpdateCalc);

// Yangi shartnoma modalida lokatsiya link kiritilsa "Ochish" havolasini ko'rsatish
function nsUpdateJoylashuvLink() {
    const val = document.getElementById('ns_joylashuv').value.trim();
    const link = document.getElementById('ns_joylashuv_link');
    if (val && /^https?:\/\//i.test(val)) {
        link.href = val;
        link.style.display = 'inline-flex';
    } else {
        link.style.display = 'none';
    }
}
document.getElementById('ns_joylashuv').addEventListener('input', nsUpdateJoylashuvLink);

// File upload for new shartnoma
function nsPreviewFile(event, id) {
    const file=event.target.files[0];
    if (!file) return;
    _nsFiles[id]=file;
    document.getElementById(id+'_ph').style.display='none';
    document.getElementById(id+'_prev').style.display='block';
    document.getElementById(id+'_name').textContent=file.name;
    const img=document.getElementById(id+'_img');
    if (file.type.startsWith('image/')) {
        const r=new FileReader();
        r.onload=function(e){ img.src=e.target.result; img.style.display='block'; };
        r.readAsDataURL(file);
    } else { img.style.display='none'; }
}
function nsRemoveFile(id) {
    delete _nsFiles[id];
    const inp=document.getElementById(id);
    if (inp) inp.value='';
    const ph=document.getElementById(id+'_ph');
    const prev=document.getElementById(id+'_prev');
    const img=document.getElementById(id+'_img');
    if (ph)   ph.style.display='';
    if (prev) prev.style.display='none';
    if (img)  { img.src=''; img.style.display='none'; }
}

function saveNewShartnoma() {
    const msg=document.getElementById('nsMsg');
    const btn=document.getElementById('nsSaveBtn');
    msg.className='form-msg'; msg.textContent='';

    const sanaVal      = document.getElementById('ns_sana').value;
    const tulovSanaVal = document.getElementById('ns_tulov_sana').value;
    const dokonId      = document.getElementById('ns_dokon').value;
    const summa        = parseFloat(document.getElementById('ns_summa').value)||0;
    const foiz         = parseFloat(document.getElementById('ns_foiz').value)||0;
    const muddat       = parseInt(document.getElementById('ns_muddat').value)||0;
    const about        = document.getElementById('ns_about').value.trim();

    if (!sanaVal)      { msg.className='form-msg error show'; msg.textContent='Shartnoma sanasini kiriting.'; return; }
    if (!tulovSanaVal) { msg.className='form-msg error show'; msg.textContent='To\'lov sanasini kiriting.'; return; }
    if (!dokonId)      { msg.className='form-msg error show'; msg.textContent='Do\'konni tanlang.'; return; }
    if (summa<=0)      { msg.className='form-msg error show'; msg.textContent='Summani kiriting.'; return; }
    if (muddat<=0)     { msg.className='form-msg error show'; msg.textContent='Muddatni kiriting.'; return; }

    const sana      = sanaVal.length===16      ? sanaVal+':00'      : sanaVal;
    const tulovSana = tulovSanaVal.length===16 ? tulovSanaVal+':00' : tulovSanaVal;

    const dto = {
        mijozId:       _currentMijozId,
        dokonId:       dokonId,
        summa:         summa,
        foiz:          foiz,
        muddat:        muddat,
        izoh:          document.getElementById('ns_izoh').value.trim(),
        about:         about,
        tannarx:       document.getElementById('ns_tannarx').value.trim(),
        oldindanTulov: parseFloat(document.getElementById('ns_oldindan').value)||0,
        joylashuv:     document.getElementById('ns_joylashuv').value.trim(),
        shartnomaSana: sana,
        tulovSana:     tulovSana
    };

    btn.disabled=true; btn.textContent='⏳ Yaratilmoqda...';

    const fd=new FormData();
    fd.append('mijoz', new Blob([JSON.stringify(dto)], {type:'application/json'}));
    if (_nsFiles['ns_kaf1']) fd.append('rasm',  _nsFiles['ns_kaf1']);
    if (_nsFiles['ns_kaf2']) fd.append('rasm2', _nsFiles['ns_kaf2']);

    fetch('/admin/add-shartnoma', {method:'POST', body:fd})
        .then(function(r){ if(!r.ok) return r.text().then(function(t){throw t||'Server xatosi: '+r.status;}); return r.text(); })
        .then(function(resText){
            msg.className='form-msg success show';
            msg.textContent=resText||'✅ Shartnoma muvaffaqiyatli yaratildi!';
            setTimeout(function(){ closeNewShartnoma(); loadData(); }, 1000);
        })
        .catch(function(err){ msg.className='form-msg error show'; msg.textContent='Xatolik: '+err; btn.disabled=false; btn.textContent='💾 Shartnoma yaratish'; });
}

// ══════ UMUMIY TO'LOV MODALI ══════
// Endi to'lov aniq bir jadval qatoriga emas, balki shartnomaga umuman qilinadi.
// Kiritilgan summa jadval bo'yicha eng eski to'lanmagan qatordan boshlab
// ketma-ket "yopib" boriladi (FIFO), qolган qism keyingi jadval(lar)ga o'tadi.
function openPayModal() {
    document.getElementById('pay_umumiy').textContent   = fmtSumma(_currentTotalSumma);
    document.getElementById('pay_tulangan').textContent = fmtSumma(_currentTotalTulandi);
    document.getElementById('pay_qolgan').textContent   = fmtSumma(_currentTotalQolgan);
    document.getElementById('pay_summa').value = _currentTotalQolgan>0 ? _currentTotalQolgan : '';
    // document.getElementById('pay_sana_input').value = nowLocalISO();
    document.getElementById('pay_turi_hidden').value='';
    document.querySelectorAll('.pay-type-btn').forEach(function(b){b.classList.remove('selected');});
    const msg=document.getElementById('payFormMsg');
    msg.className='form-msg'; msg.textContent='';
    const btn=document.getElementById('paySaveBtn');
    btn.disabled=false; btn.textContent='💾 Saqlash';
    loadMagazinlar('pay_dokon','dokonSelectWrap');
    document.getElementById('payModal').classList.add('show');
    setTimeout(function(){document.getElementById('pay_summa').focus();},120);
}
// Eng yaqin (eng eski) to'lanmagan oyni topib, uni yopish uchun kerak bo'lgan
// summani "To'lov summasi" maydoniga qo'yadi.
function fillOneMonthAmount() {
    const grafik = _currentGrafik || [];
    const engYaqinOy = grafik.find(function(g) { return g.holat === 'tulanmagan'; });
    if (!engYaqinOy) {
        const msg = document.getElementById('payFormMsg');
        msg.className = 'form-msg error show';
        msg.textContent = "Yopilishi kerak bo'lgan oy topilmadi.";
        return;
    }
    const kerak = (Number(engYaqinOy.summa) || 0) - (Number(engYaqinOy.tulangan) || 0);
    document.getElementById('pay_summa').value = kerak > 0 ? kerak : '';
    document.getElementById('pay_summa').focus();
}
function selectTuri(btn) {
    document.querySelectorAll('.pay-type-btn').forEach(function(b){b.classList.remove('selected');});
    btn.classList.add('selected');
    document.getElementById('pay_turi_hidden').value=btn.getAttribute('data-turi');
}
function closePayModal() { document.getElementById('payModal').classList.remove('show'); }
document.getElementById('payModal').addEventListener('click', function(e){ if(e.target===this) closePayModal(); });

// ══════ SHARTNOMANI O'CHIRISH (Xavfli zona) ══════
function openDeleteShartnoma() {
    document.getElementById('delContractNo').textContent = '№'+(_currentShartnomaId??'—');
    document.getElementById('delConfirmInput').value='';
    document.getElementById('delConfirmBtn').disabled=true;
    document.getElementById('delConfirmBtn').textContent="🗑️ O'chirish";
    const msg=document.getElementById('delMsg');
    msg.className='form-msg'; msg.textContent='';
    document.getElementById('deleteShartnomaModal').classList.add('show');
}
function closeDeleteShartnoma() {
    document.getElementById('deleteShartnomaModal').classList.remove('show');
}
document.getElementById('deleteShartnomaModal').addEventListener('click', function(e){ if(e.target===this) closeDeleteShartnoma(); });

function delToggleBtn() {
    const val=document.getElementById('delConfirmInput').value.trim().toUpperCase();
    document.getElementById('delConfirmBtn').disabled = (val!=="O'CHIRISH" && val!=='OCHIRISH');
}

function confirmDeleteShartnoma() {
    if (!_currentShartnomaId) return;
    const msg=document.getElementById('delMsg');
    const btn=document.getElementById('delConfirmBtn');
    msg.className='form-msg'; msg.textContent='';
    btn.disabled=true; btn.textContent='⏳ O\'chirilmoqda...';

    fetch('/admin/delete-shartnoma/'+_currentShartnomaId, {method:'DELETE'})
        .then(function(r){ return r.text().then(function(t){ if(!r.ok) throw t||'Server xatosi: '+r.status; return t; }); })
        .then(function(){
            msg.className='form-msg success show';
            msg.textContent="✅ Shartnoma o'chirildi. Yo'naltirilmoqda...";
            setTimeout(function(){ window.location.href='/admin/shartnomalar'; }, 1000); // ← kerakli sahifaga moslang
        })
        .catch(function(err){
            msg.className='form-msg error show';
            msg.textContent='Xatolik: '+err;
            btn.disabled=false; btn.textContent="🗑️ O'chirish";
        });
}

document.addEventListener('keydown', function(e){ if(e.key==='Escape'){ closePayModal(); closeNewShartnoma(); closeDeleteShartnoma(); } });

// Kiritilgan summani jadval bo'yicha eng eski to'lanmagan qatordan
// boshlab FIFO tartibida taqsimlab, har biriga alohida so'rov yuboradi.
function submitPayment() {
    const summaInput = Math.round(Number(document.getElementById('pay_summa').value) || 0);
    // const sanaVal     = document.getElementById('pay_sana_input').value;
    const dokonId     = document.getElementById('pay_dokon').value;
    const turi        = document.getElementById('pay_turi_hidden').value;
    const msg = document.getElementById('payFormMsg');
    const btn = document.getElementById('paySaveBtn');
    msg.className = 'form-msg'; msg.textContent = '';

    if (!summaInput) { msg.className = 'form-msg error show'; msg.textContent = "To'lov summasini to'g'ri kiriting."; return; }
    // if (!sanaVal)  { msg.className = 'form-msg error show'; msg.textContent = "To'lov sanasini kiriting."; return; }
    if (!dokonId)  { msg.className = 'form-msg error show'; msg.textContent = "Do'konni tanlang."; return; }
    if (!turi)     { msg.className = 'form-msg error show'; msg.textContent = "To'lov turini tanlang."; return; }

    // const sana = sanaVal.length === 16 ? sanaVal + ':00' : sanaVal;

    btn.disabled = true; btn.textContent = '⏳ Saqlanmoqda...';

    const url = '/admin/tulov-shartnoma'
        + '?shartnomaId=' + _currentShartnomaId
        + '&summa=' + summaInput
        + '&turi=' + encodeURIComponent(turi)
        // + '&sana=' + encodeURIComponent(sana)
        + '&dokonId=' + dokonId;

    fetch(url, { method: 'PUT' })
        .then(function(r) { return r.json().then(function(b) { if (!r.ok || b.getMessage === false) throw b; return b; }); })
        .then(function(data) {
            msg.className = 'form-msg success show';
            msg.textContent = '✅ ' + (data.message || data.getMessage || "To'lov saqlandi");
            setTimeout(function() { closePayModal(); loadData(); }, 1100);
        })
        .catch(function(err) {
            msg.className = 'form-msg error show';
            msg.textContent = 'Xatolik: ' + (err && err.message ? err.message : err);
            btn.disabled = false; btn.textContent = '💾 Saqlash';
        });
}
// ══════ MUDDAT O'ZGARTIRISH MODAL ══════
function openMuddatModal() {
    if (!_currentShartnomaId) return;
    const msg = document.getElementById('muddatMsg');
    msg.className = 'form-msg'; msg.textContent = '';
    document.getElementById('md_summa').textContent   = fmtSumma(_currentTotalSumma);
    document.getElementById('md_eski').textContent    = _currentMuddat ? _currentMuddat + ' oy' : '—';
    document.getElementById('md_tulangan').textContent= fmtSumma(_currentTotalTulandi);
    const inp = document.getElementById('md_yangi');
    inp.value = _currentMuddat || '';
    muddatUpdatePreview();
    const btn = document.getElementById('muddatSaveBtn');
    btn.disabled = false; btn.innerHTML = '&#128190; Saqlash';
    document.getElementById('muddatModal').classList.add('show');
}
function closeMuddatModal() {
    document.getElementById('muddatModal').classList.remove('show');
}
document.getElementById('muddatModal').addEventListener('click', function(e){ if(e.target===this) closeMuddatModal(); });
function muddatUpdatePreview() {
    const m   = parseInt(document.getElementById('md_yangi').value) || 0;
    const box = document.getElementById('md_preview');
    if (m < 1 || !_currentTotalSumma) { box.style.display = 'none'; return; }
    const oylik = Math.floor(_currentTotalSumma / m);
    const farq  = _currentTotalSumma - oylik * m;
    let text = 'Yangi oylik to\'lov: ' + fmtSumma(oylik) + ' so\'m × ' + m + ' oy';
    if (farq > 0) text += ' (oxirgi oy: ' + fmtSumma(oylik + farq) + ' so\'m)';
    box.textContent = text;
    box.style.display = 'block';
}
function submitMuddat() {
    const m   = parseInt(document.getElementById('md_yangi').value) || 0;
    const msg = document.getElementById('muddatMsg');
    const btn = document.getElementById('muddatSaveBtn');
    msg.className = 'form-msg'; msg.textContent = '';

    if (m < 1)  { msg.className = 'form-msg error show'; msg.textContent = 'Yangi muddatni kiriting.'; return; }
    if (m === _currentMuddat) { msg.className = 'form-msg error show'; msg.textContent = 'Muddat hozirgisi bilan bir xil.'; return; }

    btn.disabled = true; btn.textContent = '⏳ Saqlanmoqda...';

    fetch('/admin/update-muddat?shartnomaId=' + _currentShartnomaId + '&muddat=' + m, { method: 'PUT' })
        .then(function(r) { return r.json().then(function(b) { if (!r.ok || b.holat === false) throw b; return b; }); })
        .then(function(data) {
            msg.className = 'form-msg success show';
            msg.textContent = '✅ ' + (data.message || 'Muddat o\'zgartirildi');
            setTimeout(function() { closeMuddatModal(); loadData(); }, 1100);
        })
        .catch(function(err) {
            msg.className = 'form-msg error show';
            msg.textContent = 'Xatolik: ' + (err && err.message ? err.message : err);
            btn.disabled = false; btn.innerHTML = '&#128190; Saqlash';
        });
}

// -- JADVAL SANASINI O'ZGARTIRISH --
var _jadvalSanaId = null;

function openJadvalSanaModal(jadvalId) {
    var g = (_currentGrafik || []).filter(function(x){ return x.id === jadvalId; })[0];
    if (!g) return;
    _jadvalSanaId = jadvalId;

    var msg = document.getElementById('jsanaMsg');
    msg.className = 'form-msg'; msg.textContent = '';

    var idx = (_currentGrafik || []).indexOf(g) + 1;
    document.getElementById('js_oy').textContent    = idx + '-oy';
    document.getElementById('js_eski').textContent  = fmtDate(g.sana);
    document.getElementById('js_summa').textContent = fmtSumma(Number(g.summa) || 0);

    document.getElementById('js_yangi').value = g.sana ? String(g.sana).slice(0, 10) : '';

    var btn = document.getElementById('jsanaSaveBtn');
    btn.disabled = false; btn.innerHTML = '&#128190; Saqlash';
    document.getElementById('jadvalSanaModal').classList.add('show');
}

function closeJadvalSanaModal() {
    document.getElementById('jadvalSanaModal').classList.remove('show');
    _jadvalSanaId = null;
}
document.getElementById('jadvalSanaModal').addEventListener('click', function(e){
    if (e.target === this) closeJadvalSanaModal();
});

function submitJadvalSana() {
    var sana = document.getElementById('js_yangi').value;
    var msg  = document.getElementById('jsanaMsg');
    var btn  = document.getElementById('jsanaSaveBtn');
    msg.className = 'form-msg'; msg.textContent = '';

    if (!_jadvalSanaId) { closeJadvalSanaModal(); return; }
    if (!sana) {
        msg.className = 'form-msg error show';
        msg.textContent = "Yangi sanani tanlang.";
        return;
    }

    btn.disabled = true; btn.textContent = 'Saqlanmoqda...';

    fetch('/admin/update-jadval-sana?jadvalId=' + _jadvalSanaId + '&sana=' + encodeURIComponent(sana), { method: 'PUT' })
        .then(function(r){ return r.json().then(function(b){ if (!r.ok || b.holat === false) throw b; return b; }); })
        .then(function(data){
            msg.className = 'form-msg success show';
            msg.textContent = '\u2705 ' + (data.message || "Sana o'zgartirildi");
            setTimeout(function(){ closeJadvalSanaModal(); loadData(); }, 900);
        })
        .catch(function(err){
            msg.className = 'form-msg error show';
            msg.textContent = 'Xatolik: ' + (err && err.message ? err.message : err);
            btn.disabled = false; btn.innerHTML = '&#128190; Saqlash';
        });
}

// ── Load page ──
function loadData() {
    if (!shartnomaId||isNaN(shartnomaId)){showError("Shartnoma ID topilmadi");document.getElementById('loadingState').style.display='none';return;}
    document.getElementById('loadingState').style.display='block';
    document.getElementById('pageContent').style.display='none';
    fetch('/admin/shartnoma-detail/'+shartnomaId)
        .then(function(r){return r.ok?r.json():Promise.reject(r.status);})
        .then(function(data){fillMijoz(data.mijoz);renderShartnoma(data);document.getElementById('pageContent').style.display='block';})
        .catch(function(err){showError("Ma'lumotlarni yuklab bo'lmadi. Server xatosi: "+err);document.getElementById('pageContent').style.display='block';})
        .finally(function(){document.getElementById('loadingState').style.display='none';});
}
loadData();