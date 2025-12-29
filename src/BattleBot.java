import battleship.BattleShip2;
import battleship.BattleShipBot;
import battleship.CellState;
import battleship.ShipOrientation;
import javafx.scene.control.Cell;

import java.awt.*;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * A Sample random shooter - Takes no precaution on double shooting and has no strategy once
 * a ship is hit - This is not a good solution to the problem!
 *
 * @author mark.yendt@mohawkcollege.ca (Dec 2021)
 */
public class BattleBot implements BattleShipBot {
    private int gameSize;
    private BattleShip2 battleShip;
    private Random random;
    private CellState[][] board;

    private Point lastHitPoint = null;
    private ShipOrientation targetOrientation = ShipOrientation.Horizontal;
    private int shotDirection = 1;
    private int sinkShipsCount = 0;
    int directionIndex = 0;
    String sinkedShipData = "";
    Point[] directionPoints = new Point[]{
            new Point(1, 0),
            new Point(-1, 0),
            new Point(0, 1),
            new Point(0, -1)
    };


    /**
     * Constructor keeps a copy of the BattleShip instance
     * Create instances of any Data Structures and initialize any variables here
     *
     * @param b previously created battleship instance - should be a new game
     */

    @Override
    public void initialize(BattleShip2 b) {
        battleShip = b;
        gameSize = b.BOARD_SIZE;
        board = new CellState[gameSize][gameSize];
        sinkShipsCount = battleShip.numberOfShipsSunk();


        reset();


        // Need to use a Seed if you want the same results to occur from run to run
        // This is needed if you are trying to improve the performance of your code

        random = new Random(0xAAAAAAAA);   // Needed for random shooter - not required for more systematic approaches
    }


    public void reset() {
        lastHitPoint = null;
        sinkedShipData = "";
        directionIndex = 0;
        sinkShipsCount = 0;


        for (int i = 0; i < gameSize; ++i) {
            for (int y = 0; y < gameSize; ++y) {
                this.board[i][y] = CellState.Empty;
            }
        }
    }


    public Point getNextDirectionPoint(Point startPoint) {
        //System.out.println("GETTIN NEXT from " + startPoint);
        Point resultPoint = new Point(startPoint.x + directionPoints[directionIndex].x, startPoint.y + directionPoints[directionIndex].y);
        if (!isValidLocation(resultPoint)) {

            do {
                changeDirection();
                resultPoint = new Point(startPoint.x + directionPoints[directionIndex].x, startPoint.y + directionPoints[directionIndex].y);
            }
            while (!isValidLocation(resultPoint));
        }
        //System.out.println("GOT NEXT  " + resultPoint);



        return resultPoint;
    }

    public boolean isValidLocation(Point destPoint) {
        return ((destPoint.x >= 0) && (destPoint.x < 15) && (destPoint.y >= 0) && (destPoint.y < 15));
    }

    public boolean isValidPoint(Point destPoint) {
        return (isValidLocation(destPoint) & (board[destPoint.y][destPoint.x] == CellState.Empty));
    }


    public Point getNextPoint2(Point hitPoint, boolean horizontal) {

        Point destPoint = new Point(hitPoint.x, hitPoint.y);
        boolean validPoint = false;
        do {
            if (horizontal) {
                destPoint.x = hitPoint.x + 1;
                destPoint.y = hitPoint.y;
            } else {
                destPoint.y = hitPoint.y + 1;
                destPoint.x = hitPoint.x;
            }


            validPoint = isValidPoint(destPoint);


        } while (!validPoint);


        Point[] testPoints = new Point[]{
                new Point(1, 0),
                new Point(-1, 0),
                new Point(0, 1),
                new Point(0, -1)
        };


        return null;
    }


