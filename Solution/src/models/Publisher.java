package models;

import service.Broker;

public class Publisher {

    private final String id;
    private final String name;
    private final Broker broker;

    public Publisher(String id, String name, Broker broker) {
        this.id = id;
        this.name = name;
        this.broker = broker;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void publish(Message message) {
        broker.publish(message);
    }
}
