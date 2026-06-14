package GameDemo.RTSDemo.MapEditor;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

/**
 * Left-side panel: background selector, object palette, selected-object properties.
 */
public class MapEditorPalette extends JPanel {

    private final MapEditorCanvas canvas;

    // Properties widgets
    private JLabel    propTypeLabel;
    private JComboBox<String> teamCombo;
    private JSpinner  hpSpinner;
    private JSpinner  rotSpinner;
    private JSpinner  zLayerSpinner;
    private JButton   deleteBtn;
    private boolean   updatingProps = false;

    private static final String[] TEAM_LABELS  = { "Silver (−1)", "Green (0)", "Red (1)", "Charcoal (2)", "Tan (3)", "Arctic (4)" };
    private static final int[]    TEAM_VALUES  = { -1, 0, 1, 2, 3, 4 };

    private static final String[] BACKGROUNDS = {
        "terrainPlaygroundHighground130.png",
        "terrainPlaygroundHighground.png",
        "terrainPlayground.png"
    };

    // Dark theme colours shared across the palette
    private static final Color BG_DEEP   = new Color(30, 30, 30);
    private static final Color BG_PANEL  = new Color(42, 42, 42);
    private static final Color BG_BUTTON = new Color(55, 55, 65);
    private static final Color FG_TEXT   = new Color(220, 220, 220);
    private static final Color FG_DIM    = new Color(160, 160, 160);
    private static final Color BORDER_CLR= new Color(80, 80, 90);
    private static final Color SAVE_BG   = new Color(30, 90, 50);
    private static final Color SAVE_BDR  = new Color(60, 180, 90);
    private static final Color LOAD_BG   = new Color(30, 60, 110);
    private static final Color LOAD_BDR  = new Color(60, 120, 200);
    private static final Color DEL_BG    = new Color(110, 30, 30);
    private static final Color DEL_BDR   = new Color(200, 60, 60);

    public MapEditorPalette(MapEditorCanvas canvas) {
        this.canvas = canvas;
        setPreferredSize(new Dimension(240, 0));
        setBackground(BG_DEEP);
        setLayout(new BorderLayout(4, 4));
        setBorder(new EmptyBorder(6, 6, 6, 6));

        add(buildTopSection(),    BorderLayout.NORTH);
        add(buildPaletteScroll(), BorderLayout.CENTER);
        add(buildPropsPanel(),    BorderLayout.SOUTH);

        canvas.setOnSelectionChanged(this::refreshProps);
    }

    // ── Top: background + save/load ───────────────────────────────────────────

    private JPanel buildTopSection() {
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBackground(BG_DEEP);

        // Background selector
        JPanel bgPanel = section("Background");
        JComboBox<String> bgCombo = new JComboBox<>(BACKGROUNDS);
        bgCombo.setSelectedItem(canvas.getMapData().background);
        styleCombo(bgCombo);
        bgCombo.addActionListener(e -> canvas.changeBackground((String) bgCombo.getSelectedItem()));
        bgCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        bgPanel.add(bgCombo);
        top.add(bgPanel);
        top.add(Box.createVerticalStrut(4));

        // Save / Load row
        JPanel ioRow = new JPanel(new GridLayout(1, 2, 6, 0));
        ioRow.setBackground(BG_DEEP);
        ioRow.setBorder(new EmptyBorder(2, 0, 4, 0));

        JButton saveBtn = actionButton("SAVE MAP", SAVE_BG, SAVE_BDR);
        JButton loadBtn = actionButton("LOAD MAP", LOAD_BG, LOAD_BDR);

        saveBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File("map.json"));
            fc.setDialogTitle("Save Map");
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    MapSerializer.save(canvas.getMapData(), fc.getSelectedFile());
                    JOptionPane.showMessageDialog(this, "Map saved successfully.", "Saved", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Save failed:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        loadBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Load Map");
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    MapData loaded = MapSerializer.load(fc.getSelectedFile());
                    canvas.setMapData(loaded);
                    bgCombo.setSelectedItem(loaded.background);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Load failed:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        ioRow.add(saveBtn);
        ioRow.add(loadBtn);
        top.add(ioRow);
        return top;
    }

