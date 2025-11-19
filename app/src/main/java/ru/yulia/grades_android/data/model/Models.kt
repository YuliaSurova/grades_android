package ru.yulia.grades_android.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Student(
    val id: Int,
    val name: String
)

@Serializable
data class Subject(
    val id: Int,
    val name: String
)

@Serializable
data class Grade(
    val id: Int,
    @SerialName("student_id") val studentId: Int,
    val student: String? = null,
    @SerialName("subject_id") val subjectId: Int,
    val subject: String? = null,
    val score: Double
)

@Serializable
data class AverageResponse(
    @SerialName("subject_id") val subjectId: Int,
    val subject: String,
    val average: Double? = null,
    val count: Int = 0
)

@Serializable
data class ErrorResponse(
    val error: String
)

@Serializable
data class NewStudentRequest(
    val name: String
)

@Serializable
data class NewSubjectRequest(
    val name: String
)

@Serializable
data class NewGradeRequest(
    @SerialName("student_id") val studentId: Int,
    @SerialName("subject_id") val subjectId: Int,
    val score: Double
)

@Serializable
data class UpdateGradeRequest(
    @SerialName("student_id") val studentId: Int? = null,
    @SerialName("subject_id") val subjectId: Int? = null,
    val score: Double? = null
)
