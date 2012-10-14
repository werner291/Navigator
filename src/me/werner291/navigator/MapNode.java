package me.werner291.navigator;

import java.util.ArrayList;

public class MapNode {
	public int id;
	public int x; public int z;
	public ArrayList<Object[]> roads;
	
	public MapNode(int x, int z, int i) {
		this.x = x; this.z = z;
		roads = new ArrayList<Object[]>();
		
		id = i;
	}

	public void removeRoads(RoadMap roadMap) {
		for (int i=0; i<roads.size();i++){
			roadMap.removeRoad((MapRoad) roads.get(i)[0]);
		}
		
	}

	public void removeRoad(MapRoad road) {
		for (int i=0;i<roads.size();i++){
			if (roads.get(i)[1]==road){
				roads.remove(i); return;
			}
		}
	}

	// Return a string summarizing this object for save file writing
	public String getWriteString() {
		return "Node "+id+" "+x+" "+z;
	}
	
	public int getManhattanDistance(int x2, int z2) {
		return Math.abs(x-x2)+Math.abs(z-z2);
	}
}
