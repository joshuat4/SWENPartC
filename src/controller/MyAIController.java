package controller;

import java.util.*;

import tiles.*;
import tiles.MapTile.Type;
import utilities.Coordinate;
import world.Car;
import world.World;
import world.WorldSpatial;



public class MyAIController extends CarController {

    // ------------------------------------------------
    // For Testing.
    private boolean debug = true;
    private boolean useTestPath = false;
    private List<Node> testpath = new ArrayList<>();
    int test = 0;
    private static final float EPSILON = 0.01f;

    private enum STATE{Forward, Turning, Braking};
    private STATE currState = STATE.Forward;

    // ------------------------------------------------

    private boolean isAccelerating = true;

    // How many minimum units the wall is away from the player.
    private int wallSensitivity = 2;

    private WorldSpatial.RelativeDirection lastTurnDirection = null; // Shows the last turn direction the car takes.
    //
    private WorldSpatial.Direction previousState = null; // Keeps track of the previous state
    // Car Speed to move at
    private final float CAR_SPEED = 3;
    // Offset used to differentiate between 0 and 360 degrees
    private int EAST_THRESHOLD = 3;

    private float carSpeed;
    private WorldSpatial.Direction carOrientation = getOrientation();
    private Coordinate currPos = new Coordinate(getPosition());
    private Coordinate targetPos = new Coordinate("3,4");
    private int counter = 0;
    private int frameCounter = 0;
    private int turnIndex = 0;
//    private trueCoord tCurrPos;
//    private trueCoord tTargetPos;
    private ArrayList<Integer> turnList;
    private Coordinate nextTurn;
    private float speedAtBrake;
    private boolean firstTimeBrake = true;
    private boolean timeToGo = false;
    private boolean justTurned = false;




    public MyAIController(Car car) {
        super(car);
        makeTestPath();
        turnList = getTurnList(testpath);
        System.out.println(turnList);

    }

    @Override
    public void update(float delta) {
        // Gets what the car can see
        HashMap<Coordinate, MapTile> currentView = getView();
        carSpeed = getSpeed();
        carOrientation = getOrientation();
        currPos = new Coordinate(getPosition());
        targetPos = new Coordinate(testpath.get(counter).getName());
//        tCurrPos = new trueCoord(getX(), getY());
//        tTargetPos = new trueCoord(targetPos);
//
//        if(isAccelerating) {
//        	applyForwardAcceleration();
//        }
//
//        if(isBraking) {
//        	//System.out.println("Braking");
//        	applyBrake();
//        }
//        System.out.println("isAccelerating1: " + isAccelerating);


        decideState();

        System.out.print(isAccelerating);

//        System.out.println(currState);
        switch(currState) {
            case Forward:
                forwardState();
                break;
            case Turning:
                isAccelerating = false;
                turningState(delta);
                break;
            case Braking:
                isAccelerating = false;
                brakingState();
                break;

        }

        if(isAccelerating && getSpeed() < CAR_SPEED) {
            applyForwardAcceleration();

        }
        System.out.println(isAccelerating);


//        System.out.println("isAccelerating2: " + isAccelerating);
        System.out.println("currentAngle: " + getAngle());
        System.out.println("path[counter]: " + testpath.get(counter).getName());
        System.out.println("counter is: " + counter);
        System.out.println("framecounter is :" + frameCounter);
        System.out.println("currState is:" + currState);
        System.out.println("carSpeed is: " + carSpeed);
        System.out.println("currPosition is: " + currPos.x + "," + currPos.y);
        System.out.println("targetPosition is: "+ targetPos.x + "," + targetPos.y);
        System.out.println("turnIndex: " + turnIndex + " turnList: " + turnList);
        System.out.println("nextTurn is: "+ nextTurn.x + "," + nextTurn.y);
        System.out.println("timeToGo is: " + timeToGo);
        System.out.println("-----aaaa-----");



    }



    private void decideState() {

        if (turnIndex < turnList.size() ) {
            nextTurn = new Coordinate(testpath.get(turnList.get(turnIndex)).getName());
            float distToTurn = (nextTurn.x - currPos.x) + (nextTurn.y - currPos.y);


            if(currPos.equals(nextTurn)) {
                //currState = STATE.Turning;
            }

            if(currPos.equals(targetPos)) {
                counter += 1;
            }

            if (timeToGo) {
                if(counter-1 >= turnList.get(turnIndex)) {
                    System.out.println("PLEASE");
                    turnIndex += 1 ;
                    currState = STATE.Forward;
                    //timeToGo = false;
                }
            }



            //brake
//            if((1.46f*distToTurn) <= getSpeed() && firstTimeBrake) {
//                speedAtBrake = getSpeed();
//                firstTimeBrake = false;
//                System.out.println("BRAKESPEED: "+ speedAtBrake);
//                System.out.println(getSpeed());
//            } else {
//                firstTimeBrake = true;
//            }

            System.out.println("distToTurn is: " + distToTurn);

            if(timeToGo && justTurned){
               currState = STATE.Forward;
               justTurned = false;
            }

            if((brakeLogic(nextTurn))){
                currState = STATE.Braking;
                System.out.println("HERE: " + getSpeed());
                if(getSpeed() <= 0.25f) {
                    System.out.println("REEE");
                    currState = STATE.Turning;
                    if(timeToGo) {
                        currState = STATE.Forward;
                        timeToGo = false;	    			}
                }
            } else {
            }



        }






    }

