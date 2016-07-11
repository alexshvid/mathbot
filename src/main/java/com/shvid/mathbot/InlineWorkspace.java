package com.shvid.mathbot;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.telegram.telegrambots.logging.BotLogger;

/**
 * InlineWorkspace
 * 
 * @author Alex Shvid
 *
 */

public class InlineWorkspace {

	private static final String LOGTAG = "IWORKSPACE";

	private final ExecuteWatchdog watchdog = new ExecuteWatchdog(
	   1000L);

	private final DefaultExecuteResultHandler resultHandler;
	private final DefaultExecutor executor;
	private final PumpStreamHandler streamHandler;
	private final ByteArrayOutputStream out = new ByteArrayOutputStream();
	
	public InlineWorkspace(String command, String query) {

		ByteArrayInputStream in = new ByteArrayInputStream(query.getBytes(StandardCharsets.UTF_8));
		
		resultHandler = new DefaultExecuteResultHandler();
		executor = new DefaultExecutor();

		streamHandler = new PumpStreamHandler(out, out, in);
		
		executor.setStreamHandler(streamHandler);
		executor.setWatchdog(watchdog);

		CommandLine cl = CommandLine.parse(command);

		try {
			executor.execute(cl, resultHandler);
		} catch (ExecuteException e) {
			BotLogger.error(LOGTAG, e);
		} catch (IOException e) {
			BotLogger.error(LOGTAG, e);
		}

	}

	public byte[] waitFor() {
		try {
	    resultHandler.waitFor();
    } catch (InterruptedException e) {
			BotLogger.error(LOGTAG, e);
    }
    return out.toByteArray();
	}

	
}
