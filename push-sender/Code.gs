/**
 * BATid push sender (free Google Apps Script web app).
 * Replaces Cloud Functions (no budget). The FCM topic design is unchanged.
 *
 * The app calls this after an announcement is PUBLISHED:
 *   POST { idToken, announcementId }
 * This script:
 *   1. checks the Firebase login (ID token),
 *   2. reads users/{uid} and announcements/{id} from Firestore,
 *   3. checks the role / author rules,
 *   4. sends a DATA-ONLY FCM message to each topic (e.g. bscs_3, bsit_all).
 *
 * Secrets live in Project Settings > Script Properties (NEVER in this file):
 *   SERVICE_ACCOUNT_JSON = the whole service account key file
 *   WEB_API_KEY          = Firebase Web API key
 */

const PROJECT_ID = 'batid-c8326';
const YEAR_KEYS = { ALL: 'all', FIRST: '1', SECOND: '2', THIRD: '3', FOURTH: '4' };
const PUBLISHERS = ['FACULTY', 'ADMIN', 'COUNCIL'];

function doGet() {
  return reply_({ ok: true, message: 'BATid sender is running.' });
}

function doPost(e) {
  try {
    const req = JSON.parse(e.postData.contents);
    const announcementId = String(req.announcementId || '');
    if (!announcementId) return reply_({ ok: false, error: 'Missing announcement.' });

    const uid = verifyIdToken_(req.idToken);
    const token = getAccessToken_();

    const user = getDoc_(token, 'users/' + uid);
    const role = str_(user, 'role');
    if (PUBLISHERS.indexOf(role) < 0) return reply_({ ok: false, error: 'Your account cannot send notifications.' });

    const a = getDoc_(token, 'announcements/' + announcementId);
    if (str_(a, 'status') !== 'PUBLISHED') return reply_({ ok: false, error: 'The announcement is not published.' });
    if (str_(a, 'authorId') !== uid && role !== 'ADMIN') return reply_({ ok: false, error: 'This is not your announcement.' });

    const category = str_(a, 'category');
    if (category === 'EMERGENCY' && role !== 'FACULTY' && role !== 'ADMIN') {
      return reply_({ ok: false, error: 'Only faculty and admin can send emergency alerts.' });
    }

    // Send each announcement only once (protects against double taps and retries).
    const cache = CacheService.getScriptCache();
    if (cache.get('sent_' + announcementId)) return reply_({ ok: true, message: 'Already sent.' });

    const yearKey = YEAR_KEYS[str_(a, 'yearLevel')] || 'all';
    const topics = arr_(a, 'programs').map(function (p) { return p.toLowerCase() + '_' + yearKey; });
    if (!topics.length) return reply_({ ok: false, error: 'The announcement has no program.' });

    let body = str_(a, 'body');
    if (body.length > 300) body = body.substring(0, 297) + '...';
    const data = {
      title: str_(a, 'title'),
      body: body,
      category: category,
      announcementId: announcementId
    };
    topics.forEach(function (t) { sendToTopic_(token, t, data); });

    cache.put('sent_' + announcementId, '1', 21600); // 6 hours
    return reply_({ ok: true, topics: topics });
  } catch (err) {
    console.error(err);
    return reply_({ ok: false, error: String((err && err.message) || err) });
  }
}

function testSetup() {
  getAccessToken_();
  console.log('Service account OK');
  sendToTopic_(getAccessToken_(), 'bscs_all', {
    title: 'Sender test',
    body: 'Hello from the BATid sender',
    category: 'ACADEMICS',
    announcementId: ''
  });
  console.log('Test push sent to bscs_all');
}

// ───────────────────────── helpers ─────────────────────────

function prop_(name) {
  const v = PropertiesService.getScriptProperties().getProperty(name);
  if (!v) throw new Error('Sender setup problem: missing script property ' + name + '.');
  return v;
}

