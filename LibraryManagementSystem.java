import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class LibraryManagementSystem extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/LibraryDB";
    private static final String USER = "root"; 
    private static final String PASS = "Rizwan@123";
    public LibraryManagementSystem() {
        setTitle("Library Management System");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel titleLabel = new JLabel("Library Management System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        JButton addBookButton = new JButton("Add Book");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(addBookButton, gbc);

        JButton searchBookButton = new JButton("Search Book");
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(searchBookButton, gbc);

        JButton displayBooksButton = new JButton("Display Books");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        panel.add(displayBooksButton, gbc);

        JButton deleteBookButton = new JButton("Delete Book");
        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(deleteBookButton, gbc);

        add(panel);
        setVisible(true);

        // Add action listeners for the buttons
        addBookButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addBook();
            }
        });

        searchBookButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchBook();
            }
        });

        displayBooksButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayBooks();
            }
        });

        deleteBookButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteBook();
            }
        });
    }

    private void addBook() {
    String bookTitle = JOptionPane.showInputDialog(this, "Enter Book Title:");
    if (bookTitle != null && !bookTitle.trim().isEmpty()) {
        String getNextIdQuery = "SELECT COALESCE(MAX(id), 0) + 1 AS nextId FROM Books";
        String insertBookQuery = "INSERT INTO Books (id, title) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(getNextIdQuery)) {

            if (rs.next()) {
                int nextId = rs.getInt("nextId");

                try (PreparedStatement pstmt = conn.prepareStatement(insertBookQuery)) {
                    pstmt.setInt(1, nextId);
                    pstmt.setString(2, bookTitle);
                    pstmt.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Book added successfully!");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding book: " + e.getMessage());
        }
    } else {
        JOptionPane.showMessageDialog(this, "Book title cannot be empty!");
    }
}
private void deleteBook() {
    String bookIdStr = JOptionPane.showInputDialog(this, "Enter Book ID to Delete:");
    if (bookIdStr != null && !bookIdStr.trim().isEmpty()) {
        try {
            int bookId = Integer.parseInt(bookIdStr);
            String deleteBookQuery = "DELETE FROM Books WHERE id = ?";
            String decrementIdsQuery = "UPDATE Books SET id = id - 1 WHERE id > ?";

            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                 PreparedStatement deleteStmt = conn.prepareStatement(deleteBookQuery);
                 PreparedStatement decrementStmt = conn.prepareStatement(decrementIdsQuery)) {

                deleteStmt.setInt(1, bookId);
                int rowsAffected = deleteStmt.executeUpdate();

                if (rowsAffected > 0) {
                    decrementStmt.setInt(1, bookId);
                    decrementStmt.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Book deleted successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "Book not found!");
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid Book ID!");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting book: " + e.getMessage());
        }
    } else {
        JOptionPane.showMessageDialog(this, "Book ID cannot be empty!");
    }
}


    private void searchBook() {
        String bookTitle = JOptionPane.showInputDialog(this, "Enter Book Title to Search:");
        if (bookTitle != null && !bookTitle.trim().isEmpty()) {
            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                 PreparedStatement stmt = conn.prepareStatement("SELECT title FROM Books WHERE title = ?")) {
                stmt.setString(1, bookTitle.trim());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "Book found: " + rs.getString("title"));
                } else {
                    JOptionPane.showMessageDialog(this, "Book not found!");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error searching book: " + e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Search title cannot be empty!");
        }
    }

    private void displayBooks() {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT title FROM Books")) {
            if (!rs.isBeforeFirst()) {
                JOptionPane.showMessageDialog(this, "No books available!");
                return;
            }

            StringBuilder books = new StringBuilder("Books in the library:\n");
            while (rs.next()) {
                books.append(rs.getString("title")).append("\n");
            }
            JOptionPane.showMessageDialog(this, books.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error displaying books: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LibraryManagementSystem();
            }
        });
    }
}
