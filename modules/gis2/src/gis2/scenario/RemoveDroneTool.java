package gis2.scenario;

import javax.swing.undo.AbstractUndoableEdit;

import maps.gml.GMLShape;

/**
 * Remove Drone tool.
 */
public class RemoveDroneTool extends ShapeTool {
    /**
     * Construct a RemoveDroneTool.
     *
     * @param editor The editor instance.
     */
    public RemoveDroneTool(ScenarioEditor editor) {
        super(editor);
    }

    @Override
    public String getName() {
        return "Remove drone";
    }

    @Override
    protected boolean shouldHighlight(GMLShape shape) {
        return true;
    }

    @Override
    protected void processClick(GMLShape shape) {
        editor.getScenario().removeDrone(shape.getID());
        editor.setChanged();
        editor.updateOverlays();
        editor.addEdit(new RemoveDroneEdit(shape.getID()));
    }

    private class RemoveDroneEdit extends AbstractUndoableEdit {
        private final int id;

        public RemoveDroneEdit(int id) {
            this.id = id;
        }

        @Override
        public void undo() {
            super.undo();
            editor.getScenario().addDrone(id);
            editor.updateOverlays();
        }

        @Override
        public void redo() {
            super.redo();
            editor.getScenario().removeDrone(id);
            editor.updateOverlays();
        }
    }


}
