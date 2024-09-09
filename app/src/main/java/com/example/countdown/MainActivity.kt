package com.example.countdown

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.CalendarView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.countdown.ui.theme.CountDownTheme
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve saved date from SharedPreferences
        val prefs = getSharedPreferences("com.example.countdown.widget", Context.MODE_PRIVATE)
        val targetDateMillis = prefs.getLong("targetDate", LocalDate.now().toEpochDay())
        val savedDate = LocalDate.ofEpochDay(targetDateMillis)

        setContent {
            CountDownTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    DateSelector(
                        modifier = Modifier.padding(innerPadding),
                        onDateSelected = { selectedDate ->
                            saveTargetDate(context = this, targetDate = selectedDate)
                        },
                        initialDate = savedDate // Pass the saved date to DateSelector
                    )
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveTargetDate(context: Context, targetDate: LocalDate) {
        val prefs = context.getSharedPreferences("com.example.countdown.widget", Context.MODE_PRIVATE)
        prefs.edit().putLong("targetDate", targetDate.toEpochDay()).apply()

        // Notify the widget to update
        val intent = Intent(context, CountDownWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        }
        val ids = AppWidgetManager.getInstance(context)
            .getAppWidgetIds(ComponentName(context, CountDownWidgetProvider::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        context.sendBroadcast(intent)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable

fun DateSelector(modifier: Modifier = Modifier, onDateSelected: (LocalDate) -> Unit, initialDate: LocalDate = LocalDate.now()) {
    var selectedDate by remember { mutableStateOf(initialDate) }

    Column(modifier = modifier.padding(16.dp)) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Made by Bence <3",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Normal,
                fontSize = MaterialTheme.typography.bodySmall.fontSize
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(128.dp))

        val isDarkTheme = isSystemInDarkTheme()

        AndroidView(
            factory = { context ->
                CalendarView(context).apply {
                    if (isDarkTheme) {
                        setBackgroundColor(Color.Gray.toArgb())
                    }
                    // Set initial date to the current selection
                    date = selectedDate.toEpochDay() * 86400000 // Convert to milliseconds
                    setOnDateChangeListener { _, year, month, dayOfMonth ->
                        selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                        onDateSelected(selectedDate)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        val currentDate = LocalDate.now()
        val daysRemaining = ChronoUnit.DAYS.between(currentDate, selectedDate).toInt()

        Text(
            text = "Days Remaining: $daysRemaining",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = MaterialTheme.typography.titleLarge.fontSize
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = buildAnnotatedString {
                append("Crafted in haste but with deep affection for ")
                withStyle(style = androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("Sofia")
                }
                withStyle(style = androidx.compose.ui.text.SpanStyle(fontStyle = FontStyle.Italic)) {
                    append("—to count down the days until we meet again.")
                }
            },
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Start
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    CountDownTheme {
        DateSelector(onDateSelected = {})
    }
}
