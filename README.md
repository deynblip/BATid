# BATid

**A Department-Based Mobile Announcement and Alert System for the College of Informatics and Computing Sciences (CICS)**

*Stay BATid, stay informed.*

BATid is an Android app that gives CICS students, faculty, student council officers and administrators of Batangas State University (BatStateU), The National Engineering University – Alangilan Campus, one official place for departmental announcements and alerts.

> Final project for **CS 312: Mobile Computing**, 1st Semester AY 2026–2027, Group 15, CS-3101.

---

## Project Description

Authorized publishers post categorized announcements such as class suspensions, room changes, schedule adjustments and events. Students receive them on their phones, filtered by their **program** (BSCS or BSIT) and **year level**, and can search, filter and save the announcements they need. Urgent announcements are delivered as push notifications.

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
- Sign up and log in with a school email (`@g.batstate-u.edu.ph`)
- Home feed filtered by program and year level, with emergency announcements shown first
- Category filters: All, Academics, Events, General
- Search with filters, and a Saved tab for bookmarked announcements
- Notification preferences (Emergency alerts are always on)
- "No internet connection" banner and automatic refresh when the network changes
- Previously loaded announcements stay readable offline (Firestore cache)

**Student Council**
- Same as students, plus a Council dashboard to post Academics, Events and General announcements
- Cannot send Emergency alerts; can edit or delete only their own posts

**Faculty and Administrators**
- Dashboard to create, edit, delete and manage announcements (drafts and published)
- "Send an emergency alert" flow with quick templates and a confirmation step
- Faculty can edit or delete only their own posts; administrators can manage all posts

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
| Student | Normal sign up |
| Student Council | Sign up with a secret Council code |
| Faculty / Staff | Sign up with a secret Faculty code |
| Administrator | Set manually in the Firebase console (never from the app) |

## Mobile Computing Concepts

- **Basic data communication:** TCP/IP and HTTPS/TLS between the app and Firebase
- **Wi-Fi and cellular:** the app works on both and refreshes when the network switches
- **Push vs. polling:** FCM push notifications are used instead of periodic polling to save battery and meet the 10-second delivery target
- **Mobile constraints addressed:** limited bandwidth (lightweight text content), intermittent connectivity (offline cache, status banner), battery use, Wi-Fi/cellular handoff and security

## Tech Stack

| Layer | Technology |
|---|---|
| Language / IDE | Kotlin, Android Studio |
| UI | Jetpack Compose |
| Authentication | Firebase Authentication (email and password) |
| Database | Cloud Firestore (with offline cache) |
| Push notifications | Firebase Cloud Messaging (FCM), topic-based |
| Push sender | Small free external sender (Cloudflare Worker or Google Apps Script) |
| Network monitoring | Android ConnectivityManager |
| UI design | Canva |
| Version control | Git and GitHub |

The project uses the free Firebase **Spark** plan. Cloud Functions were replaced by the free external sender due to no budget; the FCM topic design is unchanged.

## How It Works

1. A publisher signs in and writes an announcement, choosing its category, program and year level.
2. The announcement is stored in Cloud Firestore, and Firestore security rules check the publisher's role.
3. The notification sender sends a data-only push message to the matching FCM topics (for example `bscs_3` or `bsit_all`).
4. Students subscribed to those topics receive the notification, and the app applies their notification preferences.
5. Students open BATid and read the full announcement from Firestore.

## Project Status

| Component | Status |
|---|---|
| Theme, navigation, student and admin screens | Done (interface) |
| Sign up, log in, roles and role codes | Done |
| Firestore security rules for users | Done |
| Push message receiving and notification preferences | Done (code) |
| Announcements in Firestore with offline cache | In progress |
| Firestore rules for announcements | Planned |
| Topic subscription and push sender | Planned |
| Google sign-in | Postponed |

## Getting Started

**Requirements:** Android Studio (latest stable), JDK 17 or newer, an Android phone or emulator with Android 8.0 (API 26) or higher.

1. Clone the repository:
   ```
   git clone <repository-url>
   ```
2. Open the project folder in Android Studio and wait for Gradle sync to finish.
3. Make sure `app/google-services.json` is present (it is included in the repository).
4. Connect a phone with USB debugging on, or start an emulator, then press **Run**.

To test sign up, use a school email ending in `@g.batstate-u.edu.ph`.

## Project Structure

```
app/src/main/java/com/batid/app/
├── data/
│   ├── model/        Announcement, UserProfile, enums
│   ├── remote/       Firebase Authentication code
│   ├── repository/   Announcement data access
│   └── sample/       Temporary sample data
└── ui/
    ├── auth/         Welcome, log in, create account
    ├── navigation/   App routing by role
    ├── student/      Feed, search, saved, profile, details
    └── admin/        Dashboard, new announcement, manage
```

## Security Notes

- The Faculty and Council sign-up codes are stored only in Firestore, in documents that apps cannot read. They are never written in the code or in this repository.
- Do not commit passwords, service account keys or any secret code.
- Administrator accounts can only be assigned in the Firebase console.

## Developers

- Boo, Vianci Mariz
- Banaag, Ace Angela
- Cura, Dane Mercy

**Course:** CS 312 Mobile Computing, Computer Science Department
**School:** College of Informatics and Computing Sciences, Batangas State University – The National Engineering University, Alangilan Campus
