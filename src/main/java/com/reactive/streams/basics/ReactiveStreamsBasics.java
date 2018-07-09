package com.reactive.streams.basics;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

// following
// https://medium.com/@cheron.antoine/reactor-java-1-how-to-create-mono-and-flux-471c505fa158

public class ReactiveStreamsBasics {

    public static void main(String[] args) {
        ReactiveStreamsBasics reactiveStreamsBasics = new ReactiveStreamsBasics();

        reactiveStreamsBasics.createReactiveStreams();
        reactiveStreamsBasics.consumeReactiveStreams();
        reactiveStreamsBasics.badPractices();
    }

    public void createReactiveStreams() {
        // Ways to create streams

        // Some dummy instance that will give us access to functions with a bunch of different return types
        Dummy dummy = new Dummy();


        // Flux 'returns?' 0 - n of 'something'
        // Flux from simple data
        Flux<Integer> integerFlux = Flux.just(1, 2, 3);
        Flux<String> stringFlux = Flux.just("hello", "world");

        // or from collections
        List<String> stringList = Arrays.asList("Hello", "foo", "bar");
        Flux<String> stringListFlux = Flux.fromIterable(stringList);

        // or from (non-reactive) streams
        Stream <String> stringStream = stringList.stream();
        Flux<String> streamFlux = Flux.fromStream(stringStream);

        // Flux on a range
        Flux<Integer> fluxRage = Flux.range(1, 5);

        // Creates a new value every 100ms, starting at 1
        Flux<Long> intervalFlux = Flux.interval(Duration.ofMillis(100));

        // Creating Flux from another Flux
        Flux<Integer> integerFluxCopy = Flux.from(integerFlux);

        // Empty integer flux
        Flux<Integer> empty = Flux.empty();

        // Ways to create Monos

        // Mono 'returns' 0 - 1 of 'something'
        // Mono from simple data
        Mono<Integer> integerMono = Mono.just(1);
        Mono<String> stringMono = Mono.just("hello, world");
        Mono<Integer> emptyMono = Mono.empty();

        // Mono from callable
        Mono<String> callableMono = Mono.fromCallable(() -> "Hello World");
        Mono<String> callableMonoJava8Reference = Mono.fromCallable(Dummy::someDummyMethod);

        // or from a supplier
        Random random = new Random();
        Mono<Double> doubleMono = Mono.fromSupplier(random::nextDouble);

        // Mono with the first element of a Flux
        Mono<Integer> monoFromDlux = Mono.from(Flux.range(1, 10));

        // There are three other commmon methods to create Monos and Flux:
        // - defer
        // - create
        // - error

        // Defer method is similar to fromCallable, but expects a Mono<T> instead of a T
        Mono<Integer> monoFromDefer = Mono.defer(Dummy::someIntMono);

        // With Defer you have to catch exceptions, but not with fromCallable
        // With fromCallable the exception will be wrapped in a Mono.error(...)
        Mono<Integer> couldBeAnException = Mono.fromCallable(Dummy::someException);
        Mono<Integer> couldAlsoBeAnException = Mono.defer(() -> {
            try {
                Integer res = Dummy.someException();
                return Mono.just(res);
            }
            catch(Exception e) {
                return Mono.error(e);
            }
        });

        // Mono create
        // Create is more low level as it deals with the signals inside the mono / flux
        Mono<Integer> monoFromCreate = Mono.create(callback -> {
            try {
                callback.success(this.getAnyInteger());
            }
            catch (Exception e) {
                callback.error(e);
            }
        });

        // Flux create
        Flux<Integer> fluxFromCreate = Flux.create(emitter -> {
            Random rnd = new Random();
            for (int i = 0; i < 10; i++)
                emitter.next(rnd.nextInt());

            int rndInt = rnd.nextInt(2);
            if (rndInt < 1) emitter.complete();
            else emitter.error(new RuntimeException("Bad luck.. "));
        });

    }

    public void consumeReactiveStreams() {

        // TO comsume all these fancy streams we just created, we use the subscribe method
        Mono.just("Hello Workd").subscribe(
                successValue -> System.out.println(successValue),
                error -> System.out.println(error.getMessage()),
                () -> System.out.println("Mono consumed")
        );

        Flux.range(1, 5).subscribe(
                successValue -> System.out.println(successValue),
                error -> System.out.println(error.getMessage()),
                () -> System.out.println("Flux consumed")
        );

        // Let's change this example and see what happens when we subscribe to it
        Flux<Double> fluxFromCreate = Flux.create(emitter -> {
            Random rnd = new Random();

            // emitter will continue to generate random numbers, which will be consumed by the subscriber
            for (int i = 0; i < 10; i++) {
                emitter.next(rnd.nextDouble());
            }

            // forcing error to be generated in 50% of cases
            int rndInt = rnd.nextInt(2);
            if (rndInt > 0.5) emitter.complete();
            else emitter.error(new RuntimeException("Bad luck.. "));
        });

        fluxFromCreate.subscribe(
                successValue -> System.out.println(successValue),
                error -> System.out.println(error.getMessage()),
                // if an error was produced, this line will never execute!
                () -> System.out.println("Flux consumed")
        );

    }

    public void badPractices() {
        // Some of the things you should avoid when working with reactive streams

        // block is a blocking operation (duh..) and makes the program synchronous
        String helloWorld = Mono.just("Hello world").block();
        System.out.println(helloWorld);

        // Same for Flux and the blockFirst, blockLast and non-synchronous Java methods like
        // toStream and toIterable
        String fromFlux = Flux.just("Hello", "World").blockLast(Duration.ofMillis(10000));
        System.out.println(fromFlux);
    }

    private Integer getAnyInteger() throws Exception {
        // there appear to be some cases where using the dummy does not work: Integer is not a functional interface error
        throw new RuntimeException("An error as occured for no reason.");
    }
}
