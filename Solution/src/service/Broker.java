package service;

import models.Message;
import subscriber.Subscriber;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class Broker {

    private final Map<String, CopyOnWriteArrayList<Subscriber>> registry = new ConcurrentHashMap<>();
    private final Map<Subscriber, SubscriberWorker> workers = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public void subscribe(String eventType, Subscriber subscriber) {
        registry.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(subscriber);
        workers.computeIfAbsent(subscriber, s -> {
            SubscriberWorker worker = new SubscriberWorker(s, new LinkedBlockingQueue<>(1000));
            executor.submit(worker);
            return worker;
        });
    }

    public void unsubscribe(String eventType, Subscriber subscriber) {
        List<Subscriber> subscribers = registry.get(eventType);
        if (subscribers != null) {
            subscribers.remove(subscriber);
        }
        if (!isSubscribedAnywhere(subscriber)) {
            SubscriberWorker worker = workers.remove(subscriber);
            if (worker != null) {
                worker.stop();
            }
        }
    }

    public void publish(Message message) {
        List<Subscriber> subscribers = registry.get(message.getEventType());
        if (subscribers == null) {
            return;
        }
        for (Subscriber subscriber : subscribers) {
            SubscriberWorker worker = workers.get(subscriber);
            if (worker != null) {
                worker.addMessage(message);
            }
        }
    }

    public void shutdown() {
        workers.values().forEach(SubscriberWorker::stop);
        executor.shutdown();
    }

    private boolean isSubscribedAnywhere(Subscriber subscriber) {
        for (List<Subscriber> subscribers : registry.values()) {
            if (subscribers.contains(subscriber)) {
                return true;
            }
        }
        return false;
    }
}
