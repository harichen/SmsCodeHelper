package me.drakeet.inmessage.service;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import me.drakeet.inmessage.utils.ClipboardUtils;

/**
 * Created by shengkun on 15/6/16.
 */
public class ClearReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String captchas = intent.getStringExtra("captchas");
        if (captchas != null) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(
                    Context.NOTIFICATION_SERVICE
            );
            notificationManager.cancel(52494791);
            ClipboardUtils.putTextIntoClipboard(context, captchas);
        }
    }
}
