package crawler;

import javax.swing.*;
import java.awt.*;


public class WebCrawler extends JFrame {

    //first row
    private final JLabel startURLLabel = new JLabel("Start URL:");
    private final JTextField urlTextField = new JTextField();
    private final JToggleButton runButton = new JToggleButton("Run");
    //second row
    private final JLabel workersLabel = new JLabel("Workers:");
    private final JTextField workersTextField = new JTextField();
    //third row
    private final JLabel depthLabel = new JLabel("Maximum depth:");
    private final JTextField depthTextField = new JTextField();
    private final JCheckBox depthCheckBox = new JCheckBox("Enabled");
    //fourth row
    private final JLabel timeLimitLabel = new JLabel("Time limit:");
    private final JTextField timeLimitTextField = new JTextField();
    private final JLabel secondsLabel = new JLabel("seconds");
    private final JCheckBox timeLimitEnabledCB = new JCheckBox("Enabled");
    //fifth row
    private final JLabel elapsedTimeLabel = new JLabel("Elapsed time:");
    private final JLabel elapsedTimeActual = new JLabel("0:00");
    //sixth row
    private final JLabel parsedLabel = new JLabel("Parsed pages:");
    private final JLabel parsedPagesActual = new JLabel("0");
    //seventh row
    private final JLabel exportLabel = new JLabel("Export:");
    private final JTextField exportUrlTextField = new JTextField();
    private final JButton exportButton = new JButton("Save");

    public WebCrawler() {
        setNames();
        addActionListeners();
        placeComponents();
        initialize();
    }

    private void setNames() {
        urlTextField.setName("UrlTextField");
        runButton.setName("RunButton");
        depthTextField.setName("DepthTextField");
        depthCheckBox.setName("DepthCheckBox");
        parsedLabel.setName("ParsedLabel");
        exportUrlTextField.setName("ExportUrlTextField");
        exportButton.setName("ExportButton");

    }

    private void addActionListeners() {
        runButton.addActionListener(ae -> buttonAction());
//        exportButton.addActionListener(ae -> saveAction());
    }

    private void placeComponents() {
        Container pane = this.getContentPane();
        pane.setLayout(new GridBagLayout());

        setupFirstRow(pane);
        setupSecondRow(pane);
        setupThirdRow(pane);
        setupFourthRow(pane);
        setupFifthRow(pane);
        setupSixthRow(pane);
        setupLastRow(pane);
    }

    private void setupFirstRow(Container pane) {
        GridBagConstraints constraints = getFreshConstraints();
        constraints.gridy = 0;
        constraints.gridx = 0;
        addComponent(pane, startURLLabel, constraints);

        constraints = getFreshConstraints();
        constraints.gridy = 0;
        constraints.gridx = 1;
        constraints.gridwidth = 2;
        constraints.weightx = 2;
        addComponent(pane, urlTextField, constraints);

        constraints = getFreshConstraints();
        constraints.gridy = 0;
        constraints.gridx = 3;
        addComponent(pane, runButton, constraints);

    }

    private void setupSecondRow(Container pane) {
        GridBagConstraints constraints = getFreshConstraints();
        constraints.gridy = 1;
        constraints.gridx = 0;
        addComponent(pane, workersLabel, constraints);

        constraints = getFreshConstraints();
        constraints.gridy = 1;
        constraints.gridx = 1;
        constraints.gridwidth = 3;
        addComponent(pane, workersTextField, constraints);
    }

    private void setupThirdRow(Container pane) {
        GridBagConstraints constraints = getFreshConstraints();
        constraints.gridy = 2;
        constraints.gridx = 0;
        addComponent(pane, depthLabel, constraints);

        constraints = getFreshConstraints();
        constraints.gridy = 2;
        constraints.gridx = 1;
        constraints.gridwidth = 2;
        addComponent(pane, depthTextField, constraints);

        constraints = getFreshConstraints();
        constraints.gridy = 2;
        constraints.gridx = 3;
        addComponent(pane, depthCheckBox, constraints);
    }

    private void setupFourthRow(Container pane) {
        GridBagConstraints constraints = getFreshConstraints();
        constraints.gridy = 3;
        constraints.gridx = 0;
        addComponent(pane, timeLimitLabel, constraints);

        constraints = getFreshConstraints();
        constraints.gridy = 3;
        constraints.gridx = 1;
        constraints.weightx = 2;
        addComponent(pane, timeLimitTextField, constraints);

        constraints = getFreshConstraints();
        constraints.gridy = 3;
        constraints.gridx = 2;
        addComponent(pane, secondsLabel, constraints);

        constraints = getFreshConstraints();
        constraints.gridy = 3;
        constraints.gridx = 3;
        addComponent(pane, timeLimitEnabledCB, constraints);

    }

    private void setupFifthRow(Container pane) {
        GridBagConstraints constraints = getFreshConstraints();
        constraints.gridy = 4;
        constraints.gridx = 0;
        addComponent(pane, elapsedTimeLabel, constraints);

        constraints = getFreshConstraints();
        constraints.gridy = 4;
        constraints.gridx = 1;
        addComponent(pane, elapsedTimeActual, constraints);
    }

    private void setupSixthRow(Container pane) {
        GridBagConstraints constraints = getFreshConstraints();
        constraints.gridy = 5;
        constraints.gridx = 0;
        addComponent(pane, parsedLabel, constraints);

        constraints = getFreshConstraints();
        constraints.gridy = 5;
        constraints.gridx = 1;
        addComponent(pane, parsedPagesActual, constraints);
    }

    private void setupLastRow(Container pane) {
        GridBagConstraints constraints = getFreshConstraints();
        constraints.gridy = 6;
        constraints.gridx = 0;
        addComponent(pane, exportLabel, constraints);

        constraints = getFreshConstraints();
        constraints.gridy = 6;
        constraints.gridx = 1;
        constraints.gridwidth = 2;
        addComponent(pane, exportUrlTextField, constraints);

        constraints = getFreshConstraints();
        constraints.gridy = 6;
        constraints.gridx = 3;
        addComponent(pane, exportButton, constraints);
    }

    private GridBagConstraints getFreshConstraints() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        return constraints;
    }

    private void addComponent(Container pane, Component component, GridBagConstraints constraints) {
        pane.add(component, constraints);
    }

    private void buttonAction() {
        new Listener(this).start();
    }

    public JTextField getUrlTextField() {
        return urlTextField;
    }

    public JToggleButton getRunButton() {
        return runButton;
    }

    public JTextField getWorkersTextField() {
        return workersTextField;
    }

    public JTextField getDepthTextField() {
        return depthTextField;
    }

    public JCheckBox getDepthCheckBox() {
        return depthCheckBox;
    }

    public JTextField getTimeLimitTextField() {
        return timeLimitTextField;
    }

    public JCheckBox getTimeLimitEnabledCB() {
        return timeLimitEnabledCB;
    }

    public JLabel getElapsedTimeActual() {
        return elapsedTimeActual;
    }

    public JLabel getParsedPagesActual() {
        return parsedPagesActual;
    }

    public JTextField getExportUrlTextField() {
        return exportUrlTextField;
    }

    public JButton getExportButton() {
        return exportButton;
    }

    private void initialize() {
        setTitle("Web Crawler");
        setPreferredSize(new Dimension(600, 250));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

}