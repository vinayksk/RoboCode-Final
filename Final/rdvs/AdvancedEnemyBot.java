package rdvs;

import robocode.*;


/**
 * Record the advanced state of an enemy bot.
 * 
 * @author Vinay Senthil
 * @version 5/12/16
 * 
 * @author Period - 3
 * @author Assignment - AdvancedEnemyBot
 * 
 * @author Sources - None
 */
public class AdvancedEnemyBot extends EnemyBot
{
    private double x;

    private double y;


    /**
     * resets the existing variables
     */
    public AdvancedEnemyBot()
    {
        reset();
    }


    /**
     * gets the x of the robot
     * 
     * @return x of enemy
     */
    public double getX()
    {
        return x; // Fix this!!
    }


    /**
     * gets the x of the robot
     * 
     * @return y of enemy
     */
    public double getY()
    {
        return y; // Fix this!!
    }


    /**
     * updates the enemy event
     * 
     * @param e
     *            event
     * @param robot
     *            enemy robot
     */
    public void update( ScannedRobotEvent e, Robot robot )
    {
        super.update( e );
        double absBearingDeg = ( robot.getHeading() + e.getBearing() );
        if ( absBearingDeg < 0 ) {
            absBearingDeg += 360;
        }
        // yes, you use the _sine_ to get the X value because 0 deg is North
        x = robot.getX() + Math.sin( Math.toRadians( absBearingDeg ) ) 
            * e.getDistance();
        // yes, you use the _cosine_ to get the Y value because 0 deg is North
        y = robot.getY() + Math.cos( Math.toRadians( absBearingDeg ) ) 
            * e.getDistance();
    }


    /**
     * returns the predicted x
     * 
     * @param when
     *            the time
     * @return x of enemy robot
     */
    public double getFutureX( long when )
    {

        return x + Math.sin( Math.toRadians( getHeading() ) ) 
            * getVelocity() * when;
        
    }


    /**
     * returns the predicted y
     * 
     * @param when
     *            time
     * @return y of enemy robot
     */
    public double getFutureY( long when )
    {

        return y + Math.cos( Math.toRadians( getHeading() ) ) 
            * getVelocity() * when;
        
    }
    


    /**
     * (non-Javadoc)
     * 
     * @see rdvs.EnemyBot#reset()
     */
    public void reset()
    {
        super.reset();
    }

}