    private boolean brakeLogic(Coordinate nextTurn){
        debugPrint("getX");
        System.out.println(getX());
        float distToPointX = (Math.abs(getX() - nextTurn.x));
        float distToPointY = (Math.abs(getY() - nextTurn.y));
        float distToPoint;

        debugPrint("dist to Point: "+ distToPointX);
        debugPrint("dist to Point: "+ distToPointY);
        debugPrint("speed: " + getSpeed());

        if(getOrientation().equals(WorldSpatial.Direction.EAST) ||
                getOrientation().equals(WorldSpatial.Direction.WEST)){
            distToPoint = distToPointX;
        } else {
            distToPoint = distToPointY;
        }

        //Should Add some logic for negative direction.


        double res = (Math.pow(getSpeed(), 2.0))/4f; //Some stopping distance formula. dunno Acceleration rate tho.
        debugPrint("distToPoint: "+ distToPoint);
        debugPrint("res: "+ res);

        if(distToPoint < 0.25f){
            System.out.println("HHH");
            return true;
        }

        System.out.println("TRUTH :" + (distToPoint <= res) );

        if(distToPoint <=  res ){
            return true;
        } else {
            System.out.println(" OK K");
            return false;
        }

    }






    //
    private void forwardState() {
        applySafeForwardAcceleration();
    }

    //    private void reverseState(float delta) {
//    	//applySafeReverseAcceleration();
//
//    }
//
    private void turningState(float delta) {
        frameCounter += 1;
        faceTarget(currPos, targetPos, delta);
    }

    private void brakingState() {
        applyBrake();
    }
//    private void stuck(float delta) {
//    	//recalculate!
//    }



    private void faceTarget(Coordinate currPos, Coordinate targetPos, float delta) {
        WorldSpatial.Direction targetDir = getDirection(currPos, targetPos);
        System.out.println("HUH");
    	System.out.println(currPos.x + "," + currPos.y);
    	System.out.println(targetPos.x + "," + targetPos.y);
    	System.out.println("-----------------------");



//    	if(targetDir == null) {
//    		timeToGo = true;
//    		currState = STATE.Forward;
//    	}


//    	else {
        System.out.println("targetDir degree is: " + directionToDegree(targetDir));
        if(Float.compare(getAngle(), directionToDegree(targetDir)) <= 0) {
            WorldSpatial.RelativeDirection leftRight = leftOrRight(targetDir);
            if(leftRight == null) {
                justTurned = true;
                timeToGo = true;
                frameCounter = 0;
            }
            else {
                switch(leftRight) {
                    case LEFT:
                        lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
                        rotateAntiClockwise(delta);
                        break;

                    case RIGHT:
                        lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
                        rotateClockwise(delta);
                        break;

                    default:
                        timeToGo = true;
                        frameCounter = 0;
                }
            }


        }
        else {
//            timeToGo = true;
            frameCounter = 0;
        }
//    	}
    }





    private void rotateAntiClockwise( float delta) {
        turnLeft(delta);
        if(frameCounter ==2) {
//    		applyForwardAcceleration();
            applySafeForwardAcceleration();
            System.out.println(frameCounter);
            frameCounter = 0;
        }
    }

    private void rotateClockwise( float delta) {
        turnRight(delta);
        if(frameCounter ==2) {
//    		applyForwardAcceleration();
            applySafeForwardAcceleration();
            System.out.println(frameCounter);
            frameCounter = 0;
        }
    }




    private ArrayList<Integer> getTurnList(List<Node> wholeList){
        ArrayList<WorldSpatial.Direction> directionList = new ArrayList<>();
        ArrayList<Integer> result = new ArrayList<>();
        int turns = 1;
        for(int x = 0; x<wholeList.size()-1; x++) {;
            directionList.add(getDirection(new Coordinate(wholeList.get(x).getName()),new Coordinate(wholeList.get(x+1).getName())));
        }
        for(int j =0; j<directionList.size()-1;j++) {
            if(!directionList.get(j).equals(directionList.get(j+1))){
                result.add(turns);
            }
            turns += 1;
        }

        return result;
    }

































