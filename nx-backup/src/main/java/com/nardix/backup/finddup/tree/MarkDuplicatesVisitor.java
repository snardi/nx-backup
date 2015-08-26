package com.nardix.backup.finddup.tree;

public class MarkDuplicatesVisitor implements Visitor {

	@Override
	public void forwardVisit(Node n) {
		System.out.println("F >> " + ((n.dupFileInfo != null) ? "\t" : "D\t") + n.name);
	}

	@Override
	public void backVisit(Node n) {
		System.out.println("B >> " + ((n.dupFileInfo != null) ? "F\t" : "D\t") + n.name);
	}

	@Override
	public void visit(Node n) {
		System.out.println("V >> " + ((n.dupFileInfo != null) ? "F\t" : "D\t") + n.name);
	}

}
