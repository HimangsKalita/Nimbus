# Minimalist Weather App (Kotlin)

A simple, lightweight, and minimalist weather app developed for Android using Kotlin. This app provides users with essential weather information in a clean, easy-to-use interface.

## Features

- **Current weather display**: View the current temperature, weather conditions (sunny, cloudy, etc.), and humidity.
- **Real-time data**: Fetch weather data using a weather API.
- **Minimalist design**: Focus on providing only the most important weather details.
- **Search location**: Allows users to search for weather data for different cities.

## Tech Stack

- **Kotlin**: The primary programming language for building the app.
- **Android SDK**: For building the Android app.
- **OpenWeatherMap API**: For fetching real-time weather data (or any other weather API you prefer).
- **Retrofit**: For making network requests.
- **Glide**: For image loading (e.g., displaying weather icons).
- **MVVM architecture**: For clean code separation.

## Installation

1. Clone the repository:
    ```bash
    git clone https://github.com/your-username/minimalist-weather-app.git
    ```

2. Open the project in Android Studio.

3. Add your API key from [OpenWeatherMap](https://openweathermap.org/api) or another weather data provider in `strings.xml`:

    ```xml
    <string name="api_key">your_api_key_here</string>
    ```

4. Build and run the app on your Android device or emulator.

## Screenshots

![Home Screen](assets/screenshots/home_screen.png)  
*Minimalist weather display with current weather details.*

![Search Screen](assets/screenshots/search_screen.png)  
*Search for weather in other cities.*

## Contributing

Contributions are welcome! If you have any suggestions or improvements, feel free to open an issue or create a pull request.

### How to contribute:

1. Fork the repository.
2. Create a new branch (`git checkout -b feature/your-feature`).
3. Commit your changes (`git commit -m 'Add new feature'`).
4. Push to the branch (`git push origin feature/your-feature`).
5. Open a pull request.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgements

- Thanks to [OpenWeatherMap](https://openweathermap.org/) for providing the weather data.
- Android community for providing great resources and inspiration.


