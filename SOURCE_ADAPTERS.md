# Source Adapters — LRgetBD

## Overview
LRgetBD uses a modular `SourceAdapter` architecture to mediate access to land record repositories in Bangladesh.

## Source Registry

### 1. `OfficialEporchaAdapter` (ই-পর্চা)
- **Identity:** Bangladesh Land Records & Survey Portal (`eporcha.gov.bd`)
- **Authority:** Ministry of Land, Government of the People's Republic of Bangladesh
- **Record Types:** Khatians (CS, SA, RS, BS, BRS), Mouza Maps
- **Authentication:** Citizen NID/Mobile OTP + CAPTCHA required for citizen requests; authorized API token required for enterprise/bulk access.
- **Bulk Download Status:** `DISABLED / REQUIRES_AUTHORIZED_KEY`
- **Reason:** Public web endpoints are rate-limited and require individual CAPTCHA verification per request. Bulk downloading without authorized API credentials violates portal terms and is blocked.
- **Production Handling:** Reports `SOURCE_UNVERIFIED` with a clear Bangla notification explaining that official API integration is required before automated downloading can proceed.

### 2. `TestMockAdapter` (অফলাইন টেস্ট ফিক্সচার)
- **Identity:** Internal unit-testing fixture
- **Authority:** None (Test harness only)
- **Status:** `TEST_ONLY`
- **Production Handling:** Cannot be called in normal production workflows; explicitly tagged to prevent simulated downloads from masquerading as authentic records.
