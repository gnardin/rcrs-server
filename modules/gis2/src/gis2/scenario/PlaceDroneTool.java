package gis2.scenario;

import javax.swing.undo.AbstractUndoableEdit;

import maps.gml.GMLShape;

/**
 * This is a tool for placing drones on the map.
 */
public class PlaceDroneTool extends ShapeTool {
    /**
     * Construct a PlaceDroneTool.
     *
     * @param editor The editor instance.
     */
    public PlaceDroneTool(ScenarioEditor editor) {
        super(editor);
    }

    @Override
    public String getName() {
        return "Place drone";
    }

    @Override
    protected boolean shouldHighlight(GMLShape shape) {
        return true;
    }

    @Override
    protected void processClick(GMLShape shape) {
        editor.getScenario().addDrone(shape.getID());
        editor.setChanged();
        editor.updateOverlays();
        editor.addEdit(new AddDroneEdit(shape.getID()));
    }

    private class AddDroneEdit extends AbstractUndoableEdit {
        private final int id;

        public AddDroneEdit(int id) {
            this.id = id;
        }

        @Override
        public void undo() {
            super.undo();
            editor.getScenario().removeDrone(id);
            editor.updateOverlays();
        }

        @Override
        public void redo() {
            super.redo();
            editor.getScenario().addDrone(id);
            editor.updateOverlays();
        }
    }
}
