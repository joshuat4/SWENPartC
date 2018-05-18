package controller;

import java.util.*;

import tiles.*;
import tiles.MapTile.Type;
import utilities.Coordinate;
import world.Car;
import world.World;
import world.WorldSpatial;



public class OtherAIController extends CarController {

    // ------------------------------------------------
    // For Testing.
    private boolean debug = true;
    private boolean useTestPath = false;
    private List<Node> testpath = new ArrayList<>();
    int test = 0;
    // ------------------------------------------------

    // How many minimum units the wall is away from the player.
    private int wallSensitivity = 2;


    private boolean isFollowingWall = false; // This is initialized when the car sticks to a wall.
    private WorldSpatial.RelativeDirection lastTurnDirection = null; // Shows the last turn direction the car takes.


    //STATES
    private boolean isTurningLeft = false;
    private boolean isTurningRight = false;
    private boolean isAccelerating = true;
    private boolean isReversing = false;


    //
    private WorldSpatial.Direction previousState = null; // Keeps track of the previous state

    // Car Speed to move at
    private final float CAR_SPEED = 1;
    int frameCounter = 0;


    // Offset used to differentiate between 0 and 360 degrees
    private int EAST_THRESHOLD = 3;

    public OtherAIController(Car car) {
        super(car);
    }

    Coordinate initialGuess;
    boolean notSouth = true;
    @Override
    public void update(float delta) {

        // Gets what the car can see
        HashMap<Coordinate, MapTile> currentView = getView();

        checkStateChange();

        // If you are not following a wall initially, find a wall to stick to!
        if(!isFollowingWall){
            if(getSpeed() < CAR_SPEED){
                applyForwardAcceleration();
            }
            // Turn towards the north
            if(!getOrientation().equals(WorldSpatial.Direction.NORTH)){
                lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
                rotateAntiClockwise(delta);
//                applyLeftTurn(getOrientation(),delta);
            }
            if(checkNorth(currentView)){
                // Turn right until we go back to east!
                if(!getOrientation().equals(WorldSpatial.Direction.EAST)){
                    lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
                    rotateClockwise(delta);
//                    applyRightTurn(getOrientation(),delta);
                }
                else{
                    isFollowingWall = true;
                }
            }
        }
        // Once the car is already stuck to a wall, apply the following logic
        else{

            // Readjust the car if it is misaligned.
            readjust(lastTurnDirection,delta);

            if(isTurningRight){
                rotateClockwise(delta);
//                applyRightTurn(getOrientation(),delta);
            }
            else if(isTurningLeft){
                // Apply the left turn if you are not currently near a wall.
                if(!checkFollowingWall(getOrientation(),currentView)){
                    rotateAntiClockwise(delta);
//                    applyLeftTurn(getOrientation(),delta);
                }
                else{
                    isTurningLeft = false;
                }
            }
            // Try to determine whether or not the car is next to a wall.
            else if(checkFollowingWall(getOrientation(),currentView)){
                // Maintain some velocity
                if(getSpeed() < CAR_SPEED){
                    applyForwardAcceleration();
                }
                // If there is wall ahead, turn right!
                if(checkWallAhead(getOrientation(),currentView)){
                    lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
                    isTurningRight = true;

                }

            }
            // This indicates that I can do a left turn if I am not turning right
            else{
                lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
                isTurningLeft = true;
            }
        }



    }

    /**
     * Readjust the car to the orientation we are in.
     * @param lastTurnDirection
     * @param delta
     */
    private void readjust(WorldSpatial.RelativeDirection lastTurnDirection, float delta) {
        if(lastTurnDirection != null){
            if(!isTurningRight && lastTurnDirection.equals(WorldSpatial.RelativeDirection.RIGHT)){
                adjustRight(getOrientation(),delta);
            }
            else if(!isTurningLeft && lastTurnDirection.equals(WorldSpatial.RelativeDirection.LEFT)){
                adjustLeft(getOrientation(),delta);
            }
        }

    }

    /**
     * Try to orient myself to a degree that I was supposed to be at if I am
     * misaligned.
     */
    private void adjustLeft(WorldSpatial.Direction orientation, float delta) {

        switch(orientation){
            case EAST:
                if(getAngle() > WorldSpatial.EAST_DEGREE_MIN+EAST_THRESHOLD){
                    turnRight(delta);
                }
                break;
            case NORTH:
                if(getAngle() > WorldSpatial.NORTH_DEGREE){
                    turnRight(delta);
                }
                break;
            case SOUTH:
                if(getAngle() > WorldSpatial.SOUTH_DEGREE){
                    turnRight(delta);
                }
                break;
            case WEST:
                if(getAngle() > WorldSpatial.WEST_DEGREE){
                    turnRight(delta);
                }
                break;

            default:
                break;
        }

    }