/** Checks the Firebase ID token and returns the user's uid. */
function verifyIdToken_(idToken) {
  if (!idToken) throw new Error('Please log in again.');
  const res = UrlFetchApp.fetch(
    'https://identitytoolkit.googleapis.com/v1/accounts:lookup?key=' + prop_('WEB_API_KEY'),
    {
      method: 'post',
      contentType: 'application/json',
      payload: JSON.stringify({ idToken: idToken }),
      muteHttpExceptions: true
    }
  );
  if (res.getResponseCode() !== 200) throw new Error('Please log in again.');
  const users = JSON.parse(res.getContentText()).users || [];
  if (!users.length) throw new Error('Please log in again.');
  return users[0].localId;
}

/** OAuth access token from the service account (cached for 50 minutes). */
function getAccessToken_() {
  const cache = CacheService.getScriptCache();
  const cached = cache.get('access_token');
  if (cached) return cached;

  const sa = JSON.parse(prop_('SERVICE_ACCOUNT_JSON'));
  const now = Math.floor(Date.now() / 1000);
  const header = { alg: 'RS256', typ: 'JWT' };
  const claim = {
    iss: sa.client_email,
    scope: 'https://www.googleapis.com/auth/firebase.messaging https://www.googleapis.com/auth/datastore',
    aud: 'https://oauth2.googleapis.com/token',
    iat: now,
    exp: now + 3600
  };
  const unsigned = b64_(JSON.stringify(header)) + '.' + b64_(JSON.stringify(claim));
  const signature = Utilities.computeRsaSha256Signature(unsigned, sa.private_key);
  const jwt = unsigned + '.' + Utilities.base64EncodeWebSafe(signature).replace(/=+$/, '');

  const res = UrlFetchApp.fetch('https://oauth2.googleapis.com/token', {
    method: 'post',
    payload: { grant_type: 'urn:ietf:params:oauth:grant-type:jwt-bearer', assertion: jwt },
    muteHttpExceptions: true
  });
  if (res.getResponseCode() !== 200) {
    throw new Error('Sender setup problem (service account): ' + res.getContentText().substring(0, 200));
  }
  const accessToken = JSON.parse(res.getContentText()).access_token;
  cache.put('access_token', accessToken, 3000);
  return accessToken;
}

function b64_(text) {
  return Utilities.base64EncodeWebSafe(text, Utilities.Charset.UTF_8).replace(/=+$/, '');
}

/** Reads a Firestore document (REST API) and returns its fields. */
function getDoc_(token, path) {
  const url = 'https://firestore.googleapis.com/v1/projects/' + PROJECT_ID +
    '/databases/(default)/documents/' + path;
  const res = UrlFetchApp.fetch(url, {
    headers: { Authorization: 'Bearer ' + token },
    muteHttpExceptions: true
  });
  if (res.getResponseCode() !== 200) throw new Error('Could not read ' + path.split('/')[0] + '.');
  return JSON.parse(res.getContentText()).fields || {};
}

function str_(fields, key) {
  return fields[key] && fields[key].stringValue ? fields[key].stringValue : '';
}

function arr_(fields, key) {
  const values = fields[key] && fields[key].arrayValue && fields[key].arrayValue.values;
  return values ? values.map(function (v) { return v.stringValue; }) : [];
}

/** Sends one DATA-ONLY message to an FCM topic (HTTP v1 API). */
function sendToTopic_(token, topic, data) {
  const res = UrlFetchApp.fetch('https://fcm.googleapis.com/v1/projects/' + PROJECT_ID + '/messages:send', {
    method: 'post',
    contentType: 'application/json',
    headers: { Authorization: 'Bearer ' + token },
    payload: JSON.stringify({ message: { topic: topic, data: data, android: { priority: 'HIGH' } } }),
    muteHttpExceptions: true
  });
  if (res.getResponseCode() !== 200) {
    throw new Error('Push failed for ' + topic + ': ' + res.getContentText().substring(0, 200));
  }
}

function reply_(obj) {
  return ContentService.createTextOutput(JSON.stringify(obj)).setMimeType(ContentService.MimeType.JSON);
}