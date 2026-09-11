package org.example.designPattern.Creational.Builder;

public class Student {
    private final String name;
    private final int age;
    private final String email;
    private final String address;

    private Student(StudentBuilder builder){
        this.name=builder.name;
        this.age=builder.age;
        this.address=builder.address;
        this.email= builder.email;
    }

    public static class StudentBuilder{
        private final String name;
        private int age;
        private String email;
        private String address;

        public StudentBuilder(String name){
            this.name=name;
        }

        public StudentBuilder setAge(int age){
            this.age=age;
            return  this;
        }
        public StudentBuilder setEmail(String email){
            this.email=email;
            return this;
        }
        public StudentBuilder setAddress(String address){
            this.address=address;
            return this;
        }

        public Student build(){
            return new Student(this);
        }
    }
}



