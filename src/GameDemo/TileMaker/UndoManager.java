package GameDemo.TileMaker;

import Framework.Coordinate;
import java.util.Stack;
import java.util.List;

public class UndoManager {
    private Stack<UndoAction> undoStack = new Stack<>();

    public void addUndoableAction(Coordinate coord, Tile oldTile) {
        undoStack.push(new UndoAction(coord, oldTile));
    }

    public void addUndoAction(List<Coordinate> modifiedTiles) {
        if (!modifiedTiles.isEmpty()) {
            undoStack.push(new UndoAction(modifiedTiles));
        }
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            UndoAction action = undoStack.pop();
            action.undo();
        }
    }

    private static class UndoAction {
        private List<Coordinate> coordinates;
        private Tile oldTile;

        public UndoAction(Coordinate coord, Tile oldTile) {
            this.coordinates = List.of(coord);
            this.oldTile = oldTile;
        }

        public UndoAction(List<Coordinate> coordinates) {
            this.coordinates = coordinates;
        }

        public void undo() {
            for (Coordinate coord : coordinates) {
                if (oldTile != null) {
                    TileMaker.tilemap.tileGrid[coord.x][coord.y] = oldTile;
                } else {
                    // If oldTile is null, we're undoing a batch action
                    // In this case, we need to retrieve the previous tile state
                    // This might require additional implementation in TileMaker class
                    // For now, we'll set it to null (or you could set a default tile)
                    TileMaker.tilemap.tileGrid[coord.x][coord.y] = null;
                }
            }
        }
    }
}
