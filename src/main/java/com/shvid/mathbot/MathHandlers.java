package com.shvid.mathbot;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
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

	private final AppSettings appSettings;
	private final String botToken;
	private final MathService mathService;
	private final QueryService queryService = new QueryService();

	public MathHandlers(AppSettings appSettings) {
		this.appSettings = appSettings;
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
				processInlineQuery(update.getInlineQuery());
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

	private void processInlineQuery(InlineQuery inlineQuery) {
		String query = inlineQuery.getQuery();
		System.out.println("INLINE_QUERY FROM @" + inlineQuery.getFrom().getUserName() + " TEXT '" + query + "'");
		BotLogger.debug(LOGTAG, "InlineQuery: " + query);
		try {
			if (query != null && !query.isEmpty()) {
				runInlineQuery(inlineQuery);
			}
		} catch (Exception e) {
			BotLogger.error(LOGTAG, e);
		}
	}

	private void runInlineQuery(InlineQuery inlineQuery) throws Exception {

		String query = inlineQuery.getQuery();
		if (!queryService.isValidQuery(query)) {
			return;
		}
		
		InlineWorkspace workspace = new InlineWorkspace(appSettings.getOctaveExec(), query + "\nexit\n");

		byte[] out = workspace.waitFor();
		
		List<String> resultList = IOUtils.readLines(new ByteArrayInputStream(out), StandardCharsets.UTF_8);
		
		String result = convertResults(resultList);
		
		if (result.indexOf("parse error") != -1 ||
				result.indexOf("syntax error") != -1) {
			return;
		}
		
    AnswerInlineQuery answerInlineQuery = new AnswerInlineQuery();
    answerInlineQuery.setInlineQueryId(inlineQuery.getId());
    answerInlineQuery.setCacheTime(MathConfig.MATH_CACHE_TIME);
    
    InputTextMessageContent messageContent = new InputTextMessageContent();
    messageContent.disableWebPagePreview();
    messageContent.enableMarkdown(true);
    messageContent.setMessageText(result);
    
    InlineQueryResultArticle article = new InlineQueryResultArticle();
    article.setId(inlineQuery.getId());
    article.setTitle(MathConfig.INLINE_TITLE);
    article.setInputMessageContent(messageContent);
    
    List<InlineQueryResult> articles = new ArrayList<>(1);
    articles.add(article);
    
    answerInlineQuery.setResults(articles);
    answerInlineQuery(answerInlineQuery);
    
	}


	private String convertResults(List<String> resultList) {
		StringBuilder str = new StringBuilder(); 
		
		boolean welcomeDone = false;
		for (String line : resultList) {
			
			if (line == null || line.length() == 0) {
				continue;
			}
			
			if (MathConfig.isLastWelcomeLine(line)) {
				welcomeDone = true;
				continue;
			}
			
			if (welcomeDone) {
				str.append(line).append("\n");
			}
		}
		
		return str.toString();
	}
	
	private void processMessage(Message message) throws TelegramApiException {

		String query = message.getText();
		if (query == null || query.isEmpty()) {
			return;
		}
		
		System.out.println("MESSAGE FROM @" + message.getFrom().getUserName() + " TEXT '" + query + "'");
		
		if (!queryService.isValidQuery(query)) {
			return;
		}
		
		Long chatId = message.getChatId();
		
		if (query.startsWith("/start")) {
			mathService.dropWorkspace(chatId);
			createNewWorkspace(message);
			return;
		}
		
		if (query.startsWith("/help")) {
			SendMessage replyMessage = new SendMessage();
			replyMessage.setChatId(message.getChatId().toString());
			replyMessage.enableMarkdown(true);
			replyMessage.setText("Welcome to the GetMathBot.\nScalar, vector and matrix operations are supported.\nSintax and functions are similar to MatLab/Octave.\nEnjoy.");
			sendMessage(replyMessage);
			return;
		}
		
		MathWorkspace workspace = mathService.findWorkspace(chatId);
		if (workspace == null) {
			workspace = createNewWorkspace(message);
		}
		
		runInWorkspace(workspace, message);

	}
	
	private MathWorkspace createNewWorkspace(Message message) throws TelegramApiException {
		
		MathWorkspace workspace = mathService.newWorkspace(this, message.getChatId(), message.getFrom().getUserName());
		
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
