package human;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.messages.Command;
import rescuecore2.misc.Pair;
import rescuecore2.log.Logger;

import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.RescueRobot;
import rescuecore2.standard.components.StandardAgent;

import sample.SampleSearch;

import java.util.Collection;
import java.util.List;

import javax.swing.text.html.parser.Entity;

import java.util.EnumSet;


public class ControlledRescueRobot extends StandardAgent<RescueRobot> {
    private SampleSearch search;
    private Human target;
    
    /**
    * Set the target of this rescue robot     
    * @param target The new target
    */    
    public void setTarget(Human target) {
        this.target = target;
    }

    @Override
    protected void think(int time, ChangeSet changed, Collection<Command> heard) {
        if (location() instanceof Road) {
            Road road = (Road)location();
            EntityID nearest = getNearestBlockade();
            if(nearest != null) {
                sendClear(time, nearest);
                return;
            }
        }
        if (target == null) {
            Logger.info("Nothing to do");
            return;
        }
        List<EntityID> path = search.breadthFirstSearch(me().getPosition(), target.getID());
        if (path != null) {
            sendMove(time, path);
            return;
        } else {
            Logger.info("Couldn't plan a path to target");
        }
    }

    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.RESCUE_ROBOT);
    }

    /**
     * Get the location of the entity controlled by this agent
     * @return The location of the entity
     */
    protected StandardEntity location() {
        RescueRobot me = me();
        return me.getPosition(model);
    }

    @Override
    protected void postConnect() {
        super.postConnect();
        search = new SampleSearch(model);
    }

    @Override
    public String toString() {
        if(me() == null) {
            return "Human controlled rescue robot";
        }
        return "Human controlled police force " + getID() + (target == null ? " (no target)" : " target: road " + target.getID() + " with " + (target.isBlockadesDefined() ? " unknown" : String.valueOf(target.getBlockades().size())) + " blockades");
    }

    /**
     * Get the blockade nearest to that agent
     * @return The Entity ID of the nearest blockade, or null if there are no blockades in the agents current location.
     */
    public EntityID getNearestBlockade() {
        return getNearestBlockade((Area)location(), me().getX(), me().getY());
    }

     /**
       Get the blockade that is nearest a point.
       @param area The area to check.
       @param x The X coordinate to look up.
       @param y The X coordinate to look up.
       @return The EntityID of the nearest blockade, or null if there are no blockades in this area.
    */
    public EntityID getNearestBlockade(Area area, int x, int y) {
        double bestDistance = 0;
        EntityID best = null;
        if (area.isBlockadesDefined()) {
            for (EntityID blockadeID : area.getBlockades()) {
                StandardEntity entity = model.getEntity(blockadeID);
                Pair<Integer,Integer> location = entity.getLocation(model);
                if(location == null) {
                    continue;
                }
                double dx = location.first() - x;
                double dy = location.second() - y;
                double distance = Math.hypot(dx, dy);
                if (best == null || distance < bestDistance) {
                    bestDistance = distance;
                    best = entity.getID();
                }
            }
        }
        return best;
    }
    
}
