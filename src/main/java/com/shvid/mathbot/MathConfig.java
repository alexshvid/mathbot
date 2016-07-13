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

	public static final String INLINE_TITLE = "GetMathBot ";

  public static final int MATH_CACHE_TIME = 1;

	public static final String SYSTEM_CHARS = " ~!@#$%^&*()_+-=[]{}:;'\",.<>/?|\\";

	public static final int SEND_SPEED_MAX_LINES = 15;
	
	public static final long SEND_SPEED_MLS = 1000L;
	
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
