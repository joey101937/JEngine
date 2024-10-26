package GameDemo.TileMaker;

import Framework.Coordinate;
import java.util.LinkedList;

public class UndoManager {
    private final LinkedList<UndoAction> undoStack = new LinkedList<>();
    private static final int MAX_UNDO_ACTIONS = 100;

    public synchronized void addUndoableAction(Coordinate gridCoord, Tile oldTile) {
        undoStack.addFirst(new UndoAction(gridCoord, oldTile));
        if (undoStack.size() > MAX_UNDO_ACTIONS) {
            undoStack.removeLast();
        }
    }

    public synchronized void undo() {
        if (!undoStack.isEmpty()) {
            System.out.println("size" + undoStack.size());
            UndoAction action = undoStack.removeFirst();
            action.undo();
        }
    }

    private static class UndoAction {
        private final Coordinate gridCoordinate;
        private final Tile oldTile;

        public UndoAction(Coordinate gridCoord, Tile oldTile) {
            this.gridCoordinate = gridCoord;
            this.oldTile = oldTile;
        }

        public void undo() {
            System.out.println("undo running adding " + oldTile);
            TileMaker.tilemap.tileGrid[gridCoordinate.x][gridCoordinate.y] = oldTile;
        }
    }
}
