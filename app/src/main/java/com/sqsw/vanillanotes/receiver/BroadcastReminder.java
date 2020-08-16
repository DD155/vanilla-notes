package com.sqsw.vanillanotes.receiver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.sqsw.vanillanotes.R;
import com.sqsw.vanillanotes.activities.EditActivity;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

public class BroadcastReminder extends BroadcastReceiver {
    public void onReceive (Context context, Intent intent) {
        SharedPreferences pref = context.getSharedPreferences("ID", Context.MODE_PRIVATE);
        int id = intent.getIntExtra("generatedID", 0);
        String title = pref.getString("title", null);
        String content = pref.getString("content", null);

        // Create an Intent for the activity
        Intent i = new Intent(context, EditActivity.class);
        i.putExtra("savedTitle", title);
        i.putExtra("savedText", content);
        i.putExtra("oldNote", true);
        i.putExtra("index", intent.getIntExtra("index", 0));
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(i);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(id, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "NoteChannel")
                .setSmallIcon(R.drawable.event_icon)
                .setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setAutoCancel(true)
                .setContentText(content)
                .setContentIntent(resultPendingIntent)
                .setGroupSummary(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, builder.build());
    }
}
