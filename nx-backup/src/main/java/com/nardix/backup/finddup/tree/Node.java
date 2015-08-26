package com.nardix.backup.finddup.tree;

import java.util.ArrayList;
import java.util.List;

import com.nardix.backup.finddup.DupFileInfo;

public class Node {
	protected String name;
	protected List<Node> childs;
	DupFileInfo dupFileInfo;

	public Node(String name) {
		this.name = name;
		childs = new ArrayList<Node>();
		dupFileInfo = null;
	}
	
	public Node(String name, DupFileInfo fInfo) {
		this.name = name;
		this.dupFileInfo = fInfo;
		childs = null; //new ArrayList<Node>();
	}
	
	public Node addProperty(String key, String val) {
		//properties.put(key, val);
		return this;
	}
	
	public Node addChild(String nodeName) {
		for (int i = 0; i < childs.size(); i++) {
			Node node = childs.get(i);
			int comp = nodeName.compareTo(node.name);
			if (comp == 0) {
				return node;
			} else if (comp < 0) {
				Node n = new Node(nodeName);
				childs.add(i, n);
				return n;
			}
		}
	
		Node n = new Node(nodeName);
		childs.add(n);
		return n;
	}
	
	public Node addChild(String nodeName, DupFileInfo fInfo) {
		for (int i = 0; i < childs.size(); i++) {
			Node node = childs.get(i);
			int comp = nodeName.compareTo(node.name);
			//if (comp == 0) {
				//throw new RuntimeException("Filename already exists!:" + nodeName);
			//} else if (comp < 0) {
			if (comp < 0) {
				Node n = new Node(nodeName, fInfo);
				childs.add(i, n);
				return n;
			}
		}
	
		Node n = new Node(nodeName, fInfo);
		childs.add(n);
		return n;
	}

	public Node getNextChild(Node n) {
		if (childs.size() == 0) {
			return null;
		}
		if (n == null)
			return childs.get(0);
	
		for (Node node : childs) {
			if (node.name.compareTo(n.name) > 0)
				return node;
		}
		return null;
	}


}

