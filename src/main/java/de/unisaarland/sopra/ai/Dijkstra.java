package de.unisaarland.sopra.ai;

import de.unisaarland.sopra.Direction;
import de.unisaarland.sopra.actions.Action;
import de.unisaarland.sopra.actions.MoveAction;
import de.unisaarland.sopra.commands.ActionCommand;
import de.unisaarland.sopra.commands.Command;
import de.unisaarland.sopra.controller.SimulateController;
import de.unisaarland.sopra.model.Model;
import de.unisaarland.sopra.model.Position;
import de.unisaarland.sopra.model.entities.Monster;
import de.unisaarland.sopra.model.fields.Field;



/**
 * Created by Antoine on 04.10.16.
 * <p>
 * project Anti
 */
public class Dijkstra {
	Model model;
	Monster myMonster;
	Field[][] fields = model.getBoard().getFields();


	private void allgo() {
		SimulateController controller = new SimulateController(model);
		Action move = new MoveAction(Direction.WEST);
		Command command = new ActionCommand(move, myMonster.getId());
		controller.step(command);
		controller.getModel();
	}

	private void fromTo(Position start, Position end) {


	}

}
