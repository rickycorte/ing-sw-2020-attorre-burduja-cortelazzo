![Java CI](https://github.com/rickycorte/ing-sw-2020-attorre-burduja-cortelazzo/workflows/Java%20CI/badge.svg)
[![codecov](https://codecov.io/gh/rickycorte/ing-sw-2020-attorre-burduja-cortelazzo/branch/master/graph/badge.svg?token=52N59J99Y8)](https://codecov.io/gh/rickycorte/ing-sw-2020-attorre-burduja-cortelazzo)



# ing-sw-2020-attorre-burduja-cortelazzo
Software engineering project AA2019-2020 Politecnico di Milano

Final grade: 30 cum Laude / 30

## Group AM44

**DISCLAIMER:** this is a university project, we do not own any right on Santorini game and all the visual assets. Game and assets are owned by [Roxley Games](https://roxley.com/). For any copyright issues send me a message and I'll remove this repo as soon as possible.


| Functionality | State |
|:-----------------------|:------------------------------------:|
| Basic rules |[![GREEN](https://placehold.it/15/44bb44/44bb44)](#) |
| Complete rules | [![GREEN](https://placehold.it/15/44bb44/44bb44)](#) |
| Socket | [![GREEN](https://placehold.it/15/44bb44/44bb44)](#) |
| GUI | [![GREEN](https://placehold.it/15/44bb44/44bb44)](#) |
| CLI | [![GREEN](https://placehold.it/15/44bb44/44bb44)](#) |
| Multiple games | [![GREEN](https://placehold.it/15/44bb44/44bb44)](#) |
| Persistence | [![RED](https://placehold.it/15/f03c15/f03c15)](#) |
| Advanced Gods | [![RED](https://placehold.it/15/f03c15/f03c15)](#) |
| Undo | [![GREEN](https://placehold.it/15/44bb44/44bb44)](#) |

<!--
[![RED](https://placehold.it/15/f03c15/f03c15)](#)
[![YELLOW](https://placehold.it/15/ffdd00/ffdd00)](#)
[![GREEN](https://placehold.it/15/44bb44/44bb44)](#)
-->

# Getting started

To build and run this project you need to install Java 13 or later.

After cloning this repo you can:
- Build and run tests with `mvn package`
- Build docs with `mvn javadoc:javadoc`

Notice: Codecov badge shows only Controller and Model coverage.
Network and view tests are not required by specification thus the packages are skipped.

## Starting the server

First you need to create a server instance to host the matches with:

`java -jar AM44.jar -s`

This will run a server on the default port. You can also use a custom port by running:

`java -jar AM44.jar -s <port>`

## Starting the client

Both GUI and CLI are supported and can be used to play the game.

If you want to run a new GUI client you can run:

`java -jar AM44.jar`

If you have set the Java path correctly you should be able to also use a double click to open a GUI client.

If you want to create a CLI client you can run:

`java -jar AM44.jar -cli`
