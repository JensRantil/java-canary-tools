Java canary tools
=================
> Contrary to popular beliefs, I don't think canaries requires infrastructure components set up. Routing a subset of users can be done using hashing or sampling.

(Reference: [Twitter](https://twitter.com/JensRantil/status/1436330953434091524))

Marco then
[responded](https://twitter.com/gentoomaniac/status/1436633272403087364) with
saying that "go live and deployment" don't need to be the same thing.

This library contains a few useful Java tools to be able to run soft
experiments in a Java application without having to do a redeploy. As long as
you have a sane object-oriented application adhering to the [Interface
segregation
principle](https://en.wikipedia.org/wiki/Interface_segregation_principle),
using these tools should be easy.

This Java package contains utility classes to run experiments and safe rollouts
of new implementations of Java interfaces:

 * `WeightedRoundRobinBuilder` allows you delegate a random fraction of calls
   to another implementation(s) of a shared Java interface. Usually useful if
   you have a different implementation that you would like to behave in the
   exact same way (including, having the same side-effect).
 * `WeightedShardedBuilder` constructs a Java proxy that implements a Java
   interface shared between different implementations. The proxy delegates to
   downstream implementations using `Object#hashCode()` of the first method
   argument, weighted by the downstream implementation (can be customized).
   This class is useful if you would like to try out a new Java implementation
   for a subset of, for example, users. This is useful if you are switching
   from one implementation to another which will not necessarily behave in the
   same way (nor have the same side-effects).
 * `CircuitBreakerFallbackBuilder` allows you to do safe rollout of new Java
   `interface` implementations. Use `CircuitBreakerFallbackBuilder` to
   construct a Java Proxy class that wraps two implementations of the same Java
   interfaces, the _old_ implementation and the _new_ implementation. As long as
   the new implementation never throws any exception, the Proxy class will start
   using it more and more instead of _old_. If _new_ starts throwing exceptions,
   the Proxy class will quickly roll back to use the _old_ implementation. Thus,
   significantly reducing Mean Time To Recovery (MTTR).

Examples
--------
### `WeightedRoundRobinBuilder`

```java
MyInterface proxy =
    new WeightedRoundRobinBuilder<TestInterface>()
        .add(1, newImplementation)
        .add(100, oldImplementation)
        .build(MyInterface.class);
```

### `WeightedShardedBuilder`

```java
MyInterface proxy =
    new WeightedShardedBuilder<TestInterface>()
        .add(1, newImplementation)
        .add(100, oldImplementation)
        .build(MyInterface.class, EXPERIMENT_SEED);
```
This will use the `Object#hashCode` of the _first_ parameter on each method
call to figure out which implementation to delegate to. Call
`WeightedShardedBuilder#setParamSelector` to customize how you will figure out
which implementation to be consequently called.

### `CircuitBreakerFallbackBuilder`

```java
MyInterface proxy =
    new CircuitBreakerFallbackBuilder()
        .build(
            MyInterface.class,
            oldImplementation,
            newImplementation);
```

#### Experiment with parameters

You can use the `simulation` Gradle application to play around with the
`CircuitBreakerFallbackBuilder` parameters. This is what a simulation looks like:

```
$ gradle run :simulation:run

> Task :simulation:run
ms oldImplCalls newImplCalls oldImplExceptions newImplExceptions nPhase1Samples nPhase2Samples
0 0 61 0 0 61 0
60000 0 60 0 0 60 0
120000 2 58 0 0 60 0
180000 0 60 0 0 60 0
240000 0 60 0 0 60 0
300000 0 60 0 0 60 0
360000 1 59 0 0 60 0
420000 0 60 0 0 60 0
480000 9 51 0 32 19 41
540000 20 40 0 40 0 60
600000 38 22 0 22 0 60
660000 60 0 0 0 0 60
720000 60 0 0 0 0 60
780000 59 1 0 1 0 60
840000 60 0 0 0 0 60
900000 59 1 0 1 0 60
960000 39 0 0 0 0 39

BUILD SUCCESSFUL in 1s
13 actionable tasks: 1 executed, 12 up-to-date
```
Notice how as soon as the new implementation starts throwing exceptions, calls
will instead be delegated to the old implementation.

The following will list the parameters you can play around with:
```
$ gradle run :simulation:run --args="--help"

> Task :simulation:run
Flags:

--phase-shift
--steps
--seed
--failure-penalty
--success-credits
--epsilon
--slots
--slot-duration
--duration-per-step
--phase1-orig-impl-error-ratio
--phase1-new-impl-error-ratio
--phase2-orig-impl-error-ratio
--phase2-new-impl-error-ratio

BUILD SUCCESSFUL in 1s
13 actionable tasks: 1 executed, 12 up-to-date
```
