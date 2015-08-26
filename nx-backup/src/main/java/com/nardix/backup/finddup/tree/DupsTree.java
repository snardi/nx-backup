package com.nardix.backup.finddup.tree;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.nardix.backup.finddup.DupFileInfo;

/**
 *  /one/two/three 
 *
 */
public class DupsTree {
	private Node root;
	private long numberOfFiles;
	
	
	public Object getNumberOfFiles() {
		return numberOfFiles;
	}

	public void addFile(DupFileInfo dupFile) {
		Vector<String> names = new Vector<String>();
		
		// FIXME: In case of FAT filesystems we don't
		// consider the drive unit.
		Path path = Paths.get(dupFile.file);
		for (int i = 0; i < path.getNameCount(); i++) {
			names.add(path.getName(i).toString());
		}
//		if (path.charAt(0) == '/') {
//			names = path.substring(1).split("/");
//		} else {
//			names = path.split("/");
//		}

		int startIdx = 1;
		if (root == null) {
			root = new Node(names.get(0));
		} else {
			if (!names.get(0).equals(root.name)) {
				throw new RuntimeException("Path does not belongs to this tree: " + path);
			}
		}
		
		Node currentNode = root;
		for (int i = startIdx; i < names.size(); i++) {
			if (i+1 == names.size()) { // Is the last name in path ?
				currentNode = currentNode.addChild(names.get(i), dupFile);
				numberOfFiles++;
			} else {
				currentNode = currentNode.addChild(names.get(i));
			}
			
		}	
	}
	
	public DupsTree getDuplicateFilesByContent() {
		return null;
	}
	
	public void print() {
		List<Node> path = new ArrayList<Node>();
		Node current = root;
		Node lastPrinted = null;
	
		path.add(current);
		long count = 0;
		for (;;) {
			if (current.dupFileInfo != null /*current.isFile*/) {
				printPath(path);
				count++;
				lastPrinted = path.get(path.size()-1);
				path.remove(path.size()-1);
				current = path.get(path.size()-1);

				// print all files with same name
				count += printSameName(path, current, lastPrinted.name);
			} else {
				current = current.getNextChild(lastPrinted); //SNS
				lastPrinted = null;
				if (current == null) {
					//System.err.println("Leaf is not a file !!!!");
					//printPath(path);
					lastPrinted = path.get(path.size()-1);
					path.remove(path.size()-1);
					if (path.size() == 0) {
						System.out.println("Files printed: " + count);
						return;
					}
					current = path.get(path.size()-1);
				} else {
					path.add(current);
				}
			}
		}
	}
	
	
	public void transverseLeaf(Command command) {
		List<Node> path = new ArrayList<Node>();
		Node current = root;
		Node lastPrinted = null;
	
		path.add(current);
		for (;;) {
			if (current.dupFileInfo != null /*current.isFile*/) {
				command.visit(path);
				lastPrinted = path.get(path.size()-1);
				path.remove(path.size()-1);
				current = path.get(path.size()-1);

				// visit all files with same name.
				visitSameName(command, path, current, lastPrinted.name);
			} else {
				current = current.getNextChild(lastPrinted);
				lastPrinted = null;
				if (current == null) {
					lastPrinted = path.get(path.size()-1);
					path.remove(path.size()-1);
					if (path.size() == 0) {
						return;
					}
					current = path.get(path.size()-1);
				} else {
					path.add(current);
				}
			}
		}
	}
	
	public void transverseFull(Command command) {
		List<Node> path = new ArrayList<Node>();
		Node current = root;
		Node lastPrinted = null;
	
		path.add(current);
		for (;;) {
			if (current.dupFileInfo != null /*current.isFile*/) {
				command.visit(path);
				lastPrinted = path.get(path.size()-1);
				path.remove(path.size()-1);
				current = path.get(path.size()-1);

				// visit all files with same name.
				visitSameName(command, path, current, lastPrinted.name);
			} else {
				command.visit(path);
				current = current.getNextChild(lastPrinted);
				lastPrinted = null;
				if (current == null) {
					lastPrinted = path.get(path.size()-1);
					path.remove(path.size()-1);
					if (path.size() == 0) {
						return;
					}
					current = path.get(path.size()-1);
				} else {
					path.add(current);
				}
			}
		}
	}
	
	public void transversePathFileToRoot(String pathName, Command command) {
		String names[];
		if (pathName.charAt(0) == '/') {
			names = pathName.substring(1).split("/");
		} else {
			names = pathName.split("/");
		}	
	
		List<Node> path = new ArrayList<Node>();
		Node curr = root;

		if (!curr.name.equals(names[0])) {
			throw new RuntimeException("path does not belong to this tree");
		}
		path.add(curr);
		for (int i = 1; i < names.length; i++) {
			for (int j=0; j < curr.childs.size(); j++) {
				if (curr.childs.get(j).name.equals(names[i])) {
					path.add(curr.childs.get(j));
					curr = curr.childs.get(j);
					break;
				}
			}
		}
		
		for (int i = path.size()-1; 0 <= i; i--) {
			command.visit(null, path.get(i));
		}
	}

	private long printSameName(List<Node> path, Node current, String name) {
		StringBuilder str = new StringBuilder();
		//Node file = null;
		long count = 0;

		str.append("/");
		for (Node node : path) { // path points to a directory
				str.append(node.name);
				str.append("/");
		}
		
		boolean first = true;
		for (Node c: current.childs) {
			if (c.name.equals(name)) {
				if (first)
					first = false;
				else {
					System.out.printf("%s %c %s\n", c.dupFileInfo.md5,
							(c.dupFileInfo.dup ? 'D' : ' '), str.toString() + c.name);	
					count++;
				}
					
			}
		}
		return count;
	}
	
	private void visitSameName(Command command, List<Node> path, Node current, String name) {
		boolean first = true;
		for (Node c: current.childs) {
			if (c.name.equals(name)) {
				if (first)
					first = false;
				else {
					command.visit(path, c);
				}
			}
		}
	}

	private void printPath(List<Node> path) {
		StringBuilder str = new StringBuilder();
		Node file = null;

		str.append("/");
		for (Node node : path) {
			if (node.childs == null /*.isFile*/) {
				str.append(node.name);
				file = node;
			} else {
				str.append(node.name);
				str.append("/");
			}
		}
		System.out.printf("%s %c %s\n", file.dupFileInfo.md5,
				(file.dupFileInfo.dup ? 'D' : ' '), str.toString());
	}
	
	
	

	public void printAll() {
		Print p = new Print();
		transverseFull(p);
	}


}

