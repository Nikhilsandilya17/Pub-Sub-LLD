package service;

import models.Message;
import subscriber.Subscriber;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class SubscriberWorker implements Runnable {

    private final Subscriber subscriber;
    private final BlockingQueue<Message> queue;
    private volatile boolean running = true;

    public SubscriberWorker(Subscriber subscriber, BlockingQueue<Message> queue) {
        this.subscriber = subscriber;
        this.queue = queue;
    }

    public void addMessage(Message message) {
        queue.offer(message);
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        while (running || !queue.isEmpty()) {
            try {
                Message message = queue.poll(100, TimeUnit.MILLISECONDS);
                if (message == null) {
                    continue;
                }
                try {
                    subscriber.onMessage(message);
                } catch (Exception e) {
                    System.err.println("Failed to deliver message " + message.getId()
                            + " to subscriber: " + e.getMessage());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
