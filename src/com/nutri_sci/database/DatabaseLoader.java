package com.nutri_sci.database;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

/**
 * Utility class for setting up and populating the NutriSci nutrition database.
 * Refactored to use extracted methods for better maintainability and reduced complexity.
 */
public class DatabaseLoader {

    /** Database connection URL without database name for initial setup */
    private static final String DB_URL = "jdbc:mysql://localhost/";
    
    /** Target database name to create and populate */
    private static final String DB_NAME = "nutrisci_db";
    
    /** Database username for connection */
    private static final String USER = "root";
    
    /** Database password for connection */
    private static final String PASS = "root";
    
    /** File path to the directory containing CSV data files */
    private static final String CSV_FILE_PATH = "route to csv files";

    /** Batch size for SQL insert operations */
    private static final int BATCH_SIZE = 1000;

    /** Regex pattern to handle commas inside quotes in CSV parsing */
    private static final String CSV_SPLIT_REGEX = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";

    /**
     * Main method for executing the database setup and data loading process.
     */
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

    /**
     * Creates all necessary database tables for the nutrition data.
     */
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

    /**
     * Loads data from a CSV file into the specified database table.
     * Refactored to use extracted methods for better maintainability.
     * 
     * @param stmt SQL statement for database operations
     * @param fileName name of the CSV file to load
     * @param tableName name of the target database table
     * @param numColumns number of columns in the table
     */
    private static void loadData(Statement stmt, String fileName, String tableName, int numColumns) {
        String csvFile = CSV_FILE_PATH + fileName;
        int recordCount = 0;

        System.out.println("Loading " + fileName + " into " + tableName + "...");

        try (BufferedReader br = createCsvReader(csvFile)) {
            skipHeaderLine(br);
            recordCount = processDataRows(stmt, br, tableName, numColumns);
            System.out.println("Successfully loaded " + recordCount + " records into " + tableName + ".");
        } catch (Exception e) {
            System.err.println("An error occurred while loading " + fileName);
            e.printStackTrace();
        }
    }

    /**
     * Creates a BufferedReader for reading CSV files with proper encoding.
     * Extracted method to improve readability and handle encoding concerns.
     */
    private static BufferedReader createCsvReader(String csvFile) throws Exception {
        return new BufferedReader(new InputStreamReader(new FileInputStream(csvFile), "latin1"));
    }

    /**
     * Skips the header line in the CSV file.
     * Extracted method to make the purpose explicit.
     */
    private static void skipHeaderLine(BufferedReader br) throws Exception {
        br.readLine(); // Skip header line
    }

    /**
     * Processes all data rows from the CSV file and inserts them into the database.
     * Extracted method to separate data processing logic from file handling.
     * 
     * @param stmt SQL statement for database operations
     * @param br BufferedReader for reading CSV data
     * @param tableName name of the target database table
     * @param numColumns number of columns in the table
     * @return number of records successfully processed
     */
    private static int processDataRows(Statement stmt, BufferedReader br, String tableName, int numColumns) throws Exception {
        String line;
        int recordCount = 0;

        while ((line = br.readLine()) != null) {
            if (shouldSkipLine(line)) {
                continue;
            }

            String[] data = parseCsvLine(line);
            
            if (shouldSkipRecord(data)) {
                continue;
            }

            String sql = buildInsertStatement(tableName, data, numColumns);
            if (executeBatchInsert(stmt, sql)) {
                recordCount++;
            }
        }

        // Execute any remaining batched statements
        executeRemainingBatch(stmt);
        return recordCount;
    }

    /**
     * Determines if a line should be skipped (empty or malformed).
     * Extracted method to make line validation logic explicit.
     */
    private static boolean shouldSkipLine(String line) {
        return line.trim().isEmpty() || line.trim().equals(",");
    }

    /**
     * Parses a CSV line into an array of values, handling commas within quotes.
     * Extracted method to encapsulate CSV parsing logic.
     */
    private static String[] parseCsvLine(String line) {
        return line.split(CSV_SPLIT_REGEX, -1);
    }

    /**
     * Determines if a record should be skipped based on primary key validation.
     * Extracted method to make record validation logic explicit.
     */
    private static boolean shouldSkipRecord(String[] data) {
        return data.length == 0 || 
               data[0].trim().isEmpty() || 
               data[0].trim().equalsIgnoreCase("null");
    }

    /**
     * Builds an INSERT statement for the given table and data.
     * Extracted method to separate SQL generation from execution logic.
     * 
     * @param tableName name of the target table
     * @param data array of data values
     * @param numColumns number of columns to insert
     * @return formatted INSERT SQL statement
     */
    private static String buildInsertStatement(String tableName, String[] data, int numColumns) {
        StringBuilder sql = new StringBuilder("INSERT IGNORE INTO " + tableName + " VALUES (");

        for (int i = 0; i < numColumns; i++) {
            String value = getColumnValue(data, i);
            sql.append(formatSqlValue(value));

            if (i < numColumns - 1) {
                sql.append(", ");
            }
        }
        sql.append(")");

        return sql.toString();
    }

    /**
     * Gets the value for a specific column, handling cases where data array is shorter than expected.
     * Extracted method to handle data array bounds checking.
     */
    private static String getColumnValue(String[] data, int columnIndex) {
        String value = (columnIndex < data.length) ? data[columnIndex].trim() : "";
        return cleanColumnValue(value);
    }

    /**
     * Cleans a column value by removing surrounding quotes and escaping SQL characters.
     * Extracted method to encapsulate data cleaning logic.
     */
    private static String cleanColumnValue(String value) {
        // Clean up quotes safely
        if (value.length() > 1 && value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        }
        // Escape single quotes for SQL
        value = value.replace("'", "''");
        return value;
    }

    /**
     * Formats a value for SQL insertion, handling NULL values and numeric vs. string types.
     * Extracted method to encapsulate SQL value formatting logic.
     */
    private static String formatSqlValue(String value) {
        if (value.isEmpty() || value.equalsIgnoreCase("null")) {
            return "NULL";
        } else if (isNumeric(value)) {
            return value;
        } else {
            return "'" + value + "'";
        }
    }

    /**
     * Executes a batch insert operation, managing batch size and error handling.
     * Extracted method to separate batch execution logic.
     * 
     * @param stmt SQL statement for batch operations
     * @param sql INSERT statement to add to batch
     * @return true if the statement was successfully added to batch
     */
    private static boolean executeBatchInsert(Statement stmt, String sql) {
        try {
            stmt.addBatch(sql);
            
            // Execute batch when it reaches the defined size
            if (getBatchCount(stmt) % BATCH_SIZE == 0) {
                stmt.executeBatch();
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Batch execution error on SQL: " + sql);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Simple counter to track batch operations.
     * In a real implementation, this might use statement metadata or a separate counter.
     */
    private static int batchCounter = 0;
    
    private static int getBatchCount(Statement stmt) {
        return ++batchCounter;
    }

    /**
     * Executes any remaining statements in the batch.
     * Extracted method to handle final batch execution.
     */
    private static void executeRemainingBatch(Statement stmt) {
        try {
            stmt.executeBatch();
        } catch (SQLException e) {
            System.err.println("Error executing remaining batch statements");
            e.printStackTrace();
        }
    }

    /**
     * Checks if a string represents a numeric value.
     * Helper method for data type validation.
     */
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
