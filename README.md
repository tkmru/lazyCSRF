# LazyCSRF

[![GitHub release](https://img.shields.io/github/v/release/tkmru/lazycsrf.svg)](https://github.com/tkmru/lazycsrf/releases/latest)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://github.com/tkmru/lazycsrf/blob/main/LICENSE.md)
[![](https://img.shields.io/badge/Black%20Hat%20Arsenal-EUROPE%202021-blue.svg)](https://www.blackhat.com/eu-21/arsenal/schedule/index.html#lazycsrf-a-more-useful-csrf-poc-generator-on-burpsuite-25088)

LazyCSRF is a more useful CSRF PoC generator that runs on Burp Suite.

## No longer maintenance:bow:
When I started developing lazyCSRF, I mistakenly thought that the CSRF PoC generator built into Burp Professional could not generate PoC using XHR and did not support PUT requests, etc. I am still dissatisfied with the burp built-in CSRF PoC generator, but I think it is sufficient for needs now.
I'm going to finish the maintenance now because I have other attractive themes. I'll do maintenance again when I have some free time.

-------

## Motivation
Burp Suite is an intercepting HTTP Proxy, and it is the defacto tool for performing web application security testing.
The feature of Burp Suite that I like the most is `Generate CSRF PoC`. 
However, the function to automatically determine the content of request is broken, and it will try to generate PoC using `form` even for PoC that cannot be represented by `form`, such as cases using JSON for parameters or PUT requests.
In addition, multibyte characters that can be displayed in Burp Suite itself are often garbled in the generated CSRF PoC.
These were the motivations for creating LazyCSRF.

## Features

- Automatically switch to PoC using XMLHttpRequest
  - In case the parameter is JSON
  - In case the request is a PUT/PATCH/DELETE
- Support displaying multibyte characters (like Japanese)
- Generating CSRF PoC with Burp Suite Community Edition (of course, it also works in Professional Edition)

### Difference in display of multibyte characters

The following image shows the difference in the display of multibyte characters between Burp's CSRF PoC generator and LazyCSRF.
LazyCSRF can generate PoC for CSRF without garbling multibyte characters.
This is only the case if the characters are not garbled on Burp Suite.

![display-japanese](./img/display-japanese.png)

## Installation

Download the JAR from [GitHub Releases](https://github.com/tkmru/lazyCSRF/releases/).
In Burp Suite, go to the Extensions tab in the Extender tab, and add a new extension. 
Select the extension type `Java`, and specify the location of the JAR.

## Usage
You can generate a CSRF PoC by selecting `Extensions`->`LazyCSRF`->`Generate CSRF PoC By LazyCSRF` from the menu that opens by right-clicking on Burp Suite.

![menu](./img/menu.png)

## How to Build
### intellij

If you use IntelliJ IDEA, you can build it by following `Build` -> `Build Artifacts` -> `LazyCSRF:jar` -> `Build`.

### Command line

You can build it with maven.

```
$ mvn install
```

## LICENSE

MIT License

Copyright (C) 2021 tkmru
