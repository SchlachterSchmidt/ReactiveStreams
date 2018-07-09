package com.reactive.streams.schedulers;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

public class ReactiveStreamsSchedulers {

    // following
    // https://medium.com/@cheron.antoine/reactor-java-4-how-to-take-control-over-the-execution-of-mono-and-flux-ead31dc066

    public static void main(String[] args) {

        // there are 4 different types of schedulers available with which we can control where the stream is processed
        // - subscribeOn method to set the scheduler for all operations on the stream
        // - publishOn to set the scheduler for all subsequent operations. overrides subscribeOn

        // the four types are:

        // single: a one worker thread scheduler
        // Schedulers.single()
        Mono.just(1)
                .subscribeOn(Schedulers.single())
                .map(Integer::valueOf)
                .map(n -> {
                    System.out.println("in map of: " + n + " - Thread is: " + Thread.currentThread().getName());
                    return n;
                })
                .subscribe(System.out::println);

        // elastic: a scheduler that dynamically creates threads when needed, with no up limit.
        // A thread is released after 60 non-working seconds.
        // Schedulers.elastic()
        Mono.just(1)
                .map(Integer::valueOf)
                .map(n -> {
                    System.out.println("in map of: " + n + " - Thread is: " + Thread.currentThread().getName());
                    return n;
                })
                .subscribeOn(Schedulers.elastic())
                .subscribe(System.out::println);

        // immediate is the thread where the method configuring is done
        Mono.just("1")
                .map(Integer::valueOf)
                .subscribeOn(Schedulers.immediate())
                .map(n -> {
                    System.out.println("in map of: " + n + " - Thread is: " + Thread.currentThread().getName());
                    return n;
                })
                .subscribe(System.out::println);

        // parallel is worker with as many threads as the CPU has cores
        // the amount of threads are calculated with
        System.out.println(Runtime.getRuntime().availableProcessors());
        Mono.just("1")
                .map(Integer::valueOf)
                .subscribeOn(Schedulers.parallel())
                .map(n -> {
                  System.out.println("in map of: " + n + " - Thread is: " + Thread.currentThread().getName());
                      return n;
                })
                .subscribe(System.out::println);

        // it doesn't matter when the subscribeOn is called. this is equivalent to the previous example
        Mono.just("1")
                .map(Integer::valueOf)
                .map(n -> {
                    System.out.println("in map of: " + n + " - Thread is: " + Thread.currentThread().getName());
                    return n;
                })
                .subscribeOn(Schedulers.parallel())
                .subscribe(System.out::println);

        // only the first call to subscribeOn takes effect!
        Mono.just(1).
                subscribeOn(Schedulers.single()).
                map(Integer::valueOf).
                subscribeOn(Schedulers.elastic())
                .map(n -> {
                    System.out.println("in map of: " + n + " - Thread is: " + Thread.currentThread().getName());
                    return n;
                })
                .subscribe(System.out::println);

        // publishOn only sets the thread for the following instructions
        Mono.just("1")
                .subscribeOn(Schedulers.single())
                .map(Integer::valueOf) // -> on single
                .publishOn(Schedulers.parallel()) // -> on single
                .map(x -> x * x) // -> on parallel
                .subscribe(System.out::println); // -> on parallel

        // Since flatMap returns a new Mono or Flux, these streams can have their own scheduler set, independent of the
        // parent. We can use different threads for different outcomes in the stream like this
        Flux.range(1, 10)
                .subscribeOn(Schedulers.single())
                .map(n -> n * 2)
                .flatMap(n -> {
                    if (n == 1) {
                        return Mono.error(new RuntimeException("That shouldnt happen")).subscribeOn(Schedulers.immediate());
                    }
                    else return Mono.just(n).subscribeOn(Schedulers.elastic());
                })
                .subscribe(System.out::println);

        // Flux are sequential by default, but can be made parallel
        // the .sequential() method make a stream sequential again
        Flux.range(1, 100)
                .parallel()
                .runOn(Schedulers.parallel())
                .map(n -> {
                    System.out.println("in map of: " + n + " - Thread is: " + Thread.currentThread().getName());
                    return n;
                })
                .subscribe(System.out::println);

        // give the parallel flux time to produce and consume a bunch of values
        sleepAndPrintBreak();

        // Now let's combine all of what we know

        // flatMap without changing scheduler
        Flux.range(1, 3)
                .map(n -> identityWithThreadLogging(n, "map1"))
                .flatMap(n -> Mono.just(n)
                            .map(nn -> identityWithThreadLogging(nn, "flatMap1")))
                .subscribeOn(Schedulers.parallel())
                .subscribe(n -> {
                    identityWithThreadLogging(n, "subscribe1");
                    System.out.println(n);
        });

        sleepAndPrintBreak();

        // let's run the Mono on a different scheduler
        Flux.range(1, 3)
                .map(n -> identityWithThreadLogging(n, "map2"))
                .flatMap(n -> Mono.just(n)
                        .map(nn -> identityWithThreadLogging(nn, "flatMap2"))
                        .subscribeOn(Schedulers.elastic()))
                .subscribeOn(Schedulers.parallel())
                .subscribe(n -> {
                    identityWithThreadLogging(n, "subscribe2");
                    System.out.println(n);
                });

        sleepAndPrintBreak();

        // now let's make things really complicated
        Flux.range(1, 4)
                .subscribeOn(Schedulers.immediate())
                .map(n -> identityWithThreadLogging(n, "map3"))
                .flatMap(n -> {
                    identityWithThreadLogging(n, "flatMap3");
                    if (n == 1) return Mono.just(n).subscribeOn(Schedulers.parallel());
                    if (n == 2) return Mono.just(n).subscribeOn(Schedulers.elastic());
                    if (n == 3) return Mono.just(n).subscribeOn(Schedulers.single());
                    return Mono.error(new Exception("error")).subscribeOn(Schedulers.newSingle("Error-thread"));
                })
                .map(n -> identityWithThreadLogging(n, "map3-1"))
                .subscribe(
                        success -> System.out.println(identityWithThreadLogging(success, "Success")),
                        error -> System.out.println(identityWithThreadLogging(error, "Error").getMessage())
                );

        // just so we have enough time to consume all events before the app terminates
        sleepAndPrintBreak();
    }


    private static <T> T identityWithThreadLogging(T el, String operation) {

        System.out.println(operation + " -- " + el + " -- " + Thread.currentThread().getName());
        return el;
    }

    private static void sleepAndPrintBreak() {

        try {
            Thread.sleep(2000);
            System.out.println("\n\n\n");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
