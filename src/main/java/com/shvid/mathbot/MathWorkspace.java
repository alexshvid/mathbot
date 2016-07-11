package com.shvid.mathbot;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

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
	
	public MathWorkspace(AppSettings appSettings, ChatOutputStream outputStream) {

		resultHandler = new DefaultExecuteResultHandler();
		executor = new DefaultExecutor();

		streamHandler = new ExtendedPumpStreamHandler(outputStream, System.err);
		
		executor.setStreamHandler(streamHandler);
		executor.setWatchdog(watchdog);

		CommandLine cl = CommandLine.parse(appSettings.getOctaveExec());

		try {
			executor.execute(cl, resultHandler);
		} catch (ExecuteException e) {
			BotLogger.error(LOGTAG, e);
		} catch (IOException e) {
			BotLogger.error(LOGTAG, e);
		}

		System.out.println("resultHandler.hasResult " + resultHandler.hasResult());

	}

	public void send(String query) {

		System.out.println("send query = '" + query + "'");
		
		System.out.println("watchdog.isWatching " + watchdog.isWatching());
		System.out.println("watchdog.killedProcess " + watchdog.killedProcess());
		
		System.out.println("resultHandler.hasResult " + resultHandler.hasResult());

		
		try {
	    streamHandler.writeLine(query);
    } catch (IOException e) {
    	BotLogger.error(LOGTAG, e);
    }
		
		/**
		try {
	    out.write(queryWithLn.getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
    	BotLogger.error(LOGTAG, e);
    }
    **/
		
		/*
		try {
			executor.writeLine(query);
		} catch (IOException e) {
			BotLogger.error(LOGTAG, e);
		} catch (IllegalStateException e) {
			BotLogger.error(LOGTAG, e);
		}
		*/
	}

	public void close() {
		System.out.println("Close workspace " + this);
		try {
	    streamHandler.stop();
    } catch (IOException e) {
    	BotLogger.error(LOGTAG, e);
    }
		executor.getWatchdog().destroyProcess();
	}

	public enum RemovalListenerImpl implements
	    RemovalListener<Long, MathWorkspace> {

		INSTANCE;

		@Override
		public void onRemoval(RemovalNotification<Long, MathWorkspace> notification) {
			notification.getValue().close();
		}

	}

	public static final class DirectExecutor extends DefaultExecutor {

		private Process process;
		private OutputStream processOutput;

		@Override
		protected Process launch(CommandLine command, Map<String, String> env, File dir)
		    throws IOException {
			process = super.launch(command, env, dir);
			processOutput = process.getOutputStream();
			return process;
		}

		public void writeLine(String line) throws IOException {

			if (processOutput == null) {
				throw new IllegalStateException("empty processOutput");
			}
			
			String lineWithLn = line + "\n";
			processOutput.write(lineWithLn.getBytes(StandardCharsets.UTF_8));
		}

	}

	public static class ExtendedPumpStreamHandler extends PumpStreamHandler {

		private OutputStream processOutput;
		
    public ExtendedPumpStreamHandler(final OutputStream out, final OutputStream err) {
    	super(out, err);
    }

		
		@Override
    public void setProcessInputStream(OutputStream os) {
			this.processOutput = os;
    }
		
		public void writeLine(String line) throws IOException {
			
			if (processOutput == null) {
				throw new IllegalStateException("empty processOutput");
			}
			
			String lineWithLn = line + "\n";
			processOutput.write(lineWithLn.getBytes(StandardCharsets.UTF_8));
			
		}
		
	}
	
}
