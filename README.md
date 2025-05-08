# Vonage Server SDK for Kotlin (JVM)

[![Maven Central](https://img.shields.io/maven-central/v/com.vonage/server-sdk-kotlin)](https://central.sonatype.com/artifact/com.vonage/server-sdk-kotlin)
[![Build Status](https://github.com/Vonage/vonage-kotlin-sdk/actions/workflows/build.yml/badge.svg)](https://github.com/Vonage/vonage-kotlin-sdk/actions/workflows/build.yml)
![CodeQL](https://github.com/Vonage/vonage-kotlin-sdk/actions/workflows/codeql.yml/badge.svg)
[![codecov](https://codecov.io/gh/Vonage/vonage-kotlin-sdk/graph/badge.svg?token=YNBJUD8OUT)](https://codecov.io/gh/Vonage/vonage-kotlin-sdk)
![SLOC](https://sloc.xyz/github/Vonage/vonage-kotlin-sdk?)
[![OpenSSF Scorecard](https://api.scorecard.dev/projects/github.com/Vonage/vonage-kotlin-sdk/badge)](https://scorecard.dev/viewer/?uri=github.com/Vonage/vonage-kotlin-sdk)
<!--[![Contributor Covenant](https://img.shields.io/badge/Contributor%20Covenant-2.1-4baaaa.svg)](CODE_OF_CONDUCT.md)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE.txt)-->

This Kotlin SDK allows you to use [Vonage APIs](https://developer.vonage.com/api) in any JVM-based application.
You'll need to have [created a Vonage account](https://dashboard.nexmo.com/sign-up?utm_source=DEV_REL&utm_medium=github&utm_campaign=java-client-library).

* [Supported APIs](#supported-apis)
* [Other SDKs](#other-sdks)
* [Installation](#installation)
* [Configuration](#configuration)
* [Usage](#usage)
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
- [NodeJS](https://github.com/Vonage/vonage-node-sdk)
- [Ruby](https://github.com/Vonage/vonage-ruby-sdk)

We also offer [client-side SDKs](https://developer.vonage.com/en/vonage-client-sdk/overview) for Android, iOS and JavaScript.
See all of our SDKs and integrations on the [Vonage Developer portal](https://developer.vonage.com/en/tools).

## Installation
Releases are published to [Maven Central](https://central.sonatype.com/artifact/com.vonage/server-sdk-kotlin).
Instructions for your build system can be found in the snippets section.
They're also available from [here](https://search.maven.org/artifact/com.vonage/server-sdk-kotlin/2.1.1/jar).
Release notes for each version can be found in the [changelog](CHANGELOG.md).

Here are the instructions for including the SDK in your project:

### Gradle
Add the following to your `build.gradle` or `build.gradle.kts` file:

```groovy
dependencies {
    implementation("com.vonage:server-sdk-kotlin:2.1.1")
}
```

### Maven
Add the following to the `<dependencies>` section of your `pom.xml` file:

```xml
<dependency>
    <groupId>com.vonage</groupId>
    <artifactId>server-sdk-kotlin</artifactId>
    <version>2.1.1</version>
</dependency>
```

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
The SDK requires very little configuration to get started.

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

## Usage
As with our other SDKs, the architecture is based around the `Vonage` class, which defines the authentication
credentials and optional advanced settings for the HTTP client. The class has a field for each supported API,
which returns an object containing methods available on that API. Where the SDK differs from other SDKs is that
it uses a resource-based approach for CRUD operations, rather than a flat list of methods.
These are inner classes defined for each API resources and are always prefixed with `Existing` - for example,
`ExistingCall`, `ExistingSession`, `ExistingApplication` etc. As a general rule, resources with unique identifiers
have a corresponding `Existing[Resource]` class which is used to perform operations on that resource, rather
than repeatedly passing the ID of that resource to methods on the parent class, as is the case in the Java SDK.
These resource classes are constructed from a method call in the top-level API class. So, for example, to work with
an `ExistingSession`, you would do: `client.video.session(SESSION_ID)`, where `client` is an instance of `Vonage` and
`SESSION_ID` is the unique identifier of the video session you want to work with.

### Examples
You can find complete runnable code samples in the [Code Snippets repository](https://github.com/Vonage/vonage-kotlin-code-snippets),
including [**a searchable list of snippets**](https://github.com/Vonage/vonage-kotlin-code-snippets/blob/main/SNIPPETS.md).

### Documentation
[![javadoc](https://javadoc.io/badge2/com.vonage/server-sdk-kotlin/javadoc.svg)](https://javadoc.io/doc/com.vonage/server-sdk-kotlin)

The SDK is fully documented with [KDocs](https://kotlinlang.org/docs/kotlin-doc.html), so you should have complete
documentation from your IDE. You may need to click "Download Sources" in IntelliJ to get the full documentation.
Alternatively, you can browse the documentation  using a service like [Javadoc.io](https://javadoc.io/doc/com.vonage/server-sdk-kotlin/2.1.1/index.html),
which renders the documentation for you from [the artifacts on Maven Central](https://repo.maven.apache.org/maven2/com/vonage/server-sdk-kotlin/2.1.1/).

For help with any specific APIs, refer to the relevant documentation on our [developer portal](https://developer.vonage.com/en/documentation),
using the links provided in the [Supported APIs](#supported-apis) section. For completeness, you can also consult the
[API specifications](https://developer.vonage.com/api) if you believe there are any discrepancies.

### Custom Requests
The [Java SDK supports custom HTTP requests](https://github.com/Vonage/vonage-java-sdk?tab=readme-ov-file#custom-requests),
which this SDK builds upon. This allows you to use unsupported APIs with your own data models so long as they implement
the `com.vonage.client.Jsonable` interface. Alternatively you can use `Map<String, *>` to represents the JSON structure.
See [Custom.kt](src/main/kotlin/com/vonage/client/kt/Custom.kt) documentation for more details.
Here are some examples for creating an application, all of which are equivalent.

#### Map request, Map response
```kotlin
val response: Map<String, *> = client.custom.post(
        "https://api.nexmo.com/v2/applications",
        mapOf("name" to "Demo Application")
)
```

#### Map request, Jsonable response
```kotlin
val response: Application = client.custom.post(
        "https://api.nexmo.com/v2/applications",
        mapOf("name" to "Demo Application")
)
```

#### Jsonable request, Map response
```kotlin
val response: Map<String, *> = client.custom.post(
        "https://api.nexmo.com/v2/applications",
        com.vonage.client.application.Application.builder()
            .name("Demo Application").build()
)
```

#### Jsonable request, Jsonable response
```kotlin
val response: Application = client.custom.post(
        "https://api.nexmo.com/v2/applications",
        com.vonage.client.application.Application.builder()
            .name("Demo Application").build()
)
```

#### Caveats
The same principle applies for all other supported HTTP methods. You can also use the `makeRequest` method for greater
flexibility on the request and response types. In any case, you should **ALWAYS** use strong typing when assigning
the result of the call, otherwise the compiler will not be able to infer the correct type and you will get a runtime
exception. If you'd like to ignore the result, use `Void` rather than `Unit` or `Any`. For example in `DELETE` requests:

```kotlin
client.custom.delete<Void>("https://api.nexmo.com/accounts/:api_key/secrets/:secret_id")
```

You can see valid usage examples in [CustomTest.kt](src/test/kotlin/com/vonage/client/kt/CustomTest.kt).

## Frequently Asked Questions

**Q: Why use this SDK instead of the [Vonage Java Server SDK](https://github.com/Vonage/vonage-java-sdk)?**

**A:** This Kotlin SDK is actually based on the Java SDK to improve the user experience in Kotlin. It adds
syntactic sugar, so you can avoid the cumbersome builder pattern in favour of a more idiomatic DSL-like
syntax, optional and named parameters with default values etc. whilst still reataining the strong typing
offered by the Java SDK. Furthermore, you are more partially shielded from "platform types" (the `!`)
so you have a better idea of what is and isn't nullable when creating requests. You can read more about the
differences in [the v1.0.0 announcement blog post](https://developer.vonage.com/en/blog/announcing-the-vonage-kotlin-server-sdk).

**Q: What is your policy on thread safety?**

**A:** As with the Java Server SDK, only one thread should use the client at a time.
If you would like to use the SDK in a multithreaded environment, create a separate instance of
`Vonage` for each thread, or use a ThreadLocal instance.

**Q: I'm having issues with my project when including the SDK as a dependency. How can I troubleshoot this?**

**A:** Please see [this blog post](https://developer.vonage.com/en/blog/one-simple-trick-for-resolving-java-runtime-dependency-issues).
In short, you may have conflicting dependency versions in your project which clash with this SDK's transitive dependencies.

**Q: I'm encountering HTTP request issues, such as timeouts. How can I remedy or report this?**

**A:** Since this library uses the Java SDK underneath, which in turn uses
[Apache HTTP Client 4](https://hc.apache.org/httpcomponents-client-4.5.x/index.html), you may be able
to use system properties to configure the client, or use this SDK's `httpConfig` method on the `Vonage` class
for more fine-grained control. If you believe there is an issue with the underlying client, please raise an issue
with a minimal reproducible example, including details of your environment (JVM runtime version, SDK version,
operating system etc.) on the [Vonage Java SDK repository](https://github.com/Vonage/vonage-java-sdk/issues/new/choose).

**Q: I'm not sure if my issue is with the SDK. How can I get help?**

**A:** Please see our [support page](https://api.support.vonage.com/), including contact information.

## Contribute!

_We :heart: contributions to this library!_

It is a good idea to talk to us first if you plan to add any new functionality.
Otherwise, [bug reports](https://github.com/Vonage/vonage-kotlin-sdk/issues),
[bug fixes](https://github.com/Vonage/vonage-kotlin-sdk/pulls) and feedback on the
library are always appreciated. You can also contact us through the following channels:

[![Email](https://img.shields.io/badge/Email-green?style=flat-square&logo=gmail&logoColor=FFFFFF&labelColor=3A3B3C&color=62F1CD)](mailto:community@vonage.com)
[![Community Slack](https://img.shields.io/badge/Slack-4A154B?style=flat&logo=slack&logoColor=white)](https://developer.vonage.com/community/slack)
[![Twitter](https://img.shields.io/badge/Twitter-000000?style=flat&logo=x&logoColor=white)](https://twitter.com/VonageDev)

