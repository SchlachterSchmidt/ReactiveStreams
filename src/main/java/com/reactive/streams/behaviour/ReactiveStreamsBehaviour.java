package com.reactive.streams.behaviour;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

public class ReactiveStreamsBehaviour {

    // following
    // https://medium.com/@cheron.antoine/reactor-java-3-how-do-mono-and-flux-behave-1b57ed1c432c

    public static void main(String[] args) throws InterruptedException {

       ReactiveStreamsBehaviour behaviour = new ReactiveStreamsBehaviour();
       behaviour.fluxAreLazy();
       behaviour.fluxAreImmutable();
       behaviour.fluxCanBeInfinite();
       behaviour.infiniteFluxCanBeStopped();

       try {
           Thread.sleep(1000);
       }
       catch (InterruptedException e) {
           throw e;
       }

        behaviour.fluxRunSequentially();
    }

    public void fluxAreLazy() {

        // by definition, streams are lazy. Nothing is executed until you consume them with subscribe
        // this consumes less memory and is thread safe
        Instant beginning = Instant.now();

        Flux<Integer> flux = Flux.range(1, 5)
                .flatMap(n -> {
                    try {
                        Thread.sleep(100);
                        return Mono.just(n * n);
                    }
                    catch (InterruptedException e) {
                        return Mono.error(e);
                    }
                });

        System.out.println("After step1, program runs for : " + Duration.between(beginning, Instant.now()));

        flux.subscribe(System.out::println);

        System.out.println("The whole thing tool " + Duration.between(beginning, Instant.now()));
    }

    public void fluxAreImmutable() {

        // Flux / Mono cannot be changed. Any transformation we perform create a new Flux
        Flux<Integer> flux1 = Flux.range(1, 5);

        flux1.flatMap(n -> {
            try {
                Thread.sleep(300);
                return Mono.just(n * n);
            }
            catch (InterruptedException e) {
                return Mono.error(e);
            }
        });

        // this will NOT print the squares! notice the subtile difference in this and the above methods
        flux1.subscribe(System.out::println);

        // To 'modify the output we need to create a new Flux with the modified content
        Flux<Integer> flux2 = Flux.from(flux1)
                .flatMap(n -> {
                    try {
                        Thread.sleep(300);
                        return Mono.just(n * n);
                    }
                    catch (InterruptedException e) {
                        return Mono.error(e);
                    }
                });
        flux2.subscribe(System.out::println);
    }

    public void fluxCanBeInfinite() {
        Flux.interval(Duration.ofMillis(100))
                .map(i -> "infinite Tick : " + i)
                .subscribe(System.out::println);
    }

    public void infiniteFluxCanBeStopped() {
        Disposable disposable = Flux.interval(Duration.ofMillis(100))
                .map(i -> "Tick : " + i)
                .subscribe(System.out::println);

        try { Thread.sleep(1000); }
        catch (InterruptedException e) { e.printStackTrace(); }

        disposable.dispose();
        System.out.println("Stopped flux");
    }

    public void fluxRunSequentially() {
        Flux.range(1, 3)
                .flatMap(n -> {
                    System.out.println("\t\tIn flatMap n=" + n + " --- Thread is : " + Thread.currentThread().getName());
                    try {
                        Thread.sleep(100);
                        System.out.println("\t\tAfter Thread.sleep n=" + n);
                        return Mono.just(n);
                    }
                    catch (InterruptedException e) {
                        return Mono.error(e);
                    }
                })
                .map(n -> {
                    System.out.println("\t\tIn map n=" + n);
                    return n;
                })
                .subscribe(System.out::println);
    }
}
