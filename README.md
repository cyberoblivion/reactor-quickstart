# Reactor quickstart

These examples are just some quick sketches of what you can do with Reactor. They show how to consume events, how to publish them, how to use different Dispatchers for different processing tasks, and how to compose actions around data streams.

[![Build Status](https://drone.io/github.com/reactor/reactor-quickstart/status.png)](https://drone.io/github.com/reactor/reactor-quickstart/latest)

### Build

    git clone https://github.com/reactor/reactor-quickstart.git
    cd reactor-quickstart
    ./gradlew compileJava

### Running the samples

There are classes in each submodule that are simple static main classes. There is brief documentation in each example about what each component does.

The components include:

* core - main components shared by all the examples
* simple - simple event handling using a Reactor and Consumer directly
* composable - example of using a Composable to wire components together
* groovy - example of using the Groovy helpers to make writing Consumers easier
* websocket - example of using the Jetty WebSocket API to write websocket-based Reactor applications
* spring - example of using Spring Boot to tie a `@RestController` to a `Reactor`

### License

Like Reactor, these samples are all [Apache 2.0 licensed](http://www.apache.org/licenses/LICENSE-2.0.html).
