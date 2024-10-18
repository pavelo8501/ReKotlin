# REST Service Library

This library provides a simple wrapper for creating REST services using Ktor in Kotlin. 
It includes basic configurations for error handling and a server setup.

## Features

- **Server Configuration**: Allows configuring and starting a Ktor server with customizable host, port, and application module.
- **Error Handling**: Configures error handling for common exceptions like `AuthenticationException`, `AuthorizationException`.
- **CRUD Operations**: Provides a template for implementing CRUD operations for a  `ApiEntity` interface.

## Getting Started

### Prerequisites

- Kotlin
- Gradle
- Ktor

### Installation

Add the following dependencies to your `build.gradle.kts` file:

