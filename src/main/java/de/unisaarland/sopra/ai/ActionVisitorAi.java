package de.unisaarland.sopra.ai;

import de.unisaarland.sopra.Direction;
import de.unisaarland.sopra.actions.ActionVisitor;
import de.unisaarland.sopra.actions.BiteAttack;
import de.unisaarland.sopra.actions.BlinkAction;
import de.unisaarland.sopra.actions.BoarAttackAction;
import de.unisaarland.sopra.actions.BurnAttack;
import de.unisaarland.sopra.actions.CastAttack;
import de.unisaarland.sopra.actions.ClawAttack;
import de.unisaarland.sopra.actions.CrushAttack;
import de.unisaarland.sopra.actions.CutAttack;
import de.unisaarland.sopra.actions.MoveAction;
import de.unisaarland.sopra.actions.ShootAttack;
import de.unisaarland.sopra.actions.SingeAttack;
import de.unisaarland.sopra.actions.SlashAttack;
import de.unisaarland.sopra.actions.StabAttack;
import de.unisaarland.sopra.actions.StareAttack;
import de.unisaarland.sopra.actions.SwapAttack;
import de.unisaarland.sopra.actions.WarCryAction;
import de.unisaarland.sopra.model.Position;

/**
 * Created by Antoine on 29.09.16.
 * <p>
 * project Anti
 */
public class ActionVisitorAi extends ActionVisitor<Direction> {
	@Override
	public Direction visitBiteAttack(BiteAttack biteAttack, Direction direction) {
		return null;
	}

	@Override
	public Direction visitClawAttack(ClawAttack clawAttack, Direction direction) {
		return null;
	}

	@Override
	public Direction visitCrushAttack(CrushAttack crushAttack, Direction direction) {
		return null;
	}

	@Override
	public Direction visitCutAttack(CutAttack cutAttack, Direction direction) {
		return null;
	}

	@Override
	public Direction visitSingeAttack(SingeAttack singeAttack, Direction direction) {
		return null;
	}

	@Override
	public Direction visitSlashAttack(SlashAttack slashAttack, Direction direction) {
		return direction;
	}

	@Override
	public Direction visitStabAttack(StabAttack stabAttack, Direction direction) {
		return direction;
	}

	@Override
	public Direction visitStareAttack(StareAttack stareAttack, Direction direction) {
		return null;
	}

	@Override
	public Direction visitBurnAttack(BurnAttack burnAttack) {
		return null;
	}

	@Override
	public Direction visitShootAttack(ShootAttack shootAttack, Direction direction) {
		return null;
	}

	@Override
	public Direction visitCastAttack(CastAttack castAttack, Position position) {
		return null;
	}

	@Override
	public Direction visitSwapAttack(SwapAttack swapAttack, Position position) {
		return null;
	}

	@Override
	public Direction visitMoveAction(MoveAction moveAction, Direction direction) {
		return direction;
	}

	@Override
	public Direction visitBlinkAction(BlinkAction blinkAction, Position position) {
		return null;
	}

	@Override
	public Direction visitBoarAttackAction(BoarAttackAction boarAttackAction, int i) {
		return null;
	}

	@Override
	public Direction visitWarCryAction(WarCryAction warCryAction, String s) {
		return null;
	}
}
