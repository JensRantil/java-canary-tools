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

 * `CircuitBreakerFallbackBuilder` allows you to do safe rollout of new Java
   `interface` implementations. Use `CircuitBreakerFallbackBuilder` to
   construct a Java Proxy class that wraps two implementations of the same Java
   interfaces, the _old_ implementation and the _new_ implementation. As long as
   the new implementation never throws any exception, the Proxy class will start
   using it more and more instead of _old_. If _new_ starts throwing exceptions,
   the Proxy class will quickly roll back to use the _old_ implementation.
 * `WeightedShardedBuilder` constructs a Java proxy that implements a Java
   interface shared between different implementations. The proxy delegates to
   downstream implementations using `Object#hashCode()` of the first method
   argument, weighted by the downstream implementation. This class is useful if
   you would like to try out a new Java implementation for a subset of, for
   example, users.
 * `WeightedRoundRobinBuilder` allows you delegate a fraction of calls to
   another implementation of a shared Java interface.

Playing around
--------------
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
480000 0 60 0 0 60 0
540000 1 59 0 0 60 0
600000 0 60 0 0 60 0
660000 0 60 0 0 60 0
720000 1 59 0 0 60 0
780000 0 60 0 0 60 0
840000 0 60 0 0 60 0
900000 0 60 0 0 60 0
960000 0 60 0 0 60 0
1020000 0 60 0 0 60 0
1080000 0 60 0 0 60 0
1140000 0 60 0 0 60 0
1200000 1 59 0 0 60 0
1260000 1 59 0 0 60 0
1320000 0 60 0 0 60 0
1380000 0 60 0 0 60 0
1440000 0 60 0 0 60 0
1500000 0 60 0 0 60 0
1560000 0 60 0 0 60 0
1620000 0 60 0 0 60 0
1680000 0 60 0 0 60 0
1740000 0 60 0 0 60 0
1800000 0 60 0 0 60 0
1860000 0 60 0 0 60 0
1920000 1 59 0 0 60 0
1980000 0 60 0 0 60 0
2040000 2 58 0 0 60 0
2100000 0 60 0 0 60 0
2160000 1 59 0 0 60 0
2220000 1 59 0 0 60 0
2280000 0 60 0 0 60 0
2340000 1 59 0 0 60 0
2400000 0 60 0 0 60 0
2460000 1 59 0 0 60 0
2520000 0 60 0 0 60 0
2580000 0 60 0 0 60 0
2640000 1 59 0 0 60 0
2700000 2 58 0 0 60 0
2760000 0 60 0 0 60 0
2820000 0 60 0 0 60 0
2880000 1 59 0 0 60 0
2940000 0 60 0 0 60 0
3000000 1 59 0 0 60 0
3060000 0 60 0 0 60 0
3120000 0 60 0 0 60 0
3180000 0 60 0 0 60 0
3240000 0 60 0 0 60 0
3300000 1 59 0 0 60 0
3360000 2 58 0 0 60 0
3420000 0 60 0 0 60 0
3480000 1 59 0 0 60 0
3540000 0 60 0 0 60 0
3600000 0 60 0 0 60 0
3660000 1 59 0 0 60 0
3720000 1 59 0 0 60 0
3780000 0 60 0 0 60 0
3840000 0 60 0 0 60 0
3900000 0 60 0 0 60 0
3960000 0 60 0 0 60 0
4020000 0 60 0 0 60 0
4080000 0 60 0 0 60 0
4140000 0 60 0 0 60 0
4200000 0 60 0 0 60 0
4260000 1 59 0 0 60 0
4320000 0 60 0 0 60 0
4380000 1 59 0 0 60 0
4440000 0 60 0 0 60 0
4500000 1 59 0 0 60 0
4560000 1 59 0 0 60 0
4620000 0 60 0 0 60 0
4680000 1 59 0 0 60 0
4740000 1 59 0 0 60 0
4800000 0 60 0 0 60 0
4860000 0 60 0 0 60 0
4920000 0 60 0 0 60 0
4980000 5 55 0 36 19 41
5040000 23 37 0 37 0 60
5100000 45 15 0 15 0 60
5160000 60 0 0 0 0 60
5220000 60 0 0 0 0 60
5280000 59 1 0 1 0 60
5340000 60 0 0 0 0 60
5400000 59 1 0 1 0 60
5460000 60 0 0 0 0 60
5520000 60 0 0 0 0 60
5580000 59 1 0 1 0 60
5640000 60 0 0 0 0 60
5700000 60 0 0 0 0 60
5760000 59 1 0 1 0 60
5820000 60 0 0 0 0 60
5880000 60 0 0 0 0 60
5940000 59 1 0 1 0 60
6000000 60 0 0 0 0 60
6060000 60 0 0 0 0 60
6120000 59 1 0 1 0 60
6180000 60 0 0 0 0 60
6240000 60 0 0 0 0 60
6300000 59 1 0 1 0 60
6360000 59 1 0 1 0 60
6420000 60 0 0 0 0 60
6480000 58 2 0 2 0 60
6540000 59 1 0 1 0 60
6600000 59 1 0 1 0 60
6660000 60 0 0 0 0 60
6720000 60 0 0 0 0 60
6780000 59 1 0 1 0 60
6840000 60 0 0 0 0 60
6900000 60 0 0 0 0 60
6960000 57 3 0 3 0 60
7020000 60 0 0 0 0 60
7080000 60 0 0 0 0 60
7140000 59 1 0 1 0 60
7200000 60 0 0 0 0 60
7260000 58 2 0 2 0 60
7320000 60 0 0 0 0 60
7380000 59 1 0 1 0 60
7440000 60 0 0 0 0 60
7500000 60 0 0 0 0 60
7560000 58 2 0 2 0 60
7620000 59 1 0 1 0 60
7680000 60 0 0 0 0 60
7740000 59 1 0 1 0 60
7800000 60 0 0 0 0 60
7860000 59 1 0 1 0 60
7920000 59 1 0 1 0 60
7980000 59 1 0 1 0 60
8040000 59 1 0 1 0 60
8100000 60 0 0 0 0 60
8160000 60 0 0 0 0 60
8220000 59 1 0 1 0 60
8280000 60 0 0 0 0 60
8340000 60 0 0 0 0 60
8400000 59 1 0 1 0 60
8460000 59 1 0 1 0 60
8520000 60 0 0 0 0 60
8580000 59 1 0 1 0 60
8640000 58 2 0 2 0 60
8700000 59 1 0 1 0 60
8760000 60 0 0 0 0 60
8820000 60 0 0 0 0 60
8880000 59 1 0 1 0 60
8940000 60 0 0 0 0 60
9000000 59 1 0 1 0 60
9060000 60 0 0 0 0 60
9120000 60 0 0 0 0 60
9180000 59 1 0 1 0 60
9240000 60 0 0 0 0 60
9300000 60 0 0 0 0 60
9360000 59 1 0 1 0 60
9420000 60 0 0 0 0 60
9480000 60 0 0 0 0 60
9540000 59 1 0 1 0 60
9600000 60 0 0 0 0 60
9660000 60 0 0 0 0 60
9720000 59 1 0 1 0 60
9780000 60 0 0 0 0 60
9840000 60 0 0 0 0 60
9900000 59 1 0 1 0 60
9960000 38 1 0 1 0 39

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
