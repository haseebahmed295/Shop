package shop.fx.shop;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class Database {
    // Test usage
    public static void main(String[] args) {
        DatabaseManager db = null;

        for (int i = 0; i < 10; i++) {
            try {
                db = new DatabaseManager();

                // Add a product with an image path
                Product product = new Product("Phone " + i, "High-performance Phone wrwewwwfwewjfofoewfoeoheovovcowencejncwencoewncojewcnewcnewnvewnveoaoncownoewfowenewnvnvjnnv", 300, "PhoneStore", "C:/Users/Just/Pictures/Untitled.jpg");
                db.addProduct(product);
                System.out.println("Product added with ID: " + product.getId());
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
            } finally {
                if (db != null) {
                    try {
                        db.closeConnection();
                    } catch (SQLException e) {
                        System.err.println("Error closing connection: " + e.getMessage());
                    }
                }
            }
        }
    }
}

// Product class to encapsulate product data
class Product {
    private int id;
    private final String name;
    private final String description;
    private final double price;
    private final String seller;
    private final byte[] image;

    public Product(String name, String description, double price, String seller, String imagePath) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.seller = seller;
        this.image = imagePath != null ? loadImage(imagePath) : null;
    }

    // Load image from file path and convert to byte array
    private byte[] loadImage(String imagePath) {
        try {
            return Files.readAllBytes(Paths.get(imagePath));
        } catch (IOException e) {
            System.err.println("Error reading image file: " + imagePath + ". Error: " + e.getMessage());
            return null; // Fallback to no image
        }
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public String getSeller() {
        return seller;
    }

    public byte[] getImage() {
        return image;
    }
}

// User class to encapsulate user data
class User {
    private int id;
    private final String username;
    private final String email;
    private final String role;

    public User(String username, String email, String role) {
        this.username = username;
        this.email = email;
        this.role = role;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }
}

// DatabaseManager class to handle all database operations using standard JDBC
class DatabaseManager {
    Connection conn;
    private static final String DB_URL = "jdbc:sqlite:marketplace.db";

    public DatabaseManager() {
        try {
            // Explicitly load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(DB_URL);
            conn.setAutoCommit(true);
            initializeDatabase();
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found. Ensure sqlite-jdbc.jar is in the classpath.");
            throw new RuntimeException("Failed to load SQLite driver: " + e.getMessage(), e);
        } catch (SQLException e) {
            System.err.println("Error connecting to database: " + e.getMessage());
            throw new RuntimeException("Failed to connect to database", e);
        }
    }
    public boolean isAdmin(int userId) throws SQLException {
        String sql = "SELECT role FROM users WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getString("role").equals("Admin");
            }
        }
    }

    // Initialize database and create tables
    private void initializeDatabase() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Create products table with image BLOB
            stmt.execute("CREATE TABLE IF NOT EXISTS products (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "description TEXT," +
                    "price REAL NOT NULL," +
                    "seller TEXT NOT NULL," +
                    "image BLOB)");

            // Create users table with password
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT NOT NULL UNIQUE," +
                    "email TEXT NOT NULL," +
                    "role TEXT NOT NULL," +
                    "password TEXT NOT NULL)");
        }
    }

    // Add a new product
    public void addProduct(Product product) throws SQLException {
        checkConnection();
        String sql = "INSERT INTO products (name, description, price, seller, image) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, product.getName());
            pstmt.setString(2, product.getDescription());
            pstmt.setDouble(3, product.getPrice());
            pstmt.setString(4, product.getSeller());
            pstmt.setBytes(5, product.getImage());
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    product.setId(rs.getInt(1));
                }
            }
        }
    }

    // Remove a product by ID
    public void removeProduct(int id) throws SQLException {
        checkConnection();
        String sql = "DELETE FROM products WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    // Search products by name, description, or seller
    public List<Product> searchProducts(String query) throws SQLException {
        checkConnection();
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE name LIKE ? OR description LIKE ? OR seller LIKE ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + query + "%");
            pstmt.setString(2, "%" + query + "%");
            pstmt.setString(3, "%" + query + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Product product = new Product(
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getDouble("price"),
                            rs.getString("seller"),
                            null // Image path not stored in DB, only byte[]
                    );
                    product.setId(rs.getInt("id"));
                    // Set image directly from DB
                    try {
                        Field imageField = Product.class.getDeclaredField("image");
                        imageField.setAccessible(true);
                        imageField.set(product, rs.getBytes("image"));
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        System.err.println("Error setting image field: " + e.getMessage());
                    }
                    products.add(product);
                }
            }
        }
        return products;
    }

    // Get all products
    public List<Product> getAllProducts() throws SQLException {
        checkConnection();
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Product product = new Product(
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDouble("price"),
                        rs.getString("seller"),
                        null // Image path not stored in DB, only byte[]
                );
                product.setId(rs.getInt("id"));
                // Set image directly from DB
                try {
                    Field imageField = Product.class.getDeclaredField("image");
                    imageField.setAccessible(true);
                    imageField.set(product, rs.getBytes("image"));
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    System.err.println("Error setting image field: " + e.getMessage());
                }
                products.add(product);
            }
        }
        return products;
    }

    public List<User> getAllUsers() throws SQLException {
        checkConnection();
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                User user = new User(
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("role")
                );
                user.setId(rs.getInt("id"));
                users.add(user);
            }
        }
        return users;
    }

    // Add a new user with hashed password
    public void addUser(User user, String password) throws SQLException {
        checkConnection();
        String sql = "INSERT INTO users (username, email, role, password) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getRole());
            stmt.setString(4, hashPassword(password));
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setId(rs.getInt(1));
                }
            }
        }
    }

    // Authenticate user
    public User authenticate(String username, String password) throws SQLException {
        checkConnection();
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    if (storedPassword.equals(hashPassword(password))) {
                        User user = new User(
                                rs.getString("username"),
                                rs.getString("email"),
                                rs.getString("role")
                        );
                        user.setId(rs.getInt("id"));
                        return user;
                    }
                }
            }
        }
        return null;
    }

    // Update user details
    public void updateUser(int id, String username, String email, String role, String password) throws SQLException {
        checkConnection();
        String sql = "UPDATE users SET username = ?, email = ?, role = ?, password = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setString(3, role);
            pstmt.setString(4, hashPassword(password));
            pstmt.setInt(5, id);
            pstmt.executeUpdate();
        }
    }

    // Remove a user by ID
    public void removeUser(int id) throws SQLException {
        checkConnection();
        String sql = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    // Search users by username or email
    public List<User> searchUsers(String query) throws SQLException {
        checkConnection();
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE username LIKE ? OR email LIKE ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + query + "%");
            pstmt.setString(2, "%" + query + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    User user = new User(
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getString("role")
                    );
                    user.setId(rs.getInt("id"));
                    users.add(user);
                }
            }
        }
        return users;
    }

    // Close database connection
    public void closeConnection() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
            conn = null;
        }
    }

    // Helper method to check connection
    private void checkConnection() throws SQLException {
        if (conn == null || conn.isClosed()) {
            throw new SQLException("Database connection is not initialized or has been closed");
        }
    }

    // Hash password using SHA-256
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Password hashing failed", e);
        }
    }
}