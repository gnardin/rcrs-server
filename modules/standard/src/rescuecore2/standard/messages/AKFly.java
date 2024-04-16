package rescuecore2.standard.messages;

import java.io.InputStream;
import java.io.IOException;
import java.util.List;

import org.json.JSONObject;

import rescuecore2.messages.AbstractCommand;
import rescuecore2.messages.components.EntityIDListComponent;
import rescuecore2.messages.components.IntComponent;
import rescuecore2.messages.protobuf.RCRSProto.MessageProto;
import rescuecore2.worldmodel.EntityID;


public class AKFly {
    
    private EntityIDListComponent path;
    private IntComponent x;
    private IntComponent y;

    public AKFly(InputStream in) throws IOException {
        this();
        read(in);
    }

    /** 
     * Construct a fly command for the drone
     * 
     * @param time time the command was issued
     * @param agent the id of the agent issuing the command
     * @param path the path to fly
     */
    public AKFly(EntityID agent, int time, List<EntityID> path) {
        this();
        setAgentID(agent);
        setTime(time);
        this.path.setIDs(path);
        this.x.setValue(-1);
        this.y.setValue(-1);
    }

    /**
     * Construct a fly command
     * 
     * @param time time command was issued 
     * @param agent the id of the agent issuing the command
     * @param path the path to fly
     * @param destinationX x coordinate of desired destination
     * @param destinationY y coordinate of desired destination
     */
    public AKFly(EntityID agent, int time, List<EntityID> path, int destinationX, int destinationY) {
        this();
        setAgentID(agent);
        setTime(time);
        this.path.setIDs(path);
        this.x.setValue(destinationX);
        this.y.setValue(destinationY);
    }

    public AKMove(MessageProto proto) {
        //this();
        fromMessageProto(proto);
    }

    /**
     * Get the desired path 
     * @return the flight path of the drone
     */
    public List<EntityID> getPath() {
        return path.getIDs();
    }

    /**
     * Get the destination X coor
     * @return x coordinate
     */
    public int getDestinationX() {
        return x.getValue();
    }

      /**
     * Get the destination Y coor
     * @return x coordinate
     */
    public int getDestinationY() {
        return y.getValue();
    }

    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = super.toJson();
        jsonObject.put("Path", this.getPath());
        jsonObject.put("X", this.getDestinationX());
        jsonObject.put("Y", this.getDestinationY());
    }
}
