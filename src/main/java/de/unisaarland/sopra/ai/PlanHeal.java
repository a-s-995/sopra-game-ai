package de.unisaarland.sopra.ai;

import de.unisaarland.sopra.Direction;
import de.unisaarland.sopra.actions.Action;
import de.unisaarland.sopra.actions.SlashAttack;
import de.unisaarland.sopra.actions.StabAttack;
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
		if (model.getMonster(enemyId).getHealth() <= 54 && model.getMonster(enemyId).getHealth() >= 42
				&& (myMonster.getEnergy() == 1000 || myMonster.getEnergy() == 580)) {
			return new SlashAttack(direction);
		}
		if (myMonster.getEnergy() == 900 || myMonster.getEnergy() == 480 || myMonster.getEnergy() == 700) {
			return new SlashAttack(direction);
		}
		return new StabAttack(direction);
	}
}
