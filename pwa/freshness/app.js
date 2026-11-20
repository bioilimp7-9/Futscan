/* FreshGuard iOS — Тағам балғындығын және сақтау жағдайын бақылайтын iPhone қосымшасы
 * Физика (температура, жарық, RGB) + Химия (табиғи индикатор pH) + Биология (микроағзалар) + Цифрлық талдау
 */

const INDICATORS = {
  anthocyanin: {
    id: 'anthocyanin',
    name: 'Қызыл қырыққабат (Антоциан)',
    desc: 'Қышқылда қызыл/алқызыл (pH 2–4), бейтарапта күлгін (pH 6–7), сілтіліде көк-жасыл (pH 8–10).',
    mapHueToPh: (h) => {
      // Anthocyanin RGB Hue mapping: Red/Pink (330..360, 0..25) -> pH 2.5..4.5;
      // Purple/Violet (260..329) -> pH 5.5..7.0; Blue (195..259) -> pH 7.5..8.5; Green/Yellow (60..194) -> pH 9.0..11.5
      if (h >= 330 || h <= 25) return 3.2;
      if (h >= 260) return 6.4;
      if (h >= 195) return 7.9;
      if (h >= 90) return 9.6;
      return 11.0;
    },
    scale: [
      { label: 'pH 2–3 (Қышқыл)', hex: '#E11D48', rgb: [225, 29, 72] },
      { label: 'pH 4–5 (Әлсіз қышқыл)', hex: '#EC4899', rgb: [236, 72, 153] },
      { label: 'pH 6–7 (Бейтарап)', hex: '#7C3AED', rgb: [124, 58, 237] },
      { label: 'pH 8 (Сілтілі)', hex: '#0284C7', rgb: [2, 132, 199] },
      { label: 'pH 9–10 (Күшті сілтілі)', hex: '#10B981', rgb: [16, 185, 129] }
    ]
  },
  curcumin: {
    id: 'curcumin',
    name: 'Куркумин (Зімбір/Куркума)',
    desc: 'Қышқыл және бейтарап ортада сары (pH ≤ 7), сілтілі ортада қызыл-қоңыр (pH > 8).',
    mapHueToPh: (h) => {
      if (h >= 40 && h <= 72) return 6.2;
      if (h >= 20 && h < 40) return 8.2;
      if (h < 20 || h >= 335) return 9.4;
      return 7.0;
    },
    scale: [
      { label: 'pH 5–7 (Сары — Қалыпты)', hex: '#EAB308', rgb: [234, 179, 8] },
      { label: 'pH 8 (Қызғылт сары)', hex: '#F97316', rgb: [249, 115, 22] },
      { label: 'pH 9+ (Қызыл-қоңыр — Сілті)', hex: '#B91C1C', rgb: [185, 28, 28] }
    ]
  },
  betalain: {
    id: 'betalain',
    name: 'Қызылша сығындысы (Беталаин)',
    desc: 'Бейтарап/әлсіз қышқыл ортада қанық күлгін-қызыл (pH 4–7), жоғары сілтіліде сарғыш-қоңыр (pH > 9).',
    mapHueToPh: (h) => {
      if (h >= 300 || h <= 15) return 6.2;
      if (h >= 20 && h <= 65) return 9.5;
      return 7.4;
    },
    scale: [
      { label: 'pH 4–7 (Қызыл-күлгін)', hex: '#BE185D', rgb: [190, 24, 93] },
      { label: 'pH 8 (Қоңырқай)', hex: '#9A3412', rgb: [154, 52, 18] },
      { label: 'pH 9.5+ (Сары-қоңыр)', hex: '#CA8A04', rgb: [202, 138, 4] }
    ]
  }
};

const PRODUCT_PROFILES = {
  milk: { id: 'milk', name: 'Сүт және сүт өнімдері', idealTempMax: 6, idealPhMin: 6.4, idealPhMax: 6.8, spoilageDirection: 'acid' },
  meat: { id: 'meat', name: 'Ет және құс өнімдері', idealTempMax: 4, idealPhMin: 5.5, idealPhMax: 6.4, spoilageDirection: 'alkaline' },
  fish: { id: 'fish', name: 'Балық және теңіз өнімдері', idealTempMax: 2, idealPhMin: 6.0, idealPhMax: 6.8, spoilageDirection: 'alkaline' },
  juice: { id: 'juice', name: 'Жеміс-жидек / Шырын', idealTempMax: 8, idealPhMin: 3.3, idealPhMax: 4.5, spoilageDirection: 'ferment' }
};

