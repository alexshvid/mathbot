package com.shvid.mathbot;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

public class QueryService {

	public final Set<String> denyCommands;
	
	public QueryService() {
		this.denyCommands = new HashSet<String>();
		for (String cmd : MathConfig.DENY_COMMANDS) {
			this.denyCommands.add(cmd);
		}
	}
	
	public boolean isValidQuery(String query) {

		String lowcase = query.toLowerCase();
		
		StringTokenizer tokenizer = new StringTokenizer(lowcase, MathConfig.SYSTEM_CHARS);
		
		while (tokenizer.hasMoreTokens()) {
			
			String token = tokenizer.nextToken();
			System.out.println("token = " + token);
			
			if (denyCommands.contains(token)) {
				return false;
			}
			
			
		}
		
		return true;
	}
	
}
