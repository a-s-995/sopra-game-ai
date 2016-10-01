package de.unisaarland.sopra.ai;

import de.unisaarland.sopra.Direction;
import de.unisaarland.sopra.actions.Action;
import de.unisaarland.sopra.actions.StabAttack;
import de.unisaarland.sopra.model.Model;

/**
 * Created by Antoine on 01.10.16.
 * <p>
 * project Anti
 */
class PlanWaitPhase extends Planning {

	//the Action to return
	private Action actionAttacke;

	PlanWaitPhase(Model model, int myID, int enemyId) {
		super(model, myID, enemyId);
	}

	Action getActionAttacke() {
		initializeAttack();
		return actionAttacke;
	}

	private void initializeAttack() {
		actionAttacke = getAttack();
	}

	@Override
	protected Action whichAttack(Direction direction) {
		return new StabAttack(direction);
	}
}
