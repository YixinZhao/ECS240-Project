package ecs240.views;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class LogStreamReader implements Runnable {
	private BufferedReader reader;
	private BufferedReader reader2;

	public LogStreamReader(InputStreamReader is, InputStreamReader is2) {
		this.reader = new BufferedReader(is);
		this.reader2 = new BufferedReader(is2);
	}

	public void run() {
		try {
			String line1 = reader.readLine();
			String line2 = reader2.readLine();
			while (line1 != null || line2 != null) {
				if (line1 != null) {
					System.out.println(line1);
					line1 = reader.readLine();
				}
				if (line2 != null) {
					System.out.println(line2);
					line2 = reader.readLine();
				}
			}
			reader.close();
			reader2.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}