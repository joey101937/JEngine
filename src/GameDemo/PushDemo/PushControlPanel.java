package GameDemo.PushDemo;

import Framework.Game;
import Framework.Push;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

/**
 * Floating control panel for configuring push parameters in real-time.
 * PushBlock reads settings from this panel's static fields when creating pushes.
 */
public class PushControlPanel extends JFrame {

    // Static settings read by PushBlock each time it creates a push
    public static volatile double pushStrength = 3.0;
    public static volatile double pushSpeed = 8.0;
    public static volatile int pushDuration = 60;
    public static volatile String pushType = "Decay";
    public static volatile double playerIntrinsicStrength = 1.0;

    private static Game gameRef;

    private JSpinner strengthSpinner;
    private JSpinner speedSpinner;
    private JSpinner durationSpinner;
    private JComboBox<String> typeCombo;
    private JSpinner intrinsicSpinner;

    public PushControlPanel(Game game) {
        gameRef = game;
        setTitle("Push Demo Controls");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(false);
        buildUI();
        pack();
        setLocation(50, 150);
        setAlwaysOnTop(true);
        setVisible(true);
    }

    private void buildUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        panel.setBackground(new Color(40, 40, 40));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        Font labelFont = new Font("Tahoma", Font.PLAIN, 13);
        Font titleFont = new Font("Tahoma", Font.BOLD, 15);

        // Title
        JLabel title = makeLabel("Push System Demo", titleFont, Color.WHITE);
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        panel.add(title, c);
        c.gridwidth = 1;

        // Push Strength
        c.gridx = 0; c.gridy = 1;
        panel.add(makeLabel("Push Strength", labelFont, Color.LIGHT_GRAY), c);
        strengthSpinner = new JSpinner(new SpinnerNumberModel(pushStrength, 0.1, 20.0, 0.5));
        strengthSpinner.setPreferredSize(new Dimension(80, 24));
        strengthSpinner.addChangeListener(e -> pushStrength = (double) strengthSpinner.getValue());
        c.gridx = 1;
        panel.add(strengthSpinner, c);

        // Push Speed
        c.gridx = 0; c.gridy = 2;
        panel.add(makeLabel("Push Speed (px/tick)", labelFont, Color.LIGHT_GRAY), c);
        speedSpinner = new JSpinner(new SpinnerNumberModel(pushSpeed, 0.5, 40.0, 0.5));
        speedSpinner.setPreferredSize(new Dimension(80, 24));
        speedSpinner.addChangeListener(e -> pushSpeed = (double) speedSpinner.getValue());
        c.gridx = 1;
        panel.add(speedSpinner, c);

        // Push Duration
        c.gridx = 0; c.gridy = 3;
        panel.add(makeLabel("Push Duration (ticks, -1=inf)", labelFont, Color.LIGHT_GRAY), c);
        durationSpinner = new JSpinner(new SpinnerNumberModel(pushDuration, -1, 600, 1));
        durationSpinner.setPreferredSize(new Dimension(80, 24));
        durationSpinner.addChangeListener(e -> pushDuration = (int) durationSpinner.getValue());
        c.gridx = 1;
        panel.add(durationSpinner, c);

        // Push Type
        c.gridx = 0; c.gridy = 4;
        panel.add(makeLabel("Push Type", labelFont, Color.LIGHT_GRAY), c);
        typeCombo = new JComboBox<>(new String[]{"Constant", "Decay", "Burst", "Oscillate"});
        typeCombo.addActionListener(e -> pushType = (String) typeCombo.getSelectedItem());
        c.gridx = 1;
        panel.add(typeCombo, c);

        // Player Intrinsic Strength
        c.gridx = 0; c.gridy = 5;
        panel.add(makeLabel("Player Intrinsic Strength", labelFont, Color.LIGHT_GRAY), c);
        intrinsicSpinner = new JSpinner(new SpinnerNumberModel(playerIntrinsicStrength, 0.0, 10.0, 0.1));
        intrinsicSpinner.setPreferredSize(new Dimension(80, 24));
        intrinsicSpinner.addChangeListener(e -> {
            playerIntrinsicStrength = (double) intrinsicSpinner.getValue();
            applyPlayerIntrinsicStrength();
        });
        c.gridx = 1;
        panel.add(intrinsicSpinner, c);

        // Clear Pushes button
        JButton clearBtn = new JButton("Clear All Pushes  [C]");
        clearBtn.addActionListener(e -> clearAllPushes());
        c.gridx = 0; c.gridy = 6; c.gridwidth = 2;
        panel.add(clearBtn, c);

        // Info label
        c.gridy = 7;
        panel.add(makeLabel("WASD = move red block", labelFont, Color.GRAY), c);
        c.gridy = 8;
        panel.add(makeLabel("Ram into blocks to push them", labelFont, Color.GRAY), c);

        getContentPane().add(panel);
        getContentPane().setBackground(new Color(40, 40, 40));
    }

    private JLabel makeLabel(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }

    private void applyPlayerIntrinsicStrength() {
        if (gameRef == null) return;
        for (var obj : gameRef.handler.getAllObjects()) {
            if (obj instanceof PushBlock) {
                obj.intrinsicStrength = playerIntrinsicStrength;
            }
        }
    }

    private static void clearAllPushes() {
        if (gameRef == null) return;
        for (var obj : gameRef.handler.getAllObjects()) {
            obj.clearPushes();
        }
    }

    /**
     * Creates a Push using current panel settings, directed along (dx, dy).
     */
    public static Push createPush(double dx, double dy) {
        Consumer<Push> updater = buildUpdater(pushType, pushSpeed);
        int duration = pushType.equals("Burst") ? 15 : pushDuration;
        double speed = pushType.equals("Burst") ? pushSpeed * 3.0 : pushSpeed;
        return new Push(dx, dy, speed, pushStrength, duration, updater);
    }

    private static Consumer<Push> buildUpdater(String type, double originalSpeed) {
        switch (type) {
            case "Decay":
                return p -> {
                    p.speed *= 0.96;
                    p.strength *= 0.96;
                };
            case "Oscillate":
                return p -> p.speed = originalSpeed * (0.5 + 0.5 * Math.sin(p.getTicksApplied() * 0.25));
            default:
                return null;
        }
    }
}
