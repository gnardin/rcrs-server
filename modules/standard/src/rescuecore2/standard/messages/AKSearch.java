package rescuecore2.standard.messages;

import java.io.InputStream;
import java.io.IOException;

import org.json.JSONObject;

import rescuecore2.messages.AbstractCommand;
import rescuecore2.messages.components.EntityIDComponent;
import rescuecore2.messages.protobuf.RCRSProto.MessageProto;
import rescuecore2.worldmodel.EntityID;

public class AKSearch extends AbstractCommand {

    private EntityIDComponent target;

    /**
     * An AKSearch message that populates its data from a stream
     *
     * @param in The InputStream to read
     * @throws IOException if there is a problem readin the stream
     */
    public AKSearch(InputStream in) throws IOException {
        this();
        read(in);
    }

    /**
     * Construct a search command
     *
     * @param agent
     * @param time
     */
    public AKSearch(EntityID agent, int time) {
        this();
        setAgentID(agent);
        setTime(time);
    }

    private AKSearch() {
        super(StandardMessageURN.AK_SEARCH);
    }

    public AKSearch(MessageProto proto) {
        this();
        fromMessageProto(proto);
    }

}