// Safety-labeled educational scenarios (used ONLY if user activates Simulation Mode explicitly)
const EDUCATIONAL_SCENARIOS = [
  {
    id: 'fresh_milk',
    label: 'Балғын сүт (+4 °C, қараңғы)',
    productType: 'milk',
    indicatorId: 'anthocyanin',
    tempC: 4.0,
    lightLux: 18,
    rgb: [124, 58, 237] // Violet ~ pH 6.4
  },
  {
    id: 'spoiled_milk',
    label: 'Ашыған сүт (+23 °C, жарық)',
    productType: 'milk',
    indicatorId: 'anthocyanin',
    tempC: 23.0,
    lightLux: 420,
    rgb: [225, 29, 72] // Pink-red ~ pH 3.2 (lactic acid bacteria)
  },
  {
    id: 'spoiled_fish',
    label: 'Бұзыла бастаған балық (+16 °C)',
    productType: 'fish',
    indicatorId: 'anthocyanin',
    tempC: 16.5,
    lightLux: 190,
    rgb: [16, 185, 129] // Greenish ~ pH 9.6 (volatile amines)
  }
];

const state = {
  tab: 'monitor',
  productType: 'milk',
  indicatorId: 'anthocyanin',
  // Actual device/camera sensor measurements (null until measured or entered)
  tempC: null,
  lightLux: null,
  rgb: null, // [r, g, b]
  measuredSource: 'Өлшенбеген (Құрылғы/Камера күтілуде)',
  isSimulationMode: false,
  cameraStream: null,
  capturedPhotoUrl: null,
  isProcessing: false,
  bleStatus: 'Қосылмаған',
  logs: loadStorage('freshguard_ios_logs', []),
  isOffline: !navigator.onLine
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
    // ignore storage errors
  }
}

// Service Worker & Online/Offline Tracking
if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('./service-worker.js', { scope: './' }).catch(() => {});
  });
}

function syncNetworkBanner() {
  state.isOffline = !navigator.onLine;
  document.getElementById('offline-banner')?.classList.toggle('hidden', !state.isOffline);
}

window.addEventListener('online', () => { syncNetworkBanner(); render(); });
window.addEventListener('offline', () => { syncNetworkBanner(); render(); });
window.addEventListener('DOMContentLoaded', () => { syncNetworkBanner(); render(); });

window.openInstallSheet = () => document.getElementById('ios-sheet-backdrop')?.classList.remove('hidden');
window.closeInstallSheet = () => document.getElementById('ios-sheet-backdrop')?.classList.add('hidden');

window.setTab = function (tab) {
  if (state.tab === 'indicator' && tab !== 'indicator') {
    stopLiveCamera();
  }
  state.tab = tab;
  document.querySelectorAll('.tab-item').forEach((btn) => {
    btn.classList.toggle('active', btn.dataset.tab === tab);
  });
  window.scrollTo({ top: 0, behavior: 'smooth' });
  render();
};

// Physics/Math: RGB -> HSV (Hue in 0..360)
function rgbToHsv(r, g, b) {
  const rn = r / 255;
  const gn = g / 255;
  const bn = b / 255;
  const max = Math.max(rn, gn, bn);
  const min = Math.min(rn, gn, bn);
  const d = max - min;
  let h = 0;
  if (d !== 0) {
    if (max === rn) h = ((gn - bn) / d) % 6;
    else if (max === gn) h = (bn - rn) / d + 2;
    else h = (rn - gn) / d + 4;
    h = Math.round(h * 60);
    if (h < 0) h += 360;
  }
  const s = max === 0 ? 0 : Math.round((d / max) * 100);
  const v = Math.round(max * 100);
  return { h, s, v };
}

function rgbToHex([r, g, b]) {
  return '#' + [r, g, b].map((x) => Math.max(0, Math.min(255, Math.round(x))).toString(16).padStart(2, '0')).join('').toUpperCase();
}

