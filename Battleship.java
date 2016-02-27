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
import java.util.Random;

public class Battleship {
	public static String API_KEY = "72091250"; ///////// PUT YOUR API KEY HERE /////////
	public static String GAME_SERVER = "battleshipgs.purduehackers.com";

	//////////////////////////////////////  PUT YOUR CODE HERE //////////////////////////////////////

	char[] letters;
	int[][] grid;
	boolean[] ourGrid;

	void placeShips(String opponentID) {
		// Fill Grid With -1s
		for(int i = 0; i < grid.length; i++) { for(int j = 0; j < grid[i].length; j++) grid[i][j] = -1; }
		ourGrid = new boolean[64];
		for(int i = 0; i < ourGrid.length; i++){ ourGrid[i] = false; }

		// Place Ships
		String[] temp;
		temp = placeEm(2);
		placeDestroyer(temp[0], temp[1]);
		temp = placeEm(3);
		placeSubmarine(temp[0], temp[1]);
		temp = placeEm(3);
		placeCruiser(temp[0], temp[1]);
		temp = placeEm(4);
		placeBattleship(temp[0], temp[1]);
		temp = placeEm(5);
		placeCarrier(temp[0], temp[1]);
	}

	String[] placeEm(int size){
		Random rn = new Random();
		boolean validPos = false;
		int[] places = new int[size];
		while(!validPos){
			places[0] = rn.nextInt(64);
			switch(rn.nextInt(4)){
				case 0://up
					for(int i = 1; i < size ; i++){
						places[i] = places[i-1]-8;
					}
					break;
				case 1://right
					for(int i = 1; i < size ; i++){
						places[i] = places[i-1]+1;
					}
					break;
				case 2://down
					for(int i = 1; i < size ; i++){
						places[i] = places[i-1]+8;
					}
					break;
				case 3://left
					for(int i = 1; i < size ; i++){
						places[i] = places[i-1]-1;
					}
					break;
			}

		    validPos = true;
			for(int i = 0; i < size; i++){
				if(places[i] < 0 || places[i] > 63){
					validPos = false;
				}
			}
			if(validPos){
				for(int i = 0; i < size; i++){
					if(ourGrid[places[i]]){
						validPos = false;
					}
				}
			}
			boolean right = false;
			boolean left = false;

			if(validPos){
				for(int i = 0; i < size; i++){
					if((places[i] % 8) == 0){
						left = true;
					}
					if((places[i] % 8) == 7){
						right = true;
					}
				}
			}
			if(left && right){
				validPos = false;
			}
		}
		//update our grid
		for(int i = 0; i < size; i++){
			ourGrid[places[i]] = true;
		}
		//return
		String[] ret = new String[2];
		ret[0] = placeToString(places[0]);
		ret[1] = placeToString(places[places.length-1]);
		/*for(int i = 0; i < size; i++){
			System.out.println(""+places[i]);
		}
		System.out.println(ret[0]+" "+ret[1]);*/

		return ret;
	}

	String placeToString(int place){
			int row = place % 8;
			int col = place / 8;
			char colC = (char) (col + 65);
			String result = String.valueOf(colC) + Integer.toString(row);
		return result;
	}



	void makeMove() {
		for(int i = 0; i < 8; i++) {
			for(int j = 0; j < 8; j++) {
				if (this.grid[i][j] == -1) {
					String wasHitSunkOrMiss = placeMove(this.letters[i] + String.valueOf(j));

					if (wasHitSunkOrMiss.equals("Hits") || 
							wasHitSunkOrMiss.equals("Sunk")) {
						this.grid[i][j] = 1;
					} else {
						this.grid[i][j] = 0;			
					}
					return;
				}
			}
		}
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
		else if (data.contains("Sunk")) return "Sunk";
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

