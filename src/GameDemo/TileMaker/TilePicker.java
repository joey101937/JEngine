package GameDemo.TileMaker;

import Framework.Coordinate;
import Framework.Game;
import Framework.UI_Elements.UIElement;
import Framework.GraphicalAssets.Sprite;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class TilePicker extends UIElement {
    private Game hostGame;
    private JPanel contentPanel;
    private JScrollPane scrollPane;
    private JTextField searchField;
    private ArrayList<TileButton> tileButtons;
    private TileButton selectedButton;

    public TilePicker(Game g, Coordinate location) {
        hostGame = g;
        setLayout(new BorderLayout());
        setLocation(location.x, location.y);
        setSize(300, hostGame.getHeight());

        searchField = new JTextField();
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterTiles(searchField.getText());
            }
        });
        add(searchField, BorderLayout.NORTH);

        contentPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        scrollPane = new JScrollPane(contentPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane, BorderLayout.CENTER);

        tileButtons = new ArrayList<>();
        for (Tile tile : Tileset.library) {
            TileButton button = new TileButton(tile);
            tileButtons.add(button);
            contentPanel.add(button);
        }
    }

    private void filterTiles(String searchText) {
        contentPanel.removeAll();
        for (TileButton button : tileButtons) {
            if (button.tile.getSprite().getSignature().toLowerCase().contains(searchText.toLowerCase())) {
                contentPanel.add(button);
            }
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    @Override
    public void render() {
        if (Window.currentGame != hostGame) {
            setVisible(false);
        } else {
            setVisible(true);
        }
    }

    @Override
    public void tick() {
        // No specific tick logic needed for this component
    }

    public Tile getSelectedTile() {
        return selectedButton != null ? selectedButton.tile : null;
    }

    private class TileButton extends JButton {
        private Tile tile;

        public TileButton(Tile tile) {
            this.tile = tile;
            setLayout(new BorderLayout());
            
            JLabel imageLabel = new JLabel(new ImageIcon(tile.getSprite().getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH)));
            add(imageLabel, BorderLayout.CENTER);
            
            JLabel nameLabel = new JLabel(tile.getSprite().getSignature(), SwingConstants.CENTER);
            add(nameLabel, BorderLayout.SOUTH);

            addActionListener(e -> {
                if (selectedButton != null) {
                    selectedButton.setBackground(null);
                }
                setBackground(Color.WHITE);
                selectedButton = this;
            });
        }
    }
}
