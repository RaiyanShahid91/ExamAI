package com.ai.studyassistant.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.ai.studyassistant.ui.theme.BrandTeal
import com.ai.studyassistant.ui.theme.BrandNavy

data class Exam(
    val name: String,
    val fullName: String,
    val tagline: String,
    val icon: ImageVector
)

data class Subject(
    val name: String,
    val icon: ImageVector,
    val color: Color
)

object ExamData {

    val exams = listOf(
        Exam("JEE", "Joint Entrance Examination", "Crack IIT, NIT & more", Icons.Default.Calculate),
        Exam("NEET", "National Eligibility cum Entrance Test", "Your path to medical college", Icons.Default.LocalHospital),
        Exam("UPSC", "Civil Services Examination", "Serve the nation, lead the change", Icons.Default.AccountBalance),
        Exam("SSC", "Staff Selection Commission", "Government jobs: CGL, CHSL & more", Icons.Default.Work),
        Exam("Banking", "IBPS / SBI PO & Clerk", "Land your banking career", Icons.Default.AccountBalanceWallet),
        Exam("CAT", "Common Admission Test", "Your gateway to top B-schools", Icons.Default.Business),
        Exam("CDS/NDA", "Defence Services Exam", "Serve the nation in uniform", Icons.Default.Shield),
    )

    fun subjectsFor(exam: String): List<Subject> = when (exam) {
        "JEE" -> listOf(
            Subject("Physics", Icons.Default.Science, BrandNavy),
            Subject("Chemistry", Icons.Default.Biotech, BrandTeal),
            Subject("Mathematics", Icons.Default.Calculate, Color(0xFFFF6D00)),
        )
        "NEET" -> listOf(
            Subject("Physics", Icons.Default.Science, BrandNavy),
            Subject("Chemistry", Icons.Default.Biotech, BrandTeal),
            Subject("Biology", Icons.Default.LocalHospital, Color(0xFF2E7D32)),
        )
        "UPSC" -> listOf(
            Subject("History", Icons.Default.History, Color(0xFF8D6E63)),
            Subject("Geography", Icons.Default.Public, Color(0xFF00897B)),
            Subject("Polity", Icons.Default.Gavel, BrandNavy),
            Subject("Economy", Icons.AutoMirrored.Filled.TrendingUp, Color(0xFFEF6C00)),
        )
        "SSC" -> listOf(
            Subject("General Awareness", Icons.Default.Public, Color(0xFF00897B)),
            Subject("Quantitative Aptitude", Icons.Default.Calculate, Color(0xFFFF6D00)),
            Subject("Reasoning", Icons.Default.Psychology, BrandNavy),
        )
        "Banking" -> listOf(
            Subject("Quantitative Aptitude", Icons.Default.Calculate, Color(0xFFFF6D00)),
            Subject("Reasoning", Icons.Default.Psychology, BrandNavy),
            Subject("Banking Awareness", Icons.Default.AccountBalance, Color(0xFF2E7D32)),
        )
        "CAT" -> listOf(
            Subject("Quantitative Ability", Icons.Default.Calculate, Color(0xFFFF6D00)),
            Subject("Verbal Ability", Icons.AutoMirrored.Filled.MenuBook, BrandTeal),
            Subject("Data Interpretation & Logical Reasoning", Icons.Default.BarChart, BrandNavy),
        )
        "CDS/NDA" -> listOf(
            Subject("Mathematics", Icons.Default.Calculate, Color(0xFFFF6D00)),
            Subject("General Knowledge", Icons.Default.Public, Color(0xFF00897B)),
            Subject("English", Icons.AutoMirrored.Filled.MenuBook, BrandTeal),
        )
        else -> emptyList()
    }

