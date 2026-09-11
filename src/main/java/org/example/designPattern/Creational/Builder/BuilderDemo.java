package org.example.designPattern.Creational.Builder;

// Builder design patter is creational design pattern , we can use this pattern to avoid to create multiple constructor
// For example there ia a car class in that there are some mandatory field and some are optional so we have to make multiple constructor
// to avoid this we are using builder design pattern

import java.util.stream.Stream;

public class BuilderDemo {
    public static void main(String[] args) {
       Student s1=new Student.StudentBuilder("Aman").build();
       
    }
}
