package me.drakeet.inmessage.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;

import org.litepal.crud.DataSupport;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.drakeet.inmessage.model.Message;

/**
 * Created by shengkun on 15/6/8.
 */
public class SmsUtils {

    Context mContext;

    public SmsUtils(final Context context) {
        mContext = context;
    }

    public static final Uri MMSSMS_ALL_MESSAGE_URI = Uri.parse("content://sms/");
    public static final Uri ALL_MESSAGE_URI = MMSSMS_ALL_MESSAGE_URI.buildUpon().
            appendQueryParameter("simple", "true").build();

    private static final String[] ALL_THREADS_PROJECTION = {
            "_id", "address", "person", "body",
            "date", "type", "thread_id"};

    public List<Message> getAllCaptchMessages() {
        List<String> dateGroupS = new ArrayList<>();
        ContentResolver cr = mContext.getContentResolver();
        Cursor cursor = cr.query(ALL_MESSAGE_URI, ALL_THREADS_PROJECTION,
                null, null, "date desc");
        List<Message> smsMessages = new ArrayList<>();
        int i = 0;
        while ((cursor.moveToNext())) {
            i += 1;
            int index_Body = cursor.getColumnIndex("body");
            int index_Address = cursor.getColumnIndex("address");
            int index_ThreadId = cursor.getColumnIndex("thread_id");
            String strbody = cursor.getString(index_Body);
            String strAddress = cursor.getString(index_Address);
            if (!StringUtils.isPersonalMoblieNO(strAddress) && StringUtils.isCaptchasMessage(strbody) && !StringUtils.tryToGetCaptchas(strbody).equals("")) {
                int date = cursor.getColumnIndex("date");
                //格式化短信日期提示
                SimpleDateFormat sfd = new SimpleDateFormat("MM-dd hh:mm");
                Date date_format = new Date(Long.parseLong(cursor.getString(date)));
                long thread_id = cursor.getLong(index_ThreadId);

                String time = sfd.format(date_format);
                //获得短信的各项内容
                String date_mms = time;
                Message message = new Message();
                String company = StringUtils.getContentInBracket(strbody, strAddress);
                if (company != null) {
                    message.setCompanyName(company);
                }
                String captchas = StringUtils.tryToGetCaptchas(strbody);
                if(!captchas.equals("")) {
                    message.setCaptchas(captchas);
                }
                int index_Id = cursor.getColumnIndex("_id");
                String smsId = cursor.getString(index_Id);
                message.setIsMessage(true);
                message.setDate(date_format);
                message.setSender(strAddress);
                message.setThreadId(thread_id);
                message.setContent(strbody);
                message.setSmsId(smsId);
                message.setReceiveDate(date_mms);
                String resultContent = StringUtils.getResultText(message, false);
                if(resultContent != null) {
                    message.setResultContent(resultContent);
                }
                smsMessages.add(message);
            }
        }

        List<Message> localMessages = DataSupport.where("readStatus = ?", "0").order("date asc").find(Message.class);
        for(Message message:localMessages) {
            if(message.getDate() != null) {
                message.setIsMessage(true);
                boolean find = false;
                for(int u = 0; u< smsMessages.size();u ++ ) {
                    if(message.getDate().getTime() > smsMessages.get(u).getDate().getTime()) {
                        smsMessages.add(u, message);
                        find = true;
                        break;
                    }
                }
                if(!find) {
                    smsMessages.add(message);
                }

            }
        }


        List<Message> unionMessages = new ArrayList<>();
        for(Message message: smsMessages) {
            String group = TimeUtils.getInstance().getDateGroup(message.getDate());
            if (dateGroupS.size() == 0) {
                dateGroupS.add(group);
                Message dateMessage = new Message();
                dateMessage.setReceiveDate(group);
                dateMessage.setIsMessage(false);
                unionMessages.add(dateMessage);
            } else {
                if (!group.equals(dateGroupS.get(dateGroupS.size() - 1))) {
                    dateGroupS.add(group);
                    Message dateMessage = new Message();
                    dateMessage.setReceiveDate(group);
                    dateMessage.setIsMessage(false);
                    unionMessages.add(dateMessage);
                }
            }
            unionMessages.add(message);
        }



        cursor.close();
        return unionMessages;
    }

    /**
     * 删除手机短信
     *
     * */
    public int deleteSms(String smsId) {
        final Uri SMS_URI = Uri.parse("content://sms/");
        int result = mContext.getContentResolver().delete(SMS_URI,"_id=?",new String[]{smsId});
        return result;
    }

    public String getContactNameFromPhoneBook(String phoneNum) {
        String contactName = null;
        String[] projection = {ContactsContract.PhoneLookup.DISPLAY_NAME,
                ContactsContract.PhoneLookup.NUMBER};
        Uri uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNum));
        try {
            Cursor cursor = mContext.getContentResolver().query(uri, projection,
                    null, null, null);

            if (cursor.moveToFirst()) {
                contactName = cursor
                        .getString(cursor
                                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            }
            cursor.close();
        } catch (IllegalArgumentException e) {
            return null;
        }
        return contactName;
    }

    // 根据号码获得联系人头像
    public Bitmap get_people_image(String x_number) {

        // 获得Uri
        Uri uriNumber2Contacts = Uri.parse("content://com.android.contacts/"
                + "data/phones/filter/" + x_number);
        // 查询Uri，返回数据集
        Cursor cursorCantacts = mContext.getContentResolver().query(
                uriNumber2Contacts,
                null,
                null,
                null,
                null);
        // 如果该联系人存在
        if (cursorCantacts.getCount() > 0) {
            // 移动到第一条数据
            cursorCantacts.moveToFirst();
            // 获得该联系人的contact_id
            Long contactID = cursorCantacts.getLong(cursorCantacts.getColumnIndex("contact_id"));
            cursorCantacts.close();
            // 获得contact_id的Uri
            Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactID);
            // 打开头像图片的InputStream
            InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(mContext.getContentResolver(), uri);
            // 从InputStream获得bitmap
            Bitmap bmp_head = BitmapFactory.decodeStream(input);
            return bmp_head;
        }
        cursorCantacts.close();
        return null;
    }
}
