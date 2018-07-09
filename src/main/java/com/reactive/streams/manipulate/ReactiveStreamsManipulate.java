package com.reactive.streams.manipulate;

import com.reactive.streams.manipulate.api.CommentApi;
import com.reactive.streams.manipulate.api.UserApi;
import com.reactive.streams.manipulate.repo.Comment;
import com.reactive.streams.manipulate.repo.User;
import com.reactive.streams.manipulate.repo.UserWithComments;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;


public class ReactiveStreamsManipulate {

    public static void main(String[] args) {
        // following
        // https://medium.com/@cheron.antoine/reactor-java-2-how-to-manipulate-the-data-inside-mono-and-flux-b36ae383b499

        ReactiveStreamsManipulate manipulate = new ReactiveStreamsManipulate();
        manipulate.manipulateFluxAndMono();

    }

    private void manipulateFluxAndMono() {

        // Dummy User Api
        UserApi userApi = new UserApi();
        CommentApi commentApi = new CommentApi();

        // To manipulate the values inside a Flux or Mono, we use the map and flatMap methods
        // map takes a function as argument that will be applied to each element in the Flux
        Flux<Integer> squaredFlux = Flux.range(1, 100).map(x -> x * x);
        squaredFlux.subscribe(x -> System.out.println(x));

        // Same for Mono
        Mono<String> uppercaseMono = Mono.just("hello World").map(x -> x.toUpperCase());
        uppercaseMono.subscribe(x -> System.out.println(x));

        // Let's try a more complex example (slightly contrived)

        Flux.range(1, 7).
                map(id -> userApi.findUser(id)).
                map(user -> user.someValue).
                subscribe(listUserValue -> System.out.println(listUserValue));

        // collectList collects Flux<someValue> to a Mono<List<someValue>>
        Flux.range(1, 7).
                map(id -> userApi.findUser(id)).
                map(user -> user.someValue).
                collectList().
                subscribe(listUserValue -> System.out.println(listUserValue));

        // Passing the userApi just so that we keep on working with the same set of users
        Mono<User> someUser = userApi.findUserMono(4);
        someUser.subscribe(val -> System.out.println(val.someValue));

        // flatMap is similar to map, but the supplier should return a Mono<T> or Flux<T>
        // first without flatmap. userApi.findUserMono and userToMonoString return a Mono, so we have a nested Mono as
        // return type
        Mono<Mono<String>> monoMonoString = userApi.findUserMono(3).
                map(user -> userApi.userToMonoString(user.id));
        // well this is just terrible
        monoMonoString.subscribe(out -> out.subscribe(asd -> System.out.println(asd)));

        // The more elegant solution is
        Mono<String> monoString = userApi.findUserMono(4).
                flatMap(user -> userApi.userToMonoString(user.id));
        monoString.subscribe(out -> System.out.println(out));

        // Also we can do error handling like so
        Mono<User> iMightBeAnError = flatMapErrorHandling(3, userApi);
        iMightBeAnError.subscribe(out -> System.out.println(out.toString()));

        // The zip method allows us to combine the values inside two monos
        Mono<UserWithComments> userWithCommentsMono = userWithComments(4, userApi, commentApi);
        userWithCommentsMono.subscribe(s -> s.display());

        // The Filter method allows to filter the result set on some criteria
        Flux<User> allAdmins = getAllAdmins(userApi);
        // take acts on a number of T in the Flux until consumed
        System.out.println(allAdmins.take(1000).subscribe(s -> System.out.println(s)));
        allAdmins.subscribe(s -> System.out.println(s));
    }

    private Mono<User> flatMapErrorHandling(int id, UserApi userApi) {
        return userApi.findUserMono(id)
                .flatMap(response -> {
                    if (response.id == 5) return Mono.error(new RuntimeException("Exception for no reason except that i dont like your id"));
                    else if (response.someValue == 22) return Mono.error(new RuntimeException("I can throw any exception I want if I dont like what I get"));
                    return Mono.just(response);
                });
    }

    private Mono<UserWithComments> userWithComments(int id, UserApi userApi, CommentApi commentApi) {
        // again demonstrating different ways to create Monos, from blocking and non-blocking interfaces
        Mono<User> userInfo = Mono.fromCallable(() -> userApi.findUser(id));
        Mono<List<Comment>> commentList = commentApi.getCommentsForUser(id);

        Mono<UserWithComments> userWithCommentsMono = userInfo.zipWith(commentList)
                .map(tuple -> {
                    User user = tuple.getT1();
                    List<Comment> comments = tuple.getT2();

                    return new UserWithComments(user, comments);
                });
        return userWithCommentsMono;
    }

    private Flux<User> getAllAdmins(UserApi userApi) {
        return userApi.getAllUsers()
                .filter(user -> user.isAdmin);
    };
}
