package de.unisaarland.sopra.ai;

import de.unisaarland.sopra.actions.Action;
import de.unisaarland.sopra.model.Position;

/**
 * Created by Antoine on 04.10.16.
 * <p>
 * project Anti
 */
public class Path {
	Position current;
	int cost;
	Path thePath;
	Action lastAction;
	Boolean alreadyVisit;

	public Path(Position current, int cost, Path thePath, Action lastAction) {
		this.current = current;
		this.cost = cost;
		this.thePath = thePath;
		this.lastAction = lastAction;
		this.alreadyVisit = false;
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
