package com.nardix.backup.finddup.tree;

import java.util.List;

public class Print implements Command {

	@Override
	public void visit(List<Node> path) {
		System.out.println(">>> " + path.get(path.size()-1).name);
//		StringBuilder str = new StringBuilder();
//		Node file = null;
//
//		str.append("/");
//		for (Node node : path) {
//			file = node;
//			if (node.dupFileInfo != null /*.isFile*/) {
//				str.append(node.name);
//			} else {
//				str.append(node.name);
//				str.append("/");
//			}
//		}
//		//if (file.dupFileInfo.dup) {
//			System.out.printf("%33s %c %s\n", (file.dupFileInfo != null ? file.dupFileInfo.md5 : ""),
//				(file.dupFileInfo.dup ? 'D' : ' '), str.toString());
//		//}
	}

	@Override
	public void visit(List<Node> path, Node current) {
		System.out.println(">>> " + current.name);
//		StringBuilder str = new StringBuilder();
//		//Node file = null;
//
//		str.append("/");
//		for (Node node : path) {
//			//file = node;
//			if (node.dupFileInfo != null) {
//				str.append(node.name);
//			} else {
//				str.append(node.name);
//				str.append("/");
//			}
//		}
//		
//		str.append(current.name);
//		if (current.dupFileInfo.dup) {
//			System.out.printf("%33s %c %s\n", (current.dupFileInfo != null ? current.dupFileInfo.md5 : ""),
//				(current.dupFileInfo.dup ? 'D' : ' '), str.toString());
//		}
	}

}

