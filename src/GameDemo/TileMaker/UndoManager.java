package GameDemo.TileMaker;

import Framework.Coordinate;
import java.util.LinkedList;

public class UndoManager {
    private LinkedList<UndoAction> undoStack = new LinkedList<>();
    private static final int MAX_UNDO_ACTIONS = 50;

    public void addUndoableAction(Coordinate coord, Tile oldTile) {
        undoStack.addFirst(new UndoAction(coord, oldTile));
        if (undoStack.size() > MAX_UNDO_ACTIONS) {
            undoStack.removeLast();
        }
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            UndoAction action = undoStack.removeFirst();
            action.undo();
        }
    }

    private static class UndoAction {
        private Coordinate coordinate;
        private Tile oldTile;

        public UndoAction(Coordinate coord, Tile oldTile) {
            this.coordinate = coord;
            this.oldTile = oldTile;
        }

        public void undo() {
            TileMaker.tilemap.tileGrid[coordinate.x][coordinate.y] = oldTile;
        }
    }
}
