package me.drakeet.inmessage.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.squareup.otto.Subscribe;

import me.drakeet.inmessage.R;
import me.drakeet.inmessage.events.BusProvider;
import me.drakeet.inmessage.events.ReceiveMessageEvent;
import me.drakeet.inmessage.model.Message;
import me.drakeet.inmessage.utils.ClipboardUtils;
import me.drakeet.inmessage.utils.NotificationUtils;
import me.drakeet.inmessage.utils.ToastUtils;

/**
 * Created by shengkun on 15/6/11.
 */
public class DiscernCaptchasService extends Service {

    public static boolean isAlive = false;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        BusProvider.getInstance().register(this);
        isAlive = true;
    }


    @Subscribe
    public void onReceiveMessageEvent(ReceiveMessageEvent event) {
        Message message = event.message;
        if(message.getCaptchas() != null) {
            ClipboardUtils.putTextIntoClipboard(DiscernCaptchasService.this, message.getCaptchas());
            // 弹两遍，加长时间。
            ToastUtils.showLong(String.format(getResources().getString(R.string.tip), message.getCaptchas()));
            ToastUtils.showLong(String.format(getResources().getString(R.string.tip), message.getCaptchas()));
        }
        NotificationUtils.showMessageInNotificationBar(DiscernCaptchasService.this, message);
        DiscernCaptchasService.this.stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BusProvider.getInstance().unregister(this);
        isAlive = false;
    }
}
