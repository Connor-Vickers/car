/**
 * @ AUTHOR NAME HERE
 * @ Starter Code By Guocheng
 *
 * 2016-01-30
 * For: Purdue Hackers - Battleship
 * Battleship Client
 */

import java.io.*;
import java.net.Socket;
import java.net.InetAddress;
import java.lang.Thread;
import java.util.LinkedList;
import java.awt.Point;

public class Battleship {
	public static String API_KEY = "72091250"; ///////// PUT YOUR API KEY HERE /////////
	public static String GAME_SERVER = "battleshipgs.purduehackers.com";

	//////////////////////////////////////  PUT YOUR CODE HERE //////////////////////////////////////

	char[] letters;
	int[][] grid;
	int[] ships;

	LinkedList<Point> hits = new LinkedList<Point>();

	// Destroyer - 2
	// Submarine - 3
	// Cruiser - 3
	// Battleship - 4
	// Carrier - 5

	void placeShips(String opponentID) {
		// Fill Grid With -1s
		for(int i = 0; i < grid.length; i++) { for(int j = 0; j < grid[i].length; j++) grid[i][j] = -1; }

		// Place Ships
		placeDestroyer("A0", "A1");
		placeSubmarine("B0", "B2");
		placeCruiser("C0", "C2");
		placeBattleship("D0", "D3");
		placeCarrier("E0", "E4");
	}

	int turn = 0;

	void makeMove() {

		System.out.println("turn: " + turn);
		turn++;

		System.out.println("empty?:" + hits.isEmpty());
		while (!hits.isEmpty()) {
			Point p = hits.peek();

			try { if (grid[p.x-1][p.y] == -1) { sendMove(p.x-1, p.y); return; } } catch (Exception e) {}
			try { if (grid[p.x+1][p.y] == -1) { sendMove(p.x+1, p.y); return; } } catch (Exception e) {}
			try { if (grid[p.x][p.y-1] == -1) { sendMove(p.x, p.y-1); return; } } catch (Exception e) {}
			try { if (grid[p.x][p.y+1] == -1) { sendMove(p.x, p.y+1); return; } } catch (Exception e) {}

			hits.remove();
		}


		int x = 0, y = 0;
		int max = 0;

		
		//numberShipsCanFit(0, 0);
		//numberShipsCanFit(0, 1);
		//numberShipsCanFit(1, 0);

		for(int i = 0; i < 8; i++) {
			for(int j = 0; j < 8; j++) {
				if (this.grid[i][j] == -1) {
					int temp = numberShipsCanFit(i, j);
					if (temp > max) {
						x = i;
						y = j;
						max = temp;
					}
				}
			}
		}

		sendMove(x, y);
		System.out.println("Made move: " + x + "," + y);

		/*int x = 0, y = 0;

		for(int i = 0; i < 8; i++) {
			for(int j = 0; j < 8; j++) {
				if (this.grid[i][j] == -1) {
					x = i;
					y = j;
					sendMove(x,y);
					return;
				}
			}
		}*/
	}

	private void sendMove(int x, int y) {
		String wasHitSunkOrMiss = placeMove(this.letters[x] + String.valueOf(y));
		System.out.println("Got from placing: " + wasHitSunkOrMiss);

		if (wasHitSunkOrMiss.equals("Hit")) {
			this.grid[x][y] = 1;
			hits.add(new Point(x, y));
		} else if (wasHitSunkOrMiss.equals("Sunk")) {
			this.grid[x][y] = 2;
			hits.add(new Point(x, y));
		} else {
			this.grid[x][y] = 0;			
		}
	}

	private int numberShipsCanFit(int x, int y) {
		int spaceLeft = 0;
		for (int i = x-1; (i >= 0) && (this.grid[i][y] != 0); i--) {
			spaceLeft++;
		}
		int spaceRight = 0;
		for (int i = x+1; (i < this.grid.length) && (this.grid[i][y] == -1); i++) {
			spaceRight++;
		}
		int spaceUp = 0;
		for (int i = y-1; (i >= 0) && (this.grid[x][i] == -1); i--) {
			spaceUp++;
		}
		int spaceDown = 0;
		for (int i = y+1; (i < this.grid.length) && (this.grid[x][i] == -1); i++) {
			spaceDown++;
		}

		//System.out.println("  " + spaceUp + "  ");
		//System.out.println(spaceLeft+" x " + spaceRight);
		//System.out.println("  " + spaceDown + "  ");

		int total = 0;
		//System.out.println("testing for " + x + "," + y);

		for (int i = 0; i <ships.length; i++) {
			int horz = Math.min(spaceLeft, ships[i]-1) + Math.min(spaceRight, ships[i]-1) + 1;
			int vert = Math.min(spaceUp, ships[i]-1) + Math.min(spaceDown, ships[i]-1) + 1;
			total += Math.max((horz - ships[i] + 1), 0);
			total += Math.max((vert - ships[i] + 1), 0);
		}
		return total;		
	}

