package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import com.google.common.collect.ImmutableSet;
import jakarta.annotation.Nonnull;

import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import java.util.*;

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
		private ImmutableSet<Piece> allPlayers;

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
			//winner set is ALWAYS empty, need to change this to only be empty when the game starts.
			this.winner = ImmutableSet.of();
			//this.moves = generateMoves(remaining);
			var builder = ImmutableSet.<Piece>builder();
			builder.add(mrX.piece());
			for (Player i : detectives) {
				builder.add(i.piece());
			}
			this.allPlayers = builder.build();

		}

		@Override
		public GameSetup getSetup() {
			return setup;
		}

		@Override
		public ImmutableSet<Piece> getPlayers() {
			return allPlayers;
		}

		@Override
		public Optional<Integer> getDetectiveLocation(Piece.Detective detective) {
			Optional<Integer> i = Optional.empty();
				for (Player j : detectives) {
					if (detective.isDetective() && (j.piece().equals(detective))) return Optional.of(j.location());
				}
			return Optional.empty();
		}

		@Override
		public Optional<TicketBoard> getPlayerTickets(Piece piece) {

			//using lambdas!! woop woop. but we need to be able to explain how it works SO
			//lambdas = shortcuts. If an interface has exactly ONE method, you don't need to write out
			//a whole new class to implement it, just use arrow notation ->
			//requestedTicket is the parameter, Java knows it's a ticket because the TicketBoard interface (in board)
			//says so.
			// -> can be read as "executes"
			//mrX.tickets().getOrDefault(requestedTicket, 0) is the logic, it looks in the map
			//for the requested ticket, finds it, returns it.
			//if it doesn't find it, return 0 (getOrDefault bit)

			if (piece.isMrX()) return Optional.of(requestedTicket -> mrX.tickets().getOrDefault(requestedTicket, 0));
			for (Player i : detectives) {
				if (i.piece() == piece) return Optional.of(requestedTicket -> i.tickets().getOrDefault(requestedTicket, 0));
			}
			return Optional.empty();
		}

		@Override
		public ImmutableList<LogEntry> getMrXTravelLog() {
			return log;
		}

		@Override
		public ImmutableSet<Piece> getWinner() {
			// testWinningPlayersIsEmptyInitially
			//ImmutableSet<Piece> winnerSet = new ImmutableSet<Piece>();
			//for (Player i : detectives) {
			//	if (i.location() == mrX.location()) {

			//	}
			//}
			//return ImmutableSet.of();
			return winner;
		}

		@Override
		public ImmutableSet<Move> getAvailableMoves() {
			//makeSingleMoves();
			return moves;
		}

		private static ImmutableSet<Move.SingleMove> makeSingleMoves(GameSetup setup, List<Player> detectives, Player player, int source){

			// TODO create an empty collection of some sort, say, HashSet, to store all the SingleMove we generate
			HashSet<Move.SingleMove> singleMoveHashSet = new HashSet<>();

			for(int destination : setup.graph.adjacentNodes(source)) {
				// TODO find out if destination is occupied by a detective
				//  if the location is occupied, don't add to the collection of moves to return
				for (Player i : detectives) {
					if (i.location() == destination) {
						continue;
					}
				}

				for (ScotlandYard.Transport t : setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of()) ) {
					// TODO find out if the player has the required tickets
					//  if it does, construct a SingleMove and add it the collection of moves to return
					if (t == ScotlandYard.Transport.TAXI) {
						if (player.has(t.requiredTicket())){
						singleMoveHashSet.add(new Move.SingleMove(player.piece(), source, t.requiredTicket(), destination));}
					}
					if (t == ScotlandYard.Transport.BUS) {
						if (player.has(t.requiredTicket())){
							singleMoveHashSet.add(new Move.SingleMove(player.piece(), source, t.requiredTicket(), destination));}
					}
					if (t == ScotlandYard.Transport.UNDERGROUND) {
						if (player.has(t.requiredTicket())){
							singleMoveHashSet.add(new Move.SingleMove(player.piece(), source, t.requiredTicket(), destination));}
					}
					if (t == ScotlandYard.Transport.FERRY) {
						if (player.has(t.requiredTicket())){
							singleMoveHashSet.add(new Move.SingleMove(player.piece(), source, t.requiredTicket(), destination));}
					}
				}

				// TODO consider the rules of secret moves here
				//  add moves to the destination via a secret ticket if there are any left with the player
				if (player.has(ScotlandYard.Ticket.SECRET)) {
					singleMoveHashSet.add(new Move.SingleMove(player.piece(), source, ScotlandYard.Ticket.SECRET, destination));}
			}

			// TODO return the collection of moves
            return ImmutableSet.copyOf(singleMoveHashSet);
        }

		private static ImmutableSet<Move.DoubleMove> makeDoubleMoves(GameSetup setup, List<Player> detectives, Player player, int source){
			//check that player actually has a double ticket
			if (!player.has(ScotlandYard.Ticket.DOUBLE)) return ImmutableSet.of();

			Set<Move.SingleMove> firstMoves = makeSingleMoves(setup, detectives, player, source);

			HashSet<Move.DoubleMove> secondMoveHashSet = new HashSet<>();

			for (Move.SingleMove i : firstMoves){

				//get the destination and ticket of each first move, then use the destination as the 'starting point' for the next move.
				int dest1 = i.destination;
				ScotlandYard.Ticket ticket1 = i.ticket;

				Set<Move.SingleMove> secondMoves = makeSingleMoves(setup, detectives, player, dest1);

				for (Move.SingleMove j : secondMoves) {
					int dest2 = j.destination;
					ScotlandYard.Ticket ticket2 = j.ticket;
					// if ticket1 == ticket2 it means mrX is trying to use for example 2 Taxi tickets in a row.
					// must now make sure that the player has 2 or more of that specific ticket
					if (ticket1 == ticket2){
						if (!player.hasAtLeast(ticket1, 2)) continue;
					}
					Move.DoubleMove doubleMove = new Move.DoubleMove(player.piece(), source, ticket1, dest1, ticket2, dest2);
					secondMoveHashSet.add(doubleMove);
				}
			}
			return ImmutableSet.copyOf(secondMoveHashSet);
		}
/*
		//private ImmutableSet<Move> generateMoves(ImmutableSet<Piece> remaining) {
		//	Set<Move> moves = new HashSet<>();
		//	for (Piece i : remaining) {
		//		if (i.isDetective()){
		//
 		//		}
		//	}
		//	if (remaining.contains(mrX)){
		//		//single and double moves
		//	}
		//	else if (remaining.contains(detectives)){
		//		//just single moves
		//		moves.addAll(makeSingleMoves(setup, detectives, ));
		//	}
		//}
*/
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



