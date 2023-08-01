package com.example.demo;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class User extends BaseEntity {
    public enum Role {
        JOB_SEEKER,
        EMPLOYER
    }

    private int userId;
    private String name;
    private String email;
    private String password;
    private Role role;

    public User(int userId, String name, String email, String password, Role role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public User()
    {
    	
    }
    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    
    
    public static User buildUser(String name, String email, String password, Role role) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/job_search", "root", "Root");
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO User (Name, Email, Password, Role) VALUES (?, ?, ?, ?)",
                     PreparedStatement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, password);
            statement.setString(4, role.toString().toLowerCase());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                System.out.println("Failed to create user. No rows affected.");
                return null;
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int userId = generatedKeys.getInt(1);
                    return new User(userId, name, email, password, role);
                } else {
                    System.out.println("Failed to create user. No ID obtained.");
                    return null;
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to create user. Error: " + e.getMessage());
            return null;
        }
    }
    

 
    public void updateUser(String name, String email, String password, Role role) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/job_search", "root", "Root");
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE User SET Name = ?, Email = ?, Password = ?, Role = ? WHERE UserID = ?")) {

            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, password);
            statement.setString(4, role.toString().toLowerCase());
            statement.setInt(5, userId);

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                System.out.println("Failed to update user. No rows affected.");
            } else {
                this.name = name;
                this.email = email;
                this.password = password;
                this.role = role;
                System.out.println("User updated successfully.");
            }
        } catch (SQLException e) {
            System.out.println("Failed to update user. Error: " + e.getMessage());
        }
    }
    
    
    public static void deleteUser(int userId) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/job_portal", "root", "root");
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM User WHERE UserID = ?")) {

            statement.setInt(1, userId);

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                System.out.println("Failed to delete user. No rows affected.");
            } else {
                System.out.println("User deleted successfully.");
            }
        } catch (SQLException e) {
            System.out.println("Failed to delete user. Error: " + e.getMessage());
        }
    }
    
    
    public static List<User> getEmployer() {
        List<User> employers = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/job_search", "root", "Root");
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "SELECT * FROM User WHERE UserID IN (SELECT UserID FROM User WHERE Role = 'employer')")) {

            while (resultSet.next()) {
                int userId = resultSet.getInt("UserID");
                String name = resultSet.getString("Name");
                String email = resultSet.getString("Email");
                String password = resultSet.getString("Password");
                Role role = Role.valueOf(resultSet.getString("Role").toUpperCase());

                employers.add(new User(userId, name, email, password, role));
            }
        } catch (SQLException e) {
            System.out.println("Failed to fetch employer users. Error: " + e.getMessage());
        }

        return employers;
    }
    
  
    public void viewUserApplications() {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/job_search", "root", "Root");
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT u.*, a.ApplicationID, a.Status FROM User u LEFT JOIN Application a ON u.UserID = a.UserID WHERE u.UserID = ?")) {

            statement.setInt(1, this.userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                System.out.println("User Information:");
                System.out.println("User ID: " + this.userId);
                System.out.println("Name: " + this.name);
                System.out.println("Email: " + this.email);
                System.out.println("Role: " + this.role);
                System.out.println("\nApplications for User ID: " + this.userId);
                while (resultSet.next()) {
                    int applicationId = resultSet.getInt("ApplicationID");
                    int userId = resultSet.getInt("UserID");
                    int jobId = resultSet.getInt("JobID");
                    String status = resultSet.getString("Status");

                    System.out.println("Application ID: " + applicationId);
                    System.out.println("Job ID: " + jobId);
                    System.out.println("Status: " + status);
                    System.out.println();
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to fetch user applications. Error: " + e.getMessage());
        }
    }
    

    public static User getUserById(int userId) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/job_search", "root", "Root");
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT * FROM User WHERE UserID = ?")) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String name = resultSet.getString("Name");
                    String email = resultSet.getString("Email");
                    String password = resultSet.getString("Password");
                    String roleString = resultSet.getString("Role");
                    Role role = Role.valueOf(roleString.toUpperCase());

                    return new User(userId, name, email, password, role);
                } else {
                    System.out.println("User with UserID " + userId + " not found.");
                    return null;
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to fetch user information. Error: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public int getNumberOfRecords() {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/job_search", "root", "Root");
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT COUNT(*) FROM User")) {

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to fetch user count. Error: " + e.getMessage());
        }
        return 0; // Return 0 in case of an error or no records found
    }
}