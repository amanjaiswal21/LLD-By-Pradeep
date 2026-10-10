package org.example.LLDQuestion.NotificationSystem;

/*
Functional Requirement
-> system should be able to send notification
-> system should support multiple channels SMS/EMAIL/PUSH
-> system should prioritize notification based on Notification Priority
-> client should be able to track the status of Notification
-> system send the Notification to user based on user Preference


Non-Functional Requirement
-> No same Notification system should Process
-> Code should be extensible in future we can add more channels
-> Follow SOLID Principle
*/

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

enum Channel {
    PUSH, SMS, EMAIL
}

enum Priority {
    TRANSACTIONAL(1), INFORMATIVE(2), PROMOTIONAL(3);

    final int value;

    Priority(int value) {
        this.value = value;
    }
}

enum Status {
    QUEUED, PROCESSING, FAILED, SENT, DELIVERED
}

class NotificationRequest {
    String id;
    String userId;
    Priority priority;
    String idempotencyKey;
    String message;
}

class Notification {
    String id;
    String userId;
    Priority priority;
    String idempotencyKey;
    String message;
    Status status;
    LocalDateTime createdAt;
    int retryCount;
}

interface NotificationChannel {
    void send(Notification notification);
}

class SMS implements NotificationChannel {
    private final SMSNotificationGateway smsNotificationGateway;

    public SMS() {
        this.smsNotificationGateway = new TwilliAdapter(new Twillio());
    }

    @Override
    public void send(Notification notification) {
        System.out.println("Sending SMS to user: " + notification.userId);
        smsNotificationGateway.sendNotification(notification);
    }
}

class PUSH implements NotificationChannel {
    private final PUSHNotificationGateway pushNotificationGateway;

    public PUSH() {
        this.pushNotificationGateway = new FirebaseAdapter(new Firebase());
    }

    @Override
    public void send(Notification notification) {
        System.out.println("Sending PUSH to user: " + notification.userId);
        pushNotificationGateway.sendNotification(notification);
    }
}

class Email implements NotificationChannel {
    private final EmailNotificationGateway emailNotificationGateway;

    public Email() {
        this.emailNotificationGateway = new SendGridAdapter(new SendGrid());
    }

    @Override
    public void send(Notification notification) {
        System.out.println("Sending EMAIL to user: " + notification.userId);
        emailNotificationGateway.sendNotification(notification);
    }
}

interface SMSNotificationGateway {
    void sendNotification(Notification request);
}

class Twillio {
    void processNotification(Notification notification) {
        System.out.println("SMS Notification sent by Twillio to user: " + notification.userId + " Message: " + notification.message);
    }
}

class TwilliAdapter implements SMSNotificationGateway {
    private final Twillio twillio;

    public TwilliAdapter(Twillio twillio) {
        this.twillio = twillio;
    }

    @Override
    public void sendNotification(Notification request) {
        twillio.processNotification(request);
    }
}

interface PUSHNotificationGateway {
    void sendNotification(Notification request);
}

class Firebase {
    void processNotification(Notification notification) {
        System.out.println("PUSH Notification sent by Firebase to user: " + notification.userId + " Message: " + notification.message);
    }
}

class FirebaseAdapter implements PUSHNotificationGateway {
    private final Firebase firebase;

    public FirebaseAdapter(Firebase firebase) {
        this.firebase = firebase;
    }

    @Override
    public void sendNotification(Notification request) {
        firebase.processNotification(request);
    }
}

interface EmailNotificationGateway {
    void sendNotification(Notification request);
}

class SendGrid {
    void processNotification(Notification notification) {
        System.out.println("EMAIL Notification sent by SendGrid to user: " + notification.userId + " Message: " + notification.message);
    }
}

class SendGridAdapter implements EmailNotificationGateway {
    private final SendGrid sendGrid;

    public SendGridAdapter(SendGrid sendGrid) {
        this.sendGrid = sendGrid;
    }

    @Override
    public void sendNotification(Notification request) {
        sendGrid.processNotification(request);
    }
}

class UserPreferenceRepository {
    Map<String, List<Channel>> preferences = new HashMap<>();

    List<Channel> getPreferences(String userId) {
        return preferences.getOrDefault(userId, Collections.emptyList());
    }

    void savePreferences(String userId, List<Channel> channels) {
        preferences.put(userId, channels);
        System.out.println("Preferences saved for user: " + userId + " " + channels);
    }
}

class IdempotencyRepository {
    Set<String> idepotencyKeys = new HashSet<>();

    boolean check(String key) {
        return idepotencyKeys.contains(key);
    }

    void put(String key) {
        idepotencyKeys.add(key);
        System.out.println("Idempotency key stored: " + key);
    }
}

class NotificationFactory {
    Map<Channel, NotificationChannel> notificationChannelMap = new HashMap<>();

    public NotificationFactory() {
        notificationChannelMap.put(Channel.SMS, new SMS());
        notificationChannelMap.put(Channel.EMAIL, new Email());
        notificationChannelMap.put(Channel.PUSH, new PUSH());
    }

    public NotificationChannel getNotificationChannel(Channel channel) {
        NotificationChannel notificationChannel = notificationChannelMap.get(channel);

        if (notificationChannel == null) {
            throw new IllegalArgumentException("Unsupported notification channel: " + channel);
        }

        return notificationChannel;
    }
}

