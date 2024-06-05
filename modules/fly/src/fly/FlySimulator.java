package fly;

import rescuecore2.messages.control.KSCommands;
import rescuecore2.standard.components.StandardSimulator;
import rescuecore2.worldmodel.ChangeSet;

public class FlySimulator extends StandardSimulator {

    private static  final String SIMULATOR_NAME = "Drone Fly Simulator";

    private static final String FLIGHT_HEIGHT = "";
    private static final String FLIGHT_LANDING_SPEED = "";
    private static final String FLIGHT_TAKEOFF_SPEED = "";

    @Override
    protected void postConnect() {

    }

    @Override
    protected void processCommands(KSCommands c, ChangeSet changes) {

    }



}
