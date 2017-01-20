package com;

class Edge {
	private int edgeId;
	private int startNode, endNode;

	// private double weight;

	public Edge(int edgeId, int startNode, int endNode) {
		this.edgeId = edgeId;
		this.startNode = startNode; //
		this.endNode = endNode;//
		// this.weight = weight; //
	}

	public int getStartNode() {
		return startNode;
	}

	public int getEndNode() {
		return endNode;
	}

	public int getEdgeId() {
		return edgeId;
	}

	public void printData() {
		System.out.println(edgeId + " " + startNode + " " + endNode);
	}
}

class Node {
	private int nodeId;
	private double x, y;

	public Node(int nodeId, double x, double y) {
		this.nodeId = nodeId;
		this.x = x;
		this.y = y;
	}

	public int getNodeId() {
		return nodeId;
	}

	public double getxPoint() {
		return x;
	}

	public double getyPoint() {
		return y;
	}

	public void printData() {
		System.out.println(nodeId + " " + x + " " + y);
	}
}