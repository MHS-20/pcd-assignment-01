package pcd.ass01.common;

import javax.swing.*;
import java.awt.*;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BoidsView {

    private final JButton resetButton;
    private JFrame frame;
    private BoidsPanel boidsPanel;
    private JSlider cohesionSlider, separationSlider, alignmentSlider;
    private JTextField nBoidsTextField;
    private JButton playButton;
    private BoidsModel model;
    private int width, height;
    private boolean isRunning;
    private int nBoids;
    private boolean isResetButtonPressed;
    ConcurrentLinkedQueue<List<Boid>> snapshotsQueue;


    public BoidsView(BoidsModel model, int width, int height, int nBoids) {
        this.model = model;
        this.width = width;
        this.height = height;
        this.nBoids = nBoids;

        this.isRunning = false;
        this.isResetButtonPressed = false;
        snapshotsQueue = new ConcurrentLinkedQueue<>();

        frame = new JFrame("Boids Simulation");
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel cp = new JPanel();
        LayoutManager layout = new BorderLayout();
        cp.setLayout(layout);

        boidsPanel = new BoidsPanel(this, model);
        cp.add(BorderLayout.CENTER, boidsPanel);

        JPanel slidersPanel = new JPanel();

        nBoidsTextField = new JTextField(String.valueOf(this.nBoids), 10);
        nBoidsTextField.setForeground(Color.BLACK);

        nBoidsTextField.addActionListener(l -> {
            nBoidsTextField.setForeground(Color.WHITE);
            String text = nBoidsTextField.getText();
            if (!isNumeric(text)) {
                nBoidsTextField.setBackground(Color.ORANGE);
                nBoidsTextField.setText("Int only");
            } else {
                nBoidsTextField.setBackground(Color.WHITE);
                nBoidsTextField.setForeground(Color.GREEN);
                this.nBoids = Integer.parseInt(nBoidsTextField.getText());
            }
        });

        resetButton = makeButton("Reset");
        resetButton.addActionListener(e -> {
            setResetButtonPressed();
        });


        playButton = makeButton("Resume");
        playButton.addActionListener(e -> {
            if (isRunning) {
                pause();
                playButton.setText("Resume");
                resetButton.setEnabled(true);
            } else {
                play();
                playButton.setText("Suspend");
                nBoidsTextField.setForeground(Color.BLACK);
                resetButton.setEnabled(false);
            }
        });


        separationSlider = makeSlider();
        separationSlider.addChangeListener(l -> {
            var val = separationSlider.getValue();
            model.setSeparationWeight(0.1 * val);
        });

        alignmentSlider = makeSlider();
        alignmentSlider.addChangeListener(l -> {
            var val = alignmentSlider.getValue();
            model.setAlignmentWeight(0.1 * val);
        });

        cohesionSlider = makeSlider();
        cohesionSlider.addChangeListener(l -> {
            var val = cohesionSlider.getValue();
            model.setCohesionWeight(0.1 * val);
        });

        slidersPanel.add(playButton);
        slidersPanel.add(new JLabel("Size"));
        slidersPanel.add(nBoidsTextField);
        slidersPanel.add(resetButton);
        slidersPanel.add(new JLabel("Separation"));
        slidersPanel.add(separationSlider);
        slidersPanel.add(new JLabel("Alignment"));
        slidersPanel.add(alignmentSlider);
        slidersPanel.add(new JLabel("Cohesion"));
        slidersPanel.add(cohesionSlider);

        cp.add(BorderLayout.SOUTH, slidersPanel);
        frame.setContentPane(cp);
        frame.setVisible(true);
    }

    private boolean isNumeric(String text) {
        if (text == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(text);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public synchronized boolean isRunning() {
        return this.isRunning;
    }

    public synchronized void play() {
        this.isRunning = true;
    }

    public synchronized void pause() {
        this.isRunning = false;
    }

    public synchronized boolean isResetButtonPressed() {
        return isResetButtonPressed;
    }

    public synchronized void setResetButtonUnpressed() {
        this.isResetButtonPressed = false;
    }

    public synchronized void setResetButtonPressed() {
        this.isResetButtonPressed = true;
    }

    private JButton makeButton(String text) {
        return new JButton(text);
    }

    private JSlider makeSlider() {
        var slider = new JSlider(JSlider.HORIZONTAL, 0, 20, 10);
        slider.setMajorTickSpacing(10);
        slider.setMinorTickSpacing(1);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        labelTable.put(0, new JLabel("0"));
        labelTable.put(10, new JLabel("1"));
        labelTable.put(20, new JLabel("2"));
        slider.setLabelTable(labelTable);
        slider.setPaintLabels(true);
        return slider;
    }

    public void update(int frameRate, List<Boid> snapshot) {
        snapshotsQueue.offer(snapshot);
        SwingUtilities.invokeLater(() -> {
            List<Boid> next = snapshotsQueue.poll();
            if (next != null) {
                boidsPanel.setFrameRate(frameRate);
                boidsPanel.setBoids(next);
                boidsPanel.repaint();
            }
        });
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getNumberOfBoids() {
        return this.nBoids;
    }
}
