package me.werner291.navigator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class Navigator extends JavaPlugin implements Listener{
	
	PluginManager pluginManager;
	EventHandler eventHandler;
	
	private static HashMap<World,RoadMap> maps = new HashMap<World,RoadMap>();
	private static HashMap<Player, EditorSession> editor_sessions = new HashMap<Player,EditorSession>();
	
	static Navigator programInstance;
	ArrayList<Route> routes = new ArrayList<Route>();
	BukkitScheduler scheduler;
	
	Logger log = Logger.getLogger("Minecraft");
	
	@Override
	public void onDisable() {
		routes.clear(); routes = null; maps = null;
		log.info("Navigator disabled.");
	}

	@Override
	public void onEnable() {
		log.info("Navigator V1.5.1 enabled!");
		getConfig();
		log.info("[Navigator] Configuration loaded.");
		
		// Create our plugin's folder, plugins/Navigator if it doesn't exist.
		File NavigatorFile = new File("plugins/Navigator");
		if (!NavigatorFile.exists()) NavigatorFile.mkdir();
		
		List<World> worldList = Bukkit.getWorlds();
		
		for (int i=0;i<worldList.size();i++){;
			
			World w = worldList.get(i);
			try {
				File worldFile = new File("plugins/Navigator/"+w.getName()+".map");
				if (worldFile.exists()){
					RoadMap map = new RoadMap(worldFile,w);
					map.loadFromFile(worldFile);
					maps.put(worldList.get(i), map);
					log.info("[Navigator] Loaded map of world "+w.getName());
				} else {
					log.info("[Navigator] Could not find map of world "+w.getName());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		Communicator.Initialise(getConfig().getString("language"));
		
		scheduler = getServer().getScheduler();
		
		scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
			
		    public void run() {
		        update();
		    }
		}, 2L, 2L);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		
		if (args.length == 0 || args[0].equalsIgnoreCase("Help")) {
			
			if (sender.hasPermission("navigator.use")){
				sender.sendMessage("[Navigator] /nav go <destination>");
				sender.sendMessage("[Navigator] /nav list [index]");
				sender.sendMessage("[Navigator] /nav cancel");
			} else sender.sendMessage(ChatColor.DARK_RED+"[Navigator] Permission denied");
			if (sender.hasPermission("navigator.reload")){
				sender.sendMessage("[Navigator] /nav reload");
			}
			if (sender.hasPermission("navigator.edit")){
				sender.sendMessage("[Navigator] /nav edit (See BukkitDev page.)");
			}
			return true;
		}
		
		else if (args[0].equalsIgnoreCase("go")){
			
			if (!sender.hasPermission("navigator.use")){
				sender.sendMessage(ChatColor.DARK_RED+"[Navigator] Permission denied");
				return true;
			}
			
			if (sender instanceof Player){
				
				if (args.length < 2) {
					sender.sendMessage("[Navigator] /nav go <destination>");
					return true;
				}
				
				// Start a route planning from the player's current position to the Origin.
				
				Player player = (Player)sender;
				Location loc = player.getLocation();
				
				if (!maps.containsKey(loc.getWorld())){
					Communicator.tellNoMap(player,loc.getWorld().getName());
					return false;
				}
				
				MapDest destination = maps.get(loc.getWorld()).searchDestination(args[1]);
				
				if (destination == null){
					Communicator.tellMessage(player,"DestNotFound");
					return false;
				}
				// Check distance from closest road
				if (maps.get(loc.getWorld()).getNearestRoad(loc.getBlockX(), loc.getBlockZ()).geoLine.ptLineDist(loc.getBlockX(), loc.getBlockZ())>10){
					Communicator.tellMessage(player,"TooFarFromRoad");
					return true;
				}
				
				Communicator.tellRouteCalc(player, destination.name);
				
				RoutePlanner rp = new RoutePlanner(maps.get(loc.getWorld()),
						loc.getBlockX(), loc.getBlockZ(), destination.x, destination.z);
				
				String rpResult = rp.findRoute();
				
				if (rpResult != "Success"){
					Communicator.tellNoRoute(player,destination.name);
					return false;
				} else {
					Communicator.tellNavStart(player,destination.name);
				}
				ArrayList<Instruction> rawInstr = rp.getRoute();
				routes.add(new Route(player, rawInstr));
			} else {
				Communicator.tellMessage(sender, "InGameOnlyCommand");
			}
			
			return true;
		}
		// Go as close as possible to coordinates
		else if (args[0].equalsIgnoreCase("gocoords")){
			
			if (!sender.hasPermission("navigator.use")){
				sender.sendMessage(ChatColor.DARK_RED+"[Navigator] Permission denied");
				return true;
			}
			
			if (sender instanceof Player){
				
				if (args.length < 3) {
					sender.sendMessage("[Navigator] /nav gocoords x z");
					return true;
				}
				
				// Get player
				Player player = (Player)sender;
				Location loc = player.getLocation();
				
				// Check if there's a map of the current world
				if (!maps.containsKey(loc.getWorld())){
					Communicator.tellNoMap(player,loc.getWorld().getName());
					return false;
				}
				
				RoadMap rdmap = maps.get(loc.getWorld());
				
				//Declare dest X and Z variables.
				int destX,destZ;
				
				//Parse coordinates or die.
				try {
					destX = Integer.parseInt(args[1]);
					destZ = Integer.parseInt(args[1]);
				} catch (NumberFormatException ex){
					sender.sendMessage("[Navigator] /nav gocoords x z");
					sender.sendMessage("[Navigator] ex: /nav gocoords -500 2040");
					return true;
				}
				
				double dist = rdmap.getNearestRoad(destX, destZ).geoLine.ptSegDist(destX, destZ);
				
				// Check distance from closest road
				if (dist>10){
					Communicator.tellDestCoordsFarFromRoad(player, (int) dist);
				}
				
				player.sendMessage("Route is being calculated to "+destX+" "+destZ+".");
				
				RoutePlanner rp = new RoutePlanner(maps.get(loc.getWorld()),
						loc.getBlockX(), loc.getBlockZ(), destX, destZ);
				
				rp.findRoute();
				
				ArrayList<Instruction> rawInstr = rp.getRoute();
				routes.add(new Route(player, rawInstr));
			} else {
				Communicator.tellMessage(sender, "InGameOnlyCommand");
			}
			
			return true;
		} else if (args[0].equalsIgnoreCase("list")){
			
			if (!sender.hasPermission("navigator.use")){
				sender.sendMessage(ChatColor.DARK_RED+"[Navigator] Permission denied");
				return true;
			}
			
			if (sender instanceof Player){
				Player player = (Player)sender;
				
				if (!maps.containsKey(player.getLocation().getWorld())){
					Communicator.tellNoMap(player, player.getWorld().getName());
					return false;
				}
				
				if (args.length >= 2)
					maps.get(player.getLocation().getWorld()).listDestinations(player, Integer.parseInt(args[1]));
				else
					maps.get(player.getLocation().getWorld()).listDestinations(player, 0);
			} else {
				Communicator.tellMessage(sender, "InGameOnlyCommand");
			}
		}
		else if (args[0].equalsIgnoreCase("list")){
			
			if (!sender.hasPermission("navigator.use")){
				sender.sendMessage(ChatColor.DARK_RED+"[Navigator] Permission denied");
				return true;
			}
			
			if (sender instanceof Player){
				Player player = (Player)sender;
				
				if (!maps.containsKey(player.getLocation().getWorld())){
					Communicator.tellNoMap(player, player.getWorld().getName());
					return false;
				}
				
				if (args.length >= 2)
					maps.get(player.getLocation().getWorld()).listDestinations(player, Integer.parseInt(args[1]));
				else
					maps.get(player.getLocation().getWorld()).listDestinations(player, 0);
			} else {
				Communicator.tellMessage(sender, "InGameOnlyCommand");
			}
		} else if (args[0].equalsIgnoreCase("compass")){
			
			if (!sender.hasPermission("navigator.use")){
				sender.sendMessage(ChatColor.DARK_RED+"[Navigator] Permission denied");
				return true;
			}
			
			if (sender instanceof Player){
				Player player = (Player)sender;
				
				Communicator.sendCompass(player,RoutePlanner.toCardinal((int) player.getLocation().getYaw(), 180, true));
				
			} else {
				Communicator.tellMessage(sender, "InGameOnlyCommand");
			}
		} else if (args[0].equalsIgnoreCase("reload")){
			
			if (!sender.hasPermission("navigator.reload")){
				sender.sendMessage(ChatColor.DARK_RED+"[Navigator] Permission denied");
				return true;
			}
			
			sender.sendMessage("[Navigator] Reloading...");
			reloadNavigator();
			sender.sendMessage("[Navigator] Reloaded!");
			
			log.info("Navigator reloaded.");
			
		} else if (args[0].equalsIgnoreCase("cancel")){
			if (!sender.hasPermission("navigator.use")){
				sender.sendMessage(ChatColor.DARK_RED+"[Navigator] Permission denied");
				return true;
			}
			
			if (sender instanceof Player){
				
				Player player = (Player)sender;
				player.sendMessage("Navigation cancelled.");
				
				for (int i=0;i<routes.size();i++){
					Route route = routes.get(i);
					if (route.owner == player) routes.remove(route);
				}
			}
		// Edit road map command.
		} else if (args[0].equalsIgnoreCase("edit")){
			
			if (!sender.hasPermission("navigator.edit")){
				sender.sendMessage(ChatColor.DARK_RED+"[Navigator] Permission denied");
				return true;
			}
			
			if (sender instanceof Player){
				Player player = (Player)sender;
				
				if (!editor_sessions.containsKey(player)){
					player.sendMessage("[Navigator] Edit mode enabled!");
					player.sendMessage("[Navigator] !!DO NOT FORGET TO SAVE YOUR WORK!!");
					player.sendMessage("[Navigator] For help, please visit the Navigator BukkitDev page.");
					editor_sessions.put(player, new EditorSession(player, maps.get(player.getWorld())));
				}
				
				EditorSession editorSession = editor_sessions.get(player);
				
				if (args.length<2) {
					
					sender.sendMessage("[Navigator] /nav edit createmap");
					sender.sendMessage("[Navigator] /nav edit save");
					sender.sendMessage("[Navigator] /nav edit addnode");
					sender.sendMessage("[Navigator] /nav edit selnodea/selnodeb");
					sender.sendMessage("[Navigator] /nav edit delnodea/delnodeb");
					sender.sendMessage("[Navigator] /nav edit addroad (select nodes first!)");
					sender.sendMessage("[Navigator] /nav edit delroad (nearest road)");
					sender.sendMessage("[Navigator] /nav edit addDest <unique name>");
					sender.sendMessage("[Navigator] /nav edit delDest <unique name>");
					sender.sendMessage("[Navigator] /nav edit drawmap <height>/<disable>");
					return false;
				}
				
				// Create map of current world.
				if (args[1].equalsIgnoreCase("createmap")){
					if (maps.containsKey(player.getWorld())){player.sendMessage("[Navigator] There already is a map of this world."); return true;}
					
					RoadMap map = new RoadMap(new File("plugins/Navigator/"+player.getWorld().getName()+".map"),player.getWorld());
					try {
						map.save(null);
						maps.put(player.getWorld(), map);
						player.sendMessage("[Navigator] Empty map of world \""+map.world.getName()+"\" sucessfully created and saved.");
					} catch (IOException e) {
						e.printStackTrace();
						player.sendMessage("[Navigator] IOException. See console.");
					}
				}
				// Add map node
				if (args[1].equalsIgnoreCase("addnode")){
					RoadMap rdmap = maps.get(player.getWorld());
					// Check if map is available.
					if (rdmap == null) {Communicator.tellNoMap(player, player.getWorld().getName()); return true;}
					
					rdmap.AddNode(player.getLocation().getBlockX(),player.getLocation().getBlockZ());
					player.sendMessage("[Navigator] Added node at "+player.getLocation().getBlockX()+" "+player.getLocation().getBlockZ());
					
					if (editorSession.draw_map) editorSession.drawMap(editorSession.draw_map_height);
				}
				// Save map.
				if (args[1].equalsIgnoreCase("save")){
					RoadMap rdmap = maps.get(player.getWorld());
					// Check if map is available.
					if (rdmap == null) {Communicator.tellNoMap(player, player.getWorld().getName()); return true;}
					
					try {
						rdmap.save(null);
						player.sendMessage("[Navigator] Map \""+rdmap.world.getName()+"\" saved.");
					} catch (IOException e) {
						e.printStackTrace();
						player.sendMessage("[Navigator] IOException. See console.");
					}	
				}
				// Select node A
				if (args[1].equalsIgnoreCase("selnodea")){
					RoadMap rdmap = maps.get(player.getWorld());
					// Check if map is available.
					if (rdmap == null) {Communicator.tellNoMap(player, player.getWorld().getName()); return true;}
					editor_sessions.get(player).nodeA = rdmap.getNearestNode(player.getLocation().getBlockX(),
																			player.getLocation().getBlockZ());
					player.sendMessage("[Navigator] Node selected at x:" +editor_sessions.get(player).nodeA.x+" z:"+editor_sessions.get(player).nodeA.z);
					if (editor_sessions.get(player).nodeA == editor_sessions.get(player).nodeB)
						player.sendMessage("[Navigator] Warning: Selected the same node twice.");
				}
				// Delete the MapNode in slot A
				if (args[1].equalsIgnoreCase("delnodea")){
					RoadMap rdmap = maps.get(player.getWorld());
					// Check if map is available.
					if (rdmap == null) {Communicator.tellNoMap(player, player.getWorld().getName()); return true;}
					if (editor_sessions.get(player).nodeA == null){player.sendMessage("[Navigator] No node selected in slot A."); return true;}
					
					player.sendMessage("[Navigator] Node deleted at x:" +editor_sessions.get(player).nodeA.x+" z:"+editor_sessions.get(player).nodeA.z);
					rdmap.removeNode(editor_sessions.get(player).nodeA);
					if (editorSession.draw_map) editorSession.drawMap(editorSession.draw_map_height);
				}
				// Delete the MapNode in slot B
				if (args[1].equalsIgnoreCase("delnodeb")){
					RoadMap rdmap = maps.get(player.getWorld());
					// Check if map is available.
					if (rdmap == null) {Communicator.tellNoMap(player, player.getWorld().getName()); return true;}
					if (editor_sessions.get(player).nodeB == null){player.sendMessage("[Navigator] No node selected in slot B."); return true;}
					
					player.sendMessage("[Navigator] Node deleted at x:" +editor_sessions.get(player).nodeB.x+" z:"+editor_sessions.get(player).nodeB.z);
					rdmap.removeNode(editor_sessions.get(player).nodeB);
					if (editorSession.draw_map) editorSession.drawMap(editorSession.draw_map_height);
				}
				// Select node B
				if (args[1].equalsIgnoreCase("selnodeb")){
					RoadMap rdmap = maps.get(player.getWorld());
					// Check if map is available.
					if (rdmap == null) {Communicator.tellNoMap(player, player.getWorld().getName()); return true;}
					editor_sessions.get(player).nodeB = rdmap.getNearestNode(player.getLocation().getBlockX(),
																			player.getLocation().getBlockZ());
					player.sendMessage("[Navigator] Node selected at x:" +editor_sessions.get(player).nodeB.x+" z:"+editor_sessions.get(player).nodeB.z);
					if (editor_sessions.get(player).nodeA == editor_sessions.get(player).nodeB)
						player.sendMessage("[Navigator] Warning: Selected the same node twice.");
				}
				// Create a map between the selected roads.
				if (args[1].equalsIgnoreCase("addroad")){
					RoadMap rdmap = maps.get(player.getWorld());
					// Check if map is available.
					if (rdmap == null) {Communicator.tellNoMap(player, player.getWorld().getName()); return true;}
					MapNode nodeA = editor_sessions.get(player).nodeA;
					MapNode nodeB = editor_sessions.get(player).nodeB;
					if (nodeA == null) {player.sendMessage("[Navigator] Node A not selected."); return true;}
					if (nodeB == null) {player.sendMessage("[Navigator] Node B not selected."); return true;}
					
					rdmap.AddRoad(nodeA,nodeB);
					
					player.sendMessage("[Navigator] Road created between (x:"+nodeA.x+",z:"+nodeA.z+") and (x:"+nodeB.x+",z:"+nodeB.z+").");
					
					if (editorSession.draw_map) editorSession.drawMap(editorSession.draw_map_height);
				}
				// Delete the closest road if within 10 blocks from it.
				if (args[1].equalsIgnoreCase("delroad")){
					RoadMap rdmap = maps.get(player.getWorld());
					// Check if map is available.
					if (rdmap == null) {Communicator.tellNoMap(player, player.getWorld().getName()); return true;}
					
					MapRoad road = rdmap.getNearestRoad(player.getLocation().getBlockX(), player.getLocation().getBlockZ());
					if (road.geoLine.ptLineDist(player.getLocation().getBlockX(), player.getLocation().getBlockZ())<10){
						player.sendMessage("[Navigator] Road deleted between (x:"+road.node1.x+",z:"+road.node1.z+") and (x:"+road.node2.x+",z:"+road.node2.z+").");
						rdmap.removeRoad(road);
					}
					
					if (editorSession.draw_map) editorSession.drawMap(editorSession.draw_map_height);
				}
				// Add a destination.
				if (args[1].equalsIgnoreCase("adddest") || args[1].equalsIgnoreCase("addest")){
					RoadMap rdmap = maps.get(player.getWorld());
					// Check if map is available.
					if (rdmap == null) {Communicator.tellNoMap(player, player.getWorld().getName()); return true;}
					
					if (!rdmap.AddDest(player.getLocation().getBlockX(),player.getLocation().getBlockZ(),args[2])){
						Communicator.tellDestionationExists(player,args[2]);
					}
					
					player.sendMessage("[Navigator] Destination created: "+args[2]);
					
					if (editorSession.draw_map) editorSession.drawMap(editorSession.draw_map_height);
				}
				// Delete a destination.
				if (args[1].equalsIgnoreCase("deldest")){
					RoadMap rdmap = maps.get(player.getWorld());
					// Check if map is available.
					if (rdmap == null) {Communicator.tellNoMap(player, player.getWorld().getName()); return true;}
					
					MapDest dest = rdmap.searchDestination(args[2]);
					
					if (dest == null) {player.sendMessage("[Navigator] Destination \""+args[2]+"\" not found."); return true;}
					
					rdmap.removeDestination(dest);
					
					player.sendMessage("[Navigator] Destination deleted: "+args[2]);
					
					if (editorSession.draw_map) editorSession.drawMap(editorSession.draw_map_height);
				}
				if (args[1].equalsIgnoreCase("drawmap")){
					RoadMap rdmap = maps.get(player.getWorld());
					// Check if map is available.
					if (rdmap == null) {Communicator.tellNoMap(player, player.getWorld().getName()); return true;}
					
					if (args.length < 3) {player.sendMessage("[Navigator] /nav edit drawmap <*height*/disable/false>"); return true;}
					
					if (args[2].equalsIgnoreCase("false")||args[2].contains("disable")){
						player.sendMessage("[Navigator] Draw mode disabled.");
						editor_sessions.get(player).eraseDrawnMap();
						return true;
					}
					
					player.sendMessage("[Navigator] Drawing map at y:"+args[2]+".");
					editor_sessions.get(player).drawMap(Integer.parseInt(args[2]));
				}
			} else {
				Communicator.tellMessage(sender, "InGameOnlyCommand");
			}
		}
		
		return false;
	}
	
	private void reloadNavigator() {
		for (int i=0; i<routes.size(); i++){
			routes.get(i).owner.sendMessage("[Navigator] Navigator has been reloaded. Navigation cancelled.");
			routes.get(i).owner.sendMessage("[Navigator] You are free to reuse /nav go <destination>");
		}
		
		routes.clear(); maps.clear();
		
		// Create our plugin's folder, plugins/Navigator if it doesn't exist.
		File NavigatorFile = new File("plugins/Navigator");
		if (!NavigatorFile.exists()) NavigatorFile.mkdir();
		
		List<World> worldList = Bukkit.getWorlds();
		
		for (int i=0;i<worldList.size();i++){;
			RoadMap map = new RoadMap();
			World w = worldList.get(i);
			try {
				File worldFile = new File("plugins/Navigator/"+w.getName()+".map");
				if (worldFile.exists()){
					map.loadFromFile(worldFile);
					maps.put(worldList.get(i), map);
					log.info("[Navigator] Loaded map of world "+w.getName());
				} else {
					log.info("[Navigator] Could not find map of world "+w.getName());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void update(){
		for (int i=0;i<routes.size();i++){
			if (!routes.get(i).finished) routes.get(i).update();
			else routes.remove(i);			
		}
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		if (routes.contains(event.getPlayer()))
			routes.remove(event.getPlayer());
	}
	
}
