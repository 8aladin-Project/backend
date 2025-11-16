importScripts('https://www.gstatic.com/firebasejs/9.6.11/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/9.6.11/firebase-messaging-compat.js');

let messagingInstance = null;

self.addEventListener('message', (event) => {
  const { data } = event;
  if (!data || data.type !== 'INIT_FIREBASE_MESSAGING') {
    return;
  }

  if (messagingInstance) {
    return;
  }

  firebase.initializeApp(data.config);
  messagingInstance = firebase.messaging();
});

self.addEventListener('push', (event) => {
  if (!event.data) {
    return;
  }

  try {
    const payload = event.data.json();
    const title = payload.notification?.title || '새 알림';
    const options = {
      body: payload.notification?.body || '',
      icon: payload.notification?.icon,
      data: payload.data,
    };

    event.waitUntil(self.registration.showNotification(title, options));
  } catch (error) {
    console.error('푸시 메시지 처리 중 오류', error);
  }
});

