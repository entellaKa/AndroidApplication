package com.bible.samplewidget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bible.samplewidget.ui.theme.SampleWidgetTheme
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.runtime.LaunchedEffect
import java.util.Calendar

const val TAG: String = "MainActivity"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SampleWidgetTheme {
                MyApp()
            }
        }
        scheduleAlarm(this)
    }

    @Composable
    fun MyApp() {
        // Firestore로부터 가져온 데이터 저장할 상태
        var itemsList by remember { mutableStateOf<List<Chapter>>(emptyList()) }
        var todayVerse by remember { mutableStateOf("null") }

        // Firestore에서 데이터를 가져오고 상태를 업데이트
        LaunchedEffect(Unit) {
            callTodayVerse { verse ->
                todayVerse = verse
            }
            getVerseList { verses ->
                itemsList = verses
            }
        }

        Log.d("Chapter", "Composing ${itemsList.size}")
        Scaffold(
            content = { paddingValues ->
                // LazyColumn에 padding을 적용
                ItemList(items = itemsList, todayVerse = todayVerse, paddingValues = paddingValues)
            }
        )
    }

    private fun callTodayVerse(onVerseLoaded: (String) -> Unit) {
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()

        db.collection("today")
            .document("ra3ee4s2NKHaWuQ9MkOC")
            .get()
            .addOnSuccessListener { verses ->
                val verse = verses.data?.get("verse").toString()
                Log.d("Chapter", "call Today Verse: $verse")
                onVerseLoaded(verse)  // 데이터를 가져온 후 콜백 호출
            }
    }

    private fun getVerseList(onVersesLoaded: (List<Chapter>) -> Unit) {
        val chapters = ArrayList<Chapter>()
        Log.d("Chapter", "Start get bible verses")

        val db: FirebaseFirestore = FirebaseFirestore.getInstance()

        db.collection("bible verse").document("sPRnqSRqM8mIjFEqzaAe")
            .get()
            .addOnSuccessListener { verses ->
                verses.data?.forEach { (key, value) ->
                    chapters.add(Chapter(key, value.toString()))
                }
                Log.d("Chapter", "Success. List length: ${chapters.size}")
                onVersesLoaded(chapters)  // 데이터를 가져온 후 콜백 호출
            }.addOnFailureListener {
                Log.w("Chapter", "Fail to get Verse from Firestore")
            }
    }

    @Composable
    fun ItemList(items: List<Chapter>, todayVerse: String, paddingValues: PaddingValues) {
        // LazyColumn으로 리스트 렌더링
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            items(
                items = items,
                itemContent = {
                    ListItem(
                        chapter = it.chapter,
                        verse = it.verse,
                        todayVerse = todayVerse
                    )
                }
            )
        }
    }

    @Composable
    fun ListItem(chapter: String, verse: String, todayVerse: String) {
        var backgroundColor = Color(0f, 0f, 0f, 0f)
        if (todayVerse == chapter)
            backgroundColor = Color(0.8f, 0.8f, 1f)

        // 각 항목을 Text로 표시
        Column(
            modifier = Modifier
                .padding(16.dp)
                .background(backgroundColor)
                .clickable { Log.d("item Click event", "item $chapter is click") },


            ) {
            Text(
                text = chapter,
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = verse,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(horizontal = 15.dp)
            )
            Spacer(
                modifier = Modifier
                    .background(color = Color.Black)
                    .padding(1.dp)
                    .fillMaxWidth()
            )
        }
    }

    fun scheduleAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, BootReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        // 자정 시간 설정
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // 현재 시간보다 이전이면 다음 날로 설정
            if (timeInMillis < System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        // 매일 자정에 알람 설정
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }
}

data class Chapter(val chapter: String, val verse: String)