// Combined 4-Pillar Scientific Analysis (Physics + Chemistry + Biology -> Digital Freshness Score)
function analyzeState() {
  const profile = PRODUCT_PROFILES[state.productType];
  const indicator = INDICATORS[state.indicatorId];

  const hasTemp = typeof state.tempC === 'number' && !Number.isNaN(state.tempC);
  const hasLux = typeof state.lightLux === 'number' && !Number.isNaN(state.lightLux);
  const hasRgb = Array.isArray(state.rgb) && state.rgb.length === 3;

  let hsv = null;
  let ph = null;
  if (hasRgb) {
    hsv = rgbToHsv(state.rgb[0], state.rgb[1], state.rgb[2]);
    ph = indicator.mapHueToPh(hsv.h);
  }

  if (!hasTemp && !hasLux && !hasRgb) {
    return {
      ready: false,
      freshnessScore: null,
      statusLabel: 'Деректер енгізілмеген',
      badgeClass: 'badge-neutral',
      hsv: null,
      ph: null,
      bioRisk: 'Талдау үшін датчик мәндерін енгізіңіз немесе iPhone камерасымен индикатор түсін оқыңыз.'
    };
  }

  let score = 100;
  const reasons = [];

  // 1. Physics: Temperature
  if (hasTemp) {
    if (state.tempC > profile.idealTempMax) {
      const excess = state.tempC - profile.idealTempMax;
      score -= Math.min(45, excess * 3.2);
      reasons.push(`Температура (+${state.tempC.toFixed(1)} °C) шекті нормадан (+${profile.idealTempMax} °C) жоғары — бактериялардың көбею жылдамдығы (Q10) артады.`);
    } else if (state.tempC < 0) {
      score -= 8;
      reasons.push('Мұздату температурасы (< 0 °C) микроағзаларды тежейді, бірақ құрылымға әсер етуі мүмкін.');
    }
  }

  // 2. Physics: Light Exposure
  if (hasLux && state.lightLux > 100) {
    const lightPenalty = Math.min(20, (state.lightLux - 100) / 25);
    score -= lightPenalty;
    reasons.push(`Жарық деңгейі (${state.lightLux} лк) жоғары — фотототығу мен дәрумендердің ыдырау қаупі бар.`);
  }

  // 3. Chemistry & Biology: Natural Indicator pH shift
  if (ph !== null) {
    if (ph < profile.idealPhMin) {
      const diff = profile.idealPhMin - ph;
      score -= Math.min(50, diff * 18);
      reasons.push(`Табиғи индикатор қышқылдануды көрсетті (pH ≈ ${ph.toFixed(1)}): сүтқышқылды бактериялар (Lactobacillus) лактозаны қышқылға айналдыруда.`);
    } else if (ph > profile.idealPhMax) {
      const diff = ph - profile.idealPhMax;
      score -= Math.min(50, diff * 18);
      reasons.push(`Табиғи индикатор сілтіленуді көрсетті (pH ≈ ${ph.toFixed(1)}): шірітуші бактериялар (Pseudomonas) ақуызды ыдыратып, ұшқыш аминдер (NH3) бөлуде.`);
    }
  }

  score = Math.max(0, Math.min(100, Math.round(score)));
  let statusLabel = 'Балғын • Сақтау жағдайы қалыпты';
  let badgeClass = 'badge-fresh';
  if (score < 45) {
    statusLabel = 'Бұзылу қаупі жоғары!';
    badgeClass = 'badge-danger';
  } else if (score < 75) {
    statusLabel = 'Бақылау қажет • Ауытқу байқалды';
    badgeClass = 'badge-warn';
  }

  return {
    ready: true,
    freshnessScore: score,
    statusLabel,
    badgeClass,
    hsv,
    ph,
    bioRisk: reasons.length ? reasons.join(' ') : 'Температура, жарық және қышқылдық-негіздік тепе-теңдік микроағзалар өсуін тежейтін оңтайлы деңгейде.'
  };
}

// Hardware / Manual Sensor Submission
window.applyManualSensors = function (e) {
  e.preventDefault();
  const f = e.target;
  const t = parseFloat(f.temp.value);
  const l = parseFloat(f.lux.value);
  const hex = f.colorHex.value;
  if (Number.isNaN(t) && Number.isNaN(l)) {
    alert('Кемінде температура немесе жарық мәнін енгізіңіз.');
    return;
  }
  state.isSimulationMode = false;
  if (!Number.isNaN(t)) state.tempC = t;
  if (!Number.isNaN(l)) state.lightLux = l;
  if (hex && /^#[0-9A-Fa-f]{6}$/.test(hex)) {
    state.rgb = [
      parseInt(hex.slice(1, 3), 16),
      parseInt(hex.slice(3, 5), 16),
      parseInt(hex.slice(5, 7), 16)
    ];
  }
  state.measuredSource = 'Қолмен / Құрылғыдан енгізілген дерек';
  render();
};

// JSON Packet Import from ESP32 / Arduino Hardware
window.importSensorJson = function () {
  const raw = document.getElementById('json-packet-input')?.value?.trim();
  if (!raw) return;
  try {
    const obj = JSON.parse(raw);
    state.isSimulationMode = false;
    if (typeof obj.temp === 'number') state.tempC = obj.temp;
    if (typeof obj.lux === 'number') state.lightLux = obj.lux;
    if (typeof obj.r === 'number' && typeof obj.g === 'number' && typeof obj.b === 'number') {
      state.rgb = [obj.r, obj.g, obj.b];
    }
    state.measuredSource = 'Цифрлық датчик JSON пакеті';
    render();
  } catch {
    alert('JSON форматы дұрыс емес. Мысалы: {"temp":4.2,"lux":20,"r":124,"g":58,"b":237}');
  }
};

// Web Bluetooth (supported on iOS Bluefy/WebBLE & Chrome; shows clear message on standard iOS Safari)
window.connectBleDevice = async function () {
  if (!navigator.bluetooth) {
    alert('Стандартты iPhone Safari Web Bluetooth API-ды тікелей шектейді. iPhone-да Native SwiftUI (CoreBluetooth) немесе JSON/Камера енгізуін қолданыңыз.');
    return;
  }
  try {
    state.bleStatus = 'Ізделуде...';
    render();
    const device = await navigator.bluetooth.requestDevice({ acceptAllDevices: true });
    state.bleStatus = `Қосылды: ${device.name || 'Датчик'}`;
    render();
  } catch {
    state.bleStatus = 'Қосылу тоқтатылды';
    render();
  }
};

