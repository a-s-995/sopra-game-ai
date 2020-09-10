package de.unisaarland.sopra.ai;

import de.unisaarland.sopra.actions.Action;
import de.unisaarland.sopra.model.Position;

/**
 * Created by Antoine on 04.10.16.
 * <p>
 * project Anti
 */
class Path {
	//die position des feldes, wohin dieser pfad führt
	private Position current;
	//die kosten zu dieser position
	private int cost;
	//der vorausgehende pfad, zum feld davor
	private Path theLastPath;
	//from this Action, i get from where i come from
	private Action lastAction;


	Path(Position current, int cost) {
		this.current = current;
		this.cost = cost;
	}

	Path(Position current, int cost, Path thePath, Action lastAction) {
		this.current = current;
		this.cost = cost;
		this.theLastPath = thePath;
		this.lastAction = lastAction;
	}

	Position getCurrent() {
		return current;
	}

	int getCost() {
		return cost;
	}

	Path getThePath() {
		return theLastPath;
	}

	Action getLastAction() {
		return lastAction;
	}

	void setCost(int cost) {
		this.cost = cost;
	}

	void setThePath(Path thePath) {
		this.theLastPath = thePath;
	}

	void setLastAction(Action lastAction) {
		this.lastAction = lastAction;
	}
	//new simulate controller a
	//a.step(command)
	//b = a.getmodel
	//new simulate controller(b) c

}
