package rdvs;

import robocode.*;


/**
 * Record the state of an enemy bot.
 * 
 * @author Vinay Senthil
 * @version 5/10/16
 * 
 * @author Period - 3
 * @author Assignment - EnemyBot
 * 
 * @author Sources - None
 */


public class EnemyBot
{
    private double bearing;

    private double distance;

    private double energy;

    private double heading;

    private double velocity;

    private String name;


    /**
     * reset
     */
    public EnemyBot()
    {
        reset();
    }


    /**
     * get bearing
     * @return bearing
     */
    public double getBearing()
    {
        return bearing;
    }


    /**
     * get distance
     * @return distance
     */
    public double getDistance()
    {
        return distance;
    }


    /**
     * get energy
     * @return energy
     */
    public double getEnergy()
    {
        return energy;
    }


    /**
     * get heading
     * @return heading
     */
    public double getHeading()
    {
        return heading;
    }


    /**
     * get velocity
     * @return velocity
     */
    public double getVelocity()
    {
        return velocity;
    }


    /**
     * get name
     * @return name
     */
    public String getName()
    {
        return name;
    }


    /**
     * updates enemy
     * @param srEvt the new enemy
     */
    public void update( ScannedRobotEvent srEvt )
    {
        bearing = srEvt.getBearing();
        distance = srEvt.getDistance();
        energy = srEvt.getEnergy();
        heading = srEvt.getHeading();
        velocity = srEvt.getVelocity();
        name = srEvt.getName();

    }


    /**
     * reset
     */
    public void reset()
    {
        bearing = 0.0;
        distance = 0.0;
        energy = 0.0;
        heading = 0.0;
        velocity = 0.0;
        name = "";
    }


    /**
     * checks the robot name
     * @return name is false or true
     */
    public boolean none()
    {
        return name.length() == 0;
    }
}