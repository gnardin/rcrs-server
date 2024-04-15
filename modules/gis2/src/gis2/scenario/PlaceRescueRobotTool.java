package gis2.scenario;

import javax.swing.undo.AbstractUndoableEdit;

import maps.gml.GMLShape;

/**
 * Tool for placing rescue robots.
 */
public class PlaceRescueRobotTool extends ShapeTool{
    /**
     * Construct a PlaceRescueRobotTool
     * 
     * @param editor The editor instance
     */
    public PlaceRescueRobotTool(ScenarioEditor editor) {
        super(editor);
    }

    @Override
    public String getName() {
        return "Place rescue robot";
    }

    @Override
    protected boolean shouldHighlight(GMLShape shape) {
        return true;
    }

    @Override
    protected void processClick(GMLShape shape) {
        editor.getScenario().addRescueRobot(0);
        editor.setChanged();
        editor.updateOverlays();
        editor.addEdit(new addRescueRobotEdit(shape.getID()));
    }

    private class addRescueRobotEdit extends AbstractUndoableEdit {
        private int id;

        public addRescueRobotEdit(int id) {
            this.id = id;
        }

        @Override
        public void undo() {
            super.undo();
            editor.getScenario().removeRescueRobot(id);
            editor.updateOverlays();
        }

        @Override
        public void redo() {
            super.redo();
            editor.getScenario().addRescueRobot(id);
            editor.updateOverlays();
        }
    }
}
