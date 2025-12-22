import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

public class GolayEncoderUI extends JFrame {
    private JTextArea resultArea;
    private JLabel imagePathLabel;
    private File selectedImageFile;
    private String encodedVector;
    private String receivedVector;

    public GolayEncoderUI() {
        setTitle("Golay Encoder");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 700);

        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Vector Processing Panel
        JPanel vectorPanel = createVectorPanel();

        // Text Processing Panel
        JPanel textPanel = createTextPanel();

        // Image Processing Panel
        JPanel imagePanel = createImagePanel();

        // Result Panel
        JPanel resultPanel = createResultPanel();

        // Add panels to main panel
        JPanel inputPanelRow1 = new JPanel(new GridLayout(1, 2, 10, 10));
        inputPanelRow1.add(vectorPanel);
        inputPanelRow1.add(textPanel);

        JPanel inputPanelRow2 = new JPanel(new GridLayout(1, 1, 10, 10));
        inputPanelRow2.add(imagePanel);

        JPanel inputPanelRows = new JPanel(new GridLayout(2, 1, 10, 10));
        inputPanelRows.add(inputPanelRow1);
        inputPanelRows.add(inputPanelRow2);

        mainPanel.add(inputPanelRows, BorderLayout.NORTH);
        mainPanel.add(resultPanel, BorderLayout.CENTER);

