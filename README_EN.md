<!--
PUBLIC FILE: This README is published externally.
Do not add private repository names, internal architecture, protection or encryption implementation details,
credentials, keys, internal build procedures, or permission details.
Only the public project overview, the approved closed-source statement, and the external contribution guide belong here.
-->

# GTOCore-Main

[中文](README.md)

## Project Overview

GTOCore is the unified core mod for GregTech Odyssey. It targets Minecraft 1.20.1 and Forge 47.4.20, and uses the mod ID `gtocore`.

This repository contains the public GTOCore source code, resources, data-generation entry points, and build configuration. External contributors can compile, generate data, and run the project using the dependencies included in the repository. Access to private repositories and initialization of private submodules are not required.

Public code under `com.gtocore` is distributed under the repository [LICENSE](LICENSE). gtolib remains a closed-source component.

## gtolib Closed-Source Statement

#### Why gtolib in gtocore Chooses Closed Source

We regret to announce that due to persistent code plagiarism issues, we have been forced to temporarily close-source the core code of gtolib.

**The Situation:**

- Multiple individuals or groups directly **plagiarize and misappropriate** our open-source library
- Performed batch replacements, changing all "gto" identifiers to their own
- Completely plagiarized our development achievements and redistributed them
- Claimed these were their own independently developed integration packages, completely disregarding the original creators' rights
- When faced with our protests and communications, they refused to stop their infringement under the excuse of "learning purposes, will modify later"

**Evidence Preservation:**

We have promptly preserved relevant evidence. A backup of one plagiarist's repository can be found at:
[https://github.com/wanggugu197/Pillar_of_shame](https://github.com/wanggugu197/Pillar_of_shame)

Within days of unsuccessful communication attempts, we officially began the closed-source process for our core code.

**Our Position:**

This decision is made to protect our team's two years of dedicated work. We believe genuine developers will understand and support the protection of original creators' rights.

Thank you for your understanding and support.

## External Contribution Guide

### Contribution Scope

Contributions are welcome in the following areas:

- Public source code under `src/main/java/com/gtocore/`
- Resources and generated data related to the public source code under `src/main/resources/` and `src/generated/resources/`
- Build scripts and documentation for the public project
- Bug fixes, compatibility improvements, performance optimizations, and maintainability improvements

To contribute to private modules, contact the maintainers first to understand the current situation and obtain the appropriate authorization.

Do not directly modify or replace closed-source components or prebuilt binary dependencies tracked in this repository. Please open an Issue for related problems so that the maintainers can determine whether internal changes are required.

### Development Requirements

- Java 21
- Git
- Network access to Gradle and the project's dependency repositories
- Sufficient available memory; the current Gradle configuration allows a maximum heap size of 8 GiB

Confirm that `java -version` reports Java 21, and configure both your IDE and terminal to use the same JDK.

### Clone and Build

External contributors only need a normal clone. Do not initialize private submodules:

```bash
git clone https://github.com/GregTech-Odyssey/GTOCore-Main.git
cd GTOCore-Main
bash ./gradlew build
```

If you have gtolib access: after `cd`, run `git submodule update --init GTOLib`, then `bash ./gradlew build`.

On Windows PowerShell or Command Prompt:

```bat
gradlew.bat build
```

The first build downloads Gradle and project dependencies, so it will take considerably longer than later builds.

### IDE Import

Open the repository root in IntelliJ IDEA or another IDE with Gradle support:

1. Set both the project JDK and the Gradle JVM to Java 21.
2. Import the repository as a Gradle project and wait for dependency synchronization to finish.
3. Do not add `build/`, `run/`, or local IDE configuration files to a commit.

### Common Tasks

| Purpose | macOS / Linux command |
|---------|-----------------------|
| Format source code and resources | `bash ./gradlew spotlessApply` |
| Run a full build | `bash ./gradlew build` |
| Start the development client | `bash ./gradlew runClient` |
| Start the development server | `bash ./gradlew runServer` |
| Generate data | `bash ./gradlew runData` |

`runData` writes to `src/generated/resources/`. Review the generated output before committing and keep only files related to your change.

### Recommended Contribution Workflow

1. Create a dedicated development branch from the latest `main`.
2. Open an Issue and confirm the direction with the maintainers before starting a large feature or architectural change.
3. Keep each commit and Pull Request focused on one clear objective.
4. Follow the existing code style and avoid unrelated formatting or broad refactoring.
5. Run `spotlessApply`, followed by runtime checks appropriate to the change.
6. Run at least one full `build`.
7. Review the changes with `git diff` and exclude generated outputs, logs, caches, credentials, and other local files.
8. Push the branch and open a Pull Request.

### Pull Request Requirements

Each Pull Request should include:

- The purpose of the change and an implementation summary
- The main affected areas
- Build and runtime verification performed
- A related Issue, if applicable
- Screenshots or recordings for UI or in-game behavior changes
- Known limitations or cases not yet covered

Maintainers may request implementation changes, additional verification, or a narrower scope. Do not submit code or resources with unknown origins or incompatible licenses.

### License

By contributing, you agree that your contribution will be distributed under the terms of this repository's [LICENSE](LICENSE).
