package pacman.controllers.examples.move;

import java.util.ArrayList;

import pacman.game.Constants.MOVE;

public class Tree {
	
	// headNode supports the branches, has no other major function
	private Node headNode;
	
	public Tree(int depth) {
		headNode = new Node();
		
		ArrayList<Node> currentDepthNodes = new ArrayList<Node>();
		ArrayList<Node> nextDepthNodes = new ArrayList<Node>();
		currentDepthNodes.add(headNode);
		for (int i = 0; i < depth; i++) {
			for (Node node : currentDepthNodes) {
				Node left = new Node(MOVE.LEFT, node);
				Node right = new Node(MOVE.RIGHT, node);
				Node up = new Node(MOVE.UP, node);
				Node down = new Node(MOVE.DOWN, node);
				
				nextDepthNodes.add(left);
				nextDepthNodes.add(right);
				nextDepthNodes.add(up);
				nextDepthNodes.add(down);
				
				ArrayList<Node> neighbors = new ArrayList<Node>(4);
				neighbors.add(left);
				neighbors.add(right);
				neighbors.add(up);
				neighbors.add(down);
				node.setNeighbors(neighbors);
			}
			
			currentDepthNodes = nextDepthNodes;
			nextDepthNodes = new ArrayList<Node>();
		}
	}
	
	public Node getHeadNode() {
		return headNode;
	}
}
