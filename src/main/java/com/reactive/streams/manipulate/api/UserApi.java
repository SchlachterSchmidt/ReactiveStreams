package com.reactive.streams.manipulate.api;

import com.reactive.streams.manipulate.repo.User;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

public class UserApi {

    private List<User> userList = new ArrayList<User>();

    public UserApi() {
        for (int i = 0; i < 10; i++) {
            userList.add(new User(i));
        }
    }

    public User findUser(int i) {
        return userList.get(i);
    }

    public Mono<User> findUserMono(int id) {
        return Mono.just(findUser(id));
    }

    public Mono<String> userToMonoString(int id) {
        return Mono.just(findUser(id).toString());
    }

    public Flux<User> getAllUsers() {
        return Flux.fromIterable(userList);
    }

}
