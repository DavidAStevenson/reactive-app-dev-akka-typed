# Chapter 4: Akka Basic Toolkit - Resilience

Refer to the [book's original repository](https://github.com/ironfish/reactive-application-development-scala/tree/master/chapter4_004_resilience) for details of the functionality of these actors.

This project largely replicates the functionality of the actors in the book, but using the new Akka Typed APIs.

## Summary

This application addresses the faulty behavior of the Librarian actors introduced in the previous section of the cahpter.

Trying to implement this section of the book in Akka Typed highlighted some differences in what is possible with Akka Classic supervision, versus Akka Typed supervision.

In the Akka Classic version of the program in the book, the Librarian becoming frustrated and malfunctioning is modelled by throwing an Exception. The Akka Classic supervision in the RareBooks actor uses the thrown Exception to retrieve the ActorRef of the Customer who complained to the Librarian, and the supervision itself steps in to send the Credit to the Customer instead of the faulty Librarian.

This modelling of the domain does not appear to be possible with Akka Typed supervision. In fact the modelling of the domain with Exceptions would seem to be discouraged - rather than model the scenario with an Exception, it might be better to 

- model the scenario with messages in the RareBooksProtocol
- stick to the Exception behavior, but keep the responsibility of sending the Credit to the Customer that complained with the Librarian actor, for instance by using Akka Persistence so that when the Librarian is restarted by the supervision mechanism, it is able to pick up the workflow that was in progress when it got frustrated

The supervision mechanism is more appropriately used in Akka Typed for circumstances that are out of the control of the faulty actor, such as a dependency such as a network connection dying. 

In this model domain, something like having the "Catalog" be a resource that becomes unavailable might have been a suitable candidate for the supervision example, but would be a hefty change relative to the supervision functionality demonstrated. Same again for introducing Akka Persistence for the purpose of demonstrating supervision.

So, while this was just an scenario from the book example application, if it were a more realistic applications, in having the supervisor know and assume some responsibilities of the Librarians when they fail might be a design flaw, in that the Librarian should be the actor that takes full responsibility for its role, without leaking responsibility to its supervisor in certain scenarios such as the one that is modelled.

See also [this question](https://discuss.lightbend.com/t/supervision-getting-hold-of-exception/6094) at the Akka discuss forum

In summary, my approach to mimic the Akka Classic application diverges somewhat:

- have the faulty Librarian send the message that caused it to fail, back to itself, before throwing the Exception
- have the Librarian router install supervision to restart the Librarians on failure
- a Librarian, upon being restarted (with its bad state having been cleared), will receive the message again, and this time successfully return a credit to the Customer that complained.


## Running the Application

1. Run a terminal session and navigate to the root directory, `reactive-app-dev-akka-typed`.
2. Run `sbt`.
3. Inside the `sbt` session, enter `project chapter4_002_messaging`.
4. Inside the `sbt` session, enter `run` to bootstrap the application
5. After the prompt, `Enter commands [q = quit, 2c = 2 customers, etc.]:` enter `2c<enter>` to start 2 customers
6. Repeat step 5 to start more customers, or enter `q<enter>` to shut the application down

It is also possible to specify the "odds" of a Customers book searches being found, and the number of BookNotFound responses the Customer can "tolerate" before Complaining. The format of commands is "2c <odds> <tolerance>".
For example, `1c 1 0` would create one Customer with 1% odds of requests succeeding, and 0 tolerance for BookNotFound requests.
