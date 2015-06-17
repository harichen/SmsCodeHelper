package me.drakeet.inmessage.api;

import java.util.List;

import me.drakeet.inmessage.model.Message;

/**
 * Created by shengkun on 15/2/9.
 */
public interface SmsCallback {
    void done(List<Message> examples,String errorMessage);
}
