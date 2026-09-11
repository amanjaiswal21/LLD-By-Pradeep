package org.example.LLDQuestion.NotificationSystem;

/*
Functional requirement
-> user can send the notification
-> system should support multiple channels (SMS,EMAIL,PUSH)
-> system should first process the high priority notification (otp,transaction)

Non-Functional Requirement
-> code should be extensible (add more channels in system)


 */

import java.util.Map;

enum NotificationChannel{
    EMAIL,SMS,PUSH
}

enum NotificationPriority{
    HIGH(1),LOW(3),MEDIUM(2);
    final int level;
    NotificationPriority(int level) {
        this.level=level;
    }

    public int getLevel(){
        return this.level;
    }
}

class NotificationRequest{
    String userId;
    NotificationChannel notificationChannel;
    NotificationPriority notificationPriority;
    Map<String,String>params;
}

class Notification{
    String userId;
    NotificationChannel notificationChannel;
    NotificationPriority notificationPriority;
    Map<String,String>params;
}

class NotificationSystem{
}


interface NotificationSender{
    boolean send(Notification notification);
}

public class NotificationSystemDemo {
}
