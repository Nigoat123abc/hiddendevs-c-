import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.imageio.ImageIO;

/**
 * Screenshot Tool - Captures screen and saves to custom location
 * Features: Full screen capture, custom save location, multiple formats
 * Uses Java AWT Robot class for screen capture
 */
public class ScreenshotTool extends JFrame {
    // UI Components
    private JButton captureButton;      // Button to trigger screenshot
    private JButton saveLocationButton; // Button to choose save location
    private JLabel statusLabel;         // Label showing current status
    private JLabel previewLabel;        // Label to show screenshot preview
    private JComboBox<String> formatBox; // Dropdown for image format selection
    
    // Screenshot data
    private BufferedImage currentScreenshot;  // Stores the captured image
    private File saveDirectory;               // Directory where screenshots are saved
    
    // Supported image formats
    private static final String[] FORMATS = {"PNG", "JPG", "BMP"};
    
    // Color scheme
    private static final Color PRIMARY_COLOR = new Color(0, 120, 215);
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 250);
    private static final Color SUCCESS_COLOR = new Color(34, 139, 34);
    
    /**
     * Constructor - Initializes the application
     */
    public ScreenshotTool() {
        // Set default save directory to user's home directory
        saveDirectory = new File(System.getProperty("user.home"));
        
        // Setup window
        setupWindow();
        
        // Create UI components
        createComponents();
    }
    
    /**
     * Configures the main window properties
     */
    private void setupWindow() {
        setTitle("Screenshot Tool");                    // Set window title
        setSize(600, 700);                             // Set window dimensions
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Exit on close
        setLocationRelativeTo(null);                    // Center on screen
        setResizable(false);                            // Fixed size
        
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
        // Main panel with padding
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        
        // Title label
        JLabel titleLabel = new JLabel("📸 Screenshot Tool");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        
        // Format selection panel
        mainPanel.add(createFormatPanel());
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Save location panel
        mainPanel.add(createSaveLocationPanel());
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Capture button
        captureButton = createStyledButton("Capture Screenshot", PRIMARY_COLOR);
        captureButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        captureButton.addActionListener(e -> captureScreenshot());
        mainPanel.add(captureButton);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Status label
        statusLabel = new JLabel("Ready to capture");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        statusLabel.setForeground(Color.GRAY);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(statusLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Preview panel
        mainPanel.add(createPreviewPanel());
        
        // Add main panel to frame
        add(mainPanel);
    }
    
    /**
     * Creates the format selection panel
     * @return JPanel with format dropdown
     */
    private JPanel createFormatPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setMaximumSize(new Dimension(520, 50));
        
        // Label for format selection
        JLabel label = new JLabel("Image Format:");
        label.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(label);
        
        // Dropdown for format selection
        formatBox = new JComboBox<>(FORMATS);
        formatBox.setFont(new Font("Arial", Font.PLAIN, 14));
        formatBox.setPreferredSize(new Dimension(100, 30));
        formatBox.setBackground(Color.WHITE);
        formatBox.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panel.add(formatBox);
        
        return panel;
    }
    
    /**
     * Creates the save location selection panel
     * @return JPanel with location button and label
     */
    private JPanel createSaveLocationPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setMaximumSize(new Dimension(520, 80));
        
        // Label showing current save location
        JLabel locationLabel = new JLabel("Save Location:");
        locationLabel.setFont(new Font("Arial", Font.BOLD, 16));
        locationLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(locationLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Panel for button and path display
        JPanel innerPanel = new JPanel(new BorderLayout(10, 0));
        innerPanel.setBackground(BACKGROUND_COLOR);
        innerPanel.setMaximumSize(new Dimension(520, 40));
        
        // Text field showing current directory (read-only)
        JTextField pathField = new JTextField(saveDirectory.getAbsolutePath());
        pathField.setEditable(false);
        pathField.setFont(new Font("Arial", Font.PLAIN, 12));
        pathField.setBackground(Color.WHITE);
        pathField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY, 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        // Button to change save location
        saveLocationButton = new JButton("Browse...");
        saveLocationButton.setFont(new Font("Arial", Font.PLAIN, 14));
        saveLocationButton.setBackground(Color.WHITE);
        saveLocationButton.setFocusPainted(false);
        saveLocationButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveLocationButton.setPreferredSize(new Dimension(100, 40));
        
        // Add click handler to open directory chooser
        saveLocationButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);  // Only directories
            chooser.setCurrentDirectory(saveDirectory);  // Start at current location
            
            // Show dialog and handle selection
            int result = chooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                saveDirectory = chooser.getSelectedFile();  // Update save directory
                pathField.setText(saveDirectory.getAbsolutePath());  // Update display
                statusLabel.setText("Save location updated");
                statusLabel.setForeground(SUCCESS_COLOR);
            }
        });
        
        innerPanel.add(pathField, BorderLayout.CENTER);
        innerPanel.add(saveLocationButton, BorderLayout.EAST);
        
        panel.add(innerPanel);
        
        return panel;
    }
    
    /**
     * Creates the preview panel for showing captured screenshots
     * @return JPanel with preview area
     */
    private JPanel createPreviewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setMaximumSize(new Dimension(520, 350));
        panel.setPreferredSize(new Dimension(520, 350));
        
        // Label for preview title
        JLabel titleLabel = new JLabel("Preview:");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Label to display screenshot preview
        previewLabel = new JLabel("No screenshot captured yet", SwingConstants.CENTER);
        previewLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        previewLabel.setForeground(Color.GRAY);
        previewLabel.setPreferredSize(new Dimension(520, 320));
        previewLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        previewLabel.setBackground(Color.WHITE);
        previewLabel.setOpaque(true);
        
        panel.add(previewLabel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Creates a styled button with hover effects
     * @param text Button text
     * @param color Button background color
     * @return Styled JButton
     */
    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(300, 50));
        button.setMaximumSize(new Dimension(300, 50));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.darker());  // Darken on hover
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);  // Restore original color
            }
        });
        
        return button;
    }
    
    /**
     * Captures the entire screen and saves it
     * Uses Robot class to capture screen pixels
     */
    private void captureScreenshot() {
        try {
            // Update status
            statusLabel.setText("Capturing screenshot...");
            statusLabel.setForeground(Color.ORANGE);
            
            // Hide this window temporarily for clean screenshot
            setVisible(false);
            
            // Wait a moment for window to hide
            Thread.sleep(200);
            
            // Create Robot instance for screen capture
            Robot robot = new Robot();
            
            // Get screen dimensions
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            
            // Capture the screen
            currentScreenshot = robot.createScreenCapture(screenRect);
            
            // Show window again
            setVisible(true);
            
            // Generate filename with timestamp
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String format = (String) formatBox.getSelectedItem();  // Get selected format
            String filename = "screenshot_" + timestamp + "." + format.toLowerCase();
            
            // Create file object
            File outputFile = new File(saveDirectory, filename);
            
            // Save the image to file
            ImageIO.write(currentScreenshot, format, outputFile);
            
            // Update status with success message
            statusLabel.setText("Screenshot saved: " + filename);
            statusLabel.setForeground(SUCCESS_COLOR);
            
            // Update preview with thumbnail
            updatePreview();
            
            // Show success dialog
            JOptionPane.showMessageDialog(this,
                "Screenshot saved successfully!\n" + outputFile.getAbsolutePath(),
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            
        } catch (AWTException e) {
            // Handle Robot creation failure
            statusLabel.setText("Failed to capture: Robot not supported");
            statusLabel.setForeground(Color.RED);
            JOptionPane.showMessageDialog(this,
                "Screen capture not supported on this system",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            // Handle file save failure
            statusLabel.setText("Failed to save screenshot");
            statusLabel.setForeground(Color.RED);
            JOptionPane.showMessageDialog(this,
                "Failed to save screenshot: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        } catch (InterruptedException e) {
            // Handle thread interruption
            Thread.currentThread().interrupt();
            statusLabel.setText("Capture interrupted");
            statusLabel.setForeground(Color.RED);
        }
    }
    
    /**
     * Updates the preview label with a thumbnail of the captured screenshot
     * Scales the image to fit the preview area
     */
    private void updatePreview() {
        if (currentScreenshot == null) {
            return;  // No screenshot to preview
        }
        
        // Calculate scaled dimensions to fit preview area (maintain aspect ratio)
        int previewWidth = 500;
        int previewHeight = 300;
        
        // Get original dimensions
        int originalWidth = currentScreenshot.getWidth();
        int originalHeight = currentScreenshot.getHeight();
        
        // Calculate scaling factor
        double scale = Math.min(
            (double) previewWidth / originalWidth,
            (double) previewHeight / originalHeight
        );
        
        // Calculate new dimensions
        int scaledWidth = (int) (originalWidth * scale);
        int scaledHeight = (int) (originalHeight * scale);
        
        // Create scaled image
        Image scaledImage = currentScreenshot.getScaledInstance(
            scaledWidth, scaledHeight, Image.SCALE_SMOOTH
        );
        
        // Update preview label with scaled image
        previewLabel.setIcon(new ImageIcon(scaledImage));
        previewLabel.setText("");  // Clear text
    }
    
    /**
     * Main method - Entry point of the application
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        // Ensure UI updates happen on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            ScreenshotTool tool = new ScreenshotTool();
            tool.setVisible(true);
        });
    }
}
