package ecs240.views;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class LogStreamReader implements Runnable {
	private BufferedReader reader;

	public LogStreamReader(InputStreamReader is) {
		this.reader = new BufferedReader(is);
	}

	public void run() {
		try {
			String line = reader.readLine();
			while (line != null) {
				System.out.println(line);
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}