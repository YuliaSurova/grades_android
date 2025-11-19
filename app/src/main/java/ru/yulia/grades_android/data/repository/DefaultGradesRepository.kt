package ru.yulia.grades_android.data.repository

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import ru.yulia.grades_android.data.model.AverageResponse
import ru.yulia.grades_android.data.model.ErrorResponse
import ru.yulia.grades_android.data.model.Grade
import ru.yulia.grades_android.data.model.NewGradeRequest
import ru.yulia.grades_android.data.model.NewStudentRequest
import ru.yulia.grades_android.data.model.NewSubjectRequest
import ru.yulia.grades_android.data.model.Student
import ru.yulia.grades_android.data.model.Subject
import ru.yulia.grades_android.data.model.UpdateGradeRequest
import ru.yulia.grades_android.data.remote.GradesApi
import java.io.IOException

class DefaultGradesRepository(
    private val api: GradesApi,
    private val json: Json
) : GradesRepository {

    override suspend fun getStudents(): Result<List<Student>> = safeApiCall {
        api.getStudents()
    }

    override suspend fun createStudent(name: String): Result<Student> = safeApiCall {
        api.createStudent(NewStudentRequest(name = name))
    }

    override suspend fun getSubjects(): Result<List<Subject>> = safeApiCall {
        api.getSubjects()
    }

    override suspend fun createSubject(name: String): Result<Subject> = safeApiCall {
        api.createSubject(NewSubjectRequest(name = name))
    }

    override suspend fun getGrades(
        studentId: Int?,
        subjectId: Int?
    ): Result<List<Grade>> = safeApiCall {
        api.getGrades(studentId = studentId, subjectId = subjectId)
    }

    override suspend fun createGrade(
        studentId: Int,
        subjectId: Int,
        score: Double
    ): Result<Grade> = safeApiCall {
        api.createGrade(
            NewGradeRequest(
                studentId = studentId,
                subjectId = subjectId,
                score = score
            )
        )
    }

    override suspend fun updateGrade(
        gradeId: Int,
        studentId: Int?,
        subjectId: Int?,
        score: Double?
    ): Result<Grade> = safeApiCall {
        api.updateGrade(
            gradeId = gradeId,
            body = UpdateGradeRequest(
                studentId = studentId,
                subjectId = subjectId,
                score = score
            )
        )
    }

    override suspend fun getSubjectAverage(subjectId: Int): Result<AverageResponse> = safeApiCall {
        api.getSubjectAverage(subjectId = subjectId)
    }

    private suspend fun <T> safeApiCall(
        block: suspend () -> T
    ): Result<T> {
        return try {
            Result.success(block())
        } catch (throwable: Throwable) {
            Result.failure(parseThrowable(throwable))
        }
    }

    private fun parseThrowable(throwable: Throwable): Throwable {
        return when (throwable) {
            is ApiException -> throwable
            is HttpException -> {
                val message = throwable.response()?.errorBody()?.string()?.let(::mapErrorBody)
                ApiException(message ?: "Ошибка сервера (${throwable.code()})", throwable)
            }

            is IOException -> ApiException("Проверьте подключение к интернету", throwable)
            else -> ApiException(throwable.message ?: "Неизвестная ошибка", throwable)
        }
    }

    private fun mapErrorBody(rawBody: String?): String? {
        if (rawBody.isNullOrBlank()) return null
        return try {
            json.decodeFromString(ErrorResponse.serializer(), rawBody).error
        } catch (error: SerializationException) {
            null
        }
    }
}

class ApiException(message: String, cause: Throwable? = null) : Exception(message, cause)
