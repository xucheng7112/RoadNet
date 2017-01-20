package com;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Graph {
	private Map<Integer, List<Integer>> vertexMap;// 邻接表
	private Map<Integer, Node> nodeMap; // 存储所有点
	private Map<Integer, Edge> edgeMap; // 存储所有边
	private Map<Integer, Integer> edgeDensity; // 边的密度
	private Map<Integer, List<Integer>> edgeCollection; // 边上的所有轨迹集合

	// private HashMap<Integer, List> edgeList; // Integer为边的ID，List为经过此边的所有轨迹
	// 的ID集合

	// 初始化数据
	public Graph(String edgeFile, String nodeFile) {
		// 初始化邻接表
		vertexMap = new HashMap<Integer, List<Integer>>();
		nodeMap = new HashMap<Integer, Node>();
		edgeMap = new HashMap<Integer, Edge>();
		edgeDensity = new HashMap<Integer, Integer>();
		edgeCollection = new HashMap<Integer, List<Integer>>();
		init(edgeFile, nodeFile);
	}

	// 根据点文件名，边文件名，初始化邻接表vertexList和点集nodeList的信息
	private void init(String edgeFile, String nodeFile) {
		// 初始化点信息
		String thisLine = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader(nodeFile));
			while ((thisLine = br.readLine()) != null) {
				String[] a = thisLine.split("\\s+");
				int nId = Integer.parseInt(a[0]);
				Node n = new Node(Integer.parseInt(a[0]),
						Double.parseDouble(a[1]), Double.parseDouble(a[2]));
				nodeMap.put(nId, n);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println("初始化点集合完毕");
		}
		// 初始化边信息
		try {
			BufferedReader br = new BufferedReader(new FileReader(edgeFile));

			while ((thisLine = br.readLine()) != null) {
				String[] a = thisLine.split("\\s+");
				int eid = Integer.parseInt(a[0]);
				int n1 = Integer.parseInt(a[1]);
				int n2 = Integer.parseInt(a[2]);
				// System.out.println(a[0] + " " + a[1] + " " + a[2]);
				Edge e = new Edge(eid, n1, n2);
				edgeMap.put(eid, e);
				if (vertexMap.containsKey(n1)) {
					vertexMap.get(n1).add(eid);
				} else {
					List<Integer> ls = new ArrayList<Integer>();
					ls.add(eid);
					vertexMap.put(n1, ls);
				}
				if (vertexMap.containsKey(n2)) {
					vertexMap.get(n2).add(eid);
				} else {
					List<Integer> ls = new ArrayList<Integer>();
					ls.add(eid);
					vertexMap.put(n2, ls);
				}
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println("初始化边集合完毕");
		}
	}

	// 算法第一部分，由{TR0，TR1，TR2....}得到基本簇{B0，B1，B2....}
	public HashMap<Integer, Integer> getBaseCluster(String carID) {
		// 路网匹配
		System.out.println("车辆ID：" + carID);
		String filename = "File/carInfo/" + carID + ".txt";
		List<Edge> tempway = new ArrayList<Edge>();
		String thisLine = null;
		try {
			System.out.println("路网匹配中：");
			BufferedReader br = new BufferedReader(new FileReader(filename));
			int count = 0;
			while ((thisLine = br.readLine()) != null) {
				String[] a = thisLine.split(",");
				// System.out.println(thisLine);
				int state = Integer.parseInt(a[2]);
				int effect = Integer.parseInt(a[8]);
				if ((state == 0 || state == 1) && (effect == 1)) {
					double x0 = Double.parseDouble(a[5]);
					double y0 = Double.parseDouble(a[4]);
					// System.out.println(x0 + " " + y0);
					Edge edge = getNearEdge(x0, y0);
					count++;
					// edge.printData();
					tempway.add(edge);
				}
			}
			br.close();
			System.out.println("路网数量：" + count);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
		// 补全匹配到的路径
		System.out.println("补全路径中：");
		int count = 0;
		List<Edge> finalway = new ArrayList<Edge>();
		for (int i = 0; i < tempway.size() - 1; i++) {
			int a = 0;
			count++;
			getExtraWay(finalway, tempway.get(i), tempway.get(i + 1), a);
		}
		System.out.println("填补完全之后数量：" + count);
		if ((tempway.size() - 1) >= 1) {
			finalway.add(tempway.get(tempway.size() - 1));
		}

		// 统计边的密度值
		for (int i = 0; i < finalway.size(); i++) {
			Edge e = finalway.get(i);
			// e.printData();
			int eid = e.getEdgeId();
			if (edgeDensity.containsKey(eid)) {
				edgeDensity.put(eid, edgeDensity.get(eid) + 1);
			} else {
				edgeDensity.put(eid, 1);
			}
			// 更新每条边的轨迹集合
			if (edgeCollection.containsKey(eid)) {
				edgeCollection.get(eid).add(Integer.parseInt(carID));
			} else {
				List<Integer> ls = new ArrayList<Integer>();
				ls.add(Integer.parseInt(carID));
				edgeCollection.put(eid, ls);
			}

		}

		// 写入edgeDensity
		filename = "File/edgeDensity/" + carID + "edgeDensity" + ".txt";
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
			Iterator iter = edgeDensity.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				Integer key = (Integer) entry.getKey();
				Integer val = (Integer) entry.getValue();
				// System.out.println("key = " + key + " " + "value = " +
				// val);
				bw.write(key + " " + val);
				bw.newLine();
			}
			bw.flush();
			bw.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		// 写入edgeCollection
		filename = "File/edgeCollection/" + carID + "edgeCollection" + ".txt";
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
			Iterator iter = edgeCollection.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				Integer key = (Integer) entry.getKey();
				// List<String> val = (List<String>) entry.getValue();
				String line = key + "  ";
				bw.write(line);
				bw.newLine();
			}
			bw.flush();
			bw.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		edgeDensity = null;
		edgeDensity = new HashMap<Integer, Integer>();
		edgeCollection = null;
		edgeCollection = new HashMap<Integer, List<Integer>>();

		return null;
	}

	// 补全两条不相邻的边之间的边
	private void getExtraWay(List<Edge> finalway, Edge edge1, Edge edge2, int a) {
		if (a > 3) { // 3 0.6 2 0.45 1 0.27
			finalway.add(edge1);
			// System.out.println("return");
			return;
		}
		int relation = getRelationBetweenEdge(edge1, edge2);
		if (relation == 1) {
			// do nothing
		} else if (relation == 0) {
			finalway.add(edge1);
		} else {
			Node n11 = nodeMap.get(edge1.getStartNode());
			Node n12 = nodeMap.get(edge1.getEndNode());
			Node n21 = nodeMap.get(edge2.getStartNode());
			Node n22 = nodeMap.get(edge2.getEndNode());
			double distance = 1000;
			Node midNodeS = null;
			Node midNodeE = null;
			;
			double l1 = lineSpace(n11.getxPoint(), n11.getyPoint(),
					n21.getxPoint(), n21.getyPoint());
			if (l1 < distance) {
				midNodeS = n11;
				midNodeE = n21;
				distance = l1;
				l1 = lineSpace(n11.getxPoint(), n11.getyPoint(),
						n22.getxPoint(), n22.getyPoint());
			}
			if (l1 < distance) {
				midNodeS = n11;
				midNodeE = n22;
				distance = l1;
				l1 = lineSpace(n12.getxPoint(), n12.getyPoint(),
						n21.getxPoint(), n21.getyPoint());
			}
			if (l1 < distance) {
				midNodeS = n12;
				midNodeE = n21;
				distance = l1;
				l1 = lineSpace(n12.getxPoint(), n12.getyPoint(),
						n22.getxPoint(), n22.getyPoint());
			}
			if (l1 < distance) {
				midNodeS = n12;
				midNodeE = n22;
			}
			double x = (midNodeS.getxPoint() + midNodeE.getxPoint()) / 2;
			double y = (midNodeS.getyPoint() + midNodeE.getyPoint()) / 2;
			Edge e = getNearEdge(x, y);
			if ((e.getEdgeId() != edge1.getEdgeId())
					&& (e.getEdgeId() != edge2.getEdgeId())) {
				a++;
				getExtraWay(finalway, edge1, e, a);
				getExtraWay(finalway, e, edge2, a);
			}
		}
	}

	// 获取两边之间关系 相同，相邻，或无关。
	private int getRelationBetweenEdge(Edge edge1, Edge edge2) {
		int x1 = edge1.getStartNode();
		int y1 = edge1.getEndNode();
		int x2 = edge2.getStartNode();
		int y2 = edge2.getEndNode();
		if (((x1 == x2) && (y1 == y2)) || ((x1 == y2) && (y1 == x2))) {
			return 1; // 同一条边
		} else if (((x1 == x2) || (y1 == y2)) || ((x1 == y2) || (y1 == x2))) {
			return 0; // 相连
		} else {
			return -1; // 无关
		}
	}

	// 算法第二部分，由基本簇{B0，B1，B2....}得到路径流{F0，F1，F2}
	public List<ArrayList> getFlowCluster(int min) {

		getEdgeCollectionAndEdgeDensity();
		removeMinDensity(min);
		System.out.println("读取完毕！");
		// showedgeCollection();
		// showedgeDensity();
		List<List<Integer>> flowCluster = new ArrayList<List<Integer>>();
		Edge e = null;
		while ((e = getMaxDensity()) != null) { // 获取密度最大的边
			List<Integer> flow = new ArrayList<Integer>();
			flow.add(e.getEdgeId());
			extrendFlowCluster(e, e.getStartNode(), flow);
			extrendFlowCluster(e, e.getEndNode(), flow);
			flowCluster.add(flow);
		}
		List<Integer> numList = new ArrayList<Integer>();
		for (int i = 0; i < flowCluster.size(); i++) {
			List<Integer> temp = flowCluster.get(i);
			if (temp.size() > 20) {
				numList.add(temp.size());
				// for (int j = 0; j < temp.size(); j++) {
				// Edge tempe = edgeMap.get(temp.get(j));
				// tempe.printData();
				// }
			}
		}
		Collections.sort(numList);
		for (int i = 0; i < numList.size(); i++) {
			System.out.println(numList.get(i));
		}
		System.out.println(numList.size());
		return null;
	}

	private void extrendFlowCluster(Edge e, int nodeId, List<Integer> flow) {
		List<Integer> neighbors = getneighbors(e, nodeId);
		if (neighbors == null) {
			return;
		}
		List<Integer> eCollection = edgeCollection.get(e.getEdgeId()); // 当前集合所有轨迹
		int Density = 0;
		Integer nextNeighbors = null;
		for (int i = 0; i < neighbors.size(); i++) {
			List<Integer> neighborCollection = edgeCollection.get(neighbors
					.get(i));
			if (neighborCollection != null) {
				int count = 0;
				for (int j = 0; j < neighborCollection.size(); j++) {
					if (eCollection.contains(neighborCollection.get(j))) {
						count++;
					}
				}
				if (count > Density) {
					if (edgeDensity.containsKey(neighbors.get(i))) {
						Density = count;
						nextNeighbors = neighbors.get(i);
					}
				}
			}
		}
		if (nextNeighbors != null) {
			flow.add(nextNeighbors);
			if (edgeDensity.containsKey(nextNeighbors)) {
				edgeDensity.remove(nextNeighbors);
			}
			Edge nextE = edgeMap.get(nextNeighbors);
			int nextNodeId = 0;
			if (nodeId == nextE.getStartNode()) {
				nextNodeId = nextE.getEndNode();
			} else {
				nextNodeId = nextE.getStartNode();
			}
			extrendFlowCluster(nextE, nextNodeId, flow);
		}

	}

	private List<Integer> getneighbors(Edge e, int nodeId) {
		List<Integer> neighbors = vertexMap.get(nodeId);
		Integer eId = e.getEdgeId();
		if (neighbors != null) {
			neighbors.remove(eId);
		}
		return neighbors;
	}

	private void removeMinDensity(int min) {
		Iterator iter = edgeDensity.entrySet().iterator();
		List<Integer> ls = new ArrayList<Integer>();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			Integer key = (Integer) entry.getKey();
			Integer val = (Integer) entry.getValue();
			if (val < min) {
				ls.add(key);
			}
		}
		for (int i = 0; i < ls.size(); i++) {
			edgeDensity.remove(ls.get(i));
		}
		System.out.println(edgeDensity.size());
	}

	private Edge getMaxDensity() {
		if (edgeDensity.isEmpty()) {
			return null;
		}
		Iterator iter = edgeDensity.entrySet().iterator();
		int max = 0;
		int edgeId = 0;
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			Integer key = (Integer) entry.getKey();
			Integer val = (Integer) entry.getValue();
			if (val > max) {
				max = val;
				edgeId = key;
			}
		}
		Edge e = edgeMap.get(edgeId);
		edgeDensity.remove(edgeId);
		return e;
	}

	private void getEdgeCollectionAndEdgeDensity() {
		String pathCollection = "File/edgeCollection/";
		File fileCollection = new File(pathCollection);
		File[] CollectionFile = fileCollection.listFiles();
		System.out.println("读取edgeCollection：");
		for (int i = 0; i < CollectionFile.length; i++) {
			String s = CollectionFile[i].getName();
			String carId[] = s.split("edgeCollection.txt");
			System.out.println("读取：" + carId[0]);
			String thisLine = null;
			try {
				BufferedReader br = new BufferedReader(new FileReader(
						pathCollection + s));
				while ((thisLine = br.readLine()) != null) {
					// System.out.println(thisLine);
					String[] a = thisLine.split("\\s+");
					if (edgeCollection.containsKey(Integer.parseInt(a[0]))) {
						edgeCollection.get(Integer.parseInt(a[0])).add(
								Integer.parseInt(carId[0]));
					} else {
						List<Integer> ls = new ArrayList<Integer>();
						ls.add(Integer.parseInt(carId[0]));
						edgeCollection.put(Integer.parseInt(a[0]), ls);
					}
				}
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		String pathedgeDensity = "File/edgeDensity/";
		File fileedgeDensity = new File(pathedgeDensity);
		File[] DensityFile = fileedgeDensity.listFiles();
		System.out.println("读取edgeDensity：");
		for (int i = 0; i < DensityFile.length; i++) {
			String s = DensityFile[i].getName();
			String carId[] = s.split("edgeDensity.txt");
			System.out.println("读取：" + carId[0]);
			String thisLine = null;
			try {
				BufferedReader br = new BufferedReader(new FileReader(
						pathedgeDensity + s));
				while ((thisLine = br.readLine()) != null) {
					// System.out.println(thisLine);
					String[] a = thisLine.split("\\s+");
					if (edgeDensity.containsKey(Integer.parseInt(a[0]))) {
						int tempa = edgeDensity.get(Integer.parseInt(a[0]));
						edgeDensity.put(Integer.parseInt(a[0]),
								tempa + Integer.parseInt(a[1]));
					} else {
						edgeDensity.put(Integer.parseInt(a[0]),
								Integer.parseInt(a[1]));
					}
				}
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	// 算法第三部分，路径流的优化
	public List<ArrayList> refineFlowCluster(Float min,
			List<ArrayList> flowCluster) {
		// List<ArrayList> flowClusterRefine = new ArrayList<ArrayList>();
		// 遍历每一条路径流
		// 遍历每一条其他路径流
		// 两条流分别取首尾端点（a1,b1) (a2,b2)
		// 根据点计算距离
		// 若距离小于一定阈值，则两条路径流合并
		return null;
	}

	private Edge getNearEdge(double x0, double y0) {
		Edge e = null;
		double distance = 1000;
		Iterator iter = edgeMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			Edge tempE = (Edge) entry.getValue();
			int node1 = tempE.getStartNode();
			int node2 = tempE.getEndNode();
			double tempDistance = getDistance(x0, y0, node1, node2);
			if (tempDistance < distance) {
				distance = tempDistance;
				e = tempE;
			}
		}

		// System.out.println(e.getEdgeId()+" "+distance);
		return e;
	}

	private double getDistance(double x0, double y0, int node1, int node2) {
		Node n1 = nodeMap.get(node1);
		Node n2 = nodeMap.get(node2);
		double x1 = n1.getxPoint();
		double y1 = n1.getyPoint();
		double x2 = n2.getxPoint();
		double y2 = n2.getyPoint();
		// System.out.println(x0+" "+y0+" "+x1+" "+y1+" "+x2+" "+y2);
		double space = 0;
		double a, b, c;
		a = lineSpace(x1, y1, x2, y2);// 线段的长度
		b = lineSpace(x1, y1, x0, y0);// (x1,y1)到点的距离
		c = lineSpace(x2, y2, x0, y0);// (x2,y2)到点的距离
		if (c <= 0.000001 || b <= 0.000001) {
			space = 0;
			return space;
		}
		if (a <= 0.000001) {
			space = b;
			return space;
		}
		if (c * c >= a * a + b * b) {
			space = b;
			return space;
		}
		if (b * b >= a * a + c * c) {
			space = c;
			return space;
		}
		double p = (a + b + c) / 2;// 半周长
		double s = Math.sqrt(p * (p - a) * (p - b) * (p - c));// 海伦公式求面积
		space = 2 * s / a;// 返回点到线的距离（利用三角形面积公式求高）
		return space;
	}

	private double lineSpace(double x1, double y1, double x2, double y2) {
		double lineLength = 0;
		lineLength = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
		return lineLength;
	}

	private void showedgeCollection() {
		System.out.println("当前Collection集合：");
		Iterator iter = edgeCollection.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			Integer key = (Integer) entry.getKey();
			List<Integer> val = (List<Integer>) entry.getValue();
			String s = "RodeID: ";
			s = s + key + "  Collection: ";
			for (int i = 0; i < val.size(); i++) {
				Integer c = val.get(i);
				s = s + c + " ";
			}
			System.out.println(s);
		}
	}

	private void showedgeDensity() {
		System.out.println("当前密度集合：");
		Iterator iter = edgeDensity.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			Integer key = (Integer) entry.getKey();
			Integer val = (Integer) entry.getValue();
			System.out.println(key + " " + val);

		}
	}
}
