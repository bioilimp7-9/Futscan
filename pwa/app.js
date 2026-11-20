/* FOODSCAN AI — Responsive PWA (Android Chrome, iPhone Safari, iPad, Desktop) */

const MISSING_MSG = 'Бұл көрсеткіш суреттен анық оқылмады. Қаптамадан қолмен тексеріңіз.';
const DISCLAIMER_TEXT = 'FOODSCAN AI — білім беру және ақпараттық мақсаттағы құрал. Ол медициналық кеңес бермейді.';

const SAMPLE_PACKAGES = [
  {
    id: 'rahat_chocolate',
    name: '«Қазақстан» шоколады',
    category: 'Тәттілер мен шоколад',
    manufacturer: '«Рахат» АҚ, Алматы қ.',
    ingredients: 'Қант, какао ұнтағы, какао майы, эмульгатор (соя лецитині), ваниль сығындысы.',
    servingSize: '100 г',
    basis: 'PER_100G',
    sugar: 47.0, salt: 0.05, fat: 35.6, protein: 6.3, calories: 540.0,
    rawLabelText: '100 г: Ақуыз 6.3 г, Май 35.6 г, Қант 47.0 г, Тұз 0.05 г, 540 ккал'
  },
  {
    id: 'foodmaster_yogurt',
    name: '«Ainaline» табиғи йогурты',
    category: 'Сүт өнімдері',
    manufacturer: '«ФудМастер-Продукция» ЖШС',
    ingredients: 'Нормализацияланған сүт, құрғақ майсыздандырылған сүт, тірі йогурт ашытқылары.',
    servingSize: '100 г',
    basis: 'PER_100G',
    sugar: 4.2, salt: 0.10, fat: 2.5, protein: 3.6, calories: 62.0,
    rawLabelText: '100 г: Ақуыз 3.6 г, Май 2.5 г, Қант 4.2 г, Тұз 0.1 г, 62 ккал'
  },
  {
    id: 'lays_chips',
    name: '«Lay\'s» қаймақ және жуа чипсыі',
    category: 'Снектер мен чипсы',
    manufacturer: '«Фрито Лей» ЖШС',
    ingredients: 'Картоп, өсімдік майлары, тұз, қант, қаймақ ұнтағы, жуа ұнтағы.',
    servingSize: '1 порция (30 г)',
    basis: 'PER_SERVING',
    sugar: 0.9, salt: 0.48, fat: 9.6, protein: 1.8, calories: 153.0,
    rawLabelText: '1 порция (30 г): 153 ккал, Ақуыз 1.8 г, Май 9.6 г, Қант 0.9 г, Тұз 0.48 г'
  },
  {
    id: 'juicy_apple',
    name: '«Juicy» алма-жүзім шырыны',
    category: 'Сусындар',
    manufacturer: '«RG Brands Kazakhstan» ЖШС',
    ingredients: 'Концентратталған алма және ақ жүзім шырыны. Қант қосылмаған.',
    servingSize: '100 мл',
    basis: 'PER_100G',
    sugar: 11.2, salt: 0.01, fat: 0.0, protein: 0.3, calories: 46.0,
    rawLabelText: '100 мл: Ақуыз 0.3 г, Май 0.0 г, Қант 11.2 г, Тұз 0.01 г, 46 ккал'
  }
];

const EDU_TOPICS = [
  {
    title: 'Ақуыз дегеніміз не?',
    sub: 'Жасөспірім ағзасының басты құрылыс материалы',
    body: 'Ақуыздар (протеиндер) — ағзадағы барлық жасушалардың, бұлшықеттердің, сүйектердің негізін құрайтын заттар. Көздері: ет, балық, жұмыртқа, сүт, ірімшік, бұршақ дақылдары.',
    tip: '12–17 жастағы жасөспірімге тәулігіне әр кг салмаққа 1.0–1.2 г ақуыз қажет.'
  },
  {
    title: 'Майдың ағзадағы қызметі',
    sub: 'Қаныққан, қанықпаған майлар және трансмайлар',
    body: 'Майлар энергия қоры және ми жасушалары мен гормондар құрылысына қатысады. Пайдалы майлар: өсімдік майлары, жаңғақ, балық. Трансмайларды (маргарин, чипсы) шектеу қажет.',
    tip: 'Қаптамада «гидрогенизацияланған май» жазуы болса, бұл трансмай болуы мүмкін.'
  },
  {
    title: 'Көмірсулар және энергия',
    sub: 'Күрделі және қарапайым көмірсулардың айырмасы',
    body: 'Көмірсулар — ми мен бұлшықеттердің басты отыны. Күрделі көмірсулар (сұлы, қарақұмық, нан) энергияны баяу береді, ал қарапайым қант тез шаршатады.',
    tip: 'Күнделікті рационның негізін күрделі көмірсулар құрауы тиіс.'
  },
  {
    title: 'Қант мөлшерін қалай түсінеміз?',
    sub: 'Жасырын қант және тәуліктік 25 г нормасы',
    body: 'ДДҰ жасөспірімдерге еркін қант мөлшерін тәулігіне 25 грамнан (шамамен 5-6 шай қасық) асырмауды ұсынады.',
    tip: '1 шай қасық қант = 4-5 г. 500 мл тәтті сусында 40-50 г қант болады!'
  },
  {
    title: 'Тұз мөлшерін қалай оқимыз?',
    sub: 'Натрий және ас тұзы (NaCl)',
    body: 'Тәуліктік тұз нормасы — 5 грамнан аспауы тиіс. Қаптамада «Натрий» жазылса, тұз мөлшерін табу үшін 2.5-ке көбейтіңіз.',
    tip: 'Тұз = Натрий × 2.5.'
  },
  {
    title: 'Тағамдық құндылық дегеніміз не?',
    sub: '100 грамм және 1 порция айырмашылығы',
    body: 'Өндірушілер кестені 100 г немесе 1 порция (мысалы 25 г) үшін көрсетеді. Екі өнімді тек бірдей өлшем негізіне келтіріп салыстыру керек.',
    tip: '100 г мен 30 г порцияны тікелей салыстыруға БОЛМАЙДЫ.'
  },
  {
    title: 'Өнім жапсырмасын дұрыс оқу жолдары',
    sub: 'Ингредиенттер тәртібі мен сақтау мерзімі',
    body: 'Ингредиенттер өнімдегі мөлшерінің азаю ретімен жазылады. Егер бірінші орында «Қант» тұрса, бұл өнімде қант үлесі ең жоғары.',
    tip: 'Алғашқы 3 ингредиент өнімнің нақты құрамын көрсетеді.'
  }
];

const INITIAL_SURVEYS = [
  { age: 14, q1: 'Кейде', q2: 'Қиындықпен', q3: 'Шамамен білемін', q4: 'Кейде шатасамын', q5: 'Өте пайдалы болды' },
  { age: 13, q1: 'Жоқ, ешқашан', q2: 'Таба алмаймын', q3: 'Білмеймін', q4: 'Жоқ, қиналамын', q5: 'Өте пайдалы болды' },
  { age: 15, q1: 'Иә, үнемі', q2: 'Оңай табамын', q3: 'Иә, жақсы білемін', q4: 'Иә, оңай', q5: 'Біраз көмектесті' },
  { age: 14, q1: 'Кейде', q2: 'Қиындықпен', q3: 'Білмеймін', q4: 'Кейде шатасамын', q5: 'Өте пайдалы болды' }
];

