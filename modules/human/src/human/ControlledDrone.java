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
        if(target != null) {
            Logger.info("Nothing to do.");
            return;
        }
        else {
            
        }
    }
}
