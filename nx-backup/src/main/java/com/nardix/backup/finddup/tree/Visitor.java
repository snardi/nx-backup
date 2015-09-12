package com.nardix.backup.finddup.tree;

public interface Visitor {
	public void forwardVisit(Node n);
	public void backVisit(Node n);
	public void visit(Node n);
}
