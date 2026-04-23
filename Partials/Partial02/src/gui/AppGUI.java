package gui;

import controller.VideoCreatorController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppGUI extends JFrame {

    private static final Color BG = new Color(28, 32, 38);
    private static final Color PANEL = new Color(40, 45, 52);
    private static final Color TEXT = new Color(235, 235, 235);
    private static final Color TEXT_SOFT = new Color(190, 190, 190);
    private static final Color ACCENT = new Color(70, 130, 180);

    private JPasswordField apiKeyField;
    private DefaultListModel<String> fileListModel;
    private JList<String> fileList;
    private List<File> selectedFiles;
    private JTextField outputPathField;
    private JButton createBtn;
    private JProgressBar progressBar;
    private JTextArea logArea;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public AppGUI() {
        super("GPS Media Video Creator");
        selectedFiles = new ArrayList<>();
        initUI();
        setVisible(true);
    }

    private void initUI() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(760, 700);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(12, 12, 12, 12));
        setContentPane(root);

        root.add(buildTopPanel(), BorderLayout.NORTH);
        root.add(buildCenterPanel(), BorderLayout.CENTER);
        root.add(buildBottomPanel(), BorderLayout.SOUTH);
    }

    private JPanel buildTopPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(0, 0, 12, 0));

        JLabel title = new JLabel("GPS Media Video Creator");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Create a travel video from photos and videos with GPS.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(TEXT_SOFT);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(title);
        panel.add(Box.createVerticalStrut(4));
        panel.add(subtitle);

        return panel;
    }

    private JPanel buildCenterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG);

        panel.add(buildApiRow());
        panel.add(Box.createVerticalStrut(12));
        panel.add(buildFilesPanel());
        panel.add(Box.createVerticalStrut(12));
        panel.add(buildOutputRow());
        panel.add(Box.createVerticalStrut(12));
        panel.add(buildCreateRow());
        panel.add(Box.createVerticalStrut(12));
        panel.add(buildProgressRow());

        return panel;
    }

    private JPanel buildApiRow() {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(BG);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        JLabel label = makeLabel("OpenAI API Key:");
        apiKeyField = new JPasswordField();
        styleField(apiKeyField);

        row.add(label, BorderLayout.WEST);
        row.add(apiKeyField, BorderLayout.CENTER);

        return row;
    }

    private JPanel buildFilesPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(BG);

        JLabel label = makeLabel("Files:");
        panel.add(label, BorderLayout.NORTH);

        fileListModel = new DefaultListModel<>();
        fileList = new JList<>(fileListModel);
        fileList.setBackground(PANEL);
        fileList.setForeground(TEXT);
        fileList.setFont(new Font("SansSerif", Font.PLAIN, 12));
        fileList.setSelectionBackground(new Color(80, 90, 110));
        fileList.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        JScrollPane scroll = new JScrollPane(fileList);
        scroll.setPreferredSize(new Dimension(0, 180));
        scroll.setBorder(BorderFactory.createLineBorder(new Color(90, 90, 90)));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setBackground(BG);

        JButton addBtn = makeButton("Add Files");
        JButton removeBtn = makeButton("Remove");
        JButton clearBtn = makeButton("Clear");

        addBtn.addActionListener(e -> onAddFiles());
        removeBtn.addActionListener(e -> onRemoveSelected());
        clearBtn.addActionListener(e -> {
            selectedFiles.clear();
            fileListModel.clear();
            log("File list cleared.");
        });

        buttons.add(addBtn);
        buttons.add(removeBtn);
        buttons.add(clearBtn);

        panel.add(scroll, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildOutputRow() {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(BG);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        JLabel label = makeLabel("Output:");
        outputPathField = new JTextField(System.getProperty("user.home") + File.separator + "result.mp4");
        styleField(outputPathField);

        JButton browseBtn = makeButton("Browse");
        browseBtn.addActionListener(e -> onBrowseOutput());

        row.add(label, BorderLayout.WEST);
        row.add(outputPathField, BorderLayout.CENTER);
        row.add(browseBtn, BorderLayout.EAST);

        return row;
    }

    private JPanel buildCreateRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER));
        row.setBackground(BG);

        createBtn = new JButton("Create Video");
        createBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        createBtn.setBackground(ACCENT);
        createBtn.setForeground(Color.black);
        createBtn.setFocusPainted(false);
        createBtn.addActionListener(e -> onCreateVideo());

        row.add(createBtn);
        return row;
    }

    private JPanel buildProgressRow() {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(BG);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("Ready");
        progressBar.setForeground(ACCENT);
        progressBar.setBackground(PANEL);

        row.add(progressBar, BorderLayout.CENTER);
        return row;
    }

    private JScrollPane buildBottomPanel() {
        logArea = new JTextArea(8, 0);
        logArea.setEditable(false);
        logArea.setBackground(new Color(22, 24, 28));
        logArea.setForeground(new Color(210, 210, 210));
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        logArea.setBorder(new EmptyBorder(8, 8, 8, 8));
        logArea.setText("Ready.\n");

        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(BorderFactory.createTitledBorder("Log"));
        return scroll;
    }

    private void onAddFiles() {
        JFileChooser fc = new JFileChooser();
        fc.setMultiSelectionEnabled(true);
        fc.setDialogTitle("Select media files");
        fc.setFileFilter(new FileNameExtensionFilter(
                "Media Files",
                "jpg", "jpeg", "png", "heic", "webp",
                "mp4", "mov", "avi", "mkv", "m4v", "3gp"
        ));

        int result = fc.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        for (File f : fc.getSelectedFiles()) {
            if (!selectedFiles.contains(f)) {
                selectedFiles.add(f);
                fileListModel.addElement(f.getName());
            }
        }

        log(fc.getSelectedFiles().length + " file(s) added.");
    }

    private void onRemoveSelected() {
        int[] indices = fileList.getSelectedIndices();
        for (int i = indices.length - 1; i >= 0; i--) {
            selectedFiles.remove(indices[i]);
            fileListModel.remove(indices[i]);
        }
        log("Removed " + indices.length + " file(s).");
    }

    private void onBrowseOutput() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save output video");
        fc.setSelectedFile(new File(outputPathField.getText()));
        fc.setFileFilter(new FileNameExtensionFilter("MP4 Video", "mp4"));

        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            String path = f.getAbsolutePath();
            if (!path.toLowerCase().endsWith(".mp4")) {
                path += ".mp4";
            }
            outputPathField.setText(path);
        }
    }

    private void onCreateVideo() {
        String apiKey = new String(apiKeyField.getPassword()).trim();
        if (apiKey.isEmpty()) {
            showError("Please enter your OpenAI API key.");
            return;
        }

        if (selectedFiles.isEmpty()) {
            showError("Please add at least one file.");
            return;
        }

        String outputPath = outputPathField.getText().trim();
        if (outputPath.isEmpty()) {
            showError("Please choose an output path.");
            return;
        }

        setProcessing(true);
        progressBar.setValue(0);
        progressBar.setString("Starting...");

        File outputFile = new File(outputPath);
        List<File> files = new ArrayList<>(selectedFiles);

        executor.submit(() -> {
            VideoCreatorController controller = new VideoCreatorController(
                    apiKey,
                    (current, total) -> SwingUtilities.invokeLater(() -> {
                        int pct = (int) ((current / (double) total) * 100);
                        progressBar.setValue(pct);
                        progressBar.setString(current + "/" + total + " (" + pct + "%)");
                    }),
                    message -> SwingUtilities.invokeLater(() -> log(message))
            );

            try {
                controller.createVideo(files, outputFile);
                SwingUtilities.invokeLater(() -> {
                    progressBar.setValue(100);
                    progressBar.setString("Done");
                    JOptionPane.showMessageDialog(
                            AppGUI.this,
                            "Video created successfully.\n" + outputFile.getAbsolutePath(),
                            "Done",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    progressBar.setString("Error");
                    showError("Video creation failed:\n" + ex.getMessage());
                    ex.printStackTrace();
                });
            } finally {
                SwingUtilities.invokeLater(() -> setProcessing(false));
            }
        });
    }

    private void setProcessing(boolean processing) {
        createBtn.setEnabled(!processing);
        apiKeyField.setEnabled(!processing);

        if (processing) {
            createBtn.setText("Processing...");
        } else {
            createBtn.setText("Create Video");
        }
    }

    private void log(String message) {
        logArea.append("> " + message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private JLabel makeLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT_SOFT);
        label.setFont(new Font("SansSerif", Font.PLAIN, 13));
        label.setPreferredSize(new Dimension(120, 28));
        return label;
    }

    private void styleField(JTextField field) {
        field.setBackground(PANEL);
        field.setForeground(TEXT);
        field.setCaretColor(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(90, 90, 90)),
                new EmptyBorder(4, 8, 4, 8)
        ));
    }

    private JButton makeButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(70, 70, 70));
        btn.setForeground(Color.black);
        btn.setFocusPainted(false);
        return btn;
    }
}