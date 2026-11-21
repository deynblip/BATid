package com.batid.app.ui.admin

import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.batid.app.data.model.Announcement
import com.batid.app.data.model.Category
import com.batid.app.data.model.Program
import com.batid.app.data.model.PublishStatus
import com.batid.app.data.model.YearLevel
import com.batid.app.data.model.categoryEnum
import com.batid.app.data.model.isEmergency
import com.batid.app.data.model.isPublished
import com.batid.app.data.model.programEnums
import com.batid.app.data.model.yearLevelEnum
import com.batid.app.data.repository.AnnouncementStore
import com.batid.app.ui.auth.AuthErrorText
import com.batid.app.ui.auth.AuthLabel
import com.batid.app.ui.components.BatidButton
import com.batid.app.ui.components.BatidHeader
import com.batid.app.ui.components.BatidSheet
import com.batid.app.ui.components.FilterPill
import com.batid.app.ui.theme.BorderGray
import com.batid.app.ui.theme.EmergencyBorder
import com.batid.app.ui.theme.EmergencyRed
import com.batid.app.ui.theme.EmergencyTint
import com.batid.app.ui.theme.IceBlue
import com.batid.app.ui.theme.InkNavy
import com.batid.app.ui.theme.NavyBlue
import com.batid.app.ui.theme.RoyalBlue
import com.batid.app.ui.theme.SlateGray

private val StringSetSaver = listSaver<Set<String>, String>(save = { it.toList() }, restore = { it.toSet() })
private val FormCategories = listOf(Category.ACADEMICS, Category.EVENTS, Category.GENERAL)
private val AllPrograms: Set<String> = Program.entries.map { it.name }.toSet()

/**
 * Create a new announcement ([existing] = null) or edit one.
 * [onBack] = null when it is shown as the "New" bottom tab (no back arrow).
 */
