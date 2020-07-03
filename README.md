# reactive-app-dev-akka-typed

I found the Manning book [*Reactive Application Development*](https://www.manning.com/books/reactive-application-development) to be a good introduction to its topic, but since the book was published, fast-moving Akka has recently, since version 2.6, encouraged new development be done using the newer Akka Typed APIs, over the Akka "Classic" APIs.

This repo follows the spirit of the sample programs in *Reactive Application Development*, but I have used the book as a roadmap to learn the Akka Typed APIs, and the differences between them and the Akka Classic APIs.

There are some pretty significant differences in Akka Typed to the Akka Classic APIs, but fortunately the Akka documentation has a useful reference page overviewing the differences between Classic and Typed APIs [here](https://doc.akka.io/docs/akka/current/typed/from-classic.html).

## running

1) Use sbt to start-up

```
sbt
```

2) Change to a subproject per chapter

```
sbt> project chapter2
```

See README.md in subproject directories for more on each.

- [chapter2](chapter2/README.md)
- [chapter4_001_messaging](chapter4_001_messaging/README.md)
- [chapter4_002_elasticity](chapter4_002_elasticity/README.md)
- [chapter4_003_faulty](chapter4_003_faulty/README.md)
- [chapter4_004_resilience](chapter4_004_resilience/README.md)