    fun topicsFor(exam: String, subject: String): List<String> = when (exam to subject) {
        "JEE" to "Physics" -> listOf(
            "Mechanics", "Thermodynamics", "Electrostatics", "Current Electricity",
            "Magnetism", "Optics", "Modern Physics", "Waves & Oscillations",
            "Rotational Motion", "Electromagnetic Induction"
        )
        "JEE" to "Chemistry" -> listOf(
            "Chemical Bonding", "Atomic Structure", "Thermodynamics", "Chemical Equilibrium",
            "Electrochemistry", "Organic Chemistry Basics", "Coordination Compounds",
            "Periodic Table", "Chemical Kinetics", "Solutions"
        )
        "JEE" to "Mathematics" -> listOf(
            "Calculus", "Algebra", "Coordinate Geometry", "Trigonometry", "Probability",
            "Vectors & 3D Geometry", "Matrices & Determinants", "Sequences & Series",
            "Complex Numbers", "Permutations & Combinations"
        )
        "NEET" to "Physics" -> listOf(
            "Mechanics", "Thermodynamics", "Electrostatics", "Current Electricity",
            "Magnetism", "Optics", "Modern Physics", "Waves & Oscillations",
            "Gravitation", "Semiconductor Electronics"
        )
        "NEET" to "Chemistry" -> listOf(
            "Chemical Bonding", "Organic Chemistry", "Biomolecules", "Chemical Equilibrium",
            "Electrochemistry", "Coordination Compounds", "Periodic Table",
            "Thermodynamics", "Polymers", "Chemical Kinetics"
        )
        "NEET" to "Biology" -> listOf(
            "Human Physiology", "Genetics & Evolution", "Cell Biology", "Plant Physiology",
            "Ecology", "Reproduction", "Biotechnology", "Human Health & Disease",
            "Molecular Basis of Inheritance", "Biodiversity & Conservation"
        )
        "UPSC" to "History" -> listOf(
            "Ancient India", "Medieval India", "Modern India", "Freedom Struggle",
            "Post-Independence India", "Art & Culture", "World History",
            "Indian National Movement", "Revolt of 1857", "Constitutional Development"
        )
        "UPSC" to "Geography" -> listOf(
            "Physical Geography", "Indian Geography", "World Geography", "Climate & Monsoons",
            "Natural Resources", "Population & Settlements", "Disaster Management",
            "Soils of India", "Rivers & Drainage", "Economic Geography"
        )
        "UPSC" to "Polity" -> listOf(
            "Indian Constitution", "Fundamental Rights", "Parliament", "Judiciary",
            "Federalism", "Panchayati Raj", "Election Commission", "Constitutional Amendments",
            "Directive Principles", "Centre-State Relations"
        )
        "UPSC" to "Economy" -> listOf(
            "Indian Economy Basics", "Budget & Fiscal Policy", "Monetary Policy",
            "Banking & RBI", "Five Year Plans", "GST & Taxation", "Poverty & Unemployment",
            "Agriculture Economy", "International Trade", "Inflation"
        )
        "SSC" to "General Awareness" -> listOf(
            "Indian History", "Indian Polity", "Geography", "General Science",
            "Static GK", "Current Affairs", "Sports", "Awards & Honours",
            "Books & Authors", "Important Days"
        )
        "SSC" to "Quantitative Aptitude" -> listOf(
            "Number System", "Percentage", "Profit & Loss", "Simple & Compound Interest",
            "Time & Work", "Time, Speed & Distance", "Ratio & Proportion", "Average",
            "Mensuration", "Data Interpretation"
        )
        "SSC" to "Reasoning" -> listOf(
            "Analogy", "Classification", "Series", "Coding-Decoding",
            "Blood Relations", "Direction Sense", "Ranking & Order", "Syllogism",
            "Non-Verbal Reasoning", "Puzzle"
        )
        "Banking" to "Quantitative Aptitude" -> listOf(
            "Number Series", "Simplification", "Percentage", "Profit & Loss",
            "Simple & Compound Interest", "Time & Work", "Time, Speed & Distance",
            "Data Interpretation", "Ratio & Proportion", "Mensuration"
        )
        "Banking" to "Reasoning" -> listOf(
            "Puzzle & Seating Arrangement", "Syllogism", "Coding-Decoding", "Blood Relations",
            "Direction Sense", "Inequality", "Order & Ranking", "Series",
            "Machine Input-Output", "Data Sufficiency"
        )
        "Banking" to "Banking Awareness" -> listOf(
            "RBI & Monetary Policy", "Types of Banks", "Banking History", "Payment Systems",
            "Financial Inclusion", "Banking Terms", "NABARD & SIDBI", "Money Market",
            "Capital Market", "Insurance Basics"
        )
        "CAT" to "Quantitative Ability" -> listOf(
            "Number System", "Algebra", "Geometry & Mensuration", "Arithmetic",
            "Percentage & Profit-Loss", "Time, Speed & Distance", "Time & Work",
            "Permutation & Combination", "Probability", "Logarithms"
        )
        "CAT" to "Verbal Ability" -> listOf(
            "Reading Comprehension", "Para Jumbles", "Sentence Correction", "Vocabulary",
            "Critical Reasoning", "Para Summary", "Fill in the Blanks", "Synonyms & Antonyms",
            "Idioms & Phrases", "Verbal Analogies"
        )
        "CAT" to "Data Interpretation & Logical Reasoning" -> listOf(
            "Tables", "Bar Graphs", "Line Graphs", "Pie Charts",
            "Caselets", "Seating Arrangement", "Blood Relations", "Syllogism",
            "Puzzles", "Data Sufficiency"
        )
        "CDS/NDA" to "Mathematics" -> listOf(
            "Algebra", "Trigonometry", "Geometry", "Mensuration",
            "Statistics", "Number System", "Probability", "Matrices & Determinants",
            "Coordinate Geometry", "Calculus Basics"
        )
        "CDS/NDA" to "General Knowledge" -> listOf(
            "Indian History", "Geography", "Indian Polity", "General Science",
            "Defence & Military Affairs", "Current Affairs", "Economics Basics",
            "Environment", "Sports", "Important Days"
        )
        "CDS/NDA" to "English" -> listOf(
            "Grammar", "Vocabulary", "Comprehension", "Synonyms & Antonyms",
            "Sentence Correction", "Idioms & Phrases", "Spotting Errors",
            "Ordering of Words", "Fill in the Blanks", "One Word Substitution"
        )
        else -> emptyList()
    }

