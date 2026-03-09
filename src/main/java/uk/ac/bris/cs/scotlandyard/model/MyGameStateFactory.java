package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import com.google.common.collect.ImmutableSet;
import jakarta.annotation.Nonnull;

import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import java.util.List;
import java.util.Optional;

/**
 * cw-model
 * Stage 1: Complete this class
 */
public final class MyGameStateFactory implements Factory<GameState> {

	//create initial state, validate input, set variables
	@Nonnull @Override public GameState build(
			GameSetup setup,
			Player mrX,
			ImmutableList<Player> detectives) {
		// TODO
		//throw new RuntimeException("Implement me!");
		// check detectives have no secret or double tickets
		for (Player i : detectives) {
			if (i.has(ScotlandYard.Ticket.SECRET) || i.has(ScotlandYard.Ticket.DOUBLE)) {
				throw new RuntimeException("Detective cannot have secret or double ticket types!");
			}
			//check for duplicate locations
			for (Player j : detectives) {
				if (i.location() == j.location()){
					throw new RuntimeException("Duplicate detective locations!");
				}
			}
		}
		// check that detective list is not empty
		if (detectives.isEmpty()) {
			throw new RuntimeException("No detectives!");
		}
		//check mr x piece is correct
		if (!mrX.isMrX()) {
			throw new RuntimeException("Mr X piece is invalid!");
		}
		return new MyGameState(
				setup,
				ImmutableSet.of(Piece.MrX.MRX), // only mr x moves at start
				ImmutableList.of(), // travel log starts empty
				mrX,
				detectives
		);

	}
	private final class MyGameState implements GameState {
		//attributes
		private GameSetup setup;
		private ImmutableSet<Piece> remainingPieces;
		private ImmutableList<LogEntry> log;
		private Player mrX;
		private List<Player> detectives;
		private ImmutableSet<Move> moves;
		private ImmutableSet<Piece> winner;
		private List<Player> remainingPlayers;

		//constructor
		private MyGameState(
				final GameSetup setup,
				final ImmutableSet<Piece> remaining,
				final ImmutableList<LogEntry> log,
				final Player mrX,
				final List<Player> detectives) {
				//constructor builds immutable game snapshot
		}

		@Override
		public final GameSetup getSetup() {
			return null;
		}

		@Override
		public ImmutableSet<Piece> getPlayers() {
			return null;
		}

		@Override
		public Optional<Integer> getDetectiveLocation(Piece.Detective detective) {
			return Optional.empty();
		}

		@Override
		public Optional<TicketBoard> getPlayerTickets(Piece piece) {
			return Optional.empty();
		}

		@Override
		public ImmutableList<LogEntry> getMrXTravelLog() {
			return null;
		}

		@Override
		public ImmutableSet<Piece> getWinner() {
			return null;
		}

		@Override
		public ImmutableSet<Move> getAvailableMoves() {
			return null;
		}

		@Override
		public GameState advance(Move move) {
			return null;
		}

	}

}