window.loadEducationalScenario = function (id) {
  const sc = EDUCATIONAL_SCENARIOS.find((s) => s.id === id);
  if (!sc) return;
  state.isSimulationMode = true;
  state.productType = sc.productType;
  state.indicatorId = sc.indicatorId;
  state.tempC = sc.tempC;
  state.lightLux = sc.lightLux;
  state.rgb = [...sc.rgb];
  state.measuredSource = `ҚАУІПСІЗ ОҚУ СИМУЛЯЦИЯСЫ (${sc.label})`;
  render();
};

window.clearMeasurements = function () {
  state.isSimulationMode = false;
  state.tempC = null;
  state.lightLux = null;
  state.rgb = null;
  state.capturedPhotoUrl = null;
  state.measuredSource = 'Өлшенбеген (Құрылғы/Камера күтілуде)';
  render();
};

// iPhone Live Camera & Photo RGB Pixel Sampling
window.startLiveCamera = async function () {
  if (!navigator.mediaDevices?.getUserMedia) {
    alert('Live камера қолжетімсіз. Төмендегі «Фото түсіру / Жүктеу» түймесін пайдаланыңыз.');
    return;
  }
  try {
    stopLiveCamera();
    const stream = await navigator.mediaDevices.getUserMedia({
      video: { facingMode: { ideal: 'environment' } },
      audio: false
    });
    state.cameraStream = stream;
    state.capturedPhotoUrl = null;
    render();
    const video = document.getElementById('live-cam-video');
    if (video) {
      video.srcObject = stream;
      await video.play();
    }
  } catch {
    alert('Камераға рұқсат берілмеді немесе HTTPS қажет. Төмендегі «iPhone Камера / Фото» батырмасын қолданыңыз.');
  }
};

function stopLiveCamera() {
  if (state.cameraStream) {
    state.cameraStream.getTracks().forEach((t) => t.stop());
    state.cameraStream = null;
  }
}

window.sampleCenterPixelFromVideo = function () {
  const video = document.getElementById('live-cam-video');
  if (!video || !video.videoWidth) {
    alert('Алдымен камера бейнесі іске қосылуын күтіңіз.');
    return;
  }
  state.isProcessing = true;
  document.querySelector('.camera-frame .reticle')?.classList.add('is-processing');
  const canvas = document.createElement('canvas');
  canvas.width = video.videoWidth;
  canvas.height = video.videoHeight;
  const ctx = canvas.getContext('2d');
  ctx.drawImage(video, 0, 0);
  setTimeout(() => {
    state.isProcessing = false;
    extractCenterAverageRgb(ctx, canvas.width, canvas.height, 'iPhone Live Камера');
  }, 450);
};

window.handleIndicatorPhoto = function (input) {
  const file = input.files?.[0];
  if (!file) return;
  stopLiveCamera();
  state.isProcessing = true;
  render();
  const reader = new FileReader();
  reader.onload = (ev) => {
    const img = new Image();
    img.onload = () => {
      state.capturedPhotoUrl = ev.target.result;
      render();
      const canvas = document.createElement('canvas');
      const w = Math.min(640, img.width);
      const h = Math.round(img.height * (w / img.width));
      canvas.width = w;
      canvas.height = h;
      const ctx = canvas.getContext('2d');
      ctx.drawImage(img, 0, 0, w, h);
      setTimeout(() => {
        state.isProcessing = false;
        extractCenterAverageRgb(ctx, w, h, 'iPhone Фотосурет талдауы');
      }, 450);
    };
    img.onerror = () => {
      state.isProcessing = false;
      render();
    };
    img.src = ev.target.result;
  };
  reader.onerror = () => {
    state.isProcessing = false;
    render();
  };
  reader.readAsDataURL(file);
};

function extractCenterAverageRgb(ctx, w, h, sourceLabel) {
  const box = Math.max(12, Math.round(Math.min(w, h) * 0.12));
  const sx = Math.floor(w / 2 - box / 2);
  const sy = Math.floor(h / 2 - box / 2);
  const data = ctx.getImageData(sx, sy, box, box).data;
  let r = 0, g = 0, b = 0, count = 0;
  for (let i = 0; i < data.length; i += 4) {
    r += data[i];
    g += data[i + 1];
    b += data[i + 2];
    count++;
  }
  state.isSimulationMode = false;
  state.rgb = [Math.round(r / count), Math.round(g / count), Math.round(b / count)];
  state.measuredSource = sourceLabel;
  render();
}

window.pickCalibratedSwatch = function (r, g, b, label) {
  state.isSimulationMode = false;
  state.rgb = [r, g, b];
  state.measuredSource = `Калибрленген үлгі (${label})`;
  render();
};

window.saveCurrentLog = function () {
  const analysis = analyzeState();
  if (!analysis.ready) {
    alert('Сақтау алдында датчик немесе индикатор өлшемін алыңыз.');
    return;
  }
  const entry = {
    id: Date.now(),
    time: new Date().toLocaleString('kk-KZ'),
    productName: PRODUCT_PROFILES[state.productType].name,
    indicatorName: INDICATORS[state.indicatorId].name,
    tempC: state.tempC,
    lightLux: state.lightLux,
    hex: state.rgb ? rgbToHex(state.rgb) : null,
    ph: analysis.ph,
    score: analysis.freshnessScore,
    status: analysis.statusLabel,
    isSimulation: state.isSimulationMode
  };
  state.logs.unshift(entry);
  saveStorage('freshguard_ios_logs', state.logs);
  alert('Өлшем нәтижесі журналға сақталды!');
  setTab('history');
};