const PROTOCOL_INSTRUCTIONS = [
  { step: 1, sheet: '1-парақ: Нұсқаулық', title: 'Зерттеу хаттамасы және деректер жинау тәртібі', desc: '12–17 жастағы оқушылардың тағам жапсырмаларын оқу сауаттылығын және FOODSCAN AI жүйесінің тиімділігін тексеру реті.' },
  { step: 2, sheet: '2-парақ: 30 өнім', title: 'Қаптамадағы және AI оқыған мәндерді салыстыру', desc: '30 түрлі тағам өнімін сканерлеп, AI оқыған мәндерді нақты қаптамамен салыстырып тексеріңіз.' },
  { step: 3, sheet: '3-парақ: Оқушы тесті', title: 'Пайдалануға дейінгі және кейінгі жауаптар', desc: 'Оқушы қосымшаны қолданбай тұрып «Дейін (Pre)» және қолданғаннан кейін «Кейін (Post)» тестін тапсырады.' },
  { step: 4, sheet: '4-парақ: Тест сұрақтары', title: '8 тапсырмадан тұратын білім тесті', desc: 'Қант/тұз нормасы, 100 г vs порция, ингредиенттер реті, трансмай, ақуыз, калория және бұлдыр жапсырма бойынша 8 сұрақ.' },
  { step: 5, sheet: '5-парақ: Жауап кілті', title: 'Дұрыс жауаптар мен түсіндірмелер', desc: 'Барлық 8 тапсырманың дұрыс жауабы және ғылыми-білім беру түсіндірмелері.' },
  { step: 6, sheet: '6-парақ: Қорытынды', title: 'OCR дәлдігі мен тест нәтижелерін автоматты есептеу', desc: 'Жалпы және нутриент бойынша OCR дәлдігі (%), сондай-ақ Pre/Post тест өсімі (Δ%) автоматты есептеледі.' }
];

const EIGHT_QUESTIONS = [
  { n: 1, topic: 'Қант нормасы', q: 'ДДҰ жасөспірімдерге тәулігіне еркін қант мөлшерін неше грамнан асырмауды ұсынады?', opts: ['A) 10 г', 'B) 25 г (5–6 шай қасық)', 'C) 75 г', 'D) 120 г'], ans: 1, exp: 'ДДҰ ұсынысы бойынша тәуліктік еркін қант шегі — 25 г.' },
  { n: 2, topic: 'Натрий және тұз', q: 'Қаптамада «Натрий: 0.8 г» деп жазылса, ас тұзының (NaCl) мөлшері қанша?', opts: ['A) 0.8 г', 'B) 1.6 г', 'C) 2.0 г', 'D) 4.0 г'], ans: 2, exp: 'Тұз = Натрий × 2.5. Сондықтан 0.8 × 2.5 = 2.0 г.' },
  { n: 3, topic: '100 г және порция', q: 'А өнімі 100 г (қант 12 г), Б өнімі 1 порция = 25 г (қант 6 г) болса, қалай салыстырамыз?', opts: ['A) Б өнімінде аз (6 < 12)', 'B) Екеуін де 100 г негізіне келтіреміз (Б: 24 г / 100 г)', 'C) Тек калорияны салыстырамыз', 'D) Порцияны 100-ге бөлеміз'], ans: 1, exp: '25 г × 4 = 100 г болғандықтан, Б өнімінде 100 г-да 24 г қант бар.' },
  { n: 4, topic: 'Құрам тәртібі', q: 'Қаптамадағы ингредиенттер тізімі қандай тәртіппен жазылады?', opts: ['A) Әліпби ретімен', 'B) Өсу ретімен', 'C) Салмақтық үлесінің азаю ретімен (ең көбі бірінші)', 'D) Кездейсоқ'], ans: 2, exp: 'Өнімде мөлшері ең көп компонент тізімнің басында тұрады.' },
  { n: 5, topic: 'Май түрлері', q: 'Қай жазу өнімде трансмайлар болуы мүмкін екенін білдіреді?', opts: ['A) Ішінара гидрогенизацияланған өсімдік майы', 'B) Сүт ақуызы', 'C) Тағамдық талшық', 'D) Дәрумен С'], ans: 0, exp: 'Гидрогенизацияланған май — өнеркәсіптік трансмай көзі.' },
  { n: 6, topic: 'Ақуыз қызметі', q: 'Жасөспірім ағзасында ақуыз негізінен қандай қызмет атқарады?', opts: ['A) Тәтті дәм береді', 'B) Жасушалар мен бұлшықеттің құрылыс материалы', 'C) Сақтау мерзімін ұзартады', 'D) Суды алмастырады'], ans: 1, exp: 'Ақуыз — өсу және бұлшықет дамуының басты құрылыс материалы.' },
  { n: 7, topic: 'Калория есебі', q: '100 г өнімде 500 ккал болса, 40 г порцияда қанша ккал бар?', opts: ['A) 100 ккал', 'B) 150 ккал', 'C) 200 ккал', 'D) 500 ккал'], ans: 2, exp: '500 × (40 / 100) = 200 ккал.' },
  { n: 8, topic: 'Сенімді дерек', q: 'Фото бұлдыр болып тұз мөлшері оқылмаса, дұрыс әрекет қандай?', opts: ['A) Ойдан сан жазу', 'B) 0 г қою', 'C) Мәнді ойдан шығармай, қаптамадан қолмен тексеріп енгізу', 'D) Басқа өнімнен көшіру'], ans: 2, exp: 'Жетіспейтін көрсеткіштер ойдан шығарылмайды, қаптамадан қолмен тексеріледі.' }
];

const state = {
  screen: 'dashboard',
  sessionScans: 0,
  imageBase64: null,
  selectedPresetId: null,
  isScanning: false,
  isImageProcessing: false,
  lastAnalysis: null,
  scanError: null,
  products: loadStorage('foodscan_products', SAMPLE_PACKAGES.map((s, idx) => ({
    id: idx + 1,
    productNumber: idx + 1,
    productName: s.name,
    category: s.category,
    manufacturer: s.manufacturer,
    servingSizeText: s.servingSize,
    basis: s.basis,
    sugar: s.sugar, salt: s.salt, fat: s.fat, protein: s.protein, calories: s.calories,
    extractedSugar: s.sugar, extractedSalt: s.salt, extractedFat: s.fat, extractedProtein: s.protein, extractedCalories: s.calories,
    status: 'VERIFIED'
  }))),
  surveys: loadStorage('foodscan_surveys', INITIAL_SURVEYS),
  testAttempts: loadStorage('foodscan_test_attempts', []),
  protocolSheet: 0,
  compP1Id: 1,
  compP2Id: 2,
  normalize100g: false,
  deferredPrompt: null,
  eduOpenIdx: 0,
  isOffline: !navigator.onLine,
  swReady: false
};

function loadStorage(key, fallback) {
  try {
    const raw = localStorage.getItem(key);
    return raw ? JSON.parse(raw) : fallback;
  } catch {
    return fallback;
  }
}

function saveStorage(key, data) {
  try {
    localStorage.setItem(key, JSON.stringify(data));
  } catch {
    // ignore quota errors
  }
}

function updateNetworkStatus() {
  state.isOffline = !navigator.onLine;
  const banner = document.getElementById('offline-banner');
  if (banner) {
    banner.classList.toggle('hidden', !state.isOffline);
  }
}

// Register Service Worker & Install Prompt (Android Chrome + iOS Safari)
if ('serviceWorker' in navigator) {
  window.addEventListener('load', async () => {
    try {
      const reg = await navigator.serviceWorker.register('./service-worker.js', { scope: './' });
      if (reg.active) {
        state.swReady = true;
        render();
      }
      reg.addEventListener('updatefound', () => {
        const worker = reg.installing;
        worker?.addEventListener('statechange', () => {
          if (worker.state === 'activated') {
            state.swReady = true;
            render();
          }
        });
      });
      await navigator.serviceWorker.ready;
      state.swReady = true;
      render();
    } catch {
      // File:// preview inside Android WebView does not support Service Workers; HTTPS hosting does.
    }
  });
}

