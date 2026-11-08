# Erasmus & Local Student Matching Platform

**BIL 482 Design Patterns** – TOBB ETÜ, Fall 2026
Instructor: Dr. Çiğdem Avcı

## Team – Sangre y Oro

| Name | Student No | Responsibilities (PA1) | Responsibilities (PA2) |
|---|---|---|---|
| Alberto Alvarez Polo | 261118082 | Project Definition Document, Software Architecture Overview (diagram + explanation) | Presentation Layer and web UI, Adapter pattern, SDD sections 6, 11, 12 |
| Alejandro Blas Piris | 261118081 | Project Plan Document, Pattern–Use Case Mapping Matrix | Strategy pattern and matching algorithm, SDD sections 7, 9 |
| Carlos Molina Perez | 261118083 | Software Requirements Specification (use cases, UML diagram, non-functional requirements), integration of architecture and pattern sections into the SRS | Observer pattern, match requests, domain and data layers, tests, build, SDD integration |

Detailed PA2 workload: [TASK_MATRIX.md](TASK_MATRIX.md)

## Project Summary

A multilingual (Turkish, English, Spanish) web platform that matches Erasmus and local students based on shared interests, skills and language goals, and lets them create and join events. Its goal is to prevent Erasmus students from staying isolated within their own groups.

## Core Use Cases

| ID | Use Case | Pattern |
|---|---|---|
| UC1 | Register & Create Profile | Factory Method + Adapter |
| UC2 | Find a Match | Strategy (+ Adapter for translation, Observer for notifications) – **implemented in PA2** |
| UC3 | Create/Join Event & Get Notified | Observer |
| UC4 | Report Inappropriate Content | Chain of Responsibility |

## Architecture

Layered Architecture: Presentation Layer → Business Layer → Data Access Layer, with external services (eduroam authentication, translation API) connected through Adapters.

## Repository Structure

```
README.md                                     This guide
TASK_MATRIX.md                                PA2 workload distribution
build.sh / build.bat                          Build, test and package (Linux/macOS / Windows)
data/
  students.csv                                Demo student profiles used by UC2
docs/
  PA1/
    1_Project_Definition.pdf                  Project Definition Document
    2_Project_Plan.pdf                        Project Plan Document
    3_Software_Requirements_Specification.pdf SRS (use cases, UML diagram, NFRs,
                                              architecture overview, pattern mapping)
    parts/                                    Individual sections integrated into the SRS
  PA2/
    SoftwareDesignDocument.pdf                Software Design Document of UC2
diagrams/
  use_case_diagram.png                        UML use case diagram (UC1–UC4)
  architecture_diagram.png                    Layered architecture diagram
src/
  main/java/com/sangreyoro/matching/
    presentation/                             Web server, REST JSON API (MatchController)
    business/MatchingService.java             UC2 use-case service – Strategy Context
    business/strategy/                        Strategy pattern (3 matching criteria)
    business/notification/                    Observer pattern (match events)
    business/translation/                     Adapter pattern (translation API)
    data/                                     Repositories (Data Access Layer)
    domain/  exception/  util/                Model, errors with SRS flow ids, helpers
    MatchingApplication.java                  Entry point / composition root
  main/resources/web/index.html               Web UI (Turkish / English / Spanish)
  test/java/com/sangreyoro/matching/          18 traceable tests (UC2-T01 … UC2-T18)
```

## Deliverables

### PA1 – Use Cases, Software Architecture and Pattern Mapping

| Deliverable | File |
|---|---|
| Project Definition Document | [1_Project_Definition.pdf](docs/PA1/1_Project_Definition.pdf) |
| Project Plan Document | [2_Project_Plan.pdf](docs/PA1/2_Project_Plan.pdf) |
| Software Requirements Specification | [3_Software_Requirements_Specification.pdf](docs/PA1/3_Software_Requirements_Specification.pdf) |
| Software Architecture Overview (diagram + explanation) | SRS Section 6 · [architecture_diagram.png](diagrams/architecture_diagram.png) |
| Pattern–Use Case Mapping Matrix | SRS Section 7 |
| UML Use Case Diagram | SRS Section 3.1 · [use_case_diagram.png](diagrams/use_case_diagram.png) |

### PA2 – Use Case Implementation with Design Pattern Integration (UC2 Find a Match)

| Deliverable | File |
|---|---|
| Software Design Document | [SoftwareDesignDocument.pdf](docs/PA2/SoftwareDesignDocument.pdf) |
| Source code of the selected use case | [src/](src) |
| Task matrix | [TASK_MATRIX.md](TASK_MATRIX.md) |

#### How to build and run

Requirements: **JDK 17 or newer**. No Maven, no internet connection and no third-party libraries are needed.

```bash
./build.sh          # Linux / macOS: compile, run the 18 tests, create out/matching-uc2.jar
./build.sh run      # ... and start the application
build.bat run       # Windows
java -jar out/matching-uc2.jar 8080     # run the packaged application
```

Then open **http://localhost:8080**. Suggested review scenario:

1. Logged in as **María García** (Erasmus, Spanish, learns Turkish), choose *Language Exchange*: local students who speak Turkish and learn Spanish appear first, with their score and reasons (UC2-S1…S6).
2. Switch the interface to *Español* or *Türkçe*; press **Translate** on Emre's Turkish description (UC2-A2, Adapter).
3. Send a match request to **Emre Yılmaz** (UC2-S7, S8).
4. Change the logged-in student to Emre: the notification has arrived (Observer). **Accept** it: both students are notified and see each other's contact (UC2-S9).
5. Choose **Sofía Moreno** (incomplete profile) to see exception UC2-E1. Burak (who blocked María) and Ayşe (matching disabled) are never shown (FR-2.5).

#### Where the patterns are

| Pattern | Role → class | Package |
|---|---|---|
| Strategy | Strategy `MatchingStrategy`, ConcreteStrategies `LanguageExchangeStrategy`, `HobbyMatchingStrategy`, `SportsMatchingStrategy`, Context `MatchingService` | `business/strategy` |
| Observer | Subject `MatchEventPublisher`, Observer `MatchObserver`, ConcreteObservers `InAppNotificationObserver`, `MatchAuditLogObserver` | `business/notification` |
| Adapter | Target `TranslationService`, Adapter `TranslationAdapter`, Adaptee `MockTranslationApiClient` | `business/translation` |

Each pattern class has a header comment with the pattern name, intent, motivation, improvement and measured metrics.

#### Test results

```
18 tests, 18 passed, 0 failed
[metric] average findMatches time with 1,000 students: ~2 ms (limit NFR-P.2: 3,000 ms)
```

### Traceability conventions

Use cases are numbered UC1–UC4; their steps UCn-S1…, alternative flows UCn-A1… and exceptions UCn-E1…; functional requirements FR-n.m; non-functional requirements NFR-X.n; acceptance tests AT-UCn-m. Tests of the implemented use case are numbered UC2-T01…T18 and data validation rules DV-01…DV-07. The same identifiers are used in design documents, code comments, exception flow ids, commit messages and tests.
