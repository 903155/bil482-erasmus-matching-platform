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
  PA1/   Project Definition, Project Plan, SRS
  PA2/
  PA3/
diagrams/  Use case and architecture diagrams
src/       Source code (later)
```

## Deliverables

- [PA1](docs/PA1) – Use Cases, Software Architecture and Pattern Mapping
