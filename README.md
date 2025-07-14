# NutriSci: SwEATch to Better!

NutriSci is a desktop application developed in Java that helps users track their daily nutrient intake and make smarter dietary choices. Users can log their meals, analyze the nutritional content based on the Canadian Nutrient File (CNF), and receive intelligent food swap recommendations to achieve their health goals. The application also provides visualizations to help users understand their dietary patterns and align them with Canada's Food Guide.

## Features

* **Profile Management**: Create and manage personal profiles with details like sex, date of birth, height, and weight.
* **Meal Logging**: Log daily meals, including breakfast, lunch, dinner, and snacks, by specifying ingredients and quantities.
* **Nutrient Analysis**: Automatically calculates the nutritional value of meals (calories, proteins, carbs, etc.) using the comprehensive Canadian Nutrient File (CNF) database.
* **Smart Food Swaps**: Get recommendations for food item replacements to meet specific nutritional goals, such as increasing fiber or reducing calorie intake.
* **Data Visualization**: Visualize nutrient intake and dietary patterns over time using various charts and graphs, powered by the JFreeChart library.
* **Canada Food Guide Alignment**: Compare your dietary habits with the recommendations from the Canada Food Guide.

## Technology Stack

* **Language**: Java
* **Framework**: Java Swing for the Graphical User Interface (GUI).
* **Database**: MySQL for data storage.
* **Visualization**: JFreeChart library for creating charts and graphs.

### Libraries Used

* **JFreeChart 1.5.3** – For generating charts and graphs.
* **JCommon 1.0.24** – A supporting library required by JFreeChart.
* **MySQL Connector/J 8.0.31** – For connecting and interacting with the MySQL database.

## Setup and Installation

### Prerequisites

* Java Development Kit (JDK) 8 or higher.
* MySQL Server.
* The Canadian Nutrient File (CNF) dataset provided as CSV files.

### 1. Database Setup

The application requires a MySQL database named `nutrisci_db` to be set up and populated with data from the CNF. A utility class, `DatabaseLoader.java`, is provided to automate this process.

1.  **Configure Database Connection**:
    * Open the `src/com/nutri_sci/database/DatabaseLoader.java` file.
    * Modify the `CSV_FILE_PATH` variable to point to the directory where your CNF `.csv` files are stored.
    * If your MySQL credentials are not the default (`root`/`root`), update the `USER` and `PASS` variables in both `DatabaseLoader.java` and `DBManager.java`.

2.  **Run the Database Loader**:
    * Execute the `main` method in the `DatabaseLoader.java` class.
    * This script will create the `nutrisci_db` database, set up the required tables, and load all the necessary data from the CSV files.

### 2. Running the Application

Once the database is set up, you can run the application.

1.  **Locate the Main Class**: The main entry point for the application is `src/com/nutri_sci/App.java`.
2.  **Run the App**: Execute the `main` method in the `App.java` class. (main components such as meal logging or profile creation also have their own independent main function for individual testing)
3.  **Get Started**: The application will start with a splash screen, where you can either create a new user profile or load an existing one to begin.

## Project Structure

The source code is organized into the following packages:

* `com.nutri_sci`: The root package, containing the main application entry point (`App.java`).
* `com.nutri_sci.ui`: Contains all GUI-related classes built with Java Swing (e.g., `SplashScreenUI`, `MealLoggingUI`).
* `com.nutri_sci.controller`: Includes controller classes that handle the application's business logic and mediate between the UI and the data models (e.g., `ProfileController`, `MealController`).
* `com.nutri_sci.service`: Contains service classes for specific functionalities like nutrient calculation (`NutrientCalculator`), finding food swaps (`SwapEngine`), and managing UI updates with the Observer pattern (`MealDataNotifier`).
* `com.nutri_sci.model`: Defines the data model classes that represent the core entities of the application (e.g., `UserProfile`, `Meal`).
* `com.nutri_sci.database`: Manages all database interactions, including the connection manager (`DBManager`) and the initial data loader (`DatabaseLoader`).
