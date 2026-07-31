/* =====================================================================
 *  format.js — umumiy son formatlash (1 000 000 ko'rinishida)
 *  Gilam888 admin panel
 *
 *  ULANISH: har bir sahifada boshqa skriptlardan OLDIN:
 *      <script src="/js/format.js"></script>
 *  (yoki bir marta fragment.html ichida — barcha sahifalarga tarqaladi)
 *
 *  BERADI:
 *      formatSon(1000000)   -> "1 000 000"
 *      sonQiymat("1 000 0") -> 10000  (raqamga qaytaradi)
 *      summaMatn(1000000)   -> "1 000 000 so'm"
 *      sonInputUlash(el)    -> bitta inputni qo'lda ulash
 *
 *  INPUTLAR: <input data-son> deb belgilangan maydonlar yozayotganda
 *  avtomatik bo'shliq qo'yadi. MUHIM: el.value HAR DOIM toza raqam
 *  qaytaradi ("1000000"), ekranda esa "1 000 000" ko'rinadi — shuning
 *  uchun eski JS kodlarni (parseInt(el.value), $('#summa').val())
 *  o'zgartirish shart emas.
 * ===================================================================== */
(function () {
    'use strict';

    // Bo'shliqning barcha turlari: oddiy, NBSP (\u00A0), narrow NBSP (\u202F)
    var BOSHLIQ = /[\s\u00A0\u202F]/g;

    /** Sonni "1 000 000" ko'rinishiga keltiradi. Kasr qismi vergul bilan qoladi. */
    function formatSon(n) {
        if (n === null || n === undefined || n === '') return '';

        var s = String(n).replace(BOSHLIQ, '').replace(',', '.');
        var manfiy = s.charAt(0) === '-';
        if (manfiy) s = s.slice(1);

        var qism = s.split('.');
        var butun = qism[0].replace(/\D/g, '');
        if (butun === '' && qism.length < 2) return manfiy ? '-' : '';
        if (butun === '') butun = '0';

        butun = butun.replace(/\B(?=(\d{3})+(?!\d))/g, ' ');

        var natija = butun;
        if (qism.length > 1) {
            var kasr = qism[1].replace(/\D/g, '');
            natija = butun + ',' + kasr;
        }
        return (manfiy ? '-' : '') + natija;
    }

    /** "1 000 000" / "1000000" / 1000000  ->  1000000 (Number) */
    function sonQiymat(v) {
        if (typeof v === 'number') return v;
        var s = String(v === null || v === undefined ? '' : v)
            .replace(BOSHLIQ, '')
            .replace(',', '.');
        var n = parseFloat(s);
        return isNaN(n) ? 0 : n;
    }

    /** 1000000 -> "1 000 000 so'm" */
    function summaMatn(n) {
        return formatSon(Math.round(sonQiymat(n))) + " so'm";
    }

    // ── Input maydonlarini ulash ────────────────────────────────────────
    var nativeValue = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value');

    function sonInputUlash(el) {
        if (!el || el.dataset.sonUlangan === '1') return;
        el.dataset.sonUlangan = '1';

        // type="number" bo'shliqni qabul qilmaydi — matn maydoniga o'tkazamiz
        if (el.type === 'number') {
            el.type = 'text';
            el.setAttribute('inputmode', 'numeric');
        }
        el.setAttribute('autocomplete', 'off');

        // el.value ni "toza raqam" qilib qaytaradigan qilamiz.
        // Bu tufayli eski kodlar (parseInt / Number / jQuery .val()) ishlayveradi.
        Object.defineProperty(el, 'value', {
            configurable: true,
            get: function () {
                return nativeValue.get.call(this).replace(BOSHLIQ, '').replace(',', '.');
            },
            set: function (v) {
                nativeValue.set.call(this, formatSon(v));
            }
        });

        el.addEventListener('input', function () {
            var joy = el.selectionStart;
            var oldin = nativeValue.get.call(el);
            // kursorgacha nechta raqam bor edi — keyin shu joyga qaytaramiz
            var raqamSoni = oldin.slice(0, joy).replace(/\D/g, '').length;

            var yangi = formatSon(oldin);
            if (yangi === oldin) return;

            nativeValue.set.call(el, yangi);

            var i = 0, k = 0;
            while (i < yangi.length && k < raqamSoni) {
                if (yangi.charCodeAt(i) >= 48 && yangi.charCodeAt(i) <= 57) k++;
                i++;
            }
            try { el.setSelectionRange(i, i); } catch (e) { /* type qo'llamasa — e'tiborsiz */ }
        });

        // Sahifa yuklanganda bazadan kelgan qiymat ham formatlansin
        var boshlangich = nativeValue.get.call(el);
        if (boshlangich) nativeValue.set.call(el, formatSon(boshlangich));
    }

    function hammasiniUlash(root) {
        var list = (root || document).querySelectorAll('input[data-son]');
        for (var i = 0; i < list.length; i++) sonInputUlash(list[i]);
    }

    // ── Ko'rsatish: mavjud kodlar toLocaleString('uz-UZ') ishlatadi ─────
    // Brauzerga qarab u NBSP yoki vergul berishi mumkin. Hammasini bir xil
    // oddiy bo'shliqqa keltiramiz — natija hamma joyda "1 000 000".
    // (Bu blok kerak bo'lmasa — o'chirib tashlash mumkin.)
    var eskiToLocale = Number.prototype.toLocaleString;
    Number.prototype.toLocaleString = function (loc, opt) {
        if (!opt && (loc === undefined || loc === 'uz-UZ' || loc === 'uz' || loc === 'ru-RU')) {
            return formatSon(this.valueOf());
        }
        return eskiToLocale.apply(this, arguments);
    };

    // ── Ishga tushirish ─────────────────────────────────────────────────
    function boshla() {
        hammasiniUlash(document);
        // Modal ichida keyinroq yaratilgan inputlar ham ulanib ketsin
        if (window.MutationObserver && document.body) {
            new MutationObserver(function (muts) {
                for (var i = 0; i < muts.length; i++) {
                    var nodes = muts[i].addedNodes;
                    for (var j = 0; j < nodes.length; j++) {
                        var nd = nodes[j];
                        if (nd.nodeType !== 1) continue;
                        if (nd.matches && nd.matches('input[data-son]')) sonInputUlash(nd);
                        if (nd.querySelectorAll) hammasiniUlash(nd);
                    }
                }
            }).observe(document.body, { childList: true, subtree: true });
        }
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', boshla);
    } else {
        boshla();
    }

    // Global
    window.formatSon = formatSon;
    window.sonQiymat = sonQiymat;
    window.summaMatn = summaMatn;
    window.sonInputUlash = sonInputUlash;
})();
