package com.shvid.mathbot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.logging.BotLogger;
import org.telegram.telegrambots.logging.BotsFileHandler;

/**
 * MathBotApp
 * 
 * @author Alex Shvid
 *
 */

public class MathBotApp {

	private static final String LOGTAG = "MAIN";

	private final AppSettings appSettings = new AppSettings();

	private final DefaultExecuteResultHandler resultHandler;

	public MathBotApp() throws Exception {

		resultHandler = new DefaultExecuteResultHandler();
		ExecuteWatchdog watchdog = new ExecuteWatchdog(
		    ExecuteWatchdog.INFINITE_TIMEOUT);
		Executor exec = new DefaultExecutor();

		PumpStreamHandler psh = new PumpStreamHandler(System.out, System.err,
		    System.in);

		exec.setStreamHandler(psh);
		exec.setWatchdog(watchdog);

		CommandLine cl = CommandLine.parse(appSettings.getOctaveExec());

		exec.execute(cl, resultHandler);

	}

	public void waitFor() throws Exception {

		resultHandler.waitFor();

	}

	public void close() {

	}

	public static void main(String[] args) throws Exception {

		registerLogs();

		AppSettings appSettings = new AppSettings();

		// commandLine(appSettings);

		TelegramBotsApi telegramBotsApi = new TelegramBotsApi();

		try {
			telegramBotsApi.registerBot(new MathHandlers(appSettings));
		} catch (TelegramApiException e) {
			BotLogger.error(LOGTAG, e);
		}

		System.out.println("MathBot started");

		// MathBotApp bot = new MathBotApp();

		// bot.waitFor();

		// bot.close();

	}

	private static void commandLine(AppSettings appSettings) throws IOException {
		MathWorkspace workspace = new MathWorkspace(appSettings.getOctaveExec(), System.out);

		BufferedReader b = new BufferedReader(new InputStreamReader(System.in));

		while (true) {

			String line = b.readLine();
			if (line == null) {
				break;
			}

			String command = line.trim();
			int len = command.length();
			if (len > 0) {
				System.out.println("cmd=" + command);
				workspace.send(command);
			}

		}

		workspace.close();
	}

	private static void registerLogs() {
		BotLogger.setLevel(Level.ALL);
		BotLogger.registerLogger(new ConsoleHandler());
		try {
			BotLogger.registerLogger(new BotsFileHandler());
		} catch (IOException e) {
			BotLogger.severe(LOGTAG, e);
		}
	}

}
