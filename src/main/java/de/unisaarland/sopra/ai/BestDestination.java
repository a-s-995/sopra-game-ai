package de.unisaarland.sopra.ai;

import de.unisaarland.sopra.actions.Action;
import de.unisaarland.sopra.model.Model;
import de.unisaarland.sopra.model.Position;
import de.unisaarland.sopra.model.fields.LavaField;
import de.unisaarland.sopra.model.fields.WaterField;

import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Antoine on 06.10.16.
 * <p>
 * project Anti
 */
class BestDestination {

	private Map<Position, Path> hash = new HashMap<>();
	private Model model;
	int enemyId;
	//the healingfields AND maybe bushFields
	private Collection<Position> fieldsPos;
	private Position destination;

	BestDestination(Map<Position, Path> hash, Model model, Collection<Position> fieldsPos, int enemyId) {
		this.hash = hash;
		this.model = model;
		this.enemyId = enemyId;
		this.fieldsPos = fieldsPos;
	}

	BestDestination(Map<Position, Path> hash, Model model, int enemyId) {
		this.hash = hash;
		this.model = model;
		this.enemyId = enemyId;
	}

	/**
	 * this method is called, if i want to go beside the destination
	 *
	 * @return a deque with moves beside the destination
	 */
	Deque<Action> toActionQueue() {
//		allgo();
		int min = 1001;
		destination = model.getMonster(enemyId).getPosition();
		List<Path> movebeside = new LinkedList<>();
		Path nearestPath = null;
		for (Path value : hash.values()) {
			if (value.getCost() > 1000) {
				continue;
			}
			//add all pathes, that have distance one to the destination, into a list
			if (value.getCurrent().getDistanceTo(destination) == 1) {
				movebeside.add(value);
			}
			//get the nearest path to enemy, not the cheapest
			if (value.getCurrent().getDistanceTo(destination) < min) {
				//add all pathes, that have distance one to the destination, into a list
				min = value.getCurrent().getDistanceTo(destination);
				nearestPath = value;
			}
		}
		//if i may move beside the destination, get the path with the minimal costs
		int minCost = 10000;
		if (!movebeside.isEmpty()) {
			for (Path pfd : movebeside) {
				if (pfd.getCost() < minCost) {
					minCost = pfd.getCost();
					nearestPath = pfd;
				}
			}
		}
		assert nearestPath != null;
		return extraMethod(nearestPath);
	}

	/**
	 * this method is called, if i want to move ON the destination
	 * <p>
	 * if there is no destination, this method will lead beside the enemy
	 *
	 * @return a deque
	 */
	Deque<Action> toActionQueueNotBeside() {
//		allgo();
		if (calculateDestination()) {
			int min = 1000;
			Path nearestPath = null;
			for (Path value : hash.values()) {
				if (value.getCost() > 1000) {
					continue;
				}
				//get the nearest path to enemy, not the cheapest
				if (value.getCurrent().getDistanceTo(destination) < min) {


					min = value.getCurrent().getDistanceTo(destination);
					nearestPath = value;
				}
			}
			assert nearestPath != null;
			return extraMethod(nearestPath);
		}
		return toActionQueue();
	}


	/**
	 * i need this method for no code dublication
	 * <p>
	 * in fact, it gets a path and converts it to a deque of actions
	 */
	private Deque<Action> extraMethod(Path nearestPath) {
		Deque<Action> moves = new LinkedList<>();
		boolean bool = true;
		while (nearestPath.getLastAction() != null) {
			// add the last action as first one in the queue
			if (bool) {
				if ((model.getField(nearestPath.getCurrent()) instanceof LavaField
						|| model.getField(nearestPath.getCurrent()) instanceof WaterField)) {
					nearestPath = nearestPath.getThePath();
					continue;
				}
			}
			bool = false;
			moves.addFirst(nearestPath.getLastAction());
			nearestPath = nearestPath.getThePath();
		}
		return moves;
	}

	Position getDestination() {
		return destination;
	}

	private boolean calculateDestination() {
		int cost = 1001;
		boolean hasDestination = false;
		for (Position position : fieldsPos) {
			for (Map.Entry<Position, Path> pf : hash.entrySet()) {
				if (position.equals(pf.getValue().getCurrent()) && (pf.getValue().getCost() < cost)) {
					cost = pf.getValue().getCost();
					destination = position;
					hasDestination = true;
				}
			}
		}
		return hasDestination;
	}
}

