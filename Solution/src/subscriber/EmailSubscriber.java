package subscriber;

import models.Message;

public class EmailSubscriber implements Subscriber {

    private final String email;

    public EmailSubscriber(String email) {
        this.email = email;
    }

    @Override
    public void onMessage(Message message) {
        System.out.println("[" + email + "] received '" + message.getMessageContent()
                + "' for event type '" + message.getEventType() + "'");
    }
}
