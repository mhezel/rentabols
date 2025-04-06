# Rentabols

A mobile application that connects item lenders and renters, allowing users to rent and lend items in their local area.

## Features

- User authentication (login/signup)
- Item search and discovery
  - Keyword search
  - Filters and categories
  - Map-based search
  - Location-based suggestions
- Item rental management
  - Item posting with photos
  - Direct rental requests
  - Transaction tracking
  - Push notifications
- User interactions
  - Ratings and reviews
  - Real-time messaging

## Technical Stack

- **Architecture**: MVVM with Clean Architecture
- **UI**: Jetpack Compose with Material3
- **Navigation**: Navigation Compose
- **Backend**: Firebase
  - Authentication
  - Firestore
  - Cloud Storage
  - Cloud Messaging
- **Maps**: Google Maps SDK
- **Image Loading**: Coil
- **Dependency Injection**: Koin

## Setup Instructions

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/rentabols.git
   ```

2. Set up Firebase:
   - Go to the [Firebase Console](https://console.firebase.google.com/)
   - Create a new project
   - Add an Android app with package name `com.mhez_dev.rentabols_v1`
   - Download the `google-services.json` file
   - Replace the placeholder `app/google-services.json` file with your downloaded file

3. Set up Google Maps:
   - Go to the [Google Cloud Console](https://console.cloud.google.com/)
   - Enable the Maps SDK for Android
   - Create an API key with the necessary restrictions
   - Replace the placeholder API key in `secrets.properties` with your key

> **Note:** The repository includes placeholder files for `google-services.json` and `secrets.properties`. You must replace these with your own files containing valid credentials before running the app.

4. Build and run the project using Android Studio

## Project Structure

```
app/
├── src/
│   └── main/
│       ├── java/com/mhez_dev/rentabols_v1/
│       │   ├── data/
│       │   │   └── repository/       # Repository implementations
│       │   ├── di/                   # Dependency injection
│       │   ├── domain/
│       │   │   ├── model/           # Domain models
│       │   │   ├── repository/      # Repository interfaces
│       │   │   └── usecase/         # Use cases
│       │   ├── presentation/
│       │   │   ├── auth/           # Authentication screens
│       │   │   ├── home/           # Home screen
│       │   │   ├── items/          # Item-related screens
│       │   │   ├── map/            # Map screen
│       │   │   └── profile/        # Profile screen
│       │   └── ui/
│       │       └── components/     # Reusable UI components
│       └── res/                    # Resources
└── build.gradle.kts               # App-level build config
```

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details