window.exportLogsCsv = function () {
  let csv = '\uFEFFУақыт;Өнім;Индикатор;Температура (°C);Жарық (лк);Түс HEX;pH;Балғындық (%);Күйі;Режим\n';
  state.logs.forEach((l) => {
    csv += `"${l.time}";"${l.productName}";"${l.indicatorName}";${l.tempC ?? ''};${l.lightLux ?? ''};"${l.hex ?? ''}";${l.ph ? l.ph.toFixed(1) : ''};${l.score};"${l.status}";"${l.isSimulation ? 'Симуляция' : 'Нақты өлшем'}"\n`;
  });
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = 'freshguard_ios_log.csv';
  a.click();
  URL.revokeObjectURL(url);
};

window.deleteLogItem = function (id) {
  state.logs = state.logs.filter((l) => l.id !== id);
  saveStorage('freshguard_ios_logs', state.logs);
  render();
};

function renderMonitor() {
  const analysis = analyzeState();
  const profile = PRODUCT_PROFILES[state.productType];
  const indicator = INDICATORS[state.indicatorId];
  const hex = state.rgb ? rgbToHex(state.rgb) : '#7C3AED';

  return `
    <h1 class="large-title">Монитор</h1>
    <p style="margin:0 0 12px; font-size:13px; color:var(--ios-sub);">Табиғи индикатор + Физика/Химия/Биология цифрлық талдауы</p>

    ${state.isSimulationMode ? `
      <div class="sim-banner">
        ⚠️ <strong>ҚАУІПСІЗ ОҚУ СИМУЛЯЦИЯСЫ РЕЖИМІ:</strong> Қазір оқу-зерттеу үлгі деректері көрсетілуде. Нақты өлшеу үшін камераны немесе датчик енгізуін қолданыңыз.
      </div>
    ` : ''}

    <div class="hero-gradient">
      <div style="display:flex; justify-content:space-between; align-items:flex-start; gap:10px;">
        <div>
          <div style="font-size:11px; color:#A7F3D0; font-weight:700;">БАЛҒЫНДЫҚ ИНДЕКСІ</div>
          <div style="font-size:38px; font-weight:800; line-height:1.1; margin:4px 0;">
            ${analysis.ready ? `${analysis.freshnessScore}%` : '—'}
          </div>
        </div>
        <span class="badge ${analysis.badgeClass}">${analysis.statusLabel}</span>
      </div>
      <div style="font-size:12px; color:#CBD5E1; margin-top:6px;">
        <strong>Өнім:</strong> ${profile.name}<br/>
        <strong>Индикатор:</strong> ${indicator.name}<br/>
        <strong>Дереккөз:</strong> ${state.measuredSource}
      </div>
    </div>

    <div class="grid-3">
      <div class="metric-box">
        <div class="metric-title">🌡️ ТЕМПЕРАТУРА</div>
        <div class="metric-val">${state.tempC !== null ? `${state.tempC.toFixed(1)} °C` : '—'}</div>
        <div class="metric-sub">Шек: ≤ ${profile.idealTempMax} °C</div>
      </div>
      <div class="metric-box">
        <div class="metric-title">☀️ ЖАРЫҚ</div>
        <div class="metric-val">${state.lightLux !== null ? `${state.lightLux} лк` : '—'}</div>
        <div class="metric-sub">Оңтайлы: &lt; 50 лк</div>
      </div>
      <div class="metric-box">
        <div class="metric-title">🧪 ИНДИКАТОР pH</div>
        <div class="metric-val">${analysis.ph !== null ? analysis.ph.toFixed(1) : '—'}</div>
        <div class="metric-sub">Норма: ${profile.idealPhMin}–${profile.idealPhMax}</div>
      </div>
    </div>

    <div class="ios-card" style="margin-top:12px;">
      <div style="font-size:12px; font-weight:700; color:var(--ios-purple);">🦠 БИОЛОГИЯ ЖӘНЕ ХИМИЯ ҚОРЫТЫНДЫСЫ</div>
      <p style="font-size:13px; margin:6px 0 0; color:var(--ios-text);">${analysis.bioRisk}</p>
      ${analysis.ready ? `
        <div style="display:flex; gap:8px; margin-top:12px;">
          <button class="ios-btn ios-btn-green" style="flex:1;" onclick="saveCurrentLog()">Журналға сақтау</button>
          <button class="ios-btn ios-btn-gray" onclick="clearMeasurements()">Тазалау</button>
        </div>
      ` : ''}
    </div>

    <div class="section-label">Құрылғы параметрлері мен датчиктер</div>
    <div class="ios-card">
      <label class="field-label">Бақыланатын өнім:
        <select onchange="state.productType=this.value; render();">
          ${Object.values(PRODUCT_PROFILES).map((p) => `<option value="${p.id}" ${p.id === state.productType ? 'selected' : ''}>${p.name}</option>`).join('')}
        </select>
      </label>
      <label class="field-label">Табиғи индикатор түрі:
        <select onchange="state.indicatorId=this.value; render();">
          ${Object.values(INDICATORS).map((ind) => `<option value="${ind.id}" ${ind.id === state.indicatorId ? 'selected' : ''}>${ind.name}</option>`).join('')}
        </select>
      </label>

      <form onsubmit="applyManualSensors(event)">
        <div class="grid-2">
          <label class="field-label">Температура (°C):
            <input name="temp" type="number" step="0.1" value="${state.tempC ?? ''}" placeholder="Мысалы: 4.5" />
          </label>
          <label class="field-label">Жарық (люкс):
            <input name="lux" type="number" step="1" value="${state.lightLux ?? ''}" placeholder="Мысалы: 25" />
          </label>
        </div>
        <label class="field-label">Түс датчигі (HEX немесе RGB):
          <input name="colorHex" type="color" value="${hex}" style="height:46px; padding:4px;" />
        </label>
        <button type="submit" class="ios-btn ios-btn-blue full-width">Датчик мәндерін талдау</button>
      </form>
    </div>

    <div class="section-label">Сыртқы аппараттық датчик (ESP32 / BLE / JSON)</div>
    <div class="ios-card">
      <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:8px;">
        <span style="font-size:13px; font-weight:600;">Bluetooth күйі: ${state.bleStatus}</span>
        <button class="ios-btn ios-btn-navy" style="min-height:38px; padding:6px 12px; font-size:12px;" onclick="connectBleDevice()">BLE қосу</button>
      </div>
      <label class="field-label">Микроконтроллер JSON пакеті:
        <input id="json-packet-input" type="text" placeholder='{"temp":4.2,"lux":18,"r":124,"g":58,"b":237}' />
      </label>
      <button class="ios-btn ios-btn-gray full-width" onclick="importSensorJson()">JSON дерегін оқу</button>
    </div>

    <div class="section-label">Оқу-зерттеу үлгілері (Қауіпсіз симуляция)</div>
    <div class="ios-card">
      <p style="font-size:12px; color:var(--ios-sub); margin:0 0 8px;">Құрылғы қолыңызда болмаған жағдайда табиғи индикатор реакциясын тексеруге арналған үлгілер:</p>
      <div style="display:flex; flex-direction:column; gap:8px;">
        ${EDUCATIONAL_SCENARIOS.map((sc) => `
          <button class="ios-btn ios-btn-gray full-width" style="justify-content:flex-start; font-size:13px;" onclick="loadEducationalScenario('${sc.id}')">
            🧪 ${sc.label}
          </button>
        `).join('')}
      </div>
    </div>
  `;
}

