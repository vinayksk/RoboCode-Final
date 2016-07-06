package rdvs;

import robocode.*;
import robocode.util.Utils;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;


/**
 * RamboBot Java Final
 *
 * @author Ram Damodaran and Vinay Kumar
 * @version May 27, 2016
 * @author Period: 3
 * @author Assignment: RoboFinalProject
 *
 * @author Sources: Ram Damodaran and Vinay Kumar
 */

public class RamboBot3 extends AdvancedRobot
{
    private AdvancedEnemyBot enemy = new AdvancedEnemyBot();

    private RobotPart[] parts = new RobotPart[3]; // make three parts

    private final static int RADAR = 0;

    private final static int GUN = 1;

    private final static int TANK = 2;

    private int moveDirection = -1;

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
     * Updates enemy robot information
     * 
     * @see robocode.Robot#onScannedRobot(robocode.ScannedRobotEvent)
     * @param e
     *            the incoming information
     */
    public void onScannedRobot( ScannedRobotEvent e )
    {
        // radar locks onto the robot
        Radar radar = (Radar)parts[RADAR];
        if ( radar.shouldTrack( e ) )
        {
            enemy.update( e, this );
        }
    }


    /**
     * Handles the event when Rambo hits the other bot. It maintains the gun
     * targeting while ramming. non-Javadoc)
     * 
     * @see robocode.Robot#onHitRobot(robocode.HitRobotEvent)
     */
    public void onHitRobot( HitRobotEvent e )
    {
        /**
         * Aligns the robot for ramming Needs to aim the gun while hitting the
         * robot This code is the same targeting in the gun class
         * 
         * @author Sources: http://robowiki.net/wiki/Circular_targeting
         * 
         */
        setTurnRight( enemy.getBearing() );
        setAhead( 100 );
        double bulletPower = Math.min( 3.0, getEnergy() );
        // x and y values of my bot
        double myX = getX();
        double myY = getY();
        // finds the angle between our bot and the enemy bot
        double absoluteBearing = getHeadingRadians() + Math.toRadians( enemy.getBearing() );
        // the enemy x and y calculated using cos and sin
        double enemyX = getX() + enemy.getDistance() * Math.sin( absoluteBearing );
        double enemyY = getY() + enemy.getDistance() * Math.cos( absoluteBearing );
        // enemy's heading
        double enemyHeading = enemy.getHeading();
        // change in enemy's heading
        double enemyHeadingChange = enemyHeading - oldEnemyHeading;
        double enemyVelocity = enemy.getVelocity();
        oldEnemyHeading = enemyHeading;
        // change in time variable initialization
        double deltaTime = 0;
        // dimensions of the battle field
        double battleFieldHeight = getBattleFieldHeight(), battleFieldWidth = getBattleFieldWidth();
        // variables for predicted values
        double predictedX = enemyX, predictedY = enemyY;
        // (time * velocity) == distance so its comparing the bullets
        // predicted distance to the actual distance between the
        // predicted x
        // and y.
        while ( ( ++deltaTime ) * ( 20.0 - 3.0 * bulletPower ) < Point2D.Double.distance( myX,
            myY,
            predictedX,
            predictedY ) )
        {
            // Calculates the predicted points
            predictedX += Math.sin( enemyHeading ) * enemyVelocity;
            predictedY += Math.cos( enemyHeading ) * enemyVelocity;
            // adds if there is a change
            enemyHeading += enemyHeadingChange;
            // checks to see if the predicted x and y values are within
            // the
            // 18 pixel margin
            if ( predictedX < 18.0 || predictedY < 18.0 || predictedX > battleFieldWidth - 18.0
                || predictedY > battleFieldHeight - 18.0 )
            {
                // determines that the predicted values do not go off
                // the
                // boundaries of the game field
                predictedX = Math.min( Math.max( 18.0, predictedX ), battleFieldWidth - 18.0 );
                predictedY = Math.min( Math.max( 18.0, predictedY ), battleFieldHeight - 18.0 );
                break;
            }
        }
        // the angle between the predicted and actual values
        double theta = Utils.normalAbsoluteAngle( Math.atan2( predictedX - getX(), predictedY - getY() ) );
        // turns radar to the angle of the enemy
        setTurnRadarRightRadians( Utils.normalRelativeAngle( absoluteBearing - getRadarHeadingRadians() ) );
        // turns the gun to match radar
        setTurnGunRightRadians( Utils.normalRelativeAngle( theta - getGunHeadingRadians() ) );
        // fires with 3 intensity
        fire( bulletPower );

    }