window.addEventListener('online', () => {
  updateNetworkStatus();
  render();
});

window.addEventListener('offline', () => {
  updateNetworkStatus();
  render();
});

window.addEventListener('beforeinstallprompt', (e) => {
  e.preventDefault();
  state.deferredPrompt = e;
  document.getElementById('install-pwa-btn')?.classList.remove('hidden');
});

const isIos = () => /iphone|ipad|ipod/i.test(navigator.userAgent);
const isInStandalone = () => window.matchMedia('(display-mode: standalone)').matches || window.navigator.standalone === true;

window.addEventListener('DOMContentLoaded', () => {
  if (isIos() && !isInStandalone()) {
    document.getElementById('install-pwa-btn')?.classList.remove('hidden');
  }
  updateNetworkStatus();
  render();
});

window.handleInstallClick = async function () {
  if (state.deferredPrompt) {
    state.deferredPrompt.prompt();
    await state.deferredPrompt.userChoice;
    state.deferredPrompt = null;
    document.getElementById('install-pwa-btn')?.classList.add('hidden');
  } else if (isIos()) {
    document.getElementById('ios-install-modal')?.classList.remove('hidden');
  } else {
    alert('Браузер мәзірінен «Басты экранға қосу» (Add to Home Screen) тармағын таңдаңыз.');
  }
};

window.closeIosModal = function () {
  document.getElementById('ios-install-modal')?.classList.add('hidden');
};

window.navigateTo = function (screen) {
  state.screen = screen;
  document.querySelectorAll('.nav-btn, .rail-btn').forEach((btn) => {
    btn.classList.toggle('active', btn.dataset.screen === screen);
  });
  window.scrollTo({ top: 0, behavior: 'smooth' });
  render();
};

function calculateAccuracy() {
  const checked = state.products.filter((p) => p.status !== 'UNVERIFIED');
  if (!checked.length) return 100;
  let totalFields = 0;
  let matches = 0;
  const keys = ['sugar', 'salt', 'fat', 'protein', 'calories'];
  const extKeys = ['extractedSugar', 'extractedSalt', 'extractedFat', 'extractedProtein', 'extractedCalories'];

  checked.forEach((p) => {
    keys.forEach((k, i) => {
      const a = p[k];
      const b = p[extKeys[i]];
      if (a != null && b != null) {
        totalFields++;
        if (Math.abs(a - b) <= 0.15) matches++;
      }
    });
  });
  return totalFields ? (matches / totalFields) * 100 : 100;
}

function parseLabelText(text, hint = 'Жүктелген өнім') {
  const clean = (text || '').replace(/,/g, '.');
  const isServing = /порция|serving/i.test(clean);
  const basis = isServing ? 'PER_SERVING' : 'PER_100G';

  const findVal = (re) => {
    const m = clean.match(re);
    return m ? parseFloat(m[1]) : null;
  };

  return {
    productName: hint,
    category: 'Зерттелетін өнім',
    manufacturer: 'Қаптама дерегі',
    ingredients: '',
    servingSize: isServing ? '1 порция (30 г)' : '100 г',
    basis,
    sugar: findVal(/(?:қант|сахар|sugar)[^0-9]{0,20}([0-9]+(?:\.[0-9]+)?)/i),
    salt: findVal(/(?:тұз|соль|salt|натрий)[^0-9]{0,20}([0-9]+(?:\.[0-9]+)?)/i),
    fat: findVal(/(?:май|жир\w*|fat)[^0-9]{0,20}([0-9]+(?:\.[0-9]+)?)/i),
    protein: findVal(/(?:ақуыз|бел\w*|protein)[^0-9]{0,20}([0-9]+(?:\.[0-9]+)?)/i),
    calories: findVal(/([0-9]+(?:\.[0-9]+)?)\s*(?:ккал|kcal)/i),
    rawLabelText: text || ''
  };
}

function compressImageFile(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = (ev) => {
      const img = new Image();
      img.onload = () => {
        const maxDim = 1280;
        let { width, height } = img;
        if (width > maxDim || height > maxDim) {
          const ratio = Math.min(maxDim / width, maxDim / height);
          width = Math.round(width * ratio);
          height = Math.round(height * ratio);
        }
        const canvas = document.createElement('canvas');
        canvas.width = width;
        canvas.height = height;
        canvas.getContext('2d').drawImage(img, 0, 0, width, height);
        resolve(canvas.toDataURL('image/jpeg', 0.82));
      };
      img.onerror = reject;
      img.src = ev.target.result;
    };
    reader.onerror = reject;
    reader.readAsDataURL(file);
  });
}
window.compressImageFile = compressImageFile;
window._foodscanGetProducts = () => state.products;

window.handleFilePicked = async function (input) {
  const file = input.files?.[0];
  if (!file) return;
  state.isImageProcessing = true;
  render();
  try {
    state.imageBase64 = await compressImageFile(file);
    state.selectedPresetId = null;
    state.lastAnalysis = null;
    state.scanError = null;
  } catch {
    state.scanError = 'Суретті оқу кезінде қате шықты. Басқа фото таңдаңыз.';
  } finally {
    state.isImageProcessing = false;
    render();
  }
};

window.clearSelectedImage = function () {
  state.imageBase64 = null;
  state.selectedPresetId = null;
  state.lastAnalysis = null;
  state.scanError = null;
  render();
};

window.selectSamplePreset = function (id) {
  const sample = SAMPLE_PACKAGES.find((s) => s.id === id);
  if (!sample) return;
  state.selectedPresetId = id;
  state.imageBase64 = null;
  state.lastAnalysis = { ...sample };
  state.sessionScans++;
  render();
};

window.runScanAnalysis = async function () {
  const customText = document.getElementById('manual-ocr-text')?.value || '';
  if (!state.imageBase64 && !customText.trim() && !state.selectedPresetId) {
    state.scanError = 'Алдымен сурет түсіріңіз, галереядан таңдаңыз немесе қаптама мәтінін жазыңыз.';
    render();
    return;
  }

  state.isScanning = true;
  state.scanError = null;
  render();

  try {
    const resp = await fetch('./api/analyze', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        mode: 'composition',
        imageBase64: state.imageBase64,
        customText
      })
    });

    if (resp.ok) {
      const json = await resp.json();
      state.lastAnalysis = json;
    } else {
      // Graceful fallback when serverless proxy is not deployed locally
      state.lastAnalysis = parseLabelText(customText, 'Фотосурет жапсырмасы');
    }
    state.sessionScans++;
  } catch {
    state.lastAnalysis = parseLabelText(customText, 'Фотосурет жапсырмасы');
    state.sessionScans++;
  } finally {
    state.isScanning = false;
    render();
  }
};