function renderIndicator() {
  const indicator = INDICATORS[state.indicatorId];
  const analysis = analyzeState();
  const hex = state.rgb ? rgbToHex(state.rgb) : '#8E8E93';
  const phPos = analysis.ph !== null ? Math.min(100, Math.max(0, ((analysis.ph - 2) / 10) * 100)) : 50;

  return `
    <h1 class="large-title">Индикатор</h1>
    <p style="margin:0 0 12px; font-size:13px; color:var(--ios-sub);">Табиғи индикатор түсінің өзгеруін iPhone камерасымен цифрлық pH мәніне айналдыру</p>

    <div class="ios-card">
      <label class="field-label">Табиғи индикаторды таңдаңыз:
        <select onchange="state.indicatorId=this.value; render();">
          ${Object.values(INDICATORS).map((ind) => `<option value="${ind.id}" ${ind.id === state.indicatorId ? 'selected' : ''}>${ind.name}</option>`).join('')}
        </select>
      </label>
      <p style="font-size:12px; color:var(--ios-sub); margin:0 0 12px;">${indicator.desc}</p>

      <div class="camera-frame">
        ${state.cameraStream
          ? `<video id="live-cam-video" playsinline autoplay muted></video><div class="reticle ${state.isProcessing ? 'is-processing' : ''}"></div>`
          : state.capturedPhotoUrl
            ? `<img src="${state.capturedPhotoUrl}" alt="Индикатор фотосы" /><div class="reticle ${state.isProcessing ? 'is-processing' : ''}"></div>`
            : `${state.isProcessing ? `<div class="reticle is-processing"></div>` : ''}
               <div style="color:#94A3B8; text-align:center; padding:20px; font-size:13px;">
                 📷 Индикатор жолағын немесе ерітіндіні iPhone камерасына бағыттаңыз
               </div>`}
      </div>

      <div class="grid-2">
        <button class="ios-btn ios-btn-blue" onclick="startLiveCamera()">🎥 Live Камера</button>
        <label class="ios-btn ios-btn-navy" style="cursor:pointer;">
          📸 Фото түсіру
          <input type="file" accept="image/*" capture="environment" style="display:none;" onchange="handleIndicatorPhoto(this)" />
        </label>
      </div>
      ${state.cameraStream ? `
        <button class="ios-btn ios-btn-green full-width" style="margin-top:8px;" onclick="sampleCenterPixelFromVideo()">
          🎯 Ортадағы түсті сканерлеу (RGB → pH)
        </button>
      ` : ''}
    </div>

    <div class="ios-card">
      <div style="display:flex; align-items:center; gap:14px;">
        <div class="color-swatch" style="background:${hex};"></div>
        <div style="flex:1;">
          <div style="font-size:12px; color:var(--ios-sub);">АНЫҚТАЛҒАН ТҮС ПЕН pH</div>
          <div style="font-size:18px; font-weight:800;">
            ${analysis.ph !== null ? `pH ≈ ${analysis.ph.toFixed(1)}` : 'Түс алынбаған'}
          </div>
          <div style="font-size:12px; color:var(--ios-sub);">
            ${state.rgb ? `RGB(${state.rgb.join(', ')}) • ${hex} • Hue ${analysis.hsv.h}°` : 'Камера арқылы немесе төмендегі шкаладан үлгі таңдаңыз'}
          </div>
        </div>
      </div>

      <div class="ph-bar"><div class="ph-marker" style="left:${phPos}%;"></div></div>
      <div style="display:flex; justify-content:space-between; font-size:10px; color:var(--ios-sub);">
        <span>pH 2 (Қышқыл)</span>
        <span>pH 7 (Бейтарап)</span>
        <span>pH 12 (Сілті)</span>
      </div>
    </div>

    <div class="section-label">Индикатордың калибрленген түс шкаласы (Түрту арқылы тексеру)</div>
    <div class="ios-card">
      <div style="display:flex; flex-direction:column; gap:8px;">
        ${indicator.scale.map((s) => `
          <button class="ios-btn ios-btn-gray full-width" style="justify-content:flex-start;" onclick="pickCalibratedSwatch(${s.rgb[0]},${s.rgb[1]},${s.rgb[2]},'${s.label}')">
            <span style="width:22px; height:22px; border-radius:6px; background:${s.hex}; display:inline-block; flex-shrink:0;"></span>
            <span style="font-size:13px;">${s.label}</span>
          </button>
        `).join('')}
      </div>
    </div>
  `;
}