    /**
     * Revereses direction of robot to prevent predictive shooting
     * 
     * @see robocode.Robot#onHitByBullet(robocode.HitByBulletEvent)
     * @param evnt
     *            When the robot is hit by a bullet
     */
    public void onHitByBullet( HitByBulletEvent e )
    {
        // reverse direction
        moveDirection = moveDirection * -1;
        System.out.println( "I was hit by " + e.getName() + "!" );

    }


    /**
     * Handles the event in which robot hits the wall. If RamboBot hits the
     * wall, tooCloseToWall is set to true, RamboBot is turned around, and moves
     * the other direction
     * 
     * @param evnt
     *            When the robot hits the wall
     */
    public void onHitWall( HitWallEvent e )
    {

        if ( !tooCloseToWall )
        {

            tooCloseToWall = true;
            moveDirection = -moveDirection;
            setAhead( 100 * moveDirection );

        }
    }


    /**
     * Handles the event in which robot bullet hits the enemy. Mainly used for
     * debugging.
     * 
     * @param evnt
     *            When the robot hits the opponnent
     */
    public void onBulletHit( BulletHitEvent e )
    {
        System.out.println( "I hit " + e.getName() + "!" );
        System.out.println( getTime() );
    }


    /**
     * Handles event in which robot dies
     * 
     * @see robocode.Robot#onRobotDeath(robocode.RobotDeathEvent)
     * @param e
     *            the incoming information
     */
    public void onRobotDeath( RobotDeathEvent e )
    {
        // sets the radar
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
     * @return the absolute bearing Source: PartsBot
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
     * @return the normalized bearing Source: PartsBot
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
     * WidthLock Radar
     *
     * @author Ram Damodaran and Vinay Kumar
     * @version May 27, 2016
     * @author Period: 3
     * @author Assignment: RoboFinalProject
     *
     * @author Sources: http://robowiki.net/wiki/One_on_One_Radar
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
            setAdjustRadarForGunTurn( true );
            setColors( Color.blue, Color.pink, Color.green );
        }


