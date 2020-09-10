package de.unisaarland.sopra.ai;

import de.unisaarland.sopra.Direction;
import de.unisaarland.sopra.actions.Action;
import de.unisaarland.sopra.actions.MoveAction;
import de.unisaarland.sopra.actions.SlashAttack;
import de.unisaarland.sopra.actions.StabAttack;
import de.unisaarland.sopra.model.Model;
import de.unisaarland.sopra.model.Position;
import de.unisaarland.sopra.model.fields.BushField;


import java.util.HashSet;
import java.util.Set;

/**
 * Created by Antoine on 30.09.16.
 * <p>
 * project Anti
 */
class PlanningAttack extends Planning {

	//the Action to return
	private Action actionAttack;
	private Set<Position> bushFields = new HashSet<>();

	public PlanningAttack(Model model, int myID, int enemyId, Set<Position> bushFields) {
		super(model, myID, enemyId);
		actionAttack = null;
		this.bushFields = bushFields;
	}

	Action getActionAttack() {
		initializeAttack();
		return actionAttack;
	}

	private void initializeAttack() {
		//may i attack?
		if (model.getMonster(enemyId).getPosition().getDistanceTo(myMonster.getPosition()) == 1) {
			actionAttack = getAttack();
			//is there a bush around my monster while attacking, move at the end on the bush
			if (myMonster.getEnergy() <= 250 && !(model.getField(myMonster.getPosition()) instanceof BushField)) {
				Action bush = bushArround();
				if (bush != null) {
					actionAttack = bush;
				}
			}
			return;
		}
		//evade to move back again, if i just got on the bushfield
		if (model.getField(myMonster.getPosition()) instanceof BushField
				&& myMonster.getEnergy() <= 150) {
			return;
		}
		actionAttack = getBestMove(model.getMonster(enemyId).getPosition());
	}

	/**
	 * should look, if there is a bush around, and if yes, return the movement on it
	 */
	private Action bushArround() {
		for (Position position : bushFields) {
			if (position.getDistanceTo(myMonster.getPosition()) == 1) {
				return getBestMove(position);
			}
		}
		return null;
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
