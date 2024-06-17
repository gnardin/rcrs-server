package rescuecore2.standard.view;

import rescuecore2.config.Config;
import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.standard.entities.Robot;

import java.awt.*;

public class PositionHistoryDroneLayer extends StandardEntityViewLayer<Robot> {
    private static final Color FLIGHT_PATH_COLOUR = Color.RED;

    /**
     * Construct a position history layer
     */
    public PositionHistoryDroneLayer() {
        super(Robot.class);
    }

    @Override
    public void initialise(Config config) {

    }

    @Override
    public String getName() {
        return "Position history for drone";
    }

    @Override
    public Shape render(Robot r, Graphics2D g, ScreenTransform t) {
        if (!r.isPositionHistoryDefined()) {
            return null;
        }
        int[] history = r.getPositionHistory();
        if (history.length < 4) {
            return null;
        }
        g.setColor(FLIGHT_PATH_COLOUR);
        int x = t.xToScreen(history[0]);
        int y = t.yToScreen(history[1]);
        for (int i = 2; i < history.length; i += 2) {
            int x2 = t.xToScreen(history[i]);
            int y2 = t.yToScreen(history[i + 1]);
            g.drawLine(x, y, x2, y2);
            x = x2;
            y = y2;
        }
        return null;
    }
}
