package com.shvid.mathbot;

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
	private volatile boolean welcomeDone = false;
	
	public ChatOutputStream(AbsSender sender, Long chatId) {
		this.sender = sender;
		this.chatId = chatId;
	}
	
	@Override
  protected void processLine(String line, int logLevel) {

		System.out.println("PROCESS LINE '" + line + "'");

		if (line == null || line.length() == 0) {
			return;
		}
		
		if (MathConfig.isLastWelcomeLine(line)) {
			welcomeDone = true;
			return;
		}
		
		if (welcomeDone) {
		
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
