# Erasmus & Local Student Matching Platform

**BIL 482 Design Patterns** – TOBB ETÜ, Fall 2026
Instructor: Dr. Çiğdem Avcı

## Team – Sangre y Oro

| Name | Student No | Responsibilities (PA1) |
|---|---|---|
| Alberto Alvarez Polo | 261118082 | Project Definition Document, Software Architecture Overview (diagram + explanation) |
| Alejandro Blas Piris | 261118081 | Project Plan Document, Pattern–Use Case Mapping Matrix |
| Carlos Molina Perez | 261118083 | Software Requirements Specification (use cases, UML diagram, non-functional requirements), integration of architecture and pattern sections into the SRS |

## Project Summary

A multilingual (Turkish, English, Spanish) web platform that matches Erasmus and local students based on shared interests, skills and language goals, and lets them create and join events. Its goal is to prevent Erasmus students from staying isolated within their own groups.

## Core Use Cases

| ID | Use Case | Pattern |
|---|---|---|
| UC1 | Register & Create Profile | Factory Method + Adapter |
| UC2 | Find a Match | Strategy |
| UC3 | Create/Join Event & Get Notified | Observer |
| UC4 | Report Inappropriate Content | Chain of Responsibility |

## Architecture

Layered Architecture: Presentation Layer → Business Layer → Data Access Layer, with external services (eduroam authentication, translation API) connected through Adapters.

## Repository Structure

```
docs/
  PA1/
    1_Project_Definition.pdf                  Project Definition Document
    2_Project_Plan.pdf                        Project Plan Document
    3_Software_Requirements_Specification.pdf SRS (use cases, UML diagram, NFRs,
                                              architecture overview, pattern mapping)
    parts/                                    Individual sections integrated into the SRS
      architecture_overview.pdf
      pattern_mapping_matrix.pdf
diagrams/
  use_case_diagram.png                        UML use case diagram (UC1–UC4)
  architecture_diagram.png                    Layered architecture diagram
```

`docs/PA2`, `docs/PA3` and `src/` will be added with the next assignments.

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

### Traceability conventions

Use cases are numbered UC1–UC4; their steps UCn-S1…, alternative flows UCn-A1… and exceptions UCn-E1…; functional requirements FR-n.m; non-functional requirements NFR-X.n; acceptance tests AT-UCn-m. The same identifiers are used in design documents, class/package names, commit messages and tests.
