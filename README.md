# Code for the "A path to Recursion Schemes" talk

This repository contains the code that was implemented for the [path-to-recschemes.pomadchin.com](https://path-to-recschemes.pomadchin.com/) talk.

During this talk we're going from a straightforward and the most obvious 
DSL implementation towards abstracting over the recursion, to the recursion schemes usage. 

* We'll start with defining a [domain](./src/main/scala/com/pomadchin/domain) for the DSL.
* The [first package](./src/main/scala/com/pomadchin/language/first) contains the 
DSL implementation with an explicit recursion. 
* The [second package](./src/main/scala/com/pomadchin/language/second)
contains the DSL implementation with our own generalized recursion functions defined. 
* The [third package](./src/main/scala/com/pomadchin/language/third) 
is a re-implementation of the **second package** but with the [Droste](https://github.com/higherkindness/droste) library usage. 

## License

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
