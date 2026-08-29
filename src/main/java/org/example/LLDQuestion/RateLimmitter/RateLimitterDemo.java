package org.example.LLDQuestion.RateLimmitter;

/*
 Function Requirement
  1-> Given a unique identifier, system decide to accept or reject the req
  2-> system will have multiple strategy like(Token Bucket, Fixed Window)


  Non Functional requirement
  Extensibility : In future we can add moreStrategy


 */

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

interface RateLimmiter{
    boolean isAllowed(Request request);
}

class Request{
    String ipAddress;

    public Request(String id){
        this.ipAddress=id;
    }
}

class TokenBucketConfig{
    int capacity;
    long refillRate;

    public TokenBucketConfig(int capacity, int refillRate){
        this.capacity=capacity;
        this.refillRate=refillRate;
    }
}

class TokenBucket{
    int availableToken;
    long lastFilledAt;
    Lock lock;

    public TokenBucket(int availableToken, int lastFilledAt){
        this.availableToken=availableToken;
        this.lastFilledAt=lastFilledAt;
        lock=new ReentrantLock();
    }
}

class TokenBucketStrategy implements RateLimmiter{

    Map<String,TokenBucket> buckets;
    TokenBucketConfig tokenBucketConfig;

    public TokenBucketStrategy(TokenBucketConfig tokenBucketConfig){
        this.tokenBucketConfig=tokenBucketConfig;
        buckets=new ConcurrentHashMap<>();
    }

    @Override
    public boolean isAllowed(Request request) {
        TokenBucket bucket=buckets.get(request.ipAddress);
        long currenTime=System.currentTimeMillis();
        if(bucket==null){
            bucket=new TokenBucket(tokenBucketConfig.capacity,(int)currenTime);
            bucket.availableToken--;
            buckets.put(request.ipAddress,bucket);
        }
        else{
            bucket.lock.lock();
            try{
                refill(bucket, currenTime);
                if (bucket.availableToken == 0) return false;
                bucket.availableToken--;
                buckets.put(request.ipAddress,bucket);
            }
            finally {
                bucket.lock.unlock();
            }
        }
        return true;
    }

    void refill(TokenBucket bucket, long currentTime){
        long timeDiffSeconds = (currentTime - bucket.lastFilledAt) / 1000;
        if(timeDiffSeconds<0)
            return;
        int tokenToAdd=(int)(timeDiffSeconds*tokenBucketConfig.refillRate);
        if(tokenToAdd<0) return;
        bucket.availableToken=Math.min(tokenBucketConfig.capacity,bucket.availableToken+tokenToAdd);
        bucket.lastFilledAt=currentTime;

    }
}

class FixedWindowStrategy implements RateLimmiter{

    @Override
    public boolean isAllowed(Request request) {
        return false;
    }
}

public class RateLimitterDemo {
    public static void main(String[] args) {

    }
}
