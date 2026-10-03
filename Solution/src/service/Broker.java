package service;

import models.Message;
import subscriber.Subscriber;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Broker {

    private final Map<String, CopyOnWriteArrayList<Subscriber>> registry = new ConcurrentHashMap<>();

    public void subscribe(String eventType, Subscriber subscriber) {
        registry.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(subscriber);
    }

    public void unsubscribe(String eventType, Subscriber subscriber) {
        List<Subscriber> subscribers = registry.get(eventType);
        if (subscribers != null) {
            subscribers.remove(subscriber);
        }
    }

    public void publish(Message message) {
        List<Subscriber> subscribers = registry.get(message.getEventType());
        if (subscribers == null) {
            return;
        }
        for (Subscriber subscriber : subscribers) {
            try {
                subscriber.onMessage(message);
            } catch (Exception e) {
                System.err.println("Failed to deliver message " + message.getId()
                        + " to subscriber: " + e.getMessage());
            }
        }
    }
}