	////////////////////////////////////// ^^^^^ PUT YOUR CODE ABOVE HERE ^^^^^ //////////////////////////////////////

	Socket socket;
	String[] destroyer, submarine, cruiser, battleship, carrier;

	String dataPassthrough;
	String data;
	BufferedReader br;
	PrintWriter out;
	Boolean moveMade = false;

	public Battleship() {
		this.grid = new int[8][8];
		for(int i = 0; i < grid.length; i++) { for(int j = 0; j < grid[i].length; j++) grid[i][j] = -1; }
		this.letters = new char[] {'A','B','C','D','E','F','G','H'};
		this.ships = new int[] {2,3,3,4,5};

		destroyer = new String[] {"A0", "A0"};
		submarine = new String[] {"A0", "A0"};
		cruiser = new String[] {"A0", "A0"};
		battleship = new String[] {"A0", "A0"};
		carrier = new String[] {"A0", "A0"};
	}

	void connectToServer() {
		try {
			InetAddress addr = InetAddress.getByName(GAME_SERVER);
			socket = new Socket(addr, 23345);
			br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);

			out.print(API_KEY);
			out.flush();
			data = br.readLine();
		} catch (Exception e) {
			System.out.println("Error: when connecting to the server...");
			socket = null; 
		}

		if (data == null || data.contains("False")) {
			socket = null;
			System.out.println("Invalid API_KEY");
			System.exit(1); // Close Client
		}
	}



	public void gameMain() {
		while(true) {
			try {
				if (this.dataPassthrough == null) {
					this.data = this.br.readLine();
				}
				else {
					this.data = this.dataPassthrough;
					this.dataPassthrough = null;
				}
			} catch (IOException ioe) {
				System.out.println("IOException: in gameMain"); 
				ioe.printStackTrace();
			}
			if (this.data == null) {
				try { this.socket.close(); } 
				catch (IOException e) { System.out.println("Socket Close Error"); }
				return;
			}

			if (data.contains("Welcome")) {
				String[] welcomeMsg = this.data.split(":");
				placeShips(welcomeMsg[1]);
				if (data.contains("Destroyer")) { // Only Place Can Receive Double Message, Pass Through
					this.dataPassthrough = "Destroyer(2):";
				}
			} else if (data.contains("Destroyer")) {
				this.out.print(destroyer[0]);
				this.out.print(destroyer[1]);
				out.flush();
			} else if (data.contains("Submarine")) {
				this.out.print(submarine[0]);
				this.out.print(submarine[1]);
				out.flush();
			} else if (data.contains("Cruiser")) {
				this.out.print(cruiser[0]);
				this.out.print(cruiser[1]);
				out.flush();
			} else if (data.contains("Battleship")) {
				this.out.print(battleship[0]);
				this.out.print(battleship[1]);
				out.flush();
			} else if (data.contains("Carrier")) {
				this.out.print(carrier[0]);
				this.out.print(carrier[1]);
				out.flush();
			} else if (data.contains( "Enter")) {
				this.moveMade = false;
				this.makeMove();
			} else if (data.contains("Error" )) {
				System.out.println("Error: " + data);
				System.exit(1); // Exit sys when there is an error
			} else if (data.contains("Die" )) {
				System.out.println("Error: Your client was disconnected using the Game Viewer.");
				System.exit(1); // Close Client
			} else {
				System.out.println("Recieved Unknown Response:" + data);
				System.exit(1); // Exit sys when there is an unknown response
			}
		}
	}

	void placeDestroyer(String startPos, String endPos) {
		destroyer = new String[] {startPos.toUpperCase(), endPos.toUpperCase()}; 
	}

	void placeSubmarine(String startPos, String endPos) {
		submarine = new String[] {startPos.toUpperCase(), endPos.toUpperCase()}; 
	}

	void placeCruiser(String startPos, String endPos) {
		cruiser = new String[] {startPos.toUpperCase(), endPos.toUpperCase()}; 
	}

	void placeBattleship(String startPos, String endPos) {
		battleship = new String[] {startPos.toUpperCase(), endPos.toUpperCase()}; 
	}

	void placeCarrier(String startPos, String endPos) {
		carrier = new String[] {startPos.toUpperCase(), endPos.toUpperCase()}; 
	}

	String placeMove(String pos) {
		if(this.moveMade) { // Check if already made move this turn
			System.out.println("Error: Please Make Only 1 Move Per Turn.");
			System.exit(1); // Close Client
		}
		this.moveMade = true;

		this.out.print(pos);
		out.flush();
		try { data = this.br.readLine(); } 
		catch(Exception e) { System.out.println("No response after from the server after place the move"); }

		if (data.contains("Hit")) return "Hit";
		else if (data.contains("Sunk")) return "Sun";
		else if (data.contains("Miss")) return "Miss";
		else {
			this.dataPassthrough = data;
			return "Miss";
		}
	}

	public static void main(String[] args) {
		Battleship bs = new Battleship();
		while(true) {
			bs.connectToServer();
			if (bs.socket != null) bs.gameMain();
		}	
	}
}