window.saveCurrentToLab = function () {
  if (!state.lastAnalysis) return;
  if (state.products.length >= 30) {
    alert('Ғылыми зертхана лимиті (30 өнім) толды!');
    return;
  }

  const name = document.getElementById('edit-name')?.value || state.lastAnalysis.productName;
  const getNum = (id, orig) => {
    const el = document.getElementById(id);
    if (!el) return orig;
    const v = parseFloat(el.value);
    return Number.isNaN(v) ? null : v;
  };

  const sugar = getNum('edit-sugar', state.lastAnalysis.sugar);
  const salt = getNum('edit-salt', state.lastAnalysis.salt);
  const fat = getNum('edit-fat', state.lastAnalysis.fat);
  const protein = getNum('edit-protein', state.lastAnalysis.protein);
  const calories = getNum('edit-calories', state.lastAnalysis.calories);

  const isExact =
    sugar === state.lastAnalysis.sugar &&
    salt === state.lastAnalysis.salt &&
    fat === state.lastAnalysis.fat &&
    protein === state.lastAnalysis.protein &&
    calories === state.lastAnalysis.calories;

  const newItem = {
    id: Date.now(),
    productNumber: state.products.length + 1,
    productName: name,
    category: state.lastAnalysis.category || 'Зерттелген өнім',
    manufacturer: state.lastAnalysis.manufacturer || '',
    ingredients: state.lastAnalysis.ingredients || '',
    servingSizeText: state.lastAnalysis.servingSize || '100 г',
    basis: state.lastAnalysis.basis || 'PER_100G',
    sugar, salt, fat, protein, calories,
    extractedSugar: state.lastAnalysis.sugar,
    extractedSalt: state.lastAnalysis.salt,
    extractedFat: state.lastAnalysis.fat,
    extractedProtein: state.lastAnalysis.protein,
    extractedCalories: state.lastAnalysis.calories,
    status: isExact ? 'VERIFIED' : 'CORRECTED'
  };

  state.products.push(newItem);
  saveStorage('foodscan_products', state.products);
  state.compP1Id = newItem.id;
  window.FoodScan20?.setSelectedProduct(newItem.productName);
  alert(`«${newItem.productName}» (#${newItem.productNumber}) ғылыми зертханаға сақталды!`);
  navigateTo('comparison');
};

window.exportCsvFile = function () {
  let csv = '\uFEFF№;Өнім атауы;Санаты;Өлшем негізі;Қант (г);Тұз (г);Май (г);Ақуыз (г);Калория (ккал);Тексеру күйі\n';
  state.products.forEach((p) => {
    const b = p.basis === 'PER_SERVING' ? 'Порция' : '100 г';
    csv += `${p.productNumber};"${p.productName}";"${p.category}";"${b} (${p.servingSizeText})";${p.sugar ?? ''};${p.salt ?? ''};${p.fat ?? ''};${p.protein ?? ''};${p.calories ?? ''};${p.status}\n`;
  });
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = 'foodscan_ai_research.csv';
  a.click();
  URL.revokeObjectURL(url);
};

window.deleteLabItem = function (id) {
  state.products = state.products
    .filter((p) => p.id !== id)
    .map((p, i) => ({ ...p, productNumber: i + 1 }));
  saveStorage('foodscan_products', state.products);
  render();
};

window.submitSurveyForm = function (e) {
  e.preventDefault();
  const f = e.target;
  state.surveys.unshift({
    age: parseInt(f.age.value, 10) || 14,
    q1: f.q1.value,
    q2: f.q2.value,
    q3: f.q3.value,
    q4: f.q4.value,
    q5: f.q5.value
  });
  saveStorage('foodscan_surveys', state.surveys);
  alert('Сауалнама жауабы сәтті сақталды!');
  render();
};

function disclaimerHtml() {
  return `<div class="disclaimer-card"><strong>МЕДИЦИНАЛЫҚ ЕСКЕРТУ:</strong> ${DISCLAIMER_TEXT}</div>`;
}

function renderDashboard() {
  const acc = calculateAccuracy().toFixed(1);
  return `
    <section class="hero-card">
      <span class="hero-tag">FOODSCAN AI 2.0 • SMART FOOD ANALYSIS SYSTEM</span>
      <h1>Тағам өнімдерін кешенді цифрлық талдау</h1>
      <p>Қоректік құрамды, сыртқы визуалды белгілерді, химиялық pH көрсеткіштерін және сақтау жағдайларын бір жүйеде жинап, салыстырыңыз.</p>
      ${state.swReady ? `<div class="cache-status-pill">✓ Офлайн кэш дайын — сақталған зерттеулер интернетсіз ашылады</div>` : ''}
    </section>

    <div class="section-kicker">АҒЫМДАҒЫ СЕССИЯ ЖӘНЕ ЗЕРТХАНА</div>
    <div class="stats-grid">
      <div class="stat-card"><div class="stat-val">${state.sessionScans} өнім</div><div class="stat-title">Осы сессияда</div></div>
      <div class="stat-card"><div class="stat-val">${state.products.length} / 30</div><div class="stat-title">Зертханада</div></div>
      <div class="stat-card"><div class="stat-val">${acc}%</div><div class="stat-title">OCR дәлдігі</div></div>
    </div>

    <div class="section-kicker">НЕГІЗГІ ТАЛДАУ МОДУЛЬДЕРІ</div>
    <div class="menu-4-grid">
      <button class="menu-4-btn primary" onclick="navigateTo('scanner')">
        <div>📷</div>
        <strong>1. Құрамын сканерлеу</strong>
        <span>OCR • Қант, тұз, май, ақуыз, ккал</span>
      </button>
      <button class="menu-4-btn" onclick="navigateTo('freshness')">
        <div>👁️</div>
        <strong>2. Балғындықты талдау</strong>
        <span>Камера арқылы сыртқы белгілер</span>
      </button>
      <button class="menu-4-btn" onclick="navigateTo('chemical')">
        <div>🧪</div>
        <strong>3. Химиялық pH талдау</strong>
        <span>Индикатор қағазы және шкала</span>
      </button>
      <button class="menu-4-btn" onclick="navigateTo('storage')">
        <div>🌡️</div>
        <strong>4. Сақтау жағдайы</strong>
        <span>Температура, ылғалдылық, жарық</span>
      </button>
    </div>

    <div class="card" style="margin-bottom:12px; cursor:pointer; background:#ECFDF5; border-color:#6EE7B7;" onclick="navigateTo('unified')">
      <div class="section-kicker" style="margin-top:0; color:#047857;">ЖАҢА БІРІКТІРІЛГЕН ЕСЕП (A–D) ЖӘНЕ ДИНАМИКА</div>
      <strong>📑 Біріктірілген өнім карточкасы және күндер бойынша салыстыру</strong>
      <p style="font-size:12px; color:#065F46; margin:4px 0 0;">A. Құрам • B. Визуалды • C. Химиялық pH • D. Сақтау жағдайы + графиктер →</p>
    </div>

    <div class="tiles-grid">
      <button class="tile-card" onclick="navigateTo('comparison')">
        <div>⚖️</div>
        <h3>Өнімдерді салыстыру</h3>
        <p>2 өнім құрамын қатар салыстыру</p>
      </button>
      <button class="tile-card" onclick="navigateTo('research')">
        <div>🧪</div>
        <h3>Менің зерттеуім</h3>
        <p>30 өнім зертханасы және CSV</p>
      </button>
      <button class="tile-card" onclick="navigateTo('education')">
        <div>📘</div>
        <h3>Тағам туралы білім</h3>
        <p>7 танымдық анықтамалық</p>
      </button>
      <button class="tile-card" onclick="navigateTo('survey')">
        <div>📊</div>
        <h3>Жасөспірімдер сауалнамасы</h3>
        <p>5 сұрақ және статистика</p>
      </button>
    </div>

    <div class="card" style="margin-top:14px; cursor:pointer; background:#EFF6FF; border-color:#93C5FD;" onclick="navigateTo('protocol')">
      <div class="section-kicker" style="margin-top:0; color:#1E40AF;">ЗЕРТТЕУ ХАТТАМАСЫ (6 ПАРАҚ)</div>
      <strong>Нұсқаулық • 30 өнім • Оқушы тесті • 8 сұрақ • Жауап кілті • Қорытынды</strong>
      <p style="font-size:12px; color:#1E3A8A; margin:4px 0 0;">Pre/Post тест тапсыру және OCR дәлдігін автоматты есептеу →</p>
    </div>

    <div class="card" style="margin-top:10px; cursor:pointer;" onclick="navigateTo('about')">
      <div class="section-kicker" style="margin-top:0;">8-СЫНЫП ОҚУШЫСЫНЫҢ ЗЕРТТЕУ ЖОБАСЫ</div>
      <strong>Қанағат Айым (8 «А» сынып)</strong>
      <p style="font-size:12px; color:#475569; margin:4px 0 0;">«Жасөспірімдерге арналған тағам өнімдерінің құрамын цифрлық талдау жүйесін әзірлеу және оның ақпараттық тиімділігін зерттеу»</p>
    </div>
    ${disclaimerHtml()}
  `;
}

