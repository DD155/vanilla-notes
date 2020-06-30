package com.sqsw.vanillanotes.classes;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.sqsw.vanillanotes.R;
import com.sqsw.vanillanotes.activities.NoteEditActivity;

import java.util.Date;

import androidx.core.app.NotificationCompat;

public class BroadcastReminder extends BroadcastReceiver {
    public void onReceive (Context context, Intent intent) {
        SharedPreferences pref = context.getSharedPreferences("ID", Context.MODE_PRIVATE);
        int id = intent.getIntExtra("gen_id", 0);

        String title = pref.getString("curr_title", null);
        String content = pref.getString("curr_content", null);

        Log.d("notif_test2", "Retrieved ID: "+ id);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "NoteChannel")
                .setSmallIcon(R.drawable.ic_baseline_event_note_24)
                .setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setAutoCancel(true)
                .setContentText(content)
                //.setContentIntent(resultPendingIntent)
                .setGroupSummary(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, builder.build());
    }
}
