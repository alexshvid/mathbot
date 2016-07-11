package com.shvid.mathbot;

import java.util.ArrayList;
import java.util.List;

import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent;
import org.telegram.telegrambots.api.objects.inlinequery.result.InlineQueryResult;
import org.telegram.telegrambots.api.objects.inlinequery.result.InlineQueryResultArticle;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.logging.BotLogger;

/**
 * MathHandlers
 * 
 * @author Alex Shvid
 *
 */

public class MathHandlers extends TelegramLongPollingBot {

	private static final String LOGTAG = "MATHHANDLERS";

	private final String botToken;
	private final MathService mathService;

	public MathHandlers(AppSettings appSettings) {
		this.botToken = System.getenv(appSettings.getTokenVar());

		if (this.botToken == null) {
			throw new IllegalStateException("system variable "
			    + appSettings.getTokenVar() + " is not set, expected bot token");
		}
		
		this.mathService = new MathService(appSettings);
	}

	@Override
	public String getBotToken() {
		return botToken;
	}

	@Override
	public void onUpdateReceived(Update update) {
		try {
			if (update.hasInlineQuery()) {
				handleIncomingInlineQuery(update.getInlineQuery());
			} else if (update.hasMessage() && update.getMessage().isUserMessage()) {
				try {
					processMessage(update.getMessage());
				} catch (TelegramApiException e) {
					BotLogger.error(LOGTAG, e);
				}
			}
		} catch (Exception e) {
			BotLogger.error(LOGTAG, e);
		}
	}

	@Override
	public String getBotUsername() {
		return MathConfig.MATH_BOT_USER;
	}

	private void handleIncomingInlineQuery(InlineQuery inlineQuery) {
		String query = inlineQuery.getQuery();
		BotLogger.debug(LOGTAG, "InlineQuery: " + query);
		try {
			if (!query.isEmpty()) {
				List<String> outputResults = mathService.getResult(query);
				answerInlineQuery(formatAnswer(inlineQuery, outputResults));
			} else {
				answerInlineQuery(formatAnswer(inlineQuery, new ArrayList<String>()));
			}
		} catch (TelegramApiException e) {
			BotLogger.error(LOGTAG, e);
		}
	}

	private static AnswerInlineQuery formatAnswer(InlineQuery inlineQuery,
	    List<String> outputResults) {
		AnswerInlineQuery answerInlineQuery = new AnswerInlineQuery();
		answerInlineQuery.setInlineQueryId(inlineQuery.getId());
		answerInlineQuery.setCacheTime(MathConfig.MATH_CACHE_TIME);
		answerInlineQuery.setResults(convertResults(outputResults));
		return answerInlineQuery;
	}

	private static List<InlineQueryResult> convertResults(
	    List<String> outputResults) {
		List<InlineQueryResult> results = new ArrayList<>();

		int i = 1;
		for (String outputResult : outputResults) {
			InputTextMessageContent messageContent = new InputTextMessageContent();
			messageContent.disableWebPagePreview();
			messageContent.enableMarkdown(true);
			messageContent.setMessageText(outputResult);
			InlineQueryResultArticle article = new InlineQueryResultArticle();
			article.setInputMessageContent(messageContent);
			article.setId(Integer.toString(i++));
			article.setTitle("title");
			article.setDescription("description");
			results.add(article);
		}

		return results;
	}

	private void processMessage(Message message) throws TelegramApiException {

		Long chatId = message.getChatId();
		
		MathWorkspace workspace = mathService.findWorkspace(chatId);
		if (workspace == null) {
			workspace = createNewWorkspace(message);
		}
		
		runInWorkspace(workspace, message);

	}
	
	private MathWorkspace createNewWorkspace(Message message) throws TelegramApiException {
		
		MathWorkspace workspace = mathService.newWorkspace(this, message.getChatId());
		
		SendMessage replyMessage = new SendMessage();
		replyMessage.setChatId(message.getChatId().toString());
		replyMessage.enableMarkdown(true);
		replyMessage.setText("new workspace created");
		sendMessage(replyMessage);
		
		return workspace;
	}
	
	private void runInWorkspace(MathWorkspace workspace, Message message) {
		
		String query = message.getText();
		BotLogger.debug(LOGTAG, "Query: " + query);

		workspace.send(query);
		
	}
	
}