@Composable
fun AnnouncementFormScreen(
    existing: Announcement?,
    onBack: (() -> Unit)?,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val editing = existing != null

    var title by rememberSaveable(existing?.id) { mutableStateOf(existing?.title.orEmpty()) }
    var body by rememberSaveable(existing?.id) { mutableStateOf(existing?.body.orEmpty()) }
    var categoryName by rememberSaveable(existing?.id) {
        mutableStateOf((existing?.categoryEnum ?: Category.GENERAL).name)
    }
    var programNames by rememberSaveable(existing?.id, stateSaver = StringSetSaver) {
        mutableStateOf<Set<String>>(existing?.programEnums?.map { it.name }?.toSet() ?: AllPrograms)
    }
    var yearName by rememberSaveable(existing?.id) {
        mutableStateOf((existing?.yearLevelEnum ?: YearLevel.ALL).name)
    }
    var submitted by remember { mutableStateOf(false) }

    val category = Category.fromName(categoryName)
    val year = YearLevel.fromName(yearName)
    val programs = Program.entries.filter { it.name in programNames }

    val titleOk = title.isNotBlank()
    val bodyOk = body.isNotBlank()
    val programOk = programs.isNotEmpty()
    val categoryChoices = if (existing?.isEmergency == true) Category.entries.toList() else FormCategories

    fun toast(message: String) = Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

    fun submit(status: PublishStatus) {
        submitted = true
        if (!titleOk || !bodyOk || !programOk) return
        val cleanTitle = title.trim()
        val cleanBody = body.trim()

        if (existing == null) {
            AnnouncementStore.publish(cleanTitle, cleanBody, category, programs, year, status) { toast(it) }
            toast(if (status == PublishStatus.PUBLISHED) "Announcement published" else "Draft saved")
            // Clear the form so the "New" tab is blank next time.
            title = ""
            body = ""
            categoryName = Category.GENERAL.name
            programNames = AllPrograms
            yearName = YearLevel.ALL.name
            submitted = false
        } else {
            val unchanged = existing.title == cleanTitle &&
                    existing.body == cleanBody &&
                    existing.categoryEnum == category &&
                    existing.programEnums.toSet() == programs.toSet() &&
                    existing.yearLevelEnum == year &&
                    existing.isPublished == (status == PublishStatus.PUBLISHED)
            if (unchanged) {
                toast("No changes to save")
            } else {
                AnnouncementStore.update(existing.id, cleanTitle, cleanBody, category, programs, year, status) { toast(it) }
                toast(
                    when {
                        status == PublishStatus.DRAFT -> "Draft saved"
                        !existing.isPublished -> "Announcement published"
                        else -> "Changes saved"
                    }
                )
            }
        }
        onDone()
    }

    Column(modifier = modifier.fillMaxSize()) {
        BatidHeader(
            title = if (editing) "Edit announcement" else "New announcement",
            onBack = onBack
        )
        BatidSheet(modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    FormField(
                        label = "Title",
                        value = title,
                        onValueChange = { title = it },
                        placeholder = "e.g. Room change for CS 312",
                        singleLine = true,
                        error = if (submitted && !titleOk) "Enter a title." else null
                    )
                    Spacer(Modifier.height(16.dp))
                    FormField(
                        label = "Message",
                        value = body,
                        onValueChange = { body = it },
                        placeholder = "Write the announcement. Leave a blank line between paragraphs.",
                        minLines = 6,
                        error = if (submitted && !bodyOk) "Enter the message." else null
                    )
                    Spacer(Modifier.height(16.dp))
                    AuthLabel("Category")
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categoryChoices.forEach { c ->
                            FilterPill(label = c.label, selected = category == c, onClick = { categoryName = c.name })
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    AudienceSection(
                        programNames = programNames,
                        onProgramsChange = { programNames = it },
                        yearName = yearName,
                        onYearChange = { yearName = it },
                        summary = audienceSummary(programs, year),
                        programError = if (submitted && !programOk) "Pick at least one program." else null
                    )
                }
                HorizontalDivider(color = BorderGray)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        // On the "New" tab the bottom bar already handles the nav-bar inset.
                        .then(if (onBack != null) Modifier.navigationBarsPadding() else Modifier)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (existing == null || !existing.isPublished) {
                        BatidButton(
                            text = "Save draft",
                            onClick = { submit(PublishStatus.DRAFT) },
                            outlined = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    BatidButton(
                        text = if (existing?.isPublished == true) "Save changes" else "Publish",
                        onClick = { submit(PublishStatus.PUBLISHED) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/** Emergency alert: same idea, but always published as EMERGENCY and asks for confirmation. */
@Composable
fun EmergencyAlertScreen(
    onBack: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var title by rememberSaveable { mutableStateOf("") }
    var body by rememberSaveable { mutableStateOf("") }
    var programNames by rememberSaveable(stateSaver = StringSetSaver) { mutableStateOf<Set<String>>(AllPrograms) }
    var yearName by rememberSaveable { mutableStateOf(YearLevel.ALL.name) }
    var submitted by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    val year = YearLevel.fromName(yearName)
    val programs = Program.entries.filter { it.name in programNames }
    val titleOk = title.isNotBlank()
    val bodyOk = body.isNotBlank()
    val programOk = programs.isNotEmpty()
    val summary = audienceSummary(programs, year)

    Column(modifier = modifier.fillMaxSize()) {
        BatidHeader(title = "Emergency alert", subtitle = "For urgent notices only", onBack = onBack)
        BatidSheet(modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = EmergencyTint,
                        border = BorderStroke(1.dp, EmergencyBorder)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                            Icon(Icons.Outlined.WarningAmber, contentDescription = null, tint = EmergencyRed)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "Use this for class suspensions and urgent notices. It appears at the top of every selected student's feed and in their notifications.",
                                style = MaterialTheme.typography.labelSmall,
                                color = InkNavy
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    FormField(
                        label = "Headline",
                        value = title,
                        onValueChange = { title = it },
                        placeholder = "e.g. Classes suspended tomorrow",
                        singleLine = true,
                        error = if (submitted && !titleOk) "Enter a headline." else null
                    )
                    Spacer(Modifier.height(16.dp))
                    FormField(
                        label = "Details",
                        value = body,
                        onValueChange = { body = it },
                        placeholder = "What should students know or do?",
                        minLines = 5,
                        error = if (submitted && !bodyOk) "Enter the details." else null
                    )
                    Spacer(Modifier.height(16.dp))
                    AudienceSection(
                        programNames = programNames,
                        onProgramsChange = { programNames = it },
                        yearName = yearName,
                        onYearChange = { yearName = it },
                        summary = summary,
                        programError = if (submitted && !programOk) "Pick at least one program." else null
                    )
                }
                HorizontalDivider(color = BorderGray)
                Button(
                    onClick = {
                        submitted = true
                        if (titleOk && bodyOk && programOk) showConfirm = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp)
                        .heightIn(min = 48.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed, contentColor = Color.White)
                ) {
                    Icon(Icons.Outlined.WarningAmber, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Send emergency alert", style = MaterialTheme.typography.labelLarge, maxLines = 1)
                }
            }
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            containerColor = Color.White,
            title = { Text("Send emergency alert?", style = MaterialTheme.typography.titleMedium, color = InkNavy) },
            text = {
                Text(
                    "This goes to $summary right away. You can still edit or delete it later from Manage.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateGray
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    AnnouncementStore.publish(
                        title.trim(), body.trim(), Category.EMERGENCY, programs, year, PublishStatus.PUBLISHED
                    ) { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
                    Toast.makeText(context, "Emergency alert sent", Toast.LENGTH_SHORT).show()
                    showConfirm = false
                    onDone()
                }) { Text("Send alert", color = EmergencyRed, style = MaterialTheme.typography.labelLarge) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text("Cancel", color = NavyBlue, style = MaterialTheme.typography.labelLarge)
                }
            }
        )
    }
}

// ——— shared pieces ———

private fun audienceSummary(programs: List<Program>, year: YearLevel): String =
    if (programs.isEmpty()) "Select at least one program"
    else programs.joinToString(", ") { it.label } + " · " + year.label

@Composable
private fun AudienceSection(
    programNames: Set<String>,
    onProgramsChange: (Set<String>) -> Unit,
    yearName: String,
    onYearChange: (String) -> Unit,
    summary: String,
    programError: String?
) {
    AuthLabel("Send to program")
    Spacer(Modifier.height(8.dp))
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Program.entries.forEach { p ->
            val selected = p.name in programNames
            FilterPill(
                label = p.label,
                selected = selected,
                onClick = { onProgramsChange(if (selected) programNames - p.name else programNames + p.name) }
            )
        }
    }
    AuthErrorText(programError)
    Spacer(Modifier.height(16.dp))
    AuthLabel("Year level")
    Spacer(Modifier.height(8.dp))
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        YearLevel.entries.forEach { y ->
            FilterPill(label = y.chipLabel, selected = yearName == y.name, onClick = { onYearChange(y.name) })
        }
    }
    Spacer(Modifier.height(12.dp))
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = IceBlue) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text("Will be seen by", style = MaterialTheme.typography.labelSmall, color = SlateGray)
            Text(summary, style = MaterialTheme.typography.labelMedium, color = NavyBlue)
        }
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
    minLines: Int = 1,
    error: String? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        AuthLabel(label)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, style = MaterialTheme.typography.bodySmall, color = SlateGray) },
            singleLine = singleLine,
            minLines = if (singleLine) 1 else minLines,
            isError = error != null,
            shape = RoundedCornerShape(10.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = InkNavy),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = if (singleLine) ImeAction.Next else ImeAction.Default
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NavyBlue,
                unfocusedBorderColor = RoyalBlue,
                errorBorderColor = EmergencyRed,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                errorContainerColor = Color.White,
                cursorColor = NavyBlue
            )
        )
        AuthErrorText(error)
    }
}