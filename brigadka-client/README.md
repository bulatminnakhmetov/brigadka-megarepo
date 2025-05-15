# Brigadka App

Brigadka is a mobile application designed for improv performers to create profiles, search for teammates, and connect with other improvisers.

## Features

- **User Profiles**: Create and manage your improv performer profile with personal information, bio, and media
- **Profile Discovery**: Search for other improvisers based on various criteria including location, age, and improv styles
- **Media Sharing**: Upload avatar images and videos to showcase your performances
- **Messaging**: Contact other users directly through the application

## Technical Implementation

The app is built using Kotlin Multiplatform with Compose Multiplatform for the UI layer. This enables sharing code across Android and iOS platforms while maintaining a native user experience.

### Key components:

- **API Service**: Communicates with the backend for data retrieval and user authentication
- **Repository Pattern**: Abstracts the data sources and provides clean interfaces for the presentation layer
- **Coroutines**: Handles asynchronous operations efficiently
- **Compose UI**: Implements a modern, declarative UI with support for theming

## Project Structure

- **data**: Contains API models, repositories, and data sources
- **domain**: Business logic and use cases
- **presentation**: UI components, view models, and screens
- **common**: Shared utilities and extensions

## Development Setup

1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle files
4. Run the app on your preferred device or emulator

## Dependencies

- Kotlin Multiplatform
- Compose Multiplatform
- Ktor for networking
- KotlinX Serialization
- Kermit for logging