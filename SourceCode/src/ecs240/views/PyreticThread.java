package ecs240.views;

import java.io.InputStreamReader;

public class PyreticThread implements Runnable {

	private String pyreticModule;
	private ProcessBuilder pb;
	private Process pyretic;

	public PyreticThread(String pyreticpolicy) {
		pyreticModule = pyreticpolicy;
	}

	public void run() {
		try {
			// Runtime.getRuntime().exec("pkill -SIGINT pyretic");
			pb = new ProcessBuilder().inheritIO();
			pb.command("pyretic.py", pyreticModule);
			pyretic = pb.start();
			InputStreamReader streamReader = new InputStreamReader(
					pyretic.getInputStream());
			InputStreamReader streamReader2 = new InputStreamReader(
					pyretic.getErrorStream());
			LogStreamReader reader = new LogStreamReader(streamReader,
					streamReader2);
			Thread thread = new Thread(reader, "LogStreamReader");
			thread.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void stop() {
		if (pyretic != null) {
			try {
				Runtime.getRuntime().exec("pkill -SIGINT pyretic");

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
