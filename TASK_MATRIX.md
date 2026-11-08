# Task Matrix – Project Assignment 2 (PA2)

**Selected use case:** UC2 – Find a Match · **Patterns:** Strategy, Adapter, Observer
**Team:** Sangre y Oro – Alberto Alvarez Polo (261118082), Alejandro Blas Piris (261118081), Carlos Molina Perez (261118083)

## 1. Implementation

| # | Task | Files / packages | Responsible |
|---|---|---|---|
| I-1 | Strategy pattern: strategy interface, three matching strategies, strategy registry | `src/main/java/.../business/strategy/` | Alejandro Blas Piris |
| I-2 | Use-case service UC2-S1…S6 (`findMatches`, eligibility rules, ranking) | `business/MatchingService.java` (search part) | Alejandro Blas Piris |
| I-3 | Match requests UC2-S7…S9, UC2-A3 (`sendRequest`, `respond`) | `business/MatchingService.java` (request part) | Carlos Molina Perez |
| I-4 | Observer pattern: publisher, observer interface, in-app and audit-log observers | `business/notification/` | Carlos Molina Perez |
| I-5 | Adapter pattern: translation interface, adapter, mock third-party API, description service (UC2-A2, UC2-E4) | `business/translation/` | Alberto Alvarez Polo |
| I-6 | Domain model and exception hierarchy with SRS flow ids | `domain/`, `exception/` | Carlos Molina Perez |
| I-7 | Data Access Layer: repositories, CSV loader, demo data | `data/` (package), `data/students.csv` | Carlos Molina Perez |
| I-8 | Presentation Layer: web server, REST JSON API, static files | `presentation/` | Alberto Alvarez Polo |
| I-9 | Web user interface in Turkish, English and Spanish | `src/main/resources/web/index.html` | Alberto Alvarez Polo |
| I-10 | Composition root (Dependency Injection), JSON writer, logging | `MatchingApplication.java`, `util/` | Carlos Molina Perez |
| I-11 | Pattern comments with intent, motivation, improvement and metrics | all pattern classes | each author for their own classes |

## 2. Tests and build

| # | Task | Files | Responsible |
|---|---|---|---|
| T-1 | Dependency-free test kit (runner + assertions) | `src/test/java/.../testkit/` | Carlos Molina Perez |
| T-2 | Strategy unit tests UC2-T01…T03 | `StrategyTest.java` | Alejandro Blas Piris |
| T-3 | Service tests UC2-T04…T10, T14, T16 (incl. performance) | `MatchingServiceTest.java`, `Fixture.java` | Carlos Molina Perez |
| T-4 | Adapter / Observer / data tests UC2-T11…T13, T15, T17 | `PatternTest.java` | Carlos Molina Perez |
| T-5 | End-to-end HTTP test UC2-T18 | `ApiTest.java` | Alberto Alvarez Polo |
| T-6 | Build scripts and packaging | `build.sh`, `build.bat`, `.gitignore` | Carlos Molina Perez |

## 3. Software Design Document (`docs/PA2/SoftwareDesignDocument.pdf`)

| Sections | Responsible |
|---|---|
| 1–5 Introduction, overview, context, features, assumptions | Carlos Molina Perez |
| 6 Architectural design | Alberto Alvarez Polo |
| 7 Component design, sequence and state diagrams | Alejandro Blas Piris |
| 8 Data design (ER, storage, DFD, validation) | Carlos Molina Perez |
| 9 Design patterns and metrics | Alejandro Blas Piris |
| 10 Implementation notes | Carlos Molina Perez |
| 11–12 User interface, external interfaces | Alberto Alvarez Polo |
| 13–18 Performance, errors and logging, testability, deployment, change log, future work | Carlos Molina Perez |
| Review of the document | Alberto Alvarez Polo, Alejandro Blas Piris |
| Integration, formatting, README, this task matrix | Carlos Molina Perez |

## 4. Summary of effort

| Member | Main contribution |
|---|---|
| Alberto Alvarez Polo | Presentation Layer, web UI, Adapter pattern, API test, SDD sections 6, 11, 12 |
| Alejandro Blas Piris | Strategy pattern, matching algorithm, strategy tests, SDD sections 7, 9 |
| Carlos Molina Perez | Observer pattern, requests, domain and data layers, test kit and most tests, build, SDD integration |
