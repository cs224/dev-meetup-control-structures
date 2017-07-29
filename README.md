
Use the following link to brose the dev-meetup content: [dev-meetup-control-structures](https://rawcdn.githack.com/cs224/dev-meetup-control-structures/master/index.html)

You can rebuild the document locally by executing:
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ {.bash}
> rm index.html
> make index.html
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Keyword List: Control Structures and Paradigms:

* Parallel Universe: Quasar Library: Continuations for Core Java and the JVM
* Continuations, Coroutines, Generators
* Continuation Passing Style (CPS)
* Flow-Based Programming (FBP)
* Communicating Sequential Processes (CSP)
  * Clojure core.async
* Complex Event Processing (CEP)
* Actors
  * "Let it crash" vs. try-catch-finally
* Incremental Computing
  * Functional Reactive Programming (FRP)
    * Reactive Extension (Rx)
  * Propagators
  * Self-Adjusting Computation
  * Bidirectional Programming (Lenses)
* Fiber, Green Threads, Light-Weight Thread, Cooperative Multi-Tasking
* Push vs. Pull, Polarity of Pipelines
* Thread Based vs. Event Driven
* Cooperative Multi-Tasking vs. Pre-Emptive Multi-Tasking
* (pull, fiber/stack-based and lazy) form a unit and (push, event-driven and eager) form a unit
* Cooperative Task Management without Manual Stack Management, avoiding “call-back hell” and “stack ripping” in single threaded applications
* Event Sourced Architectures, CQRS

Core Message

The core of the message that I want to share with you is that event-driven or fiber style programming are from a performance perspective equivalent.

Actually event-driven, fiber-style, reactive programming, data-flow programming, complex event processing or communicating-sequential-processes are all from a performance perspective equivalent. There exist mechanical transformations between the one style of programming into the other style. These mechanical transformations can be performed under the hood via compiler technology, e.g. via byte code manipulation.

If from a performance perspective all of these programming styles are equivalent then the choice of which style to use should be based on factors like how well the code is understandable, e.g. in a static set-up as code visible in your IDE without running the program. Here the fiber style is much better suited for us humans to understand than the event-driven style!
