![fritz2](https://www.fritz2.dev/images/fritz2_logo_grey.png)

# Ktor Chat

This project is an example for using [fritz2](https://www.fritz2.dev/) with in a ktor websocket-application.
It will demonstrate how-to interact with a backend application in fritz2 and how-to setup this kind of project.

This project is a Kotlin [multiplatform](https://kotlinlang.org/docs/reference/multiplatform.html) project which contains everything.
In the `commonMain` section is the shared model and validation of the application.
The `jsMain` section contains the complete code of the client-application written with fritz2. 
The backend part is in the `jvmMain` section where all the code for the server is.

This project uses the following libraries:
* [fritz2](https://github.com/jwstegemann/fritz2) - mainly in the frontend, except validation and model on both sides
* [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) - used in frontend and backend for serializing the shared model
* [Ktor](https://ktor.io/) - running the server with
* [Ktor Websockets](https://ktor.io/docs/servers-features-websockets.html) - providing serverside Websocket support

# Features
TODO

# Run
To run this application you only need to run the following gradle task:
```
> ./gradlew run 
``` 
Then navigate in your browser to [localhost:8080](http://localhost:8080/)

# Fat Jar
To build a runnable fat jar with all needed dependencies included you only have to run:
```
> ./gradlew shadowJar 
``` 
The bundled fat jar is located under `build/libs/fritz2-ktor-chat-1.0-all.jar`.
You can easily run it by calling:
```
> java -jar fritz2-ktor-chat-1.0-all.jar
```

# Contribution
If you like this example and how fritz2 works it would be great 
when you give us a &#11088; at our [fritz2 github page](https://github.com/jwstegemann/fritz2).

When you find some bugs or improvements please let us know by reporting an issue 
[here](https://github.com/jamowei/fritz2-spring-todomvc/issues).
Also, feel free to use this project as template for your own applications.