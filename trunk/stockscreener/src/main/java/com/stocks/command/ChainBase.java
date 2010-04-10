package com.stocks.command;

import java.util.Collection;

import org.apache.commons.chain.Command;

public class ChainBase extends org.apache.commons.chain.impl.ChainBase {
	public ChainBase(Collection<Command> commands){
		super(commands);
	}
	
	public Command[] getCommands(){
		return commands;
	}
}
