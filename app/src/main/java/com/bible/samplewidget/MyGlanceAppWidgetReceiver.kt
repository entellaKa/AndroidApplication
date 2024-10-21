package com.bible.samplewidget

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import kotlin.random.Random


class MyGlanceWidget : GlanceAppWidget() {
//    private val db = FirebaseFirestore.getInstance()
    private val firestoreRepository = FirestoreRepository()


    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                MyContent()
            }
        }
    }


    suspend fun updateall(context: Context, glanceId: GlanceId) {
        // 위젯 갱신
        updateAll(context)
    }

    @Composable
    private fun MyContent() {
        Log.d("Widget Log", "log start")
        var randomField by remember { mutableStateOf<String?>(null) }
        var randomValue by remember { mutableStateOf<String?>(null) }

        firestoreRepository.checkDate{isMoreThanADay ->
            if (isMoreThanADay) {
                firestoreRepository.updateTimestamp()
                firestoreRepository.updateVerse { updatedField ->
                    randomField = updatedField
                    firestoreRepository.getVerse { chapter, verse ->
                        randomValue = verse
                        Log.d("Widget Log", "get data $chapter, $verse")
                    }
                }
            } else {
                Log.d("Widget Log", "아직 하루가 경과하지 않았습니다.")
            }
        }

        myWidget(randomField, randomValue)
    }

    @Composable
    private fun myWidget(randomField: String?, randomValue: String?) {
        // Box 레이아웃을 활용
        Log.d("Widget Log", "composing widget")

        Text(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.background)
                .padding(12.dp)
                .clickable(actionStartActivity<MainActivity>()),
            text = "$randomField: $randomValue",
        )
    }



}


class MyGlanceAppWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MyGlanceWidget()
}
