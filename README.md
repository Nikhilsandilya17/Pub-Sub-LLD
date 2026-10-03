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

---

## Design Overview

```
                            ┌──────────────────────┐
                            │       Broker         │
                            │  ┌────────────────┐  │
   ┌──────────┐  publish    │  │    Registry     │  │
   │Publisher │─────────────┼─▶│ Map<eventType,  │  │
   └──────────┘             │  │ List<Subscriber>>│  │
                            │  └────────────────┘  │
                            │                      │
                            │  ┌────────────────┐  │
                            │  │     Workers     │  │
                            │  │ Map<Subscriber, │  │
                            │  │ SubscriberWorker▶ │ │
                            │  └───────┬────────┘  │
                            └──────────┼───────────┘
                                       │ enqueue + own thread
                     ┌─────────────────┼─────────────────┐
                     ▼                 ▼                  ▼
              ┌────────────┐    ┌────────────┐     ┌────────────┐
              │Subscriber A│    │Subscriber B│     │Subscriber C│
              └────────────┘    └────────────┘     └────────────┘
```

Publishers and subscribers never know about each other — the broker decouples them completely.

## Class Structure

```
Solution/src/
├── Main.java                       # Demo
├── models/
│   ├── Message.java                 # Immutable: id, content, eventType, timestamp
│   └── Publisher.java               # Dumb delegate — holds a Broker reference
├── subscriber/
│   ├── Subscriber.java              # Interface: onMessage(Message)
│   ├── EmailSubscriber.java         # Concrete subscriber
│   └── SmsSubscriber.java          # Concrete subscriber
└── service/
    ├── Broker.java                  # Core registry + routing
    └── SubscriberWorker.java        # Per-subscriber queue + delivery thread
```

## Design Decisions

| Decision | Choice | Why |
|---|---|---|
| Event type representation | `String` (dynamic) | New event types without code changes; a `Map` key lookup is O(1) |
| Subscriber abstraction | `interface Subscriber` | Broker is decoupled from concrete subscribers; any class can subscribe |
| Message model | Immutable class | Thread-safe by construction; safe to share across queues/threads |
| Registry | `ConcurrentHashMap<String, CopyOnWriteArrayList<Subscriber>>` | Lock-free reads; safe iteration while subscribers unsubscribe concurrently |
| Subscribe atomicity | `computeIfAbsent` | Atomic check-then-act — no race between "list missing" and "add" |
| Delivery model | Async: per-subscriber `BlockingQueue` + worker thread | `publish()` returns immediately; a slow subscriber only delays itself |
| Queue capacity | Bounded (1000) | Backpressure control — memory stays predictable |
| Queue full policy | `offer()` → drop | Simple; discuss block/dead-letter as alternatives (see Extensions) |

## Message Flow

1. **Publish** — `Publisher.publish(message)` → `Broker.publish(message)`
2. **Route** — broker looks up `message.getEventType()` in the registry
3. **Enqueue** — for each subscriber, broker drops the message into that subscriber's `SubscriberWorker` queue
4. **Deliver** — each worker's own thread polls its queue and calls `subscriber.onMessage(message)`
5. **Isolation** — one slow subscriber backs up only its own queue; delivery to others is unaffected

## Concurrency & Thread Safety

- **`ConcurrentHashMap`** for both registry and worker map — concurrent subscribe/unsubscribe/publish without locking the whole structure
- **`CopyOnWriteArrayList`** per event type — iterating subscribers during a publish never throws `ConcurrentModificationException` if another thread unsubscribes mid-iteration
- **`BlockingQueue` per subscriber** — producer (broker) and consumer (worker thread) hand-off is thread-safe by design
- **`volatile boolean running`** in `SubscriberWorker` — stop signal is visible across threads; worker drains its queue before exiting
- **One worker per subscriber, not per subscription** — a subscriber on 3 event types gets 1 queue and 1 thread; per-subscriber FIFO ordering is preserved

## Design Patterns Used

| Pattern | Where | Role |
|---|---|---|
| **Observer** | `Subscriber` interface + broker registry | Core of pub-sub: one-to-many dependency, state change (message) auto-notifies observers |
| **Mediator** | `Broker` | Publishers and subscribers communicate only through the broker; no direct coupling |
| **Producer-Consumer** | `BlockingQueue` + `SubscriberWorker` | Broker produces into queues; worker threads consume — decouples publish rate from delivery rate |
| **Facade** | `Publisher.publish()` | Hides broker routing behind a simple call for the publishing side |
| **Strategy** (light) | `Subscriber` implementations | Different delivery behaviors (email, SMS) interchangeable behind one interface |

## Delivery Semantics

- Current: **at-most-once per queue slot** (a dropped message on full queue is lost)
- Ordering: **per-subscriber FIFO** — a subscriber receives messages for all its event types in publish order; no cross-subscriber ordering guarantee
- Failure isolation: a throwing `onMessage()` is caught by the worker — it doesn't kill delivery or block other subscribers

## Running

```bash
cd Solution
javac -d ../out $(find src -name "*.java")
java -cp ../out Main
```

## Possible Extensions (talking points)

- Acknowledgements + retry → at-least-once delivery
- Per-subscriber offsets + retention → missed messages replayed after reconnect (Kafka-style)
- Dead-letter queue for messages that repeatedly fail delivery
- Message TTL and expired-message cleanup
- Wildcard subscription (`orders.*`)
- Persistence (write-ahead log) for durability across broker restarts
