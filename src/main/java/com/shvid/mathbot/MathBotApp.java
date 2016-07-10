package com.shvid.mathbot;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;

/**
 * MathBotApp
 * 
 * @author Alex Shvid
 *
 */

public class MathBotApp {
	
	private final AppSettings appSettings = new AppSettings();

	private final DefaultExecuteResultHandler resultHandler;
	
	public MathBotApp() throws Exception {

		resultHandler = new DefaultExecuteResultHandler();
		ExecuteWatchdog watchdog  = new ExecuteWatchdog( ExecuteWatchdog.INFINITE_TIMEOUT );
		Executor exec = new DefaultExecutor();

		PumpStreamHandler psh = new PumpStreamHandler(System.out, System.err, System.in);

		exec.setStreamHandler( psh );
		exec.setWatchdog( watchdog );

		CommandLine cl = CommandLine.parse(appSettings.getOctaveExec());
		
		exec.execute(cl, resultHandler );

	}

	public void waitFor() throws Exception {

		resultHandler.waitFor();

	}
	
	public void close() {

	}

	public static void main(String[] args) throws Exception {

		System.out.println("MathBot");

		MathBotApp bot = new MathBotApp();

		bot.waitFor();
		
		bot.close();

	}

}
