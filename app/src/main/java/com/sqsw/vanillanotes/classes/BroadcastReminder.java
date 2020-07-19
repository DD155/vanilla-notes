package com.sqsw.vanillanotes.classes;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.sqsw.vanillanotes.R;
import com.sqsw.vanillanotes.activities.NoteEditActivity;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

public class BroadcastReminder extends BroadcastReceiver {
    public void onReceive (Context context, Intent intent) {
        SharedPreferences pref = context.getSharedPreferences("ID", Context.MODE_PRIVATE);
        int id = intent.getIntExtra("gen_id", 0);

        String title = pref.getString("title"+id, null);
        String content = pref.getString("content"+id, null);

        Log.d("notif_test2", "Retrieved ID: "+ id);

        // Create an Intent for the activity
        Intent i = new Intent(context, NoteEditActivity.class);
        i.putExtra("savedTitle", title);
        i.putExtra("savedText", content);
        i.putExtra("caller", intent.getStringExtra("caller"));
        i.putExtra("index", intent.getIntExtra("index", 0));
        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(i);
        // Get the PendingIntent containing the entire back stack
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

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, builder.build());
    }
}
