import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * TodoList Application - A modern task management system
 * Features: Add tasks, mark complete, delete, persistent storage
 * Uses Java Swing for GUI with custom styling and animations
 */
public class TodoList extends JFrame {
    // Data structure to hold all tasks
    private List<Task> tasks;  // ArrayList to store Task objects
    
    // UI Components
    private JPanel tasksPanel;        // Panel that contains all task items
    private JTextField inputField;    // Text field for entering new tasks
    private JButton addButton;        // Button to add new tasks
    private JScrollPane scrollPane;   // Scrollable container for tasks
    private JLabel counterLabel;      // Label showing task count
    
    // File path for saving/loading tasks
    private static final String SAVE_FILE = "tasks.txt";
    
    // Color scheme for modern UI
    private static final Color PRIMARY_COLOR = new Color(0, 120, 215);      // Blue
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 250); // Light gray
    private static final Color TASK_BG_COLOR = Color.WHITE;                 // White
    private static final Color COMPLETED_COLOR = new Color(200, 200, 200);  // Gray
    
    /**
     * Inner class representing a single task
     * Encapsulates task data and behavior
     */
    private class Task {
        private String text;        // The task description
        private boolean completed;  // Whether task is done
        
        /**
         * Constructor for creating a new task
         * @param text The task description
         */
        public Task(String text) {
            this.text = text;
            this.completed = false;  // New tasks start as incomplete
        }
        
        /**
         * Gets the task text
         * @return The task description
         */
        public String getText() {
            return text;
        }
        
        /**
         * Checks if task is completed
         * @return true if completed, false otherwise
         */
        public boolean isCompleted() {
            return completed;
        }
        
        /**
         * Toggles the completion status
         */
        public void toggleComplete() {
            completed = !completed;  // Flip the boolean value
        }
        
        /**
         * Sets completion status explicitly
         * @param completed The new completion status
         */
        public void setCompleted(boolean completed) {
            this.completed = completed;
        }
    }
    
    /**
     * Constructor - Initializes the application
     */
    public TodoList() {
        // Initialize the tasks list
        tasks = new ArrayList<>();
        
        // Setup window properties
        setupWindow();
        
        // Create all UI components
        createComponents();
        
        // Load previously saved tasks
        loadTasks();
        
        // Display all loaded tasks
        refreshTasksDisplay();
    }
    
    /**
     * Configures the main window properties
     */
    private void setupWindow() {
        setTitle("Todo List");                          // Set window title
        setSize(500, 700);                             // Set window dimensions
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Exit on close
        setLocationRelativeTo(null);                    // Center on screen
        setResizable(false);                            // Fixed size window
        
        // Set modern look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Creates and arranges all UI components
     */
    private void createComponents() {
        // Main container panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Top panel with title and input
        JPanel topPanel = createTopPanel();
        mainPanel.add(topPanel, BorderLayout.NORTH);
        
        // Center panel with scrollable task list
        JPanel centerPanel = createCenterPanel();
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Bottom panel with statistics
        JPanel bottomPanel = createBottomPanel();
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        // Add main panel to frame
        add(mainPanel);
    }
    
    /**
     * Creates the top panel containing title and input field
     * @return JPanel with title and input components
     */
    private JPanel createTopPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BACKGROUND_COLOR);
        
        // Title label
        JLabel titleLabel = new JLabel("📝 My Tasks");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Input panel for adding new tasks
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(BACKGROUND_COLOR);
        inputPanel.setMaximumSize(new Dimension(460, 45));
        
        // Text field for task input
        inputField = new JTextField();
        inputField.setFont(new Font("Arial", Font.PLAIN, 16));
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        
        // Add placeholder text behavior
        inputField.setForeground(Color.GRAY);
        inputField.setText("Enter a new task...");
        inputField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                // Clear placeholder when focused
                if (inputField.getText().equals("Enter a new task...")) {
                    inputField.setText("");
                    inputField.setForeground(Color.BLACK);
                }
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                // Restore placeholder if empty
                if (inputField.getText().isEmpty()) {
                    inputField.setForeground(Color.GRAY);
                    inputField.setText("Enter a new task...");
                }
            }
        });
        
        // Allow Enter key to add task
        inputField.addActionListener(e -> addTask());
        
        // Add button with custom styling
        addButton = new JButton("Add");
        addButton.setFont(new Font("Arial", Font.BOLD, 16));
        addButton.setBackground(PRIMARY_COLOR);
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.setBorderPainted(false);
        addButton.setPreferredSize(new Dimension(80, 45));
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        addButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                addButton.setBackground(PRIMARY_COLOR.darker());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                addButton.setBackground(PRIMARY_COLOR);
            }
        });
        
        // Add click handler
        addButton.addActionListener(e -> addTask());
        
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(addButton, BorderLayout.EAST);
        
        panel.add(inputPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        return panel;
    }
    
    /**
     * Creates the center panel with scrollable task list
     * @return JPanel containing the tasks display
     */
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        
        // Panel to hold all task items
        tasksPanel = new JPanel();
        tasksPanel.setLayout(new BoxLayout(tasksPanel, BoxLayout.Y_AXIS));
        tasksPanel.setBackground(BACKGROUND_COLOR);
        
        // Scrollable container
        scrollPane = new JScrollPane(tasksPanel);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);  // Smooth scrolling
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Creates the bottom panel with task counter
     * @return JPanel with statistics
     */
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        counterLabel = new JLabel("0 tasks");
        counterLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        counterLabel.setForeground(Color.GRAY);
        panel.add(counterLabel);
        
        return panel;
    }
    
    /**
     * Adds a new task to the list
     * Validates input and updates display
     */
    private void addTask() {
        String taskText = inputField.getText().trim();  // Get text and remove whitespace
        
        // Validate input - don't add empty or placeholder text
        if (taskText.isEmpty() || taskText.equals("Enter a new task...")) {
            return;  // Exit method if invalid
        }
        
        // Create new task object and add to list
        Task newTask = new Task(taskText);
        tasks.add(newTask);
        
        // Clear input field
        inputField.setText("");
        inputField.setForeground(Color.GRAY);
        inputField.setText("Enter a new task...");
        
        // Update display
        refreshTasksDisplay();
        
        // Save to file
        saveTasks();
        
        // Animate scroll to bottom to show new task
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }
    
    /**
     * Refreshes the entire task display
     * Clears and rebuilds all task UI components
     */
    private void refreshTasksDisplay() {
        // Remove all existing task components
        tasksPanel.removeAll();
        
        // Create UI component for each task
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            JPanel taskPanel = createTaskPanel(task, i);
            tasksPanel.add(taskPanel);
            tasksPanel.add(Box.createRigidArea(new Dimension(0, 10)));  // Spacing
        }
        
        // Update task counter
        updateCounter();
        
        // Refresh display
        tasksPanel.revalidate();
        tasksPanel.repaint();
    }
    
    /**
     * Creates a UI panel for a single task
     * @param task The task object to display
     * @param index The position of this task in the list
     * @return JPanel representing the task
     */
    private JPanel createTaskPanel(Task task, int index) {
        // Main task panel with rounded border
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(TASK_BG_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        panel.setMaximumSize(new Dimension(460, 70));
        
        // Checkbox for completion status
        JCheckBox checkBox = new JCheckBox();
        checkBox.setSelected(task.isCompleted());
        checkBox.setBackground(TASK_BG_COLOR);
        checkBox.setFocusPainted(false);
        checkBox.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Handle checkbox toggle
        checkBox.addActionListener(e -> {
            task.toggleComplete();
            refreshTasksDisplay();
            saveTasks();
        });
        
        // Task text label
        JLabel textLabel = new JLabel(task.getText());
        textLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        
        // Apply strikethrough if completed
        if (task.isCompleted()) {
            textLabel.setForeground(COMPLETED_COLOR);
            textLabel.setFont(new Font("Arial", Font.ITALIC, 16));
        } else {
            textLabel.setForeground(Color.BLACK);
        }
        
        // Delete button
        JButton deleteBtn = new JButton("🗑");
        deleteBtn.setFont(new Font("Arial", Font.PLAIN, 18));
        deleteBtn.setBackground(TASK_BG_COLOR);
        deleteBtn.setBorderPainted(false);
        deleteBtn.setFocusPainted(false);
        deleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteBtn.setPreferredSize(new Dimension(40, 40));
        
        // Delete button hover effect
        deleteBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                deleteBtn.setForeground(Color.RED);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                deleteBtn.setForeground(Color.BLACK);
            }
        });
        
        // Delete button click handler
        final int taskIndex = index;  // Capture index for lambda
        deleteBtn.addActionListener(e -> {
            tasks.remove(taskIndex);
            refreshTasksDisplay();
            saveTasks();
        });
        
        // Assemble components
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setBackground(TASK_BG_COLOR);
        leftPanel.add(checkBox);
        leftPanel.add(textLabel);
        
        panel.add(leftPanel, BorderLayout.CENTER);
        panel.add(deleteBtn, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * Updates the task counter label
     */
    private void updateCounter() {
        int total = tasks.size();
        int completed = 0;
        
        // Count completed tasks
        for (Task task : tasks) {
            if (task.isCompleted()) {
                completed++;
            }
        }
        
        // Update label text
        counterLabel.setText(completed + " / " + total + " tasks completed");
    }
    
    /**
     * Saves all tasks to a file
     * Format: each line is "completed|task text"
     */
    private void saveTasks() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SAVE_FILE))) {
            // Write each task as a line
            for (Task task : tasks) {
                // Format: "1|task text" or "0|task text"
                String line = (task.isCompleted() ? "1" : "0") + "|" + task.getText();
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            // Show error dialog if save fails
            JOptionPane.showMessageDialog(this, 
                "Failed to save tasks: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Loads tasks from file
     * Reads the save file and recreates task objects
     */
    private void loadTasks() {
        File file = new File(SAVE_FILE);
        
        // Check if file exists
        if (!file.exists()) {
            return;  // No saved tasks, start fresh
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            
            // Read each line from file
            while ((line = reader.readLine()) != null) {
                // Parse line format: "completed|task text"
                if (line.contains("|")) {
                    String[] parts = line.split("\\|", 2);  // Split into 2 parts
                    
                    if (parts.length == 2) {
                        boolean completed = parts[0].equals("1");  // Check completion status
                        String text = parts[1];                     // Get task text
                        
                        // Create task and add to list
                        Task task = new Task(text);
                        task.setCompleted(completed);
                        tasks.add(task);
                    }
                }
            }
        } catch (IOException e) {
            // Show error dialog if load fails
            JOptionPane.showMessageDialog(this,
                "Failed to load tasks: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Main method - Entry point of the application
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        // Ensure UI updates happen on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            TodoList app = new TodoList();
            app.setVisible(true);
        });
    }
}
