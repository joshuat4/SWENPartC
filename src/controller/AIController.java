package controller;

import java.util.*;

import tiles.*;
import tiles.MapTile.Type;
import utilities.Coordinate;
import utilities.PeekTuple;
import world.Car;
import world.World;
import world.WorldSpatial;

public class AIController extends CarController {
	
	// How many minimum units the wall is away from the player.
	private int wallSensitivity = 2;
	private boolean debug = true;
	private boolean useTestPath = false;
	
	
	private boolean isFollowingWall = false; // This is initialized when the car sticks to a wall.
	private WorldSpatial.RelativeDirection lastTurnDirection = null; // Shows the last turn direction the car takes.
	private boolean isTurningLeft = false;
	private boolean isTurningRight = false; 
	private WorldSpatial.Direction previousState = null; // Keeps track of the previous state
	
	// Car Speed to move at
	private final float CAR_SPEED = 3;
	
	// Offset used to differentiate between 0 and 360 degrees
	private int EAST_THRESHOLD = 3;
	
	private Car carrrr;
	
	public AIController(Car car) {
		super(car);
		this.carrrr = car;
	}
	
	Coordinate initialGuess;
	boolean notSouth = true;
	
	HashMap<Coordinate, MapTile> wholeMap = new HashMap<>();
	Graph graphWithShortestPaths;
	int firstTime = 0;
	boolean hasReachedNextDest = true;
	int test = 0;
	int counter = 0;
	boolean isTurningSoon= false;
	boolean processing = false;
	