function renderScience() {
  const pillars = [
    {
      tag: 'ФИЗИКА',
      color: '#007AFF',
      title: 'Температура, жарық және RGB түс датчиктері',
      body: '• Температура датчигі (DS18B20 / NTC): +2…+6 °C тоңазытқыш аймағынан ауытқуды бақылайды.\n• Жарық датчигі (BH1750 / LDR): жарық сәулесі (люкс) әсерінен майлар мен дәрумендердің фотототығуын қадағалайды.\n• RGB түс датчигі (TCS34725): индикатор бетінен шағылған қызыл (650 нм), жасыл (530 нм), көк (460 нм) сәуле қарқындылығын өлшейді.'
    },
    {
      tag: 'ХИМИЯ',
      color: '#AF52DE',
      title: 'Табиғи индикаторлар және қышқылдық (pH) өзгерісі',
      body: '• Қызыл қырыққабат антоцианы: pH < 4 кезінде флавилий катионы түзіліп қызыл-алқызыл түс береді; бейтарап ортада күлгін; pH > 8 кезінде хиноидальды негіз түзіліп көк-жасыл түске енеді.\n• Куркумин: кето-енол таутомериясы есебінен сілтілі ортада сарыдан қызыл-қоңырға ауысады.\n• Сүт ашығанда сүт қышқылы жиналып pH төмендейді (6.6 → 4.5), ал ет пен балық бұзылғанда ұшқыш аминдер (NH3) жиналып pH жоғарылайды.'
    },
    {
      tag: 'БИОЛОГИЯ',
      color: '#34C759',
      title: 'Микроағзалардың тағам бұзылуындағы рөлі',
      body: '• 4 °C – 60 °C «Қауіпті температуралық аймақ» бактериялардың (Lactobacillus, Pseudomonas, E. coli) бөлінуін 20–30 минут сайын еселейді.\n• Сүт өнімдерінде лактобактериялар көмірсуды ашытып қышқыл түзеді.\n• Ет пен балықта аэробты бактериялар ақуыздарды ыдыратып сілтілі газдар бөледі. Табиғи индикатор осы биохимиялық процесті лезде көрсетеді.'
    },
    {
      tag: 'ЦИФРЛЫҚ ТЕХНОЛОГИЯ',
      color: '#FF9500',
      title: 'Табиғи индикатор түсін цифрлық жүйемен байланыстыру',
      body: '1. iPhone камерасы немесе TCS34725 датчигі индикатордан RGB сигнал алады.\n2. Алгоритм RGB → HSV (Hue бұрышы 0–360°) кеңістігіне түрлендіреді.\n3. Калибрлеу қисығы арқылы pH мәні есептеледі.\n4. Температура (°C) + Жарық (лк) + pH біріктіріліп, 0–100% Балғындық индексі шығарылады.'
    }
  ];

  return `
    <h1 class="large-title">Ғылым негізі</h1>
    <p style="margin:0 0 12px; font-size:13px; color:var(--ios-sub);">Пәнаралық интеграция: Физика • Химия • Биология • Цифрлық технология</p>
    ${pillars.map((p) => `
      <div class="ios-card">
        <span class="badge" style="background:${p.color}20; color:${p.color};">${p.tag}</span>
        <h3 style="margin:8px 0 6px; font-size:16px;">${p.title}</h3>
        <p style="margin:0; font-size:13px; color:#3A3A3C; white-space:pre-line;">${p.body}</p>
      </div>
    `).join('')}
  `;
}

