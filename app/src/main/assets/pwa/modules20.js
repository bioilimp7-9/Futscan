/* FOODSCAN AI 2.0 — Smart Food Analysis System Modules (PWA)
 * Module 1: Freshness Vision (Visual Freshness Analysis)
 * Module 2: Chemical Analysis (pH Indicator Paper + Reference Scale)
 * Module 3: Storage Monitor (Manual Entry + ESP32-ready Architecture)
 * Unified Product Report (A-D) & Multi-Day Product Tracking/Comparison
 */

(function () {
  const SOURCE_TAGS = {
    OCR: 'Қаптамадан оқылды',
    CAMERA: 'Камера арқылы талданды',
    PH_PAPER: 'pH қағазы арқылы бағаланды',
    USER: 'Пайдаланушы енгізді',
    NONE: 'Дерек жоқ'
  };

  const ALLOWED_VISION_STATUSES = [
    'Көрінетін өзгерістер аз',
    'Сыртқы өзгерістер байқалды',
    'Айқын сыртқы өзгерістер байқалды',
    'Сурет сапасы талдауға жеткісіз'
  ];

  function loadJson(key, fallback) {
    try {
      const raw = localStorage.getItem(key);
      return raw ? JSON.parse(raw) : fallback;
    } catch {
      return fallback;
    }
  }

  function saveJson(key, val) {
    try {
      localStorage.setItem(key, JSON.stringify(val));
    } catch {
      // ignore storage quota errors
    }
  }

  function formatNowLocal() {
    const d = new Date();
    const pad = (n) => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
  }

  function esc(str) {
    return String(str ?? '')
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }

  // Future hardware sensor adapter interface (ESP32 / BLE) — currently disconnected
  const StorageSensorAdapter = {
    connected: false,
    providerName: 'ESP32 / BLE (қазір қосылмаған)',
    getStatusText() {
      return 'Физикалық датчиктер қосылмаған. Барлық параметрлер телефоннан қолмен енгізіледі (болашақта ESP32 қосуға дайын архитектура).';
    }
  };

  const v2State = {
    freshnessRecords: loadJson('foodscan20_freshness_records', []),
    phRecords: loadJson('foodscan20_ph_records', []),
    storageRecords: loadJson('foodscan20_storage_records', []),

    // Drafts
    visionProductName: '',
    visionImage: null,
    visionLoading: false,
    visionDraft: null,
    visionError: null,

    phProductName: '',
    phSampleType: '',
    phImage: null,
    phScaleIncluded: false,
    phLoading: false,
    phDraft: null,
    phError: null,

    storageSavedMsg: null,
    selectedUnifiedProduct: '',
    pdfStudentAuthor: localStorage.getItem('foodscan20_pdf_author') || '',
    pdfProjectTitle: localStorage.getItem('foodscan20_pdf_project_title') || 'Мектеп ғылыми жобасы: Тағамды кешенді талдау',
    liveCameraStream: null,
    liveCameraCallback: null
  };

  // --- Camera Helper (Real getUserMedia permission + Gallery fallback) ---
  window.openLiveCameraFor = async function (targetMode) {
    const modal = document.getElementById('live-camera-modal');
    const video = document.getElementById('live-camera-video');
    const errBox = document.getElementById('live-camera-error');
    if (!modal || !video) return;

    v2State.liveCameraCallback = targetMode;
    errBox?.classList.add('hidden');
    modal.classList.remove('hidden');

    if (!navigator.mediaDevices?.getUserMedia) {
      if (errBox) {
        errBox.textContent = 'Бұл браузерде тікелей камера қолжетімсіз. Галереядан жүктеуді немесе жүйелік «Фото түсіру» түймесін пайдаланыңыз.';
        errBox.classList.remove('hidden');
      }
      return;
    }

    try {
      stopV2LiveCamera();
      const stream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: { ideal: 'environment' } },
        audio: false
      });
      v2State.liveCameraStream = stream;
      video.srcObject = stream;
      await video.play();
    } catch {
      if (errBox) {
        errBox.textContent = 'Камераға рұқсат берілмеді немесе HTTPS қажет. Төмендегі «Жабу» түймесін басып, «Галереядан жүктеу» баламасын пайдаланыңыз.';
        errBox.classList.remove('hidden');
      }
    }
  };

  function stopV2LiveCamera() {
    if (v2State.liveCameraStream) {
      v2State.liveCameraStream.getTracks().forEach((t) => t.stop());
      v2State.liveCameraStream = null;
    }
  }

  window.closeLiveCameraModal = function () {
    stopV2LiveCamera();
    document.getElementById('live-camera-modal')?.classList.add('hidden');
  };

  window.captureLiveCameraFrame = function () {
    const video = document.getElementById('live-camera-video');
    if (!video || !video.videoWidth) {
      alert('Камера бейнесі іске қосылмаған. Галереядан фото жүктеңіз.');
      return;
    }
    const reticle = document.getElementById('live-camera-reticle');
    reticle?.classList.add('is-processing');
    const maxDim = 1024;
    let w = video.videoWidth;
    let h = video.videoHeight;
    if (w > maxDim || h > maxDim) {
      const r = Math.min(maxDim / w, maxDim / h);
      w = Math.round(w * r);
      h = Math.round(h * r);
    }
    const canvas = document.createElement('canvas');
    canvas.width = w;
    canvas.height = h;
    canvas.getContext('2d').drawImage(video, 0, 0, w, h);
    const dataUrl = canvas.toDataURL('image/jpeg', 0.85);
    const target = v2State.liveCameraCallback;
    setTimeout(() => {
      reticle?.classList.remove('is-processing');
      window.closeLiveCameraModal();
      applyCapturedImage(target, dataUrl);
    }, 320);
  };

  function applyCapturedImage(target, dataUrl) {
    if (target === 'freshness') {
      v2State.visionImage = dataUrl;
      v2State.visionDraft = null;
      v2State.visionError = null;
    } else if (target === 'chemical') {
      v2State.phImage = dataUrl;
      v2State.phDraft = null;
      v2State.phError = null;
    }
    if (typeof window.render === 'function') window.render();
  }

  window.handleV2FilePicked = async function (input, target) {
    const file = input.files?.[0];
    if (!file) return;
    if (target === 'freshness') v2State.visionLoading = true;
    if (target === 'chemical') v2State.phLoading = true;
    if (typeof window.render === 'function') window.render();
    try {
      const dataUrl = await window.compressImageFile(file);
      if (target === 'freshness') v2State.visionLoading = false;
      if (target === 'chemical') v2State.phLoading = false;
      applyCapturedImage(target, dataUrl);
    } catch {
      if (target === 'freshness') {
        v2State.visionLoading = false;
        v2State.visionError = 'Суретті оқу кезінде қате шықты.';
      }
      if (target === 'chemical') {
        v2State.phLoading = false;
        v2State.phError = 'Суретті оқу кезінде қате шықты.';
      }
      if (typeof window.render === 'function') window.render();
    }
  };

  // --- On-device deterministic Canvas Visual Inspection (strictly non-inventing fallback) ---
  function inspectImagePixels(dataUrl) {
    return new Promise((resolve) => {
      const img = new Image();
      img.onload = () => {
        const w = Math.min(180, img.width || 180);
        const h = Math.min(180, img.height || 180);
        const canvas = document.createElement('canvas');
        canvas.width = w;
        canvas.height = h;
        const ctx = canvas.getContext('2d');
        ctx.drawImage(img, 0, 0, w, h);
        const data = ctx.getImageData(0, 0, w, h).data;
        let rSum = 0, gSum = 0, bSum = 0, darkCount = 0, count = 0;
        for (let i = 0; i < data.length; i += 4) {
          const r = data[i], g = data[i + 1], b = data[i + 2];
          rSum += r; gSum += g; bSum += b;
          const lum = 0.299 * r + 0.587 * g + 0.114 * b;
          if (lum < 65) darkCount++;
          count++;
        }
        const avgR = Math.round(rSum / count);
        const avgG = Math.round(gSum / count);
        const avgB = Math.round(bSum / count);
        const avgLum = Math.round(0.299 * avgR + 0.587 * avgG + 0.114 * avgB);
        const darkPct = Math.round((darkCount / count) * 100);

        // Center box color for pH strip preview
        const bw = Math.max(12, Math.round(w * 0.14));
        const cx = Math.floor(w / 2 - bw / 2);
        const cy = Math.floor(h / 2 - bw / 2);
        const cData = ctx.getImageData(cx, cy, bw, bw).data;
        let cr = 0, cg = 0, cb = 0, cc = 0;
        for (let i = 0; i < cData.length; i += 4) {
          cr += cData[i]; cg += cData[i + 1]; cb += cData[i + 2]; cc++;
        }
        resolve({
          avgRgb: [avgR, avgG, avgB],
          centerRgb: [Math.round(cr / cc), Math.round(cg / cc), Math.round(cb / cc)],
          avgLum,
          darkPct
        });
      };
      img.onerror = () => resolve({ avgRgb: [128, 128, 128], centerRgb: [128, 128, 128], avgLum: 120, darkPct: 0 });
      img.src = dataUrl;
    });
  }

  function describeRgbColorKz([r, g, b]) {
    const hex = '#' + [r, g, b].map((x) => x.toString(16).padStart(2, '0')).join('').toUpperCase();
    let tone = 'аралас реңк';
    if (r > 180 && g < 110 && b < 110) tone = 'қызыл / алқызыл';
    else if (r > 190 && g > 130 && b < 90) tone = 'қызғылт сары';
    else if (r > 185 && g > 180 && b < 110) tone = 'сары';
    else if (g > r + 20 && g > b + 15) tone = 'жасыл';
    else if (b > r + 20 && b > g + 15) tone = 'көк / көгілдір';
    else if (r > 120 && b > 120 && g < 110) tone = 'күлгін';
    else if (r > 200 && g > 200 && b > 200) tone = 'ақшыл / ашық';
    else if (r < 75 && g < 75 && b < 75) tone = 'қою қара';
    return `${tone} (RGB: ${r}, ${g}, ${b} • ${hex})`;
  }

  // --- MODULE 1: FRESHNESS VISION ---
  window.runFreshnessVisionAnalysis = async function () {
    const nameInput = document.getElementById('vision-product-name')?.value?.trim() || v2State.visionProductName;
    v2State.visionProductName = nameInput;
    if (!v2State.visionImage) {
      v2State.visionError = 'Алдымен камерамен тағам суретін түсіріңіз немесе галереядан таңдаңыз.';
      window.render();
      return;
    }

    v2State.visionLoading = true;
    v2State.visionError = null;
    window.render();

    try {
      const resp = await fetch('./api/analyze', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          mode: 'freshness_vision',
          imageBase64: v2State.visionImage,
          productName: v2State.visionProductName
        })
      });

      if (resp.ok) {
        const ai = await resp.json();
        const validStatus = ALLOWED_VISION_STATUSES.includes(ai.statusLabel)
          ? ai.statusLabel
          : 'Сурет сапасы талдауға жеткісіз';
        v2State.visionDraft = {
          recognizedFoodName: ai.recognizedFoodName || v2State.visionProductName || 'анықтау мүмкін болмады',
          surfaceDescription: ai.surfaceDescription || 'анықтау мүмкін болмады',
          observedChanges: ai.observedChanges || 'анықтау мүмкін болмады',
          comparativeConclusion: ai.comparativeConclusion || 'Көрінетін сыртқы белгілер бойынша бағалау.',
          confidenceAndLimits: ai.confidenceAndLimits || 'Визуалды талдау тек беткі белгілерді сипаттайды; ішкі микробиологиялық қауіпсіздік фотодан анықталмайды.',
          statusLabel: validStatus,
          dataSource: SOURCE_TAGS.CAMERA,
          timestamp: formatNowLocal()
        };
      } else {
        throw new Error('Server proxy unavailable');
      }
    } catch {
      const px = await inspectImagePixels(v2State.visionImage);
      const lowLight = px.avgLum < 45 || px.avgLum > 240;
      v2State.visionDraft = {
        recognizedFoodName: v2State.visionProductName || 'анықтау мүмкін болмады (бұлттық Gemini Vision қосылмаған)',
        surfaceDescription: `Орташа түс реңкі: ${describeRgbColorKz(px.avgRgb)}; орташа жарықтық: ${px.avgLum}/255.`,
        observedChanges: `Күңгірт/қарайған пикселдер үлесі: ${px.darkPct}%. Нақты дақтар мен көгерген жерлерді көзбен тексеріп толықтырыңыз.`,
        comparativeConclusion: lowLight
          ? 'Жарық жеткіліксіз немесе шамадан тыс жарық — суретті жарық жағдайында қайта түсіру ұсынылады.'
          : 'Бұлттық Gemini Vision сервері офлайн болғандықтан тек түс/жарық метрикасы алынды. Көрінетін өзгерісті төменнен растаңыз.',
        confidenceAndLimits: 'Шектеу: Бұлттық AI сервері (/api/analyze) қолжетімсіз. Бактериялар мен ішкі бұзылу фотосуреттен анықталмайды.',
        statusLabel: lowLight ? 'Сурет сапасы талдауға жеткісіз' : 'Көрінетін өзгерістер аз',
        dataSource: SOURCE_TAGS.CAMERA,
        timestamp: formatNowLocal()
      };
    } finally {
      v2State.visionLoading = false;
      window.render();
    }
  };

  window.saveFreshnessVisionRecord = function () {
    if (!v2State.visionDraft) return;
    const foodName = (document.getElementById('vision-edit-name')?.value || v2State.visionDraft.recognizedFoodName).trim();
    const surfaceDescription = document.getElementById('vision-edit-surface')?.value?.trim() || v2State.visionDraft.surfaceDescription;
    const observedChanges = document.getElementById('vision-edit-changes')?.value?.trim() || v2State.visionDraft.observedChanges;
    const comparativeConclusion = document.getElementById('vision-edit-conclusion')?.value?.trim() || v2State.visionDraft.comparativeConclusion;
    const statusLabel = document.getElementById('vision-edit-status')?.value || v2State.visionDraft.statusLabel;

    const record = {
      id: Date.now(),
      productName: foodName || 'Атауы көрсетілмеген өнім',
      recognizedFoodName: foodName,
      surfaceDescription,
      observedChanges,
      comparativeConclusion,
      confidenceAndLimits: v2State.visionDraft.confidenceAndLimits,
      statusLabel: ALLOWED_VISION_STATUSES.includes(statusLabel) ? statusLabel : 'Сурет сапасы талдауға жеткісіз',
      imageBase64: v2State.visionImage,
      dataSource: SOURCE_TAGS.CAMERA,
      timestamp: formatNowLocal()
    };

    v2State.freshnessRecords.unshift(record);
    saveJson('foodscan20_freshness_records', v2State.freshnessRecords);
    v2State.selectedUnifiedProduct = record.productName;
    alert(`«${record.productName}» визуалды бақылау тарихына сақталды!`);
    window.render();
  };

  window.deleteFreshnessRecord = function (id) {
    v2State.freshnessRecords = v2State.freshnessRecords.filter((r) => r.id !== id);
    saveJson('foodscan20_freshness_records', v2State.freshnessRecords);
    window.render();
  };

  // --- MODULE 2: CHEMICAL ANALYSIS (pH) ---
  window.runChemicalPhAnalysis = async function () {
    v2State.phProductName = document.getElementById('ph-product-name')?.value?.trim() || '';
    v2State.phSampleType = document.getElementById('ph-sample-type')?.value?.trim() || '';
    v2State.phScaleIncluded = Boolean(document.getElementById('ph-scale-included')?.checked);

    if (!v2State.phProductName) {
      v2State.phError = 'Зерттелетін тағамның атауын енгізіңіз.';
      window.render();
      return;
    }
    if (!v2State.phImage) {
      v2State.phError = 'pH индикатор қағазының суретін камерамен түсіріңіз немесе галереядан таңдаңыз.';
      window.render();
      return;
    }

    v2State.phLoading = true;
    v2State.phError = null;
    window.render();

    try {
      const resp = await fetch('./api/analyze', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          mode: 'chemical_ph',
          imageBase64: v2State.phImage,
          productName: v2State.phProductName,
          sampleType: v2State.phSampleType || 'Көрсетілмеген'
        })
      });

      if (resp.ok) {
        const ai = await resp.json();
        const scaleOk = Boolean(ai.scaleVisible);
        v2State.phDraft = {
          productName: v2State.phProductName,
          sampleType: v2State.phSampleType || 'Көрсетілмеген',
          paperDetected: Boolean(ai.paperDetected),
          paperColor: ai.paperColor || 'анықтау мүмкін болмады',
          scaleVisible: scaleOk,
          estimatedPhRange: scaleOk ? (ai.estimatedPhRange || 'анықтау мүмкін болмады') : 'анықтау мүмкін болмады',
          acidityExplanation: scaleOk
            ? (ai.acidityExplanation || 'Индикатор қағазы арқылы шамамен бағалау.')
            : 'pH түстер шкаласы суретте анық көрінбеді. Нақты pH саны ойдан шығарылмайды — шкаласы бар қаптаманы индикатор қағазымен бірге қайта суретке түсіріңіз.',
          confidenceLevel: ai.confidenceLevel || 'Төмен',
          errorMarginNote: (ai.errorMarginNote || '') + ' (Назар аударыңыз: бұл шамамен бағалау, зертханалық pH-метр өлшемі емес.)',
          dataSource: SOURCE_TAGS.PH_PAPER,
          timestamp: formatNowLocal()
        };
      } else {
        throw new Error('Server proxy unavailable');
      }
    } catch {
      const px = await inspectImagePixels(v2State.phImage);
      const centerColorDesc = describeRgbColorKz(px.centerRgb);
      v2State.phDraft = {
        productName: v2State.phProductName,
        sampleType: v2State.phSampleType || 'Көрсетілмеген',
        paperDetected: true,
        paperColor: centerColorDesc,
        scaleVisible: v2State.phScaleIncluded,
        estimatedPhRange: 'анықтау мүмкін болмады',
        acidityExplanation: v2State.phScaleIncluded
          ? 'Бұлттық Gemini Vision сервері офлайн болғандықтан, суреттегі орталық индикатор түсі ғана оқылды. Қаптамадағы физикалық шкаламен салыстырып, шамамен pH аралығын төменде қолмен растай аласыз.'
          : 'Суретте pH түстер шкаласы расталмады немесе AI сервері офлайн. Шкаласыз pH мәні ойдан шығарылмайды — қағазды pH шкаласымен бірге қайта түсіріңіз.',
        confidenceLevel: 'Төмен',
        errorMarginNote: 'Офлайн / жарық жағдайына байланысты ықтимал қателік (±0.5–1.0 pH). Бұл зертханалық pH-метр емес, тек шамамен бағалау.',
        dataSource: SOURCE_TAGS.PH_PAPER,
        timestamp: formatNowLocal()
      };
    } finally {
      v2State.phLoading = false;
      window.render();
    }
  };

  window.saveChemicalPhRecord = function () {
    if (!v2State.phDraft) return;
    const manualPhRange = document.getElementById('ph-edit-range')?.value?.trim();
    const manualExplanation = document.getElementById('ph-edit-explanation')?.value?.trim();
    const finalPhRange = manualPhRange || v2State.phDraft.estimatedPhRange;
    const isMeasured = finalPhRange && finalPhRange !== 'анықтау мүмкін болмады';

    const record = {
      id: Date.now(),
      productName: v2State.phDraft.productName,
      sampleType: v2State.phDraft.sampleType,
      paperColor: v2State.phDraft.paperColor,
      scaleVisible: v2State.phDraft.scaleVisible,
      estimatedPhRange: isMeasured ? finalPhRange : 'анықтау мүмкін болмады',
      acidityExplanation: manualExplanation || v2State.phDraft.acidityExplanation,
      confidenceLevel: v2State.phDraft.confidenceLevel,
      errorMarginNote: v2State.phDraft.errorMarginNote,
      imageBase64: v2State.phImage,
      dataSource: isMeasured ? SOURCE_TAGS.PH_PAPER : SOURCE_TAGS.NONE,
      timestamp: formatNowLocal()
    };

    v2State.phRecords.unshift(record);
    saveJson('foodscan20_ph_records', v2State.phRecords);
    v2State.selectedUnifiedProduct = record.productName;
    alert(`«${record.productName}» pH талдау жазбасы сақталды!`);
    window.render();
  };

  window.deletePhRecord = function (id) {
    v2State.phRecords = v2State.phRecords.filter((r) => r.id !== id);
    saveJson('foodscan20_ph_records', v2State.phRecords);
    window.render();
  };

  // --- MODULE 3: STORAGE MONITOR (Manual Only — No Fake Sensor Readings) ---
  window.saveStorageMonitorRecord = function (e) {
    e.preventDefault();
    const f = e.target;
    const productName = f.productName.value.trim();
    if (!productName) {
      alert('Өнім атауын енгізіңіз.');
      return;
    }

    const parseOptNum = (val) => {
      if (val === '' || val === null || val === undefined) return null;
      const n = parseFloat(val);
      return Number.isNaN(n) ? null : n;
    };

    const roomTempC = parseOptNum(f.roomTemp.value);
    const fridgeTempC = parseOptNum(f.fridgeTemp.value);
    const humidityPct = parseOptNum(f.humidity.value);
    const lightCondition = f.lightCondition.value || 'Өлшенбеген';
    const storageLocation = f.storageLocation.value || 'Өлшенбеген';
    const duration = f.duration.value.trim() || null;
    const timestamp = f.measuredAt.value.trim() || formatNowLocal();

    const record = {
      id: Date.now(),
      productName,
      roomTempC,
      fridgeTempC,
      humidityPct,
      lightCondition,
      storageLocation,
      duration,
      timestamp,
      dataSource: SOURCE_TAGS.USER
    };

    v2State.storageRecords.unshift(record);
    saveJson('foodscan20_storage_records', v2State.storageRecords);
    v2State.selectedUnifiedProduct = productName;
    v2State.storageSavedMsg = `«${productName}» сақтау жағдайы сақталды (${timestamp}).`;
    window.render();
  };

  window.deleteStorageRecord = function (id) {
    v2State.storageRecords = v2State.storageRecords.filter((r) => r.id !== id);
    saveJson('foodscan20_storage_records', v2State.storageRecords);
    window.render();
  };

  // --- Helper: Collect all product names across all 4 modules ---
  function getAllProductNames(existingProducts) {
    const names = new Set();
    (existingProducts || []).forEach((p) => p.productName && names.add(p.productName.trim()));
    v2State.freshnessRecords.forEach((r) => r.productName && names.add(r.productName.trim()));
    v2State.phRecords.forEach((r) => r.productName && names.add(r.productName.trim()));
    v2State.storageRecords.forEach((r) => r.productName && names.add(r.productName.trim()));
    return Array.from(names);
  }

  function extractNumericPh(phStr) {
    if (!phStr || phStr === 'анықтау мүмкін болмады') return null;
    const matches = String(phStr).replace(/,/g, '.').match(/[0-9]+(?:\.[0-9]+)?/g);
    if (!matches || !matches.length) return null;
    const nums = matches.map(Number).filter((n) => n >= 0 && n <= 14);
    if (!nums.length) return null;
    return nums.reduce((a, b) => a + b, 0) / nums.length;
  }

  function renderMiniSvgChart(points, color, unitLabel, minY, maxY) {
    if (!points.length) {
      return `<div style="font-size:12px; color:#64748B; padding:10px; background:#F8FAFC; border-radius:10px;">График құру үшін кемінде 1 өлшенген мән қажет.</div>`;
    }
    const w = 320, h = 110, pad = 22;
    const spanY = Math.max(1, maxY - minY);
    const coords = points.map((pt, i) => {
      const x = points.length === 1 ? w / 2 : pad + (i / (points.length - 1)) * (w - pad * 2);
      const clamped = Math.min(maxY, Math.max(minY, pt.val));
      const y = h - pad - ((clamped - minY) / spanY) * (h - pad * 2);
      return { x, y, val: pt.val, label: pt.label };
    });
    const polyline = coords.map((c) => `${c.x.toFixed(1)},${c.y.toFixed(1)}`).join(' ');
    return `
      <svg viewBox="0 0 ${w} ${h}" style="width:100%; max-height:130px; background:#F8FAFC; border-radius:10px; border:1px solid #E2E8F0;">
        <line x1="${pad}" y1="${h - pad}" x2="${w - pad}" y2="${h - pad}" stroke="#CBD5E1" stroke-width="1"/>
        ${coords.length > 1 ? `<polyline fill="none" stroke="${color}" stroke-width="2.5" points="${polyline}"/>` : ''}
        ${coords.map((c) => `
          <circle cx="${c.x.toFixed(1)}" cy="${c.y.toFixed(1)}" r="4.5" fill="${color}" />
          <text x="${c.x.toFixed(1)}" y="${(c.y - 8).toFixed(1)}" font-size="10" font-weight="700" text-anchor="middle" fill="#0F172A">${c.val.toFixed(1)}${unitLabel}</text>
          <text x="${c.x.toFixed(1)}" y="${h - 6}" font-size="8.5" text-anchor="middle" fill="#64748B">${esc(c.label.slice(5, 10))}</text>
        `).join('')}
      </svg>
    `;
  }

  // --- VIEWS ---
  function renderFreshnessVisionView() {
    const d = v2State.visionDraft;
    const statusBadgeColor = (s) => {
      if (s === 'Көрінетін өзгерістер аз') return '#059669';
      if (s === 'Сыртқы өзгерістер байқалды') return '#D97706';
      if (s === 'Айқын сыртқы өзгерістер байқалды') return '#DC2626';
      return '#475569';
    };

    return `
      <div class="section-kicker" style="color:var(--cyan);">MODULE 1 • FRESHNESS VISION</div>
      <h2 style="margin:0 0 6px;">Визуалды балғындық талдау</h2>
      <p style="font-size:13px; color:#475569; margin:0 0 12px;">Телефон камерасы немесе галерея арқылы тағамның сыртқы түсі мен көрінетін өзгерістерін сипаттау.</p>

      <div class="warning-box">
        <strong>МАҢЫЗДЫ ШЕКТЕУ:</strong> Визуалды талдау медициналық немесе санитарлық қауіпсіздік қорытындысы ЕМЕС. Фотосуреттен ішкі микробиологиялық қауіпсіздік пен бактерия саны анықталмайды.
      </div>

      <div class="card">
        <label style="font-size:12px; font-weight:700;">Өнім атауы (күндер бойынша салыстыру үшін):
          <input id="vision-product-name" type="text" value="${esc(v2State.visionProductName)}" placeholder="Мысалы: Алма (сорт Апорт) немесе Сүт" />
        </label>

        <div class="preview-box camera-viewport" style="margin-top:12px;">
          ${v2State.visionImage
            ? `<div style="position:relative; width:100%; display:flex; justify-content:center;">
                 <img src="${v2State.visionImage}" alt="Тағам суреті" />
                 <div class="camera-reticle ${v2State.visionLoading ? 'is-processing' : ''}" aria-hidden="true"></div>
               </div>
               <button class="btn-outline" style="margin-top:8px;" onclick="window._v2ClearVision()">✕ Суретті өшіру</button>`
            : `${v2State.visionLoading ? `<div class="camera-reticle is-processing" aria-hidden="true"></div>` : ''}
               <span style="color:#64748B; font-size:13px;">Тағамның жарық жағдайында түсірілген фотосын жүктеңіз</span>`}
        </div>

        <div class="btn-grid-2">
          <button type="button" class="btn-primary btn-navy" onclick="openLiveCameraFor('freshness')">📷 Камераны ашу</button>
          <label class="btn-outline" style="cursor:pointer;">
            🖼️ Галереядан жүктеу
            <input type="file" accept="image/*" style="display:none;" onchange="handleV2FilePicked(this, 'freshness')" />
          </label>
        </div>

        <div style="margin-top:8px;">
          <label class="btn-outline full-width" style="cursor:pointer; font-size:12px;">
            📱 Мобильді камера (балама файл арқылы түсіру)
            <input type="file" accept="image/*" capture="environment" style="display:none;" onchange="handleV2FilePicked(this, 'freshness')" />
          </label>
        </div>

        <button class="btn-primary full-width" style="margin-top:10px;" onclick="runFreshnessVisionAnalysis()">
          ${v2State.visionLoading ? '⏳ Gemini Vision талдауы жүруде...' : '🔍 Сыртқы белгілерді талдау'}
        </button>
        ${v2State.visionError ? `<div class="warning-box" style="margin-top:10px;">${esc(v2State.visionError)}</div>` : ''}
      </div>

      ${d ? `
        <div class="section-kicker">ВИЗУАЛДЫ ТАЛДАУ НӘТИЖЕСІ</div>
        <div class="card">
          <div style="display:flex; justify-content:space-between; align-items:center; gap:8px; flex-wrap:wrap; margin-bottom:10px;">
            <span class="status-pill" style="background:${statusBadgeColor(d.statusLabel)}; color:#fff;">${esc(d.statusLabel)}</span>
            <span class="source-pill">${esc(d.dataSource)} • ${esc(d.timestamp)}</span>
          </div>

          <label class="field-sm">1. Танылған тағам атауы:
            <input id="vision-edit-name" type="text" value="${esc(d.recognizedFoodName)}" />
          </label>
          <label class="field-sm">2. Сыртқы көрініс сипаттамасы:
            <textarea id="vision-edit-surface" rows="2">${esc(d.surfaceDescription)}</textarea>
          </label>
          <label class="field-sm">3. Байқалған өзгерістер (қараю, қоңырлану, дақтар, зақымдану):
            <textarea id="vision-edit-changes" rows="2">${esc(d.observedChanges)}</textarea>
          </label>
          <label class="field-sm">4. Көрнекі белгілер бойынша салыстырмалы қорытынды:
            <textarea id="vision-edit-conclusion" rows="2">${esc(d.comparativeConclusion)}</textarea>
          </label>
          <label class="field-sm">Қорытынды мәртебесі:
            <select id="vision-edit-status">
              ${ALLOWED_VISION_STATUSES.map((st) => `<option value="${esc(st)}" ${d.statusLabel === st ? 'selected' : ''}>${esc(st)}</option>`).join('')}
            </select>
          </label>
          <div style="margin-top:10px; font-size:12px; color:#334155; background:#F8FAFC; padding:10px; border-radius:10px;">
            <strong>5. Талдаудың сенімділігі мен шектеулері:</strong><br/>${esc(d.confidenceAndLimits)}
          </div>

          <button class="btn-primary full-width" style="margin-top:12px;" onclick="saveFreshnessVisionRecord()">✓ Тарихқа және Біріктірілген есепке сақтау</button>
        </div>
      ` : ''}

      <div class="section-kicker">БҰРЫНҒЫ ВИЗУАЛДЫ БАҚЫЛАУЛАР (${v2State.freshnessRecords.length})</div>
      ${v2State.freshnessRecords.length === 0
        ? `<div class="card" style="font-size:13px; color:#64748B;">Әзірге визуалды талдау жазбалары жоқ.</div>`
        : v2State.freshnessRecords.map((rec) => `
          <div class="card" style="margin-bottom:10px;">
            <div style="display:flex; justify-content:space-between; align-items:start; gap:8px;">
              <div>
                <strong>${esc(rec.productName)}</strong>
                <div style="font-size:11px; color:#64748B;">${esc(rec.timestamp)} • ${esc(rec.dataSource)}</div>
              </div>
              <button class="btn-outline" style="min-height:34px; padding:4px 8px; color:#DC2626;" onclick="deleteFreshnessRecord(${rec.id})">Өшіру</button>
            </div>
            <div style="margin:6px 0;"><span class="status-pill" style="background:${statusBadgeColor(rec.statusLabel)}; color:#fff;">${esc(rec.statusLabel)}</span></div>
            ${rec.imageBase64 ? `<img src="${rec.imageBase64}" alt="${esc(rec.productName)}" style="width:100%; max-height:150px; object-fit:cover; border-radius:8px; margin-bottom:6px;" />` : ''}
            <div style="font-size:12px; color:#334155;"><strong>Өзгерістер:</strong> ${esc(rec.observedChanges)}</div>
          </div>
        `).join('')}
    `;
  }

  // --- MODULE 2: CHEMICAL ANALYSIS (pH) ---
  function renderChemicalPhView() {
    const d = v2State.phDraft;
    return `
      <div class="section-kicker" style="color:var(--purple);">MODULE 2 • CHEMICAL ANALYSIS</div>
      <h2 style="margin:0 0 6px;">Химиялық pH талдау</h2>
      <p style="font-size:13px; color:#475569; margin:0 0 12px;">Универсалды pH индикатор қағазын телефон камерасымен бағалау.</p>

      <div class="warning-box">
        <strong>ЗЕРТХАНАЛЫҚ ЕСКЕРТУ:</strong> pH қағазының нәтижесі — тек <strong>шамамен бағалау</strong>, ол нақты зертханалық pH-метр өлшемін алмастырмайды. Шкала көрінбесе, pH мәні ойдан жазылмайды.
      </div>

      <div class="card">
        <label class="field-sm">Зерттелетін тағамның атауы:
          <input id="ph-product-name" type="text" value="${esc(v2State.phProductName)}" placeholder="Мысалы: Пияз немесе Сүт" />
        </label>
        <label class="field-sm">Үлгі түрі:
          <input id="ph-sample-type" type="text" value="${esc(v2State.phSampleType)}" placeholder="Мысалы: Пияздың судағы сығындысы" />
        </label>
        <label style="display:flex; align-items:center; gap:8px; margin-top:10px; font-size:13px;">
          <input id="ph-scale-included" type="checkbox" style="width:20px; height:20px; margin:0;" ${v2State.phScaleIncluded ? 'checked' : ''} />
          <span>Суретте pH түстер шкаласы бар қаптама бірге түсірілген</span>
        </label>

        <div class="preview-box camera-viewport" style="margin-top:12px;">
          ${v2State.phImage
            ? `<div style="position:relative; width:100%; display:flex; justify-content:center;">
                 <img src="${v2State.phImage}" alt="pH индикатор қағазы" />
                 <div class="camera-reticle ${v2State.phLoading ? 'is-processing' : ''}" aria-hidden="true"></div>
               </div>
               <button class="btn-outline" style="margin-top:8px;" onclick="window._v2ClearPh()">✕ Суретті өшіру</button>`
            : `${v2State.phLoading ? `<div class="camera-reticle is-processing" aria-hidden="true"></div>` : ''}
               <span style="color:#64748B; font-size:13px;">pH индикатор қағазын (мүмкін болса шкаламен бірге) түсіріңіз</span>`}
        </div>

        <div class="btn-grid-2">
          <button type="button" class="btn-primary btn-navy" onclick="openLiveCameraFor('chemical')">📷 Камераны ашу</button>
          <label class="btn-outline" style="cursor:pointer;">
            🖼️ Галереядан жүктеу
            <input type="file" accept="image/*" style="display:none;" onchange="handleV2FilePicked(this, 'chemical')" />
          </label>
        </div>

        <button class="btn-primary full-width" style="margin-top:10px;" onclick="runChemicalPhAnalysis()">
          ${v2State.phLoading ? '⏳ Индикатор түсі мен шкала талдануда...' : '🧪 pH индикатор қағазын талдау'}
        </button>
        ${v2State.phError ? `<div class="warning-box" style="margin-top:10px;">${esc(v2State.phError)}</div>` : ''}
      </div>

      ${d ? `
        <div class="section-kicker">pH ТАЛДАУ НӘТИЖЕСІ (ШАМАМЕН БАҒАЛАУ)</div>
        <div class="card">
          <div style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:6px; margin-bottom:10px;">
            <span class="source-pill">${esc(d.dataSource)}</span>
            <span style="font-size:11px; color:#64748B;">${esc(d.timestamp)} • Сенімділік: <strong>${esc(d.confidenceLevel)}</strong></span>
          </div>
          <div style="font-size:13px; line-height:1.6;">
            <div><strong>Зерттелетін өнім:</strong> ${esc(d.productName)}</div>
            <div><strong>Үлгінің түрі:</strong> ${esc(d.sampleType)}</div>
            <div><strong>Индикатор түсі:</strong> ${esc(d.paperColor)}</div>
            <div><strong>Шкала көрінді ме:</strong> ${d.scaleVisible ? 'Иә' : 'Жоқ (қайта түсіру ұсынылады)'}</div>
          </div>

          <label class="field-sm">Болжамды pH мәні немесе аралығы (тек шкала бойынша салыстырылса):
            <input id="ph-edit-range" type="text" value="${esc(d.estimatedPhRange)}" />
          </label>
          <label class="field-sm">Қышқыл, бейтарап немесе сілтілі орта туралы түсіндірме:
            <textarea id="ph-edit-explanation" rows="3">${esc(d.acidityExplanation)}</textarea>
          </label>
          <div style="margin-top:8px; font-size:12px; color:#92400E; background:#FEF3C7; padding:10px; border-radius:10px;">
            <strong>Ықтимал қателік:</strong> ${esc(d.errorMarginNote)}
          </div>

          <button class="btn-primary full-width" style="margin-top:12px;" onclick="saveChemicalPhRecord()">✓ pH нәтижесін сақтау</button>
        </div>
      ` : ''}

      <div class="section-kicker">САҚТАЛҒАН pH ӨЛШЕМДЕРІ (${v2State.phRecords.length})</div>
      ${v2State.phRecords.length === 0
        ? `<div class="card" style="font-size:13px; color:#64748B;">Әзірге сақталған pH жазбалары жоқ.</div>`
        : v2State.phRecords.map((r) => `
          <div class="card" style="margin-bottom:10px;">
            <div style="display:flex; justify-content:space-between; align-items:start;">
              <div>
                <strong>${esc(r.productName)}</strong> — <span style="color:var(--purple); font-weight:800;">${esc(r.estimatedPhRange)}</span>
                <div style="font-size:11px; color:#64748B;">${esc(r.sampleType)} • ${esc(r.timestamp)}</div>
                <div style="font-size:11px; color:#047857;">${esc(r.dataSource)}</div>
              </div>
              <button class="btn-outline" style="min-height:34px; padding:4px 8px; color:#DC2626;" onclick="deletePhRecord(${r.id})">Өшіру</button>
            </div>
          </div>
        `).join('')}
    `;
  }

  // --- MODULE 3: STORAGE MONITOR ---
  function renderStorageMonitorView() {
    const na = (v, suffix = '') => (v === null || v === undefined || v === '' ? 'Өлшенбеген' : `${v}${suffix}`);
    return `
      <div class="section-kicker" style="color:var(--green);">MODULE 3 • STORAGE MONITOR</div>
      <h2 style="margin:0 0 6px;">Сақтау жағдайын бақылау</h2>
      <p style="font-size:13px; color:#475569; margin:0 0 12px;">Физикалық датчиктер қосылмағандықтан, барлық параметрді нақты өлшем бойынша қолмен енгізіңіз.</p>

      <div class="disclaimer-card" style="margin-top:0; margin-bottom:12px;">
        <strong>АРХИТЕКТУРА КҮЙІ:</strong> ${esc(StorageSensorAdapter.getStatusText())}
        <br/>Температура мен ылғалдылық камерадан немесе AI арқылы болжанбайды; толтырылмаған өрістер «Өлшенбеген» деп сақталады.
      </div>

      ${v2State.storageSavedMsg ? `<div class="card" style="background:#ECFDF5; border-color:#6EE7B7; color:#065F46; margin-bottom:12px; font-weight:600; font-size:13px;">✓ ${esc(v2State.storageSavedMsg)}</div>` : ''}

      <form class="card" onsubmit="saveStorageMonitorRecord(event)">
        <label class="field-sm">Өнім атауы:
          <input name="productName" type="text" required value="${esc(v2State.selectedUnifiedProduct)}" placeholder="Мысалы: Табиғи сүт" />
        </label>

        <div class="btn-grid-2" style="margin-top:8px;">
          <label class="field-sm">Бөлме температурасы (°C):
            <input name="roomTemp" type="number" step="0.1" placeholder="Мысалы: 22.5" />
          </label>
          <label class="field-sm">Тоңазытқыш температурасы (°C):
            <input name="fridgeTemp" type="number" step="0.1" placeholder="Мысалы: 4.0" />
          </label>
        </div>

        <div class="btn-grid-2" style="margin-top:8px;">
          <label class="field-sm">Ауа ылғалдылығы (%, бар болса):
            <input name="humidity" type="number" step="1" min="0" max="100" placeholder="Өлшенбесе бос қалдырыңыз" />
          </label>
          <label class="field-sm">Жарық жағдайы:
            <select name="lightCondition">
              <option value="Өлшенбеген">Өлшенбеген</option>
              <option value="Жарық">Жарық</option>
              <option value="Көлеңке">Көлеңке</option>
              <option value="Қараңғы">Қараңғы</option>
            </select>
          </label>
        </div>

        <div class="btn-grid-2" style="margin-top:8px;">
          <label class="field-sm">Сақтау орны:
            <select name="storageLocation">
              <option value="Тоңазытқыш">Тоңазытқыш</option>
              <option value="Бөлме">Бөлме</option>
              <option value="Басқа">Басқа</option>
            </select>
          </label>
          <label class="field-sm">Сақтау ұзақтығы:
            <input name="duration" type="text" placeholder="Мысалы: 24 сағат / 2 күн" />
          </label>
        </div>

        <label class="field-sm">Өлшеу күні мен уақыты:
          <input name="measuredAt" type="text" value="${formatNowLocal()}" />
        </label>

        <button type="submit" class="btn-primary full-width" style="margin-top:12px;">🌡️ Сақтау жағдайын сақтау</button>
      </form>

      <div class="section-kicker">САҚТАЛҒАН ЖАҒДАЙ ЖАЗБАЛАРЫ (${v2State.storageRecords.length})</div>
      ${v2State.storageRecords.length === 0
        ? `<div class="card" style="font-size:13px; color:#64748B;">Әзірге сақтау жағдайы бойынша жазба жоқ.</div>`
        : v2State.storageRecords.map((s) => `
          <div class="card" style="margin-bottom:10px;">
            <div style="display:flex; justify-content:space-between; align-items:start;">
              <div>
                <strong>${esc(s.productName)}</strong>
                <div style="font-size:11px; color:#64748B;">${esc(s.timestamp)} • ${esc(s.dataSource)}</div>
              </div>
              <button class="btn-outline" style="min-height:34px; padding:4px 8px; color:#DC2626;" onclick="deleteStorageRecord(${s.id})">Өшіру</button>
            </div>
            <div style="display:grid; grid-template-columns:repeat(3,1fr); gap:6px; margin-top:8px; font-size:11px; background:#F8FAFC; padding:8px; border-radius:8px;">
              <div>Бөлме t°<br/><strong>${na(s.roomTempC, ' °C')}</strong></div>
              <div>Тоңазытқыш t°<br/><strong>${na(s.fridgeTempC, ' °C')}</strong></div>
              <div>Ылғалдылық<br/><strong>${na(s.humidityPct, '%')}</strong></div>
              <div>Жарық<br/><strong>${esc(s.lightCondition)}</strong></div>
              <div>Орын<br/><strong>${esc(s.storageLocation)}</strong></div>
              <div>Ұзақтығы<br/><strong>${esc(s.duration || 'Дерек жоқ')}</strong></div>
            </div>
          </div>
        `).join('')}
    `;
  }

  // --- UNIFIED PRODUCT REPORT (A-D) + MULTI-DAY COMPARISON ---
  function renderUnifiedReportView() {
    const existingProducts = window._foodscanGetProducts ? window._foodscanGetProducts() : [];
    const productNames = getAllProductNames(existingProducts);
    if (!v2State.selectedUnifiedProduct && productNames.length) {
      v2State.selectedUnifiedProduct = productNames[0];
    }
    const targetName = v2State.selectedUnifiedProduct;
    const norm = (s) => (s || '').trim().toLowerCase();

    const ocrMatch = existingProducts.find((p) => norm(p.productName) === norm(targetName));
    const visionMatches = v2State.freshnessRecords.filter((r) => norm(r.productName) === norm(targetName));
    const phMatches = v2State.phRecords.filter((r) => norm(r.productName) === norm(targetName));
    const storageMatches = v2State.storageRecords.filter((r) => norm(r.productName) === norm(targetName));

    const latestVision = visionMatches[0] || null;
    const latestPh = phMatches[0] || null;
    const latestStorage = storageMatches[0] || null;

    // Multi-day trend points (chronological)
    const phPoints = [...phMatches]
      .reverse()
      .map((r) => ({ val: extractNumericPh(r.estimatedPhRange), label: r.timestamp }))
      .filter((p) => p.val !== null);

    const tempPoints = [...storageMatches]
      .reverse()
      .map((s) => ({
        val: s.fridgeTempC !== null && s.fridgeTempC !== undefined ? s.fridgeTempC : s.roomTempC,
        label: s.timestamp
      }))
      .filter((p) => p.val !== null && p.val !== undefined);

    return `
      <div class="print-only-header">
        <div class="print-kicker">FOODSCAN AI 2.0 • SMART FOOD ANALYSIS SYSTEM</div>
        <h1 id="print-header-title" style="margin:4px 0 6px; font-size:20px; color:#0A2540;">${esc(v2State.pdfProjectTitle || 'Мектеп ғылыми жобасы: Біріктірілген өнім есебі (A–D)')}</h1>
        <div style="font-size:13px; color:#1E293B;">
          <strong>Зерттелетін өнім:</strong> ${esc(targetName || 'Таңдалмаған')}
          &nbsp;|&nbsp;
          <strong>Орындаған:</strong> <span id="print-header-author">${esc(v2State.pdfStudentAuthor || 'Көрсетілмеген')}</span>
          &nbsp;|&nbsp;
          <strong>Күні:</strong> ${esc(formatNowLocal())}
        </div>
      </div>

      <div class="section-kicker no-print" style="color:var(--navy-primary);">SMART FOOD ANALYSIS SYSTEM • A–D ЕСЕП</div>
      <h2 class="no-print" style="margin:0 0 8px;">Біріктірілген өнім карточкасы және динамика</h2>

      <div class="card no-print" style="margin-bottom:12px;">
        <label style="font-size:12px; font-weight:700;">Зерттелетін өнімді таңдаңыз:
          <select onchange="window._v2SelectUnifiedProduct(this.value)">
            ${productNames.length === 0
              ? `<option value="">Өнімдер әзірге жоқ</option>`
              : productNames.map((n) => `<option value="${esc(n)}" ${n === targetName ? 'selected' : ''}>${esc(n)}</option>`).join('')}
          </select>
        </label>
        <div style="font-size:11px; color:#64748B; margin-top:6px;">
          Өлшенбеген параметрлер автоматты түрде толықтырылмайды — барлық дереккөз нақты көрсетіледі.
        </div>
      </div>

      <div class="card no-print" style="margin-bottom:12px; border-color:#A7F3D0;">
        <div class="section-kicker" style="margin-top:0; color:var(--green);">МЕКТЕП ЖОБАСЫНА PDF ЭКСПОРТТАУ</div>
        <div class="btn-grid-2">
          <label class="field-sm" style="margin-top:0;">Оқушы аты-жөні / Сыныбы:
            <input id="pdf-author-input" type="text" value="${esc(v2State.pdfStudentAuthor)}" oninput="window._v2UpdatePdfMeta('author', this.value)" placeholder="Мысалы: Қанағат Айым • 8-сынып" />
          </label>
          <label class="field-sm" style="margin-top:0;">Ғылыми жоба тақырыбы:
            <input id="pdf-project-input" type="text" value="${esc(v2State.pdfProjectTitle)}" oninput="window._v2UpdatePdfMeta('title', this.value)" />
          </label>
        </div>
        <button type="button" class="btn-primary btn-navy full-width" style="margin-top:10px;" onclick="exportUnifiedReportPdf()">
          📄 PDF ретінде сақтау / Басып шығару
        </button>
        <div style="font-size:11px; color:#475569; margin-top:6px;">
          Android Chrome: «PDF ретінде сақтау» таңдаңыз. iPhone Safari: Басып шығару көрінісінде «Бөлісу» (Share) → «Файлдарға сақтау» (Save to Files) басыңыз.
        </div>
      </div>

      <!-- SECTION A: FOOD COMPOSITION -->
      <div class="card report-section">
        <div class="report-head">
          <strong>A. Қоректік құрам (FOOD COMPOSITION)</strong>
          <span class="source-pill">${ocrMatch ? SOURCE_TAGS.OCR : SOURCE_TAGS.NONE}</span>
        </div>
        ${ocrMatch ? `
          <div style="font-size:12px; color:#475569; margin-bottom:6px;">Өлшем негізі: <strong>${esc(ocrMatch.servingSizeText)}</strong></div>
          <div style="display:grid; grid-template-columns:repeat(5,1fr); gap:4px; text-align:center; font-size:11px; background:#F8FAFC; padding:8px; border-radius:8px;">
            <div>Ккал<br/><strong>${ocrMatch.calories ?? 'Дерек жоқ'}</strong></div>
            <div>Қант<br/><strong>${ocrMatch.sugar != null ? ocrMatch.sugar + 'г' : 'Дерек жоқ'}</strong></div>
            <div>Май<br/><strong>${ocrMatch.fat != null ? ocrMatch.fat + 'г' : 'Дерек жоқ'}</strong></div>
            <div>Ақуыз<br/><strong>${ocrMatch.protein != null ? ocrMatch.protein + 'г' : 'Дерек жоқ'}</strong></div>
            <div>Тұз<br/><strong>${ocrMatch.salt != null ? ocrMatch.salt + 'г' : 'Дерек жоқ'}</strong></div>
          </div>
          ${ocrMatch.ingredients ? `<div style="font-size:12px; color:#334155; margin-top:6px;"><strong>Ингредиенттер:</strong> ${esc(ocrMatch.ingredients)}</div>` : ''}
        ` : `<div style="font-size:13px; color:#64748B;">Дерек жоқ. <a href="#" onclick="navigateTo('scanner'); return false;">Құрамын сканерлеу →</a></div>`}
      </div>

      <!-- SECTION B: VISUAL ANALYSIS -->
      <div class="card report-section">
        <div class="report-head">
          <strong>B. Визуалды талдау (FRESHNESS VISION)</strong>
          <span class="source-pill">${latestVision ? esc(latestVision.dataSource) : SOURCE_TAGS.NONE}</span>
        </div>
        ${latestVision ? `
          <div style="font-size:12px; line-height:1.55;">
            <div><strong>Мәртебе:</strong> ${esc(latestVision.statusLabel)}</div>
            <div><strong>Сипаттама:</strong> ${esc(latestVision.surfaceDescription)}</div>
            <div><strong>Өзгерістер:</strong> ${esc(latestVision.observedChanges)}</div>
            <div style="color:#64748B;"><strong>Күні:</strong> ${esc(latestVision.timestamp)}</div>
          </div>
        ` : `<div style="font-size:13px; color:#64748B;">Дерек жоқ. <a href="#" onclick="navigateTo('freshness'); return false;">Балғындықты талдау →</a></div>`}
      </div>

      <!-- SECTION C: CHEMICAL ANALYSIS -->
      <div class="card report-section">
        <div class="report-head">
          <strong>C. Химиялық талдау (pH)</strong>
          <span class="source-pill">${latestPh ? esc(latestPh.dataSource) : SOURCE_TAGS.NONE}</span>
        </div>
        ${latestPh ? `
          <div style="font-size:12px; line-height:1.55;">
            <div><strong>Үлгі түрі:</strong> ${esc(latestPh.sampleType)}</div>
            <div><strong>Индикатор түсі:</strong> ${esc(latestPh.paperColor)}</div>
            <div><strong>Шамамен pH:</strong> ${esc(latestPh.estimatedPhRange)}</div>
            <div><strong>Түсіндірме:</strong> ${esc(latestPh.acidityExplanation)}</div>
            <div style="color:#64748B;"><strong>Күні:</strong> ${esc(latestPh.timestamp)}</div>
          </div>
        ` : `<div style="font-size:13px; color:#64748B;">Дерек жоқ. <a href="#" onclick="navigateTo('chemical'); return false;">Химиялық pH талдау →</a></div>`}
      </div>

      <!-- SECTION D: STORAGE CONDITIONS -->
      <div class="card report-section">
        <div class="report-head">
          <strong>D. Сақтау жағдайы (STORAGE MONITOR)</strong>
          <span class="source-pill">${latestStorage ? esc(latestStorage.dataSource) : SOURCE_TAGS.NONE}</span>
        </div>
        ${latestStorage ? `
          <div style="display:grid; grid-template-columns:repeat(3,1fr); gap:6px; font-size:11px; background:#F8FAFC; padding:8px; border-radius:8px;">
            <div>Бөлме t°<br/><strong>${latestStorage.roomTempC ?? 'Өлшенбеген'}</strong></div>
            <div>Тоңазытқыш t°<br/><strong>${latestStorage.fridgeTempC ?? 'Өлшенбеген'}</strong></div>
            <div>Ылғалдылық<br/><strong>${latestStorage.humidityPct != null ? latestStorage.humidityPct + '%' : 'Өлшенбеген'}</strong></div>
            <div>Жарық<br/><strong>${esc(latestStorage.lightCondition)}</strong></div>
            <div>Орын<br/><strong>${esc(latestStorage.storageLocation)}</strong></div>
            <div>Ұзақтығы<br/><strong>${esc(latestStorage.duration || 'Дерек жоқ')}</strong></div>
          </div>
          <div style="font-size:11px; color:#64748B; margin-top:6px;">Өлшенген уақыты: ${esc(latestStorage.timestamp)}</div>
        ` : `<div style="font-size:13px; color:#64748B;">Дерек жоқ. <a href="#" onclick="navigateTo('storage'); return false;">Сақтау жағдайын енгізу →</a></div>`}
      </div>

      <!-- MULTI-DAY COMPARISON & GRAPHS -->
      <div class="section-kicker">БІР ӨНІМДІ КҮНДЕР БОЙЫНША САЛЫСТЫРУ</div>
      <div class="warning-box">
        <strong>ТҮС САЛЫСТЫРУ ЕСКЕРТУІ:</strong> Сурет жарығы әртүрлі болса, түс салыстыру нәтижесінің дәлдігі төмендеуі мүмкін.
      </div>

      <div class="card" style="margin-bottom:12px;">
        <strong style="font-size:13px;">Суреттер мен сыртқы белгілер динамикасы (${visionMatches.length})</strong>
        ${visionMatches.length === 0
          ? `<p style="font-size:12px; color:#64748B; margin:6px 0 0;">Бұл өнім бойынша әр күнгі фотосуреттер әлі қосылмаған.</p>`
          : `<div class="photo-compare-grid" style="margin-top:8px;">
              ${visionMatches.map((v) => `
                <div class="photo-compare-item">
                  ${v.imageBase64 ? `<img src="${v.imageBase64}" alt="${esc(v.timestamp)}" />` : ''}
                  <div style="font-size:10px; color:#64748B; margin-top:4px;">${esc(v.timestamp)}</div>
                  <div style="font-size:11px; font-weight:700;">${esc(v.statusLabel)}</div>
                  <div style="font-size:11px; color:#334155;">${esc(v.observedChanges)}</div>
                </div>
              `).join('')}
            </div>`}
      </div>

      <div class="card" style="margin-bottom:12px;">
        <strong style="font-size:13px; display:block; margin-bottom:6px;">pH өзгерісінің графигі (күндер бойынша)</strong>
        ${renderMiniSvgChart(phPoints, '#8B5CF6', '', 2, 12)}
      </div>

      <div class="card" style="margin-bottom:12px;">
        <strong style="font-size:13px; display:block; margin-bottom:6px;">Сақтау температурасының графигі (°C)</strong>
        ${renderMiniSvgChart(tempPoints, '#0284C7', '°', -5, 35)}
      </div>

      <div class="card">
        <strong style="font-size:13px;">Өлшемдердің жиынтық кестесі</strong>
        <table class="comp-table" style="margin-top:8px; font-size:11px;">
          <thead><tr><th>Күні/уақыты</th><th>Бөлім</th><th>Нәтиже</th><th>Дереккөз</th></tr></thead>
          <tbody>
            ${visionMatches.map((v) => `<tr><td>${esc(v.timestamp)}</td><td>Визуалды</td><td>${esc(v.statusLabel)}</td><td>${esc(v.dataSource)}</td></tr>`).join('')}
            ${phMatches.map((p) => `<tr><td>${esc(p.timestamp)}</td><td>pH</td><td>${esc(p.estimatedPhRange)}</td><td>${esc(p.dataSource)}</td></tr>`).join('')}
            ${storageMatches.map((s) => `<tr><td>${esc(s.timestamp)}</td><td>Сақтау</td><td>Бөлме: ${s.roomTempC ?? '—'}°C / Тоң: ${s.fridgeTempC ?? '—'}°C</td><td>${esc(s.dataSource)}</td></tr>`).join('')}
            ${visionMatches.length + phMatches.length + storageMatches.length === 0
              ? `<tr><td colspan="4" style="color:#64748B;">Таңдалған өнім бойынша өлшемдер әлі жоқ.</td></tr>`
              : ''}
          </tbody>
        </table>
      </div>
    `;
  }

  window._v2ClearVision = function () {
    v2State.visionImage = null;
    v2State.visionDraft = null;
    v2State.visionError = null;
    window.render();
  };

  window._v2ClearPh = function () {
    v2State.phImage = null;
    v2State.phDraft = null;
    v2State.phError = null;
    window.render();
  };

  window._v2SelectUnifiedProduct = function (val) {
    v2State.selectedUnifiedProduct = val;
    window.render();
  };

  window._v2UpdatePdfMeta = function (field, val) {
    if (field === 'author') {
      v2State.pdfStudentAuthor = val;
      try { localStorage.setItem('foodscan20_pdf_author', val); } catch {}
      const authorEl = document.getElementById('print-header-author');
      if (authorEl) authorEl.textContent = val || 'Көрсетілмеген';
    } else if (field === 'title') {
      v2State.pdfProjectTitle = val;
      try { localStorage.setItem('foodscan20_pdf_project_title', val); } catch {}
      const headerTitle = document.getElementById('print-header-title');
      if (headerTitle) headerTitle.textContent = val || 'Мектеп ғылыми жобасы: Біріктірілген өнім есебі (A–D)';
    }
  };

  window.exportUnifiedReportPdf = function () {
    const prevTitle = document.title;
    const cleanProduct = (v2State.selectedUnifiedProduct || 'Onim')
      .trim()
      .replace(/\s+/g, '_')
      .slice(0, 32);
    document.title = `FOODSCAN_AI_2.0_${cleanProduct}_Esep`;
    try {
      window.print();
    } finally {
      setTimeout(() => {
        document.title = prevTitle;
      }, 600);
    }
  };

  window.FoodScan20 = {
    renderFreshnessVisionView,
    renderChemicalPhView,
    renderStorageMonitorView,
    renderUnifiedReportView,
    setSelectedProduct(name) {
      if (name) v2State.selectedUnifiedProduct = name;
    }
  };
})();
