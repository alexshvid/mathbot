package com.shvid.mathbot;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * AppSettings
 * 
 * @author Alex Shvid
 *
 */

public final class AppSettings {

	public static final String DFS_CONF = "../conf/mathbot.conf";
	
	protected final Properties props;
	
	AppSettings() {
		this.props = loadProperties();
	}
	
	static Properties loadProperties() {
		
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(DFS_CONF));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return props;
		
	}
	
	String getOctaveExec() {
		return props.getProperty("octave.exec");
	}

	String getTokenVar() {
		return props.getProperty("token.var");
	}
	
}
