package ru.yulia.grades_android.data.repository

import ru.yulia.grades_android.data.model.AverageResponse
import ru.yulia.grades_android.data.model.Grade
import ru.yulia.grades_android.data.model.Student
import ru.yulia.grades_android.data.model.Subject

interface GradesRepository {
    suspend fun getStudents(): Result<List<Student>>
    suspend fun createStudent(name: String): Result<Student>
    suspend fun getSubjects(): Result<List<Subject>>
    suspend fun createSubject(name: String): Result<Subject>
    suspend fun getGrades(studentId: Int? = null, subjectId: Int? = null): Result<List<Grade>>
    suspend fun createGrade(studentId: Int, subjectId: Int, score: Double): Result<Grade>
    suspend fun updateGrade(
        gradeId: Int,
        studentId: Int? = null,
        subjectId: Int? = null,
        score: Double? = null
    ): Result<Grade>

    suspend fun getSubjectAverage(subjectId: Int): Result<AverageResponse>
}
