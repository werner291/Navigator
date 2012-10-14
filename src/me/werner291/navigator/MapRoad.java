package me.werner291.navigator;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import me.werner291.navigator.vecmath.VecMath2D;

public class MapRoad {
	
	MapNode node1; MapNode node2; int id;
	Line2D geoLine;
	
	public MapRoad(MapNode node1, MapNode node2, int id) {
		this.node1 = node1; this.node2 = node2;
		geoLine = new Line2D.Double(node1.x, node1.z, node2.x, node2.z);
		this.id = id;
	}

	public String getWriteString() {
		return "Road "+id+" "+node1.id+" "+node2.id;
	}

	public Point2D getClosestPoint(int x, int z) {
		return VecMath2D.getClosestPointOnSegment(new Point2D.Double(node1.x,node1.z), new Point2D.Double(node2.x,node2.z), new Point2D.Double(x,z));
	}
}
