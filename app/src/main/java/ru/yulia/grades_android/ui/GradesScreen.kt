package ru.yulia.grades_android.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.yulia.grades_android.data.model.Grade
import ru.yulia.grades_android.data.model.Student
import ru.yulia.grades_android.data.model.Subject
import ru.yulia.grades_android.ui.components.DropdownField

enum class GradesTab(val title: String) {
    GRADES("Оценки"),
    STUDENTS("Ученики"),
    SUBJECTS("Предметы")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradesApp(viewModel: GradesViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = rememberSnackbarHost()
    val errorMessage = uiState.errorMessage

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage)
            viewModel.clearError()
        }
    }

    var selectedTab by rememberSaveable { mutableStateOf(GradesTab.GRADES) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Журнал оценок") }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AnimatedVisibility(visible = uiState.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
            TabRow(selectedTabIndex = selectedTab.ordinal) {
                GradesTab.entries.forEach { tab ->
                    Tab(
                        selected = tab == selectedTab,
                        onClick = { selectedTab = tab },
                        text = { Text(tab.title) }
                    )
                }
            }
            when (selectedTab) {
                GradesTab.GRADES -> GradesTabContent(
                    state = uiState,
                    modifier = Modifier.weight(1f, fill = true),
                    onStudentFilterSelected = viewModel::updateStudentFilter,
                    onSubjectFilterSelected = viewModel::updateSubjectFilter,
                    onToggleAddForm = viewModel::toggleAddGradeForm,
                    onStudentSelectedForGrade = viewModel::setAddGradeStudent,
                    onSubjectSelectedForGrade = viewModel::setAddGradeSubject,
                    onScoreChanged = viewModel::updateAddGradeScore,
                    onSubmitGrade = viewModel::submitNewGrade
                )

                GradesTab.STUDENTS -> StudentsTabContent(
                    students = uiState.students,
                    nameInput = uiState.studentNameInput,
                    onNameChange = viewModel::updateStudentNameInput,
                    onAddStudent = viewModel::submitNewStudent,
                    modifier = Modifier.weight(1f, fill = true)
                )

                GradesTab.SUBJECTS -> SubjectsTabContent(
                    subjects = uiState.subjects,
                    nameInput = uiState.subjectNameInput,
                    onNameChange = viewModel::updateSubjectNameInput,
                    onAddSubject = viewModel::submitNewSubject,
                    modifier = Modifier.weight(1f, fill = true)
                )
            }
        }
    }
}

@Composable
private fun rememberSnackbarHost(): SnackbarHostState = androidx.compose.runtime.remember { SnackbarHostState() }

@Composable
private fun GradesTabContent(
    state: GradesUiState,
    modifier: Modifier = Modifier,
    onStudentFilterSelected: (Int?) -> Unit,
    onSubjectFilterSelected: (Int?) -> Unit,
    onToggleAddForm: () -> Unit,
    onStudentSelectedForGrade: (Int?) -> Unit,
    onSubjectSelectedForGrade: (Int?) -> Unit,
    onScoreChanged: (String) -> Unit,
    onSubmitGrade: () -> Unit
) {
    val selectedFilterStudent = state.students.firstOrNull { it.id == state.selectedStudentFilterId }
    val selectedFilterSubject = state.subjects.firstOrNull { it.id == state.selectedSubjectFilterId }
    val selectedStudentForGrade = state.students.firstOrNull { it.id == state.addGradeStudentId }
    val selectedSubjectForGrade = state.subjects.firstOrNull { it.id == state.addGradeSubjectId }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Фильтры",
            style = MaterialTheme.typography.titleMedium
        )
        DropdownField(
            label = "Ученик",
            items = state.students,
            selectedItem = selectedFilterStudent,
            onItemSelected = { onStudentFilterSelected(it.id) },
            allowClear = true,
            onClear = { onStudentFilterSelected(null) },
            itemLabel = { it.name }
        )
        DropdownField(
            label = "Предмет",
            items = state.subjects,
            selectedItem = selectedFilterSubject,
            onItemSelected = { onSubjectFilterSelected(it.id) },
            allowClear = true,
            onClear = { onSubjectFilterSelected(null) },
            itemLabel = { it.name }
        )
        Button(onClick = onToggleAddForm) {
            Text(if (state.isAddGradeFormVisible) "Скрыть форму" else "Добавить оценку")
        }
        AnimatedVisibility(visible = state.isAddGradeFormVisible) {
            AddGradeForm(
                students = state.students,
                subjects = state.subjects,
                selectedStudent = selectedStudentForGrade,
                selectedSubject = selectedSubjectForGrade,
                scoreInput = state.addGradeScore,
                onStudentSelected = onStudentSelectedForGrade,
                onSubjectSelected = onSubjectSelectedForGrade,
                onScoreChanged = onScoreChanged,
                onSubmit = onSubmitGrade
            )
        }
        Divider()
        Text(
            text = "Оценки",
            style = MaterialTheme.typography.titleMedium
        )
        if (state.grades.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true),
                contentAlignment = Alignment.Center
            ) {
                Text("Список оценок пуст")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f, fill = true)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(
                    items = state.grades,
                    key = { it.id }
                ) { grade ->
                    GradeCard(
                        grade = grade,
                        students = state.students,
                        subjects = state.subjects
                    )
                }
            }
        }
    }
}