function renderScanner() {
  const r = state.lastAnalysis;
  const fieldCard = (label, val, unit, id) => {
    const missing = val === null || val === undefined;
    return `
      <div class="nutrient-card">
        <div class="nutrient-head">
          <strong>${label}</strong>
          <span class="nutrient-val">${missing ? '—' : val.toFixed(1) + ' ' + unit}</span>
        </div>
        ${missing
          ? `<div class="warning-box" style="margin:4px 0 0;">${MISSING_MSG}</div>`
          : ''}
        <label style="font-size:11px; color:#64748B;">Түзету/Растау (${unit}):
          <input id="${id}" type="number" step="0.1" value="${missing ? '' : val}" placeholder="Қолмен енгізіңіз" />
        </label>
      </div>
    `;
  };

  return `
    <div class="section-kicker">ТАҒАМ ЖАПСЫРМАСЫН ТАЛДАУ</div>
    <h2 style="margin:0 0 10px;">Өнімнің фотосын жүктеу</h2>

    <div class="card">
      <div class="preview-box camera-viewport">
        ${state.imageBase64
          ? `<div style="position:relative; width:100%; display:flex; justify-content:center;">
               <img src="${state.imageBase64}" alt="Өнім фотосы" />
               <div class="camera-reticle ${(state.isScanning || state.isImageProcessing) ? 'is-processing' : ''}" aria-hidden="true"></div>
             </div>
             <button class="btn-outline" style="margin-top:8px;" onclick="clearSelectedImage()">✕ Фотоны өшіру</button>`
          : state.selectedPresetId
            ? `<strong>${SAMPLE_PACKAGES.find((s) => s.id === state.selectedPresetId)?.name}</strong>
               <span style="font-size:12px; color:#64748B;">Сынақ үлгісі таңдалды</span>`
            : `${(state.isScanning || state.isImageProcessing) ? `<div class="camera-reticle is-processing" aria-hidden="true"></div>` : ''}
               <span style="color:#64748B; font-size:13px;">Қаптама суретін камерамен түсіріңіз немесе галереядан таңдаңыз</span>`}
      </div>

      <div style="display:grid; grid-template-columns:1fr 1fr; gap:8px;">
        <label class="btn-primary btn-navy" style="cursor:pointer;">
          📷 Камера
          <input type="file" accept="image/*" capture="environment" style="display:none;" onchange="handleFilePicked(this)" />
        </label>
        <label class="btn-outline" style="cursor:pointer;">
          🖼️ Галерея
          <input type="file" accept="image/*" style="display:none;" onchange="handleFilePicked(this)" />
        </label>
      </div>

      <label style="display:block; margin-top:12px; font-size:12px; font-weight:600;">
        Қаптама мәтіні (қалауыңыз бойынша OCR көмекшісі):
        <textarea id="manual-ocr-text" rows="2" placeholder="Мысалы: 100 г: Ақуыз 6г, Май 14г, Қант 28г, Тұз 0.4г, 390 ккал"></textarea>
      </label>

      <button class="btn-primary full-width" style="margin-top:10px;" onclick="runScanAnalysis()">
        ${state.isScanning ? '⏳ Талдау жүруде...' : '🔍 Өнім жапсырмасын талдау'}
      </button>
      ${state.scanError ? `<div class="warning-box" style="margin-top:10px;">${state.scanError}</div>` : ''}
    </div>

    <div class="section-kicker">ДАЙЫН ҚАЗАҚСТАН ӨНІМДЕРІ (СЫНАҚ)</div>
    <div class="chips-row">
      ${SAMPLE_PACKAGES.map((s) => `
        <button class="chip ${state.selectedPresetId === s.id ? 'selected' : ''}" onclick="selectSamplePreset('${s.id}')">${s.name}</button>
      `).join('')}
    </div>

    ${r ? `
      <div class="section-kicker">ТАЛДАУ НӘТИЖЕЛЕРІ ЖӘНЕ РАСТАУ</div>
      <div class="card" style="margin-bottom:10px;">
        <label style="font-size:12px; font-weight:600;">Өнім атауы:
          <input id="edit-name" type="text" value="${r.productName || ''}" />
        </label>
        <p style="font-size:12px; color:#475569; margin:8px 0 0;">
          <strong>Өлшем негізі:</strong> ${r.basis === 'PER_SERVING' ? '1 порция үшін' : r.basis === 'PER_100ML' ? '100 мл үшін' : '100 г үшін'} (${r.servingSize || '100 г'})
        </p>
        ${r.ingredients ? `<p style="font-size:12px; color:#475569; margin:6px 0 0;"><strong>Ингредиенттер:</strong> ${r.ingredients}</p>` : ''}
      </div>

      ${fieldCard('1. Қант мөлшері', r.sugar, 'г', 'edit-sugar')}
      ${fieldCard('2. Тұз мөлшері', r.salt, 'г', 'edit-salt')}
      ${fieldCard('3. Май мөлшері', r.fat, 'г', 'edit-fat')}
      ${fieldCard('4. Ақуыз мөлшері', r.protein, 'г', 'edit-protein')}
      ${fieldCard('5. Энергетикалық құндылығы', r.calories, 'ккал', 'edit-calories')}

      <button class="btn-primary full-width" onclick="saveCurrentToLab()">🧪 Растау және Зертханаға сақтау</button>
      <button class="btn-outline full-width" style="margin-top:8px;" onclick="window.FoodScan20?.setSelectedProduct(document.getElementById('edit-name')?.value || ''); navigateTo('unified')">📑 Біріктірілген есепті (A–D) ашу</button>
    ` : ''}

    ${disclaimerHtml()}
  `;
}

