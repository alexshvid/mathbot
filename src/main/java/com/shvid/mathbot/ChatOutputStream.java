package com.shvid.mathbot;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.exec.LogOutputStream;
import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.logging.BotLogger;

/**
 * ChatOutputStream
 * 
 * @author Alex Shvid
 *
 */

public final class ChatOutputStream extends LogOutputStream {

	private static final String LOGTAG = "CHATOUT";
	
	private final AbsSender sender;
	private final Long chatId;
	private final String receiver;
	private final AtomicBoolean welcomeDone = new AtomicBoolean(false);
	private final AtomicInteger intervalLines = new AtomicInteger(0);
	private final AtomicLong sendingIntervalMls = new AtomicLong(0L);
	
	public ChatOutputStream(AbsSender sender, Long chatId, String receiver) {
		this.sender = sender;
		this.chatId = chatId;
		this.receiver = receiver;
	}
	
	@Override
  protected void processLine(String line, int logLevel) {

		if (line == null || line.length() == 0) {
			return;
		}
		
		if (MathConfig.isLastWelcomeLine(line)) {
			welcomeDone.set(true);
			return;
		}

		if (welcomeDone.get()) {
			
			long current = System.currentTimeMillis();
			long interval = current - (current % MathConfig.SEND_SPEED_MLS);

			if (interval > sendingIntervalMls.get()) {
				sendingIntervalMls.set(interval);
				intervalLines.set(0);
			}
			else {
				int lines = intervalLines.get();
				if (lines == MathConfig.SEND_SPEED_MAX_LINES) {
					line = "And more lines...";						
				}
				else if (lines > MathConfig.SEND_SPEED_MAX_LINES) {
					return;
				}
			}

			intervalLines.incrementAndGet();
			
			System.out.println("SEND TO " + receiver +" TEXT '" + line + "'");

			SendMessage sendMessage = new SendMessage();
			sendMessage.setChatId(chatId.toString());
			sendMessage.enableMarkdown(true);
			sendMessage.setText(line);
			try {
		    sender.sendMessage(sendMessage);
	    } catch (TelegramApiException e) {
				BotLogger.error(LOGTAG, e);
	    }
			
		}
  }

	
}