	//List of paths that the car will need to go through
	Queue<List<Node>> pathList = new LinkedList<>();
	//Current path being followed
	List<Node> path = new ArrayList<>();
	List<Node> testpath = new ArrayList<>();
	List<Coordinate> coordList = new ArrayList<>();

	
	@Override
	public void update(float delta) {
		// Gets what the car can see
		HashMap<Coordinate, MapTile> currentView = getView();
		checkStateChange();
		for(Coordinate i : currentView.keySet()) {
			
			//Building up wholeMap with data from currentView
			if(i.x >= 0 && i.y >= 0 && i.x < World.MAP_WIDTH && i.y < World.MAP_HEIGHT ) {
				wholeMap.put(i, currentView.get(i));
			}
	
		}

		//Handles Steering
		if(isTurningLeft){
			debugPrint("TRYING TO TURN LEFT");
			applySafeLeftTurn(delta);
		} else if(isTurningRight){
			debugPrint("TRYING TO TURN RIGHT");
			applySafeRightTurn(delta);
		} else {
			readjust(lastTurnDirection, delta);
		}


		//
		if(hasReachedNextDest) {
			debugPrint("Recalculating Route.");
//			System.out.println("recalculating");
			List<Node> result =exploreDijkstras(new Coordinate(carrrr.getPosition()));
			result.add(new Node("1,2"));
			pathList.add(result);
			hasReachedNextDest = false;
		}




		//Car movement code-----------------------------------------------------------------------------------
		//If there's a path in the path array to follow
		if(pathList.size() > 0 || processing) {
			if(processing) {
				checkStateChange();
				
				//Identify if target tile is N,S,E or W.
				try {
					path.get(counter+1);
				}
				catch(Exception e) {
					debugPrint("Starting new path");
					counter = 0;
					processing = false;
					hasReachedNextDest = true;
				}



				Coordinate currPos = new Coordinate (getPosition());
				Coordinate targetPos = new Coordinate(path.get(counter).getName());

				if(!coordList.contains(currPos)){
					debugPrint("Recalculating Route (Not on route).");
					List<Node> result =exploreDijkstras(new Coordinate(getPosition()));
					result.add(new Node("99,99")); //This is just to make sure something is there.
					pathList.add(result);
					processing = false;
					debugPrint(result);
				}


				WorldSpatial.Direction dir = getDirection(currPos, targetPos);

				boolean isFacingTarget = false;

				System.out.println("-----------------");
				debugPrint("currPos: "+ currPos);
				debugPrint("targetPos: "+ targetPos);
				System.out.println("-----------------");

				// If we are on the target, move on to next.
				if(currPos.equals(targetPos)) {
					counter++;
					currPos = targetPos;
					targetPos = new Coordinate(path.get(counter).getName());



					if(!isFacingTarget){
						isTurningSoon = true;
					}
					dir = getDirection(currPos, targetPos);
					System.out.println("dir: "+ dir);
				}
				else {

					System.out.println("dir: "+ dir + "|| carFront: " + getOrientation());
					isFacingTarget = dir.equals(getOrientation());

					//Not on target yet.
					if(!isFacingTarget) {
						//If not moving in Direction of target Coord:
						if(leftOrRight(dir) == null){
							//this is bad.
							debugPrint("Not left or right?");
							debugPrint("UTURN REQUIRED");

						} else {
							debugPrint("leftOrRight: " + leftOrRight(dir));
							lastTurnDirection = leftOrRight(dir);
							int targetDegree = directionToDegree(dir);

							debugPrint("PEEK: "+peek(getVelocity(), targetDegree, WorldSpatial.RelativeDirection.RIGHT, delta).getCoordinate().toString());

							if(peek(getVelocity(), targetDegree, WorldSpatial.RelativeDirection.RIGHT, delta).getCoordinate().equals(currPos)){
								applyForwardAcceleration();
							}
							if(lastTurnDirection.equals(WorldSpatial.RelativeDirection.RIGHT)){
								if(peek(getVelocity(),targetDegree, WorldSpatial.RelativeDirection.RIGHT, delta).getCoordinate().equals(targetPos)){
									debugPrint("RIGHT TURN SAFE");
									isTurningRight = true;
								}
							} else{
								if(peek(getVelocity(),targetDegree, WorldSpatial.RelativeDirection.LEFT, delta).getCoordinate().equals(targetPos)) {
									debugPrint("LEFT TURN SAFE");

									isTurningLeft = true;
								}
							}
						}
						
						
					} else {
						readjust(lastTurnDirection, delta);
						//If moving in right direction,
						//Accelerate if not traveling at max speed
						float x = CAR_SPEED;
						if(isTurningSoon) {
							x = CAR_SPEED/4f;
						}
						if(getSpeed() < x){
							isTurningSoon = false;
							readjust(lastTurnDirection, delta);
							applyForwardAcceleration();
						}

					}
					
					
					
				}
				
			} else {
				if(useTestPath){
					makeTestPath();
					path = testpath;
				} else {
					path = pathList.poll();
					//Populate coordList
					for(Node n: path){
						coordList.add(new Coordinate(n.getName()));
					}

				}
				debugPrint(path);
				processing = true; 
				
			}
	
			
		} else {
			debugPrint("End of path list");
		}
		
	
		
		
		
		

/*		// If you are not following a wall initially, find a wall to stick to!
		if(!isFollowingWall){
			if(getSpeed() < CAR_SPEED){
				applyForwardAcceleration();
			}
			// Turn towards the north
			if(!getOrientation().equals(WorldSpatial.Direction.NORTH)){
				lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
				applyLeftTurn(getOrientation(),delta);
			}
			if(checkNorth(currentView)){
				// Turn right until we go back to east!
				if(!getOrientation().equals(WorldSpatial.Direction.EAST)){
					lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
					applyRightTurn(getOrientation(),delta);
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
				applyRightTurn(getOrientation(),delta);
			}
			else if(isTurningLeft){
				// Apply the left turn if you are not currently near a wall.
				if(!checkFollowingWall(getOrientation(),currentView)){
					applyLeftTurn(getOrientation(),delta);
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
		
		*/

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
	
		
	private WorldSpatial.RelativeDirection leftOrRight(WorldSpatial.Direction targetDir){
		debugPrint("INSIDE SWITCH LEFT OR RIGHT: " + getOrientation() + "|| Target: "+targetDir);
		switch (getOrientation()){
			case EAST:
				switch (targetDir){
					case NORTH:
						return WorldSpatial.RelativeDirection.LEFT;
					case SOUTH:
						return WorldSpatial.RelativeDirection.RIGHT;
				}

			case SOUTH:
				switch(targetDir){
					case WEST:
						return WorldSpatial.RelativeDirection.RIGHT;

					case EAST:
						return WorldSpatial.RelativeDirection.LEFT;

				}

			case WEST:
				switch (targetDir){
					case NORTH:
						return WorldSpatial.RelativeDirection.RIGHT;

					case SOUTH:
						return WorldSpatial.RelativeDirection.LEFT;

				}

			case NORTH:
				switch (targetDir){
					case EAST:
						return WorldSpatial.RelativeDirection.RIGHT;

					case WEST:
						return WorldSpatial.RelativeDirection.LEFT;
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
	
	
	
	
	
	private void debugPrint(String message){
		if(debug){
			System.out.println("DEBUGGER: "+ message);
		}
	}
	
	
	
	

	
	
	private Graph initialiseDijkstras(Coordinate startingPosition) {
		Node startingNode = new Node("no");
		Graph graph = new Graph();
		for(Coordinate i: wholeMap.keySet()) {
			//Does not add node if it's of type WALL
			Node newNode = new Node(i.toString());
			graph.addNode(newNode);
		}
		for(Node currNode : graph.getNodes()) {
			String nodeName = currNode.getName(); //e.g. 1,2
			String[] splitCoordinate = nodeName.split(",");
			int x = Integer.parseInt(splitCoordinate[0]);
			int y = Integer.parseInt(splitCoordinate[1]);
			ArrayList<String> coords = new ArrayList<>();
			String right = String.format("%d,%d", x+1, y);
			String left = String.format("%d,%d", x-1, y);
			String up = String.format("%d,%d", x, y+1);
			String down = String.format("%d,%d", x, y-1);
			coords.add(right);
			coords.add(left);
			coords.add(up);
			coords.add(down);
			//Looks for the adjacent nodes in graph
			for(Node cNode: graph.getNodes()) {
				for(String adjacentCoords : coords) {
					if(cNode.getName().equals(adjacentCoords)) {
						int danger = 1;
						if(wholeMap.get(new Coordinate(cNode.getName())) instanceof LavaTrap) {
							danger = 5;
						}
						if(wholeMap.get(new Coordinate(cNode.getName())).getType() == Type.WALL) {
							danger = 100000;
						}
						currNode.addDestination(cNode, danger);
					}
				}
				//More efficient to do startingNode retrieval here
				if(cNode.getName().equals(startingPosition.toString())) {
					startingNode = cNode;
				}
			}
		}
		return Graph.calculateShortestPathFromSource(graph, startingNode);
	}
	
	
	
	
	
	private List<Node> exploreDijkstras(Coordinate startingPosition) {
		Node startingNode = new Node("no");
		Graph graph = new Graph();
		List<Node> returnPlaceholder = new ArrayList<>();
		
		//Build graph --------------------------------------------------------------
		for(Coordinate i: wholeMap.keySet()) {
			//Does not add node if it's of type WALL
			Node newNode = new Node(i.toString());
			graph.addNode(newNode);
		}
		
		for(Node currNode : graph.getNodes()) {
			String nodeName = currNode.getName(); //e.g. 1,2
			String[] splitCoordinate = nodeName.split(",");
			int x = Integer.parseInt(splitCoordinate[0]);
			int y = Integer.parseInt(splitCoordinate[1]);
			ArrayList<String> coords = new ArrayList<>();
			String right = String.format("%d,%d", x+1, y);
			String left = String.format("%d,%d", x-1, y);
			String up = String.format("%d,%d", x, y+1);
			String down = String.format("%d,%d", x, y-1);
			coords.add(right);
			coords.add(left);
			coords.add(up);
			coords.add(down);
			//Looks for the adjacent nodes in graph
			for(Node cNode: graph.getNodes()) {
				for(String adjacentCoords : coords) {
					if(cNode.getName().equals(adjacentCoords)) {
						int danger = 1;
						if(wholeMap.get(new Coordinate(cNode.getName())) instanceof LavaTrap) {
							danger = 5;
						}
						if(wholeMap.get(new Coordinate(cNode.getName())).getType() == Type.WALL) {
							danger = 100000;
						}
						currNode.addDestination(cNode, danger);
					}
				}
				//More efficient to do startingNode retrieval here
				if(cNode.getName().equals(startingPosition.toString())) {
					startingNode = cNode;
				}
			}
		}
		
		//--------------------------------------------------------------
		Graph smollGraph = Graph.calculateShortestPathFromSource(graph,startingNode);
		
		
		ArrayList<Node> exposedNodeList = new ArrayList<>();	
		for(Node node : graph.getNodes()) {		
			boolean noWall = true;
			if(node.getAdjacentNodes().size() < 4 && wholeMap.get( new Coordinate(node.getName())).getType() != Type.WALL) {
				List<Node> currShort = node.getShortestPath();
				for(Node currNode : currShort) {
					if(getCoordType(currNode.getName()) == Type.WALL) {
						noWall = false; 
					}
				}
				
				if(noWall) {
					exposedNodeList.add(node);
				}
			}
		}
		Collections.sort(exposedNodeList, new Comparator<Node>() {
		    public int compare(Node obj1, Node obj2) {
		    	return obj1.getAdjacentNodes().size() - obj2.getAdjacentNodes().size();
		    }
		});
		Node nextDest = exposedNodeList.get(0);
		for(Node n : smollGraph.getNodes()) {
			if(n.getName().equals(nextDest.getName())) {
				System.out.println(n.getName());
				return n.getShortestPath();
			}
		}
		return returnPlaceholder; 
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * Readjust the car to the orientation we are in.
	 * @param lastTurnDirection
	 * @param delta
	 */
	private void readjust(WorldSpatial.RelativeDirection lastTurnDirection, float delta) {
		debugPrint("READJUSTING: "+ lastTurnDirection);
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
	private boolean checkLeftWall(WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView) {
		
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

	private boolean checkRightWall(WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView) {

		switch(orientation){
			case EAST:
				return checkSouth(currentView);
			case NORTH:
				return checkEast(currentView);
			case SOUTH:
				return checkWest(currentView);
			case WEST:
				return checkNorth(currentView);
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
	
	
	public MapTile.Type getCoordType(String coord){		
		return wholeMap.get(new Coordinate(coord)).getType();
	}
	private void applySafeLeftTurn(float delta){
//		if(peek(getVelocity(),90f, WorldSpatial.RelativeDirection.LEFT, delta).getCoordinate().equals(targetPos)){
//			applyLeftTurn(getOrientation(), delta);
//		}
		if(!checkLeftWall(getOrientation(),getView())){
			applyLeftTurn(getOrientation(), delta);
		}
	}
	private void applySafeRightTurn(float delta){
//		if(peek(getVelocity(),0f, WorldSpatial.RelativeDirection.RIGHT, delta).getCoordinate().equals(targetPos)){
//			applyRightTurn(getOrientation(), delta);
//		}
		if(!checkRightWall(getOrientation(), getView())){
			applyRightTurn(getOrientation(), delta);
		}
	}

	public void debugPrint(List<Node> path){
		if(debug){
			System.out.print("Debugger: [");
			for(Node i: path){
				System.out.print("("+i.getName()+")");
			}
			System.out.println("]");
		}

	}
	public void makeTestPath(){
		if(test == 0) {
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
