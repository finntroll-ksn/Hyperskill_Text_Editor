package editor;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextEditor extends JFrame {
    private final int WIDTH = 640;
    private final int HEIGHT = 480;
    private int currentResult = 0;
    private boolean useRegex = false;
    private Matcher matcher = null;
    private ArrayList<MatchResult> results = null;

    private JTextArea textArea = new JTextArea();
    private JScrollPane scrollArea = new JScrollPane(textArea);
    private JFileChooser dlgChooseFile = new JFileChooser();
    private JTextField tbxFind = new JTextField();

    private JMenuBar menuBar = new JMenuBar();

    private JMenu menuFile = new JMenu("File");
    private JMenuItem menuOpen = new JMenuItem("Open");
    private JMenuItem menuSave = new JMenuItem("Save");
    private JMenuItem menuExit = new JMenuItem("Exit");
    private JCheckBox chbRegex = new JCheckBox("Use regex");

    private JMenu menuSearch = new JMenu("Search");
    private JMenuItem menuStartSearch = new JMenuItem("Start search");
    private JMenuItem menuPreviousSearch = new JMenuItem("Previous search");
    private JMenuItem menuNextMatch = new JMenuItem("Next match");
    private JMenuItem menuUseRegExp = new JMenuItem("Use regular expressions");

    private JButton btnSave = new JButton(new ImageIcon("save.png"));
    private JButton btnLoad = new JButton(new ImageIcon("load.png"));
    private JButton btnFind = new JButton(new ImageIcon("search.png"));
    private JButton btnPrev = new JButton(new ImageIcon("prev.png"));
    private JButton btnNext = new JButton(new ImageIcon("next.png"));

    public TextEditor() {
        setTitle("Text Editor");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setVisible(true);

        textArea.setName("TextArea");
        scrollArea.setName("ScrollPane");
        dlgChooseFile.setName("FileChooser");

        add(dlgChooseFile);
        add(scrollArea, BorderLayout.CENTER);
        add(createTopPanel(), BorderLayout.NORTH);
        setJMenuBar(createMenuBar());
    }

    private JMenuBar createMenuBar() {
        menuBar.add(createMenuFile());
        menuBar.add(createMenuSearch());

        return menuBar;
    }

    private JMenu createMenuFile() {
        menuOpen.addActionListener(actionEvent -> loadFile());
        menuSave.addActionListener(actionEvent -> saveFile());
        menuExit.addActionListener(actionEvent -> this.dispose());

        menuFile.setName("MenuFile");
        menuOpen.setName("MenuOpen");
        menuSave.setName("MenuSave");
        menuExit.setName("MenuExit");

        menuFile.add(menuOpen);
        menuFile.add(menuSave);
        menuFile.addSeparator();
        menuFile.add(menuExit);

        return menuFile;
    }

    private JMenu createMenuSearch() {
        menuStartSearch.addActionListener(actionEvent -> threadCreateResult());
        menuPreviousSearch.addActionListener(ActiveEvent -> previousFind());
        menuNextMatch.addActionListener(ActiveEvent -> nextFind());
        menuUseRegExp.addActionListener(actionEvent -> toggleUseRegex());

        menuSearch.setName("MenuSearch");
        menuStartSearch.setName("MenuStartSearch");
        menuPreviousSearch.setName("MenuPreviousMatch");
        menuNextMatch.setName("MenuNextMatch");
        menuUseRegExp.setName("MenuUseRegExp");

        menuSearch.add(menuStartSearch);
        menuSearch.add(menuPreviousSearch);
        menuSearch.add(menuNextMatch);
        menuSearch.add(menuUseRegExp);

        return menuSearch;
    }

    private JPanel createTopPanel() {
        btnLoad.setName("OpenButton");
        btnLoad.addActionListener(ActiveEvent -> loadFile());
        btnSave.setName("SaveButton");
        btnSave.addActionListener(ActiveEvent -> saveFile());

        btnFind.setName("StartSearchButton");
        btnFind.addActionListener(ActiveEvent -> threadCreateResult());
        btnPrev.setName("PreviousMatchButton");
        btnPrev.addActionListener(ActiveEvent -> previousFind());
        btnNext.setName("NextMatchButton");
        btnNext.addActionListener(ActiveEvent -> nextFind());
        chbRegex.setName("UseRegExCheckbox");
        chbRegex.addActionListener(ActiveEvent -> toggleUseRegex());

        tbxFind.setName("SearchField");

        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel leftPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        JPanel rightPanel = new JPanel(new FlowLayout());
        JPanel rightFindPanel = new JPanel(new GridLayout(1, 3, 5, 5));

        leftPanel.add(btnLoad);
        leftPanel.add(btnSave);

        rightFindPanel.add(btnFind);
        rightFindPanel.add(btnPrev);
        rightFindPanel.add(btnNext);
        rightPanel.add(rightFindPanel);
        rightPanel.add(chbRegex);

        topPanel.add(leftPanel, BorderLayout.WEST);
        topPanel.add(tbxFind, BorderLayout.CENTER);
        topPanel.add(rightPanel, BorderLayout.EAST);

        return topPanel;
    }

    public void loadFile() {
        File chooseFile = null;

        if (dlgChooseFile.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        chooseFile = dlgChooseFile.getSelectedFile();
        textArea.setText("");

        try {
            textArea.setText(loadFileAsString(chooseFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveFile() {
        File chooseFile = null;

        if (dlgChooseFile.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        chooseFile = dlgChooseFile.getSelectedFile();

        try {
            saveString(chooseFile, getTextArea());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String loadFileAsString(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()));
    }

    private void saveString(File file, String data) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(data);
        }
    }

    String getTextArea() {
        return textArea.getText();
    }

    public synchronized void threadCreateResult() {
        Thread thread = new Thread(() -> createResults());
        thread.start();
    }

    private void createResults() {
        this.results = createResults(isUseRegex());

        if (results.size() > 0) {
            setCurrentResultIndex(0);
        } else {
            currentResult = -1;
        }
    }

    private ArrayList<MatchResult> createResults(boolean isUseRegex) {
        String pattern = getTbxFind();
        String string = getTextArea();
        return isUseRegex ? getRegexResults(string, pattern) : getNotRegexResults(string, pattern);
    }

    public boolean isUseRegex() {
        return useRegex;
    }

    public String getTbxFind() {
        return tbxFind.getText();
    }

    private void setUseRegex(boolean value) {
        useRegex = value;
        chbRegex.setSelected(useRegex);
    }

    private void toggleUseRegex() {
        setUseRegex(!isUseRegex());
    }

    private ArrayList<MatchResult> getNotRegexResults(String string, String pattern) {
        ArrayList<MatchResult> results = new ArrayList<>();
        int start = -1;

        while (true) {
            start = string.indexOf(pattern, start + 1);

            if (start == -1) {
                break;
            }

            results.add(new MatchResult(start, pattern));
        }

        return results;
    }

    private ArrayList<MatchResult> getRegexResults(String string, String pattern) {
        matcher = Pattern.compile(pattern).matcher(string);
        ArrayList<MatchResult> results = new ArrayList<>();

        while (matcher.find()) {
            results.add(new MatchResult(matcher.start(), matcher.group()));
        }

        return results;
    }

    private void setCurrentResultIndex(int index) {
        currentResult = index;
        setSelection();
    }

    private void setSelection() {
        if (currentResult < 0) {
            return;
        }

        setSelection(results.get(currentResult));
    }

    private void setSelection(MatchResult result) {
        if (result == null) {
            return;
        }

        textArea.setCaretPosition(result.start + result.found.length());
        textArea.select(result.start, result.start + result.found.length());
        textArea.grabFocus();
    }

    public void previousFind() {
        if (currentResult == -1) {
            return;
        }

        int newIndex = currentResult - 1;

        if (newIndex < 0) {
            newIndex = results.size() - 1;
        }

        setCurrentResultIndex(newIndex);
    }

    public void nextFind() {
        if (currentResult == -1) {
            return;
        }

        int newIndex = currentResult + 1;

        if (newIndex >= results.size()) {
            newIndex = 0;
        }

        setCurrentResultIndex(newIndex);
    }
}
