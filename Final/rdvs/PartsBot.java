package rdvs;

import robocode.*;
import java.awt.Color;
import java.awt.geom.Point2D;

/**
 *  Introduction to Java
 *
 *  @author  Vinay
 *  @version May 17, 2016
 *  @author  Period: 3
 *  @author  Assignment: Robo05PartsBot
 *
 *  @author  Sources: None
 */
public class PartsBot extends AdvancedRobot
{
    private AdvancedEnemyBot enemy = new AdvancedEnemyBot();

    private RobotPart[] parts = new RobotPart[3]; // make three parts

    private final static int RADAR = 0;

    private final static int GUN = 1;

    private final static int TANK = 2;


    /**
     * (non-Javadoc)
     * 
     * @see robocode.Robot#run()
     */
    public void run()
    {
        parts[RADAR] = new Radar();
        parts[GUN] = new Gun();
        parts[TANK] = new Tank();

        // initialize each part
        for ( int i = 0; i < parts.length; i++ )
        {
            // behold, the magic of polymorphism
            parts[i].init();
        }

        // iterate through each part, moving them as we go
        for ( int i = 0; true; i = ( i + 1 ) % parts.length )
        {
            // polymorphism galore!
            parts[i].move();
            if ( i == 0 )
            {
                execute();
            }
        }
    }


    /**
     * (non-Javadoc)
     * 
     * @see robocode.Robot#onScannedRobot(robocode.ScannedRobotEvent)
     * @param e
     *            the incoming information
     */
    public void onScannedRobot( ScannedRobotEvent e )
    {
        Radar radar = (Radar)parts[RADAR];
        if ( radar.shouldTrack( e ) )
        {
            enemy.update( e, this );
        }
    }


    /**
     * (non-Javadoc)
     * 
     * @see robocode.Robot#onRobotDeath(robocode.RobotDeathEvent)
     * @param e
     *            the incoming information
     */
    public void onRobotDeath( RobotDeathEvent e )
    {
        Radar radar = (Radar)parts[RADAR];
        if ( radar.wasTracking( e ) )
        {
            enemy.reset();
        }

    }


    /**
     * Computes the absolute bearing between two points
     *
     * @param x1
     *            the first point's x
     * @param y1
     *            the first point's y
     * @param x2
     *            the second point's x
     * @param y2
     *            the second point's y
     * @return the absolute bearing
     */
    public double absoluteBearing( double x1, double y1, double x2, double y2 )
    {
        double xo = x2 - x1;
        double yo = y2 - y1;
        double hyp = Point2D.distance( x1, y1, x2, y2 );
        double arcSin = Math.toDegrees( Math.asin( xo / hyp ) );
        double bearing = 0;

        if ( xo > 0 && yo > 0 )
        { // both pos: lower-Left
            bearing = arcSin;
        }
        else if ( xo < 0 && yo > 0 )
        { // x neg, y pos: lower-right
            bearing = 360 + arcSin; // arcsin is negative here, actually 360
                                    // -
                                    // ang
        }
        else if ( xo > 0 && yo < 0 )
        { // x pos, y neg: upper-left
            bearing = 180 - arcSin;
        }
        else if ( xo < 0 && yo < 0 )
        { // both neg: upper-right
            bearing = 180 - arcSin; // arcsin is negative here, actually 180
                                    // +
                                    // ang
        }

        return bearing;
    }


    /**
     * Normalizes a bearing to between +180 and -180
     *
     * @param angle
     *            the angle that needs to be normalized
     * @return the normalized bearing
     */
    public double normalizeBearing( double angle )
    {
        while ( angle > 180 )
        {
            angle -= 360;
        }
        while ( angle < -180 )
        {
            angle += 360;
        }
        return angle;
    }


    /**
     * The inner classes
     */
    public interface RobotPart
    {
        /**
         * Radar code
         */
        public void init();


        /**
         * Movement code
         */
        public void move();
    }


    /**
     * The class for the radar
     */
    public class Radar implements RobotPart
    {
        /**
         * (non-Javadoc)
         * 
         * @see rdvs.PartsBot.RobotPart#init()
         */
        public void init()
        {
            setAdjustRadarForGunTurn( true );
        }


        /**
         * (non-Javadoc)
         * 
         * @see rdvs.PartsBot.RobotPart#move()
         */
        public void move()
        {
            double radarDirection = 0.0;
            double turn = getHeading() - getRadarHeading() + enemy.getBearing();
            turn += 30 * radarDirection;
            setTurnRadarRight( turn );
            radarDirection *= -1;
        }


        /**
         * Scans for the track
         * 
         * @param e
         *            the event
         * @return angle and distance away
         */
        public boolean shouldTrack( ScannedRobotEvent e )
        {
            // track if we have no enemy, the one we found is significantly
            // closer, or we scanned the one we've been tracking.
            return ( enemy.none() || e.getDistance() < enemy.getDistance() - 70
                || e.getName().equals( enemy.getName() ) );
        }


        /**
         * Tracking
         * 
         * @param e
         *            event
         * @return the name
         */
        public boolean wasTracking( RobotDeathEvent e )
        {
            return e.getName().equals( enemy.getName() );
        }
    }


    /**
     * The class for the gun
     */
    public class Gun implements RobotPart
    {
        /**
         * (non-Javadoc)
         * 
         * @see rdvs.PartsBot.RobotPart#init()
         */
        public void init()
        {
            setAdjustGunForRobotTurn( true );
        }


        /**
         * (non-Javadoc)
         * 
         * @see rdvs.PartsBot.RobotPart#move()
         */
        public void move()
        {
            // don't shoot if I've got no enemy
            if ( enemy.none() )
            {
                return;
            }

            // calculate firepower based on distance
            double firePower = Math.min( 500 / enemy.getDistance(), 3 );
            // calculate speed of bullet
            double bulletSpeed = 20 - firePower * 3;
            // distance = rate * time, solved for time
            long time = (long)( enemy.getDistance() / bulletSpeed );

            // calculate gun turn to predicted x,y location
            double futureX = enemy.getFutureX( time );
            double futureY = enemy.getFutureY( time );
            double absDeg = absoluteBearing( getX(), getY(), futureX, futureY );
            // non-predictive firing can be done like this:
            // double absDeg = absoluteBearing(getX(), getY(), enemy.getX(),
            // enemy.getY());

            // turn the gun to the predicted x,y location
            setTurnGunRight( normalizeBearing( absDeg - getGunHeading() ) );

            // if the gun is cool and we're pointed in the right direction,
            // shoot!
            if ( getGunHeat() == 0 && Math.abs( getGunTurnRemaining() ) < 10 )
            {
                setFire( firePower );
            }
        }

    }


    /**
     * The class for the tank
     */
    public class Tank implements RobotPart
    {
        private int moveDirection = 1;


        /**
         * (non-Javadoc)
         * 
         * @see rdvs.PartsBot.RobotPart#init()
         */
        public void init()
        {
            setColors( Color.pink, Color.red, Color.cyan );
        }


        /**
         * (non-Javadoc)
         * 
         * @see rdvs.PartsBot.RobotPart#move()
         */
        public void move()
        {
            setTurnRight( enemy.getBearing() + 90 );

            // strafe by changing direction every 20 ticks
            if ( getTime() % 20 == 0 )
            {
                moveDirection *= -1;
                setAhead( 150 * moveDirection );
            }
        }
    }
}