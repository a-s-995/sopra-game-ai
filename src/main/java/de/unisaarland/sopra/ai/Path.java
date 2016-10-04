package de.unisaarland.sopra.ai;

import de.unisaarland.sopra.actions.Action;
import de.unisaarland.sopra.model.Position;

/**
 * Created by Antoine on 04.10.16.
 * <p>
 * project Anti
 */
public class Path {
	//die position des feldes, wohin dieser pfad führt
	Position current;
	//die kosten zu dieser position
	int cost;
	//der vorausgehende pfad, zum feld davor
	Path thePath;

	Action lastAction;
	//true, if its already visited, initial false
	Boolean alreadyVisit = false;

	public Path() {

	}

	public Position getCurrent() {
		return current;
	}

	public int getCost() {
		return cost;
	}

	public Path getThePath() {
		return thePath;
	}

	public Action getLastAction() {
		return lastAction;
	}

	public void setCurrent(Position current) {
		this.current = current;
	}

	public void setCost(int cost) {
		this.cost = cost;
	}

	public void setThePath(Path thePath) {
		this.thePath = thePath;
	}

	public void setLastAction(Action lastAction) {
		this.lastAction = lastAction;
	}
	//new simulate controller a
	//a.step(command)
	//b = a.getmodel
	//new simulate controller(b) c

}