class NotificationDispatcher {
    BlockingQueue<Notification> notifications;
    ExecutorService executor;
    NotificationFactory notificationFactory;
    UserPreferenceRepository userPreferenceRepository;
    volatile boolean isShutdown;

    public NotificationDispatcher() {
        this.userPreferenceRepository = new UserPreferenceRepository();
        this.isShutdown = false;
        this.notificationFactory = new NotificationFactory();
        this.notifications = new PriorityBlockingQueue<>(11, (a, b) -> a.priority.value - b.priority.value);
        this.executor = Executors.newFixedThreadPool(3);

        for (int i = 0; i < 3; i++) {
            executor.submit(this::process);
        }

        System.out.println("Notification Dispatcher started");
    }

    public void enqueue(Notification notification) {
        notifications.offer(notification);
        System.out.println("Notification added to queue: " + notification.id + " Priority: " + notification.priority);
    }

    private void process() {
        while (!isShutdown) {
            Notification notification;

            try {
                notification = notifications.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            try {
                notification.status = Status.PROCESSING;
                System.out.println("Processing notification: " + notification.id);

                List<Channel> preferences = userPreferenceRepository.getPreferences(notification.userId);

                if (preferences.isEmpty()) {
                    System.out.println("No notification preference found for user: " + notification.userId);
                    notification.status = Status.FAILED;
                    continue;
                }

                for (Channel channel : preferences) {
                    try {
                        notificationFactory.getNotificationChannel(channel).send(notification);
                    } catch (Exception e) {
                        System.out.println("Failed to send through channel: " + channel);
                    }
                }

                notification.status = Status.SENT;
                System.out.println("Notification sent successfully: " + notification.id);

            } catch (Exception e) {
                notification.status = Status.FAILED;
                System.out.println("Notification failed: " + notification.id);
            }
        }
    }

    public void shutdown() {
        isShutdown = true;
        executor.shutdownNow();
        System.out.println("Notification Dispatcher stopped");
    }
}

class NotificationService {
    IdempotencyRepository idempotencyRepository;
    NotificationDispatcher notificationDispatcher;

    public NotificationService(IdempotencyRepository idempotencyRepository, NotificationDispatcher notificationDispatcher) {
        this.idempotencyRepository = idempotencyRepository;
        this.notificationDispatcher = notificationDispatcher;
    }

    public void sendNotification(NotificationRequest notificationRequest) {
        if (idempotencyRepository.check(notificationRequest.idempotencyKey)) {
            throw new RuntimeException("This notification has already been sent");
        }

        idempotencyRepository.put(notificationRequest.idempotencyKey);

        Notification notification = createNotification(notificationRequest);

        System.out.println("Notification created: " + notification.id);

        notificationDispatcher.enqueue(notification);
    }

    Notification createNotification(NotificationRequest notificationRequest) {
        Notification notification = new Notification();

        notification.id = UUID.randomUUID().toString();
        notification.userId = notificationRequest.userId;
        notification.priority = notificationRequest.priority;
        notification.idempotencyKey = notificationRequest.idempotencyKey;
        notification.message = notificationRequest.message;
        notification.status = Status.QUEUED;
        notification.createdAt = LocalDateTime.now();
        notification.retryCount = 0;

        return notification;
    }
}

public class NotificationSystemDemo2 {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Notification System Started");

        IdempotencyRepository idempotencyRepository = new IdempotencyRepository();
        NotificationDispatcher notificationDispatcher = new NotificationDispatcher();
        NotificationService notificationService = new NotificationService(idempotencyRepository, notificationDispatcher);

        notificationDispatcher.userPreferenceRepository.savePreferences("USER-1", Arrays.asList(Channel.SMS, Channel.EMAIL, Channel.PUSH));
        notificationDispatcher.userPreferenceRepository.savePreferences("USER-2", Arrays.asList(Channel.EMAIL, Channel.PUSH));
        notificationDispatcher.userPreferenceRepository.savePreferences("USER-3", Collections.singletonList(Channel.SMS));

        NotificationRequest request1 = new NotificationRequest();
        request1.userId = "USER-1";
        request1.priority = Priority.TRANSACTIONAL;
        request1.idempotencyKey = "ORDER-101-SHIPPED";
        request1.message = "Your order 101 has been shipped";

        NotificationRequest request2 = new NotificationRequest();
        request2.userId = "USER-2";
        request2.priority = Priority.PROMOTIONAL;
        request2.idempotencyKey = "SALE-2026-USER2";
        request2.message = "Flat 30% discount available today";

        NotificationRequest request3 = new NotificationRequest();
        request3.userId = "USER-3";
        request3.priority = Priority.INFORMATIVE;
        request3.idempotencyKey = "ACCOUNT-UPDATE-USER3";
        request3.message = "Your account profile has been updated";

        notificationService.sendNotification(request1);
        notificationService.sendNotification(request2);
        notificationService.sendNotification(request3);

        try {
            NotificationRequest duplicateRequest = new NotificationRequest();
            duplicateRequest.userId = "USER-1";
            duplicateRequest.priority = Priority.TRANSACTIONAL;
            duplicateRequest.idempotencyKey = "ORDER-101-SHIPPED";
            duplicateRequest.message = "Your order 101 has been shipped";

            notificationService.sendNotification(duplicateRequest);
        } catch (RuntimeException e) {
            System.out.println("Duplicate prevented: " + e.getMessage());
        }

        Thread.sleep(2000);

        notificationDispatcher.shutdown();

        System.out.println("Notification System Stopped");
    }
}