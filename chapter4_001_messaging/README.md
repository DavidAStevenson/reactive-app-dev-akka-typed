# Chapter 4: Akka Basic Toolkit - Messaging

Refer to the [book's original repository](https://github.com/ironfish/reactive-application-development-scala/tree/master/chapter4_001_messaging) for details of the functionality of these actors.

This project largely replicates the functionality of the actors in the book, but using the new Akka Typed APIs.

## Summary

While being message based, this version of the application has a bottleneck in that the RareBooks actor only creates a single Librarian child to serve Customer requests. Starting up many Customers will simply see the requests in excess of what the single Librarian can handle, being dropped.

One difference in this implementation as compared with the book's, is that only limited messages are stashed while RareBooks is in the Closed state, and the Librarian itself only stashes limited numbers of messages. This means, in effect, that Customers who have their requests lost, will never receive a response. Future "enhancements" to improve this might
- have the Librarian escalate to its supervisor if it gets to the point where it is needing to drop messages to avoid overflowing its stash buffer
- have RareBooks itself communicate to the Customers in the case their requests cannot be serviced.
- have the Customers make a re-request if they don't hear back from a Librarian in a certain amount of time (but this isn't really in line with the idea of RareBooks itself being "Reactive" - responsive to requests from clients)

However, while RareBooks is constrained by having a single (of fixed number of) Librarians, throughput is constrained, even if the Responsiveness of the application were enhanced.

## Running the Application

1. Run a terminal session and navigate to the root directory, `reactive-app-dev-akka-typed`.
2. Run `sbt`.
3. Inside the `sbt` session, enter `project chapter4_001_messaging`.
4. Inside the `sbt` session, enter `run` to bootstrap the application
5. After the prompt, `Enter commands [q = quit, 2c = 2 customers, etc.]:` enter `2c<enter>` to start 2 customers
6. Repeat step 5 to start more customers, or enter `q<enter>` to shut the application down

It is also possible to specify the "odds" of a Customers book searches being found, and the number of BookNotFound responses the Customer can "tolerate" before Complaining. The format of commands is "2c <odds> <tolerance>".
For example, `1c 1 0` would create one Customer with 1% odds of requests succeeding, and 0 tolerance for BookNotFound requests.