function renderComparison() {
  const p1 = state.products.find((p) => p.id === state.compP1Id) || state.products[0];
  const p2 = state.products.find((p) => p.id === state.compP2Id) || state.products[1];
  if (!p1 || !p2) {
    return `<div class="card">Салыстыру үшін кемінде 2 өнім қажет.</div>`;
  }

  const mismatch = p1.basis !== p2.basis;
  const factor = (p) => {
    if (!state.normalize100g || p.basis === 'PER_100G') return 1;
    const g = parseFloat((p.servingSizeText || '').replace(/[^0-9.]/g, '')) || 30;
    return 100 / g;
  };
  const f1 = factor(p1);
  const f2 = factor(p2);
  const fmt = (v, f) => (v != null ? (v * f).toFixed(1) : '—');

  return `
    <div class="section-kicker">САЛЫСТЫРМАЛЫ ТАЛДАУ</div>
    <h2 style="margin:0 0 10px;">Өнімдерді салыстыру</h2>

    <div style="display:grid; grid-template-columns:1fr 1fr; gap:10px; margin-bottom:12px;">
      <label class="card" style="font-size:11px; font-weight:700;">1-ӨНІМ:
        <select onchange="state.compP1Id=Number(this.value); render();">
          ${state.products.map((p) => `<option value="${p.id}" ${p.id === p1.id ? 'selected' : ''}>${p.productName}</option>`).join('')}
        </select>
      </label>
      <label class="card" style="font-size:11px; font-weight:700;">2-ӨНІМ:
        <select onchange="state.compP2Id=Number(this.value); render();">
          ${state.products.map((p) => `<option value="${p.id}" ${p.id === p2.id ? 'selected' : ''}>${p.productName}</option>`).join('')}
        </select>
      </label>
    </div>

    ${mismatch && !state.normalize100g ? `
      <div class="warning-box">
        <strong>ӨЛШЕМ НЕГІЗДЕРІ СӘЙКЕС ЕМЕС!</strong><br/>
        Біреуі 100 г, ал екіншісі порция негізінде есептелген. Төмендегі «100 г негізіне келтіру» түймесін қосыңыз.
      </div>
    ` : ''}

    <div class="card" style="display:flex; justify-content:space-between; align-items:center; margin-bottom:12px;">
      <div>
        <strong style="font-size:13px;">100 грамға бірыңғай келтіру</strong>
        <div style="font-size:11px; color:#64748B;">Екі өнімді бірдей өлшеммен салыстыру</div>
      </div>
      <input type="checkbox" style="width:24px; height:24px;" ${state.normalize100g ? 'checked' : ''} onchange="state.normalize100g=this.checked; render();" />
    </div>

    <div class="card">
      <table class="comp-table">
        <thead>
          <tr><th>Көрсеткіш</th><th>${p1.productName}</th><th>${p2.productName}</th></tr>
        </thead>
        <tbody>
          <tr><td>Негізі</td><td>${state.normalize100g ? '100 г' : p1.servingSizeText}</td><td>${state.normalize100g ? '100 г' : p2.servingSizeText}</td></tr>
          <tr><td>Қант (г)</td><td>${fmt(p1.sugar, f1)}</td><td>${fmt(p2.sugar, f2)}</td></tr>
          <tr><td>Тұз (г)</td><td>${fmt(p1.salt, f1)}</td><td>${fmt(p2.salt, f2)}</td></tr>
          <tr><td>Май (г)</td><td>${fmt(p1.fat, f1)}</td><td>${fmt(p2.fat, f2)}</td></tr>
          <tr><td>Ақуыз (г)</td><td>${fmt(p1.protein, f1)}</td><td>${fmt(p2.protein, f2)}</td></tr>
          <tr><td>Калория (ккал)</td><td>${fmt(p1.calories, f1)}</td><td>${fmt(p2.calories, f2)}</td></tr>
        </tbody>
      </table>
    </div>
    <button class="btn-outline full-width" style="margin-top:12px;" onclick="navigateTo('unified')">📑 Бір өнімді күндер бойынша салыстыру (фото, pH, t°)</button>
    ${disclaimerHtml()}
  `;
}

function renderResearch() {
  const acc = calculateAccuracy().toFixed(1);
  return `
    <div class="section-kicker" style="color:var(--green);">ҒЫЛЫМИ ЗЕРТТЕУ ЗЕРТХАНАСЫ</div>
    <h2 style="margin:0 0 10px;">Зерттеу дерекқоры (${state.products.length} / 30)</h2>

    <div class="hero-card" style="padding:16px;">
      <div style="display:flex; justify-content:space-between;">
        <div><div style="font-size:11px; color:#94A3B8;">ӨНІМДЕР САНЫ</div><strong style="font-size:20px;">${state.products.length} / 30</strong></div>
        <div style="text-align:right;"><div style="font-size:11px; color:#94A3B8;">OCR ДӘЛДІГІ</div><strong style="font-size:20px; color:#10B981;">${acc}%</strong></div>
      </div>
    </div>

    <div style="display:flex; gap:10px; margin-bottom:10px;">
      <button class="btn-primary btn-navy" style="flex:1;" onclick="exportCsvFile()">⬇️ CSV экспорттау</button>
      <button class="btn-outline" style="flex:1;" onclick="navigateTo('scanner')">➕ Жаңа өнім</button>
    </div>
    <button class="btn-outline full-width" style="margin-bottom:14px;" onclick="navigateTo('protocol')">📋 6 парақты хаттама (Нұсқаулық, 8 сұрақ тесті, Қорытынды)</button>

    ${state.products.map((p) => `
      <div class="card" style="margin-bottom:10px;">
        <div style="display:flex; justify-content:space-between; align-items:start;">
          <div>
            <strong>#${p.productNumber} ${p.productName}</strong>
            <div style="font-size:11px; color:#64748B;">${p.category} • ${p.servingSizeText}</div>
          </div>
          <button class="btn-outline" style="min-height:32px; padding:4px 8px; color:#DC2626;" onclick="deleteLabItem(${p.id})">Өшіру</button>
        </div>
        <div style="display:grid; grid-template-columns:repeat(5,1fr); gap:4px; margin-top:8px; font-size:11px; text-align:center; background:#F8FAFC; padding:8px; border-radius:8px;">
          <div>Қант<br/><strong>${p.sugar ?? '—'}г</strong></div>
          <div>Тұз<br/><strong>${p.salt ?? '—'}г</strong></div>
          <div>Май<br/><strong>${p.fat ?? '—'}г</strong></div>
          <div>Ақуыз<br/><strong>${p.protein ?? '—'}г</strong></div>
          <div>Ккал<br/><strong>${p.calories ?? '—'}</strong></div>
        </div>
      </div>
    `).join('')}
    ${disclaimerHtml()}
  `;
}

function renderSurvey() {
  const total = state.surveys.length || 1;
  const barSummary = (title, key, opts) => `
    <div class="card" style="margin-bottom:10px;">
      <strong style="font-size:13px;">${title}</strong>
      ${opts.map((o) => {
        const c = state.surveys.filter((s) => s[key]?.includes(o.slice(0, 4))).length;
        const pct = Math.round((c / total) * 100);
        return `
          <div class="bar-row">
            <span style="width:120px;">${o}</span>
            <div class="bar-track"><div class="bar-fill" style="width:${pct}%; background:var(--green);"></div></div>
            <span style="width:48px; text-align:right;">${c} (${pct}%)</span>
          </div>
        `;
      }).join('')}
    </div>
  `;

  return `
    <div class="section-kicker">ЖАСӨСПІРІМДЕР САУАЛНАМАСЫ</div>
    <h2 style="margin:0 0 10px;">Сауалнама және нәтижелер (${state.surveys.length})</h2>

    <form class="card" onsubmit="submitSurveyForm(event)" style="margin-bottom:14px;">
      <label style="font-size:12px; font-weight:700;">Жасы (12–17):
        <input name="age" type="number" min="12" max="17" value="14" />
      </label>
      <label style="display:block; margin-top:10px; font-size:12px; font-weight:700;">1. Тағам өнімін сатып алғанда құрамын оқисың ба?
        <select name="q1"><option>Иә, үнемі</option><option selected>Кейде</option><option>Жоқ, ешқашан</option></select>
      </label>
      <label style="display:block; margin-top:10px; font-size:12px; font-weight:700;">2. Қант мөлшерін қаптамадан таба аласың ба?
        <select name="q2"><option>Оңай табамын</option><option selected>Қиындықпен</option><option>Таба алмаймын</option></select>
      </label>
      <label style="display:block; margin-top:10px; font-size:12px; font-weight:700;">3. 100 грамм және бір порция айырмашылығын білесің бе?
        <select name="q3"><option>Иә, жақсы білемін</option><option selected>Шамамен білемін</option><option>Білмеймін</option></select>
      </label>
      <label style="display:block; margin-top:10px; font-size:12px; font-weight:700;">4. Екі өнімнің құрамын салыстыра аласың ба?
        <select name="q4"><option>Иә, оңай</option><option selected>Кейде шатасамын</option><option>Жоқ, қиналамын</option></select>
      </label>
      <label style="display:block; margin-top:10px; font-size:12px; font-weight:700;">5. FOODSCAN AI жүйесі ақпаратты түсінуге көмектесті ме?
        <select name="q5"><option selected>Өте пайдалы болды</option><option>Біраз көмектесті</option><option>Әлі толық түсінбедім</option></select>
      </label>
      <button type="submit" class="btn-primary full-width" style="margin-top:12px;">Жауапты сақтау</button>
    </form>

    ${barSummary('1. Құрамды оқу жиілігі', 'q1', ['Иә, үнемі', 'Кейде', 'Жоқ, ешқашан'])}
    ${barSummary('5. FOODSCAN AI тиімділігі', 'q5', ['Өте пайдалы болды', 'Біраз көмектесті', 'Әлі толық түсінбедім'])}
    ${disclaimerHtml()}
  `;
}

