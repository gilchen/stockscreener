package com.stocks.command;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

public class BseReportCommand extends AbstractCommand {

	public boolean execute(Context arg0) throws Exception {
		System.out.println( "execute() Called." );
		return Command.CONTINUE_PROCESSING;
	}

}
