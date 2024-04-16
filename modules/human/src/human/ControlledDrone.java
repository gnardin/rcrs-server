package human;

import static rescuecore2.misc.Handy.objectsToIDs;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.messages.Command;
import rescuecore2.log.Logger;

import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.components.StandardAgent;

import sample.SampleSearch;

import java.util.Collection;
import java.util.List;
import java.util.EnumSet;


public class ControlledDrone extends StandardAgent<Drone>{
    private SampleSearch search;
    private Human target;

    /** 
     * Set the target of this drone
     * @param target The new target
     */
    public void setTarget(Human target) {
        this.target = target;
    }

    @Override
    public void think(int time, ChangeSet changed, Collection<Command> heard) {
        
    }

    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.DRONE);
    }
        
    /**
    * Get the location of the entity controlled by this agent
    @return The location of the entity 
    */
    protected StandardEntity location() {
        AmbulanceTeam me = me();
        return me.getPosition(model);
    }

    @Override
    protected void postConnect() {
        super.connect();
        search = new SampleSearch(model);
    }

    @Override
    public String toString() {
        if (me() == null) {
            return "Human controlled drone";
        }
        StringBuilder result = new StringBuilder();
        result.append("Human controlled drone");
        result.append(getID());
        result.append(" ");
        if (target == null) {
            result.append("no target");
        } else {
            result.append("target: human");
            result.append(target.getID());
        }
        return result.toString();
    }

}
