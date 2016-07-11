package com.shvid.mathbot;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.telegram.telegrambots.bots.AbsSender;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * MathService
 * 
 * @author Alex Shvid
 *
 */

public class MathService {

	private static final String LOGTAG = "MATHSERVICE";

	private final AppSettings appSettings;
	
	/**
	 * key is chat id
	 */

	private final Cache<Long, MathWorkspace> workspaceMap = CacheBuilder.newBuilder()
	    .maximumSize(1000).expireAfterAccess(20, TimeUnit.MINUTES)
	    .removalListener(MathWorkspace.RemovalListenerImpl.INSTANCE).build();

	public MathService(AppSettings appSettings) {
		this.appSettings = appSettings;
	}
	
	public MathWorkspace findWorkspace(Long chatId) {
		MathWorkspace workspace = workspaceMap.getIfPresent(chatId);
		if (workspace != null) {
			if (workspace.isAlive()) {
				return workspace;
			}
			workspaceMap.invalidate(chatId);
			workspace.close();
		}
		return null;
	}
	
	public MathWorkspace newWorkspace(AbsSender sender, Long chatId) {
		
		ChatOutputStream outputStream = new ChatOutputStream(sender, chatId);
		
		MathWorkspace workspace = new MathWorkspace(appSettings, outputStream);
		workspaceMap.put(chatId, workspace);
		return workspace;
	}
	
	public List<String> getResult(String query) {
		return Arrays.asList("hello");

	}

}
