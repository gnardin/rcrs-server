package gis2.scenario;

import javax.swing.undo.AbstractUndoableEdit;

import maps.gml.GMLShape;

/**
 * This is the tool for removing rescue robots from the map.
 */
public class RemoveRescueRobotTool extends ShapeTool{
    /**
     * Construct a RemoveRescueRobot tool.
     * 
     * @param editor The editor instance.
     */
    public RemoveRescueRobotTool(ScenarioEditor editor) {
        super(editor);
    }

    @Override
    public String getName() {
        return "Remove rescue robot";
    }

    @Override
    protected boolean shouldHighlight(GMLShape shape) {
        return true;
    }

    @Override
    protected void processClick(GMLShape shape) {
        editor.getScenario().removeRescueRobot(shape.getID());
        editor.setChanged();
        editor.updateOverlays();
        editor.addEdit(new RemoveRescueRobotEdit(shape.getID()));
    }

    private class RemoveRescueRobotEdit extends AbstractUndoableEdit {
        private final int id;

        public RemoveRescueRobotEdit(int id) {
            this.id = id;
        }

        @Override
        public void undo() {
            super.undo();
            editor.getScenario().addRescueRobot(id);
            editor.updateOverlays();
        }

        @Override
        public void redo() {
            super.redo();
            editor.getScenario().removeRescueRobot(id);
            editor.updateOverlays();
        }
    }
    
}
