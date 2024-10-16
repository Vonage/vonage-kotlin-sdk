# Vonage Server SDK for Kotlin (JVM)

[![Maven Central](https://img.shields.io/maven-central/v/com.vonage/server-sdk-kotlin)](https://central.sonatype.com/artifact/com.vonage/server-sdk-kotlin)
[![Build Status](https://github.com/Vonage/vonage-kotlin-sdk/actions/workflows/build.yml/badge.svg)](https://github.com/Vonage/vonage-kotlin-sdk/actions/workflows/build.yml)
[![codecov](https://codecov.io/gh/Vonage/vonage-kotlin-sdk/graph/badge.svg?token=YNBJUD8OUT)](https://codecov.io/gh/Vonage/vonage-kotlin-sdk)
![SLOC](https://sloc.xyz/github/Vonage/vonage-kotlin-sdk)
[![Contributor Covenant](https://img.shields.io/badge/Contributor%20Covenant-2.1-4baaaa.svg)](CODE_OF_CONDUCT.md)
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
- [Account](https://developer.vonage.com/en/account/overview)
- [Application](https://developer.vonage.com/en/application/overview)
- [Conversion](https://developer.vonage.com/en/messaging/conversion-api/overview)
- [Messages](https://developer.vonage.com/en/messages/overview)
- [Number Insight](https://developer.vonage.com/en/number-insight/overview)
- [Number Management](https://developer.vonage.com/en/numbers/overview)
- [Number Verification](https://developer.vonage.com/en/number-verification/overview)
- [Redact](https://developer.vonage.com/en/redact/overview)
- [SIM Swap](https://developer.vonage.com/en/sim-swap/overview)
- [SMS](https://developer.vonage.com/en/messaging/sms/overview)
- [Subaccounts](https://developer.vonage.com/en/account/subaccounts/overview)
- [Verify](https://developer.vonage.com/en/verify/overview)
- [Video](https://developer.vonage.com/en/video/overview)
- [Voice](https://developer.vonage.com/en/voice/voice-api/overview)

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

**Note**: We *strongly recommend* that you use a tool with dependency management,
such as [Maven](https://maven.apache.org/) or [Gradle](https://gradle.org/).

Alternatively you can clone the repo and build the JAR file yourself:

```bash
git clone git@github.com:vonage/vonage-kotlin-sdk.git
mvn install -P uberjar
```

The `uberjar` profile will create a JAR file with all dependencies included in the `target`
directory at the root of the repo. You can then include this in your project's classpath.

## Configuration

## Typical Instantiation
For default configuration, you just need to specify your Vonage account credentials using API key and secret,
private key and application ID or both. For maximum compatibility with all APIs, it is recommended that you
specify both authentication methods, like so:

```kotlin
import com.vonage.client.kt.Vonage

val vonage = Vonage {
    apiKey(API_KEY); apiSecret(API_SECRET)
    applicationId(APPLICATION_ID)
    privateKeyPath(PRIVATE_KEY_PATH)
}
```

You can also use environment variables for convenience, by setting the following:
- `VONAGE_API_KEY` - Your account API key
- `VONAGE_API_SECRET` - Your account API secret
- `VONAGE_SIGNATURE_SECRET` - (Advanced, optional) Signature secret for signed requests when using SMS API
- `VONAGE_APPLICATION_ID` - UUID of the Vonage application you want to use
- `VONAGE_PRIVATE_KEY_PATH` - Absolute path to the private key file for the application

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

**A:** This Kotlin SDK is actually based on the Java SDK to improve the user experience in Kotlin. It adds
syntactic sugar, so you can avoid the cumbersome builder pattern in favour of a more idiomatic DSL-like
syntax, optional and named parameters with default values etc. whilst still reataining the strong typing
offered by the Java SDK. Furthermore, you are more partially shielded from "platform types" (the `!`)
so you have a better idea of what is and isn't nullable when creating requests.

**Q: What is your policy on thread safety?**

**A:** As with the Java Server SDK, only one thread should use the client at a time.
If you would like to use the SDK in a multithreaded environment, create a separate instance of
`Vonage` for each thread, or use a ThreadLocal instance.

## Contribute!

_We :heart: contributions to this library!_

It is a good idea to talk to us first if you plan to add any new functionality.
Otherwise, [bug reports](https://github.com/Vonage/vonage-kotlin-sdk/issues),
[bug fixes](https://github.com/Vonage/vonage-kotlin-sdk/pulls) and feedback on the
library are always appreciated.

### Contact
[![Slack](https://img.shields.io/badge/Slack-4A154B?style=flat&logo=slack&logoColor=white)](https://developer.vonage.com/community/slack)
[![Twitter](https://img.shields.io/badge/Twitter-000000?style=flat&logo=x&logoColor=white)](https://twitter.com/VonageDev)
