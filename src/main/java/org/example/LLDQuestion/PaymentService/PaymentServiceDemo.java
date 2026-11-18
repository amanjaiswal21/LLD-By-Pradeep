package org.example.LLDQuestion.PaymentService;
/*
Functional Requirement
->Create Payment
->Multiple payment method-> UPI/Card/
->Multiple external payment gateway-> razorpay/paypal/stripe
->Track payment status:
->Support payment refunds.
-> Handle payment gateway webhooks for asynchronous payment status updates.
Non Functional Requirement
->Idempotency
-> payment status tracker
->Retry mechanism
-> Extensibility
 */
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
enum PaymentStatus {
    CREATED,
    FAILED,
    SUCCESS,
    PROCESSING,
    REFUNDED
}
enum GatewayType {
    RAZORPAY,
    PAYPAL,
    STRIPE
}
class Request {
    String userId;
    PaymentMethod paymentMethod;
    double amount;
    String idempotencyKey;
    public Request(String userId, PaymentMethod paymentMethod, double amount, String idempotencyKey) {
        this.userId = userId;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.idempotencyKey = idempotencyKey;
    }
}
class PaymentRequest {
    String paymentId;
    String userId;
    PaymentMethod paymentMethod;
    double amount;
    PaymentStatus paymentStatus;
    LocalDateTime createdAt;
    String idempotencyKey;
    String gatewayTransactionId;
    GatewayType gatewayType;
    public PaymentRequest(String paymentId, String userId, PaymentMethod paymentMethod, double amount, String idempotencyKey) {
        this.paymentId = paymentId;
        this.userId = userId;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.idempotencyKey = idempotencyKey;
        this.paymentStatus = PaymentStatus.CREATED;
        this.createdAt = LocalDateTime.now();
    }
}
interface PaymentMethod {
    void processPayment(PaymentRequest paymentRequest);
}
class UPI implements PaymentMethod {
    @Override
    public void processPayment(PaymentRequest paymentRequest) {
        System.out.println("Processing UPI payment for user: " + paymentRequest.userId);
    }
}
class CARD implements PaymentMethod {
    @Override
    public void processPayment(PaymentRequest paymentRequest) {
        System.out.println("Processing Card payment for user: " + paymentRequest.userId);
    }
}
interface PaymentGateway {
    void makePayment(PaymentRequest paymentRequest) throws Exception;
    boolean refund(String gatewayTransactionId, double amount);
}
class RazorpayGateway implements PaymentGateway {
    @Override
    public void makePayment(PaymentRequest paymentRequest) {
        System.out.println("Calling Razorpay for payment: " + paymentRequest.paymentId);
        boolean success = true;
        if (!success) {
            throw new RuntimeException("Razorpay temporary failure");
        }
        System.out.println("Payment request submitted to Razorpay");
    }
    @Override
    public boolean refund(String gatewayTransactionId, double amount) {
        System.out.println( "Refunding " + amount + " using Razorpay transaction: " + gatewayTransactionId );
        return true;
    }
}
class StripeGateway implements PaymentGateway {
    @Override
    public void makePayment(PaymentRequest paymentRequest) {
        System.out.println("Calling Stripe for payment: " + paymentRequest.paymentId);
        boolean success = true;
        if (!success) {
            throw new RuntimeException("Stripe temporary failure");
        }
        System.out.println("Payment request submitted to Stripe");
    }
    @Override
    public boolean refund(String gatewayTransactionId, double amount) {
        System.out.println( "Refunding " + amount + " using Stripe transaction: " + gatewayTransactionId );
        return true;
    }
}
class PaymentRepository {
    private final Map<String, PaymentRequest> payments = new ConcurrentHashMap<>();
    public void save(PaymentRequest payment) {
        payments.put(payment.paymentId, payment);
    }
    public PaymentRequest findById(String paymentId) {
        return payments.get(paymentId);
    }
    public Map<String, PaymentRequest> getPayments() {
        return payments;
    }
}

