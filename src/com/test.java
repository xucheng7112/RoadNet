package com;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class test {

	public static void main(String[] args) {
		String path = "File/carInfo";
		File file = new File(path);
		File[] a = file.listFiles();
		List<String> carId = new ArrayList<String>();
		Executor executor = Executors.newFixedThreadPool(6);
		for (int i = 0; i < a.length; i++) {
			String[] s = a[i].getName().split(".txt");
			carId.add(s[0]);
			ThreadGraph t = new ThreadGraph(s[0]);
			executor.execute(t);
		}

		// g.getFlowCluster(0);
	}

}

class ThreadGraph extends Thread {
	String carID;

	public ThreadGraph(String s) {
		this.carID = s;
	}

	public void run() {
		String edgeFile = "File/Graph/edges.txt";
		String nodeFile = "File/Graph/vertices.txt";
		Graph g = new Graph(edgeFile, nodeFile);
		g.getBaseCluster(carID);
	}

}
