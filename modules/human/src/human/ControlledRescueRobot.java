package human;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.messages.Command;
import rescuecore2.misc.Pair;
import rescuecore2.log.Logger;

import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.RescueRobot;
import rescuecore2.standard.components.StandardAgent;

import sample.SampleSearch;

import java.util.Collection;
import java.util.List;
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
   
    }
    
}
