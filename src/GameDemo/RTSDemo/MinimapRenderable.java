package GameDemo.RTSDemo;

import java.awt.Color;

public interface MinimapRenderable {
    enum Shape { CIRCLE, RECTANGLE }

    Color getMinimapColor();

    default Shape getMinimapShape() {
        return Shape.RECTANGLE;
    }
}
