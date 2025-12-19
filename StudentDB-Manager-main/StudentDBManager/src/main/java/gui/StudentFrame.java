package gui;

import model.Student;
import dao.studentDAO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class StudentFrame extends JFrame {
    private JTextField txtFirstName, txtLastName, txtAge, txtEmail, txtSearch;
    private JButton btnAdd, btnView, btnSearch;
    private JTextArea txtStatus;
    private JTable table;
    private DefaultTableModel tableModel;
    private studentDAO studentDAO;
    
    public StudentFrame() {
        studentDAO = new studentDAO();
        initComponents();
        setTitle("StudentDB Manager");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Input Panel
        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Student Information"));
        
        inputPanel.add(new JLabel("First Name:"));
        txtFirstName = new JTextField();
        inputPanel.add(txtFirstName);
        
        inputPanel.add(new JLabel("Last Name:"));
        txtLastName = new JTextField();
        inputPanel.add(txtLastName);
        
        inputPanel.add(new JLabel("Age:"));
        txtAge = new JTextField();
        inputPanel.add(txtAge);
        
        inputPanel.add(new JLabel("Email:"));
        txtEmail = new JTextField();
        inputPanel.add(txtEmail);
        
        inputPanel.add(new JLabel("Search by ID:"));
        txtSearch = new JTextField();
        inputPanel.add(txtSearch);
        
        mainPanel.add(inputPanel, BorderLayout.NORTH);
        
        // Center Panel
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        
        // Table
        String[] columns = {"ID", "First Name", "Last Name", "Age", "Email"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        
        // Status
        txtStatus = new JTextArea(3, 50);
        txtStatus.setEditable(false);
        txtStatus.setBorder(BorderFactory.createTitledBorder("Status Messages"));
        
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(new JScrollPane(txtStatus), BorderLayout.SOUTH);
        
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        btnAdd = new JButton("Add Student");
        btnView = new JButton("View All Students");
        btnSearch = new JButton("Search Student");
        
        // Add action listeners with SwingWorker (Threading)
        btnAdd.addActionListener(e -> addStudentWithThread());
        btnView.addActionListener(e -> viewStudentsWithThread());
        btnSearch.addActionListener(e -> searchStudent());
        
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnView);
        buttonPanel.add(btnSearch);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    // ===== THREADING METHODS =====
    
    private void addStudentWithThread() {
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                updateStatus("Adding student...");
                
                String firstName = txtFirstName.getText().trim();
                String lastName = txtLastName.getText().trim();
                String ageText = txtAge.getText().trim();
                String email = txtEmail.getText().trim();
                
                if (firstName.isEmpty() || lastName.isEmpty() || ageText.isEmpty() || email.isEmpty()) {
                    updateStatus("Error: All fields required!");
                    return false;
                }
                
                try {
                    int age = Integer.parseInt(ageText);
                    Student student = new Student(firstName, lastName, age, email);
                    Thread.sleep(1000); // Simulate DB delay
                    return studentDAO.addStudent(student);
                } catch (NumberFormatException e) {
                    updateStatus("Error: Age must be a number!");
                    return false;
                }
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        updateStatus("Student added successfully!");
                        clearFields();
                        viewStudentsWithThread();
                    } else {
                        updateStatus("Failed to add student!");
                    }
                } catch (Exception e) {
                    updateStatus("Error: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    private void viewStudentsWithThread() {
        SwingWorker<List<Student>, Void> worker = new SwingWorker<List<Student>, Void>() {
            @Override
            protected List<Student> doInBackground() throws Exception {
                updateStatus("Loading students...");
                Thread.sleep(800); // Simulate loading delay
                return studentDAO.getAllStudents();
            }
            
            @Override
            protected void done() {
                try {
                    List<Student> students = get();
                    updateTable(students);
                    updateStatus("Loaded " + students.size() + " student(s)");
                } catch (Exception e) {
                    updateStatus("Error loading students: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    private void searchStudent() {
        try {
            String searchText = txtSearch.getText().trim();
            if (searchText.isEmpty()) {
                updateStatus("Please enter ID to search!");
                return;
            }
            
            int id = Integer.parseInt(searchText);
            updateStatus("Searching for ID: " + id + "...");
            
            Student student = studentDAO.searchStudentById(id);
            
            if (student != null) {
                tableModel.setRowCount(0);
                Object[] row = {student.getId(), student.getFirstName(), 
                              student.getLastName(), student.getAge(), student.getEmail()};
                tableModel.addRow(row);
                updateStatus("Student found!");
            } else {
                updateStatus("No student with ID: " + id);
            }
            
        } catch (NumberFormatException e) {
            updateStatus("Error: ID must be a number!");
        }
    }
    
    // Helper methods
    private void updateTable(List<Student> students) {
        tableModel.setRowCount(0);
        for (Student student : students) {
            Object[] row = {student.getId(), student.getFirstName(), 
                          student.getLastName(), student.getAge(), student.getEmail()};
            tableModel.addRow(row);
        }
    }
    
    private void updateStatus(String message) {
        SwingUtilities.invokeLater(() -> {
            txtStatus.setText(message);
        });
    }
    
    private void clearFields() {
        txtFirstName.setText("");
        txtLastName.setText("");
        txtAge.setText("");
        txtEmail.setText("");
        txtSearch.setText("");
    }
}