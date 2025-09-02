# dietai
DietAI is a JavaFX diet analysis program that takes in a CSV file of data from the Perfect Diet Tracker (or other diet tracker with similiar CSV data file structure) and uses ai to provide an evaluation. 

[![Click for larger image](https://github.com/dean-703/dietai/blob/main/DietGuru600.png?raw=true)](https://github.com/dean-703/dietai/blob/main/DietGuru.png?raw=true)

The program does the following:

- Imports diet-tracker CSV files
- Displays the entries in a table
- Summarizes totals and trends (macros, daily calories)
- Uses AI (OpenAI) to provide an evaluation, with an offline fallback if no API key is set
- Includes error handling, background tasks, and good practices
- User profile (age, sex, weight, height, activity)
- Goal settings (lose/maintain/gain with weekly rate, protein/fat per kg, optional fiber and sodium targets)
- Calculator for BMR, TDEE, calorie target, and macro targets
- Profile & Targets settings dialog with validation and persistence
- Summary display comparing actual averages vs targets
- AI analysis payload includes profile and targets

CSV headers
- Date
- Item
- Quantity/Duration
- Calories
- Carbohydrates
- Protein
- Fat
- Sodium
- Fiber
- Meal
- Notes

Project structure:
- pom.xml (Maven build file)
- src/main/java/com/courtney/dietai/App.java
- src/main/java/com/courtney/dietai/model/DietEntry.java
- src/main/java/com/courtney/dietai/io/CsvImporter.java
- src/main/java/com/courtney/dietai/analysis/NutritionAnalyzer.java
- src/main/java/com/courtney/dietai/analysis/DietSummary.java
- src/main/java/com/courtney/dietai/ai/OpenAIService.java
- src/main/java/com/courtney/dietai/ai/SimpleHeuristicAnalyzer.java
- src/main/java/com/courtney/dietai/profile/UserProfile.java
- src/main/java/com/courtney/dietai/profile/GoalSettings.java
- src/main/java/com/courtney/dietai/profile/Targets.java
- src/main/java/com/courtney/dietai/profile/TargetCalculator.java
- src/main/java/com/courtney/dietai/profile/ProfileStore.java

