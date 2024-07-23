package databaseViewer;

import com.jtattoo.plaf.aluminium.AluminiumLookAndFeel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import javax.swing.*;

public class DatabaseViewer extends JFrame {
    private static final long serialVersionUID = -8393898832414986357L;
    private final JTextArea queryArea;
    private final JButton executeButton;
    private final JTextArea resultArea;

    private final JTextField idField;
    private final JTextField nameField;
    private final JTextField emailField;

    private final JButton createButton;
    private final JButton readButton;
    private final JButton updateButton;
    private final JButton deleteButton;

    public DatabaseViewer() {
        // Set modern look and feel
        try {
            UIManager.setLookAndFeel(new AluminiumLookAndFeel());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        setTitle("Database Viewer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        queryArea = new JTextArea(5, 40);
        queryArea.setBorder(BorderFactory.createTitledBorder("Enter your SQL query here"));

        executeButton = new JButton("Run SQL Query");
        executeButton.setPreferredSize(new Dimension(150, 40)); // Set size

        resultArea = new JTextArea(20, 40);
        resultArea.setEditable(false);
        resultArea.setBorder(BorderFactory.createTitledBorder("Output will be displayed here"));

        idField = new JTextField(15);
        nameField = new JTextField(15);
        emailField = new JTextField(15);

        setPlaceholder(idField, "Enter ID");
        setPlaceholder(nameField, "Enter Name");
        setPlaceholder(emailField, "Enter Email");

        createButton = new JButton("Create");
        readButton = new JButton("Read");
        updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");

        // Styling buttons
        JButton[] buttons = {createButton, readButton, updateButton, deleteButton};
        for (JButton button : buttons) {
            button.setPreferredSize(new Dimension(150, 40)); // Set size
            button.setBackground(new Color(255, 255, 224)); // Light yellow background
            button.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        }

        JPanel inputPanel = new JPanel(new GridLayout(4, 3, 10, 10));
        inputPanel.setBorder(BorderFactory.createTitledBorder("CRUD Operations"));

        inputPanel.add(new JLabel("ID:"));
        inputPanel.add(idField);
        inputPanel.add(createButton);

        inputPanel.add(new JLabel("Name:"));
        inputPanel.add(nameField);
        inputPanel.add(readButton);

        inputPanel.add(new JLabel("Email:"));
        inputPanel.add(emailField);
        inputPanel.add(updateButton);

        inputPanel.add(new JLabel());
        inputPanel.add(new JLabel());
        inputPanel.add(deleteButton);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JScrollPane(queryArea), BorderLayout.NORTH);
        panel.add(executeButton, BorderLayout.CENTER);
        panel.add(new JScrollPane(resultArea), BorderLayout.SOUTH);
        panel.add(inputPanel, BorderLayout.EAST);

        executeButton.addActionListener((ActionEvent e) -> executeQuery());
        createButton.addActionListener((ActionEvent e) -> createRecord());
        readButton.addActionListener((ActionEvent e) -> readRecord());
        updateButton.addActionListener((ActionEvent e) -> updateRecord());
        deleteButton.addActionListener((ActionEvent e) -> deleteRecord());

        add(panel);
        pack(); // Adjusts the window to fit its components
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Maximizes the window to fit screen size
    }

    private void setPlaceholder(JTextField field, String placeholder) {
        field.setText(placeholder);
        field.setForeground(Color.GRAY);
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }

            public void focusLost(java.awt.event.FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setForeground(Color.GRAY);
                    field.setText(placeholder);
                }
            }
        });
    }

    private void executeQuery() {
        String query = queryArea.getText();
        try (Connection connection = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/Test", "postgres", "123456");
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            StringBuilder resultBuilder = new StringBuilder();

            for (int i = 1; i <= columnCount; i++) {
                resultBuilder.append(metaData.getColumnName(i)).append("\t");
            }
            resultBuilder.append("\n");

            while (resultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    resultBuilder.append(resultSet.getString(i)).append("\t");
                }
                resultBuilder.append("\n");
            }

            resultArea.setText(resultBuilder.toString());

        } catch (SQLException e) {
            resultArea.setText("Error executing query: " + e.getMessage());
        }
    }

    private void createRecord() {
        String id = idField.getText();
        String name = nameField.getText();
        String email = emailField.getText();

        String query = "INSERT INTO test (id, name, email) VALUES (?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/Test", "postgres", "123456");
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, Integer.parseInt(id));
            preparedStatement.setString(2, name);
            preparedStatement.setString(3, email);
            int rowsAffected = preparedStatement.executeUpdate();

            resultArea.setText("Rows inserted: " + rowsAffected);

        } catch (SQLException e) {
            resultArea.setText("Error creating record: " + e.getMessage());
        }
    }

    private void readRecord() {
        String id = idField.getText();

        String query = "SELECT * FROM test WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/Test", "postgres", "123456");
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, Integer.parseInt(id));
            ResultSet resultSet = preparedStatement.executeQuery();

            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            StringBuilder resultBuilder = new StringBuilder();

            for (int i = 1; i <= columnCount; i++) {
                resultBuilder.append(metaData.getColumnName(i)).append("\t");
            }
            resultBuilder.append("\n");

            while (resultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    resultBuilder.append(resultSet.getString(i)).append("\t");
                }
                resultBuilder.append("\n");
            }

            resultArea.setText(resultBuilder.toString());

        } catch (SQLException e) {
            resultArea.setText("Error reading record: " + e.getMessage());
        }
    }

    private void updateRecord() {
        String id = idField.getText();
        String name = nameField.getText();
        String email = emailField.getText();

        String query = "UPDATE test SET name = ?, email = ? WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/Test", "postgres", "123456");
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, name);
            preparedStatement.setString(2, email);
            preparedStatement.setInt(3, Integer.parseInt(id));
            int rowsAffected = preparedStatement.executeUpdate();

            resultArea.setText("Rows updated: " + rowsAffected);

        } catch (SQLException e) {
            resultArea.setText("Error updating record: " + e.getMessage());
        }
    }

    private void deleteRecord() {
        String id = idField.getText();

        String query = "DELETE FROM test WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/Test", "postgres", "123456");
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, Integer.parseInt(id));
            int rowsAffected = preparedStatement.executeUpdate();

            resultArea.setText("Rows deleted: " + rowsAffected);

        } catch (SQLException e) {
            resultArea.setText("Error deleting record: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DatabaseViewer viewer = new DatabaseViewer();
            viewer.setVisible(true);
        });
    }
}
