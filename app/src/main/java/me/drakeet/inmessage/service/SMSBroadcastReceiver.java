package me.drakeet.inmessage.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsMessage;

import com.badoo.mobile.util.WeakHandler;

import java.text.SimpleDateFormat;
import java.util.Date;

import me.drakeet.inmessage.events.BusProvider;
import me.drakeet.inmessage.events.ReceiveMessageEvent;
import me.drakeet.inmessage.model.Message;
import me.drakeet.inmessage.utils.StringUtils;
import me.drakeet.inmessage.utils.VersionUtils;

/**
 * Created by shengkun on 15/6/11.
 */
public class SMSBroadcastReceiver extends BroadcastReceiver {

    Intent mServiceIntent;
    WeakHandler mHandler;

    @Override
    public void onReceive(Context context, Intent intent) {
        //从Intent中接受信息
        Object[] pdus = (Object[]) intent.getExtras().get("pdus");
        for (Object p : pdus) {
            byte[] sms = (byte[]) p;
            SmsMessage message = SmsMessage.createFromPdu(sms);
            //获取短信内容
            final String content = message.getMessageBody();
            //获取发送时间
            final Date date = new Date(message.getTimestampMillis());
            final String sender = message.getOriginatingAddress();
            if (!StringUtils.isPersonalMoblieNO(sender) && StringUtils.isCaptchasMessage(content) && !StringUtils
                    .tryToGetCaptchas(content)
                    .equals("")) {
                if (!DiscernCaptchasService.isAlive) {
                    mServiceIntent = new Intent(context, DiscernCaptchasService.class);
                    context.startService(mServiceIntent);
                }
                this.abortBroadcast();
                mHandler = new WeakHandler();
                mHandler.postDelayed(new Runnable() {
                                         @Override
                                         public void run() {
                                             Message smsMessage = new Message();
                                             smsMessage.setContent(content);
                                             smsMessage.setSender(sender);
                                             smsMessage.setDate(date);
                                             String company = StringUtils.getContentInBracket(content, sender);
                                             if (company != null) {
                                                 smsMessage.setCompanyName(company);
                                             }
                                             smsMessage.setIsMessage(true);
                                             //格式化短信日期提示
                                             SimpleDateFormat sfd = new SimpleDateFormat("MM-dd hh:mm");
                                             String time = sfd.format(date);
                                             //获得短信的各项内容
                                             String date_mms = time;
                                             smsMessage.setReceiveDate(date_mms);
                                             smsMessage.setReadStatus(0);
                                             smsMessage.setFromSmsDB(1);
                                             String captchas = StringUtils.tryToGetCaptchas(content);
                                             if(!captchas.equals("")) {
                                                 smsMessage.setCaptchas(captchas);
                                             }
                                             String resultContent = StringUtils.getResultText(smsMessage, false);
                                             if(resultContent != null) {
                                                 smsMessage.setResultContent(resultContent);
                                             }
                                             if(!VersionUtils.IS_MORE_THAN_LOLLIPOP) {
                                                 smsMessage.save();
                                             }
                                             BusProvider.getInstance().register(this);
                                             BusProvider.getInstance().post(new ReceiveMessageEvent(smsMessage));
                                             BusProvider.getInstance().unregister(this);
                                             //终止广播
                                         }
                                     }, 358
                );
            }
        }
    }

}
