package com.ai.studyassistant.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.ai.studyassistant.ui.theme.Cyan
import com.ai.studyassistant.ui.theme.DeepPurple

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
    )

    fun subjectsFor(exam: String): List<Subject> = when (exam) {
        "JEE" -> listOf(
            Subject("Physics", Icons.Default.Science, DeepPurple),
            Subject("Chemistry", Icons.Default.Biotech, Cyan),
            Subject("Mathematics", Icons.Default.Calculate, Color(0xFFFF6D00)),
        )
        "NEET" -> listOf(
            Subject("Physics", Icons.Default.Science, DeepPurple),
            Subject("Chemistry", Icons.Default.Biotech, Cyan),
            Subject("Biology", Icons.Default.LocalHospital, Color(0xFF2E7D32)),
        )
        "UPSC" -> listOf(
            Subject("History", Icons.Default.History, Color(0xFF8D6E63)),
            Subject("Geography", Icons.Default.Public, Color(0xFF00897B)),
            Subject("Polity", Icons.Default.Gavel, DeepPurple),
            Subject("Economy", Icons.AutoMirrored.Filled.TrendingUp, Color(0xFFEF6C00)),
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
        else -> null
    }

    /** Best-effort Open Trivia DB category mapping (https://opentdb.com/api_category.php). Null = no good match, use local bank only. */
    fun triviaCategoryFor(subject: String): Int? = when (subject) {
        "Physics", "Chemistry", "Biology" -> 17 // Science & Nature
        "Mathematics" -> 19 // Science: Mathematics
        "History" -> 23 // History
        "Geography" -> 22 // Geography
        "Polity" -> 24 // Politics
        else -> null // e.g. Economy has no matching OpenTDB category
    }
}
