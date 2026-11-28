# BATid

**A Department-Based Mobile Announcement and Alert System for the College of Informatics and Computing Sciences (CICS)**

*Stay BATid, stay informed.*

BATid is an Android app that gives CICS students, faculty, student council officers and administrators of Batangas State University (BatStateU), The National Engineering University – Alangilan Campus, one official place for departmental announcements and alerts.

> Final project for **CS 312: Mobile Computing**, 1st Semester AY 2026–2027, Group 15, CS-3101.

---

## Project Description

Authorized publishers post categorized announcements such as class suspensions, room changes, schedule adjustments and events, with an optional link and image. Students receive them on their phones, filtered by their **program** (BSCS or BSIT) and **year level**, and can search, filter and save the announcements they need. New announcements are delivered as push notifications, and emergency alerts always notify.

## Problem

CICS students often miss important departmental announcements (class suspensions, room changes, schedule adjustments, academic activities). These are spread across emails, Google chats, Messenger conversations and other channels, so students overlook updates or cannot find announcements that were shared earlier. The lack of a centralized platform causes confusion, delayed information and missed updates.

BATid solves this with one mobile app for official CICS announcements.

## Objectives

**General:** To develop a mobile-based announcement and alert system for CICS that improves the accessibility and delivery of departmental information.

**Specific:**
1. Implement a centralized platform for publishing and managing categorized departmental announcements.
2. Provide program-based and year-level filtering so announcements reach the relevant audience.
3. Implement push notifications for timely delivery of important announcements and emergency alerts.

## Features

**Students**
- Sign up with a school email (`@g.batstate-u.edu.ph`) and verify it through the link sent by email, or use **Continue with Google** with the school account (no password, no verification step)
- Home feed filtered by program and year level, updated in real time, with emergency announcements shown first
- Category filters: All, Academics, Events, General
- Search with filters, and a Saved tab for bookmarked announcements
- Announcement details with an optional **Open link** button and an optional **image** (tap to zoom)
- Push notifications that open the announcement when tapped, plus an in-app notification list (bell)
- Notification preferences per account (Emergency alerts are always on); year level can be changed in Profile
- "No internet connection" banner, previously loaded announcements stay readable offline (Firestore cache), and the app opens offline with the saved profile
- Menu with Help & FAQ, About BATid, Contact / Report a problem and Privacy note

**Student Council**
- Same as students, plus a Council dashboard to post Academics, Events and General announcements
- Cannot send Emergency alerts; can see, edit or delete only their own posts

**Faculty and Administrators**
- Dashboard with the published feed and collapsible Program and Year filters
- Create, edit, delete and manage announcements (drafts and published), with optional link and image
- "Send an emergency alert" flow with quick templates ("Classes suspended", "Room unavailable", "Urgent notice") and a confirmation step
- Faculty can see, edit or delete only their own posts; administrators manage all posts, with filters by author (Mine, Faculty, Council), program and year

### Categories

| Category | Purpose |
|---|---|
| Emergency | Class suspensions and urgent notices, always notifies |
| Academics | Room changes, schedule adjustments, academic reminders |
| Events | College activities and registrations |
| General | Other departmental updates |

### Roles

| Role | How the account is created |
|---|---|
| Student | Normal sign up or Continue with Google |
| Student Council | Sign up (or Google + Complete profile) with a secret Council code |
| Faculty / Staff | Sign up (or Google + Complete profile) with a secret Faculty code |
| Administrator | Set manually in the Firebase console (never from the app) |

## Mobile Computing Concepts

- **Basic data communication:** TCP/IP and HTTPS/TLS between the app and Firebase
- **Wi-Fi and cellular:** the app works on both and refreshes when the network switches
- **Push vs. polling:** FCM push notifications and Firestore real-time listeners are used instead of periodic polling to save battery and meet the 10-second delivery target
- **Mobile constraints addressed:** limited bandwidth (lightweight text, images compressed to under 400 KB), intermittent connectivity (offline cache, status banner, queued posts), battery use, Wi-Fi/cellular handoff and security

## Tech Stack

| Layer | Technology |
|---|---|
| Language / IDE | Kotlin, Android Studio |
| UI | Jetpack Compose |
| Authentication | Firebase Authentication (email and password with email verification, Google sign-in through Credential Manager) |
| Database | Cloud Firestore (with offline cache); announcement images stored as compressed Base64 JPEG |
| Push notifications | Firebase Cloud Messaging (FCM), topic-based, data-only messages |
| Push sender | Google Apps Script web app (free), code in `push-sender/Code.gs` |
| Network monitoring | Android ConnectivityManager |
| UI design | Canva |
| Version control | Git and GitHub |

The project uses the free Firebase **Spark** plan. Cloud Functions were replaced by a free Google Apps Script web app (push sender) due to no budget; the FCM topic design is unchanged. Cloud Storage is not used: images are compressed and saved in Firestore.

## How It Works

