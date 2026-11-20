const CACHE_NAME = 'foodscan-v1';

const CORE_ASSETS = [
  './',
  './index.html',
  './styles.css',
  './app.js',
  './modules20.js',
  './manifest.json',
  './icons/icon-192.png',
  './icons/icon-512.png',
  './icons/apple-touch-icon.png'
];

// Install: cache index.html, styles.css, app.js, manifest, and icons in 'foodscan-v1'
self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then(async (cache) => {
      await Promise.all(
        CORE_ASSETS.map(async (assetUrl) => {
          try {
            await cache.add(new Request(assetUrl, { cache: 'reload' }));
          } catch {
            try {
              await cache.add(assetUrl);
            } catch {
              // Ignore optional path failures so core shell assets remain cached
            }
          }
        })
      );
    })
  );
  self.skipWaiting();
});

// Activate: remove old caches and immediately claim active clients
self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((keys) =>
      Promise.all(
        keys
          .filter((key) => key !== CACHE_NAME)
          .map((key) => caches.delete(key))
      )
    )
  );
  self.clients.claim();
});

self.addEventListener('message', (event) => {
  if (event.data && event.data.type === 'SKIP_WAITING') {
    self.skipWaiting();
  }
});

// Fetch: cache-first strategy using 'foodscan-v1' (excluding /api/ calls)
self.addEventListener('fetch', (event) => {
  const { request } = event;
  const url = new URL(request.url);

  // Never cache AI API proxy endpoints or non-GET requests
  if (url.pathname.includes('/api/') || request.method !== 'GET') {
    event.respondWith(
      fetch(request).catch(
        () =>
          new Response(
            JSON.stringify({
              error: 'Офлайн режим: интернет байланысы жоқ. Сақталған зерттеулерді қарау қолжетімді.',
              offline: true,
              fallback: true
            }),
            {
              status: 503,
              headers: { 'Content-Type': 'application/json; charset=utf-8' }
            }
          )
      )
    );
    return;
  }

  // Only intercept same-origin HTTP(S) requests
  if (url.origin !== self.location.origin) {
    return;
  }

  // Cache-first lookup in 'foodscan-v1'
  event.respondWith(
    caches.open(CACHE_NAME).then(async (cache) => {
      const cachedResponse = await cache.match(request, { ignoreSearch: true });
      if (cachedResponse) {
        return cachedResponse;
      }

      try {
        const networkResponse = await fetch(request);
        if (networkResponse && networkResponse.status === 200 && networkResponse.type === 'basic') {
          cache.put(request, networkResponse.clone());
        }
        return networkResponse;
      } catch (err) {
        if (request.mode === 'navigate') {
          return (
            (await cache.match('./index.html', { ignoreSearch: true })) ||
            (await cache.match('./', { ignoreSearch: true }))
          );
        }
        throw err;
      }
    })
  );
});
