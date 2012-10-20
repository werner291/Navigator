package me.werner291.navigator;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class EditorSession {
	
	EditorSession(Player owner, RoadMap rdmap){
		this.owner = owner;
		drawn_pixels = new ArrayList<int[]>();
		this.rdmap = rdmap;
		this.world = owner.getWorld();
	}
	
	World world;
	Player owner;
	RoadMap rdmap;
	public MapNode nodeA;
	public MapNode nodeB;
	boolean draw_map;
	int draw_map_height;
	List<int[]> drawn_pixels;
	public MapRoad selRoad;
	
	
	public void drawMap() {
		eraseDrawnMap();
		draw_map = true;
		draw_map_height = height;
		List<MapNode> toDraw = rdmap.getNodesAround(owner.getLocation().getBlockX(),owner.getLocation().getBlockZ(),100);
		for (int i=0;i<toDraw.size();i++){
			DrawGhostBlock(toDraw.get(i).x,toDraw.get(i).y+2,toDraw.get(i).z,Material.GLOWSTONE);
			
			DrawGhostBlock(toDraw.get(i).x,toDraw.get(i).y+3,toDraw.get(i).z,Material.GLOWSTONE);
			DrawGhostBlock(toDraw.get(i).x-1,toDraw.get(i).y+3,toDraw.get(i).z,Material.GLOWSTONE);
			DrawGhostBlock(toDraw.get(i).x+1,toDraw.get(i).y+3,toDraw.get(i).z,Material.GLOWSTONE);
			DrawGhostBlock(toDraw.get(i).x,toDraw.get(i).y+3,toDraw.get(i).z-1,Material.GLOWSTONE);
			DrawGhostBlock(toDraw.get(i).x,toDraw.get(i).y+3,toDraw.get(i).z+1,Material.GLOWSTONE);
			
			DrawGhostBlock(toDraw.get(i).x,toDraw.get(i).y+4,toDraw.get(i).z,Material.GLOWSTONE);
		}
		
		List<MapRoad> roadsToDraw = rdmap.getRoadsAround(owner.getLocation().getBlockX(),owner.getLocation().getBlockZ(),100);
		for (int i=0;i<roadsToDraw.size();i++){
			DrawGhostBlock((int)(roadsToDraw.get(i).geoLine.getX1()*0.25+roadsToDraw.get(i).geoLine.getX2()*0.75),
						   (int)(roadsToDraw.get(i).geoLine.getY1()*0.25+roadsToDraw.get(i).geoLine.getY2()*0.75),
						   (int)(roadsToDraw.get(i).geoLine.getZ1()*0.25+roadsToDraw.get(i).geoLine.getZ2()*0.75),
						   Material.BRICK);
			DrawGhostBlock((int)(roadsToDraw.get(i).geoLine.getX1()*0.50),
					   (int)(roadsToDraw.get(i).geoLine.getY1()*0.50),
					   (int)(roadsToDraw.get(i).geoLine.getZ1()*0.50),
					   Material.BRICK);
			DrawGhostBlock((int)(roadsToDraw.get(i).geoLine.getX1()*0.75+roadsToDraw.get(i).geoLine.getX2()*0.25),
					   (int)(roadsToDraw.get(i).geoLine.getY1()*0.75+roadsToDraw.get(i).geoLine.getY2()*0.25),
					   (int)(roadsToDraw.get(i).geoLine.getZ1()*0.75+roadsToDraw.get(i).geoLine.getZ2()*0.25),
					   Material.BRICK);
		}
		List<MapDest> destsToDraw = rdmap.getDestinationsAround(owner.getLocation().getBlockX(),owner.getLocation().getBlockZ(),100);
		for (int i=0;i<destsToDraw.size();i++){
			
			DrawGhostBlock(destsToDraw.get(i).x,draw_map_height,destsToDraw.get(i).z,Material.WOOD);
		}
	}
	
	public void eraseDrawnMap() {
		for (int i=0;i<drawn_pixels.size();i++){
			int[] coords = drawn_pixels.get(i);
			Block original = world.getBlockAt(coords[0], coords[1], coords[2]);
			owner.sendBlockChange(original.getLocation(),original.getType(),original.getData());
		}
	}
	
	void DrawGhostBlock(int x, int y, int z, Material mat){
		if (world.getBlockAt(x,y,z).getType()!=Material.AIR) return;
		owner.sendBlockChange(new Location(owner.getWorld(),x,y,z),mat,(byte)0);
		drawn_pixels.add(new int[]{x,y,z});
	}
}
