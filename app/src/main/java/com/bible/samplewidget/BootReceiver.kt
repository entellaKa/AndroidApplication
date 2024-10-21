package com.bible.samplewidget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidgetManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        // CoroutineScope를 사용하여 suspend 함수 호출
        CoroutineScope(Dispatchers.Main).launch {
            val glanceManager = GlanceAppWidgetManager(context)

            // suspend 함수 호출
            val glanceIds = glanceManager.getGlanceIds(MyGlanceWidget::class.java)

            // 각 glanceId에 대해 업데이트 호출
            glanceIds.forEach { glanceId ->
                MyGlanceWidget().updateall(context, glanceId)
            }
        }
    }
}