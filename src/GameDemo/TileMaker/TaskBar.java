package GameDemo.TileMaker;

import Framework.Coordinate;
import Framework.Game;
import Framework.Main;
import Framework.UI_Elements.UIElement;
import Framework.Window;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class TaskBar extends UIElement {
    private Game hostGame;
    private JPanel panel;
    private JLabel titleLabel;
    private JButton saveButton, saveAsButton, exportImageButton, hideUIButton, loadButton;
    private JSlider translucencySlider;

    public TaskBar(Game g, Coordinate location) {
        hostGame = g;
        setLayout(new BorderLayout());
        setLocation(location.x, location.y);
        setSize(hostGame.getWindowWidth(), 46);

        panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setPreferredSize(new Dimension(hostGame.getWindowWidth(), 46));

        titleLabel = new JLabel(TileMaker.tilemap.name);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        panel.add(titleLabel);

        saveButton = createButton("Save", e -> {handleSaveClick();});
        saveAsButton = createButton("Save As...", e -> {handleSaveAsClick();});
        loadButton = createButton("Load", e -> {handleLoadClick();});
        exportImageButton = createButton("Export Image", e -> {});

        translucencySlider = new JSlider(JSlider.HORIZONTAL, 0, 2, 1);
        translucencySlider.setMajorTickSpacing(1);
        translucencySlider.setPaintTicks(true);
        translucencySlider.setPaintLabels(false);
        translucencySlider.setPreferredSize(new Dimension(150, 46));
        translucencySlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                System.out.println("Translucency Slider: " + translucencySlider.getValue());
            }
        });

        JLabel sliderLabel = new JLabel("Translucency:");
        panel.add(sliderLabel);
        panel.add(translucencySlider);

        hideUIButton = createButton("Hide UI", e -> {});

        this.add(panel, BorderLayout.CENTER);
        setVisible(true);
    }

    private JButton createButton(String text, ActionListener ac) {
        JButton button = new JButton(text);
        button.addActionListener(ac);
        panel.add(button);
        return button;
    }

    @Override
    public void render() {
    }

    @Override
    public void tick() {
         if(hostGame.getGameTickNumber() % Main.ticksPerSecond == 0) {
            if (Window.currentGame != hostGame) {
            setVisible(false);
            } else {
                setVisible(true);
            }
            revalidate();
        }
    }
    
    
    public void handleSaveClick() {
        Tileset.exportTileGridToCSV(TileMaker.tilemap.tileGrid, TileMaker.tilemap.name);
        TileMaker.game.requestFocus();
    }
    
    public void handleSaveAsClick() {
        TileMaker.tilemap.name = Main.prompt("Enter Filename");
        titleLabel.setText(TileMaker.tilemap.name);
        Tileset.exportTileGridToCSV(TileMaker.tilemap.tileGrid, TileMaker.tilemap.name);
    }
    
    public void handleLoadClick() {
        var loaded = Tileset.importTileGridFromCSV();
        if(loaded == null) return;
        TileMaker.game.requestFocus();
    }
}