package rdvs;

import robocode.*;
import java.awt.geom.Point2D;
import java.awt.Color;

public class WallAvoider extends AdvancedRobot
{
    private AdvancedEnemyBot enemy = new AdvancedEnemyBot();
    private byte radarDirection = 1;
    private byte moveDirection = 1;
    private int tooCloseToWall = 0;
    private int wallMargin = 60;

    public void run()
    {
        setColors( Color.cyan, Color.magenta, Color.cyan );
        setAdjustRadarForGunTurn( true );
        setAdjustGunForRobotTurn( true );

        // Don't get too close to the walls
        addCustomEvent( new Condition( "too_close_to_walls" )
        {
            public boolean test()
            {
                return (
                // we're too close to the left wall
                ( getX() <= wallMargin ||
                // or we're too close to the right wall
                    getX() >= getBattleFieldWidth() - wallMargin ||
                    // or we're too close to the bottom wall
                    getY() <= wallMargin ||
                // or we're too close to the top wall
                getY() >= getBattleFieldHeight() - wallMargin ) );
            }
        } );

        while ( true )
        {
            doRadar();
            doMove();
            doGun();
            execute();
        }
    }

    public void onScannedRobot( ScannedRobotEvent e )
    {
        // track if we have no enemy, the one we found is significantly
        // closer, or we scanned the one we've been tracking.
        if ( enemy.none() || e.getDistance() < enemy.getDistance() - 120
            || e.getName().equals( enemy.getName() ) )
        {

            // track him using the NEW update method
            enemy.update( e, this );
        }
    }

    public void onRobotDeath( RobotDeathEvent e )
    {
        // see if the robot we were tracking died
        if ( e.getName().equals( enemy.getName() ) )
        {
            enemy.reset();
        }
    }

    public void onCustomEvent( CustomEvent e )
    {
        if ( e.getCondition().getName().equals( "too_close_to_walls" ) )
        {
            if ( tooCloseToWall <= 0 )
            {
                // if we weren't already dealing with the walls, we are now
                tooCloseToWall += wallMargin;
                /*
                 * -- don't do it this way // switch directions and move away
                 * moveDirection *= -1; setAhead(10000 * moveDirection);
                 */
                setMaxVelocity( 0 ); // stop!!!
            }
        }
    }

    public void onHitWall( HitWallEvent e )
    {
        out.println( "OUCH! I hit a wall anyway!" );
    }

    public void onHitRobot( HitRobotEvent e )
    {
        tooCloseToWall = 0;
    }

    void doRadar()
    {
        // rotate the radar
        setTurnRadarRight( 360 );
    }

    public void doMove()
    {
        // always square off our enemy, turning slightly toward him
        setTurnRight( normalizeBearing( enemy.getBearing() + 90
            - ( 15 * moveDirection ) ) );

        // if we're close to the wall, eventually, we'll move away
        if ( tooCloseToWall > 0 )
            tooCloseToWall--;

        // switch directions if we've stopped
        // (also handles moving away from the wall if too close)
        if ( getVelocity() == 0 )
        {
            setMaxVelocity( 8 );
            moveDirection *= -1;
            setAhead( 10000 * moveDirection );
        }
    }

    void doGun()
    {
        if ( enemy.none() )
            return;

        double firePower = Math.min( 400 / enemy.getDistance(), 3 );
        double bulletSpeed = 20 - firePower * 3;
        long time = (long)( enemy.getDistance() / bulletSpeed );
        double futureX = enemy.getFutureX( time );
        double futureY = enemy.getFutureY( time );
        double absDeg = absoluteBearing( getX(), getY(), futureX, futureY );
        setTurnGunRight( normalizeBearing( absDeg - getGunHeading() ) );

        if ( getGunHeat() == 0 && Math.abs( getGunTurnRemaining() ) < 10 )
        {
            setFire( firePower );
        }
    }

    // computes the absolute bearing between two points
    double absoluteBearing( double x1, double y1, double x2, double y2 )
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
            bearing = 360 + arcSin; // arcsin is negative here, actually 360 -
                                    // ang
        }
        else if ( xo > 0 && yo < 0 )
        { // x pos, y neg: upper-left
            bearing = 180 - arcSin;
        }
        else if ( xo < 0 && yo < 0 )
        { // both neg: upper-right
            bearing = 180 - arcSin; // arcsin is negative here, actually 180 +
                                    // ang
        }

        return bearing;
    }

    // normalizes a bearing to between +180 and -180
    double normalizeBearing( double angle )
    {
        while ( angle > 180 )
            angle -= 360;
        while ( angle < -180 )
            angle += 360;
        return angle;
    }
}
