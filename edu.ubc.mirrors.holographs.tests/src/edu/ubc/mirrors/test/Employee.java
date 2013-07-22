package edu.ubc.mirrors.test;

import java.util.HashSet;
import java.util.Set;

public class Employee {

    private String name;
    private int age;
    
    public Employee(String name, int age) {
        this.name = name;
        this.age = age;
    }
    
    public static Set<Employee> employeesOver40(Employee[] input) {
        Set<Employee> result = new HashSet<Employee>();
        for (Employee e : input) {
            if (e.age > 40) {
                result.add(e);
            }
        }
        return result;
    }
    
    public static void main(String[] args) {
        while (true) {
            foo();
        }
    }
    
    
    public static void foo() {
    }
    
}
