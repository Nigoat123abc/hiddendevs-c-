import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;
import java.io.File;

/**
 * System Monitor Application - Displays real-time CPU, RAM, and Disk usage
 * Uses Java Swing for GUI and JMX for system metrics
 */
public class SystemMonitor extends JFrame {
    // UI Components - declared as instance variables for access across methods
    private JProgressBar cpuBar;      // Progress bar to visualize CPU usage
    private JProgressBar ramBar;      // Progress bar to visualize RAM usage
    private JProgressBar diskBar;     // Progress bar to visualize Disk usage
    private JLabel cpuLabel;          // Label to display CPU percentage text
    private JLabel ramLabel;          // Label to display RAM usage in MB
    private JLabel diskLabel;         // Label to display Disk usage in GB
    
    // System monitoring objects - used to fetch real-time metrics
    private OperatingSystemMXBean osBean;  // JMX bean for OS-level metrics
    private Runtime runtime;               // Runtime object for memory info
    private Timer updateTimer;             // Timer to refresh metrics periodically
    
    // Animation variables for smooth transitions
    private float currentCpuValue = 0;     // Current animated CPU value
    private float targetCpuValue = 0;      // Target CPU value to animate towards
    private float currentRamValue = 0;     // Current animated RAM value
    private float targetRamValue = 0;      // Target RAM value to animate towards
    
    /**
     * Constructor - Initializes the UI and starts monitoring
     */
    public SystemMonitor() {
        // Initialize system monitoring objects
        osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        runtime = Runtime.getRuntime();
        
        // Setup the main window
        setupWindow();
        
        // Create and layout all UI components
        createComponents();
        
        // Start the timer that updates metrics every 500ms
        startMonitoring();
    }
    
