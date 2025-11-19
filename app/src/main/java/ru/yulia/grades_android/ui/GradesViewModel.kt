package ru.yulia.grades_android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.yulia.grades_android.data.model.Grade
import ru.yulia.grades_android.data.model.Student
import ru.yulia.grades_android.data.model.Subject
import ru.yulia.grades_android.data.repository.GradesRepository
import java.util.Locale

data class GradesUiState(
    val students: List<Student> = emptyList(),
    val subjects: List<Subject> = emptyList(),
    val grades: List<Grade> = emptyList(),
    val selectedStudentFilterId: Int? = null,
    val selectedSubjectFilterId: Int? = null,
    val studentNameInput: String = "",
    val subjectNameInput: String = "",
    val isAddGradeFormVisible: Boolean = false,
    val addGradeStudentId: Int? = null,
    val addGradeSubjectId: Int? = null,
    val addGradeScore: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class GradesViewModel(
    private val repository: GradesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GradesUiState())
    val uiState: StateFlow<GradesUiState> = _uiState.asStateFlow()

    private var loadingCounter: Int = 0

    init {
        refreshAll()
    }

    fun refreshAll() {
        refreshStudents()
        refreshSubjects()
        refreshGrades()
    }

    fun refreshStudents() {
        viewModelScope.launch {
            runRequest(
                request = { repository.getStudents() },
                onSuccess = { students ->
                    _uiState.update { it.copy(students = students) }
                }
            )
        }
    }

    fun refreshSubjects() {
        viewModelScope.launch {
            runRequest(
                request = { repository.getSubjects() },
                onSuccess = { subjects ->
                    _uiState.update { it.copy(subjects = subjects) }
                }
            )
        }
    }

    fun refreshGrades() {
        val filterStudent = _uiState.value.selectedStudentFilterId
        val filterSubject = _uiState.value.selectedSubjectFilterId
        viewModelScope.launch {
            runRequest(
                request = { repository.getGrades(filterStudent, filterSubject) },
                onSuccess = { grades ->
                    _uiState.update { it.copy(grades = grades) }
                }
            )
        }
    }

    fun updateStudentFilter(studentId: Int?) {
        _uiState.update { it.copy(selectedStudentFilterId = studentId) }
        refreshGrades()
    }

    fun updateSubjectFilter(subjectId: Int?) {
        _uiState.update { it.copy(selectedSubjectFilterId = subjectId) }
        refreshGrades()
    }

    fun updateStudentNameInput(value: String) {
        _uiState.update { it.copy(studentNameInput = value) }
    }

    fun updateSubjectNameInput(value: String) {
        _uiState.update { it.copy(subjectNameInput = value) }
    }

    fun toggleAddGradeForm() {
        _uiState.update { state ->
            state.copy(isAddGradeFormVisible = !state.isAddGradeFormVisible)
        }
    }

    fun setAddGradeStudent(id: Int?) {
        _uiState.update { it.copy(addGradeStudentId = id) }
    }

    fun setAddGradeSubject(id: Int?) {
        _uiState.update { it.copy(addGradeSubjectId = id) }
    }

    fun updateAddGradeScore(value: String) {
        _uiState.update { it.copy(addGradeScore = value) }
    }

    fun submitNewStudent() {
        val name = _uiState.value.studentNameInput.trim()
        if (name.isBlank()) {
            postError("Введите имя ученика")
            return
        }
        viewModelScope.launch {
            runRequest(
                request = { repository.createStudent(name) },
                onSuccess = { student ->
                    _uiState.update { state ->
                        state.copy(
                            students = (state.students + student).sortedBy { it.name.lowercase(Locale.getDefault()) },
                            studentNameInput = ""
                        )
                    }
                }
            )
        }
    }

    fun submitNewSubject() {
        val name = _uiState.value.subjectNameInput.trim()
        if (name.isBlank()) {
            postError("Введите название предмета")
            return
        }
        viewModelScope.launch {
            runRequest(
                request = { repository.createSubject(name) },
                onSuccess = { subject ->
                    _uiState.update { state ->
                        state.copy(
                            subjects = (state.subjects + subject).sortedBy { it.name.lowercase(Locale.getDefault()) },
                            subjectNameInput = ""
                        )
                    }
                }
            )
        }
    }

    fun submitNewGrade() {
        val state = _uiState.value
        val studentId = state.addGradeStudentId
        val subjectId = state.addGradeSubjectId
        val score = state.addGradeScore.toDoubleOrNull()
        when {
            studentId == null -> {
                postError("Выберите ученика")
                return
            }

            subjectId == null -> {
                postError("Выберите предмет")
                return
            }

            score == null -> {
                postError("Введите числовое значение оценки")
                return
            }
        }
        viewModelScope.launch {
            runRequest(
                request = { repository.createGrade(studentId, subjectId, score) },
                onSuccess = {
                    refreshGrades()
                    _uiState.update {
                        it.copy(
                            isAddGradeFormVisible = false,
                            addGradeStudentId = null,
                            addGradeSubjectId = null,
                            addGradeScore = ""
                        )
                    }
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private suspend fun <T> runRequest(
        request: suspend () -> Result<T>,
        onSuccess: (T) -> Unit
    ) {
        updateLoading(true)
        val result = try {
            request()
        } catch (throwable: Throwable) {
            Result.failure(throwable)
        }

        result
            .onSuccess(onSuccess)
            .onFailure { error ->
                postError(error.message ?: "Неизвестная ошибка")
            }

        updateLoading(false)
    }

    private fun updateLoading(isStarting: Boolean) {
        loadingCounter = (loadingCounter + if (isStarting) 1 else -1).coerceAtLeast(0)
        val showLoading = loadingCounter > 0
        _uiState.update { it.copy(isLoading = showLoading) }
    }

    private fun postError(message: String) {
        _uiState.update { it.copy(errorMessage = message) }
    }
}