    public Point getNextPoint(Point hitPoint) {

        Point[] testPoints = new Point[]{
                new Point(1, 0),
                new Point(-1, 0),
                new Point(0, 1),
                new Point(0, -1)
        };

        for (int i = 0; i < testPoints.length; i++) {
            Point testPoint = testPoints[i];
            Point destPoint = new Point(hitPoint.x + testPoint.x, hitPoint.y + testPoint.y);
            //System.out.println(" D1 " + destPoint +  GetPointState(destPoint));
            boolean validPoint = isValidPoint(destPoint);

            if (validPoint && GetPointState(destPoint) == CellState.Empty) {
                //System.out.println(" D2 " + destPoint);
                return destPoint;
            }

        }

        return null;
    }


    public void printBoard() {
        String line = "";
        for (int i = 0; i < gameSize; ++i) {
            line = "";
            for (int y = 0; y < gameSize; ++y) {

                line += "  " + this.board[i][y];
            }
            //System.out.println(line);
        }
    }


    public Point getRandomPoint() throws Exception {


        try {
            Point nextPoint = new Point(random.nextInt(gameSize), random.nextInt(gameSize));
            //System.out.println("GETTING RANDOM " + nextPoint);
            while (!isValidPoint(nextPoint)) {
                nextPoint = new Point(random.nextInt(gameSize), random.nextInt(gameSize));
                //System.out.println("try " + nextPoint);
            }
            //System.out.println("GOT RANDOM " + nextPoint);
            return nextPoint;
        } catch (Exception e) {
            throw new Exception("RRRR");
        }


    }


    public CellState GetPointState(Point point) {
        return board[point.y][point.x];
    }


    public boolean tryShoot(Point point) {
        //System.out.println("SHOOTING " + point);
        boolean hit = battleShip.shoot(point);
        board[point.y][point.x] = hit ? CellState.Hit : CellState.Miss;
        //System.out.println(" SHOOTING: " + point);
        if (hit) {
            //System.out.println("SHOOTING - HIT " + "  DIR: " + directionIndex + " POINT: " + point);
            lastHitPoint = point;
            sinkedShipData += ", " + lastHitPoint;
            printBoard();
            //System.out.println(" SHOOTING - SUCCESS - " + lastHitPoint);
        } else {
            changeDirection();
        }





        return hit;
    }


    public void changeDirection() {
        directionIndex++;
        if (directionIndex > directionPoints.length - 1)
            directionIndex = 0;


        String dir = "";
        switch (directionIndex) {
            case 0:
                dir = "Right";
                break;
            case 1:
                dir = "Left";
                break;
            case 2:
                dir = "Down";
                break;
            case 3:
                dir = "Up";
                break;

        }
        //System.out.println("Changing direction to " + dir);

    }


    /**
     * Create a random shot and calls the battleship shoot method
     * Put all logic here (or in other methods called from here)
     * The BattleShip API will call your code until all ships are sunk
     */

    @Override
    public void fireShot() {

        Point nextPoint = null;

        if ((lastHitPoint == null)) {
            try {
                nextPoint = getRandomPoint();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            //System.out.println(" RANDOM POINT - " + nextPoint);
            tryShoot(nextPoint);


        } else {
            nextPoint = getNextDirectionPoint(lastHitPoint);
            //System.out.println(" GOT NEXT POINT - " + directionIndex + " - FROM POINT: " + lastHitPoint + " > " + nextPoint);
            if  (GetPointState(nextPoint) == CellState.Hit) {
                do {
                    nextPoint = getNextDirectionPoint(nextPoint);
                }
                while (GetPointState(nextPoint) != CellState.Empty);
            }

            boolean shiphit = tryShoot(nextPoint);


            int currentSinkedCount = battleShip.numberOfShipsSunk();
            if (currentSinkedCount > sinkShipsCount) {
                sinkShipsCount = currentSinkedCount;
                lastHitPoint = null;
                //System.out.println("SUNKED: " + sinkedShipData);
                printBoard();
                sinkedShipData = "";
                directionIndex = 0;
            }


            if (battleShip.allSunk()) {
                printBoard();
                reset();
            }





        }


    }

    /**
     * Authorship of the solution - must return names of all students that contributed to
     * the solution
     *
     * @return names of the authors of the solution
     */

    @Override
    public String getAuthors() {
        return "Mark Yendt (CSAIT Professor)";
    }
}
