package GameDemo.TileMaker;

import Framework.Coordinate;
import Framework.Game;
import Framework.UI_Elements.UIElement;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class TaskBar extends UIElement {
    private Game hostGame;
    private JPanel panel;
    private JLabel titleLabel;
    private JButton saveButton, saveAsButton, exportImageButton, hideUIButton;
    private JSlider translucencySlider;

    public TaskBar(Game g, Coordinate location) {
        hostGame = g;
        setLayout(new BorderLayout());
        setLocation(location.x, location.y);
        setSize(hostGame.getWindowWidth(), 40);

        panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setPreferredSize(new Dimension(hostGame.getWindowWidth(), 40));

        titleLabel = new JLabel("Untitled");
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        panel.add(titleLabel);

        saveButton = createButton("Save");
        saveAsButton = createButton("Save As");
        exportImageButton = createButton("Export Image");

        translucencySlider = new JSlider(JSlider.HORIZONTAL, 0, 2, 1);
        translucencySlider.setMajorTickSpacing(1);
        translucencySlider.setPaintTicks(true);
        translucencySlider.setPaintLabels(true);
        translucencySlider.setPreferredSize(new Dimension(150, 40));
        translucencySlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                System.out.println("Translucency Slider: " + translucencySlider.getValue());
            }
        });

        JLabel sliderLabel = new JLabel("Translucency:");
        panel.add(sliderLabel);
        panel.add(translucencySlider);

        hideUIButton = createButton("Hide UI");

        this.add(panel, BorderLayout.CENTER);
        setVisible(true);
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.addActionListener(e -> System.out.println(text));
        panel.add(button);
        return button;
    }

    @Override
    public void render() {
        if (hostGame.getGameTickNumber() % 60 == 0) {
            setVisible(Window.currentGame == hostGame);
        }
    }

    @Override
    public void tick() {
        // No specific tick logic needed for this component
    }
}