        /**
         * Width Lock
         * 
         * @see RamboBot.PartsBot.RobotPart#move()
         */
        public void move()
        {
            // Figures out the angle towards the enemy robot
            double angleToEnemy = getHeadingRadians() + Math.toRadians( enemy.getBearing() );

            // Figures out how many degrees needed to turn to enemy and
            // normalizes angle
            double radarTurn = Utils.normalRelativeAngle( angleToEnemy - getRadarHeadingRadians() );

            // How much extra we should turn to avoid errors
            // It calculates this by either figuring out Rules.RADAR...
            // or the degree between 36 and the distance to enemy
            // Rules.RADAR_TURN... is the rate that the robot turns in radians
            double extraTurn = Math.min( Math.atan( 36.0 / enemy.getDistance() ), Rules.RADAR_TURN_RATE_RADIANS );

            // Makes the radar turn a bit more to the direction it is turning
            // so it will overshoot enemy and not slip
            // the ? and : is short hand notation for an if-else, so depending
            // on which way the robot is turning, it will add a bit more that
            // way
            //
            radarTurn += ( radarTurn < 0 ? -extraTurn : extraTurn );

            // Turns the radar to right angle
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
            return e.getName().equals( enemy.getName() );
        }
    }


    /**
     * Predictive shooter
     *
     * @author Ram Damodaran and Vinay Kumar
     * @version May 27, 2016
     * @author Period: 3
     * @author Assignment: RoboFinalProject
     *
     * @author Sources: PartsBot,
     *         http://205.173.41.10/robocode/RoboLessons.html#%5B%5BImproved%
     *         20Targeting%5D%5D
     */
    public class Gun implements RobotPart
    {

        /**
         * Sets gun to adjust to tank turn
         * 
         * @see RamboBot.PartsBot.RobotPart#init()
         */
        public void init()
        {
            setAdjustGunForRobotTurn( true );
        }


        /**
         * Predictive Shooting
         * 
         * @see RamboBot.PartsBot.RobotPart#move()
         */
        public void move()
        {
            // Doesn't shoot if there is no enemy
            if ( enemy.none() )
            {
                return;
            }

            // Finds bullet power based of distance
            double firePower = Math.min( 500 / enemy.getDistance(), 3 );
            // Base code to figure out the bulletSpeed
            double bulletSpeed = 20 - firePower * 3;
            // Finds time using D=rt equation
            long time = (long)( enemy.getDistance() / bulletSpeed );

            // Figures out the enemy's future x and y coordinates
            double futureX = enemy.getFutureX( time );
            double futureY = enemy.getFutureY( time );
            double absDeg = absoluteBearing( getX(), getY(), enemy.getX(), enemy.getY() );

            // Turns gun to the predicted location
            setTurnGunRight( normalizeBearing( absDeg - getGunHeading() ) );

            // Checks if the gun is cooled down. If it is, it shoots
            if ( getGunHeat() == 0 && Math.abs( getGunTurnRemaining() ) < 10 )
            {
                setFire( firePower );
            }
        }

    }


    /**
     * Spiraler Tank And Bullet Avoidance And Wall Avoidance
     *
     * @author Ram Damodaran and Vinay Kumar
     * @version May 27, 2016
     * @author Period: 3
     * @author Assignment: RoboFinalProject
     *
     * @author Sources: Spiraler Robot, Vinay Kumar, Ram Damodaran
     * http://robowiki.net/wiki/Wall_Smoothing/Implementations
     */
    public class Tank implements RobotPart
    {
        // direction variable

        /**
         * (non-Javadoc)
         * 
         * @see RamboBot.PartsBot.RobotPart#init()
         */
        public void init()
        {
            setColors( Color.black, Color.orange, Color.cyan );
        }

        // just to initialize
        double prev = 100.0;


        /**
         * Wall avoidance, spiral, bullet avoidance
         * 
         * @see RamboBot.PartsBot.RobotPart#move()
         */
        public void move()
        {

            // Create a rectangle, which will be the boundries for where
            // RamboBot can go. The rectangle width and height is 100 px less
            // than the actual battle field width and height. This makes sure
            // that RamboBot will not crash into walls (wall-smoothing code)
            Rectangle2D inGameField = new Rectangle2D.Double( 50,
                50,
                getBattleFieldWidth() - 100,
                getBattleFieldHeight() - 100 );
            // If RamboBot is within this boundry, it will do it's normal code,
            // which is spiraling around and ramming when close by

            if ( inGameField.contains( getX(), getY() ) )
            {
                // This is my movement code
                // Since in the beginning there is no enemy, we wait 50 ticks
                // before calling an enemy.function so the robot does not stall
                // if statement determines if the robot is close enough for
                // ramming
                // by checking if it is within 200 pixels
                if ( ( getTime() > 50 ) && ( enemy.getDistance() < 200 ) )
                {
                    // ramming
                    setTurnRight( enemy.getBearing() );
                    setAhead( 100 );
                }
                else
                {
                    if ( ( getVelocity() == 0 && ( prev - enemy.getEnergy() > 0.0 ) ) )
                    // flips direction
                    {
                        moveDirection = moveDirection * -1;
                    }

                    // spiral toward our enemy
                    setTurnRight( normalizeBearing( enemy.getBearing() + 90 - ( 15 * moveDirection ) ) );
                    // distance and direction
                    setAhead( enemy.getDistance() * moveDirection );
                    // sets the previous energy to new one for the next cycle
                    prev = enemy.getEnergy();
                }

            }
            // If RamboBot is outside the boundry, then it will get back within
            // the boundries

            else
            {
                // If the enemy robot is to the left of RamboBot, RamboBot will
                // turn left and move towards it

                if ( enemy.getBearing() < 0 )
                {
                    setTurnLeft( 65 );
                    setAhead( 1000 );
                }
                // If the enemy robot is to the right of RamboBot, RamboBot will
                // turn right and move towards it.

                else
                {
                    setTurnRight( 65 );
                    setAhead( 1000 );
                }
            }

        }
    }
}


