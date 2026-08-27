package org.example.designPattern.Creational.Builder;

public class Student {
    private String name;  //mandatoryField
    private int rollNo;  //optional
    private int age; //optional
    private String address;  //optional

    private Student(StudentBuilder builder ) {
     this.name=builder.name;
     this.address=builder.address;
     this.age=builder.age;
     this.rollNo=builder.rollNo;
    }

    public static class StudentBuilder {
        private String name;  //mandatoryField
        private int rollNo;  //optional
        private int age; //optional
        private String address;  //optional

        public StudentBuilder(String name){
            this.name=name;
        }

        public StudentBuilder setAge(int age){
            this.age=age;
            return this;
        }
        public StudentBuilder setRollNo(int rollNo){
            this.rollNo=rollNo;
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
