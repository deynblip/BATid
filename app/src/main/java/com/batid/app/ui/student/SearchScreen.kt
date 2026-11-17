package com.batid.app.ui.student

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.batid.app.data.model.Announcement
import com.batid.app.data.model.Category
import com.batid.app.data.model.Program
import com.batid.app.data.model.YearLevel
import com.batid.app.data.model.categoryEnum
import com.batid.app.data.model.programEnums
import com.batid.app.data.model.yearLevelEnum
import com.batid.app.data.sample.SampleAnnouncements
import com.batid.app.ui.components.AnnouncementCard
import com.batid.app.ui.components.BatidSheet
import com.batid.app.ui.components.EmptyState
import com.batid.app.ui.components.FilterPill
import com.batid.app.ui.theme.BATidTheme
import com.batid.app.ui.theme.InkNavy
import com.batid.app.ui.theme.NavyBlue
import com.batid.app.ui.theme.NavyDark
import com.batid.app.ui.theme.ScreenBackground
import com.batid.app.ui.theme.SlateGray

private val searchCategories = listOf(Category.ACADEMICS, Category.EVENTS, Category.GENERAL)
private val searchPrograms = listOf(Program.BSCS, Program.BSIT)
private val searchYears = listOf(YearLevel.FIRST, YearLevel.SECOND, YearLevel.THIRD, YearLevel.FOURTH)

/**
 * Student Search + filters.
 * [announcements] is already the student's own feed (program/year audience applied),
 * so search and filters only narrow down what the student is allowed to see.
 */
@Composable
fun SearchScreen(
    announcements: List<Announcement>,
    savedIds: Set<String>,
    onOpenAnnouncement: (String) -> Unit,
    onToggleSave: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf<Category?>(null) }
    var program by rememberSaveable { mutableStateOf<Program?>(null) }
    var year by rememberSaveable { mutableStateOf<YearLevel?>(null) }

    var filtersExpanded by rememberSaveable { mutableStateOf(false) }

    val hasFilters = category != null || program != null || year != null
    val activeFilterCount = listOf(category, program, year).count { it != null }

    val results = remember(announcements, query, category, program, year) {
        val q = query.trim()
        val selectedProgram: Program? = program // local copy so Kotlin can smart-cast
        announcements.filter { a ->
            (q.isEmpty() ||
                    a.title.contains(q, ignoreCase = true) ||
                    a.body.contains(q, ignoreCase = true)) &&
                    (category == null || a.categoryEnum == category) &&
                    (selectedProgram == null || selectedProgram in a.programEnums) &&
                    // an "All years" announcement applies to every year level
                    (year == null || a.yearLevelEnum == year || a.yearLevelEnum == YearLevel.ALL)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        SearchHeader(query = query, onQueryChange = { query = it })
        BatidSheet(modifier = Modifier.weight(1f), color = ScreenBackground) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    FilterToggleRow(
                        expanded = filtersExpanded,
                        activeCount = activeFilterCount,
                        onClick = { filtersExpanded = !filtersExpanded }
                    )
                }
                item {
                    AnimatedVisibility(visible = filtersExpanded) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            FilterSection("CATEGORY") {
                                FilterPill("All", category == null, { category = null }, large = true)
                                searchCategories.forEach { c ->
                                    FilterPill(c.label, category == c, { category = c }, large = true)
                                }
                            }
                            FilterSection("PROGRAM") {
                                FilterPill("All", program == null, { program = null }, large = true)
                                searchPrograms.forEach { p ->
                                    FilterPill(p.label, program == p, { program = p }, large = true)
                                }
                            }
                            FilterSection("YEAR LEVEL") {
                                FilterPill("All", year == null, { year = null }, large = true)
                                searchYears.forEach { y ->
                                    FilterPill(y.chipLabel, year == y, { year = y }, large = true)
                                }
                            }
                        }
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (results.size == 1) "1 result" else "${results.size} results",
                            style = MaterialTheme.typography.labelMedium,
                            color = InkNavy,
                            modifier = Modifier.weight(1f)
                        )
                        if (hasFilters) {
                            TextButton(onClick = {
                                category = null
                                program = null
                                year = null
                            }) {
                                Text("Clear filters", style = MaterialTheme.typography.labelMedium, color = NavyBlue)
                            }
                        }
                    }
                }
                if (results.isEmpty()) {
                    item {
                        EmptyState(
                            title = "No results found",
                            message = "Try a different keyword or clear your filters."
                        )
                    }
                } else {
                    items(results, key = { it.id }) { announcement ->
                        AnnouncementCard(
                            announcement = announcement,
                            isSaved = announcement.id in savedIds,
                            onClick = { onOpenAnnouncement(announcement.id) },
                            onToggleSave = { onToggleSave(announcement.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchHeader(query: String, onQueryChange: (String) -> Unit) {
    val focusManager = LocalFocusManager.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(NavyBlue, NavyDark)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 24.dp)
        ) {
            Text("Search", style = MaterialTheme.typography.titleLarge, color = Color.White)
            Spacer(Modifier.size(12.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(start = 14.dp, end = 4.dp, top = 2.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Search, contentDescription = null, tint = SlateGray)
                    Spacer(Modifier.width(10.dp))
                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = InkNavy),
                        cursorBrush = SolidColor(NavyBlue),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                        modifier = Modifier.weight(1f),
                        decorationBox = { inner ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (query.isEmpty()) {
                                    Text(
                                        "Search announcements",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = SlateGray
                                    )
                                }
                                inner()
                            }
                        }
                    )
                    // Keeps the row height the same whether or not the clear button shows
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Outlined.Close, contentDescription = "Clear search", tint = SlateGray)
                        }
                    } else {
                        Spacer(Modifier.size(48.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterToggleRow(expanded: Boolean, activeCount: Int, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.FilterList, contentDescription = null, tint = NavyBlue)
            Spacer(Modifier.width(10.dp))
            Text(
                text = if (activeCount > 0) "Filters ($activeCount active)" else "Filters",
                style = MaterialTheme.typography.labelLarge,
                color = NavyBlue,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                contentDescription = if (expanded) "Hide filters" else "Show filters",
                tint = SlateGray
            )
        }
    }
}

@Composable
private fun FilterSection(title: String, pills: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = SlateGray
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            pills()
        }
    }
}

// Preview-only: uses the fake sample data.
@Preview(showBackground = true, heightDp = 800)
@Composable
private fun SearchScreenPreview() {
    BATidTheme {
        SearchScreen(
            announcements = SampleAnnouncements.feedFor(Program.BSCS, YearLevel.THIRD),
            savedIds = emptySet(),
            onOpenAnnouncement = {},
            onToggleSave = {}
        )
    }
}