function renderEducation() {
  return `
    <div class="section-kicker">ЖАСӨСПІРІМДЕРГЕ АРНАЛҒАН БІЛІМ БАЗАСЫ</div>
    <h2 style="margin:0 0 10px;">Тағам туралы білім</h2>

    ${EDU_TOPICS.map((t, idx) => `
      <div class="card" style="margin-bottom:10px; cursor:pointer;" onclick="state.eduOpenIdx=(state.eduOpenIdx===${idx}?-1:${idx}); render();">
        <div style="display:flex; justify-content:space-between; align-items:center;">
          <div>
            <strong>${t.title}</strong>
            <div style="font-size:11px; color:#64748B;">${t.sub}</div>
          </div>
          <span>${state.eduOpenIdx === idx ? '▲' : '▼'}</span>
        </div>
        ${state.eduOpenIdx === idx ? `
          <p style="font-size:13px; color:#334155; margin:10px 0 6px;">${t.body}</p>
          <div style="background:#D1FAE5; color:#065F46; padding:8px 10px; border-radius:8px; font-size:12px; font-weight:700;">✓ ${t.tip}</div>
        ` : ''}
      </div>
    `).join('')}
    ${disclaimerHtml()}
  `;
}

function renderAbout() {
  return `
    <section class="hero-card">
      <span class="hero-tag">8-СЫНЫП ҒЫЛЫМИ ЖОБАСЫ</span>
      <h1>«Жасөспірімдерге арналған тағам өнімдерінің құрамын цифрлық талдау жүйесін әзірлеу және оның ақпараттық тиімділігін зерттеу»</h1>
      <p>Автор: Қанағат Айым, 8 «А» сынып оқушысы</p>
    </section>
    <div class="card" style="margin-bottom:10px;">
      <strong>Android және iPhone (iOS Safari) PWA үйлесімділігі</strong>
      <p style="font-size:13px; color:#475569; margin:6px 0 0;">Қосымша Android Chrome және iPhone Safari браузерлерінде, сондай-ақ iPad пен компьютер экрандарында толық бейімделіп жұмыс істейді және басты экранға орнатылады.</p>
    </div>
    ${disclaimerHtml()}
  `;
}

function calcNutrientAccuracy(key, extKey) {
  const checked = state.products.filter((p) => p.status !== 'UNVERIFIED' && p[key] != null && p[extKey] != null);
  if (!checked.length) return 100;
  const matches = checked.filter((p) => Math.abs(p[key] - p[extKey]) <= 0.15).length;
  return (matches / checked.length) * 100;
}

window.submitEightQuiz = function (e) {
  e.preventDefault();
  const f = e.target;
  const answers = [];
  for (let i = 0; i < EIGHT_QUESTIONS.length; i++) {
    const val = f[`q_${i}`]?.value;
    if (val === undefined || val === '') {
      alert('Барлық 8 тапсырмаға жауап таңдаңыз.');
      return;
    }
    answers.push(Number(val));
  }
  const letters = ['A', 'B', 'C', 'D'];
  const score = EIGHT_QUESTIONS.reduce((acc, q, i) => acc + (answers[i] === q.ans ? 1 : 0), 0);
  state.testAttempts.unshift({
    id: Date.now(),
    studentName: (f.studentName.value || '').trim() || 'Оқушы #1',
    age: Number(f.age.value) || 14,
    grade: (f.grade.value || '').trim() || '8 «А»',
    phase: f.phase.value,
    answersCsv: answers.map((idx) => letters[idx] || '—').join(','),
    score,
    total: EIGHT_QUESTIONS.length
  });
  saveStorage('foodscan_test_attempts', state.testAttempts);
  alert(`Тест сақталды! Нәтиже: ${score} / 8`);
  state.protocolSheet = 2;
  render();
};

window.deleteTestAttempt = function (id) {
  state.testAttempts = state.testAttempts.filter((a) => a.id !== id);
  saveStorage('foodscan_test_attempts', state.testAttempts);
  render();
};

window.exportProtocolSummaryCsv = function () {
  const pre = state.testAttempts.filter((a) => a.phase === 'PRE');
  const post = state.testAttempts.filter((a) => a.phase === 'POST');
  const avgPre = pre.length ? (pre.reduce((s, a) => s + a.score, 0) / pre.length).toFixed(2) : '0';
  const avgPost = post.length ? (post.reduce((s, a) => s + a.score, 0) / post.length).toFixed(2) : '0';

  let csv = '\uFEFFFOODSCAN AI — 6 парақты хаттама қорытындысы\n';
  csv += `Жалпы OCR дәлдігі (%);${calculateAccuracy().toFixed(1)}\n`;
  csv += `Қант дәлдігі (%);${calcNutrientAccuracy('sugar', 'extractedSugar').toFixed(1)}\n`;
  csv += `Тұз дәлдігі (%);${calcNutrientAccuracy('salt', 'extractedSalt').toFixed(1)}\n`;
  csv += `Май дәлдігі (%);${calcNutrientAccuracy('fat', 'extractedFat').toFixed(1)}\n`;
  csv += `Ақуыз дәлдігі (%);${calcNutrientAccuracy('protein', 'extractedProtein').toFixed(1)}\n`;
  csv += `Калория дәлдігі (%);${calcNutrientAccuracy('calories', 'extractedCalories').toFixed(1)}\n`;
  csv += `Pre-test саны;${pre.length};Орташа балл;${avgPre}\n`;
  csv += `Post-test саны;${post.length};Орташа балл;${avgPost}\n`;

  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = 'foodscan_protocol_summary.csv';
  a.click();
  URL.revokeObjectURL(url);
};

