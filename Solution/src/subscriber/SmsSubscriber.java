package subscriber;

import models.Message;

public class SmsSubscriber implements Subscriber {

    private final String phoneNumber;

    public SmsSubscriber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public void onMessage(Message message) {
        System.out.println("[" + phoneNumber + "] received '" + message.getMessageContent()
                + "' for event type '" + message.getEventType() + "'");
    }
}
