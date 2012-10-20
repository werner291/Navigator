package me.werner291.navigator;


public class MapDest{
		public String name;
		public MapNode closestNode;
		public int x,y,z;
		
		public MapDest(int x, int y, int z, MapNode closestNode, String name) {
			this.x = x; this.y = y; this.z = z;
			this.name = name;
			this.closestNode = closestNode;
		}
		
		// Return a string summarizing this object for save file writing
		public String getWriteString() {
			return "Dest "+name+" "+x+" "+y+" "+z;
	}
}
