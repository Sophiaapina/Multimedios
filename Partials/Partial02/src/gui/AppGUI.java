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

    private static final Color BG_DARK     = new Color(12, 18, 28);
    private static final Color BG_PANEL    = new Color(20, 28, 40);
    private static final Color ACCENT_TEAL = new Color(0, 220, 180);
    private static final Color ACCENT_RED  = new Color(180, 20, 40);
    private static final Color TEXT_WHITE  = new Color(240, 240, 240);
    private static final Color TEXT_DIM    = new Color(150, 160, 170);


    private JPasswordField  apiKeyField;
    private DefaultListModel<String> fileListModel;
    private JList<String>   fileList;
    private List<File>      selectedFiles;
    private JTextField      outputPathField;
    private JButton         createBtn;
    private JProgressBar    progressBar;
    private JTextArea       logArea;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public AppGUI() {
        super("GPS Media Video Creator");
        selectedFiles = new ArrayList<>();
        initUI();
        setVisible(true);
    }

    private void initUI() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(820, 760);
        setLocationRelativeTo(null);
        setResizable(true);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG_DARK);
        setContentPane(root);

        root.add(buildHeader(),      BorderLayout.NORTH);
        root.add(buildMainPanel(),   BorderLayout.CENTER);
        root.add(buildLogPanel(),    BorderLayout.SOUTH);
    }


    private JPanel buildHeader() {
        JPanel header = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(0, 70, 60),
                        getWidth(), getHeight(), new Color(60, 0, 20));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(24, 32, 24, 32));

        JLabel title = new JLabel("GPS MEDIA VIDEO CREATOR");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(TEXT_WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Multimedia & Computer Graphics — Universidad Panamericana 2026");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13));
        sub.setForeground(ACCENT_TEAL);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        sep.setForeground(ACCENT_RED);
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(sep);
        header.add(Box.createVerticalStrut(12));
        header.add(title);
        header.add(Box.createVerticalStrut(6));
        header.add(sub);
        return header;
    }


    private JPanel buildMainPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(20, 32, 10, 32));

        panel.add(buildApiKeyRow());
        panel.add(Box.createVerticalStrut(16));
        panel.add(buildFileSection());
        panel.add(Box.createVerticalStrut(16));
        panel.add(buildOutputRow());
        panel.add(Box.createVerticalStrut(20));
        panel.add(buildCreateRow());
        panel.add(Box.createVerticalStrut(14));
        panel.add(buildProgressRow());
        return panel;
    }


    private JPanel buildApiKeyRow() {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(BG_DARK);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JLabel label = styledLabel("Gemini API Key:");
        apiKeyField = new JPasswordField();
        styleTextField(apiKeyField);
        apiKeyField.setToolTipText("Paste your Google AI Studio API key here");

        row.add(label,       BorderLayout.WEST);
        row.add(apiKeyField, BorderLayout.CENTER);
        return row;
    }


    private JPanel buildFileSection() {
        JPanel section = new JPanel(new BorderLayout(0, 8));
        section.setBackground(BG_DARK);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = styledLabel("Media Files (photos & videos with GPS data):");
        section.add(label, BorderLayout.NORTH);

        fileListModel = new DefaultListModel<>();
        fileList = new JList<>(fileListModel);
        fileList.setBackground(BG_PANEL);
        fileList.setForeground(TEXT_WHITE);
        fileList.setSelectionBackground(ACCENT_TEAL.darker());
        fileList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        fileList.setBorder(BorderFactory.createLineBorder(new Color(40, 55, 70)));

        JScrollPane scroll = new JScrollPane(fileList);
        scroll.setPreferredSize(new Dimension(0, 160));
        scroll.getViewport().setBackground(BG_PANEL);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0, 120, 100)));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnPanel.setBackground(BG_DARK);

        JButton addBtn    = styledButton("+ Add Files",   ACCENT_TEAL);
        JButton removeBtn = styledButton("− Remove",      ACCENT_RED);
        JButton clearBtn  = styledButton("✕ Clear All",   new Color(80, 90, 110));

        addBtn.addActionListener(e -> onAddFiles());
        removeBtn.addActionListener(e -> onRemoveSelected());
        clearBtn.addActionListener(e -> {
            selectedFiles.clear();
            fileListModel.clear();
            log("File list cleared.");
        });

        btnPanel.add(addBtn);
        btnPanel.add(removeBtn);
        btnPanel.add(clearBtn);

        section.add(scroll,   BorderLayout.CENTER);
        section.add(btnPanel, BorderLayout.SOUTH);
        return section;
    }

    private JPanel buildOutputRow() {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(BG_DARK);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JLabel label = styledLabel("Output Video:");
        outputPathField = new JTextField(
                System.getProperty("user.home") + File.separator + "my_journey.mp4");
        styleTextField(outputPathField);

        JButton browseBtn = styledButton("Browse", new Color(60, 80, 120));
        browseBtn.addActionListener(e -> onBrowseOutput());

        row.add(label,           BorderLayout.WEST);
        row.add(outputPathField, BorderLayout.CENTER);
        row.add(browseBtn,       BorderLayout.EAST);
        return row;
    }

    private JPanel buildCreateRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER));
        row.setBackground(BG_DARK);

        createBtn = new JButton("▶  CREATE VIDEO");
        createBtn.setFont(new Font("SansSerif", Font.BOLD, 16));
        createBtn.setForeground(Color.WHITE);
        createBtn.setBackground(ACCENT_RED);
        createBtn.setFocusPainted(false);
        createBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_RED.darker(), 1),
                new EmptyBorder(10, 40, 10, 40)));
        createBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        createBtn.addActionListener(e -> onCreateVideo());

        row.add(createBtn);
        return row;
    }

    private JPanel buildProgressRow() {
        JPanel row = new JPanel(new BorderLayout(0, 4));
        row.setBackground(BG_DARK);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("Ready");
        progressBar.setForeground(ACCENT_TEAL);
        progressBar.setBackground(BG_PANEL);
        progressBar.setFont(new Font("SansSerif", Font.PLAIN, 12));
        progressBar.setBorder(BorderFactory.createLineBorder(new Color(40, 55, 70)));

        row.add(progressBar, BorderLayout.CENTER);
        return row;
    }

    private JScrollPane buildLogPanel() {
        logArea = new JTextArea(7, 0);
        logArea.setEditable(false);
        logArea.setBackground(new Color(8, 12, 18));
        logArea.setForeground(ACCENT_TEAL);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        logArea.setBorder(new EmptyBorder(8, 12, 8, 12));
        logArea.setText("» GPS Media Video Creator ready.\n");

        JScrollPane sp = new JScrollPane(logArea);
        sp.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(0, 80, 60)));
        return sp;
    }

    private void onAddFiles() {
        JFileChooser fc = new JFileChooser();
        fc.setMultiSelectionEnabled(true);
        fc.setDialogTitle("Select Photos and Videos");
        fc.setFileFilter(new FileNameExtensionFilter(
                "Media Files (jpg, jpeg, png, heic, mp4, mov, avi, mkv)",
                "jpg", "jpeg", "png", "heic", "webp",
                "mp4", "mov", "avi", "mkv", "m4v", "3gp"));

        int result = fc.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        for (File f : fc.getSelectedFiles()) {
            if (!selectedFiles.contains(f)) {
                selectedFiles.add(f);
                fileListModel.addElement(f.getName() + "  [" + f.getParentFile().getName() + "]");
            }
        }
        log(fc.getSelectedFiles().length + " file(s) added. Total: " + selectedFiles.size());
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
        fc.setDialogTitle("Save Output Video");
        fc.setSelectedFile(new File(outputPathField.getText()));
        fc.setFileFilter(new FileNameExtensionFilter("MP4 Video", "mp4"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            String path = f.getAbsolutePath();
            if (!path.toLowerCase().endsWith(".mp4")) path += ".mp4";
            outputPathField.setText(path);
        }
    }

    private void onCreateVideo() {
        // Validate inputs
        String apiKey = new String(apiKeyField.getPassword()).trim();
        if (apiKey.isEmpty()) {
            showError("Please enter your Gemini API key.");
            return;
        }
        if (selectedFiles.isEmpty()) {
            showError("Please add at least one media file.");
            return;
        }
        String outputPath = outputPathField.getText().trim();
        if (outputPath.isEmpty()) {
            showError("Please specify an output file path.");
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
                        progressBar.setString(current + "/" + total + "  (" + pct + "%)");
                    }),
                    message -> SwingUtilities.invokeLater(() -> log(message))
            );

            try {
                controller.createVideo(files, outputFile);
                SwingUtilities.invokeLater(() -> {
                    progressBar.setValue(100);
                    progressBar.setString("Complete!");
                    JOptionPane.showMessageDialog(AppGUI.this,
                            "Video created successfully!\n" + outputFile.getAbsolutePath(),
                            "Done", JOptionPane.INFORMATION_MESSAGE);
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
            createBtn.setText("⏳  Processing...");
        } else {
            createBtn.setText("▶  CREATE VIDEO");
        }
    }

    private void log(String message) {
        logArea.append("» " + message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private JLabel styledLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(TEXT_DIM);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        lbl.setPreferredSize(new Dimension(175, 28));
        return lbl;
    }

    private void styleTextField(JTextField field) {
        field.setBackground(BG_PANEL);
        field.setForeground(TEXT_WHITE);
        field.setCaretColor(ACCENT_TEAL);
        field.setFont(new Font("SansSerif", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 100, 80)),
                new EmptyBorder(4, 8, 4, 8)));
    }

    private JButton styledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(6, 14, 6, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}