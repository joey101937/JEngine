package GameDemo.RTSDemo.MapEditor;

import GameDemo.RTSDemo.RTSAssetManager;
import java.awt.*;
import javax.swing.*;

public class MapEditorMain {

    public static void main(String[] args) {
        System.setProperty("sun.java2d.uiScale", "1");

        // Show loading splash while assets initialize
        JWindow splash = new JWindow();
        JLabel splashLabel = new JLabel("Loading assets...", SwingConstants.CENTER);
        splashLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        splashLabel.setForeground(Color.WHITE);
        JPanel splashPanel = new JPanel(new BorderLayout());
        splashPanel.setBackground(new Color(30, 30, 30));
        splashPanel.add(splashLabel, BorderLayout.CENTER);
        splash.setContentPane(splashPanel);
        splash.setSize(300, 100);
        splash.setLocationRelativeTo(null);
        splash.setVisible(true);

        // Initialize assets on a background thread so the splash shows
        Thread initThread = new Thread(() -> {
            RTSAssetManager.initialize();
            SwingUtilities.invokeLater(() -> {
                splash.dispose();
                showEditor();
            });
        });
        initThread.setDaemon(true);
        initThread.start();
    }

    private static void showEditor() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (Exception ignored) {}

        JFrame frame = new JFrame("RTS Map Editor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        MapEditorCanvas canvas = new MapEditorCanvas();
        MapEditorPalette palette = new MapEditorPalette(canvas);

        JLabel statusBar = new JLabel(" Ready");
        statusBar.setFont(new Font("Monospaced", Font.PLAIN, 11));
        statusBar.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        statusBar.setBackground(new Color(30, 30, 30));
        statusBar.setForeground(Color.LIGHT_GRAY);
        statusBar.setOpaque(true);
        canvas.setStatusUpdater(statusBar::setText);

        frame.add(palette, BorderLayout.WEST);
        frame.add(canvas,  BorderLayout.CENTER);
        frame.add(statusBar, BorderLayout.SOUTH);

        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setMinimumSize(new Dimension(900, 600));
        frame.setVisible(true);

        // Focus canvas so keyboard shortcuts work immediately
        SwingUtilities.invokeLater(canvas::requestFocusInWindow);
    }
}
