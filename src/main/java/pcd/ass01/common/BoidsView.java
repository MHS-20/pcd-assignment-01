package pcd.ass01.common;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
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
    private int width, height;

    private int nBoids;
    private BoidsController controller;
    private boolean isRunning; //only for internal usage and button toggle

    public BoidsView(BoidsModel model, BoidsController sim, int width, int height, int nBoids) {
        this.width = width;
        this.height = height;
        this.nBoids = nBoids;
        this.controller = sim;
        this.isRunning = false;

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
                stop();
                isRunning = !isRunning;
                playButton.setText("Resume");
                resetButton.setEnabled(true);
            } else {
                start();
                isRunning = !isRunning;
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

    public void update(int frameRate, List<Boid> boids) {
        try {
            SwingUtilities.invokeAndWait(() -> {
                boidsPanel.setFrameRate(frameRate);
                boidsPanel.setBoids(boids);
                boidsPanel.repaint();
            });
        } catch (InterruptedException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public void start() {
        controller.notifyStart();
    }

    public void stop() {
        controller.notifyStop();
    }

    public void setResetButtonPressed() {
        controller.notifyResetPressed();
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
