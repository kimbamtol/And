package com.and.alarm

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.prolificinteractive.materialcalendarview.CalendarDay
import java.util.Calendar

class AlarmFunctions(private val context: Context?) {
    private lateinit var pendingIntent: PendingIntent

    @SuppressLint("SimpleDateFormat", "ScheduleExactAlarm")
    fun callAlarm(time: Long, alarmCode: Int, weeks: Array<Boolean>?) {
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val receiverIntent = Intent(context, AlarmReceiver::class.java) //리시버로 전달될 인텐트 설정

        receiverIntent.apply {
            putExtra("alarm_rqCode", alarmCode) //요청 코드를 리시버에 전달
            putExtra("week", weeks)
        }

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                alarmCode,
                receiverIntent,
                PendingIntent.FLAG_IMMUTABLE
            )

        val alarmClock = AlarmManager.AlarmClockInfo(getMatchAlarmTime(time), pendingIntent)
        alarmManager.setAlarmClock(alarmClock, pendingIntent)
    }

    fun cancelAlarm(alarmCode: Int) {
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        pendingIntent = PendingIntent.getBroadcast(context, alarmCode, intent, PendingIntent.FLAG_IMMUTABLE)
        alarmManager.cancel(pendingIntent)
    }

    private fun getMatchAlarmTime(time: Long): Long {
        val temp = Calendar.getInstance().apply {
            timeInMillis = time
        }

        if (System.currentTimeMillis() < time) {
            return temp.timeInMillis
        }

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, temp.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, temp.get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)
        }

        if (System.currentTimeMillis() > calendar.timeInMillis) {
            calendar.add(Calendar.DATE, 1)
        }

        return calendar.timeInMillis
    }
}