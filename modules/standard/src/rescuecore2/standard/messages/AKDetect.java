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

/**
 * An agent detect command
 */
public class AKDetect extends AbstractCommand {

    private EntityIDListComponent path;
    private IntComponent x;
    private IntComponent y;

    /**
     * An AKDetect message that populates its data from a stream
     */
    public AKDetect(InputStream in) throws IOException {
        this();
        read(in);
    }

    /**
     * Construct a detect command that allows the agent to move around to detect civilians
     * 
     * @param time the time it was issued 
     * @param agent the id of the agent issuing the command
     * @param target the id of the entity to detect
     */
    public AKDetect(EntityID agent, int time, EntityID target) {
        this();
        setAgentID(agent);
        setTime(time);
        this.target.setValue(target);
    }

    private AKDetect() {
        super(StandardMessageURN.AK_DETECT);
        target = new EntityIDComponent(StandardMessageComponentURN.Target);
        addMessageComponent(target);
    }

    public AKDetect(MessageProto proto) {
        //this();
        fromMessageProto(proto);
    }

    /** 
     * Get the desired civilian
     * 
     * @return the target id
     */
    public EntityID getTarget() {
        return target.getValue();
    }

    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = super.toJson();
        jsonObject.put("Target", this.getTarget());
        return jsonObject;
    }

}
