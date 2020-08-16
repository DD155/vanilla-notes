package com.sqsw.vanillanotes.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class ReceiverOnReboot extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent notif = new Intent();
            SharedPreferences sharedPreferences =
                    context.getSharedPreferences("ID", Context.MODE_PRIVATE);
            int id = sharedPreferences.getInt("id", 0);
            int index = sharedPreferences.getInt("index", 0);
            String title = sharedPreferences.getString("title" + id, "");
            String content = sharedPreferences.getString("content" + id, "");
            notif.putExtra("title", title);
            notif.putExtra("content", content);
            notif.putExtra("id", id);
            notif.putExtra("id", index);

            RebootService.enqueueWork(context, notif);
        }
    }
}
