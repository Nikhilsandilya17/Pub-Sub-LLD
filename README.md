# Pub-Sub System — LLD (Java)

Low-level design and implementation of a Publisher-Subscriber system.

## Requirements

1. The Pub-Sub system should allow publishers to publish messages/events for a specific event type.
2. All subscribers subscribed to that event type should receive that message.
3. Subscribers should be able to subscribe to event types of interest.
4. Subscribers should be able to unsubscribe from an event type at any time.
5. The system should support multiple publishers and subscribers.
6. Messages should be delivered to all subscribers of an event type in real-time.
7. The system should handle concurrent access and ensure thread safety.
8. The Pub-Sub system should be scalable and efficient in terms of message delivery.

## Tech

- Java (no external dependencies)
