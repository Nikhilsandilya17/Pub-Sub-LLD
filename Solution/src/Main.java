import models.Message;
import models.Publisher;
import service.Broker;
import subscriber.EmailSubscriber;
import subscriber.SmsSubscriber;
import subscriber.Subscriber;

public class Main {

    public static void main(String[] args) {
        Broker broker = new Broker();

        Subscriber alice = new EmailSubscriber("alice@example.com");
        Subscriber bob = new EmailSubscriber("bob@example.com");
        Subscriber carol = new SmsSubscriber("+91-9876543210");

        broker.subscribe("NEWS_ALERT", alice);
        broker.subscribe("NEWS_ALERT", bob);
        broker.subscribe("NEWS_ALERT", carol);
        broker.subscribe("NOTIFICATION_ALERT", alice);

        Publisher publisher = new Publisher("p1", "Headlines Publisher", broker);

        publisher.publish(new Message("Breaking: market hits record high", "NEWS_ALERT"));
        publisher.publish(new Message("Your order has shipped", "NOTIFICATION_ALERT"));

        System.out.println("--- bob unsubscribes from NEWS_ALERT ---");
        broker.unsubscribe("NEWS_ALERT", bob);

        publisher.publish(new Message("Breaking: rain expected tonight", "NEWS_ALERT"));

        System.out.println("--- publishing to an event type with no subscribers ---");
        publisher.publish(new Message("Stock tip of the day", "STOCK_TIPS"));
    }
}
