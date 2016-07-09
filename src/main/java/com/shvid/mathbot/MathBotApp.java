package com.shvid.mathbot;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MathBotApp {

	private final Process proc;
	private final BufferedReader bufferedReader;

	public MathBotApp() throws Exception {

		this.proc = Runtime.getRuntime().exec("/usr/local/octave/3.8.0/bin/octave");

		InputStream inputStream = proc.getInputStream();
		this.bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

	}

	public String getOutput() throws Exception {

		StringBuilder output = new StringBuilder();
		
		while (bufferedReader.ready()) {
			String line = bufferedReader.readLine();
			if (line != null) {
				output.append(line).append("\n");
			}
			else {
				break;
			}
		}

		return output.toString();
	}

	public void waitFor() throws Exception {

		int exitValue = proc.waitFor();
		if (exitValue != 0) {
			System.err.println(proc.exitValue());
		}

	}
	
	public void close() {
		proc.destroy();
	}

	public static void main(String[] args) throws Exception {

		System.out.println("MathBot");

		MathBotApp bot = new MathBotApp();
		
		Thread.currentThread().sleep(1000);
		
		System.out.println(bot.getOutput());
		
		bot.close();

	}

}
