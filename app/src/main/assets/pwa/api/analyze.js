/**
 * FOODSCAN AI 2.0 — Server-Side Gemini Vision Proxy (Node.js / Serverless compatible)
 * Keeps GEMINI_API_KEY strictly on the server via process.env.GEMINI_API_KEY.
 * Supports 3 modes:
 *   - composition: Food label OCR & nutrition extraction (FOODSCAN AI 1.0 preserved)
 *   - freshness_vision: External visual freshness appearance analysis (Module 1)
 *   - chemical_ph: Universal pH indicator paper + reference scale comparison (Module 2)
 */
export default async function handler(req, res) {
  if (req.method !== 'POST') {
    return res.status(405).json({ error: 'Method not allowed' });
  }

  const apiKey = process.env.GEMINI_API_KEY;
  if (!apiKey) {
    return res.status(503).json({
      error: 'GEMINI_API_KEY серверде бапталмаған',
      fallback: true
    });
  }

  try {
    const {
      mode = 'composition',
      imageBase64,
      mimeType = 'image/jpeg',
      customText = '',
      productName = '',
      sampleType = ''
    } = req.body || {};

    if (!imageBase64 && !customText) {
      return res.status(400).json({ error: 'Сурет немесе мәтін жіберіңіз' });
    }

    let prompt = '';
    if (mode === 'freshness_vision') {
      prompt = `Сен FOODSCAN AI 2.0 (SMART FOOD ANALYSIS SYSTEM) визуалды тағам талдау жүйесісің.
Қазақ тілінде жауап бер. Тек суреттен көрінетін сыртқы белгілерді ғана сипатта.
ҚАТАҢ ЕРЕЖЕЛЕР:
- Суретте жоқ мәліметті ОЙДАН ШЫҒАРМА. Күмәнді болса «анықтау мүмкін болмады» деп көрсет.
- Тағамның ішкі микробиологиялық қауіпсіздігін, бактерия санын немесе жеуге жарамдылығын кепілдендіретін медициналық/санитарлық қорытынды ЖАСАМА.
- Температураны суреттен анықтадым деп айтпа.
- statusLabel өрісіне ТЕК мына 4 мәртебенің бірін ғана қой:
  1) "Көрінетін өзгерістер аз"
  2) "Сыртқы өзгерістер байқалды"
  3) "Айқын сыртқы өзгерістер байқалды"
  4) "Сурет сапасы талдауға жеткісіз"

${productName ? `Пайдаланушы көрсеткен өнім: ${productName}` : ''}

JSON форматы:
{
  "recognizedFoodName": "string (немесе 'анықтау мүмкін болмады')",
  "surfaceDescription": "string (сыртқы түсі, беткі құрылымы сипаттамасы)",
  "observedChanges": "string (көрінетін қараю, қоңырлану, дақтар мен зақымдану белгілері немесе 'анықтау мүмкін болмады')",
  "comparativeConclusion": "string (тек көрнекі белгілер бойынша қорытынды)",
  "confidenceAndLimits": "string (талдау сенімділігі, жарық/бұрыш шектеулері және бактерия/микробиологиялық қауіпсіздік фотодан анықталмайтыны туралы ескерту)",
  "statusLabel": "Көрінетін өзгерістер аз | Сыртқы өзгерістер байқалды | Айқын сыртқы өзгерістер байқалды | Сурет сапасы талдауға жеткісіз"
}`;
    } else if (mode === 'chemical_ph') {
      prompt = `Сен FOODSCAN AI 2.0 химиялық pH индикатор қағазын талдайтын көмекші жүйесің.
Қазақ тілінде жауап бер.
ТАПСЫРМА:
1. Суретте универсалды pH индикатор қағазы бар-жоғын тексер.
2. Индикатор қағазының реакция түсін анықта.
3. Суретте pH түстер шкаласы бар қаптама көрініп тұр ма, тексер.
4. ЕГЕР шкала көрініп тұрса, қағаз түсін шкаламен салыстырып шамамен pH мәнін немесе аралығын ұсын.
5. ЕГЕР шкала көрінбесе немесе сурет сапасы төмен болса, НАҚТЫ pH САНЫН ОЙДАН ШЫҒАРМА! scaleVisible: false, estimatedPhRange: "анықтау мүмкін болмады" деп қайтар және пайдаланушыдан шкаламен бірге қайта түсіруді сұра.
6. Нәтиже нақты зертханалық pH-метр емес, шамамен бағалау екенін міндетті түрде көрсет.

Зерттелетін өнім: ${productName || 'көрсетілмеген'}
Үлгі түрі: ${sampleType || 'көрсетілмеген'}

JSON форматы:
{
  "paperDetected": boolean,
  "paperColor": "string (немесе 'анықтау мүмкін болмады')",
  "scaleVisible": boolean,
  "estimatedPhRange": "string (мысалы: 'шамамен pH 5.5–6.0' немесе 'анықтау мүмкін болмады')",
  "acidityExplanation": "string (қышқыл, бейтарап немесе сілтілі орта түсіндірмесі, не қайта түсіру нұсқауы)",
  "confidenceLevel": "Жоғары | Орташа | Төмен",
  "errorMarginNote": "string (сурет сапасы мен жарыққа байланысты ықтимал қателік және шамамен бағалау екені)"
}`;
    } else {
      prompt = `Сен FOODSCAN AI 2.0 тағам жапсырмасын талдайтын ғылыми жүйесің.
Қаптамадағы тағамдық құндылық кестесін оқып, тек нақты көрінетін сандарды ғана JSON форматында қайтар.
МАҢЫЗДЫ: Жоқ немесе бұлдыр мәндерді ОЙДАН ШЫҒАРМА, оларға null қой.
Қазақ тілінде жауап бер.

JSON құрылымы:
{
  "productName": "string",
  "category": "string",
  "manufacturer": "string",
  "ingredients": "string",
  "servingSize": "string",
  "basis": "PER_100G | PER_100ML | PER_SERVING",
  "sugar": number | null,
  "salt": number | null,
  "fat": number | null,
  "protein": number | null,
  "calories": number | null,
  "rawLabelText": "string"
}`;
    }

    const parts = [{ text: prompt }];
    if (customText) {
      parts.push({ text: `Қосымша мәтін: ${customText}` });
    }
    if (imageBase64) {
      parts.push({
        inlineData: {
          mimeType,
          data: imageBase64.replace(/^data:image\/\w+;base64,/, '')
        }
      });
    }

    const response = await fetch(
      `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=${apiKey}`,
      {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          contents: [{ parts }],
          generationConfig: { responseMimeType: 'application/json' }
        })
      }
    );

    const data = await response.json();
    const text = data?.candidates?.[0]?.content?.parts?.[0]?.text;
    if (!text) {
      return res.status(502).json({ error: 'AI жауабы бос', fallback: true });
    }

    return res.status(200).json(JSON.parse(text));
  } catch (err) {
    return res.status(500).json({ error: err.message, fallback: true });
  }
}
