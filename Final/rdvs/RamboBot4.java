package rdvs;

import robocode.*;
import robocode.util.Utils;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;


public class RamboBot4 extends AdvancedRobot
{
    private AdvancedEnemyBot enemy = new AdvancedEnemyBot();

    private RobotPart[] parts = new RobotPart[3]; // make three parts

    private final static int RADAR = 0;

    private final static int GUN = 1;

    private final static int TANK = 2;

    static double enemyEnergy;

    static double direction;

    static double oldEnemyHeading;

    private boolean tooCloseToWall = false;

    static double counter = 1;


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
        //gets the on cann
        Radar radar = (Radar)parts[RADAR];
        if ( radar.shouldTrack( e ) )
        {
            enemy.update( e, this );
        }
    }


    public void OnHitWall( HitWallEvent evnt )
    {

        if ( !tooCloseToWall )
        {
            tooCloseToWall = true;
            direction *= -1;
            setAhead( 10000 * direction );

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
         * @see RamboBot.PartsBot.RobotPart#init()
         */
        public void init()
        {
            //color change
            setAdjustRadarForGunTurn( true );
            setColors( Color.blue, Color.pink, Color.green );
        }


        /**
         * (non-Javadoc)
         * 
         * @see RamboBot.PartsBot.RobotPart#move()
         */
        public void move()
        {
            // Absolute angle towards target
            double angleToEnemy = getHeadingRadians() + Math.toRadians( enemy.getBearing() );

            // Subtract current radar heading to get the turn required to face
            // the enemy, be sure it is normalized
            double radarTurn = Utils.normalRelativeAngle( angleToEnemy - getRadarHeadingRadians() );

            // Distance we want to scan from middle of enemy to either side
            // The 36.0 is how many units from the center of the enemy robot it
            // scans.
            double extraTurn = Math.min( Math.atan( 36.0 / enemy.getDistance() ), Rules.RADAR_TURN_RATE_RADIANS );

            // Adjust the radar turn so it goes that much further in the
            // direction it is going to turn
            // Basically if we were going to turn it left, turn it even more
            // left, if right, turn more right.
            // This allows us to overshoot our enemy so that we get a good sweep
            // that will not slip.
            radarTurn += ( radarTurn < 0 ? -extraTurn : extraTurn );

            // Turn the radar
            setTurnRadarRightRadians( radarTurn );
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
            //the current robot event that is being tracked
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
         * @see RamboBot.PartsBot.RobotPart#init()
         */
        public void init()
        {
            //gun can be adjusted
            setAdjustGunForRobotTurn( true );
        }


        /**
         * (non-Javadoc)
         * 
         * @see RamboBot.PartsBot.RobotPart#move()
         */
        public void move()
        {
            // chooses the option with a lower cost
            double bulletPower = Math.min( 3.0, getEnergy() );
            // x and y values of my bot
            double myX = getX();
            double myY = getY();
            // finds the angle between our bot and the enemy bot
            double absoluteBearing = getHeadingRadians() + Math.toRadians( enemy.getBearing() );
            //the enemy x and y calculated using cos and sin
            double enemyX = getX() + enemy.getDistance() * Math.sin( absoluteBearing );
            double enemyY = getY() + enemy.getDistance() * Math.cos( absoluteBearing );
            //enemy's heading
            double enemyHeading = enemy.getHeading();
            //change in enemy's heading
            double enemyHeadingChange = enemyHeading - oldEnemyHeading;
            double enemyVelocity = enemy.getVelocity();
            oldEnemyHeading = enemyHeading;
            //change in time variable initialization
            double deltaTime = 0;
            // dimensions of the battle field
            double battleFieldHeight = getBattleFieldHeight(), battleFieldWidth = getBattleFieldWidth();
            //variables for predicted values
            double predictedX = enemyX, predictedY = enemyY;
            //(time * velocity) == distance so its comparing the bullets predicted distance to the actual distance between the predicted x and y.
            while ( ( ++deltaTime ) * ( 20.0 - 3.0 * bulletPower ) < Point2D.Double.distance( myX,
                myY,
                predictedX,
                predictedY ) )
            {
                //Calculates the predicted points
                predictedX += Math.sin( enemyHeading ) * enemyVelocity;
                predictedY += Math.cos( enemyHeading ) * enemyVelocity;
                //adds if there is a change
                enemyHeading += enemyHeadingChange;
                //checks to see if the predicted x and y values are within the 18 pixel margin
                if ( predictedX < 18.0 || predictedY < 18.0 || predictedX > battleFieldWidth - 18.0
                    || predictedY > battleFieldHeight - 18.0 )
                {
                    // determines that the predicted values do not go off the boundaries of the game field
                    predictedX = Math.min( Math.max( 18.0, predictedX ), battleFieldWidth - 18.0 );
                    predictedY = Math.min( Math.max( 18.0, predictedY ), battleFieldHeight - 18.0 );
                    break;
                }
            }
            //the angle between the predicted and actual values
            double theta = Utils.normalAbsoluteAngle( Math.atan2( predictedX - getX(), predictedY - getY() ) );
            //turns radar to the angle of the enemy
            setTurnRadarRightRadians( Utils.normalRelativeAngle( absoluteBearing - getRadarHeadingRadians() ) );
            //turns the gun to match radar
            setTurnGunRightRadians( Utils.normalRelativeAngle( theta - getGunHeadingRadians() ) );
            //fires with 3 intensity
            fire( 3 );
        }
    }


    /**
     * The class for the tank
     */
    public class Tank implements RobotPart
    {
        //direction variable
        private int moveDirection = 1;


        /**
         * (non-Javadoc)
         * 
         * @see RamboBot.PartsBot.RobotPart#init()
         */
        public void init()
        {
            setColors( Color.black, Color.orange, Color.cyan );
        }

        double prev = 100.0;


        /**
         * (non-Javadoc)
         * 
         * @see RamboBot.PartsBot.RobotPart#move()
         */
        public void move()
        {
            
            // reverse direction if we stopped
        
            
            if ( getVelocity() == 0  && (prev - enemy.getEnergy() > 0.0))
                //flips direction
                moveDirection *= -1;

            // spiral toward our enemy
            setTurnRight( normalizeBearing( enemy.getBearing() + 90 - ( 15 * moveDirection ) ) );
            //distance and direction
            setAhead( enemy.getDistance() * moveDirection );
            //sets the previous energy to new one for the next cycle
            prev = enemy.getEnergy();

        }

    }
}


