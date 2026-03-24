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
	private final static class MyGameState implements GameState {
		//attributes
		private final GameSetup setup;
		private final ImmutableSet<Piece> remaining;
		private final ImmutableList<LogEntry> log;
		private final Player mrX;
		private final List<Player> detectives;
		private ImmutableSet<Move> moves;
		private final ImmutableSet<Piece> winner;
		private final ImmutableSet<Piece> allPlayers;

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

			var builder = ImmutableSet.<Piece>builder();
			builder.add(mrX.piece());
			for (Player i : detectives) {
				builder.add(i.piece());
			}
			this.allPlayers = builder.build();

			this.moves = getAvailableMoves();

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
			HashSet<Move> moves = new HashSet<>();
			for (Piece i : remaining) {
				Player player = null;
				if (i.isMrX()) player = mrX;
				else {
					for (Player j : detectives){
						if (j.piece() == i) {
							player = j;
							break;
						}
					}
				}
				if (player == null) throw new IllegalArgumentException("Piece not found!");

				int location = player.location();

				moves.addAll(makeSingleMoves(setup, detectives, player, location));
				//check that there are enough move available to make a double move by substracting the remaining moves from the total number of moves allowed.
				if (i.isMrX() && (setup.moves.size() - log.size() >= 2)) {
					moves.addAll(makeDoubleMoves(setup, detectives, player, location));
				}
			}
			return ImmutableSet.copyOf(moves);
		}

		private static ImmutableSet<Move.SingleMove> makeSingleMoves(GameSetup setup, List<Player> detectives, Player player, int source){

			// TODO create an empty collection of some sort, say, HashSet, to store all the SingleMove we generate
			HashSet<Move.SingleMove> singleMoveHashSet = new HashSet<>();

			for(int destination : setup.graph.adjacentNodes(source)) {
				// TODO find out if destination is occupied by a detective
				//  if the location is occupied, don't add to the collection of moves to return
				boolean isLocationOccupied = false;
				for (Player i : detectives) {
					if (i.location() == destination) {
						isLocationOccupied = true;
						break;
					}
				}

				if (isLocationOccupied) continue;

				for (ScotlandYard.Transport t : setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of()) ) {
					// TODO find out if the player has the required tickets
					//  if it does, construct a SingleMove and add it the collection of moves to return
					if (player.has(t.requiredTicket())) {
						singleMoveHashSet.add(new Move.SingleMove(player.piece(), source, t.requiredTicket(), destination));
					}
				}
				// TODO consider the rules of secret moves here
				//  add moves to the destination via a secret ticket if there are any left with the player
				if (player.has(ScotlandYard.Ticket.SECRET)) {
					singleMoveHashSet.add(new Move.SingleMove(player.piece(), source, ScotlandYard.Ticket.SECRET, destination));
				}
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

		@Override
		public GameState advance(Move move) {
			//EXPLANATION OF THE VISITOR PATTERN
			//In java, if you have a Move object, the compiler doesn't know whether it is a SingleMove or DoubleMove object
			//the visitor pattern allows the object to tell you what it is
			//we pass into move.accept(...) an anonymous inner class that implements the Move.Visitor interface.
			//the game state tracks whose turn it is using the 'remaining' attribute
			//advance()'s job if to determine what this set should look like for the next turn

			if(!moves.contains(move)) throw new IllegalArgumentException("Illegal move: "+move);
			//takes in a move and returns a new game state. a move comes in, giving the new position. need to return new game state
			//with player at that new position.
			//when mrx: is it a reveal round?
			//when detectives moves:
			//use visitor pattern

			GameState nextState = move.accept(new Move.Visitor<GameState>() {
				@Override
				public GameState visit(Move.SingleMove move) {
					List<LogEntry> newLog = new ArrayList<>(log);
					ArrayList<Player> newDetectives = new ArrayList<>(detectives);
					//singlemove uses dynamic dispatch and visitor pattern NEED TO KNOW THIS WELL
					//logic to deal with two cases of single move (mrx or detectives).
					//you can find out if mrx or detectives as there is stuff in the move. e.g. move.commencedBy().isMr()
					//find out if it is a reveal round or not
					//if you loop through (setup.moves == true) it is a reveal round
					//loop through the log immutable list for this

					if (move.commencedBy().isMrX()) {
						//in the setup attribute, if
						if (setup.moves.get(log.size()) == true){
							//add the ticket and the destination to the log
							LogEntry newEntry = LogEntry.reveal(move.ticket, move.destination);
							newLog.add(newEntry);
						}
						else {
							//add hidden move to the log
							newLog.add(LogEntry.hidden(move.ticket));
						}
						//.use(Ticket) returns a new player with that specific ticket deducted
						//.give(Ticket) returns a new play with that specific ticket added
						//.at(int location) returns a new player sitting at the new destination
						//player.use(Ticket).at(location) creates a new version of the player who has one less ticket and is now standing at a new location
						Player newMrX = mrX.use(move.ticket).at(move.destination);
					}
					else if (move.commencedBy().isDetective()) {
						for (Player i : detectives) {
							if (i.piece() == move.commencedBy()){
								Player newDetective = i.use(move.ticket).at(move.destination);
								//give mrX the exact ticket the detective just spent, his location statys the same
								Player newMrx = mrX.give(move.ticket);
								newDetectives.add(newDetective);
							}
							newDetectives.add(i);
						}
					}
					//for detectives: use the immutable pieces set remaining.
					//when detectives makes a move, don't put in new set of remaining. look at what tucket was used and give to mr x
					//if it's a double move, it will be mr x
					return new MyGameState(setup, remaining, ImmutableList.copyOf(newLog), mrX, ImmutableList.copyOf(newDetectives));
				}

				@Override
				public GameState visit(Move.DoubleMove move) {
					List<LogEntry> newLog = new ArrayList<>(log);

					//check whether move 1 is a reveal round or not
					if (setup.moves.get(log.size()) == true){
						newLog.add(LogEntry.reveal(move.ticket1, move.destination1));
					}
					else {
						newLog.add(LogEntry.hidden(move.ticket1));
					}

					//check whether move 2 is a reveal round or not
					if (setup.moves.get(log.size() + 1) == true){
						newLog.add(LogEntry.reveal(move.ticket2, move.destination2));
					}
					else {
						newLog.add(LogEntry.hidden(move.ticket2));
					}
					Player newMrX = mrX.use(ScotlandYard.Ticket.DOUBLE).use(move.ticket1).use(move.ticket2).at(move.destination2);
					return new MyGameState(setup, remaining, ImmutableList.copyOf(newLog), mrX, detectives);
				}
			});
            return nextState;
        }

	}


}



