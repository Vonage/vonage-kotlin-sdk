# Vonage Server SDK for Kotlin (JVM)

[![Maven Release](https://maven-badges.herokuapp.com/maven-central/com.vonage/server-sdk-kotlin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.vonage/server-sdk-kotlin)
[![Build Status](https://github.com/Vonage/vonage-kotlin-sdk/actions/workflows/build.yml/badge.svg)](https://github.com/Vonage/vonage-kotlin-sdk/actions/workflows/build.yml)
[![codecov](https://codecov.io/gh/Vonage/vonage-kotlin-sdk/graph/badge.svg?token=YNBJUD8OUT)](https://codecov.io/gh/Vonage/vonage-kotlin-sdk)
[![Contributor Covenant](https://img.shields.io/badge/Contributor%20Covenant-v2.0%20adopted-ff69b4.svg)](CODE_OF_CONDUCT.md)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE.txt)


This Kotlin SDK allows you to use [Vonage APIs](https://developer.vonage.com/api) in any JVM-based application.
You'll need to have [created a Vonage account](https://dashboard.nexmo.com/sign-up?utm_source=DEV_REL&utm_medium=github&utm_campaign=java-client-library).

* [Supported APIs](#supported-apis)
* [Other SDKs](#other-sdks)
* [Installation](#installation)
* [Configuration](#configuration)
* [FAQ](#frequently-asked-questions)
* [Contribute!](#contribute)

## Supported APIs
- [Messages](https://developer.vonage.com/en/messages/overview)
- [Verify](https://developer.vonage.com/en/verify/overview)
- [Voice](https://developer.vonage.com/en/voice/voice-api/overview)
- [SMS](https://developer.vonage.com/en/messaging/sms/overview)
- [Conversion](https://developer.vonage.com/en/messaging/conversion-api/overview)
- [Redact](https://developer.vonage.com/en/redact/overview)

## Other SDKs

We also provide server SDKs in other languages:
- [Java](https://github.com/Vonage/vonage-java-sdk)
- [.NET](https://github.com/Vonage/vonage-dotnet-sdk)
- [PHP](https://github.com/Vonage/vonage-php-sdk)
- [Python](https://github.com/Vonage/vonage-python-sdk)
- [Ruby](https://github.com/Vonage/vonage-ruby-sdk)
- [NodeJS](https://github.com/Vonage/vonage-node-sdk)

We also offer [client-side SDKs](https://developer.vonage.com/en/vonage-client-sdk/overview) for iOS, Android and JavaScript.
See all of our SDKs and integrations on the [Vonage Developer portal](https://developer.vonage.com/en/tools).

## Installation

Releases are published to [Maven Central](https://central.sonatype.com/artifact/com.vonage/server-sdk-kotlin).
Instructions for your build system can be found in the snippets section.
They're also available from [here](https://mvnrepository.com/artifact/com.vonage/server-sdk-kotlin/latest).
Release notes can be found in the [changelog](CHANGELOG.md).

### Build It Yourself

Alternatively you can clone the repo and build the JAR file yourself:

```bash
git clone git@github.com:vonage/vonage-kotlin-sdk.git
mvn install
```

### Download everything in a ZIP file

**Note**: We *strongly recommend* that you use a tool that supports dependency management,
such as [Maven](https://maven.apache.org/), [Gradle](https://gradle.org/) or [Ivy](http://ant.apache.org/ivy/).

We provide a [ZIP file for each release](https://github.com/Vonage/vonage-kotlin-sdk/releases/),
containing the Kotlin Server SDK JAR, along with all the dependencies. Download the file, unzip it, and add the JAR files
to your project's classpath.

## Configuration

## Typical Instantiation
For default configuration, you just need to specify your Vonage account credentials using API key and secret, private
key and application ID or both. For maximum compatibility with all APIs, it is recommended that you specify both
authentication methods, like so:

```kotlin
import com.vonage.client.kt.Vonage

val vonage = Vonage {
    apiKey(API_KEY); apiSecret(API_SECRET)
    applicationId(APPLICATION_ID)
    privateKeyPath(PRIVATE_KEY_PATH)
}
```

You can also use environment variables for convenience, by setting the following:
- `VONAGE_API_KEY`
- `VONAGE_API_SECRET`
- `VONAGE_SIGNATURE_SECRET`
- `VONAGE_APPLICATION_ID`
- `VONAGE_PRIVATE_KEY_PATH`

and then instantiate the client with:

```kotlin
val vonage = Vonage { authFromEnv() }
```

### Customization
You can configure the base URI (for example, to do integration tests) and HTTP request timeout with `httpConfig`
during instantiation, like so:

```kotlin
val vonageClient = Vonage {
    authFromEnv()
    httpConfig {
        baseUri("http://localhost:8976")
        timeoutMillis(15000)
    }
}
```

## Frequently Asked Questions

**Q: Why use this SDK instead of the [Vonage Java Server SDK](https://github.com/Vonage/vonage-java-sdk)?**

**A:** This Kotlin SDK is actually based on the to improve the user experience in Kotlin. It adds
syntactic sugar, so you can avoid the cumbersome builder pattern in favour of a more idiomatic DSL-like
syntax. Furthermore, you are more partially shielded from "platform types" (the `!`) so you have a better
idea of what is and isn't nullable when creating requests.

**Q: What is your policy on thread safety?**

**A:** As with the Java Server SDK, only one thread should use the client at a time.
If you would like to use the SDK in a multithreaded environment, create a separate instance of
`Vonage` for each thread, or use a ThreadLocal instance.

## Contribute!

_We :heart: contributions to this library!_

It is a good idea to [talk to us](https://developer.vonage.com/community/slack)
first if you plan to add any new functionality.
Otherwise, [bug reports](https://github.com/Vonage/vonage-kotlin-sdk/issues),
[bug fixes](https://github.com/Vonage/vonage-kotlin-sdk/pulls) and feedback on the
library are always appreciated.
