# Security & Privacy Policy — LRgetBD

## Free-First and Privacy Principles
1. **Local Storage First:** All records and metadata are stored on the user's Android phone using the Storage Access Framework (SAF) or app-scoped internal storage. No mandatory external cloud or subscription database is utilized.
2. **Zero Credentials in Repository:** Secrets, cookies, tokens, NID numbers, phone numbers, and OTPs are NEVER committed to source control.
3. **No Unofficial Scraping or CAPTCHA Bypassing:** The application strictly respects government service access boundaries. It does not attempt to bypass CAPTCHAs, forge citizen sessions, or circumvent rate limits.
4. **Authenticity Guarantee:** No synthetic, fake, or mock records will ever be reported as genuine government records. If an official government gateway requires authentication or is not yet configured, the status `SOURCE_UNVERIFIED` or `AUTH_REQUIRED` is presented.
5. **Integrity Verification:** Every downloaded document undergoes strict PDF signature checks, size checks, HTML-rejection filters, and SHA-256 cryptographic hashing.
