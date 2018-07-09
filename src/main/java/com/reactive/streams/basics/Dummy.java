package com.reactive.streams.basics;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Dummy {

    // dummy class containing some methods to demo possibilities
    public static String someDummyMethod() {
        return "Hello World";
    }

    public static Mono<Integer> someIntMono() {
        return Mono.from(Flux.range(1, 100));
    }

    public static Integer someException() throws Exception {
        throw new RuntimeException("An error has occurred for no reason");
    }
}
