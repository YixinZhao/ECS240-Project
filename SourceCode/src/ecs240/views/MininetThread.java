package ecs240.views;

import java.io.File;
import java.io.InputStreamReader;

import ecs240.Activator;

public class MininetThread implements Runnable {

	private String app;
	private ProcessBuilder pb;
	private Process mininet;

	public MininetThread(String ap) {
		this.app = ap;
	}

	public MininetThread() {
		app = "pingall";
	}

	public void run() {
		try {
			Runtime.getRuntime().exec("gksudo mn -c");

			String loc = Activator.getDefault().getBundle().getLocation();
			String dir = loc.substring(loc.lastIndexOf(':') + 1)
					+ "src/ecs240/views";
			System.out.println(dir);
			pb = new ProcessBuilder().inheritIO();
			// TODO relative path
			pb.directory(new File(dir));
			String command = "python MininetRunner.py --test " + app
					+ " --topo my --controller remote --mac";

			pb.command("gksudo", command);
			mininet = pb.start();
			InputStreamReader streamReader = new InputStreamReader(
					mininet.getInputStream());
			InputStreamReader streamReader2 = new InputStreamReader(
					mininet.getErrorStream());
			LogStreamReader reader = new LogStreamReader(streamReader,
					streamReader2);
			Thread thread = new Thread(reader, "LogStreamReader");
			thread.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void stop() {
		try {
			Runtime.getRuntime().exec("gksudo mn -c");
			mininet.destroy();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