    private void applySafeReverseAcceleration() {
        applyReverseAcceleration();
    }

    private void applySafeForwardAcceleration() {
        isAccelerating = true;
        //	applyForwardAcceleration();

    }

















































































//        // If you are not following a wall initially, find a wall to stick to!
//        if(!isFollowingWall){
//            if(getSpeed() < CAR_SPEED){
//                applyForwardAcceleration();
//            }
//            // Turn towards the north
//            if(!getOrientation().equals(WorldSpatial.Direction.NORTH)){
//                lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
//                applyLeftTurn(getOrientation(),delta);
//            }
//            if(checkNorth(currentView)){
//                // Turn right until we go back to east!
//                if(!getOrientation().equals(WorldSpatial.Direction.EAST)){
//                    lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
//                    applyRightTurn(getOrientation(),delta);
//                }
//                else{
//                    isFollowingWall = true;
//                }
//            }
//        }
//        // Once the car is already stuck to a wall, apply the following logic
//        else{
//
//            // Readjust the car if it is misaligned.
//            readjust(lastTurnDirection,delta);
//
//            if(isTurningRight){
//                applyRightTurn(getOrientation(),delta);
//            }
//            else if(isTurningLeft){
//                // Apply the left turn if you are not currently near a wall.
//                if(!checkFollowingWall(getOrientation(),currentView)){
//                    applyLeftTurn(getOrientation(),delta);
//                }
//                else{
//                    isTurningLeft = false;
//                }
//            }
//            // Try to determine whether or not the car is next to a wall.
//            else if(checkFollowingWall(getOrientation(),currentView)){
//                // Maintain some velocity
//                if(getSpeed() < CAR_SPEED){
//                    applyForwardAcceleration();
//                }
//                // If there is wall ahead, turn right!
//                if(checkWallAhead(getOrientation(),currentView)){
//                    lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
//                    isTurningRight = true;
//
//                }
//
//            }
//            // This indicates that I can do a left turn if I am not turning right
//            else{
//                lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
//                isTurningLeft = true;
//            }
//        }



