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
`CircuitBreakerFallbackBuilder` parameters.
