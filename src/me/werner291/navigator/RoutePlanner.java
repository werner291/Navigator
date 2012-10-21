package me.werner291.navigator;

import java.util.ArrayList;
import java.util.Collections;

public class RoutePlanner {
	
	PlannerNode start;
	ArrayList<PlannerNode> openNodes;
	ArrayList<MapNode> closedNodes;
	MapNode destNode1,destNode2;
	ArrayList<MapNode> rawRoute;
	int destX,destY,destZ;
	MapRoad destRoad;
	ArrayList<Instruction> route;
	
	RoutePlanner(RoadMap roadMap, int startX, int startY, int startZ, int destX, int destY, int destZ){
		// Locate the starting point on the map
		MapRoad startRoad = roadMap.getNearestRoad(startX, startY, startZ);
		MapNode node1 = startRoad.node1; MapNode node2 = startRoad.node2;
		
		double[] startRoadPoint = startRoad.getClosestPoint(startX, startY, startZ);
		
		// Make a "Ghost node" to start from.
		MapNode startNode = new MapNode((int)startRoadPoint[0],(int)startRoadPoint[1],(int)startRoadPoint[2], 0);
		startNode.roads.add(new Object[]{null,node1});
		startNode.roads.add(new Object[]{null,node2});
		
		//Find the road that is nearest to the destination
		destRoad = roadMap.getNearestRoad(destX, destY, destZ);
		double[] endRoadPoint = destRoad.getClosestPoint(destX, destY, destZ);
		this.destX = (int)endRoadPoint[0];
		this.destY = (int) endRoadPoint[1];
		this.destZ = (int) endRoadPoint[2];
		destNode1 = destRoad.node1; destNode2 = destRoad.node2;
		
		this.start = new PlannerNode(startNode, 0, Math.abs(this.destX-startX)+Math.abs(this.destZ-startZ), null);
		openNodes = new ArrayList<PlannerNode>();
		closedNodes = new ArrayList<MapNode>();
	}
	
	public String findRoute(){
		
		//Start the process by looking at all available directions from the starting point.
		//And closing the starting point.
		
		closedNodes.add(start.mapNode);
		
		for (int i=0;i<start.mapNode.roads.size();i++){
			MapNode newConn = (MapNode) start.mapNode.roads.get(i)[1];
			if (!closedNodes.contains(newConn)){
				int dist = Math.abs(start.mapNode.x-newConn.x)+Math.abs(start.mapNode.z-newConn.z);
				
				PlannerNode newPNode = new PlannerNode(newConn, dist, Math.abs(destX-newConn.x)+Math.abs(destZ-newConn.z), start);
				start.next.add(newPNode); openNodes.add(newPNode);
			}
		}
		
		boolean working = true;
		
		PlannerNode lastNode = null;
		
		// Apply an a*-like algorithm to find the shortest route.
		
		while (working){
			// Get, from the open nodes, the most potential one.
			int dist = Integer.MAX_VALUE; PlannerNode node = null;
			for (int i=0;i<openNodes.size();i++){
				if (openNodes.get(i).distance+openNodes.get(i).distLeft<dist
						&&!(closedNodes.contains(openNodes.get(i).mapNode))){
					dist = openNodes.get(i).distance+openNodes.get(i).distLeft; node = openNodes.get(i);
				}
			}
			
			if (node == null) return "Failed";
			
			// Add all nodes that this most potential node is connected to and that are open to the open nodes list
			if (node != null){
				for (int i=0;i<node.mapNode.roads.size();i++){
					
					MapNode newConn = (MapNode) (node.mapNode.roads.get(i))[1];
					if (!closedNodes.contains(newConn)){
						dist = node.distance+Math.abs(node.mapNode.x-newConn.x)+Math.abs(node.mapNode.z-newConn.z);
						
						
						PlannerNode newPNode = new PlannerNode(newConn, dist,Math.abs(destX-newConn.x)+Math.abs(destZ-newConn.z), node);
						start.next.add(newPNode); openNodes.add(newPNode);
					}
				}
				// Add the current node to the list of closed nodes because it has already been treated.
				closedNodes.add(node.mapNode);
				openNodes.remove(node);
			}
			
			if (node.mapNode == destNode1 || node.mapNode == destNode2){
				working = false; lastNode = node;
			}
		}
		
		ArrayList<MapNode> route = new ArrayList<MapNode>();
		
		// Add the destination at the top of the list
		route.add(new MapNode(destX, destY, destZ, 0));
		
		// Follow up the path from the last node to the first to find the actual route
		PlannerNode treatedNode = lastNode;
		
		while (treatedNode.previous != null){
			route.add(treatedNode.mapNode);
			treatedNode = treatedNode.previous;
		}
		
		// Add the start node to the route
		route.add(start.mapNode);
		
		// Since the nodes were entered from last to first, the route has to be reversed.
		Collections.reverse(route);
		
		this.rawRoute = route;
		
		this.route = createRoute();
		
		return "Success";	
	}

