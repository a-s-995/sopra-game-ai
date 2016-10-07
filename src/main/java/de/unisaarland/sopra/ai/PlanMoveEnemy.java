package de.unisaarland.sopra.ai;

import de.unisaarland.sopra.Direction;
import de.unisaarland.sopra.actions.Action;
import de.unisaarland.sopra.actions.SlashAttack;
import de.unisaarland.sopra.actions.StabAttack;
import de.unisaarland.sopra.model.Model;

/**
 * Created by Antoine on 30.09.16.
 * <p>
 * project Anti
 */
class PlanMoveEnemy extends Planning {

	//the Action to return
	private Action moveAction;

	PlanMoveEnemy(Model model, int myID, int enemyID) {
		super(model, myID, enemyID);
	}

	Action getMoveAction() {
		initializeMove();
		return moveAction;
	}

	private void initializeMove() {
		this.moveAction = getBestMove(model.getMonster(enemyId).getPosition());
	}


	protected Action whichAttack(Direction direction) {
		/*if (myMonster.getEnergy() == 900 || myMonster.getEnergy() == 480 || myMonster.getEnergy() == 700) {
			return new SlashAttack(direction);
		}*/
		return new StabAttack(direction);
	}
}
