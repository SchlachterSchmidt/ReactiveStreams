package com.reactive.streams.manipulate.repo;


import java.util.Random;

public class User {

    public int id;
    public int someValue;
    public boolean isAdmin;

    private Random random = new Random();

    public User(int id) {
        this.id = id;
        this.someValue = random.nextInt(100);
        this.isAdmin = random.nextBoolean();
    }

    public String toString() {
        return "Hi my ID is: " + this.id + " and I have " + this.someValue + "and I am an admin: " + isAdmin;
    }
}
