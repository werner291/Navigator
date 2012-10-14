package me.werner291.navigator;

public class Instruction {
	
	enum InstructionType {
		GO_STRAIGHT,TURN_LEFT,TURN_RIGHT,TURN_AROUND, START, END	
	}
	
	int x; int z; int range;
	Cardinal cardinalDir;
	InstructionType instructionType;
	
	Instruction(int x, int z, int range, InstructionType instructionType, Cardinal cardinalDir){
		this.x = x; this.z = z; this.range = range;
		this.instructionType = instructionType;
		this.cardinalDir=cardinalDir;
	}
	
}
