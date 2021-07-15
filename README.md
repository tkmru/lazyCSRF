# LazyCSRF

## Motivation

## Features

- Generating CSRF PoC with Burp Community Edition (of course, it also works in Professional Edition)
- Support JSON parameter (like GraphQL Request)
- Support PUT/DELETE (only work with CORS enabled with an unrestrictive policy)

## Installation

In Burp, go to the Extensions tab in the Extender tab, and add a new extension. Select the extension type `Java`, and specify the location of your JAR file.

## How to Build
### inellij

If you use IntelliJ IDEA, you can build it by following `Build` -> `Build Artifacts` -> `LazyCSRF:jar` -> `Build`.

### Command line

```
$ mvn install
```

## Usage


## LICENSE

GPLv3 - GNU General Public License, version 3

Copyright (C) 2021 tkmru
