package ru.yulia.grades_android.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import ru.yulia.grades_android.data.model.AverageResponse
import ru.yulia.grades_android.data.model.Grade
import ru.yulia.grades_android.data.model.NewGradeRequest
import ru.yulia.grades_android.data.model.NewStudentRequest
import ru.yulia.grades_android.data.model.NewSubjectRequest
import ru.yulia.grades_android.data.model.Student
import ru.yulia.grades_android.data.model.Subject
import ru.yulia.grades_android.data.model.UpdateGradeRequest

/**
 * Retrofit definition for the grade book REST API.
 */
interface GradesApi {

    @GET("students")
    suspend fun getStudents(): List<Student>

    @POST("students")
    suspend fun createStudent(
        @Body body: NewStudentRequest
    ): Student

    @GET("subjects")
    suspend fun getSubjects(): List<Subject>

    @POST("subjects")
    suspend fun createSubject(
        @Body body: NewSubjectRequest
    ): Subject

    @GET("grades")
    suspend fun getGrades(
        @Query("student_id") studentId: Int? = null,
        @Query("subject_id") subjectId: Int? = null
    ): List<Grade>

    @POST("grades")
    suspend fun createGrade(
        @Body body: NewGradeRequest
    ): Grade

    @PATCH("grades/{id}")
    suspend fun updateGrade(
        @Path("id") gradeId: Int,
        @Body body: UpdateGradeRequest
    ): Grade

    @GET("subjects/{id}/average")
    suspend fun getSubjectAverage(
        @Path("id") subjectId: Int
    ): AverageResponse
}