1. A publisher signs in and writes an announcement, choosing its category, program and year level, with an optional link and image.
2. The announcement is stored in Cloud Firestore (the image in its own `announcementImages` document), and Firestore security rules check the publisher's role.
3. After the post is saved, the app calls the Apps Script push sender. The sender verifies the publisher's Firebase login, checks the role and author, and sends a data-only push message to the matching FCM topics (for example `bscs_3` or `bsit_all`).
4. Students subscribed to those topics receive the notification, and the app applies their notification preferences.
5. Students open BATid (or tap the notification) and read the full announcement, which is also updated live in their feed.

## Project Status

| Component | Status |
|---|---|
| Theme, navigation, student and admin screens, menu | Done |
| Sign up, log in, roles and role codes | Done |
| Email verification and Google sign-in | Done |
| Firestore security rules (users, announcements, images) | Done |
| Announcements in Firestore with offline cache | Done |
| Links and images in announcements | Done |
| Topic subscription, push sender and notification preferences | Done |
| Emergency alert flow with templates | Done |
| Demo content and documentation | In progress |

## Download the App

You can install BATid on any Android phone without Android Studio.

**[Download BATid (APK)](https://drive.google.com/drive/folders/1pAdQR_oVS5Cz3-vPCJEyUU0AhVhi9Czi?usp=sharing)**

1. Open the link on an Android phone (Android 8.0 or newer) and download the `.apk` file.
2. Open the downloaded file. If asked, allow **Install unknown apps** for your browser or Google Drive.
3. If Google Play Protect shows a warning, tap **More details > Install anyway** (the app is not on the Play Store).
4. Open BATid and sign in with your `@g.batstate-u.edu.ph` account, or tap **Continue with Google**.

To update, download the newest APK from the same link. If the install fails, uninstall the old version first.

## Getting Started (for developers)

**Requirements:** Android Studio (latest stable), JDK 17 or newer, an Android phone or emulator with Android 8.0 (API 26) or higher.

1. Clone the repository:
   ```
   git clone <https://github.com/deynblip/BATid.git>
   ```
2. Open the project folder in Android Studio and wait for Gradle sync to finish.
3. Make sure `app/google-services.json` is present (it is included in the repository).
4. Connect a phone with USB debugging on, or start an emulator, then press **Run**.

To test sign up, use a school email ending in `@g.batstate-u.edu.ph`.

**Optional, Google sign-in from your own computer:** Continue with Google only works in builds from computers whose SHA-1 fingerprint is registered in Firebase. Run `.\gradlew signingReport`, add the debug `SHA1` in Firebase (Project settings > Your apps > BATid > Add fingerprint), then download the new `google-services.json`. Email and password sign-in works without this.

Do not upgrade Kotlin, the Android Gradle plugin or the Firebase BoM, and keep `googleid` at 1.1.1 (newer versions need a newer Kotlin).

## Project Structure

```
app/src/main/java/com/batid/app/
├── data/
│   ├── local/          Saved and read announcements (per account)
│   ├── model/          Announcement, UserProfile, enums, notifications
│   ├── network/        Internet connection monitoring
│   ├── remote/         Firebase Authentication and Google sign-in
│   ├── repository/     Announcements and images in Firestore
│   └── sample/         Sample data for previews
├── notifications/      FCM service, topics, push sender, preferences
├── ui/
│   ├── admin/          Dashboard, announcement form, emergency alert, manage
│   ├── auth/           Welcome, log in, create account, verify email, complete profile
│   ├── components/     Shared UI (buttons, banners, announcement image)
│   ├── help/           Menu, Help & FAQ, About, Contact, Privacy
│   ├── navigation/     App routing by role
│   ├── student/        Feed, search, saved, profile, details, notifications
│   └── theme/          Colors and typography
└── util/               Time formatting
push-sender/Code.gs     Google Apps Script push sender (no secrets)
firestore.rules         Firestore security rules
```

## Security Notes

- The Faculty and Council sign-up codes are stored only in Firestore, in documents that apps cannot read. They are never written in the code or in this repository.
- The push sender's service account key is stored only in the Apps Script project's Script Properties.
- Do not commit passwords, service account keys or any secret code.
- Administrator accounts can only be assigned in the Firebase console. Presentation accounts can skip email verification only through a field set in the console (`skipEmailVerification`); the app cannot set it.
- Users can change only their year level; role and program are locked by the security rules.

## Limitations and Future Work

- Faculty and Council codes are shared per role; per-person codes with an expiry date would be safer.
- The APK is a test (debug) build shared by link; a signed release build would need its own SHA-1 fingerprint for Google sign-in.
- Profile pictures and section-level targeting are not included.
- Images are limited to one per announcement (under 400 KB) because Cloud Storage is not used.

## Developers

- Boo, Vianci Mariz
- Banaag, Ace Angela
- Cura, Dane Mercy

**Course:** CS 312 Mobile Computing, Computer Science Department
**School:** College of Informatics and Computing Sciences, Batangas State University – The National Engineering University, Alangilan Campus