function renderProtocol() {
  const sheetNames = ['1. Нұсқаулық', '2. 30 өнім', '3. Оқушы тесті', '4. Тест сұрақтары', '5. Жауап кілті', '6. Қорытынды'];
  const tabsHtml = `
    <div class="protocol-tabs">
      ${sheetNames.map((t, i) => `<button class="protocol-tab ${state.protocolSheet === i ? 'active' : ''}" onclick="state.protocolSheet=${i}; render();">${t}</button>`).join('')}
    </div>
  `;

  let bodyHtml = '';
  if (state.protocolSheet === 0) {
    bodyHtml = PROTOCOL_INSTRUCTIONS.map((s) => `
      <div class="card" style="margin-bottom:10px;">
        <div class="section-kicker" style="margin-top:0; color:var(--green);">${s.sheet}</div>
        <strong>${s.step}. ${s.title}</strong>
        <p style="font-size:12px; color:#475569; margin:6px 0 0;">${s.desc}</p>
      </div>
    `).join('');
  } else if (state.protocolSheet === 1) {
    bodyHtml = state.products.map((p) => `
      <div class="card" style="margin-bottom:10px;">
        <strong>#${p.productNumber} ${p.productName}</strong>
        <div style="font-size:11px; color:#64748B; margin-bottom:6px;">${p.category} • ${p.servingSizeText}</div>
        <table class="comp-table">
          <thead><tr><th>Көрсеткіш</th><th>Қаптама</th><th>AI оқуы</th></tr></thead>
          <tbody>
            <tr><td>Қант (г)</td><td>${p.sugar ?? '—'}</td><td>${p.extractedSugar ?? '—'}</td></tr>
            <tr><td>Тұз (г)</td><td>${p.salt ?? '—'}</td><td>${p.extractedSalt ?? '—'}</td></tr>
            <tr><td>Май (г)</td><td>${p.fat ?? '—'}</td><td>${p.extractedFat ?? '—'}</td></tr>
            <tr><td>Ақуыз (г)</td><td>${p.protein ?? '—'}</td><td>${p.extractedProtein ?? '—'}</td></tr>
            <tr><td>Ккал</td><td>${p.calories ?? '—'}</td><td>${p.extractedCalories ?? '—'}</td></tr>
          </tbody>
        </table>
      </div>
    `).join('');
  } else if (state.protocolSheet === 2) {
    bodyHtml = `
      <button class="btn-primary full-width" style="margin-bottom:12px;" onclick="state.protocolSheet=3; render();">📝 Жаңа Pre / Post тест тапсыру</button>
      ${state.testAttempts.length === 0
        ? `<div class="card"><strong>Әзірге тапсырылған Pre/Post тест нәтижелері жоқ.</strong><p style="font-size:12px; color:#64748B; margin:6px 0 0;">«4. Тест сұрақтары» парағында дейінгі және кейінгі жауаптарды енгізіңіз.</p></div>`
        : state.testAttempts.map((a) => `
          <div class="card" style="margin-bottom:10px;">
            <div style="display:flex; justify-content:space-between; align-items:center;">
              <div>
                <strong>${a.studentName}</strong> (${a.grade}, ${a.age} жас)
                <div style="font-size:12px; color:${a.phase === 'POST' ? '#047857' : '#B45309'}; font-weight:700;">
                  ${a.phase === 'POST' ? 'Кейін (Post-test)' : 'Дейін (Pre-test)'}: ${a.score} / ${a.total} (${Math.round((a.score / a.total) * 100)}%)
                </div>
                <div style="font-size:11px; color:#64748B;">Жауаптар: ${a.answersCsv}</div>
              </div>
              <button class="btn-outline" style="min-height:36px; padding:4px 10px; color:#DC2626;" onclick="deleteTestAttempt(${a.id})">Жою</button>
            </div>
          </div>
        `).join('')}
    `;
  } else if (state.protocolSheet === 3) {
    bodyHtml = `
      <form onsubmit="submitEightQuiz(event)">
        <div class="card" style="margin-bottom:12px;">
          <label style="font-size:12px; font-weight:700;">Оқушы аты-жөні / коды:
            <input name="studentName" type="text" placeholder="Мысалы: Айым" />
          </label>
          <div style="display:grid; grid-template-columns:1fr 1fr; gap:8px; margin-top:8px;">
            <label style="font-size:12px; font-weight:700;">Сыныбы:<input name="grade" type="text" value="8 «А»" /></label>
            <label style="font-size:12px; font-weight:700;">Жасы:<input name="age" type="number" min="10" max="18" value="14" /></label>
          </div>
          <label style="display:block; margin-top:8px; font-size:12px; font-weight:700;">Кезең:
            <select name="phase">
              <option value="PRE">Пайдалануға дейін (Pre-test)</option>
              <option value="POST">Пайдаланудан кейін (Post-test)</option>
            </select>
          </label>
        </div>
        ${EIGHT_QUESTIONS.map((q, i) => `
          <div class="card" style="margin-bottom:10px;">
            <div class="section-kicker" style="margin-top:0;">${q.n}-тапсырма • ${q.topic}</div>
            <strong style="font-size:13px;">${q.q}</strong>
            <div style="margin-top:8px; display:flex; flex-direction:column; gap:6px;">
              ${q.opts.map((opt, oi) => `
                <label style="display:flex; align-items:center; gap:8px; font-size:13px; cursor:pointer;">
                  <input type="radio" name="q_${i}" value="${oi}" style="width:18px; height:18px; margin:0;" />
                  <span>${opt}</span>
                </label>
              `).join('')}
            </div>
          </div>
        `).join('')}
        <button type="submit" class="btn-primary full-width">✓ Тестті аяқтау және сақтау</button>
      </form>
    `;
  } else if (state.protocolSheet === 4) {
    bodyHtml = EIGHT_QUESTIONS.map((q) => `
      <div class="card" style="margin-bottom:10px;">
        <strong>${q.n}-тапсырма • ${q.topic}</strong>
        <p style="font-size:12px; color:#334155; margin:4px 0 8px;">${q.q}</p>
        <div style="background:#D1FAE5; color:#065F46; padding:8px 10px; border-radius:8px; font-size:12px; font-weight:700;">Дұрыс жауап: ${q.opts[q.ans]}</div>
        <p style="font-size:12px; color:#475569; margin:8px 0 0;"><strong>Түсіндірме:</strong> ${q.exp}</p>
      </div>
    `).join('');
  } else {
    const pre = state.testAttempts.filter((a) => a.phase === 'PRE');
    const post = state.testAttempts.filter((a) => a.phase === 'POST');
    const avgPrePct = pre.length ? (pre.reduce((s, a) => s + a.score, 0) / (pre.length * 8)) * 100 : 0;
    const avgPostPct = post.length ? (post.reduce((s, a) => s + a.score, 0) / (post.length * 8)) * 100 : 0;
    const gain = pre.length && post.length ? (avgPostPct - avgPrePct).toFixed(1) : '—';
    bodyHtml = `
      <div class="hero-card">
        <span class="hero-tag">А. OCR ДӘЛДІГІ (${state.products.length} / 30 ӨНІМ)</span>
        <h1>${calculateAccuracy().toFixed(1)}%</h1>
        <p>Қант: ${calcNutrientAccuracy('sugar', 'extractedSugar').toFixed(1)}% • Тұз: ${calcNutrientAccuracy('salt', 'extractedSalt').toFixed(1)}% • Май: ${calcNutrientAccuracy('fat', 'extractedFat').toFixed(1)}% • Ақуыз: ${calcNutrientAccuracy('protein', 'extractedProtein').toFixed(1)}% • Ккал: ${calcNutrientAccuracy('calories', 'extractedCalories').toFixed(1)}%</p>
      </div>
      <div class="stats-grid">
        <div class="stat-card"><div class="stat-val">${pre.length ? avgPrePct.toFixed(0) + '%' : '—'}</div><div class="stat-title">Дейін (${pre.length})</div></div>
        <div class="stat-card"><div class="stat-val">${post.length ? avgPostPct.toFixed(0) + '%' : '—'}</div><div class="stat-title">Кейін (${post.length})</div></div>
        <div class="stat-card"><div class="stat-val">${gain === '—' ? '—' : (Number(gain) >= 0 ? '+' + gain : gain) + '%'}</div><div class="stat-title">Өсім (Δ)</div></div>
      </div>
      <button class="btn-primary full-width" onclick="exportProtocolSummaryCsv()">⬇️ Қорытындыны CSV экспорттау</button>
    `;
  }

  return `
    <div class="section-kicker">ҒЫЛЫМИ ЗЕРТТЕУ ХАТТАМАСЫ (6 ПАРАҚ)</div>
    <h2 style="margin:0 0 10px;">Деректер жинау, тест және қорытынды</h2>
    ${tabsHtml}
    ${bodyHtml}
    ${disclaimerHtml()}
  `;
}

function render() {
  const container = document.getElementById('app-content');
  if (!container) return;
  const views = {
    dashboard: renderDashboard,
    scanner: renderScanner,
    freshness: () => window.FoodScan20?.renderFreshnessVisionView() || '',
    chemical: () => window.FoodScan20?.renderChemicalPhView() || '',
    storage: () => window.FoodScan20?.renderStorageMonitorView() || '',
    unified: () => window.FoodScan20?.renderUnifiedReportView() || '',
    comparison: renderComparison,
    research: renderResearch,
    protocol: renderProtocol,
    survey: renderSurvey,
    education: renderEducation,
    about: renderAbout
  };
  container.innerHTML = (views[state.screen] || renderDashboard)();
}
window.render = render;
