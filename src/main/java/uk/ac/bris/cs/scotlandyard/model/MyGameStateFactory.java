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
// hello hello hello
public final class MyGameStateFactory implements Factory<GameState> {

	//create initial state, validate input, set variables
	@Nonnull @Override public GameState build(
			GameSetup setup,
			Player mrX,
			ImmutableList<Player> detectives) {
		// TODO
		//throw new RuntimeException("Implement me!");
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
		private ImmutableSet<Piece> remaining;
		private ImmutableList<LogEntry> log;
		private Player mrX;
		private List<Player> detectives;
		private ImmutableSet<Move> moves;
		private ImmutableSet<Piece> winner;

		//constructor
		private MyGameState(
				final GameSetup setup,
				final ImmutableSet<Piece> remaining,
				final ImmutableList<LogEntry> log,
				final Player mrX,
				final List<Player> detectives) {
			//constructor builds immutable game snapshot (computes moves and winner)
			//generate initial moves
			//moves = getAvailableMoves();
			//if mrx has no legal move at the beginning, he immediately loses
			//if (moves.isEmpty() && remaining.contains(mrX)) {
			//	winner.equals(detectives);
			//} else {winner.equals(null);};
			this.setup = setup;
			this.remaining = remaining;
			this.log = log;
			this.mrX = mrX;
			this.detectives = detectives;
			if(setup.moves.isEmpty()) throw new IllegalArgumentException("Moves is empty!");
			if(remaining.isEmpty()) throw new IllegalArgumentException("Remaining pieces set is empty!");
			if(mrX == null) throw new NullPointerException("mrX is empty!");
			if (!mrX.isMrX()) throw new IllegalArgumentException("Mr X piece is invalid!");
			if(detectives.isEmpty()) throw new IllegalArgumentException("Detectives is empty!");
			if(setup.graph.edges().isEmpty() || setup.graph.nodes().isEmpty()) throw new IllegalArgumentException("Graph is empty!");
			for (Player i : detectives) {
				if (i.has(ScotlandYard.Ticket.SECRET) || i.has(ScotlandYard.Ticket.DOUBLE)) {
					throw new IllegalArgumentException("Detective cannot have secret or double ticket types!");
				}
				for (Player j : detectives) {
					if ((i.location() == j.location()) && (i != j)){
						throw new IllegalArgumentException("Duplicate detective locations!");
					}
				}
			}
		}

		@Override
		public GameSetup getSetup() {
			return setup;
		}

		@Override
		public ImmutableSet<Piece> getPlayers() {
			return null;
		}

		@Override
		public Optional<Integer> getDetectiveLocation(Piece.Detective detective) {
			for (Player j : detectives) {
				if (j.equals(detective)) return Optional.of(j.location());
			}

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
			// testWinningPlayersIsEmptyInitially
			return null;
		}

		@Override
		public ImmutableSet<Move> getAvailableMoves() {
			return null;
		}

		@Override
		public GameState advance(Move move) {
			if(!moves.contains(move)) throw new IllegalArgumentException("Illegal move: "+move);
			//takes in a move and returns a new game state. a move comes in, giving the new position. need to return new game state
			//with player at that new position.
			//when mrx: is it a reveal round?
			//when detectives moves:
			//use visitor pattern

			return move.accept(new Move.Visitor<GameState>() {
				@Override
				public GameState visit(Move.SingleMove move) {
					//singlemove uses dynamic dispatch and visitor pattern NEED TO KNOW THIS WELL
					//logic to deal with two cases of single move (mrx or detectives).
					//you can find out if mrx or detectives as there is stuff in the move. e.g. move.commencedBy().isMr()
					//find out if it is a reveal round or not
					if (move.commencedBy().isMrX()) {

					};
					//if you loop through (setup.moves == true) it is a reveal round
					//loop through the log immutable list for this

					//for detectives: use the immutable pieces set remaining.
					//when detectives makes a moves, don't put in new set of remaining. look at what tucket was used and give to mr x
					//if it's a double move, it will be mr x
					return null;
				}

				@Override
				public GameState visit(Move.DoubleMove move) {
					return null;
				}
			});
		}

	}

}



