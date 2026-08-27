package org.example.designPattern.Creational.Singleton;

// Singleton Pattern:
// Singleton is a creational design pattern that ensures a class has only one instance
// and provides a global way to access that instance. It is useful when the same shared
// object/resource should be reused across the application.
//
// Example:
// In Spring Boot, a configured WebClient can be a singleton bean so the same client
// is reused for multiple service-to-service HTTP calls, avoiding unnecessary object
// creation and keeping configuration centralized.

public class SingletonDemo {
    public static void main(String[] args) {
      Runnable task=()->{
          Logger instnce=Logger.getInstance();
      };

      for(int i=0;i<10000;i++){
          new Thread(task).start();
      }
    }
}
