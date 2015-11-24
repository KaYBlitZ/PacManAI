package pacman.controllers.examples.algorithms;

import java.util.ArrayList;
import java.util.Random;

import pacman.game.Constants.MOVE;

public class KNearestNeighbor {
	/*
	 * The way K Nearest Neighbor works is as follows:
	 * For a plane with a number of points (each designated as some class),
	 * the class for some point we choose is found by drawing a circle of 
	 * some radius. In this case, the radius is the depth. Looking at all the points
	 * inside the circle, the class with the most instances is the class we
	 * assign to the our chosen point.
	 * 
	 * For PacMan, 10 points are created for each of the 4 moves.
	 * Each point will be randomly created in a square defined by the 2 points (-5, -5)
	 * and (5, 5). Our chosen point will be the origin. The radius of the
	 * circle is the depth centered at the origin. We will return the MOVE with the most
	 * instances in the circle.
	 */
	private class KClass {
		public float x, y;
		public MOVE move;
		public KClass(MOVE move) {
			Random random = new Random(System.currentTimeMillis());
			x = (random.nextInt(101) - 50) / 5f; // from -10 to 10
			y = (random.nextInt(101) - 50) / 5f; // from -10 to 10
			this.move = move;
		}
	}
	
	public MOVE getMove(int depth) {
		// in this case, depth is k
		ArrayList<KClass> classes = new ArrayList<KClass>(40);
		for (int i = 0; i < 10; i++) classes.add(new KClass(MOVE.LEFT));
		for (int i = 0; i < 10; i++) classes.add(new KClass(MOVE.RIGHT));
		for (int i = 0; i < 10; i++) classes.add(new KClass(MOVE.UP));
		for (int i = 0; i < 10; i++) classes.add(new KClass(MOVE.DOWN));
		
		int numLefts = 0;
		int numRights = 0;
		int numUps = 0;
		int numDowns = 0;
		for (KClass k : classes) {
			if (Math.sqrt(Math.pow(k.x, 2) + Math.pow(k.y, 2)) < depth) {
				switch (k.move) {
				case LEFT:
					numLefts++;
					break;
				case RIGHT:
					numRights++;
					break;
				case UP:
					numUps++;
					break;
				case DOWN:
					numDowns++;
					break;
				default:
					break;
				}
			}
		}
		
		MOVE move = MOVE.LEFT;
		int maxNum = numLefts;
		
		if (numRights > maxNum){
			maxNum = numRights;
			move = MOVE.RIGHT;
		}
		if (numUps > maxNum) {
			maxNum = numUps;
			move = MOVE.UP;
		}
		if (numDowns > maxNum) {
			move = MOVE.DOWN;
		}
		return move;
	}
}
