package com.nutri_sci.database;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

public class DatabaseLoader {

    private static final String DB_URL = "jdbc:mysql://localhost/";
    private static final String DB_NAME = "nutrisci_db";
    private static final String USER = "root";
    private static final String PASS = "root";
    private static final String CSV_FILE_PATH = "route to csv files";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             Statement stmt = conn.createStatement()) {

            System.out.println("Connecting to database...");

            // Create database
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            System.out.println("Database created successfully...");

            // Select database
            stmt.executeUpdate("USE " + DB_NAME);

            // Create tables
            createTables(stmt);

            // Load data
            loadAllData(stmt);

            System.out.println("Database setup and data loading complete.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createTables(Statement stmt) throws SQLException {
        System.out.println("Creating tables...");

        // Support Tables
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS FOOD_GROUP (" +
                "FoodGroupID BIGINT PRIMARY KEY," +
                "FoodGroupCode BIGINT," +
                "FoodGroupName VARCHAR(255)," +
                "FoodGroupNameF VARCHAR(255))");

        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS FOOD_SOURCE (" +
                "FoodSourceID BIGINT PRIMARY KEY," +
                "FoodSourceCode BIGINT," +
                "FoodSourceDescription VARCHAR(255)," +
                "FoodSourceDescriptionF VARCHAR(255))");

        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS NUTRIENT_NAME (" +
                "NutrientID BIGINT PRIMARY KEY," +
                "NutrientCode BIGINT," +
                "NutrientSymbol VARCHAR(255)," +
                "NutrientUnit VARCHAR(255)," +
                "NutrientName VARCHAR(255)," +
                "NutrientNameF VARCHAR(255)," +
                "Tagname VARCHAR(255)," +
                "NutrientDecimals BIGINT)");

        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS NUTRIENT_SOURCE (" +
                "NutrientSourceID BIGINT PRIMARY KEY," +
                "NutrientSourceCode BIGINT," +
                "NutrientSourceDescription VARCHAR(255)," +
                "NutrientSourceDescriptionF VARCHAR(255))");

        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS MEASURE_NAME (" +
                "MeasureID BIGINT PRIMARY KEY," +
                "MeasureDescription VARCHAR(255)," +
                "MeasureDescriptionF VARCHAR(255))");

        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS REFUSE_NAME (" +
                "RefuseID DOUBLE PRIMARY KEY," +
                "RefuseDescription VARCHAR(255)," +
                "RefuseDescriptionF VARCHAR(255))");

        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS YIELD_NAME (" +
                "YieldID DOUBLE PRIMARY KEY," +
                "YieldDescription VARCHAR(255)," +
                "YieldDescriptionF VARCHAR(255))");

        // Principal Tables
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS FOOD_NAME (" +
                "FoodID BIGINT PRIMARY KEY," +
                "FoodCode BIGINT," +
                "FoodGroupID BIGINT," +
                "FoodSourceID BIGINT," +
                "FoodDescription TEXT," +
                "FoodDescriptionF TEXT," +
                "FoodDateOfEntry VARCHAR(255)," +
                "FoodDateOfPublication VARCHAR(255)," +
                "CountryCode DOUBLE," +
                "ScientificName VARCHAR(255))");

        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS NUTRIENT_AMOUNT (" +
                "FoodID BIGINT," +
                "NutrientID BIGINT," +
                "NutrientValue DOUBLE," +
                "StandardError DOUBLE," +
                "NumberOfObservations DOUBLE," +
                "NutrientSourceID BIGINT," +
                "NutrientDateOfEntry VARCHAR(255)," +
                "PRIMARY KEY (FoodID, NutrientID))");

        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS CONVERSION_FACTOR (" +
                "FoodID BIGINT," +
                "MeasureID BIGINT," +
                "ConversionFactorValue DOUBLE," +
                "ConvFactorDateOfEntry VARCHAR(255)," +
                "PRIMARY KEY (FoodID, MeasureID))");

        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS REFUSE_AMOUNT (" +
                "FoodID BIGINT," +
                "RefuseID BIGINT," +
                "RefuseAmount BIGINT," +
                "RefuseDateOfEntry VARCHAR(255)," +
                "PRIMARY KEY (FoodID, RefuseID))");

        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS YIELD_AMOUNT (" +
                "FoodID BIGINT," +
                "YieldID BIGINT," +
                "YieldAmount BIGINT," +
                "YieldDateOfEntry VARCHAR(255)," +
                "PRIMARY KEY (FoodID, YieldID))");

        System.out.println("Tables created successfully.");
    }

    private static void loadAllData(Statement stmt) {
        System.out.println("Loading data from CSV files...");
        loadData(stmt, "FOOD GROUP.csv", "FOOD_GROUP", 4);
        loadData(stmt, "FOOD SOURCE.csv", "FOOD_SOURCE", 4);
        loadData(stmt, "NUTRIENT NAME.csv", "NUTRIENT_NAME", 8);
        loadData(stmt, "NUTRIENT SOURCE.csv", "NUTRIENT_SOURCE", 4);
        loadData(stmt, "MEASURE NAME.csv", "MEASURE_NAME", 3);
        loadData(stmt, "REFUSE NAME.csv", "REFUSE_NAME", 3);
        loadData(stmt, "YIELD NAME.csv", "YIELD_NAME", 3);
        loadData(stmt, "FOOD NAME.csv", "FOOD_NAME", 10);
        loadData(stmt, "NUTRIENT AMOUNT.csv", "NUTRIENT_AMOUNT", 7);
        loadData(stmt, "CONVERSION FACTOR.csv", "CONVERSION_FACTOR", 4);
        loadData(stmt, "REFUSE AMOUNT.csv", "REFUSE_AMOUNT", 4);
        loadData(stmt, "YIELD AMOUNT.csv", "YIELD_AMOUNT", 4);
        System.out.println("Data loading complete.");
    }

    private static void loadData(Statement stmt, String fileName, String tableName, int numColumns) {
        String csvFile = CSV_FILE_PATH + fileName;
        String line = "";
        String cvsSplitBy = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"; // Regex to handle commas inside quotes
        int batchSize = 1000;
        int count = 0;

        System.out.println("Loading " + fileName + " into " + tableName + "...");

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(csvFile), "latin1"))) {
            br.readLine(); // Skip header line

            while ((line = br.readLine()) != null) {
                // Skip empty or malformed lines
                if (line.trim().isEmpty() || line.trim().equals(",")) {
                    continue;
                }

                String[] data = line.split(cvsSplitBy, -1);

                // Skip row if the primary key is empty
                if (data[0].trim().isEmpty() || data[0].trim().equalsIgnoreCase("null")) {
                    continue;
                }

                // Use INSERT IGNORE to skip duplicate primary key errors
                StringBuilder sql = new StringBuilder("INSERT IGNORE INTO " + tableName + " VALUES (");

                for (int i = 0; i < numColumns; i++) {
                    // Check if data array has this index
                    String value = (i < data.length) ? data[i].trim() : "";

                    // Clean up quotes safely
                    if (value.length() > 1 && value.startsWith("\"") && value.endsWith("\"")) {
                        value = value.substring(1, value.length() - 1);
                    }
                    // Escape single quotes for SQL
                    value = value.replace("'", "''");

                    if (value.isEmpty() || value.equalsIgnoreCase("null")) {
                        sql.append("NULL");
                    } else if (isNumeric(value)) {
                        sql.append(value);
                    } else {
                        sql.append("'").append(value).append("'");
                    }

                    if (i < numColumns - 1) {
                        sql.append(", ");
                    }
                }
                sql.append(")");

                try {
                    stmt.addBatch(sql.toString());
                    count++;
                    if (count % batchSize == 0) {
                        stmt.executeBatch();
                    }
                } catch (SQLException e) {
                    System.err.println("Batch execution error on SQL: " + sql.toString());
                    e.printStackTrace();
                }
            }
            stmt.executeBatch(); // Insert remaining records
            System.out.println("Successfully loaded " + count + " records into " + tableName + ".");
        } catch (Exception e) {
            System.err.println("An error occurred while loading " + fileName);
            e.printStackTrace();
        }
    }

    // A simple check to see if a string is numeric
    private static boolean isNumeric(String str) {
        if (str == null) {
            return false;
        }
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
