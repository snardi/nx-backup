package com.nardix.backup.finddup.tree;

public class PrintNodeVisitor implements Visitor {

	@Override
	public void forwardVisit(Node n) {
		if (n.dupFileInfo != null) {
			throw new RuntimeException("This node is a leaf!!!!");
		} else {
			System.out.println("F >> " + n.dirMd5 + " " + n.name);
		}
	}

	@Override
	public void backVisit(Node n) {
		//System.out.println("B >> " + ((n.dupFileInfo != null) ? "F\t" : "D\t") + n.name);
	}

	@Override
	public void visit(Node n) {
		if (n.dupFileInfo == null) {
			throw new RuntimeException("This node is not a leaf!!!!");
		} else {
			System.out.println("V >> " + n.dupFileInfo.md5 + " " + n.dupFileInfo.file);
		}
	}

}
