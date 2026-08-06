/* =====================================================================
 *  iqtibos.js — login qilgandan keyin chiqadigan motivatsion oyna
 *  Gilam888 admin panel
 *
 *  ULANISH:
 *    1. login.js da redirectdan OLDIN belgi qo'yiladi (pastdagi izohga qarang)
 *    2. dashboard.html oxiriga:  <script src="/js/iqtibos.js"></script>
 *
 *  XULQ:
 *    - Faqat YANGI login qilingandan keyin chiqadi. Dashboardni qayta
 *      yangilaganda yoki menyudan qaytib kelganda chiqmaydi.
 *    - Har safar boshqa iqtibos. Oxirgi 8 tasi eslab qolinadi va
 *      takrorlanmaydi (localStorage).
 *    - "Rahmat" tugmasi, Esc yoki fondan tashqariga bosish bilan yopiladi.
 * ===================================================================== */
(function () {
    'use strict';

    // Login qilinganda qo'yiladigan belgi (login.js shu nomni ishlatadi)
    var BELGI = 'gilam-yangi-kirish';
    // Yaqinda ko'rsatilgan iqtiboslar (takrorlanmasligi uchun)
    var TARIX = 'gilam-iqtibos-tarix';
    var TARIX_UZUNLIGI = 8;

    var IQTIBOSLAR = [
        "Bugun boshlagan ishing, bir oydan keyin ortga qarab faxrlanadigan narsangga aylanadi.",
        "Kuningni rejalashtir — aks holda kuning seni rejalashtiradi.",
        "Katta natijalar bir kunda emas, har kuni tug'iladi.",
        "Sen qila oladigan eng qiyin ish — boshlash. Qolgani osonroq.",
        "Har bir qo'ng'iroq — ishonchni tiklash uchun yangi imkoniyat.",
        "Motivatsiya boshlashga yordam beradi, odat esa oxiriga yetkazadi.",
        "Bugun charchaganing — ertaga kuchliroq bo'lishingning belgisi.",
        "Muammoni emas, keyingi qadamni o'yla. Qadam har doim topiladi.",
        "Mijoz raqamini terishdan oldin bir nafas ol. Ovozing tinch bo'lsa, suhbat ham tinch o'tadi.",
        "Kichik ishlarni tartibli qilgan odam, katta ishga tayyor bo'ladi.",
        "Bugun qiynalgan narsang, uch oydan keyin oddiy ishga aylanadi.",
        "Kuchli bo'lganing uchun davom etmaysan — davom etganing uchun kuchli bo'lasan.",
        "Bitta yopilgan shartnoma — o'nta rejadan qimmatroq.",
        "Xato qilishdan qo'rqma. Bir xil xatoni ikki marta qilishdan qo'rq.",
        "Ro'yxatingdagi eng noqulay ishni birinchi qil — qolgan kun yengil o'tadi.",
        "Har bir mijoz ortida bir oila turibdi. Shuni yodda tutib gaplash.",
        "Shoshilma, lekin to'xtama.",
        "Bugungi bitta halol suhbat — yuzta shoshilinch qo'ng'iroqdan foydali.",
        "O'zingni kecha bilan solishtir, boshqalar bilan emas.",
        "Rad javobi — yakun emas, shunchaki hozircha 'yo'q' degani.",
        "Eng yaxshi paytni kutma. Bor paytni eng yaxshisiga aylantir.",
        "Tayyor bo'lguningcha kutsang, hech qachon boshlamaysan.",
        "Bugungi tartibing — ertangi xotirjamliging.",
        "Qiyin suhbatni ertaga qoldirsang, u ertaga yanada qiyinlashadi.",
        "Sabr — bu kutish emas, kutayotganda ham ishlashda davom etish.",
        "Diqqating qayerda bo'lsa, natijang ham o'sha yerda bo'ladi.",
        "Bir ishni oxirigacha qilgan, o'nta ishni yarim qilgandan ustun.",
        "Ishonch sekin quriladi, tez buziladi — har bir va'dangni bajar.",
        "Bugun 1% yaxshiroq ishla. Bir yilda bu ulkan farqqa aylanadi.",
        "Charchoq — to'xtash belgisi emas, dam olish belgisi. Dam ol va davom et.",
        "Sen nazorat qila oladigan yagona narsa — keyingi harakating.",
        "Yaxshi kayfiyatni kutma — harakat kayfiyatni o'zi olib keladi.",
        "Hurmat bilan aytilgan so'z eng qattiq dalildan kuchliroq.",
        "Bugun qilingan ish — ertaga qilishing shart bo'lmagan ish.",
        "Har kuni bir qadam — bir yilda uch yuz oltmish besh qadam.",
        "O'zingga ishon: bu ishni sendan yaxshi biladigan odam yo'q.",
        "Natija ko'rinmayotgan bo'lsa ham, mehnating yo'qolmaydi — u to'planyapti.",
        "Eng samarali odam ko'p ishlagan emas, keraklisini qilgan odam.",
        "Bugun ham keldingu, ishga kirishding — bu allaqachon g'alaba.",
        "Ertaga o'zingga rahmat aytadigan qilib bugun ishla."
    ];

    // ── Takrorlanmaydigan tasodifiy tanlash ────────────────────────────
    function tarixOl() {
        try { return JSON.parse(localStorage.getItem(TARIX) || '[]'); }
        catch (e) { return []; }
    }

    function tanla() {
        var tarix = tarixOl();
        var qolgan = [];
        for (var i = 0; i < IQTIBOSLAR.length; i++) {
            if (tarix.indexOf(i) === -1) qolgan.push(i);
        }
        // Hammasi ko'rsatilib bo'lgan bo'lsa — tarixni tozalaymiz
        if (!qolgan.length) { qolgan = IQTIBOSLAR.map(function (_, i) { return i; }); tarix = []; }

        var tanlangan = qolgan[Math.floor(Math.random() * qolgan.length)];

        tarix.push(tanlangan);
        while (tarix.length > TARIX_UZUNLIGI) tarix.shift();
        try { localStorage.setItem(TARIX, JSON.stringify(tarix)); } catch (e) { /* e'tiborsiz */ }

        return IQTIBOSLAR[tanlangan];
    }

    // ── Uslub ──────────────────────────────────────────────────────────
    function uslubQosh() {
        if (document.getElementById('iqtibosStyle')) return;
        var st = document.createElement('style');
        st.id = 'iqtibosStyle';
        st.textContent = [
            '.iq-overlay{position:fixed;inset:0;z-index:2000;display:flex;align-items:center;justify-content:center;',
            'background:rgba(10,30,45,.55);backdrop-filter:blur(3px);padding:20px;opacity:0;transition:opacity .25s ease;}',
            '.iq-overlay.ochiq{opacity:1;}',
            '.iq-box{background:var(--bg-card,#fff);border:1px solid var(--color-card-bdr,#c5dff0);border-radius:18px;',
            'max-width:480px;width:100%;padding:2.2rem 2rem 1.6rem;text-align:center;',
            'box-shadow:0 18px 50px rgba(0,0,0,.22);transform:translateY(14px) scale(.97);transition:transform .28s cubic-bezier(.2,.8,.3,1);}',
            '.iq-overlay.ochiq .iq-box{transform:translateY(0) scale(1);}',
            '.iq-quote{font-size:34px;line-height:1;color:#1a6fa8;opacity:.35;margin-bottom:.4rem;}',
            '[data-theme="dark"] .iq-quote{color:#5ab4dc;}',
            '.iq-matn{font-size:17px;line-height:1.6;font-weight:600;color:var(--color-text,#1a3a52);margin:0 0 1.5rem;}',
            '.iq-salom{font-size:12px;font-weight:700;letter-spacing:.06em;text-transform:uppercase;',
            'color:var(--color-sub,#5a8fa8);margin:0 0 1.2rem;}',
            '.iq-btn{background:#1a6fa8;color:#fff;border:none;border-radius:10px;padding:11px 30px;',
            'font-size:14px;font-weight:600;cursor:pointer;transition:background .2s;}',
            '.iq-btn:hover{background:#155d8e;}',
            '.iq-btn:active{transform:scale(.98);}'
        ].join('');
        document.head.appendChild(st);
    }

    // ── Oynani ko'rsatish ──────────────────────────────────────────────
    function korsat() {
        uslubQosh();
        var iq = tanla();

        var overlay = document.createElement('div');
        overlay.className = 'iq-overlay';
        overlay.setAttribute('role', 'dialog');
        overlay.setAttribute('aria-modal', 'true');

        overlay.innerHTML =
            '<div class="iq-box">'
            + '<p class="iq-salom">' + salomlash() + '</p>'
            + '<div class="iq-quote">&#8220;</div>'
            + '<p class="iq-matn">' + iq + '</p>'
            + '<button class="iq-btn" type="button">Rahmat</button>'
            + '</div>';

        document.body.appendChild(overlay);
        requestAnimationFrame(function () { overlay.classList.add('ochiq'); });

        var btn = overlay.querySelector('.iq-btn');
        btn.focus();

        function yop() {
            overlay.classList.remove('ochiq');
            document.removeEventListener('keydown', esc);
            setTimeout(function () { overlay.remove(); }, 250);
        }
        function esc(e) { if (e.key === 'Escape') yop(); }

        btn.addEventListener('click', yop);
        overlay.addEventListener('click', function (e) { if (e.target === overlay) yop(); });
        document.addEventListener('keydown', esc);
    }

    function salomlash() {
        var s = new Date().getHours();
        if (s < 5)  return 'Xayrli tun';
        if (s < 11) return 'Xayrli tong';
        if (s < 17) return 'Xayrli kun';
        return 'Xayrli kech';
    }

    // ── Ishga tushirish: faqat yangi login qilingan bo'lsa ─────────────
    function boshla() {
        var yangiKirish;
        try { yangiKirish = sessionStorage.getItem(BELGI); } catch (e) { return; }
        if (!yangiKirish) return;

        // Belgi darhol o'chiriladi — sahifa yangilansa qayta chiqmasin
        try { sessionStorage.removeItem(BELGI); } catch (e) { /* e'tiborsiz */ }

        setTimeout(korsat, 400); // dashboard chizilib bo'lsin
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', boshla);
    } else {
        boshla();
    }

    // Sinash uchun: konsolda  iqtibosKorsat()  deb yozing
    window.iqtibosKorsat = korsat;
})();