package org.example.LLDQuestion.RateLimmitter;


/*
 Function Requirement
  1-> Given a unique identifier, system decide to accept or reject the req
  2-> system will have multiple strategy like(Token Bucket, Fixed Window)


  Non-Functional requirement
  Extensibility : In future we can add moreStrategy
  Thread Safety
 */


import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;


interface RateLimiter {
    boolean isAllowed(Request request);
}

class Config {
    static class TokenBucketConfig {
        final int maxToken = 5;
        final int refillRateInMin = 5;
    }

    static class SlidingWindowConfig{
        final int maxTokenInMin=5;
    }
}

class TokenBucket {
    int currentToken;
    LocalDateTime lastFilledAt;

    TokenBucket(int currentToken, LocalDateTime lastFilledAt) {
        this.currentToken = currentToken;
        this.lastFilledAt = lastFilledAt;
    }
}

class SlidingWindow{
    Queue<LocalDateTime>queue=new LinkedList<>();
}

class Repository {
    static class TokenBucketRepository {
        ConcurrentHashMap<String, TokenBucket> tokenBucketHashMap = new ConcurrentHashMap<>();
    }

    static class SlidingWindowRepository{
        ConcurrentHashMap<String,SlidingWindow>slidingWindowMap=new ConcurrentHashMap<>();
    }
}

class SlidingWindowRateLimiter implements RateLimiter{

    Config.SlidingWindowConfig slidingWindowConfig;
    Repository.SlidingWindowRepository slidingWindowRepository;

    SlidingWindowRateLimiter(){
        this.slidingWindowConfig=new Config.SlidingWindowConfig();
        this.slidingWindowRepository=new Repository.SlidingWindowRepository();
    }

    @Override
    public boolean isAllowed(Request request) {
        LocalDateTime endTime=LocalDateTime.now();
        SlidingWindow slidingWindow= slidingWindowRepository.slidingWindowMap.computeIfAbsent(request.IpAddress,(key)-> new SlidingWindow());

        synchronized (slidingWindow) {
            LocalDateTime startTime = endTime.minusMinutes(1);
            while (!slidingWindow.queue.isEmpty() && slidingWindow.queue.peek().isBefore(startTime)) {
                slidingWindow.queue.poll();
            }
            if (slidingWindow.queue.size() >= slidingWindowConfig.maxTokenInMin) return false;
            slidingWindow.queue.offer(endTime);
            return true;
        }
    }
}

class TokenBucketRateLimiter implements RateLimiter {

    Config.TokenBucketConfig tokenBucketConfig;
    Repository.TokenBucketRepository tokenBucketRepository;

    TokenBucketRateLimiter() {
        this.tokenBucketConfig = new Config.TokenBucketConfig();
        this.tokenBucketRepository =
                new Repository.TokenBucketRepository();
    }

    @Override
    public boolean isAllowed(Request request) {
        LocalDateTime currentTime = LocalDateTime.now();
        if (!tokenBucketRepository.tokenBucketHashMap.containsKey(request.IpAddress)) {
            TokenBucket tokenBucket = new TokenBucket(tokenBucketConfig.maxToken - 1, currentTime);
            tokenBucketRepository.tokenBucketHashMap.put(request.IpAddress, tokenBucket);
            return true;
        }
        TokenBucket tokenBucket = tokenBucketRepository.tokenBucketHashMap.get(request.IpAddress);
        synchronized (tokenBucket) {
            Duration elapsedTime = Duration.between(tokenBucket.lastFilledAt, currentTime);
            Long min = elapsedTime.toMinutes();
            if (min > 0) {
                tokenBucket.currentToken = Math.min(tokenBucketConfig.maxToken, (int) (tokenBucket.currentToken + tokenBucketConfig.refillRateInMin * min));
                tokenBucket.lastFilledAt = tokenBucket.lastFilledAt.plusMinutes(min);
                ;
            }
            if (tokenBucket.currentToken <= 0) return false;
            tokenBucket.currentToken -= 1;
            tokenBucketRepository.tokenBucketHashMap.put(request.IpAddress, tokenBucket);
            return true;
        }
    }
}

class Request {
    String IpAddress;
}


public class RateLimiterDemo2 {
    public static void main(String[] args) throws InterruptedException {
        RateLimiter tokenBucket = new TokenBucketRateLimiter();
        RateLimiter slidingWindow = new SlidingWindowRateLimiter();

        Request request = new Request();
        request.IpAddress = "192.168.1.1";

        System.out.println("========== TOKEN BUCKET ==========");

        for (int i = 1; i <= 7; i++) {
            boolean allowed = tokenBucket.isAllowed(request);
            System.out.println("Request " + i + " -> " +
                    (allowed ? "ACCEPTED" : "REJECTED"));
        }

        System.out.println("\n========== SLIDING WINDOW ==========");

        for (int i = 1; i <= 7; i++) {
            boolean allowed = slidingWindow.isAllowed(request);
            System.out.println("Request " + i + " -> " +
                    (allowed ? "ACCEPTED" : "REJECTED"));
        }

        // Test different IP address
        Request request2 = new Request();
        request2.IpAddress = "192.168.1.2";

        System.out.println("\n========== DIFFERENT IP ==========");

        System.out.println("Token Bucket -> " +
                tokenBucket.isAllowed(request2));

        System.out.println("Sliding Window -> " +
                slidingWindow.isAllowed(request2));

        // Test after a short delay
        System.out.println("\n========== AFTER 2 SECONDS ==========");

        Thread.sleep(2000);

        System.out.println("Token Bucket -> " +
                tokenBucket.isAllowed(request));

        System.out.println("Sliding Window -> " +
                slidingWindow.isAllowed(request));
    }
}
