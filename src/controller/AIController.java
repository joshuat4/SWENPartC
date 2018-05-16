package controller;

import java.util.*;

import tiles.*;
import tiles.MapTile.Type;
import utilities.Coordinate;
import world.Car;
import world.World;
import world.WorldSpatial;

public class AIController extends CarController {
	
	// How many minimum units the wall is away from the player.
	private int wallSensitivity = 2;
	
	
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
	
	@Override
	public void update(float delta) {
		// Gets what the car can see
		HashMap<Coordinate, MapTile> currentView = getView();
		for(Coordinate i : currentView.keySet()) {
			
			//Building up wholeMap with data from currentView
			if(i.x >= 0 && i.y >= 0 && i.x < World.MAP_WIDTH && i.y < World.MAP_HEIGHT ) {
				wholeMap.put(i, currentView.get(i));
			}
	
		}
		
		if(hasReachedNextDest) {
			System.out.println("reacalculating");
			List<Node> result =exploreDijkstras(new Coordinate(carrrr.getPosition()));
			result.add(new Node("1,2"));
			pathList.add(result);
			hasReachedNextDest = false;      
		}
		
		
		//While no exposed nodes
//		if(wholeMap.keySet().size() >= (World.MAP_HEIGHT * World.MAP_WIDTH) && firstTime == 0) {
//			System.out.println(carrrr.getPosition().toString());
//			graphWithShortestPaths = initialiseDijkstras(new Coordinate(carrrr.getPosition()));
//			//Look for keys
//			for(Coordinate j : wholeMap.keySet()) {
//				if(wholeMap.get(j) instanceof LavaTrap) {
//					LavaTrap l;
//					l = (LavaTrap)wholeMap.get(j);
//					if(l.getKey()==1 || l.getKey() == 2 || l.getKey() == 3) {
//						//If this succeeds j will be the coordinate of the first key
//						for(Node node : graphWithShortestPaths.getNodes()){
//							if(node.getName().equals(j.toString())) {
//								List<Node> result = node.getShortestPath(); 
//								System.out.println(l.getKey());  
////								System.out.println(result.toString());
//								for(Node k : result) {
//									System.out.print(k.getName() + "--");
//								}
//								System.out.print(node.getName());
//								System.out.print("\n");           
//							}
//						}
//					}
//					
//				}
//			}
//			
//			firstTime = 1;
//		}
//		
		
//		if(test == 0) {
//			Node node1 = new Node("3,3");
//			Node node2 = new Node("4,3");
//			Node node3 = new Node("5,3");
//			Node node4 = new Node("6,3");
//			Node node5 = new Node("7,3");
//			Node node6 = new Node("8,3");
//			Node node7 = new Node("9,3");
//			Node node8 = new Node("10,3");
//			Node node9 = new Node("11,3");
//			Node node10 = new Node("12,3");
//			Node node11 = new Node("13,3");
//			Node node12 = new Node("14,3");
//			Node node13 = new Node("15,3");
//			path.add(node1);
//			path.add(node2);
//			path.add(node3);
//			path.add(node4);
//			path.add(node5);
//			path.add(node6);
//			path.add(node7);
//			path.add(node8);
//			path.add(node9);
//			path.add(node10);
//			path.add(node11);
//			path.add(node12);
//			path.add(node13);
//			path.add(new Node("16,3"));
//			path.add(new Node("17,3"));
//			path.add(new Node("18,3"));
//			path.add(new Node("19,3"));
//			path.add(new Node("20,3"));
//			path.add(new Node("21,3"));
//			newPath.add(new Node("21,4"));
//			newPath.add(new Node("21,5"));
//			newPath.add(new Node("21,6"));
//			newPath.add(new Node("21,7"));
//			newPath.add(new Node("21,8"));
//			newPath.add(new Node("21,9"));
//			newPath.add(new Node("21,10"));
//			newPath.add(new Node("21,11"));
//			newPath.add(new Node("21,12"));
//			newPath.add(new Node("21,13"));
//			newPath.add(new Node("21,14"));
//			newPath.add(new Node("21,15"));
//			newPath.add(new Node("22,15"));
//			newPath.add(new Node("23,15"));
//			test = 1;
//		}
//		
		
		
		
		
		
		//Car movement code-----------------------------------------------------------------------------------
		
		//System.out.println(pathList.size()) ;
		//If there's a path in the path array to follow
		if(pathList.size() > 0 || processing) {
			if(processing) {
				checkStateChange();
				
				//Identify if target tile is N,S,E or W.
				try {
					path.get(counter+1);
				}
				catch(Exception e) {
					counter = 0;
					processing = false;
					hasReachedNextDest = true;
				}
				Coordinate currPos = new Coordinate (getPosition());
				Coordinate targetPos = new Coordinate(path.get(counter).getName());
				WorldSpatial.Direction dir = getDirection(currPos, targetPos);
				boolean isFacingTarget = dir.equals(getOrientation());
				// If we are on the target, move on to next.
				if(currPos.equals(targetPos)) {
					System.out.println(currPos);
					System.out.println(targetPos);
					System.out.println("-------");
					counter++;
					currPos = targetPos;
					targetPos = new Coordinate(path.get(counter).getName()); 
					if(!isFacingTarget){
						isTurningSoon = true;
					}
				}
				else {
					//Not on target yet.
					if(!isFacingTarget) {
						//If not moving in Direction of target Coord:
						if(getOrientation().equals(WorldSpatial.Direction.EAST)) {
							if(dir == WorldSpatial.Direction.SOUTH) {
								lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
								applyRightTurn(getOrientation(), delta);
							}
							if(dir == WorldSpatial.Direction.NORTH) {
								lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
								applyLeftTurn(getOrientation(), delta);
							}
						}
						if(getOrientation().equals(WorldSpatial.Direction.SOUTH)) {
							if(dir == WorldSpatial.Direction.WEST) {
								lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
								applyRightTurn(getOrientation(), delta);
							}
							if(dir == WorldSpatial.Direction.EAST) {
								lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
								applyLeftTurn(getOrientation(), delta);
							}
						}
						if(getOrientation().equals(WorldSpatial.Direction.WEST)) {
							if(dir == WorldSpatial.Direction.NORTH) {
								lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
								applyRightTurn(getOrientation(), delta);
							}
							if(dir == WorldSpatial.Direction.SOUTH) {
								lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
								applyLeftTurn(getOrientation(), delta);
							}
						}
						if(getOrientation().equals(WorldSpatial.Direction.NORTH)) {
							if(dir == WorldSpatial.Direction.EAST) {
								lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
								applyRightTurn(getOrientation(), delta);
							}
							if(dir == WorldSpatial.Direction.WEST) {
								lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
								applyLeftTurn(getOrientation(), delta);
							}
						}
						
						
					} else {
						readjust(lastTurnDirection, delta);
						//If moving in right direction,
						//Accelerate if not traveling at max speed
						float x = CAR_SPEED;
						if(isTurningSoon) {
							x = CAR_SPEED/8f;
						}
						if(getSpeed() < x){
							isTurningSoon = false;
							applyForwardAcceleration();
						}
						
					}
					
					
					
				}
				
			} else {
				path = pathList.poll();
				processing = true; 
				
			}
	
			
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
	
	
	
		
	
	
	
	private WorldSpatial.Direction getDirection(Coordinate source, Coordinate destination){
		if(destination.x > source.x) {
			return WorldSpatial.Direction.EAST;
		} else if(destination.x < source.x) {
			return WorldSpatial.Direction.WEST;
		} else if(destination.y > source.y) {
			return WorldSpatial.Direction.NORTH;
		} else {
			return WorldSpatial.Direction.SOUTH;
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
	
	
	public MapTile.Type getCoordType(String coord){		
		return wholeMap.get(new Coordinate(coord)).getType();
	}
	
}