    // ── Center: palette ───────────────────────────────────────────────────────

    private JScrollPane buildPaletteScroll() {
        JPanel palette = new JPanel();
        palette.setLayout(new BoxLayout(palette, BoxLayout.Y_AXIS));
        palette.setBackground(BG_PANEL);

        addCategory(palette, "Units",
            EditorObjectType.TANK, EditorObjectType.LIGHT_TANK, EditorObjectType.TRUCK,
            EditorObjectType.HELICOPTER, EditorObjectType.APACHE, EditorObjectType.TRANSPORT_HELI,
            EditorObjectType.RIFLEMAN, EditorObjectType.BAZOOKAMAN, EditorObjectType.LANDMINE);

        addCategory(palette, "Key Buildings", EditorObjectType.KEY_BUILDING);

        addCategory(palette, "Scenery",
            EditorObjectType.HANGAR, EditorObjectType.BUILDING_GREEN1, EditorObjectType.ORANGE_WOOD_HOUSE,
            EditorObjectType.PROPANE_TANK, EditorObjectType.GREEN_CONTAINER, EditorObjectType.METAL_SHACK,
            EditorObjectType.TREE1, EditorObjectType.TREE2, EditorObjectType.TREE3,
            EditorObjectType.STONE_WALL1,
            EditorObjectType.BARREL1, EditorObjectType.BARREL2,
            EditorObjectType.ROCK1, EditorObjectType.ROCK2,
            EditorObjectType.LOG1, EditorObjectType.LOG2,
            EditorObjectType.BARBED_WIRE, EditorObjectType.BARBED_WIRE_LONG,
            EditorObjectType.STUMP1, EditorObjectType.STUMP2, EditorObjectType.STUMP3,
            EditorObjectType.BUSH1, EditorObjectType.BUSH2, EditorObjectType.BUSH3);

        JScrollPane scroll = new JScrollPane(palette);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_CLR));
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        return scroll;
    }

    private void addCategory(JPanel parent, String name, EditorObjectType... types) {
        JPanel sec = section(name);
        sec.setLayout(new BoxLayout(sec, BoxLayout.Y_AXIS));
        for (EditorObjectType t : types) {
            sec.add(buildTypeButton(t));
            sec.add(Box.createVerticalStrut(2));
        }
        parent.add(sec);
        parent.add(Box.createVerticalStrut(4));
    }

    private JButton buildTypeButton(EditorObjectType type) {
        BufferedImage thumb = type.getThumbnail(36);
        ImageIcon icon = thumb != null ? new ImageIcon(thumb) : null;

        JButton btn = new JButton(type.displayName, icon);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setIconTextGap(6);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btn.setBackground(BG_BUTTON);
        btn.setForeground(FG_TEXT);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_CLR),
                new EmptyBorder(3, 6, 3, 6)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            canvas.setPaletteSelection(type);
            canvas.requestFocusInWindow();
        });
        return btn;
    }

    // ── Bottom: properties panel ──────────────────────────────────────────────

    private JPanel buildPropsPanel() {
        JPanel props = section("Properties");
        props.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(2, 2, 2, 2);

        propTypeLabel = new JLabel("—");
        propTypeLabel.setForeground(FG_DIM);

        teamCombo = new JComboBox<>(TEAM_LABELS);
        styleCombo(teamCombo);
        teamCombo.addActionListener(e -> applyTeam());

        hpSpinner = new JSpinner(new SpinnerNumberModel(100, 1, 100, 1));
        hpSpinner.addChangeListener(e -> applyHp());

        rotSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 359.9, 15.0));
        rotSpinner.addChangeListener(e -> applyRotation());

        zLayerSpinner = new JSpinner(new SpinnerNumberModel(1, -1000, 1000, 1));
        zLayerSpinner.addChangeListener(e -> applyZLayer());

        deleteBtn = actionButton("DELETE", DEL_BG, DEL_BDR);
        deleteBtn.addActionListener(e -> canvas.deleteSelectedObject());

        int row = 0;
        addRow(props, c, row++, "Type:",     propTypeLabel);
        addRow(props, c, row++, "Team:",     teamCombo);
        addRow(props, c, row++, "HP %:",     hpSpinner);
        addRow(props, c, row++, "Rotation:", rotSpinner);
        addRow(props, c, row++, "Z-Layer:",  zLayerSpinner);

        c.gridx = 0; c.gridy = row; c.gridwidth = 2;
        c.insets = new Insets(6, 2, 2, 2);
        props.add(deleteBtn, c);

        setPropsEnabled(false);
        return props;
    }

    private void addRow(JPanel p, GridBagConstraints c, int row, String label, JComponent widget) {
        c.gridwidth = 1; c.weightx = 0;
        c.gridx = 0; c.gridy = row;
        JLabel lbl = new JLabel(label);
        lbl.setForeground(FG_DIM);
        p.add(lbl, c);
        c.gridx = 1; c.weightx = 1;
        p.add(widget, c);
    }

    // ── Property sync ─────────────────────────────────────────────────────────

    public void refreshProps() {
        PlacedObject sel = canvas.getSelectedObject();
        if (sel == null) {
            propTypeLabel.setText("—");
            setPropsEnabled(false);
            return;
        }
        EditorObjectType type = EditorObjectType.fromClassName(sel.type);
        propTypeLabel.setText(type != null ? type.displayName : sel.type);

        updatingProps = true;
        teamCombo.setSelectedIndex(teamIndex(sel.team));
        teamCombo.setEnabled(type != null && type.hasTeam());
        hpSpinner.setValue(Math.max(1, Math.min(100, sel.hpPercent)));
        hpSpinner.setEnabled(type != null && type.hasHp());
        rotSpinner.setValue(sel.rotation);
        int displayZLayer = (sel.zLayer != Integer.MIN_VALUE) ? sel.zLayer
                          : (type != null ? type.defaultZLayer : 1);
        zLayerSpinner.setValue(displayZLayer);
        setPropsEnabled(true);
        updatingProps = false;
    }

    private void applyTeam() {
        if (updatingProps) return;
        PlacedObject sel = canvas.getSelectedObject();
        if (sel == null) return;
        sel.team = TEAM_VALUES[teamCombo.getSelectedIndex()];
        canvas.repaint();
    }

    private void applyHp() {
        if (updatingProps) return;
        PlacedObject sel = canvas.getSelectedObject();
        if (sel == null) return;
        sel.hpPercent = (Integer) hpSpinner.getValue();
    }

    private void applyRotation() {
        if (updatingProps) return;
        PlacedObject sel = canvas.getSelectedObject();
        if (sel == null) return;
        sel.rotation = (Double) rotSpinner.getValue();
        canvas.repaint();
    }

    private void applyZLayer() {
        if (updatingProps) return;
        PlacedObject sel = canvas.getSelectedObject();
        if (sel == null) return;
        sel.zLayer = (Integer) zLayerSpinner.getValue();
    }

    private void setPropsEnabled(boolean en) {
        teamCombo.setEnabled(en);
        hpSpinner.setEnabled(en);
        rotSpinner.setEnabled(en);
        zLayerSpinner.setEnabled(en);
        deleteBtn.setEnabled(en);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static int teamIndex(int team) {
        for (int i = 0; i < TEAM_VALUES.length; i++) if (TEAM_VALUES[i] == team) return i;
        return 1;
    }

    /** Prominently coloured action button — works under any Swing L&F. */
    private static JButton actionButton(String text, Color bg, Color border) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(getModel().isPressed() ? bg.darker() : getModel().isRollover() ? bg.brighter() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setForeground(Color.WHITE);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border, 2),
                new EmptyBorder(5, 4, 5, 4)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static void styleCombo(JComboBox<?> cb) {
        cb.setBackground(BG_BUTTON);
        cb.setForeground(FG_TEXT);
    }

    /** Dark-themed section panel with a titled border. */
    private static JPanel section(String title) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_PANEL);
        TitledBorder tb = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_CLR), title);
        tb.setTitleColor(FG_DIM);
        p.setBorder(tb);
        return p;
    }
}
