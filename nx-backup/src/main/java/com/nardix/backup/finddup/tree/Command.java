package com.nardix.backup.finddup.tree;

import java.util.List;
import java.util.List;

public interface Command {
	void visit(List<Node> path);
	void visit(List<Node> path, Node current);
}
