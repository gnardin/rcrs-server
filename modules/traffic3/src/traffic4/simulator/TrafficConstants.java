package traffic4.simulator;

import org.uncommons.maths.random.ContinuousUniformGenerator;
import org.uncommons.maths.number.NumberGenerator;
import rescuecore2.config.Config;

public class TrafficConstants {
    // agent force constants
    private static double NUDGE_MAGNITUDE = 0.001;
    private static double DRONE_FORCE_COEFFICIENT_A = 0.0001;
    private static double DRONE_FORCE_COEFFICIENT_B = 0.001;
    private static double DRONE_FORCE_COEFFICIENT_C = 0.00001;
    private static double DRONE_DISTANCE_CUTOFF = 10000;
    private static double DRONE_FORCE_LIMIT = 0.0001;
    private static NumberGenerator<Double> nudge;

    // ground force constants
    private static double GROUND_DISTANCE_CUTOFF = 3000;
    private static double GROUND_FORCE_COEFFICIENT_A = 0.01;
    private static double GROUND_F0RCE_COEFFICIENT_B = 0.7;

    // wall force constants
    private static double  W_DISTANCE_CUTOFF = 3000;
    private static double W_FORCE_COEFFICIENT_A = 0.01;
    private static double W_FORCE_COEFFICIENT_B = 0.7;

    private TrafficConstants() {
    }

    static void init(Config config) {
        NUDGE_MAGNITUDE = config.getFloatValue("traffic4.nudge-magnitude", NUDGE_MAGNITUDE);
        DRONE_FORCE_COEFFICIENT_A = config.getFloatValue("traffic4.drone.force.coefficient.a", DRONE_FORCE_COEFFICIENT_A);
        DRONE_FORCE_COEFFICIENT_B = config.getFloatValue("traffic4.drone.force.coefficient.b", DRONE_FORCE_COEFFICIENT_B);
        DRONE_FORCE_COEFFICIENT_C = config.getFloatValue("traffic4.drone.force.coefficient.c", DRONE_FORCE_COEFFICIENT_C);
        DRONE_DISTANCE_CUTOFF = config.getFloatValue("traffic4.drone.distance.cutoff", DRONE_DISTANCE_CUTOFF);
        DRONE_FORCE_LIMIT = config.getFloatValue("traffic4.drone.force.limit", DRONE_FORCE_LIMIT);
        W_DISTANCE_CUTOFF = config.getFloatValue("traffic4.wall.distance.cutoff", W_DISTANCE_CUTOFF);
        W_FORCE_COEFFICIENT_A = config.getFloatValue("traffic4.wall.force.coefficient.a", W_FORCE_COEFFICIENT_A);
        W_FORCE_COEFFICIENT_B = config.getFloatValue("traffic4.wall.force.coefficient.b", W_FORCE_COEFFICIENT_B);
        GROUND_DISTANCE_CUTOFF = config.getFloatValue("traffic4.ground.distance.cutoff", GROUND_DISTANCE_CUTOFF);
        GROUND_F0RCE_COEFFICIENT_B = config.getFloatValue("traffic4.ground.force.coefficient.b", GROUND_F0RCE_COEFFICIENT_B);
        GROUND_FORCE_COEFFICIENT_A = config.getFloatValue("traffic4.ground.force.coefficient.a", GROUND_FORCE_COEFFICIENT_A);
        nudge = new ContinuousUniformGenerator(-NUDGE_MAGNITUDE, NUDGE_MAGNITUDE, config.getRandom());
    }

    /**
     Get a (randomised) nudge for agents that are co-located.
     @return A random nudge force.
     */
    public static double getColocatedAgentNudge() {
        return nudge.nextValue();
    }

    /**
     Get the maximum distance at which drones affect each other.
     @return The drone force distance cutoff.
     */
    public static double getDroneDistanceCutoff() {
        return DRONE_DISTANCE_CUTOFF;
    }

    /**
     Get the drone force function coefficient "A".
     @return The drone force function coefficient "A".
     */
    public static double getDroneForceCoefficientA() {
        return DRONE_FORCE_COEFFICIENT_A;
    }

    /**
     Get the agent force function coefficient "B".
     @return The agent force function coefficient "B".
     */
    public static double getDroneForceCoefficientB() {
        return DRONE_FORCE_COEFFICIENT_B;
    }

    /**
     Get the drone force function coefficient "C".
     @return The drone force function coefficient "C".
     */
    public static double getDroneForceCoefficientC() {
        return DRONE_FORCE_COEFFICIENT_C;
    }

    /**
     Get the maximum total drone force.
     @return The maximum total drone force.
     */
    public static double getDroneForceLimit() {
        return DRONE_FORCE_LIMIT;
    }

    /**
     Get the maximum distance at which walls affect agents.
     @return The wall force distance cutoff.
     */
    public static double getWallDistanceCutoff() {
        return W_DISTANCE_CUTOFF;
    }

    /**
     Get the wall force function coefficient "A".
     @return The wall force function coefficient "A".
     */
    public static double getWallForceCoefficientA() {
        return W_FORCE_COEFFICIENT_A;
    }

    /**
     Get the wall force function coefficient "B".
     @return The wall force function coefficient "B".
     */
    public static double getWallForceCoefficientB() {
        return W_FORCE_COEFFICIENT_B;
    }

    /**
     Get the maximum height at which the ground affects drones.
     @return The ground force distance cutoff
     */
    public static double getGroundDistanceCutoff() {
        return GROUND_DISTANCE_CUTOFF;
    }

    /**
     Get the ground force function coefficient "A"
     @return The ground force function coefficient A
     */
    public static double getGroundForceCoefficientA() {
        return GROUND_FORCE_COEFFICIENT_A;
    }

    /**
     Get the ground force function coefficient "B"
     @return The ground force function coefficient B
     */
    public static double getGroundF0rceCoefficientB() {
        return GROUND_F0RCE_COEFFICIENT_B;
    }








}
