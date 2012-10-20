package me.werner291.navigator;

import me.werner291.navigator.vecmath.VecMath2D;

public class MapRoad {
	
	MapNode node1; MapNode node2; int id;
	
	int x1,y1,z1,x2,y2,z2;
	
	public MapRoad(MapNode node1, MapNode node2, int id) {
		this.node1 = node1; this.node2 = node2;
		x1 = node1.x; y1 = node1.y; z1 = node1.z;
		x2 = node2.x; y2 = node2.y; z2 = node2.z;
		this.id = id;
	}

	public String getWriteString() {
		return "Road "+id+" "+node1.id+" "+node2.id;
	}
	
	/**
	 * @return A double array with 3 doubles representing a 3d point {x,y,z}
	 */
	public double[] getClosestPoint(int x, int y, int z) {
		return VecMath2D.getClosestPointOnSegment(node1.x,node1.y,node1.z, node2.x,node2.y,node2.z, x, y, z);
	}

	public double distanceFromPoint(int x, int y, int z) {
		return VecMath2D.distanceToSegment(x1, y1, z1, x2, y2, z2, x, y, z);
	}
}
