# GitHub Packages SDKs

The two independent Java 11, MIT-licensed SDKs are available as Maven dependencies. The closed-source runtime plugin is not a Maven development package.

| SDK | Maven coordinates |
| --- | --- |
| Read-only queries and committed-trade notifications | `com.kitemc:kitemarket-api:1.0.0` |
| Page rendering and freely developed IA interfaces | `com.kitemc:kitemarket-ui-api:1.0.0` |

Registry: `https://maven.pkg.github.com/kitemc/KiteMarket`. Both SDKs include sources, Javadoc and their MIT license. Select a version matching the installed host. Runtime JARs, language/configuration archives, working examples and direct SDK downloads remain in [GitHub Releases](https://github.com/KiteMC/KiteMarket/releases).

## Authentication

GitHub requires authentication even for public Maven packages. For local downloads, create a [classic PAT](https://github.com/settings/tokens/new?scopes=read:packages&description=KiteMarket%20SDK%20read) with `read:packages` and use your own GitHub username. Fine-grained PATs are not supported by the Maven registry. Store credentials in your user-level `~/.gradle/gradle.properties`, never in a committed project file:

```properties
gpr.user=YOUR_GITHUB_USERNAME
gpr.key=YOUR_CLASSIC_PAT
```

Alternatively, set `GITHUB_ACTOR` and `GITHUB_TOKEN`. Direct Release downloads need no Maven registry authentication; they still work with `compileOnly(files("libs/KiteMarket-API-1.0.0.jar"))`.

## Gradle Kotlin DSL

```kotlin
repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven {
        name = "KiteMarketPackages"
        url = uri("https://maven.pkg.github.com/kitemc/KiteMarket")
        content { includeGroup("com.kitemc") }
        credentials {
            username = providers.gradleProperty("gpr.user")
                .orElse(providers.environmentVariable("GITHUB_ACTOR")).orNull
            password = providers.gradleProperty("gpr.key")
                .orElse(providers.environmentVariable("GITHUB_TOKEN")).orNull
        }
    }
}
dependencies {
    compileOnly("com.kitemc:kitemarket-api:1.0.0")
    // Add only when extending presentation:
    compileOnly("com.kitemc:kitemarket-ui-api:1.0.0")
    compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
}
```

The public SDK build uses JDK 21 and emits Java 11 bytecode. The IA adapter example still requires Java 21. Neither SDK adds transitive Paper, ItemsAdder or private-core dependencies; add the public APIs needed by your own platform.

Use `compileOnly`. Never bundle, shade or relocate SDK classes into a third-party plugin; KiteMarket supplies the single runtime copy. For Maven, use `provided`:

```xml
<repository>
  <id>github-kitemarket</id>
  <url>https://maven.pkg.github.com/kitemc/KiteMarket</url>
</repository>

<dependency>
  <groupId>com.kitemc</groupId>
  <artifactId>kitemarket-api</artifactId>
  <version>1.0.0</version>
  <scope>provided</scope>
</dependency>
```

Configure credentials in user-level `~/.m2/settings.xml`. Its server ID must match the repository ID:

```xml
<server>
  <id>github-kitemarket</id>
  <username>${env.GITHUB_ACTOR}</username>
  <password>${env.GITHUB_TOKEN}</password>
</server>
```

## Third-party GitHub Actions

Downloading with a workflow's `GITHUB_TOKEN` requires `packages: read` and access to the package from that workflow repository. When that access is unavailable, configure a `PACKAGES_READ_TOKEN` classic PAT secret in the consuming repository and pass it as `GITHUB_TOKEN: ${{ secrets.PACKAGES_READ_TOKEN }}` to the dependency configuration above. SDK consumers need neither package write permission nor KiteMC's `RELEASES_TOKEN`.

## KiteMC publication

The public repository's `Publish SDKs to GitHub Packages` workflow runs when a stable Release is published. It can also be dispatched from [Actions](https://github.com/KiteMC/KiteMarket/actions/workflows/packages.yml), using an existing public tag such as `v1.0.0`. It checks out that tag and uses its own `GITHUB_TOKEN` with `contents: read` and `packages: write`; no cross-repository publishing PAT is required.

Before uploading, it verifies Java 11 bytecode, public class boundaries, Maven coordinates and MIT licensing. An existing byte-identical version is skipped; changed content is rejected. A partially uploaded version stops the workflow for administrator review. Synchronize public SDK source and version before creating a new tag; do not replace an existing version's contents.

This workflow publishes only the two SDKs. It does not build or upload the runtime plugin, mappings, license configuration or private sources, and does not create a Release. The obfuscated runtime release workflow remains separate.

For `401/403`, check classic PAT type, `read:packages`, username, organization SSO authorization and Actions package access. A `404` can also mean that version has not been published. Keep tokens out of logs.

Official references: [Gradle registry](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry), [package permissions](https://docs.github.com/en/packages/learn-github-packages/about-permissions-for-github-packages), [publishing with Gradle Actions](https://docs.github.com/en/actions/use-cases-and-examples/publishing-packages/publishing-java-packages-with-gradle).
