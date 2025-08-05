package com.nutri_sci.database;

import com.nutri_sci.model.UserProfile;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Repository for managing user profile data persistence.
 * Extracted from DBManager to follow Single Responsibility Principle.
 */
public class UserProfileRepository {
    private final Connection connection;

    public UserProfileRepository(Connection connection) {
        this.connection = connection;
    }

    public UserProfile saveProfile(UserProfile profile) {
        String sql = "INSERT INTO USER_PROFILE (ProfileName, Sex, DateOfBirth, HeightCM, WeightKG, MeasurementUnit) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, profile.getName());
            pstmt.setString(2, profile.getSex());
            pstmt.setDate(3, new java.sql.Date(profile.getDateOfBirth().getTime()));
            pstmt.setDouble(4, profile.getHeight());
            pstmt.setDouble(5, profile.getWeight());
            pstmt.setString(6, profile.getMeasurementUnit());
            pstmt.executeUpdate();

            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                profile.setId(generatedKeys.getInt(1));
            }
            return profile;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public UserProfile getProfile(String profileName) {
        String sql = "SELECT * FROM USER_PROFILE WHERE ProfileName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, profileName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                UserProfile profile = new UserProfile();
                profile.setId(rs.getInt("UserID"));
                profile.setName(rs.getString("ProfileName"));
                profile.setSex(rs.getString("Sex"));
                profile.setDateOfBirth(rs.getDate("DateOfBirth"));
                profile.setHeight(rs.getDouble("HeightCM"));
                profile.setWeight(rs.getDouble("WeightKG"));
                profile.setMeasurementUnit(rs.getString("MeasurementUnit"));
                return profile;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Set<String> getAllUserNames() {
        Set<String> userNames = new HashSet<>();
        String sql = "SELECT ProfileName FROM USER_PROFILE";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                userNames.add(rs.getString("ProfileName"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userNames;
    }

    public boolean updateProfile(UserProfile profile) {
        String sql = "UPDATE USER_PROFILE SET ProfileName = ?, Sex = ?, DateOfBirth = ?, HeightCM = ?, WeightKG = ?, MeasurementUnit = ? WHERE UserID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, profile.getName());
            pstmt.setString(2, profile.getSex());
            pstmt.setDate(3, new java.sql.Date(profile.getDateOfBirth().getTime()));
            pstmt.setDouble(4, profile.getHeight());
            pstmt.setDouble(5, profile.getWeight());
            pstmt.setString(6, profile.getMeasurementUnit());
            pstmt.setInt(7, profile.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
} 