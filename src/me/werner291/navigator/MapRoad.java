package me.werner291.navigator;

public class MapRoad {
	
	MapNode node1; MapNode node2; int id;
	
	int x1,y1,z1,x2,y2,z2;
	
	public MapRoad(MapNode node1, MapNode node2, int id) {
		this.node1 = node1; this.node2 = node2;
		
		this.id = id;
	}

	public String getWriteString() {
		return "Road "+id+" "+node1.id+" "+node2.id;
	}
	
	/**
	 * @return A double array with 3 doubles representing a 3d point {x,y,z}
	 */
	public double[] getClosestPoint(int px, int py, int pz) {
		x1 = node1.x; y1 = node1.y; z1 = node1.z;
		x2 = node2.x; y2 = node2.y; z2 = node2.z;
		
		System.out.println("Searching closest point!");
		
		double[] uVec = new double[]{x2-x1,y2-y1,z2-z1};
    	double[] vVec = new double[]{px-x1,py-y1,pz-z1};
    	
    	double uLen = Math.sqrt(uVec[0]*uVec[0]+uVec[1]*uVec[1]+uVec[2]*uVec[2]);
    	uVec = new double[]{uVec[0]/uLen,uVec[1]/uLen,uVec[1]/uLen};
    	
    	double udotv = uVec[0]*vVec[0]+uVec[1]*vVec[1]+uVec[2]*vVec[2];
    	
    	return new double[]{x1+uVec[0]*udotv,y1+uVec[1]*udotv,z1+uVec[2]*udotv};
	}

	public double distanceFromPoint(int px, int py, int pz) {
		x1 = node1.x; y1 = node1.y; z1 = node1.z;
		x2 = node2.x; y2 = node2.y; z2 = node2.z;
		
		// Square distance between the nodes of the road
		double l2 = Math.abs((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1)+(z2-z1)*(z2-z1));
		
		// If the road is 0 in length return the distance between the point and the first node
		if (l2 == 0.0) return Math.sqrt((px-x1)*(px-x1)+(py-y1)*(py-y1)+(pz-z1)*(pz-z1));
		// Consider the line extending the segment, parameterized as v + t (w - v).
		// We find projection of point p onto the line. 
		// It falls where t = [(p-v) . (w-v)] / |w-v|^2
		double t = ((px-x1)*(x2-x1)
				   +(py-y1)*(y2-y1)
				   +(pz-z1)*(z2-z1)
				   )/l2;
		
		if (t < 0.0) return Math.sqrt((px-x1)*(px-x1)
				+(py-y1)*(py-y1)
				+(pz-z1)*(pz-z1));// Beyond the 'v' end of the segment
		else if (t > 1.0) return Math.sqrt((px-x2)*(px-x2)
				+(py-y2)*(py-y2)
				+(pz-z2)*(pz-z2));  // Beyond the 'w' end of the segment
		double resultX = x1+ t * (x2-x1);  // Projection falls on the segment
		double resultY = y1+ t * (y2-y1);
		double resultZ = z1+ t * (z2-z1);
		return Math.sqrt((px-resultX)*(px-resultX)
				+(py-resultY)*(py-resultY)
				+(pz-resultZ)*(pz-resultZ));
	}
}
