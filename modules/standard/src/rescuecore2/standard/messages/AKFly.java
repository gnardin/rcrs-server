package rescuecore2.standard.messages;

import java.io.InputStream;
import java.io.IOException;
import java.util.List;

import org.json.JSONObject;

import rescuecore2.messages.AbstractCommand;
import rescuecore2.messages.components.EntityIDListComponent;
import rescuecore2.messages.components.IntComponent;
import rescuecore2.messages.protobuf.RCRSProto.MessageProto;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;


public class AKFly extends AbstractCommand {

    //private IntComponent height;
    private EntityIDListComponent path;
    private IntComponent x;
    private IntComponent y;



    /**
     * An AKFly message that populates its data from a stream.
     *
     * @param in The InputStream to read.
     * @throws IOException If there is a problem reading the stream.
     */
    public AKFly(InputStream in) throws IOException {
        this();
        read(in);
    }

    /**
     * Construct a fly command
     *
     * @param time The time the command was issued
     * @param agent The ID of the agent issuing the command
     * @param path The drone's path to fly.
     *
     */
    public AKFly(EntityID agent, int time, List<EntityID> path) {
        this();
        setAgentID(agent);
        setTime(time);
        //
        this.path.setIDs(path);
        this.x.setValue(-1);
        this.y.setValue(-1);
    }

    /**
     * Construct a fly command.
     *
     * @param time
     * @param agent
     * @param path
     * @param destinationX
     * @param destinationY
     * @param height
     */
    public AKFly(EntityID agent, int time, List<EntityID> path,
                 int destinationX, int destinationY) {
        this();
        setAgentID(agent);
        setTime(time);
        this.path.setIDs(path);
//        this.height.setValue(height);
        this.x.setValue(destinationX);
        this.y.setValue(destinationY);
    }

    private AKFly() {
        super(StandardMessageURN.AK_FLY);
        path = new EntityIDListComponent(StandardMessageComponentURN.Path);
        x = new IntComponent(StandardMessageComponentURN.DestinationX);
        y = new IntComponent(StandardMessageComponentURN.DestinationY);
//        height = new IntComponent(StandardMessageComponentURN.Height);
        addMessageComponent(path);
//        addMessageComponent(height);
        addMessageComponent(x);
        addMessageComponent(y);
    }

    public AKFly(MessageProto proto) {
        this();
        fromMessageProto(proto);
    }


    /**
     * Get the desired flight path.
     *
     * @return The flight path.
     */
    public List<EntityID> getPath() {
        return path.getIDs();
    }

    /**
     * Get the height of the drone.
     *
     * @return The height of the drone.
     */
//    public int getHeight() {
//        return height.getValue();
//    }

    /**
     * Get the x coordinate destination of the drone.
     *
     * @return The destination X coordinate
     */
    public int getDestinationX() {
        return x.getValue();
    }


    /**
     * Get the Y coordinate destination of the drone.
     *
     * @return The destination Y coordinate
     */
    public int getDestinationY() {
        return y.getValue();
    }

    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = super.toJson();
        jsonObject.put("Path", this.getPath());
        jsonObject.put("X:", this.getDestinationX());
        jsonObject.put("Y:", this.getDestinationY());
//        jsonObject.put("Height", this.getHeight());
        return jsonObject;
    }


}
