package com.reactive.streams.manipulate.repo;

import java.util.Random;

public class Comment {

    int id;
    String comment;

    private Random random = new Random();

    public Comment(int id) {

        this.id = id;
        this.comment = "I am a comment with a random number: " + random.nextInt(100);
    }

    public String toString() {
        return "id: " + id + "; comment: " + comment;
    }
}
