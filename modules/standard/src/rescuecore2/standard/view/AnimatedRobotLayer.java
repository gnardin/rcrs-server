package rescuecore2.standard.view;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Queue;
import java.util.LinkedList;

import rescuecore.Agent;
import rescuecore2.misc.Pair;
import rescuecore2.config.Config;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.standard.misc.AgentPath;


public class AnimatedRobotLayer extends RobotLayer {
    private Set<EntityID> robotIDs;

    private Map<EntityID, Queue<Pair<Integer, Integer>>> frames;
    private boolean animationDone;

    /**
     * Construct an animated robot view layer.
     */
    public AnimatedRobotLayer() {
        robotIDs = new HashSet<EntityID>();
        frames = new HashMap<EntityID, Queue<Pair<Integer, Integer>>>();
        animationDone = true;
    }

    @Override
    public void initialise(Config config) {
        super.initialise(config);
        robotIDs.clear();
        synchronized (this) {
            frames.clear();
            animationDone = true;
        }
    }

    @Override
    public String getName() {
        return "Robots (animated)";
    }

    /**
     * Increase the frame number.
     * @return True if a new frame is required
     */
    public boolean nextFrame() {
        synchronized (this) {
            if( animationDone ) {
                return false;
            }
            animationDone = true;
            for (Queue<Pair<Integer, Integer>> next : frames.values()) {
                if (next.size() > 0) {
                    next.remove();
                    animationDone = false;
                }
            }
            return !animationDone;
        }
    }

    @Override
    protected Pair<Integer, Integer> getLocation(Robot robot) {
        synchronized (this) {
            Queue<Pair<Integer, Integer>> robotFrames = frames.get(robot.getID());
            if (robotFrames != null && !robotFrames.isEmpty()) {
                return robotFrames.peek();
            }
        }
        return robot.getLocation(world);
    }

    @Override
    protected void preView() {
        super.preView();
        robotIDs.clear();
    }

    @Override
    protected void viewObject(Object obj) {
        super.viewObject(obj);
        if (obj instanceof  Robot) {
            robotIDs.add(((Robot)obj).getID());
        }
    }

    /**
     * Compute the animation frames
     * @param frameCount The number of animation frames to compute.
     */
//    void computeAnimation(int frameCount) {
//        synchronized (this) {
//            frames.clear();
//            //compute animation
//            double step = 1.0 / (frameCount - 1.0);
//            for (EntityID next : robotIDs) {
//                Queue<Pair<Integer, Integer>> result = new LinkedList<>();
//                Robot robot = (Robot) world.getEntity(next);
//                if (robot == null) {
//                    continue;
//                }
//                AgentPath path;
//                StandardEntity position = world.getEntity(robot.getPosition());
//                if (position instanceof Drone) {
//                    path = AgentPath.computePath((Drone)position, world);
//                } else {
//                    path = AgentPath.computePath(robot, world);
//                }
//                if (path == null) {
//                    continue;
//                }
//                for (int i = 0; i < frameCount; ++i) {
//                    Pair<Integer, Integer> nextPoint = path.getPointOnPath(i + step);
//                    result.add(nextPoint);
//                }
//                frames.put(next, result);
//            }
//            animationDone = false;
//        }
//    }


}
