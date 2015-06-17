package me.drakeet.inmessage.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import me.drakeet.inmessage.R;
import me.drakeet.inmessage.constant.StaticObjectInterface;
import me.drakeet.inmessage.model.Message;

public class NotificationUtils implements StaticObjectInterface {

    public static void showMessageInNotificationBar(Context context, Message message) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context).setSmallIcon(R.mipmap.ic_incode_small);
        builder.setAutoCancel(true);

        RemoteViews remoteViews;
        remoteViews = new RemoteViews(context.getPackageName(), R.layout.view_notification);

        remoteViews.setTextViewText(R.id.tv_title, message.getSender());

        if (message.getCaptchas() != null) {
            remoteViews.setTextViewText(
                    R.id.tv_content, String.format(context.getResources().getString(R.string.notify_msg), message.getCaptchas())
            );
        } else {
            remoteViews.setTextViewText(R.id.tv_content, message.getContent());
        }
        builder.setContent(remoteViews);
        Notification notification = builder.build();
        //设定Notification出现时的声音
        notification.defaults |= Notification.DEFAULT_SOUND;
        //设定如何振动
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(
                Context.NOTIFICATION_SERVICE
        );
        Intent notificationIntent = new Intent(ACTION_CLICK);
        notificationIntent.putExtra("captchas", message.getCaptchas());
        PendingIntent broadcast = PendingIntent.getBroadcast(
                context, 1, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT
        );
        notification.contentView.setOnClickPendingIntent(R.id.tv_content, broadcast);
        int mNotificationId = 52494791;
        notificationManager.notify(mNotificationId, notification);
    }
}
