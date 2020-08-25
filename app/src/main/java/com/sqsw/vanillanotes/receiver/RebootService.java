package com.sqsw.vanillanotes.receiver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.sqsw.vanillanotes.R;
import com.sqsw.vanillanotes.activities.EditActivity;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

public class RebootService extends JobIntentService {

    final static int ID = 0;

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, RebootService.class, ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        //SharedPreferences pref = mContext.getSharedPreferences("ID", Context.MODE_PRIVATE);
        int id = intent.getIntExtra("id", 0);
        int index = intent.getIntExtra("index", 0);
        String title = intent.getStringExtra("title");
        String content = intent.getStringExtra("content");

        // Create an Intent for the activity
        Intent i = new Intent(getApplicationContext(), EditActivity.class);
        i.putExtra("oldNote", true);
        i.putExtra("index", index);
        i.putExtra("favorite", intent.getBooleanExtra("favorite", false));
        i.putExtra("trash", intent.getBooleanExtra("trash", false));
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
        stackBuilder.addNextIntentWithParentStack(i);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(id, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "NoteChannel")
                .setSmallIcon(R.drawable.event_icon)
                .setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setAutoCancel(true)
                .setContentText(content)
                .setContentIntent(resultPendingIntent)
                .setGroupSummary(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager notificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, builder.build());
    }
}
