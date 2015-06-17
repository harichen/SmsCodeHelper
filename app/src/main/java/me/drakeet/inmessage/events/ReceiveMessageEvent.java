package me.drakeet.inmessage.events;

import me.drakeet.inmessage.model.Message;

/**
 * Created by shengkun on 15/6/11.
 */
public class ReceiveMessageEvent {
    public Message message;

    public ReceiveMessageEvent(Message message) {
        this.message = message;
    }
}
