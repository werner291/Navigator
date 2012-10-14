package me.werner291.navigator;

import java.util.ArrayList;

public class PlannerNode {
	MapNode mapNode; int distance; PlannerNode previous = null; ArrayList<PlannerNode> next; int distLeft;
	
	PlannerNode(MapNode mapNode, int distance, int distLeft, PlannerNode previous){
		this.mapNode = mapNode; this.distance = distance;
		this.previous=previous; this.distLeft = distLeft;
		next = new ArrayList<PlannerNode>();
	}
}
