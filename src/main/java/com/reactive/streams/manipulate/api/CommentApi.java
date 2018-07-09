package com.reactive.streams.manipulate.api;

import com.reactive.streams.manipulate.repo.Comment;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

public class CommentApi {

    List<Comment> commentList = new ArrayList<Comment>();

    public CommentApi() {
        for (int i = 0; i < 10; i++) {
            commentList.add(new Comment(i));
        }
        System.out.println("initalized comment list");
        for (Comment comment : commentList) {
            System.out.println(comment);
        }
    }

    public Comment findComment(int id) {
        return commentList.get(id);
    }

    public Mono<Comment> findCommentMono(int id) {
        return Mono.just(findComment(id));
    }

    public Mono<String> commentToMonoString(int id) {
        return Mono.just(findComment(id).toString());
    }

    public Mono<List<Comment>> getCommentsForUser(int id) {
        return Mono.just(commentList);
    }
}
