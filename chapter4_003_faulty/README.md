# Chapter 4: Akka Basic Toolkit - Faulty Librarians

Refer to the [book's original repository](https://github.com/ironfish/reactive-application-development-scala/tree/master/chapter4_003_faulty) for details of the functionality of these actors.

This project largely replicates the functionality of the actors in the book, but using the new Akka Typed APIs.

## Summary

This application adds faulty behavior to the Librarian actors. The faults will be dealt with through Akka's supervision in the next application for this chapter.

## Running the Application

1. Run a terminal session and navigate to the root directory, `reactive-app-dev-akka-typed`.
2. Run `sbt`.
3. Inside the `sbt` session, enter `project chapter4_002_messaging`.
4. Inside the `sbt` session, enter `run` to bootstrap the application
5. After the prompt, `Enter commands [q = quit, 2c = 2 customers, etc.]:` enter `2c<enter>` to start 2 customers
6. Repeat step 5 to start more customers, or enter `q<enter>` to shut the application down
7. Note that due to faulty Librarians and the impact this has on Customers, RareBooks will eventually cease to receive messages from the Customers.

It is also possible to specify the "odds" of a Customers book searches being found, and the number of BookNotFound responses the Customer can "tolerate" before Complaining. The format of commands is "2c <odds> <tolerance>".
For example, `1c 1 0` would create one Customer with 1% odds of requests succeeding, and 0 tolerance for BookNotFound requests.