    /**
     * Readjust the car to the orientation we are in.
     * @param lastTurnDirection
     * @param delta
     */
    private void readjust(WorldSpatial.RelativeDirection lastTurnDirection, float delta) {
        if(lastTurnDirection != null){
            if(lastTurnDirection.equals(WorldSpatial.RelativeDirection.RIGHT)){
                adjustRight(getOrientation(),delta);
            }
            else if(lastTurnDirection.equals(WorldSpatial.RelativeDirection.LEFT)){
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
//    private void checkStateChange() {
//        if(previousState == null){
//            previousState = getOrientation();
//        }
//        else{
//            if(previousState != getOrientation()){
//                if(isTurningLeft){
//                    isTurningLeft = false;
//                }
//                if(isTurningRight){
//                    isTurningRight = false;
//                }
//                previousState = getOrientation();
//            }
//        }
//    }

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

//    /**
//     * Check if you have a wall in front of you!
//     * @param orientation the orientation we are in based on WorldSpatial
//     * @param currentView what the car can currently see
//     * @return
//    //     */
//    private boolean checkWallAhead(WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView){
//        switch(orientation){
//            case EAST:
//                return checkEast(currentView);
//            case NORTH:
//                return checkNorth(currentView);
//            case SOUTH:
//                return checkSouth(currentView);
//            case WEST:
//                return checkWest(currentView);
//            default:
//                return false;
//
//        }
//    }

//    /**
//     * Check if the wall is on your left hand side given your orientation
//     * @param orientation
//     * @param currentView
//     * @return
//    //     */
//    private boolean checkFollowingWall(WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView) {
//
//        switch(orientation){
//            case EAST:
//                return checkNorth(currentView);
//            case NORTH:
//                return checkWest(currentView);
//            case SOUTH:
//                return checkEast(currentView);
//            case WEST:
//                return checkSouth(currentView);
//            default:
//                return false;
//        }
//
//    }

//
//    /**
//     * Method below just iterates through the list and check in the correct coordinates.
//     * i.e. Given your current position is 10,10
//     * checkEast will check up to wallSensitivity amount of tiles to the right.
//     * checkWest will check up to wallSensitivity amount of tiles to the left.
//     * checkNorth will check up to wallSensitivity amount of tiles to the top.
//     * checkSouth will check up to wallSensitivity amount of tiles below.
//     */
//    public boolean checkEast(HashMap<Coordinate, MapTile> currentView){
//        // Check tiles to my right
//        Coordinate currentPosition = new Coordinate(getPosition());
//        for(int i = 0; i <= wallSensitivity; i++){
//            MapTile tile = currentView.get(new Coordinate(currentPosition.x+i, currentPosition.y));
//            if(tile.isType(MapTile.Type.WALL)){
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public boolean checkWest(HashMap<Coordinate,MapTile> currentView){
//        // Check tiles to my left
//        Coordinate currentPosition = new Coordinate(getPosition());
//        for(int i = 0; i <= wallSensitivity; i++){
//            MapTile tile = currentView.get(new Coordinate(currentPosition.x-i, currentPosition.y));
//            if(tile.isType(MapTile.Type.WALL)){
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public boolean checkNorth(HashMap<Coordinate,MapTile> currentView){
//        // Check tiles to towards the top
//        Coordinate currentPosition = new Coorswitch(leftRight) {
//		case LEFT:
//			lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
//			rotateAntiClockwise(delta);
//			break;
//
//		case RIGHT:
//			lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
//			rotateClockwise(delta);
//			break;
//
//		default:
//			timeToGo = true;
//			frameCounter = 0;
//		}dinate(getPosition());
//        for(int i = 0; i <= wallSensitivity; i++){
//            MapTile tile = currentView.get(new Coordinate(currentPosition.x, currentPosition.y+i));
//            if(tile.isType(MapTile.Type.WALL)){
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public boolean checkSouth(HashMap<Coordinate,MapTile> currentView){
//        // Check tiles towards the bottom
//        Coordinate currentPosition = new Coordinate(getPosition());
//        for(int i = 0; i <= wallSensitivity; i++){
//            MapTile tile = currentView.get(new Coordinate(currentPosition.x, currentPosition.y-i));
//            if(tile.isType(MapTile.Type.WALL)){
//                return true;
//            }
//        }
//        return false;
//    }


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


    private float directionToDegree(WorldSpatial.Direction dir){
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


    private WorldSpatial.RelativeDirection leftOrRight(WorldSpatial.Direction targetDir){
        int caseNum = 0;
        if(Float.compare(getAngle(), 0f) == 0 || Float.compare(getAngle(), 360f) == 0) {
            caseNum = 0;
        }
        else if(Float.compare(getAngle(), 90f) == 0) {
            caseNum = 1;
        }
        else if(Float.compare(getAngle(), 180f) == 0) {
            caseNum = 2;
        }
        else if(Float.compare(getAngle(), 270f) == 0) {
            caseNum = 3;
        }


        switch (caseNum){
            case 0:
                switch (targetDir){
                    case NORTH:
                        return WorldSpatial.RelativeDirection.LEFT;
                    case SOUTH:
                        return WorldSpatial.RelativeDirection.RIGHT;
                    case WEST:
                        return WorldSpatial.RelativeDirection.LEFT;
                }
                break;


            case 3:
                switch(targetDir){
                    case WEST:
                        return WorldSpatial.RelativeDirection.RIGHT;

                    case EAST:
                        return WorldSpatial.RelativeDirection.LEFT;

                    case NORTH:
                        return WorldSpatial.RelativeDirection.LEFT;

                }
                break;

            case 2:
                switch (targetDir){
                    case NORTH:
                        return WorldSpatial.RelativeDirection.RIGHT;

                    case SOUTH:
                        return WorldSpatial.RelativeDirection.LEFT;

                    case EAST:
                        return WorldSpatial.RelativeDirection.LEFT;

                }
                break;

            case 1:
                switch (targetDir){
                    case EAST:
                        return WorldSpatial.RelativeDirection.RIGHT;

                    case WEST:
                        return WorldSpatial.RelativeDirection.LEFT;

                    case SOUTH:
                        return WorldSpatial.RelativeDirection.LEFT;
                }
                break;
            default:
                System.out.println("THIS IS BAD LINE 883");
//                return WorldSpatial.RelativeDirection.LEFT;
        }

        System.out.println("THIS IS BAD LINE 887");
        return null;

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
        Node node10 = new Node("7,8");
        Node node11 = new Node("7,9");
        Node node12 = new Node("7,10");
        Node node13 = new Node("7,11");
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
        testpath.add(new Node("7,12"));
        testpath.add(new Node("7,13"));
        testpath.add(new Node("7,14"));
        testpath.add(new Node("6,14"));
        testpath.add(new Node("5,14"));
        testpath.add(new Node("4,14"));
        testpath.add(new Node("3,14"));
        testpath.add(new Node("3,13"));
        testpath.add(new Node("3,12"));
        testpath.add(new Node("3,11"));
        testpath.add(new Node("3,10"));
        testpath.add(new Node("4,10"));
        testpath.add(new Node("5,10"));
    }







}

//}