    private void adjustRight(WorldSpatial.Direction orientation, float delta) {
        switch(orientation){
            case EAST:
                if(getAngle() > WorldSpatial.SOUTH_DEGREE && getAngle() < WorldSpatial.EAST_DEGREE_MAX){
                    turnLeft(delta);
                }
                break;
            case NORTH:
                if(getAngle() < WorldSpatial.NORTH_DEGREE){
                    turnLeft(delta);
                }
                break;
            case SOUTH:
                if(getAngle() < WorldSpatial.SOUTH_DEGREE){
                    turnLeft(delta);
                }
                break;
            case WEST:
                if(getAngle() < WorldSpatial.WEST_DEGREE){
                    turnLeft(delta);
                }
                break;

            default:
                break;
        }

    }

    /**
     * Checks whether the car's state has changed or not, stops turning if it
     *  already has.
     */
    private void checkStateChange() {
        if(previousState == null){
            previousState = getOrientation();
        }
        else{
            if(previousState != getOrientation()){
                if(isTurningLeft){
                    isTurningLeft = false;
                }
                if(isTurningRight){
                    isTurningRight = false;
                }
                previousState = getOrientation();
            }
        }
    }

    /**
     * Turn the car counter clock wise (think of a compass going counter clock-wise)
     */
    private void applyLeftTurn(WorldSpatial.Direction orientation, float delta) {
        switch(orientation){
            case EAST:
                if(!getOrientation().equals(WorldSpatial.Direction.NORTH)){
                    turnLeft(delta);
                }
                break;
            case NORTH:
                if(!getOrientation().equals(WorldSpatial.Direction.WEST)){
                    turnLeft(delta);
                }
                break;
            case SOUTH:
                if(!getOrientation().equals(WorldSpatial.Direction.EAST)){
                    turnLeft(delta);
                }
                break;
            case WEST:
                if(!getOrientation().equals(WorldSpatial.Direction.SOUTH)){
                    turnLeft(delta);
                }
                break;
            default:
                break;

        }

    }

    /**
     * Turn the car clock wise (think of a compass going clock-wise)
     */
    private void applyRightTurn(WorldSpatial.Direction orientation, float delta) {
        switch(orientation){
            case EAST:
                if(!getOrientation().equals(WorldSpatial.Direction.SOUTH)){
                    turnRight(delta);
                }
                break;
            case NORTH:
                if(!getOrientation().equals(WorldSpatial.Direction.EAST)){
                    turnRight(delta);
                }
                break;
            case SOUTH:
                if(!getOrientation().equals(WorldSpatial.Direction.WEST)){
                    turnRight(delta);
                }
                break;
            case WEST:
                if(!getOrientation().equals(WorldSpatial.Direction.NORTH)){
                    turnRight(delta);
                }
                break;
            default:
                break;

        }

    }

    /**
     * Check if you have a wall in front of you!
     * @param orientation the orientation we are in based on WorldSpatial
     * @param currentView what the car can currently see
     * @return
     */
    private boolean checkWallAhead(WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView){
        switch(orientation){
            case EAST:
                return checkEast(currentView);
            case NORTH:
                return checkNorth(currentView);
            case SOUTH:
                return checkSouth(currentView);
            case WEST:
                return checkWest(currentView);
            default:
                return false;

        }
    }

    /**
     * Check if the wall is on your left hand side given your orientation
     * @param orientation
     * @param currentView
     * @return
     */
    private boolean checkFollowingWall(WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView) {

        switch(orientation){
            case EAST:
                return checkNorth(currentView);
            case NORTH:
                return checkWest(currentView);
            case SOUTH:
                return checkEast(currentView);
            case WEST:
                return checkSouth(currentView);
            default:
                return false;
        }

    }


    /**
     * Method below just iterates through the list and check in the correct coordinates.
     * i.e. Given your current position is 10,10
     * checkEast will check up to wallSensitivity amount of tiles to the right.
     * checkWest will check up to wallSensitivity amount of tiles to the left.
     * checkNorth will check up to wallSensitivity amount of tiles to the top.
     * checkSouth will check up to wallSensitivity amount of tiles below.
     */
    public boolean checkEast(HashMap<Coordinate, MapTile> currentView){
        // Check tiles to my right
        Coordinate currentPosition = new Coordinate(getPosition());
        for(int i = 0; i <= wallSensitivity; i++){
            MapTile tile = currentView.get(new Coordinate(currentPosition.x+i, currentPosition.y));
            if(tile.isType(MapTile.Type.WALL)){
                return true;
            }
        }
        return false;
    }

    public boolean checkWest(HashMap<Coordinate,MapTile> currentView){
        // Check tiles to my left
        Coordinate currentPosition = new Coordinate(getPosition());
        for(int i = 0; i <= wallSensitivity; i++){
            MapTile tile = currentView.get(new Coordinate(currentPosition.x-i, currentPosition.y));
            if(tile.isType(MapTile.Type.WALL)){
                return true;
            }
        }
        return false;
    }