    /**
     * Configures the main window properties
     */
    private void setupWindow() {
        setTitle("System Monitor");                    // Set window title
        setSize(600, 500);                            // Set window dimensions
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Exit app when window closes
        setLocationRelativeTo(null);                   // Center window on screen
        setResizable(false);                           // Prevent window resizing
        
        // Set modern look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Creates all UI components and adds them to the window
     */
    private void createComponents() {
        // Main panel with padding and background color
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));  // Vertical layout
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40)); // Add padding
        mainPanel.setBackground(new Color(240, 240, 245));  // Light gray background
        
        // Title label with custom font and color
        JLabel titleLabel = new JLabel("SYSTEM MONITOR");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));  // Large bold font
        titleLabel.setForeground(new Color(0, 102, 204));      // Blue color
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);   // Center align
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 30))); // Add spacing
        
        // Create CPU monitoring section
        mainPanel.add(createMetricPanel("CPU Usage", Color.GREEN));
        cpuBar = createProgressBar(Color.GREEN);  // Green progress bar for CPU
        cpuLabel = new JLabel("0.0%");            // Initialize label
        mainPanel.add(createBarPanel(cpuBar, cpuLabel));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 25))); // Spacing between sections
        
        // Create RAM monitoring section
        mainPanel.add(createMetricPanel("RAM Usage", new Color(255, 165, 0)));
        ramBar = createProgressBar(new Color(255, 165, 0));  // Orange progress bar for RAM
        ramLabel = new JLabel("0 MB / 0 MB");                // Initialize label
        mainPanel.add(createBarPanel(ramBar, ramLabel));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 25))); // Spacing
        
        // Create Disk monitoring section
        mainPanel.add(createMetricPanel("Disk Usage (C:)", new Color(220, 20, 60)));
        diskBar = createProgressBar(new Color(220, 20, 60));  // Red progress bar for Disk
        diskLabel = new JLabel("0 GB / 0 GB");                // Initialize label
        mainPanel.add(createBarPanel(diskBar, diskLabel));
        
        // Add main panel to window
        add(mainPanel);
    }
    
    /**
     * Creates a label panel for metric titles
     * @param text The metric name to display
     * @param color The color theme for this metric
     * @return JPanel containing the styled label
     */
    private JPanel createMetricPanel(String text, Color color) {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));  // Left-aligned layout
        panel.setBackground(new Color(240, 240, 245));     // Match main background
        panel.setMaximumSize(new Dimension(520, 30));      // Fixed height
        
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 16));   // Bold font for titles
        label.setForeground(Color.BLACK);                   // Black text
        panel.add(label);
        
        return panel;
    }
    
    /**
     * Creates a styled progress bar with custom appearance
     * @param color The color for the progress bar fill
     * @return Configured JProgressBar
     */
    private JProgressBar createProgressBar(Color color) {
        JProgressBar bar = new JProgressBar(0, 100);  // Range from 0 to 100
        bar.setStringPainted(false);                   // Don't show percentage text on bar
        bar.setPreferredSize(new Dimension(520, 25));  // Set bar dimensions
        bar.setForeground(color);                      // Set fill color
        bar.setBackground(Color.WHITE);                // Set background color
        bar.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1)); // Add border
        return bar;
    }
    
    /**
     * Creates a panel containing a progress bar and its value label
     * @param bar The progress bar to display
     * @param label The label showing the numeric value
     * @return JPanel containing both components
     */
    private JPanel createBarPanel(JProgressBar bar, JLabel label) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());               // Border layout for positioning
        panel.setBackground(new Color(240, 240, 245));     // Match main background
        panel.setMaximumSize(new Dimension(520, 50));      // Fixed size
        
        panel.add(bar, BorderLayout.CENTER);               // Progress bar in center
        
        // Label panel on the right side
        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        labelPanel.setBackground(new Color(240, 240, 245));
        label.setFont(new Font("Arial", Font.PLAIN, 14)); // Regular font for values
        labelPanel.add(label);
        panel.add(labelPanel, BorderLayout.SOUTH);         // Label below bar
        
        return panel;
    }
    
    /**
     * Starts the monitoring timer that updates metrics periodically
     */
    private void startMonitoring() {
        // Create timer that fires every 500ms (twice per second)
        updateTimer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateMetrics();  // Update all system metrics
            }
        });
        updateTimer.start();  // Start the timer
        
        // Also create animation timer for smooth transitions (60 FPS)
        Timer animationTimer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                animateValues();  // Smoothly animate progress bars
            }
        });
        animationTimer.start();
    }
    
    /**
     * Fetches current system metrics and updates target values
     */
    private void updateMetrics() {
        // Get CPU usage as percentage (0.0 to 1.0, multiply by 100 for percentage)
        double cpuUsage = osBean.getSystemCpuLoad() * 100;
        if (cpuUsage < 0) cpuUsage = 0;  // Handle invalid values
        targetCpuValue = (float) cpuUsage;  // Set target for animation
        
        // Get RAM usage in megabytes
        long totalMemory = osBean.getTotalPhysicalMemorySize();  // Total RAM in bytes
        long freeMemory = osBean.getFreePhysicalMemorySize();    // Free RAM in bytes
        long usedMemory = totalMemory - freeMemory;              // Calculate used RAM
        
        // Convert bytes to megabytes (divide by 1024 * 1024)
        long usedMB = usedMemory / (1024 * 1024);
        long totalMB = totalMemory / (1024 * 1024);
        
        // Calculate RAM usage percentage
        double ramPercentage = ((double) usedMemory / totalMemory) * 100;
        targetRamValue = (float) ramPercentage;  // Set target for animation
        
        // Update RAM label with current values
        ramLabel.setText(usedMB + " MB / " + totalMB + " MB (" + 
                        String.format("%.1f", ramPercentage) + "%)");
        
        // Get Disk usage for C: drive
        File cDrive = new File("C:\\");
        long totalSpace = cDrive.getTotalSpace();   // Total disk space in bytes
        long freeSpace = cDrive.getFreeSpace();     // Free disk space in bytes
        long usedSpace = totalSpace - freeSpace;    // Calculate used space
        
        // Convert bytes to gigabytes (divide by 1024^3)
        long usedGB = usedSpace / (1024L * 1024L * 1024L);
        long totalGB = totalSpace / (1024L * 1024L * 1024L);
        
        // Calculate disk usage percentage
        double diskPercentage = ((double) usedSpace / totalSpace) * 100;
        
        // Update disk progress bar and label
        diskBar.setValue((int) diskPercentage);
        diskLabel.setText(usedGB + " GB / " + totalGB + " GB (" + 
                         String.format("%.1f", diskPercentage) + "%)");
    }
    
    /**
     * Smoothly animates progress bar values towards their targets
     * Creates smooth transitions instead of instant jumps
     */
    private void animateValues() {
        // Smoothly interpolate CPU value towards target (ease-out effect)
        float cpuDiff = targetCpuValue - currentCpuValue;
        currentCpuValue += cpuDiff * 0.1f;  // Move 10% closer each frame
        cpuBar.setValue((int) currentCpuValue);
        cpuLabel.setText(String.format("%.1f", currentCpuValue) + "%");
        
        // Smoothly interpolate RAM value towards target
        float ramDiff = targetRamValue - currentRamValue;
        currentRamValue += ramDiff * 0.1f;  // Move 10% closer each frame
        ramBar.setValue((int) currentRamValue);
    }
    
    /**
     * Main method - Entry point of the application
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        // Use SwingUtilities to ensure thread safety with Swing components
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Create and display the system monitor window
                SystemMonitor monitor = new SystemMonitor();
                monitor.setVisible(true);  // Make window visible
            }
        });
    }
}
