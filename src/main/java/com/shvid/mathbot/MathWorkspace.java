package com.shvid.mathbot;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.telegram.telegrambots.logging.BotLogger;

import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

/**
 * MathWorkspace
 * 
 * @author Alex Shvid
 *
 */

public class MathWorkspace {

	private static final String LOGTAG = "WORKSPACE";

	private final ExecuteWatchdog watchdog = new ExecuteWatchdog(
	    ExecuteWatchdog.INFINITE_TIMEOUT);

	private final DefaultExecuteResultHandler resultHandler;
	private final DefaultExecutor executor;
	private final ExtendedPumpStreamHandler streamHandler;
	
	public MathWorkspace(String command, OutputStream outputStream) {

		resultHandler = new DefaultExecuteResultHandler();
		executor = new DefaultExecutor();

		streamHandler = new ExtendedPumpStreamHandler(outputStream, outputStream);
		
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
	

	public void waitFor() {
		try {
	    resultHandler.waitFor();
    } catch (InterruptedException e) {
			BotLogger.error(LOGTAG, e);
    }
	}
	
	public void send(String query) {

		try {
	    streamHandler.writeLine(query);
    } catch (IOException e) {
    	BotLogger.error(LOGTAG, e);
    }

	}

	public boolean isAlive() {
		return !resultHandler.hasResult();
	}
	
	public void close() {
		try {
	    streamHandler.stop();
			executor.getWatchdog().destroyProcess();
    } catch (IOException e) {
    	BotLogger.error(LOGTAG, e);
    }
	}

	public enum RemovalListenerImpl implements
	    RemovalListener<Long, MathWorkspace> {

		INSTANCE;

		@Override
		public void onRemoval(RemovalNotification<Long, MathWorkspace> notification) {
			notification.getValue().close();
		}

	}



	public static class ExtendedPumpStreamHandler extends PumpStreamHandler {

		private PrintStream printStream;

    public ExtendedPumpStreamHandler(final OutputStream out, final OutputStream err) {
    	super(out, err);
    }
    
    public ExtendedPumpStreamHandler(final OutputStream out, final OutputStream err, final InputStream in) {
    	super(out, err, in);
    }

		
		@Override
    public void setProcessInputStream(OutputStream os) {
			this.printStream = new PrintStream(new BufferedOutputStream(os));
    }
		
		public void writeLine(String line) throws IOException {
			if (printStream == null) {
				throw new IllegalStateException("empty printStream");
			}
			printStream.println(line);
			printStream.flush();
		}
		
	}
	
}
