package GameDemo.TileMaker;

import Framework.Coordinate;
import Framework.Game;
import Framework.Main;
import Framework.UI_Elements.UIElement;
import Framework.Window;

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
        setSize(300, hostGame.getWindowHeight());

        searchField = new JTextField();
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterTiles(searchField.getText());
            }
        });
        searchField.setBorder(BorderFactory.createCompoundBorder(
            searchField.getBorder(), 
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        searchPanel.add(searchField, BorderLayout.CENTER);
        this.add(searchPanel, BorderLayout.NORTH);

        contentPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        scrollPane = new JScrollPane(contentPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        this.add(scrollPane, BorderLayout.CENTER);

        tileButtons = new ArrayList<>();
        for (Tile tile : Tileset.library) {
            TileButton button = new TileButton(tile);
            tileButtons.add(button);
            contentPanel.add(button);
        }
        setVisible(true);
    }

    private void filterTiles(String searchText) {
        contentPanel.removeAll();
        for (TileButton button : tileButtons) {
            if (button.tile.getSprite().getSignature().toLowerCase().contains(searchText.toLowerCase())) {
                contentPanel.add(button);
            }
        }
        contentPanel.revalidate();
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
        }
    }

    public Tile getSelectedTile() {
        return selectedButton != null ? selectedButton.tile : null;
    }

    private class TileButton extends JButton {
        private Tile tile;

        public TileButton(Tile tile) {
            this.tile = tile;
            setLayout(new BorderLayout());
            
            int buttonSize = 120;
            setPreferredSize(new Dimension(buttonSize, buttonSize));
            
            JLabel imageLabel = new JLabel(new ImageIcon(tile.getSprite().getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH)));
            add(imageLabel, BorderLayout.CENTER);
            
            JLabel nameLabel = new JLabel("<html><center>" + tile.getSprite().getSignature() + "</center></html>", SwingConstants.CENTER);
            nameLabel.setPreferredSize(new Dimension(buttonSize, 30));
            add(nameLabel, BorderLayout.SOUTH);

            addActionListener(e -> {
                if (selectedButton != null) {
                    selectedButton.setBackground(null);
                }
                setBackground(Color.WHITE);
                selectedButton = this;
                TileMaker.game.requestFocus();
            });
        }
    }    
}
