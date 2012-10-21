package me.werner291.navigator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import me.werner291.navigator.IdManager.IdConflictException;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class RoadMap {

	boolean oldCompatibilityMode;
	
	File file = new File("world.map");
	World world;
	ArrayList<MapNode> nodes;
	HashMap<Integer, MapNode> nodeMap;
	ArrayList<MapRoad> roads;
	ArrayList<MapDest> destinations;

	public RoadMap() {
		nodes = new ArrayList<MapNode>();
		nodeMap = new HashMap<Integer, MapNode>();
		roads = new ArrayList<MapRoad>();
		destinations = new ArrayList<MapDest>();
	}

	public RoadMap(File file, World world) {
		nodes = new ArrayList<MapNode>();
		nodeMap = new HashMap<Integer, MapNode>();
		roads = new ArrayList<MapRoad>();
		destinations = new ArrayList<MapDest>();
		this.file = file;
		this.world = world;
	}

	public MapNode getNodeAt(int x2, int z2) {

		for (int i = 0; i < nodes.size(); i++) {
			MapNode node = nodes.get(i);

			if (Math.abs(node.x - x2) + Math.abs(node.z - z2) < 5)
				return node;
		}

		return null;
	}

	public void removeNode(MapNode node) {
		node.removeRoads(this);
		nodes.remove(node);
	}

	public void addRoad(MapRoad mapRoad) {
		roads.add(mapRoad);
		mapRoad.node1.roads.add(new Object[] { mapRoad, mapRoad.node2 });
		mapRoad.node2.roads.add(new Object[] { mapRoad, mapRoad.node1 });
	}

	public void removeRoad(MapRoad road) {
		roads.remove(road);
		road.node1.removeRoad(road);
		road.node2.removeRoad(road);
	}

	void loadFromFile(File file) throws IOException {
		int fileVersion = 1;

		// Initialize reader
		final BufferedReader in = new BufferedReader(new FileReader(file));
		String str;
		// Read entire file loading in the map nodes
		while ((str = in.readLine()) != null) {
			// Split up input string
			String Words[] = str.split(" ");
			if (Words[0].equals("SAVEFORMAT")) {
				oldCompatibilityMode = false;
				fileVersion = Integer.parseInt(Words[1]);
			}

			else if (Words[0].equals("Node")) {
				MapNode newNode;
				if (fileVersion == 1) // Old format support
					newNode = new MapNode(Integer.parseInt(Words[2]),80,
						Integer.parseInt(Words[3]), Integer.parseInt(Words[1]));
				else
					newNode = new MapNode(
							Integer.parseInt(Words[2]), //X
							Integer.parseInt(Words[3]), //Y
							Integer.parseInt(Words[4]), //Z
							Integer.parseInt(Words[1]));//id
				nodes.add(newNode);
				nodeMap.put(newNode.id, newNode);
				
				try {
					IdManager.registerId(newNode.id);
				} catch (IdConflictException e) {
					newNode.id = IdManager.createId();
				}
			} else if (Words[0].equals("Road")) {
				MapNode node1 = nodeMap.get(Integer.parseInt(Words[2]));
				MapNode node2 = nodeMap.get(Integer.parseInt(Words[3]));
				addRoad(new MapRoad(node1, node2, Integer.parseInt(Words[1])));
				try {
					IdManager.registerId(Integer.parseInt(Words[1]));
				} catch (IdConflictException e) {
					e.printStackTrace();
				}
			} else if (Words[0].equals("Dest")) {
				MapDest newDest;
				
				if (fileVersion == 1) // Old format support
				 newDest= new MapDest(
						 Integer.parseInt(Words[2]),//X
						 80,//Y
						 Integer.parseInt(Words[3]),//Z
						 getNearestNode(Integer.parseInt(Words[2]), //X
								 		80,//Y
								 		Integer.parseInt(Words[3])),//Z
						Words[1].toLowerCase());//Name
				else // Old format support
					 newDest= new MapDest(
							 Integer.parseInt(Words[2]),//X
							 Integer.parseInt(Words[3]),//Y
							 Integer.parseInt(Words[4]),//Z
							 getNearestNode(Integer.parseInt(Words[2]), //X
									 		Integer.parseInt(Words[3]),//Y
									 		Integer.parseInt(Words[4])),//Z
							Words[1].toLowerCase());//Name
				destinations.add(newDest);
			}
		}

		in.close();
	}

	public MapNode getNearestNode(int x, int y, int z) {
		MapNode closest = null;
		int dist = Integer.MAX_VALUE;
		for (int i = 0; i < nodes.size(); i++) {
			MapNode current = nodes.get(i);
			
			int dist2 = current.getManhattanDistance(x, y, z);
			if (dist2 < dist) {
				dist = dist2;
				closest = current;
			}
		}

		return closest;
	}

	public MapRoad getNearestRoad(int x, int y, int z) {
		MapRoad closest = null;
		int dist = Integer.MAX_VALUE;

		for (int i = 0; i < roads.size(); i++) {
			MapRoad current = roads.get(i);

			if (current.node1 == current.node2) {
				System.out.println("Found a node connected to itself!");
			} else {

				int dist2 = (int) current.distanceFromPoint(x, y, z);

				if (dist2 < dist) {
					dist = dist2;
					closest = current;
				}
			}
		}

		return closest;
	}

	public MapDest searchDestination(String name) {
		ArrayList<MapDest> partialMatch = new ArrayList<MapDest>();

		for (int i = 0; i < destinations.size(); i++) {
			MapDest iDest = destinations.get(i);
			if (iDest.name.equalsIgnoreCase(name))
				return iDest;
			else if (iDest.name.toLowerCase().contains(name.toLowerCase()))
				partialMatch.add(iDest);
		}

		return null;
	}

	public MapDest getDestinationExact(String name) {

		for (int i = 0; i < destinations.size(); i++) {
			MapDest iDest = destinations.get(i);
			if (iDest.name.equalsIgnoreCase(name))
				return iDest;
		}

		return null;
	}

	public void listDestinations(Player requester, int index) {

		if (index > destinations.size()) {
			requester.sendMessage("[Navigator] Too high index, maximum is "
					+ destinations.size() + '.');
			return;
		}
		ArrayList<String> destNameList = new ArrayList<String>();

		for (int i = 0; i < destinations.size(); i++) {
			destNameList.add(destinations.get(i).name);
		}

		Collections.sort(destNameList);

		requester
				.sendMessage("[Navigator] Listing available destinations from index "
						+ index);
		requester
				.sendMessage("[Navigator] ------------------------------------------");

		for (int i = index; i < destNameList.size(); i++) {
			requester.sendMessage("[Navigator] " + destNameList.get(i));

			if (i - index > 6)
				break;
		}

	}

	// Save map to filesystem.
	public void save(File saveFile) throws IOException {

		if (saveFile == null)
			saveFile = file;
		else
			file = saveFile;

		BufferedWriter out = new BufferedWriter(new FileWriter(saveFile, false));

		out.write("SAVEFORMAT 2");
		out.newLine();
		out.flush();

		for (int i = 0; i < nodes.size(); i++) {
			String writeString = null;

			MapNode node = nodes.get(i);

			writeString = node.getWriteString();

			if (writeString != null) {
				out.write(writeString);
				out.newLine();
				out.flush();
			}
		}
		for (int i = 0; i < roads.size(); i++) {
			String writeString = null;

			MapRoad road = roads.get(i);

			// Don't save roads that connect a node to itself

			if (road.node1 != road.node2) {
				writeString = road.getWriteString();

				if (writeString != null) {
					out.write(writeString);
					out.newLine();
					out.flush();
				}
			}
		}

		for (int i = 0; i < destinations.size(); i++) {
			String writeString = null;

			MapDest dest = destinations.get(i);

			writeString = dest.getWriteString();

			if (writeString != null) {
				out.write(writeString);
				out.newLine();
				out.flush();
			}
		}
		out.close();
	}

	public void AddNode(int x, int y, int z) {
		nodes.add(new MapNode(x, y, z, IdManager.createId()));
	}

	public void AddRoad(MapNode nodeA, MapNode nodeB) {
		addRoad(new MapRoad(nodeA, nodeB, IdManager.createId()));
	}

	public boolean AddDest(int x, int y, int z, String name) {
		name = name.toLowerCase();

		if (getDestinationExact(name) == null) {
			destinations.add(new MapDest(x, y, z, getNearestNode(x, y, z), name));
			return true;
		} else
			return false;
	}

	// Get all nodes that are maximally dist from (x,z).
	public List<MapNode> getNodesAround(int x, int z, int dist) {
		List<MapNode> results = new ArrayList<MapNode>();

		for (int i = 0; i < nodes.size(); i++) {
			MapNode current = nodes.get(i);
			int x2 = current.x;
			int z2 = current.z;
			int dist2 = Math.abs(x - x2) + Math.abs(z - z2);
			if (dist2 <= dist)
				results.add(current);
			System.out.println("searching node: " + dist2 + "<" + dist);
		}

		return results;
	}

	public List<MapDest> getDestinationsAround(int x, int z, int dist) {
		List<MapDest> results = new ArrayList<MapDest>();

		for (int i = 0; i < destinations.size(); i++) {
			MapDest current = destinations.get(i);
			int x2 = current.x;
			int z2 = current.z;
			int dist2 = Math.abs(x - x2) + Math.abs(z - z2);
			if (dist2 <= dist)
				results.add(current);
			System.out.println("searching node: " + dist2 + "<" + dist);
		}

		return results;
	}

	public List<MapRoad> getRoadsAround(int x, int y, int z, int dist) {
		List<MapRoad> results = new ArrayList<MapRoad>();

		for (int i = 0; i < roads.size(); i++) {
			MapRoad current = roads.get(i);
			int dist2 = (int) current.distanceFromPoint(x, y, z);
			if (dist2 <= dist)
				results.add(current);
			System.out.println("searching node: " + dist2 + "<" + dist);
		}

		return results;
	}

	public void removeDestination(MapDest dest) {
		destinations.remove(dest);
	}
}
