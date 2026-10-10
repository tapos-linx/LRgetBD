# LRgetBD — Free-First Native Android Land Record Downloader

[![Build & Release](https://github.com/tapos-linx/LRgetBD/actions/workflows/android.yml/badge.svg)](https://github.com/tapos-linx/LRgetBD/actions)
[![Download APK](https://img.shields.io/badge/Download_APK-LRgetBD_Latest-brightgreen?style=for-the-badge&logo=android)](https://github.com/tapos-linx/LRgetBD/releases/latest/download/LRgetBD-latest.apk)
[![GitHub Releases](https://img.shields.io/badge/GitHub-Releases-blue?style=for-the-badge&logo=github)](https://github.com/tapos-linx/LRgetBD/releases)

---

## 📲 ১-ক্লিকে ফোনে এপিকে ডাউনলোড ও ইনস্টল (1-Click APK Download)

যেকোনো অ্যান্ড্রয়েড ফোনে সরাসরি অ্যাপটি ডাউনলোড এবং ইনস্টল করতে নিচের বাটনে বা লিঙ্কে ক্লিক করুন:

### 📥 সরাসরি ডাউনলোড লিংক:
1. 👉 **[LRgetBD-latest.apk ডাউনলোড করুন (সরাসরি ডাউনলোড)](https://github.com/tapos-linx/LRgetBD/releases/latest/download/LRgetBD-latest.apk)**
2. 🔄 **[বিকল্প ডাউনলোড লিংক (app-debug.apk)](https://github.com/tapos-linx/LRgetBD/releases/latest/download/app-debug.apk)**
3. 📦 **[সকল রিলিজ ও সংস্করণ দেখুন (GitHub Releases)](https://github.com/tapos-linx/LRgetBD/releases)**

---

### 📱 ফোনে ইনস্টল করার সহজ নিয়ম (Installation Steps):
1. আপনার ফোনের ব্রাউজার (Chrome, Firefox বা অন্য ব্রাউজার) দিয়ে উপরের **[LRgetBD-latest.apk ডাউনলোড লিংক](https://github.com/tapos-linx/LRgetBD/releases/latest/download/LRgetBD-latest.apk)**-এ ট্যাপ করুন।
2. ডাউনলোড শেষ হলে নোটিফিকেশন থেকে ফাইলে ক্লিক করুন অথবা ফোনের **Files / Downloads** ফোল্ডারে গিয়ে `LRgetBD-latest.apk` ওপেন করুন।
3. যদি অ্যান্ড্রয়েড **"Install unknown apps"** সতর্কবার্তা দেখায়:
   - **Settings** এ ক্লিক করুন।
   - **"Allow from this source"** অপশনটি অন (Enable) করে দিন।
4. **Install** বাটনে চাপুন।
5. ইনস্টলেশন সম্পন্ন হলে **Open** চাপুন এবং **LRgetBD** ব্যবহার শুরু করুন!
6. অ্যাপের **সেটিংস (Settings)** এ গিয়ে আপনার পছন্দের সংরক্ষণ ফোল্ডার (Storage Folder) নির্বাচন করে নিন।

---

## 📋 প্রজেক্ট পরিচিতি ও বৈশিষ্ট্য (Core Features)

LRgetBD হলো একটি নেটিভ অ্যান্ড্রয়েড অ্যাপ্লিকেশন (Kotlin, Jetpack Compose, Material 3, Room, WorkManager) যা বাংলাদেশের ভূমি রেকর্ড (খতিয়ান ও মৌজা ম্যাপ) সুসংগঠিতভাবে সংরক্ষণ, ডাউনলোড ও যাচাইকরণের জন্য তৈরি।

- **বাংলা ইন্টারফেস (Bangla-First):** সমস্ত এলাকা নির্বাচন, স্ট্যাটাস ব্যাজ ও নোটিফিকেশন বাংলায়।
- **প্রশাসনিক ক্যাসকেডিং নির্বাচন (Cascading Selection):**
  - বিভাগ → জেলা → উপজেলা → মৌজা ধারাবাহিক ফিল্টারিং।
  - উপজেলা নির্বাচন করলে সংশ্লিষ্ট সমস্ত মৌজা স্বয়ংক্রিয়ভাবে নির্বাচিত হয়।
  - সবগুলো নির্বাচন (Select All) ও বাদ দেওয়ার (Deselect All) সুবিধা।
  - ০, ১, ৭, ৮ এবং ২০০+ মৌজার জন্য স্কেলেবল আর্কিটেকচার।
- **জরিপ ও রেকর্ড ধরণ:** সিএস (CS), এসএ (SA), আরএস (RS), বিএস (BS), বিআরএস (BRS) খতিয়ান ও নকশা।
- **কার্যসারি ব্যবস্থাপনা (Persistent Queue):**
  - ডিফল্ট ব্যাচ সাইজ: **৭টি মৌজা প্রতি ব্যাচে**।
  - স্থগিত (Pause), পুনরায় চালু (Resume), বাতিল (Cancel) এবং ব্যর্থগুলো পুনরায় চেষ্টা (Retry Failed)।
- **নিরাপদ পিডিএফ ভ্যালিডেশন ইঞ্জিন (PDF Integrity Engine):**
  - এইচটিএমএল লগইন/ত্রুটি পৃষ্ঠা স্বয়ংক্রিয়ভাবে প্রত্যাখ্যান।
  - `%PDF-` হেডার এবং `%%EOF` ট্রেইলার যাচাইকরণ।
  - SHA-256 ক্রিপ্টোগ্রাফিক হ্যাশ যাচাই এবং ডুপ্লিকেট সনাক্তকরণ।
- **লোকাল স্টোরেজ ও আর্কাইভ (Storage Access Framework):**
  - ফোল্ডার বিন্যাস: `LRgetBD/Bangladesh/<বিভাগ>/<জেলা>/<উপজেলা>/<মৌজা>/<জরিপ>/<রেকর্ড>/`
  - প্রতিটি ডিরেক্টরিতে `manifest.json` ফাইল তৈরি।
  - এক ক্লিকে সম্পূর্ণ মেটাডাটা **CSV** ও **JSON** এক্সপোর্ট।
- **নিরাপত্তা ও নৈতিকতা (Security & Data Ethics):**
  - কোনো ফেক ডেটা বা কাল্পনিক ডাউনলোড প্রোডাকশনে রিপোর্ট করা হয় না।
  - সরকারি ই-পর্চা পোর্টালে অফিসিয়াল অনুমোদিত এপিআই ছাড়া বাল্ক ডাউনলোড বন্ধ রেখে স্বচ্ছভাবে `SOURCE_UNVERIFIED` প্রদর্শন করে।

---

## 🛠️ বিল্ড ও টেস্টিং (Build & Testing)

### লোকাল টেস্ট চালানো:
```bash
gradle :app:testDebugUnitTest
```

### লোকাল ডিবাগ এপিকে বিল্ড করা:
```bash
gradle :app:assembleDebug
```
বিল্ড আউটপুট লোকেশন: `app/build/outputs/apk/debug/app-debug.apk`

---

## 📄 লাইসেন্স ও নীতিমালা
LRgetBD সম্পূর্ণ ফ্রি-ফার্স্ট (Free-First) আর্কিটেকচারে তৈরি। আপনার সংরক্ষিত সকল ফাইল সম্পূর্ণ নিরাপদভাবে আপনার নিজস্ব ফোনেই সংরক্ষিত থাকে।