	public ArrayList<Instruction> createRoute() {
		
		ArrayList<Instruction> route = new ArrayList<Instruction>();
		
		for (int i=0;i<rawRoute.size();i++){
			MapNode node = rawRoute.get(i);
			
			if (i==0){
				MapNode next = rawRoute.get(i+1);
				int dir = (int) Math.toDegrees(Math.atan2(-(next.z-node.z), next.x-node.x));
				route.add(new Instruction(node.x,node.y, node.z, 4, Instruction.InstructionType.START,RoutePlanner.toCardinal(dir, 90, false)));
			} else if (i==rawRoute.size()-1){
				route.add(new Instruction(node.x,node.y, node.z, 4, Instruction.InstructionType.END,Cardinal.NULL));
			} else {
				MapNode next = rawRoute.get(i+1);
				MapNode prev = rawRoute.get(i-1);
				
				int dir = (int) Math.toDegrees(Math.atan2(-(next.z-node.z), next.x-node.x));
				int prevDir = (int) Math.toDegrees(Math.atan2(-(node.z-prev.z), node.x-prev.x));
				int relDir = dir-prevDir;
				
				if (relDir < 180) relDir += 360;
				if (relDir > 180) relDir -= 360;
				
				if (relDir < 30 && relDir > -30) route.add(new Instruction(node.x, node.y, node.z, 5, Instruction.InstructionType.GO_STRAIGHT, RoutePlanner.toCardinal(dir, 90, false)));
				if (relDir < 150 && relDir > 30) route.add(new Instruction(node.x, node.y, node.z, 5, Instruction.InstructionType.TURN_LEFT,RoutePlanner.toCardinal(dir, 90, false)));
				if (relDir < -30 && relDir > -150) route.add(new Instruction(node.x, node.y, node.z, 5, Instruction.InstructionType.TURN_RIGHT,RoutePlanner.toCardinal(dir, 90, false)));
				if ((relDir > 150 && relDir <= 180)||(relDir < -150 && relDir >= -180)) route.add(new Instruction(node.x, node.y, node.z, 5,
						Instruction.InstructionType.TURN_AROUND,RoutePlanner.toCardinal(dir, 90, false)));
			}
		}
		return route;
	}

	public static Cardinal toCardinal(int dir, int NorthAngle, boolean clockwize) {
		
		dir -= NorthAngle;
		
		if (clockwize) dir = -dir;
		
		if (dir < 180) dir += 360;
		if (dir > 180) dir -= 360;
		
		if (dir == 0) return Cardinal.NORTH;
		if (dir == -90) return Cardinal.EAST;
		if (dir == 90) return Cardinal.WEST;
		if (dir == 180 || dir == -180) return Cardinal.SOUTH;
		
		if (dir == 45) return Cardinal.NORTH_WEST;
		if (dir == -45) return Cardinal.NORTH_EAST;
		if (dir == 135) return Cardinal.SOUTH_WEST;
		if (dir == -135) return Cardinal.SOUTH_EAST;
		
		if (dir < 45 && dir > -45) return Cardinal.NORTH;
		if (dir < -45 && dir > -135) return Cardinal.EAST;
		if (dir < -135 && dir > -180) return Cardinal.SOUTH;
		if (dir < 180 && dir > 135) return Cardinal.SOUTH;
		if (dir < 135 && dir > 45) return Cardinal.WEST;
		
		return Cardinal.NORTH;
	}
	
	ArrayList<Instruction> getRoute(){
		return route;
	}
	
}
