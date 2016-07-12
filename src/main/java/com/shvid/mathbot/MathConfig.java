package com.shvid.mathbot;

/**
 * 
 * MathConfig
 * 
 * @author Alex Shvid
 *
 */

public final class MathConfig {

	private MathConfig() {
	}
	
	public static final String MATH_BOT_USER = "GetMathBot";

	public static final String INLINE_TITLE = "GetMathBot Answer";

  public static final int MATH_CACHE_TIME = 86400;

	public static final String SYSTEM_CHARS = " ~!@#$%^&*()_+-=[]{}:;'\",.<>/?|\\";

  public static final String[] DENY_COMMANDS = {
  	"pwd",
  	"cd",
  	"cat",
  	"ls",
  	"load",
  	"save",
  	"exit",
  	"system"
  };
  
	public static final String[] LAST_WELCOME_LINE = { 
		
		"For information about changes from previous versions, type 'news'.",
		"octave: disabling GUI features"
		
	};

	public static boolean isLastWelcomeLine(String line) {
		for (String s : LAST_WELCOME_LINE) {
			if (s.equals(line)) {
				return true;
			}
		}
		return false;
	}
	
}
