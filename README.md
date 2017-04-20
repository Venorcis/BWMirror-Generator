# BWMirror API

A Java API for [BWAPI](http://bwapi.github.io/) and [BWTA2](https://bitbucket.org/auriarte/bwta2) allowing you to create Starcraft: Broodwar AIs using Java.

This repository contains a "generator" project which is used to programmatically generate the  Java classes that will mirror the ones found in BWAPI/BWTA2 and automatically generate the C++ JNI glue that makes it all work together. It's fairly involved, but if you're just interested in writing a Starcraft AI in Java, then you don't need to worry about the details too much!

## Usage

### Including BWMirror in your project

If your project uses Maven, you can add a dependency to BWMirror:

```
<dependency>
    <groupId>com.github.gered</groupId>
    <artifactId>bwmirror</artifactId>
    <version>2.7.1</version>
</dependency>
```

If not, you can download the BWMirror JAR file from the [releases](https://github.com/gered/BWMirror-Generator/releases) page and include it as a dependency in your project.

### Example code

```java
public class ExampleBot {
    public void run() {
        final Mirror mirror = new Mirror();

        mirror.getModule().setEventListener(new DefaultBWListener() {
            @Override
            public void onStart() {
                System.out.println("Starting game");
                BWTA.readMap();
                BWTA.analyze();
            }

            @Override
            public void onEnd(boolean isWinner) {
                System.out.println("Game has ended");
            }

            @Override
            public void onFrame() {
                // this object is ONLY valid while Mirror.startGame is running
                // (which is fine 99% of the time, it just means you should only
                // use it within code that is invoked from these listener methods)
                Game game = mirror.getGame();
                Player self = game.self();

                // TODO: awesome AI code here
            }
        });

        // if you pass false, blocks indefinitely and keeps a Broodwar 
        // instance connection to keep playing subsequent matches.
        // if you pass true, startGame will return after a single match and
        // disconnects from the Broodwar instance. you can call startGame as
        // many times as you wish in this case.
        mirror.startGame(true);
    }

    public static void main(String... args) {
        new ExampleBot().run();
    }
}
```

## Building BWMirror

This is a fair bit involved due to the nature of the project. If you want to contribute to BWMirror, read on. If you are only interested in writing a Starcraft AI, then you can stop here.

### Introduction: How This All Works

Basically, in this repository the `/bwmirror` directory contains the final BWMirror Java library. However, you will find it's missing basically all of the code!

This is because BWMirror consists largely of code that is _automatically generated_ from BWAPI and BWTA2 C++ header files.  The generator project under `/generator` is where all of this code generation occurs. 

The generator project works by parsing the C++ header files and building a list of classes and methods that exist on the C++ side of things. Then these are translated into equivalent "mirror" Java classes. These mirror classes will contain public Java methods that bots will use. As well, there will be an equivalent number of private `native` methods which is where JNI code will be used to call the actual BWAPI/BWTA2 libraries. Note that there are a small number of Java classes that were manually developed and do not contain automatically generated code. These classes are simply copied into the BWMirror project as-is and their source is located under `/manual-bwapi-src`.

`javah` is used to generate JNI function declarations for all of these `native` Java class methods, and then a JNI C++ implementation file, `impl.cpp` is generated with almost all automatically generated function implementations (since the vast majority of these are really basic and simply call BWAPI/BWTA2 functions with the same number of parameters, etc). A very small number of functions in `impl.cpp` are manually implemented using specially-crafted code.

A Visual Studio 2013 project is also included in `/bwapi_bridge` which needs to be used to compile the automatically generated JNI C++ code into a DLL.

All of the automatically generated Java code and the JNI DLL are automatically placed into the appropriate places in the `/bwmirror` directory. Once all of this is done, the BWMirror library simply needs to be built.

> **NOTE:** Generally speaking, you should not be directly editing code under `/bwmirror` or `/output` unless you're just testing/debugging quick changes. This is due to the fact that all of the code in there is automatically being generated/copied.
> 
> The "proper" way to make changes to BWMirror is to modify the generator project and/or edit the Java classes under `/manual-bwapi-src`.


### Installing Pre-Requisites

There are a number of pre-requisites needed before you can get started with the things in this repository.

#### JDK 7 or 8
http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html

You can build BWMirror with a 64-bit JDK, but in order to run/test BWMirror bots, you will need a 32-bit JRE. If you don't want to install both, grab the 32-bit JDK (x86) to make things simpler.

Once you've installed it (or if you already have it installed), take note of the directory where it was installed to. Create an environment variable `JAVA_HOME` that has the location of the root JDK directory. e.g. "C:\Program Files (x86)\Java\jdk1.8.0_121".

It might also be helpful to add the JDK `bin` directory to your `PATH` so you can run `java` directly from a command prompt if needed.

#### Maven
https://maven.apache.org/download.cgi

Download and extract the "Binary zip archive" file and extract it's contents somewhere. If you didn't already have this installed, you'll probably also want to add the Maven `bin` directory that was in the files you extracted to your `PATH` so you can run `mvn` directly from a command prompt.

#### Java IDE
Not a _requirement_ technically, but if you're looking at BWMirror then you're probably intending on developing a bot with Java, so you'll probably want to get a nice IDE. I personally use [IntelliJ](https://www.jetbrains.com/idea/). For the remainder of these instructions I will assume you have some kind of IDE that can open Maven projects.

#### Visual Studio 2013
https://www.visualstudio.com/vs/older-downloads/

Older versions probably won't work. Either get the "Express for Desktop" or "Community" edition. I personally use the Community edition.

#### BWAPI 4.1.2
https://github.com/bwapi/bwapi/releases

Download the installer "BWAPI_412_Setup.exe" and install BWAPI. Take note of where you install it to and create an environment variable `BWAPI_HOME` that has this directory. e.g. "C:\Program Files (x86)\BWAPI"

#### BWTA 2.2.7
https://bitbucket.org/auriarte/bwta2/downloads/

Download and extract the BWTA files somewhere and take note of the location you extracted them to, creating a `BWTA_HOME` environment variable that points to this same location. e.g. "C:\dev\BWTAlib_2.2"

### Running the Generator

If you've not already done so, clone the BWMirror-Generator repository.

Open the Java Maven project in the `/generator` directory in your Java IDE. 

This project is pretty much intended to only be run from an IDE. Build and run the project, using the `main` method class `bwmirror.generator.CJavaPipeline`. Assuming everything so far has been done correctly, this should complete without errors (you can ignore text spam about skipping files, etc from between the "Phase 1 & 2" and "Phase 3" output).

If you run into problems with paths not being found, double check that your environment variables have all been set up correctly (if you already had your IDE open when you were creating these, you may have to re-open it completely). Also in the project run configuration, try explicitly setting the working directory to be same as the `/generator` directory in the repository.

Once you've run it succesfully ...

The generator should have created a few things for us:

* BWAPI and BWTA Java classes under `/bwmirror/src/main/java`
* C++ JNI sources under `/output/bwapi_bridge_src`
* Intermediate JNI headers and Java compiled class files under `/output/compiled` and `/output/headers`. However we don't really care about either of these, the previous two items are what we really want.

Note that the `/manual-bwapi-src` directory has Java sources that will also be copied as-is to `/bwmirror/src/main/java/bwapi`. You can edit these classes from within `/manual-bwapi-src` as needed without worrying about auto-generated code or other build operations clobbering your changes.

### Building `bwapi_bridge.dll`

Open the Visual Studio 2013 project under `/bwapi_bridge` (bwapi_bridge.sln).

Select the "Release" configuration and build the project. Again, assuming everything up until now has been done correctly, this should complete without errors.

Build errors could occur if you have incorrectly set your `JAVA_HOME`, `BWAPI_HOME` and/or `BWTA_HOME` environment variables or you installed (or already had installed) the wrong/older versions of BWAPI or BWTA.

When built successfully, you should see that `bwapi_bridge.dll` was copied from the build output to `/bwmirror/src/main/resources`.

### Building and Testing BWMirror

Open the Java Maven project in the `/bwmirror` directory in your Java IDE.

This is the main BWMirror Java library. You should see all of the following at this point:

* `/src/main/java` has the generated Java classes sources for BWAPI and BWTA.
* `/src/main/resources` has `bwapi_bridge.dll`, `libgmp-10.dll` and `libmpfr-4.dll` as well as a sub-directory `bwapi-data/BWTA2` with a number of `.bwta` files in them (for the SSCAI maps).
* `/src/test/java` has a simple test bot that can be run to verify everything is working.

You can build and install BWMirror to your local Maven repository either within your IDE, or running `mvn install` from the command line within the `/bwmirror` directory. Or if you just want to use a JAR file directly, run the Maven `package` command (or `mvn package` from the command line) and you should find the JAR file under `/bwmirror/target/bwmirror-X.Y.jar` where "X.Y" is the version number).

To run the included test bot to verify everything, set up a run configuration in your IDE to run the project using the `main` method class `bwmirror.TestBot`. Once running and you see that it's waiting to connect to a Broodwar instance, run Chaoslauncher from your BWAPI installation and start up Broodwar and create a single player melee game with yourself and 1 computer player. Once in game, you should see some extra text display and your workers should start gathering minerals and new workers should start getting trained. The bot doesn't do anything else really, but this is a good test of everything.

## License

Licensed under LGPL3. See `LICENSE` for more details.
