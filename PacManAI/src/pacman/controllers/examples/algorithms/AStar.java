package pacman.controllers.examples.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.PriorityQueue;

import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;


public class AStar {
	
	public N headNode;
	
	public AStar( int depth){
		createTree(depth);
	}
	
	public void createTree(int depth){
		ArrayList<N> currentDepthNodes = new ArrayList<N>();
		ArrayList<N> nextDepthNodes = new ArrayList<N>();
		headNode = new N(MOVE.NEUTRAL, null);
		currentDepthNodes.add(headNode);
		for (int i = 0; i < depth; i++) {
			for (N node : currentDepthNodes) {
				N left = new N(MOVE.LEFT, node);
				N right = new N(MOVE.RIGHT, node);
				N up = new N(MOVE.UP, node);
				N down = new N(MOVE.DOWN, node);
				
				nextDepthNodes.add(left);
				nextDepthNodes.add(right);
				nextDepthNodes.add(up);
				nextDepthNodes.add(down);
				
				ArrayList<N> neighbors = new ArrayList<N>(4);
				neighbors.add(left);
				neighbors.add(right);
				neighbors.add(up);
				neighbors.add(down);
				node.adj = neighbors;
			}
			
			currentDepthNodes = nextDepthNodes;
			nextDepthNodes = new ArrayList<N>();
		}
	}
	
	public MOVE getMove(Game game, EnumMap<GHOST, MOVE> ghostMoves){
		long startTime = System.currentTimeMillis();
		LinkedList<N> nodes = new LinkedList<N>();
		nodes.add(headNode);
		
		N start = headNode;

        PriorityQueue<N> open = new PriorityQueue<N>();
        ArrayList<N> closed = new ArrayList<N>();

        start.g = 0;
        headNode.gameState = game;
        start.h = -1 *(Evaluation.evaluateGameState(headNode.gameState));

        //N n = new N(start);
        open.add(start);
        while(!open.isEmpty()){
            N node = open.poll();
            closed.add(node);
            node.visited = true;
            
            if(node.gameState.getNumberOfActivePills() == 0) break; 
            
            if (node.predecessor != null) { // regular Node
				// set gameState and advance move based on current node
				Game gameState = node.predecessor.gameState.copy();
				gameState.advanceGame(node.move, ghostMoves);
				node.gameState = gameState;
			}else{
				Game gameState = node.gameState.copy();
				gameState.advanceGame(node.move, ghostMoves);
				node.gameState = gameState;
			}
            
            for(N next : node.adj){
            	if(next.move!=node.move.opposite()){
            		//System.out.println("In the loop");
	                double currentDistance = next.h;
	                Game nextGameState = next.predecessor.gameState.copy();
	                nextGameState.advanceGame(next.move, ghostMoves);
	                next.gameState = nextGameState;
	
	                if (!open.contains(next) && !closed.contains(next)){
	                    next.g = (currentDistance + node.g);
	                    next.h = ( (Evaluation.evaluateGameState(next.gameState)));
	                    next.predecessor = node;
 
	                    open.add(next);
	                    
	                }
	                else if (currentDistance + node.g < next.g){
	                    //next.node.g = currentDistance + currentNode.g;
	                	next.g = (currentDistance + node.g); 
	                    //next.node.parent = currentNode;
	                	next.predecessor = (node);
	                    
	                	
	                    if (open.contains(next))
	                        open.remove(next);
	
	                    if (closed.contains(next))
	                        closed.remove(next);
	                   
	                    open.add(next);
	                    
	                }
	            }
            }
        }
        

        if (Evaluation.LOG_TIME) System.out.println(System.currentTimeMillis() - startTime);
        return extractPath(closed.get(closed.size()-1));
    }
	
	public MOVE extractPath(N node){
		ArrayList<MOVE> route = new ArrayList<MOVE>();
		while(node.predecessor != null){
			route.add(node.move);
			node = node.predecessor;
		}
		Collections.reverse(route);
		return route.get(0);
	}
	
	class N implements Comparable<N>{
	    public N predecessor;
	    public double g, h;
	    public boolean visited = false;
	    public ArrayList<N> adj;
	    public MOVE move;
	    public Game gameState;

	    public N(MOVE move, N node){
	        adj = new ArrayList<N>();
	        this.move = move;
	        this.predecessor = node;
	        g = 1;
	    }

	    public N(double g, double h){
	        this.g = g;
	        this.h = h;
	    }

	    @Override
		public int compareTo(N another){
	      if ((g + h) < (another.g + another.h))
	    	  return -1;
	      else  if ((g + h) > (another.g + another.h))
	    	  return 1;
			
			return 0;
		}
	}
}