    public boolean checkNorth(HashMap<Coordinate,MapTile> currentView){
        // Check tiles to towards the top
        Coordinate currentPosition = new Coordinate(getPosition());
        for(int i = 0; i <= wallSensitivity; i++){
            MapTile tile = currentView.get(new Coordinate(currentPosition.x, currentPosition.y+i));
            if(tile.isType(MapTile.Type.WALL)){
                return true;
            }
        }
        return false;
    }

    public boolean checkSouth(HashMap<Coordinate,MapTile> currentView){
        // Check tiles towards the bottom
        Coordinate currentPosition = new Coordinate(getPosition());
        for(int i = 0; i <= wallSensitivity; i++){
            MapTile tile = currentView.get(new Coordinate(currentPosition.x, currentPosition.y-i));
            if(tile.isType(MapTile.Type.WALL)){
                return true;
            }
        }
        return false;
    }


    //-----------------------------------------------------------------
    //Helper Functions.


    private WorldSpatial.Direction oppositeOfOrientation(WorldSpatial.Direction dir){
        switch(dir){
            case EAST:
                return WorldSpatial.Direction.WEST;
            case WEST:
                return WorldSpatial.Direction.EAST;
            case NORTH:
                return WorldSpatial.Direction.SOUTH;
            case SOUTH:
                return WorldSpatial.Direction.NORTH;
        }
        return null;
    }


    private int directionToDegree(WorldSpatial.Direction dir){
        switch(dir){
            case WEST:
                return WorldSpatial.WEST_DEGREE;
            case EAST:
                return WorldSpatial.EAST_DEGREE_MIN;
            case NORTH:
                return WorldSpatial.NORTH_DEGREE;
            case SOUTH:
                return WorldSpatial.SOUTH_DEGREE;
        }
        return 0;
    }
    private void rotateAntiClockwise(float delta){
        turnLeft(delta);
        frameCounter++;
        if(frameCounter == 2){
            applyForwardAcceleration();
            frameCounter= 0;
        }
    }
    private void rotateClockwise(float delta){
        turnRight(delta);
        if(frameCounter == 2){
            applyForwardAcceleration();
            frameCounter= 0;
        }
    }


    private WorldSpatial.RelativeDirection leftOrRight(WorldSpatial.Direction targetDir){
        debugPrint("INSIDE SWITCH LEFT OR RIGHT: " + getOrientation() + "|| Target: "+targetDir);
        switch (getOrientation()){
            case EAST:
                switch (targetDir){
                    case NORTH:
                        return WorldSpatial.RelativeDirection.LEFT;
                    case SOUTH:
                        return WorldSpatial.RelativeDirection.RIGHT;
                    case WEST:
                        return null;
                }

            case SOUTH:
                switch(targetDir){
                    case WEST:
                        return WorldSpatial.RelativeDirection.RIGHT;

                    case EAST:
                        return WorldSpatial.RelativeDirection.LEFT;

                    case NORTH:
                        return null;

                }

            case WEST:
                switch (targetDir){
                    case NORTH:
                        return WorldSpatial.RelativeDirection.RIGHT;

                    case SOUTH:
                        return WorldSpatial.RelativeDirection.LEFT;

                    case EAST:
                        return null;

                }

            case NORTH:
                switch (targetDir){
                    case EAST:
                        return WorldSpatial.RelativeDirection.RIGHT;

                    case WEST:
                        return WorldSpatial.RelativeDirection.LEFT;

                    case SOUTH:
                        return null;
                }
            default:
                return null;
        }
    }


    private WorldSpatial.Direction getDirection(Coordinate source, Coordinate destination){
        if(destination.x > source.x) {
            return WorldSpatial.Direction.EAST;
        } else if(destination.x < source.x) {
            return WorldSpatial.Direction.WEST;
        } else if(destination.y > source.y) {
            return WorldSpatial.Direction.NORTH;
        } else if (destination.y < source.y){
            return WorldSpatial.Direction.SOUTH;
        } else {
            return null;
        }
    }




    // Debug Helpers
    private void debugPrint(String message){
        if(debug){
            System.out.println("DEBUGGER: "+ message);
        }
    }

    public void makeTestPath() {
        if (test == 0) {
            Node node0 = new Node("2,3");
            Node node1 = new Node("3,3");
            Node node2 = new Node("4,3");
            Node node3 = new Node("5,3");
            Node node4 = new Node("6,3");
            Node node5 = new Node("7,3");
            Node node6 = new Node("7,4");
            Node node7 = new Node("7,5");
            Node node8 = new Node("7,6");
            Node node9 = new Node("7,7");
            Node node10 = new Node("8,7");
            Node node11 = new Node("9,7");
            Node node12 = new Node("10,7");
            Node node13 = new Node("11,7");
            testpath.add(node0);
            testpath.add(node1);
            testpath.add(node2);
            testpath.add(node3);
            testpath.add(node4);
            testpath.add(node5);
            testpath.add(node6);
            testpath.add(node7);
            testpath.add(node8);
            testpath.add(node9);
            testpath.add(node10);
            testpath.add(node11);
            testpath.add(node12);
            testpath.add(node13);
        }







    }

}