    /** Key used to look up the bundled local question bank (assets/questions.json). */
    fun localBankKey(exam: String, subject: String): String? = when (exam to subject) {
        "JEE" to "Physics" -> "jee_physics"
        "JEE" to "Chemistry" -> "jee_chemistry"
        "JEE" to "Mathematics" -> "jee_maths"
        "NEET" to "Physics" -> "neet_physics"
        "NEET" to "Chemistry" -> "neet_chemistry"
        "NEET" to "Biology" -> "neet_biology"
        "UPSC" to "History" -> "upsc_history"
        "UPSC" to "Polity" -> "upsc_polity"
        "UPSC" to "Geography" -> "upsc_geography"
        "UPSC" to "Economy" -> "upsc_economy"
        "SSC" to "General Awareness" -> "ssc_general_awareness"
        "SSC" to "Quantitative Aptitude" -> "ssc_quant"
        "SSC" to "Reasoning" -> "ssc_reasoning"
        "Banking" to "Quantitative Aptitude" -> "banking_quant"
        "Banking" to "Reasoning" -> "banking_reasoning"
        "Banking" to "Banking Awareness" -> "banking_awareness"
        "CAT" to "Quantitative Ability" -> "cat_quant"
        "CAT" to "Verbal Ability" -> "cat_verbal"
        "CAT" to "Data Interpretation & Logical Reasoning" -> "cat_dilr"
        "CDS/NDA" to "Mathematics" -> "cds_maths"
        "CDS/NDA" to "General Knowledge" -> "cds_gk"
        "CDS/NDA" to "English" -> "cds_english"
        else -> null
    }

    /** Best-effort Open Trivia DB category mapping (https://opentdb.com/api_category.php). Null = no good match, use local bank only. */
    fun triviaCategoryFor(subject: String): Int? = when (subject) {
        "Physics", "Chemistry", "Biology" -> 17 // Science & Nature
        "Mathematics", "Quantitative Aptitude", "Quantitative Ability" -> 19 // Science: Mathematics
        "History" -> 23 // History
        "Geography" -> 22 // Geography
        "Polity" -> 24 // Politics
        // Best-effort fallback to General Knowledge for subjects with no closer OpenTDB category.
        "General Awareness", "General Knowledge", "Banking Awareness", "Reasoning",
        "Verbal Ability", "English", "Data Interpretation & Logical Reasoning" -> 9
        else -> null // e.g. Economy has no matching OpenTDB category
    }
}
