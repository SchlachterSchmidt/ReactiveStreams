package com.reactive.streams.manipulate.repo;

import java.util.ArrayList;
import java.util.List;

public class UserWithComments {

    User user;
    List<Comment> comments = new ArrayList<>();
    public UserWithComments(User user, List<Comment> comments) {
        this.user = user;
        this.comments.addAll(comments);
    }

    public void display() {
        for (Comment comment: comments) {
            System.out.println(comment + " from user: " + user);
        }
    }
}
