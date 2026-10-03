package models;

import java.time.LocalDateTime;
import java.util.UUID;

public class Message {
    private final String id;
    private final String messageContent;
    private final String eventType;
    private final LocalDateTime timestamp;

    public Message(String messageContent, String eventType) {
        this.id = UUID.randomUUID().toString();
        this.messageContent = messageContent;
        this.eventType = eventType;
        this.timestamp = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public String getEventType() {
        return eventType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
