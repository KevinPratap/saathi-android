package com.saathi.benchmark

import com.saathi.model.RiskLevel
import com.saathi.model.ScamCategory

/**
 * Data class defining a synthetic benchmark test case.
 */
data class BenchmarkFixture(
    val id: String,
    val description: String,
    val text: String,
    val packageName: String,
    val isScam: Boolean,
    val expectedCategory: ScamCategory?,
    val expectedRiskLevel: RiskLevel
)

/**
 * Benchmark catalog of 26 synthetic test cases covering positive threat vectors
 * (English, Devanagari, homoglyphs, leetspeak, zero-width) and benign negative controls.
 */
object ScamFixtures {

    val FIXTURES = listOf(
        // 1. Standard English OTP request
        BenchmarkFixture(
            id = "TC-01",
            description = "Standard English OTP theft",
            text = "Your bank verification code is 584920. Please share OTP with the agent to verify.",
            packageName = "com.whatsapp",
            isScam = true,
            expectedCategory = ScamCategory.OTP_THEFT,
            expectedRiskLevel = RiskLevel.HIGH
        ),
        // 2. Hindi Devanagari OTP request
        BenchmarkFixture(
            id = "TC-02",
            description = "Devanagari OTP harvest",
            text = "कृपया अपना बैंक ओटीपी सत्यापन कोड अधिकारी को तुरंत भेजें।",
            packageName = "com.whatsapp",
            isScam = true,
            expectedCategory = ScamCategory.OTP_THEFT,
            expectedRiskLevel = RiskLevel.HIGH
        ),
        // 3. Zero-width character obfuscated OTP
        BenchmarkFixture(
            id = "TC-03",
            description = "Zero-width injected OTP scam",
            text = "Sh\u200Bare y\u200Cour O\u200BT\u200CP c\u200Code 492019 n\u200Bow",
            packageName = "com.whatsapp",
            isScam = true,
            expectedCategory = ScamCategory.OTP_THEFT,
            expectedRiskLevel = RiskLevel.HIGH
        ),
        // 4. Leetspeak obfuscated OTP
        BenchmarkFixture(
            id = "TC-04",
            description = "Leetspeak unfolded OTP scam",
            text = "Please sh@re your 0TP verification code 892011 to continue",
            packageName = "com.whatsapp",
            isScam = true,
            expectedCategory = ScamCategory.OTP_THEFT,
            expectedRiskLevel = RiskLevel.HIGH
        ),
        // 5. Cyrillic homoglyph OTP attack
        BenchmarkFixture(
            id = "TC-05",
            description = "Cyrillic homoglyph OTP scam",
            text = "Please share your \u041E\u0422P verification code with bank manager",
            packageName = "com.whatsapp",
            isScam = true,
            expectedCategory = ScamCategory.OTP_THEFT,
            expectedRiskLevel = RiskLevel.HIGH
        ),
        // 6. Standard English Bank KYC
        BenchmarkFixture(
            id = "TC-06",
            description = "Bank account freeze KYC expiry",
            text = "Your SBI YONO account blocked. Update KYC immediately or card will be suspended.",
            packageName = "com.whatsapp",
            isScam = true,
            expectedCategory = ScamCategory.BANKING_KYC_FRAUD,
            expectedRiskLevel = RiskLevel.HIGH
        ),
        // 7. Devanagari Bank KYC Coercion
        BenchmarkFixture(
            id = "TC-07",
            description = "Devanagari Bank KYC suspension",
            text = "प्रिय ग्राहक आपका बैंक खाता ब्लॉक हो गया है, तुरंत केवाईसी एक्सपायर लिंक पर अपडेट करें।",
            packageName = "com.whatsapp",
            isScam = true,
            expectedCategory = ScamCategory.BANKING_KYC_FRAUD,
            expectedRiskLevel = RiskLevel.HIGH
        ),
        // 8. Leetspeak Dotted Acronym KYC
        BenchmarkFixture(
            id = "TC-08",
            description = "Dotted acronym KYC scam",
            text = "Your K.Y.C is expired, update pan card now or service deactivated",
            packageName = "com.whatsapp",
            isScam = true,
            expectedCategory = ScamCategory.BANKING_KYC_FRAUD,
            expectedRiskLevel = RiskLevel.HIGH
        ),
        // 9. Electricity bill disconnection (English)
        BenchmarkFixture(
            id = "TC-09",
            description = "Electricity bill cut tonight scam",
            text = "Dear consumer, electricity power will be cut tonight at 9:30 PM due to unpaid bill. Call officer immediately.",
            packageName = "com.google.android.apps.messaging",
            isScam = true,
            expectedCategory = ScamCategory.BANKING_KYC_FRAUD,
            expectedRiskLevel = RiskLevel.HIGH
        ),
        // 10. Electricity bill disconnection (Devanagari)
        BenchmarkFixture(
            id = "TC-10",
            description = "Devanagari electricity disconnection scam",
            text = "बिजली बिल भुगतान न होने के कारण आज रात बिजली कनेक्शन कट कर दिया जाएगा। तुरंत संपर्क करें।",
            packageName = "com.google.android.apps.messaging",
            isScam = true,
            expectedCategory = ScamCategory.BANKING_KYC_FRAUD,
            expectedRiskLevel = RiskLevel.HIGH
        ),
        // 11. Digital Arrest (English)
        BenchmarkFixture(
            id = "TC-11",
            description = "Digital Arrest police warrant extortion",
            text = "Supreme Court and CBI police notice: You are under Digital Arrest. Pay penalty fee immediately to avoid jail.",
            packageName = "com.whatsapp",
            isScam = true,
            expectedCategory = ScamCategory.DIGITAL_ARREST,
            expectedRiskLevel = RiskLevel.HIGH
        ),
        // 12. Digital Arrest (Devanagari)
        BenchmarkFixture(
            id = "TC-12",
            description = "Devanagari police warrant arrest scam",
            text = "पुलिस वारंट: आपके खिलाफ गैर-जमानती गिरफ्तारी वारंट जारी हुआ है। तुरंत जुर्माना भुगतान करें।",
            packageName = "com.whatsapp",
            isScam = true,
            expectedCategory = ScamCategory.DIGITAL_ARREST,
            expectedRiskLevel = RiskLevel.HIGH
        ),
        // 13. Customs / Narcotics seizure
        BenchmarkFixture(
            id = "TC-13",
            description = "Narcotics parcel customs extortion",
            text = "Customs notice: A narcotics parcel was seized in your name. Pay fine immediately to avoid police case.",
            packageName = "com.whatsapp",
            isScam = true,
            expectedCategory = ScamCategory.DIGITAL_ARREST,
            expectedRiskLevel = RiskLevel.HIGH
        ),
        // 14. Lottery / KBC Scam (English)
        BenchmarkFixture(
            id = "TC-14",
            description = "KBC lottery winner scam",
            text = "Congratulations! You won ₹25 lakh in KBC lottery lucky draw. Claim prize money now.",
            packageName = "com.whatsapp",
            isScam = true,
            expectedCategory = ScamCategory.LOTTERY_PRIZE_SCAM,
            expectedRiskLevel = RiskLevel.HIGH
        ),
        // 15. Lottery Scam (Devanagari)
        BenchmarkFixture(
            id = "TC-15",
            description = "Devanagari lottery prize scam",
            text = "बधाई हो! आपने लॉटरी जीत ली है 25 लाख रुपए का इनाम क्लेम करें।",
            packageName = "com.whatsapp",
            isScam = true,
            expectedCategory = ScamCategory.LOTTERY_PRIZE_SCAM,
            expectedRiskLevel = RiskLevel.HIGH
        ),
        // 16. Remote Access / RAT install (AnyDesk)
        BenchmarkFixture(
            id = "TC-16",
            description = "AnyDesk installation coercion",
            text = "Please install AnyDesk on your phone and share 9-digit code for banking refund.",
            packageName = "com.whatsapp",
            isScam = true,
            expectedCategory = ScamCategory.REMOTE_ACCESS_COERCION,
            expectedRiskLevel = RiskLevel.HIGH
        ),
        // 17. Remote Access (TeamViewer / QuickSupport)
        BenchmarkFixture(
            id = "TC-17",
            description = "TeamViewer QuickSupport coercion",
            text = "Download TeamViewer QuickSupport to resolve technical error on your device.",
            packageName = "com.whatsapp",
            isScam = true,
            expectedCategory = ScamCategory.REMOTE_ACCESS_COERCION,
            expectedRiskLevel = RiskLevel.HIGH
        ),
        // 18. Fake Customer Support APK
        BenchmarkFixture(
            id = "TC-18",
            description = "Malicious customer support APK install",
            text = "Download and install official bank support APK file now to resolve your complaint.",
            packageName = "com.whatsapp",
            isScam = true,
            expectedCategory = ScamCategory.REMOTE_ACCESS_COERCION,
            expectedRiskLevel = RiskLevel.HIGH
        ),
        // 19. Urgent Money Transfer (English)
        BenchmarkFixture(
            id = "TC-19",
            description = "Urgent medical emergency money transfer",
            text = "Hospital emergency! Send money now to pay hospital bill immediately.",
            packageName = "com.whatsapp",
            isScam = true,
            expectedCategory = ScamCategory.URGENT_TRANSFER,
            expectedRiskLevel = RiskLevel.HIGH
        ),
        // 20. Urgent Money Transfer (Hinglish/Hindi)
        BenchmarkFixture(
            id = "TC-20",
            description = "Hinglish urgent money transfer coercion",
            text = "Bhaiya urgent payment hai, turant paise transfer karo please.",
            packageName = "com.whatsapp",
            isScam = true,
            expectedCategory = ScamCategory.URGENT_TRANSFER,
            expectedRiskLevel = RiskLevel.HIGH
        ),

        // === BENIGN NEGATIVE CONTROLS (Must NOT trigger alerts) ===
        // 21. Family greeting
        BenchmarkFixture(
            id = "TC-21",
            description = "Benign family greeting",
            text = "Hello grandmother, how are you feeling today? Did you take your medicines?",
            packageName = "com.whatsapp",
            isScam = false,
            expectedCategory = null,
            expectedRiskLevel = RiskLevel.SAFE
        ),
        // 22. Grocery transaction receipt
        BenchmarkFixture(
            id = "TC-22",
            description = "Benign grocery receipt",
            text = "Thank you for shopping at BigBasket. Your order of ₹650 will be delivered by 5 PM.",
            packageName = "com.bigbasket.mobileapp",
            isScam = false,
            expectedCategory = null,
            expectedRiskLevel = RiskLevel.SAFE
        ),
        // 23. Train ticket confirmation
        BenchmarkFixture(
            id = "TC-23",
            description = "Benign IRCTC ticket SMS",
            text = "IRCTC Ticket: PNR 2451234567, Train 12951, Mumbai Rajdhani, Coach B2, Berth 35 Confirmed.",
            packageName = "com.google.android.apps.messaging",
            isScam = false,
            expectedCategory = null,
            expectedRiskLevel = RiskLevel.SAFE
        ),
        // 24. Weather forecast
        BenchmarkFixture(
            id = "TC-24",
            description = "Benign weather update",
            text = "Today's weather forecast for New Delhi: Clear skies with a high of 32 degrees Celsius.",
            packageName = "com.google.android.googlequicksearchbox",
            isScam = false,
            expectedCategory = null,
            expectedRiskLevel = RiskLevel.SAFE
        ),
        // 25. Medicine reminder
        BenchmarkFixture(
            id = "TC-25",
            description = "Benign healthcare reminder",
            text = "Reminder: Please remember to take your blood pressure medication after lunch today.",
            packageName = "com.whatsapp",
            isScam = false,
            expectedCategory = null,
            expectedRiskLevel = RiskLevel.SAFE
        ),
        // 26. Genuine banking balance inquiry in verified bank app
        BenchmarkFixture(
            id = "TC-26",
            description = "Benign balance check inside PhonePe",
            text = "Available Account Balance for State Bank of India: ₹24,500.00",
            packageName = "com.phonepe.app",
            isScam = false,
            expectedCategory = null,
            expectedRiskLevel = RiskLevel.SAFE
        )
    )
}
