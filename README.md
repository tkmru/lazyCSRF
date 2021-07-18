# LazyCSRF

## Motivation
The feature of burp that I like the most is "Generate CSRF PoC". 
However, this does not support JSON parameters. 
It also uses the `<form>`, so it cannot send PUT/DELETE requests. 
Those were the motivations for creating this extension.

## Features

- Generating CSRF PoC with Burp Community Edition (of course, it also works in Professional Edition)
- Support JSON parameter (like GraphQL Request)
- Support PUT/DELETE (only work with CORS enabled with an unrestrictive policy)
- Support displaying multibyte characters(like Japanese)

## Installation

In Burp, go to the Extensions tab in the Extender tab, and add a new extension. Select the extension type `Java`, and specify the location of your JAR file.

## How to Build
### intellij

If you use IntelliJ IDEA, you can build it by following `Build` -> `Build Artifacts` -> `LazyCSRF:jar` -> `Build`.

### Command line

You can build it with maven.

```
$ mvn install
```

## Usage

![menu](./img/menu.png)

## LICENSE

MIT License

Copyright (C) 2021 tkmru
