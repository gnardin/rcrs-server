package rescuecore2.standard.entities;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;

public class RescueRobot extends Human {

    /**
     * Construct a RescueRobot object with entirely undefined values.
     * 
     * @param id
     */
    public RescueRobot( EntityID id ) {
        super( id );
    }

    /**
     * RescueRobot copy constructor.
     * 
     * @param other
     */
    public RescueRobot( RescueRobot other ) {
        super( other );
    }

    @Override
    protected Entity copyImpl() {
        return new RescueRobot( getID() );
    }

    @Override
    public StandardEntityURN getStandardURN() {
        return StandardEntityURN.RESCUE_ROBOT;
    }

    @Override
    protected String getEntityName() {
        return "RescueRobot";
    }
    
}
