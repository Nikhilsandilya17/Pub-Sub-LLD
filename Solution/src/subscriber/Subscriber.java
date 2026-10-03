package subscriber;

import models.Message;

public interface Subscriber {
    void onMessage(Message message);
}