@Composable
private fun AddGradeForm(
    students: List<Student>,
    subjects: List<Subject>,
    selectedStudent: Student?,
    selectedSubject: Subject?,
    scoreInput: String,
    onStudentSelected: (Int?) -> Unit,
    onSubjectSelected: (Int?) -> Unit,
    onScoreChanged: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DropdownField(
                label = "Выберите ученика",
                items = students,
                selectedItem = selectedStudent,
                onItemSelected = { onStudentSelected(it.id) },
                itemLabel = { it.name }
            )
            DropdownField(
                label = "Выберите предмет",
                items = subjects,
                selectedItem = selectedSubject,
                onItemSelected = { onSubjectSelected(it.id) },
                itemLabel = { it.name }
            )
            OutlinedTextField(
                value = scoreInput,
                onValueChange = onScoreChanged,
                label = { Text("Оценка") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = onSubmit) {
                    Text("Сохранить")
                }
            }
        }
    }
}

@Composable
private fun StudentsTabContent(
    students: List<Student>,
    nameInput: String,
    onNameChange: (String) -> Unit,
    onAddStudent: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Ученики", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = nameInput,
            onValueChange = onNameChange,
            label = { Text("Имя ученика") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Button(onClick = onAddStudent) {
            Text("Добавить ученика")
        }
        Divider()
        if (students.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true),
                contentAlignment = Alignment.Center
            ) {
                Text("Пока нет учеников")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f, fill = true)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(students, key = { it.id }) { student ->
                    Card {
                        Text(
                            text = student.name,
                            modifier = Modifier
                                .padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectsTabContent(
    subjects: List<Subject>,
    nameInput: String,
    onNameChange: (String) -> Unit,
    onAddSubject: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Предметы", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = nameInput,
            onValueChange = onNameChange,
            label = { Text("Название предмета") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Button(onClick = onAddSubject) {
            Text("Добавить предмет")
        }
        Divider()
        if (subjects.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true),
                contentAlignment = Alignment.Center
            ) {
                Text("Пока нет предметов")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f, fill = true)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(subjects, key = { it.id }) { subject ->
                    Card {
                        Text(
                            text = subject.name,
                            modifier = Modifier
                                .padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GradeCard(
    grade: Grade,
    students: List<Student>,
    subjects: List<Subject>
) {
    val studentName = grade.student
        ?: students.firstOrNull { it.id == grade.studentId }?.name
        ?: "ID ${grade.studentId}"
    val subjectName = grade.subject
        ?: subjects.firstOrNull { it.id == grade.subjectId }?.name
        ?: "ID ${grade.subjectId}"

    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = studentName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subjectName,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Оценка:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = grade.score.toString(),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}

@Composable
fun PreviewGradesContent() {
    val students = listOf(
        Student(id = 1, name = "Алексей Иванов"),
        Student(id = 2, name = "Мария Смирнова")
    )
    val subjects = listOf(
        Subject(id = 1, name = "Математика"),
        Subject(id = 2, name = "История")
    )
    val grades = listOf(
        Grade(id = 1, studentId = 1, student = "Алексей Иванов", subjectId = 1, subject = "Математика", score = 4.5),
        Grade(id = 2, studentId = 2, student = "Мария Смирнова", subjectId = 2, subject = "История", score = 5.0)
    )
    GradesTabContent(
        state = GradesUiState(
            students = students,
            subjects = subjects,
            grades = grades
        ),
        onStudentFilterSelected = {},
        onSubjectFilterSelected = {},
        onToggleAddForm = {},
        onStudentSelectedForGrade = {},
        onSubjectSelectedForGrade = {},
        onScoreChanged = {},
        onSubmitGrade = {}
    )
}