function renderHistory() {
  return `
    <h1 class="large-title">Журнал</h1>
    <p style="margin:0 0 12px; font-size:13px; color:var(--ios-sub);">iPhone-да сақталған бақылау жазбалары (${state.logs.length})</p>

    <div class="grid-2" style="margin-bottom:12px;">
      <button class="ios-btn ios-btn-navy" onclick="exportLogsCsv()">⬇️ CSV экспорт</button>
      <button class="ios-btn ios-btn-blue" onclick="setTab('indicator')">+ Жаңа өлшем</button>
    </div>

    ${state.logs.length === 0 ? `
      <div class="ios-card">
        <strong>Әзірге өлшем жазбалары жоқ</strong>
        <p style="font-size:13px; color:var(--ios-sub); margin:6px 0 0;">«Монитор» немесе «Индикатор» бөлімінде өлшем жасап, «Журналға сақтау» батырмасын басыңыз.</p>
      </div>
    ` : state.logs.map((l) => `
      <div class="ios-card">
        <div style="display:flex; justify-content:space-between; align-items:flex-start; gap:8px;">
          <div>
            <strong>${l.productName}</strong>
            <div style="font-size:11px; color:var(--ios-sub);">${l.time} • ${l.indicatorName}</div>
            ${l.isSimulation ? `<div style="font-size:10px; color:#B45309; font-weight:700;">Симуляциялық үлгі</div>` : ''}
          </div>
          <span class="badge ${l.score >= 75 ? 'badge-fresh' : l.score >= 45 ? 'badge-warn' : 'badge-danger'}">${l.score}%</span>
        </div>
        <div class="grid-3" style="margin-top:10px;">
          <div class="metric-box"><div class="metric-title">Темп</div><div style="font-weight:700;">${l.tempC ?? '—'} °C</div></div>
          <div class="metric-box"><div class="metric-title">Жарық</div><div style="font-weight:700;">${l.lightLux ?? '—'} лк</div></div>
          <div class="metric-box"><div class="metric-title">pH / Түс</div><div style="font-weight:700;">${l.ph ? l.ph.toFixed(1) : '—'} ${l.hex ?? ''}</div></div>
        </div>
        <button class="ios-btn ios-btn-gray full-width" style="margin-top:10px; min-height:40px; color:#B91C1C;" onclick="deleteLogItem(${l.id})">Өшіру</button>
      </div>
    `).join('')}
  `;
}

function renderIphoneGuide() {
  return `
    <h1 class="large-title">iPhone орнату</h1>
    <p style="margin:0 0 12px; font-size:13px; color:var(--ios-sub);">iPhone Safari PWA және Xcode SwiftUI нұсқасы</p>

    <div class="ios-card">
      <h3 style="margin:0 0 6px; font-size:16px;">1. iPhone-да бірден іске қосу (Safari PWA)</h3>
      <p style="font-size:13px; color:#3A3A3C; margin:0 0 10px;">Қосымша iOS Safari талаптарына сай (Safe Area, 16px енгізу өрістері, 180×180 Apple Touch Icon, офлайн Service Worker) жасалған. App Store-сыз тікелей iPhone басты экранына орнатылады.</p>
      <button class="ios-btn ios-btn-blue full-width" onclick="openInstallSheet()">Safari арқылы орнату қадамдары</button>
    </div>

    <div class="ios-card">
      <h3 style="margin:0 0 6px; font-size:16px;">2. Нативті iPhone (SwiftUI + Xcode) коды</h3>
      <p style="font-size:13px; color:#3A3A3C; margin:0;">Жобаның <strong>ios/FreshGuardApp/</strong> бумасында Mac компьютердегі Xcode бағдарламасы арқылы тікелей iPhone-ға жинауға дайын SwiftUI бастапқы коды (<code>FreshGuardApp.swift</code>, <code>ColorSensorAnalyzer.swift</code>, <code>ContentView.swift</code>, <code>CameraIndicatorView.swift</code>, <code>Info.plist</code>) қоса берілген.</p>
    </div>
  `;
}

function render() {
  const container = document.getElementById('ios-content');
  if (!container) return;
  const views = {
    monitor: renderMonitor,
    indicator: renderIndicator,
    science: renderScience,
    history: renderHistory,
    iphone: renderIphoneGuide
  };
  container.innerHTML = (views[state.tab] || renderMonitor)();
}
