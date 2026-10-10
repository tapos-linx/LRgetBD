# LRgetBD — Free-First Native Android Land Record Downloader

[![Download APK Directly](https://img.shields.io/badge/📥_Direct_Download-LRgetBD.apk_(23MB)-brightgreen?style=for-the-badge&logo=android)](https://github.com/tapos-linx/LRgetBD/raw/main/apk/LRgetBD.apk)
[![GitHub Repository](https://img.shields.io/badge/GitHub-tapos--linx%2FLRgetBD-blue?style=for-the-badge&logo=github)](https://github.com/tapos-linx/LRgetBD)

---

## 📲 ১-ক্লিকে ফোনে এপিকে ডাউনলোড ও ইনস্টল (1-Click APK Download)

আপনার অ্যান্ড্রয়েড ফোনে সরাসরি কোনো ঝামেলা বা ৪০৪ এরর ছাড়াই অ্যাপটি ডাউনলোড করতে নিচের লিঙ্কে ট্যাপ করুন:

### 🟢 ১. সরাসরি ডাইরেক্ট ডাউনলোড লিঙ্ক (১০০% কার্যকর - No 404):
👉 **[📲 এখানে ক্লিক করে LRgetBD.apk ডাউনলোড করুন (Direct Raw Download)](https://github.com/tapos-linx/LRgetBD/raw/main/apk/LRgetBD.apk)**

> 💡 **নোট:** এই লিঙ্কটি সরাসরি গিটহাব রিপোজিটরি থেকে মূল এপিকে ফাইলটি আপনার ফোনের ডাউনলোড ফোল্ডারে নামিয়ে দেয়। কোনো রিলিজ তৈরির অপেক্ষা করতে হয় না।

### 🌐 ২. গিটহাব ফাইল ভিউয়ার লিঙ্ক (বিকল্প):
👉 **[গিটহাবে এপিকে ফাইল পেজ ওপেন করুন (View in GitHub & Download)](https://github.com/tapos-linx/LRgetBD/blob/main/apk/LRgetBD.apk)**  
*(পেজটি ওপেন হলে স্ক্রিনের **"Download"** বাটনে চাপুন)*

### 📦 ৩. গিটহাব রিলিজ পেজ (GitHub Releases):
👉 **[সকল রিলিজ ও সংস্করণ দেখুন](https://github.com/tapos-linx/LRgetBD/releases)**

---

### 📱 ফোনে ইনস্টল করার সহজ নিয়ম (Step-by-Step Installation):
1. উপরের **[ডাইরেক্ট ডাউনলোড লিঙ্ক (LRgetBD.apk)](https://github.com/tapos-linx/LRgetBD/raw/main/apk/LRgetBD.apk)** এ ট্যাপ করুন।
2. ডাউনলোড শেষ হলে নোটিফিকেশন থেকে ফাইলে ক্লিক করুন অথবা ফোনের **Files / Downloads** ফোল্ডারে গিয়ে `LRgetBD.apk` ওপেন করুন।
3. যদি অ্যান্ড্রয়েড **"Install unknown apps"** সতর্কবার্তা দেখায়:
   - **Settings** এ ক্লিক করুন।
   - ব্রাউজারের জন্য **"Allow from this source"** অপশনটি অন (Enable) করে দিন।
4. **Install** বাটনে চাপুন।
5. ইনস্টলেশন সম্পন্ন হলে **Open** চাপুন এবং **LRgetBD** ব্যবহার শুরু করুন!
6. অ্যাপের **সেটিংস (Settings)** এ গিয়ে আপনার পছন্দের অফলাইন সংগ্রহ ফোল্ডার (Storage Folder) নির্বাচন করে নিন।

---

## 📋 প্রজেক্ট পরিচিতি ও বৈশিষ্ট্য (Core Features)

LRgetBD হলো একটি নেটিভ অ্যান্ড্রয়েড অ্যাপ্লিকেশন (Kotlin, Jetpack Compose, Material 3, Room, WorkManager) যা বাংলাদেশের ভূমি রেকর্ড (খতিয়ান ও মৌজা ম্যাপ) সুসংগঠিতভাবে সংরক্ষণ, ডাউনলোড ও যাচাইকরণের জন্য তৈরি।

- **বাংলা ইন্টারফেস (Bangla-First):** সমস্ত এলাকা নির্বাচন, স্ট্যাটাস ব্যাজ ও নোটিফিকেশন বাংলায়।
- **০ থেকে ১০০% ডাউনলোড মিটার ও স্ট্যাটাস কার্ড (0-100% Download Meter):**
  - লাইভ প্রগ্রেস বার এবং রিয়েল-টাইম স্টেজ ট্র্যাকার।
  - ৪টি স্পষ্ট পর্যায়: **অনুরোধ (Pending) ➔ প্রক্রিয়া (Processing) ➔ ডাউনলোড (Ready to Download) ➔ প্রস্তুত ও সংরক্ষিত (Ready & Saved)**।
  - ফাইল ওপেন ও শেয়ার করার সরাসরি অপশন।
- **প্রশাসনিক ক্যাসকেডিং নির্বাচন (Cascading Selection):**
  - বিভাগ → জেলা → উপজেলা → মৌজা ধারাবাহিক ফিল্টারিং।
  - উপজেলা নির্বাচন করলে সংশ্লিষ্ট সমস্ত মৌজা স্বয়ংক্রিয়ভাবে নির্বাচিত হয়।
  - সবগুলো নির্বাচন (Select All) ও বাদ দেওয়ার (Deselect All) সুবিধা।
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
বিল্ড আউটপুট লোকেশন: `apk/LRgetBD.apk`

---

## 📄 লাইসেন্স ও নীতিমালা
LRgetBD সম্পূর্ণ ফ্রি-ফার্স্ট (Free-First) আর্কিটেকচারে তৈরি। আপনার সংরক্ষিত সকল ফাইল সম্পূর্ণ নিরাপদভাবে আপনার নিজস্ব ফোনেই সংরক্ষিত থাকে।
