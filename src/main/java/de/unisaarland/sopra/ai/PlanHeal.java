package de.unisaarland.sopra.ai;

import de.unisaarland.sopra.Direction;
import de.unisaarland.sopra.actions.Action;
import de.unisaarland.sopra.model.Model;
import de.unisaarland.sopra.model.Position;

/**
 * Created by Antoine on 02.10.16.
 * <p>
 * project Anti
 */
public class PlanHeal extends Planning {

	Action healinAciton;
	Position healinField;

	PlanHeal(Model model, int myID, int enemyId, Position healinField) {
		super(model, myID, enemyId);
		this.healinField = healinField;
	}

	Action getMoveAct() {
		initializeHeal();
		return healinAciton;
	}

	private void initializeHeal() {
		// schaumal, obs buschfeld hinter dem feind liegt

		this.healinAciton = getBestMove(healinField);
	}
	@Override
	protected Action whichAttack(Direction direction) {
		return null;
	}
}
