package de.unisaarland.sopra.ai;

import com.sun.org.apache.xerces.internal.impl.dv.xs.BooleanDV;
import com.sun.org.apache.xpath.internal.operations.Bool;
import de.unisaarland.sopra.Direction;
import de.unisaarland.sopra.actions.Action;
import de.unisaarland.sopra.actions.MoveAction;
import de.unisaarland.sopra.actions.StabAttack;
import de.unisaarland.sopra.model.Model;
import de.unisaarland.sopra.model.Position;

/**
 * Created by Antoine on 30.09.16.
 * <p>
 * project Anti
 */
class PlanMoveBush extends Planning {


	//the Action to return
	private Action moveAct;
	private Position closestBush;

	/**
	 * will just be created if there are bushes
	 *
	 * @param model   the model
	 * @param myID    my id
	 * @param enemyId enemies id
	 */
	PlanMoveBush(Model model, int myID, int enemyId, Position closestBush) {
		super(model, myID, enemyId);
		this.closestBush = closestBush;
	}

	Action getMoveAct() {
		initializeMove();
		return moveAct;
	}

	private void initializeMove() {
		//when mein monster neben dem gegner steht zu beginn dieser phase, soll es 1mal angreifen und dann gehen
		if (model.getMonster(enemyId).getPosition().getDistanceTo(myMonster.getPosition()) == 1) {
			if (myMonster.getEnergy() == 1000 || myMonster.getEnergy() == 750) {
				moveAct = getAttack();
				if (moveAct != null) {
					return;
				}
			}
		}
		// schaumal, obs buschfeld hinter dem feind liegt
		if(closestBush.getDistanceTo(myMonster.getPosition()) >
				closestBush.getDistanceTo(model.getMonster(enemyId).getPosition())) {
			if(myMonster.getEnergy() == 500 || myMonster.getEnergy() == 250) {
				//kann zum gegner hinlaufen, nicht blockiert
				if(!canMoveToBush(closestBush)) {
					this.moveAct = getAttack();
					return;
					// TODO: 01.10.16  schau jetzt mal das der was gescheites macht!!!!
				}
			}
		}
		this.moveAct = getBestMove(closestBush);
	}

	private Boolean canMoveToBush(Position whereIWantToGo) {
		//the distance to whereIWantToGo, if myMonster moves to it, this has to decrease
		int oldDistance = whereIWantToGo.getDistanceTo(myMonster.getPosition());

		//go through all directions
		for (Direction direction : Direction.values()) {
			Action move = new MoveAction(direction);

			//first, is a move in this direction valid?
			if (move.validate(model, myMonster)) {

				//if its valid, then get the neighbourFields position in the direction
				Position position = model.getBoard().getNeighbour(myMonster.getPosition(), direction).getPosition();

				//and from this position, get the distance to where i want to go, and look if it is lower than before
				if (position.getDistanceTo(whereIWantToGo) <= oldDistance) {
					//if its lower, return this move from which myMonster is closer to the enemy
					return true;
				}
			}
		}
		return false;
	}


	protected Action whichAttack(Direction direction) {
		return new StabAttack(direction);
	}
}
