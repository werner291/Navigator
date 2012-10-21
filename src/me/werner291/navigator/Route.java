package me.werner291.navigator;

import java.awt.geom.Line2D;
import java.util.ArrayList;

import org.bukkit.entity.Player;

public class Route{
	
	Player owner; 
	ArrayList<Instruction> instructions = new ArrayList<Instruction>();
	int cInstID = 0;
	boolean finished = false;
	int warnTimer = 0;
	
	Route(Player owner, ArrayList<Instruction> instructions){
		this.owner = owner; this.instructions = instructions;
	}
	
	public void update() {
		if (cInstID<instructions.size()){
			Instruction cInst = instructions.get(cInstID);
			
			int dist = Math.abs(owner.getLocation().getBlockX()-cInst.x)
					+Math.abs(owner.getLocation().getBlockZ()-cInst.z);
			
			if (warnTimer > 0) warnTimer -= 5; else warnTimer = 0;
			
			if (cInstID>0 && warnTimer == 0){
				Line2D routeLine = new Line2D.Double(instructions.get(cInstID-1).x, instructions.get(cInstID-1).z, cInst.x, cInst.z);
				
				int drift = (int) routeLine.ptSegDist(owner.getLocation().getBlockX(), owner.getLocation().getBlockZ());
				
				
				if (drift > 25){
					Communicator.sendOffRouteWarningB(owner);
					finished = true;
				} else if (drift > 15){
					Communicator.sendOffRouteWarningA(owner);
					warnTimer = 200;
				}
			}
			
			if (dist < cInst.range || cInstID == 0){
				Communicator.sendNavigationInstruction(owner,cInst);
				cInstID++;
			}
		} else {
			finished = true;
		}
	}

	
	
}