        add(mainPanel);
    }

    private JPanel createVectorPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new TitledBorder("Vector Processing (12 bit length)"));

        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Vector input
        JTextField vectorField = addTextToPanel(inputPanel, gbc, "Vector:", "111000111000", 1);

        // Error rate
        JTextField errorField = addTextToPanel(inputPanel, gbc, "Error Rate:", "0.07", 2);

        // Encoded vector input
        JTextField receivedVectorField = addTextToPanel(inputPanel, gbc, "Received Vector:", "", 3);
        receivedVectorField.setEditable(false);

        panel.add(inputPanel, BorderLayout.CENTER);

        // Process button
        JButton processTextBtn = new JButton("Process");
        processTextBtn.addActionListener(e -> processVector(vectorField, receivedVectorField, errorField));
        panel.add(processTextBtn, BorderLayout.EAST);

        return panel;
    }

    private JPanel createTextPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new TitledBorder("Text Processing"));

        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Text input
        JTextField textField = addTextToPanel(inputPanel, gbc, "Text:", "Šokoladas ir bananas 🤕", 1);

        // Error rate
        JTextField errorField = addTextToPanel(inputPanel, gbc, "Error Rate:", "0.07", 2);

        panel.add(inputPanel, BorderLayout.CENTER);

        // Process button
        JButton processTextBtn = new JButton("Process");
        processTextBtn.addActionListener(e -> processText(textField, errorField));
        panel.add(processTextBtn, BorderLayout.EAST);

        return panel;
    }

    private JPanel createImagePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new TitledBorder("Image Processing"));

        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Image selection
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        inputPanel.add(new JLabel("Image:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        imagePathLabel = new JLabel("No image selected");
        imagePathLabel.setBorder(BorderFactory.createEtchedBorder());
        inputPanel.add(imagePathLabel, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        JButton selectImageBtn = new JButton("Browse...");
        selectImageBtn.addActionListener(this::selectImage);
        inputPanel.add(selectImageBtn, gbc);

        // Output path
        JTextField outputPath = addTextToPanel(inputPanel, gbc, "Output Path:", "images/result.bmp", 2);
        JTextField outputCorruptedPath = addTextToPanel(inputPanel, gbc, "Output Path (not fixed):", "images/result_corrupted.bmp", 3);

        // Error rate
        JTextField errorField = addTextToPanel(inputPanel, gbc, "Error Rate:", "0.07", 4);

        panel.add(inputPanel, BorderLayout.CENTER);

        // Process button
        JButton processImageBtn = new JButton("Process");
        processImageBtn.addActionListener(e -> processImage(outputPath, outputCorruptedPath, errorField));
        panel.add(processImageBtn, BorderLayout.EAST);

        return panel;
    }

    private JTextField addTextToPanel(JPanel panel, GridBagConstraints gbc, String title, String placeholderText, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel(title), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        JTextField field = new JTextField(placeholderText);
        panel.add(field, gbc);

        return field;
    }

    private JPanel createResultPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Results"));

        resultArea = new JTextArea(15, 60);
        resultArea.setEditable(false);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(resultArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton clearBtn = new JButton("Clear");
        clearBtn.addActionListener(e -> resultArea.setText(""));
        panel.add(clearBtn, BorderLayout.SOUTH);

        return panel;
    }

    private void selectImage(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileFilter() {
            public boolean accept(File file) {
                return file.isDirectory() || file.getName().toLowerCase().endsWith(".bmp");
            }
            public String getDescription() {
                return "Bitmap Images (*.bmp)";
            }
        });

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedImageFile = fileChooser.getSelectedFile();
            imagePathLabel.setText(selectedImageFile.getName());
        }
    }

    /**
     * Handles Vector Processing inputs on button press.
     */
    private void processVector(JTextField vectorInput, JTextField receivedVectorField, JTextField errorField) {
        int length = 12;
        try {
            int[] vector = getVectorFromString(vectorInput.getText(), length);

            double errorRate = Double.parseDouble(errorField.getText());
            if (errorRate < 0 || errorRate > 1) {
                showError("Error rate must be between 0 and 1");
                return;
            }

            // if vector has not been sent, send and show received value.
            if (receivedVectorField.getText().isEmpty()) {
                encodedVector = getEncodedVector(vector);
                receivedVector = sendAndReceiveVector(toIntArray(encodedVector), errorRate);
                receivedVectorField.setText(receivedVector);

                receivedVectorField.setEditable(true);
            }
            // if vector was received, let the user append the value and decode it.
            else {
                receivedVector = receivedVectorField.getText();
                length = 23;
                int[] receivedVector = getVectorFromString(receivedVectorField.getText(), length);
                String decodedVector = getDecodedVector(receivedVector);

                processVectorResult(vectorInput.getText(), decodedVector, errorRate);

                receivedVectorField.setText("");
                receivedVectorField.setEditable(false);
            }
        } catch (NumberFormatException e) {
            showError("Invalid vector. Please enter only 1 or 0 values of length " + length + ".");
        } catch (Exception ex) {
            showError("Error: " + ex.getMessage());
        }
    }

    private void processVectorResult(String inputVector, String decodedVector, double errorRate) {
        resultArea.append("=== Processing Vector ===\n");
        resultArea.append("Input: " + inputVector + "\n");
        resultArea.append("Error Rate: " + errorRate + "\n\n");

        resultArea.append(String.format("%-20s","Encoded vector: ") + encodedVector + "\n");
        resultArea.append(String.format("%-20s","Received vector: ") + receivedVector + "\n");
        resultArea.append(String.format("%-20s","Errors at: ") + getErrorsVector(encodedVector, receivedVector) + "\n");
        resultArea.append(String.format("%-20s","Decoded vector: ") + decodedVector + "\n");
        resultArea.append("=".repeat(50) + "\n\n");
    }

    private String getErrorsVector(String encodedVector, String receivedVector) {
        StringBuilder errorsVector = new StringBuilder();
        for (int i = 0; i < encodedVector.length(); i++) {
            errorsVector.append(encodedVector.charAt(i) != receivedVector.charAt(i) ? "1" : "0");
        }
        return errorsVector.toString();
    }

    private void processText(JTextField textInput, JTextField errorField) {
        String text = textInput.getText();
        if (text.isEmpty()) {
            showError("Please enter text to process");
            return;
        }

        try {
            double errorRate = Double.parseDouble(errorField.getText());
            if (errorRate < 0 || errorRate > 1) {
                showError("Error rate must be between 0 and 1");
                return;
            }

            resultArea.append("=== Processing Text ===\n");
            resultArea.append("Input: " + text + "\n");
            resultArea.append("Error Rate: " + errorRate + "\n\n");

            String decodedText = sendText(errorRate, textInput, true);
            String decodedCorruptedText = sendText(errorRate, textInput, false);

            resultArea.append("Output: " + decodedText + "\n");
            resultArea.append("Output (not fixed): " + decodedCorruptedText + "\n");
            resultArea.append("=".repeat(50) + "\n\n");

        } catch (NumberFormatException ex) {
            showError("Invalid error rate. Please enter a decimal number.");
        } catch (Exception ex) {
            showError("Error processing text: " + ex.getMessage());
        }
    }

    private void processImage(JTextField outputPath, JTextField outputCorruptedPath, JTextField errorField) {
        if (selectedImageFile == null) {
            showError("Please select an image file");
            return;
        }

        try {
            double errorRate = Double.parseDouble(errorField.getText());
            if (errorRate < 0 || errorRate > 1) {
                showError("Error rate must be between 0 and 1");
                return;
            }

            resultArea.append("=== Processing Image ===\n");
            resultArea.append("Input: " + selectedImageFile.getAbsolutePath() + "\n");
            resultArea.append("Error Rate: " + errorRate + "\n");

            // Process with Golay encoder
            String resultPath = sendImage(errorRate, outputPath, true);
            String resultCorruptedPath = sendImage(errorRate, outputCorruptedPath, false);

            resultArea.append("Output: " + resultPath + "\n");
            resultArea.append("Output (not fixed): " + resultCorruptedPath + "\n");
            resultArea.append("=".repeat(50) + "\n\n");

        } catch (NumberFormatException ex) {
            showError("Invalid numeric input. Please check error rate.");
        } catch (IOException ex) {
            showError("Error processing image: " + ex.getMessage());
        } catch (Exception ex) {
            showError("Error: " + ex.getMessage());
        }
    }

    private String getEncodedVector(int[] vector) {
        GolayEncoder encoder = new GolayEncoder();

        int[] encodedVector = encoder.encode(vector);
        return encoder.toString(encodedVector);
    }

    private String sendAndReceiveVector(int[] encodedVector, double errorRate) {
        GolayEncoder encoder = new GolayEncoder();
        Channel channel = new Channel(errorRate);

        int[][] encodedVectors = {encodedVector};
        channel.send(encodedVectors, 0);

        return encoder.toString(channel.receiveVector());
    }

    private String getDecodedVector(int[] receivedVector) {
        GolayEncoder encoder = new GolayEncoder();
        return encoder.toString(encoder.decode(receivedVector));
    }

    private String sendText(double errorRate, JTextField textInput, boolean fixErrors) {
        GolayEncoder encoder = new GolayEncoder();
        encoder.setFixErrors(fixErrors);
        Channel channel = new Channel(errorRate);

        int[][] encodedText = encoder.encode(textInput.getText());
        channel.send(encodedText, encoder.getOverflow());

        int[][] receivedData = channel.receive();
        int safeData = channel.receiveSafeData();

        return encoder.decode(receivedData, safeData);
    }

    private String sendImage(double errorRate, JTextField outputPathLabel, boolean fixErrors) throws IOException {
        GolayEncoder encoder = new GolayEncoder();
        encoder.setFixErrors(fixErrors);
        Channel channel = new Channel(errorRate);

        int[][] encodedImg = encoder.encodeFile(selectedImageFile.getAbsolutePath());
        int headerSize = 16;
        channel.send(encodedImg, encoder.getOverflow(), headerSize);

        int[][] receivedData = channel.receive();
        int safeData = channel.receiveSafeData();

        String outputPath = outputPathLabel.getText();
        encoder.decodeFile(receivedData, outputPath, safeData);
        return outputPath;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private int[] toIntArray(String string) {
        int[] array = new int[string.length()];
        for (int i = 0; i < string.length(); i++) {
            array[i] = Integer.parseInt(String.valueOf(string.charAt(i)));
        }
        return array;
    }

    private int[] getVectorFromString(String vectorString, int length) {
        int[] vector = new int[length];
        for (int i = 0; i < vector.length; i++) {
            String bit = String.valueOf(vectorString.toCharArray()[i]);
            int parsedInt = Integer.parseInt(bit);
            if ((parsedInt != 1 && parsedInt != 0) || vectorString.length() != length) {
                throw new NumberFormatException();
            }
            vector[i] = parsedInt;
        }
        return vector;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.out.println();
            }

            GolayEncoderUI frame = new GolayEncoderUI();
            frame.setVisible(true);
        });
    }
}