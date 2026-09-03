package com.saathi.engine

import android.graphics.Rect
import com.saathi.data.entity.PatternEntity
import com.saathi.model.RiskLevel
import com.saathi.model.ScamCategory
import com.saathi.model.ScamEvaluationResult
import com.saathi.model.UiNodeSnapshot

/**
 * Deterministic On-Device Scam Detection Engine.
 * Integrates 5-stage text normalization, Aho-Corasick trie matching, contextual regex verification,
 * package trust evaluation, and cooldown suppression.
 */
class ScamDetectionEngine(
    private val cooldownManager: AlertCooldownManager = AlertCooldownManager()
) : IScamDetectionEngine {

    private val trie = Trie()
    private var sensitivity: String = "MEDIUM"

    // Precompiled contextual regex patterns for structural verification
    private val compiledRegexRules = mutableMapOf<String, Regex>()
    private val ruleWeights = mutableMapOf<String, Int>()
    private val ruleCategories = mutableMapOf<String, ScamCategory>()

    init {
        initDefaultRules()
    }

    private fun initDefaultRules() {
        // 1. OTP Theft & Exfiltration
        registerRule(
            ruleId = "RULE_OTP_HARVEST",
            category = ScamCategory.OTP_THEFT,
            baseWeight = 85,
            regex = """(?i)(\b(?:\d{4,8}|otp|ओटीपी|code)\b.*(?:share|send|tell|forward|bhejo|batao|verify|बताएं|भेजें|शेयर))|(?:(?:share|tell|give|send|forward|bhejo|batao|बताएं|भेजें|शेयर).*(?:otp|ओटीपी|code|\b\d{4,8}\b))""",
            keywords = listOf("otp", "one time password", "verification code", "share code", "forward sms", "ओटीपी", "सत्यापन कोड", "share otp", "send otp", "bhejo otp", "ओटीपी भेजें", "ओटीपी बताएं", "ओटीपी शेयर")
        )

        // 2. Bank Account Freeze / KYC Suspension / Electricity bill
        registerRule(
            ruleId = "RULE_KYC_SUSPENSION",
            category = ScamCategory.BANKING_KYC_FRAUD,
            baseWeight = 80,
            regex = """(?i)(?:account|card|sim|service|kyc|pan|electricity|power|bijli|bill|खाता|बिजली|बिल|केवाईसी).*(?:block|suspend|deactivat|expir|updat|disconnect|cut|unpaid|bhejo|ब्लॉक|सस्पेंड|कट|अपडेट|बंद)""",
            keywords = listOf(
                "account blocked", "kyc expired", "debit card suspended", "pan card update",
                "खाता ब्लॉक", "केवाईसी एक्सपायर", "केवाईसी अपडेट", "sbi yono", "electricity disconnected",
                "power will be cut", "bijli connection", "बिजली कट", "बिजली बिल", "बिजली कनेक्शन",
                "बिजली कनेक्शन कट", "bill unpaid tonight", "electricity bill", "बिल भुगतान"
            )
        )

        // 3. Digital Arrest & Law Enforcement Impersonation
        registerRule(
            ruleId = "RULE_DIGITAL_ARREST",
            category = ScamCategory.DIGITAL_ARREST,
            baseWeight = 95,
            regex = """(?i)(?:digital\s*arrest|cbi|police|customs|court|trai|narcotics|डिजिटल|पुलिस|गिरफ्तारी|वारंट).*(?:penalty|fine|warrant|payment|transfer|arrest|case|seize|जुर्माना|भुगतान)""",
            keywords = listOf("digital arrest", "police notice", "cbi warrant", "supreme court order", "customs penalty", "गिरफ्तारी", "पुलिस वारंट", "narcotics parcel", "डिजिटल अरेस्ट")
        )

        // 4. Lottery, Prize & KBC Scams
        registerRule(
            ruleId = "RULE_LOTTERY_KBC",
            category = ScamCategory.LOTTERY_PRIZE_SCAM,
            baseWeight = 70,
            regex = """(?i)(?:won|winner|lottery|prize|kbc|cashback|लॉटरी|इनाम).*(?:₹|\b\d{4,}\b|claim|deposit|fee|lakh|लाख|रुपये|जीत)""",
            keywords = listOf("congratulations won", "kbc lottery", "claim prize", "लॉटरी जीत", "इनाम", "lucky draw", "cash prize", "won lottery", "लॉटरी")
        )

        // 5. Remote Access Software Coercion / Malicious APK
        registerRule(
            ruleId = "RULE_RAT_INSTALL",
            category = ScamCategory.REMOTE_ACCESS_COERCION,
            baseWeight = 90,
            regex = """(?i)(?:install|download|open|start|इंस्टॉल|डाउनलोड).*(?:anydesk|teamviewer|quicksupport|rustdesk|apk)""",
            keywords = listOf("install anydesk", "download teamviewer", "quicksupport", "rustdesk", "share screen", "9-digit code", "install apk", "download apk")
        )

        // 6. Urgent Money Transfer
        registerRule(
            ruleId = "RULE_URGENT_TRANSFER",
            category = ScamCategory.URGENT_TRANSFER,
            baseWeight = 60,
            regex = """(?i)(?:urgent|immediate|turant|emergency|तुरंत|इमरजेंसी).*(?:send|transfer|money|pay|paise|पैसे|ट्रांसफर)""",
            keywords = listOf("send money now", "immediate transfer", "today only", "urgent payment", "turant paise", "hospital emergency", "तुरंत पैसे", "पैसे ट्रांसफर")
        )

        trie.buildFailureTransitions()
    }

    private fun registerRule(
        ruleId: String,
        category: ScamCategory,
        baseWeight: Int,
        regex: String?,
        keywords: List<String>
    ) {
        ruleCategories[ruleId] = category
        ruleWeights[ruleId] = baseWeight
        if (!regex.isNullOrBlank()) {
            compiledRegexRules[ruleId] = Regex(regex)
        }
        for (kw in keywords) {
            trie.insert(kw, category, ruleId)
        }
    }

    override fun loadPatterns(patterns: List<PatternEntity>) {
        trie.clear()
        compiledRegexRules.clear()
        ruleWeights.clear()
        ruleCategories.clear()

        // Re-register default base rules
        initDefaultRules()

        // Load custom DB patterns
        for (entity in patterns) {
            if (!entity.isActive) continue
            val keywords = entity.keywordsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            registerRule(
                ruleId = entity.patternId,
                category = entity.category,
                baseWeight = entity.baseWeight,
                regex = entity.regexRule,
                keywords = keywords
            )
        }
        trie.buildFailureTransitions()
    }

    override fun setSensitivity(level: String) {
        this.sensitivity = level
    }

    override fun evaluate(snapshot: UiNodeSnapshot): ScamEvaluationResult {
        val startTime = System.nanoTime()

        // 1. Extract actionable node presence and spatial cues
        val hasActionableNode = snapshot.nodes.any { it.isClickable || it.isEditable }
        var targetBounds: Rect? = null

        // 2. Normalize text across the snapshot
        val normalizedText = TextNormalizer.normalize(snapshot.flattenedText)
        if (normalizedText.isBlank()) {
            return ScamEvaluationResult.Safe
        }

        // 3. Trie search over normalized text
        val trieMatches = trie.search(normalizedText)

        // 4. Regex verification
        val matchedRules = mutableSetOf<String>()
        var highestWeight = 0
        var primaryCategory = ScamCategory.GENERIC_SUSPICIOUS
        var matchedSnippet = ""

        for (match in trieMatches) {
            matchedRules.add(match.ruleId)
            val weight = ruleWeights[match.ruleId] ?: 50
            if (weight > highestWeight) {
                highestWeight = weight
                primaryCategory = match.category
                matchedSnippet = match.keyword
            }
        }

        // Evaluate contextual regexes
        for ((ruleId, regex) in compiledRegexRules) {
            if (regex.containsMatchIn(normalizedText)) {
                matchedRules.add(ruleId)
                val weight = ruleWeights[ruleId] ?: 50
                if (weight > highestWeight) {
                    highestWeight = weight
                    primaryCategory = ruleCategories[ruleId] ?: ScamCategory.GENERIC_SUSPICIOUS
                    val found = regex.find(normalizedText)?.value ?: ""
                    if (found.isNotEmpty()) {
                        matchedSnippet = found
                    }
                }
            }
        }

        if (matchedRules.isEmpty() || highestWeight == 0) {
            return ScamEvaluationResult.Safe
        }

        // 5. Find target node bounds if available
        if (matchedSnippet.isNotEmpty()) {
            val matchingNode = snapshot.nodes.firstOrNull { node ->
                val nodeText = node.text?.lowercase() ?: ""
                val nodeDesc = node.contentDescription?.lowercase() ?: ""
                nodeText.contains(matchedSnippet.lowercase()) || nodeDesc.contains(matchedSnippet.lowercase())
            }
            if (matchingNode != null) {
                targetBounds = matchingNode.bounds
            }
        }

        // 6. Compute composite score
        val compositeScore = RiskEvaluator.computeScore(
            baseWeight = highestWeight,
            packageName = snapshot.packageName,
            hasActionableNode = hasActionableNode,
            hasSpatialProximity = targetBounds != null,
            sensitivity = sensitivity
        )

        val riskLevel = RiskEvaluator.determineRiskLevel(compositeScore)
        if (riskLevel == RiskLevel.SAFE) {
            return ScamEvaluationResult.Safe
        }

        // 7. Check Cooldown
        val ruleId = matchedRules.firstOrNull() ?: "GENERIC"
        val cooldownKey = "${snapshot.packageName}_$ruleId"
        if (cooldownManager.isOnCooldown(cooldownKey, snapshot.timestampMs)) {
            return ScamEvaluationResult.Safe
        }
        cooldownManager.recordTrigger(cooldownKey, snapshot.timestampMs)

        val latencyMs = (System.nanoTime() - startTime) / 1_000_000.0

        return ScamEvaluationResult.ThreatDetected(
            category = primaryCategory,
            riskLevel = riskLevel,
            confidenceScore = compositeScore,
            matchedRuleId = ruleId,
            triggerSnippet = matchedSnippet,
            targetNodeBounds = targetBounds,
            executionLatencyMs = latencyMs
        )
    }

    override fun evaluateText(text: String, packageName: String): ScamEvaluationResult {
        val now = System.currentTimeMillis()
        val snapshot = UiNodeSnapshot(
            packageName = packageName,
            timestampMs = now,
            nodes = emptyList(),
            flattenedText = text
        )
        return evaluate(snapshot)
    }
}