class IdempotencyStore {
    private final Map<String, String> idempotencyMap = new ConcurrentHashMap<>();
    public boolean exists(String key) {
        return idempotencyMap.containsKey(key);
    }
    public String getPaymentId(String key) {
        return idempotencyMap.get(key);
    }
    public void save(String key, String paymentId) {
        idempotencyMap.put(key, paymentId);
    }
}
class PaymentProcessor {
    PaymentRepository paymentRepository;
    IdempotencyStore idempotencyStore;
    Map<GatewayType, PaymentGateway> gateways = new ConcurrentHashMap<>();
    ExecutorService executorService = Executors.newFixedThreadPool(3);
    private static final int MAX_RETRIES = 3;
    public PaymentProcessor() {
        paymentRepository = new PaymentRepository();
        idempotencyStore = new IdempotencyStore();
        gateways.put(GatewayType.RAZORPAY, new RazorpayGateway());
        gateways.put(GatewayType.STRIPE, new StripeGateway());
    }
    public PaymentRequest processPayment(Request request, GatewayType gatewayType) {
        if (idempotencyStore.exists(request.idempotencyKey)) {
            String existingPaymentId = idempotencyStore.getPaymentId(request.idempotencyKey);
            System.out.println("Duplicate request detected");
            return paymentRepository.findById(existingPaymentId);
        }
        String paymentId = UUID.randomUUID().toString();
        PaymentRequest payment = new PaymentRequest( paymentId, request.userId, request.paymentMethod, request.amount, request.idempotencyKey );
        payment.gatewayType = gatewayType;
        paymentRepository.save(payment);
        idempotencyStore.save( request.idempotencyKey, paymentId );
        request.paymentMethod.processPayment(payment);
        payment.paymentStatus = PaymentStatus.PROCESSING;
        paymentRepository.save(payment);
        PaymentGateway gateway = gateways.get(gatewayType);
        executorService.submit(() -> {
            int retryCount = 0;
            while (retryCount < MAX_RETRIES) {
                try {
                    retryCount++;
                    System.out.println("Gateway attempt: " + retryCount);
                    gateway.makePayment(payment);
                    return;
                } catch (Exception e) {
                    System.out.println( "Gateway failed: " + e.getMessage() );
                }
            }
            payment.paymentStatus = PaymentStatus.FAILED;
            paymentRepository.save(payment);
        });
        return payment;
    }
    public PaymentStatus getPaymentStatus(String paymentId) {
        PaymentRequest payment = paymentRepository.findById(paymentId);
        return payment.paymentStatus;
    }
    public void refund(String paymentId) {
        PaymentRequest payment = paymentRepository.findById(paymentId);
        PaymentGateway gateway = gateways.get(payment.gatewayType);
        boolean refundSuccess = gateway.refund( payment.gatewayTransactionId, payment.amount );
        if (refundSuccess) {
            payment.paymentStatus = PaymentStatus.REFUNDED;
            paymentRepository.save(payment);
            System.out.println("Refund successful");
        }
    }
    public void handleWebhook(String paymentId, String gatewayTransactionId, PaymentStatus status) {
        PaymentRequest payment = paymentRepository.findById(paymentId);
        payment.gatewayTransactionId = gatewayTransactionId;
        payment.paymentStatus = status;
        paymentRepository.save(payment);
        System.out.println( "Webhook updated payment " + payment.paymentId + " -> " + status );
    }
    public void shutdown() {
        executorService.shutdown();
    }
}
class PaymentService {
    PaymentProcessor paymentProcessor;
    public PaymentService() {
        paymentProcessor = new PaymentProcessor();
    }
    public PaymentRequest pay(Request request, GatewayType gatewayType) {
        return paymentProcessor.processPayment( request, gatewayType );
    }
    public PaymentStatus getPaymentStatus(String paymentId) {
        return paymentProcessor.getPaymentStatus( paymentId );
    }
    public void refund(String paymentId) {
        paymentProcessor.refund(paymentId);
    }
    public void handleWebhook(String paymentId, String gatewayTransactionId, PaymentStatus status) {
        paymentProcessor.handleWebhook( paymentId, gatewayTransactionId, status );
    }
    public void shutdown() {
        paymentProcessor.shutdown();
    }
}
public class PaymentServiceDemo {
    public static void main(String[] args) throws InterruptedException {
        PaymentService paymentService = new PaymentService();
        String idempotencyKey = UUID.randomUUID().toString();
        Request request = new Request( "USER-101", new UPI(), 1000, idempotencyKey );
        PaymentRequest payment = paymentService.pay( request, GatewayType.RAZORPAY );
        System.out.println( "Payment Status = " + paymentService.getPaymentStatus( payment.paymentId ) );
        Thread.sleep(2000);
        paymentService.handleWebhook( payment.paymentId, "RAZORPAY_TXN_123", PaymentStatus.SUCCESS );
        System.out.println( "Payment Status after webhook = " + paymentService.getPaymentStatus( payment.paymentId ) );
        paymentService.refund( payment.paymentId );
        System.out.println( "Final Status = " + paymentService.getPaymentStatus( payment.paymentId ) );
        paymentService.shutdown();
    }
}