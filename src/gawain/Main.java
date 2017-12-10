package gawain;

import java.util.List;
import java.util.Random;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Main {

	public static void main(String[] args) throws IOException {
		final String server = args.length >= 1 ? args[0] : "127.0.0.1";
		final int port = args.length >= 2 ? Integer.valueOf(args[1]) : 6789;
		final String name = args.length >= 1 ? args[0] : "java-test-client";
        Socket socket = null;
        PrintWriter out = null;
        Scanner in = null;
        System.out.println("UMPGGK2017 Java Test Client");
        try {
            socket = new Socket(server, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new Scanner(new InputStreamReader(socket.getInputStream()));
            out.print("100 " + name + "\r\n");
            out.flush();
            String color = "";
            int[][] state = new int[1][1];
            Random random = new Random();
            new Nogo();
            while (true) {
            	String msg = in.nextLine();
            	System.out.println(msg);
                String[] parts = msg.split(" ");
                if (parts[0].equals("200")) {
                	state = Nogo.getInitialState();
                	if (parts[1].equals("black")) {
                		color = "black";
                		List<int[]> moves = Nogo.getLegalMoves(state, color);
                		if (moves.size() > 0) {
                			int[] move = moves.get(random.nextInt(moves.size()));
        			    	state = Nogo.play(state, color, move[0], move[1]);
        			    	Nogo.print(state);
        			    	out.print("210 " + (move[1] + 1) + " " + (move[0] + 1) + "\r\n");
        			    	System.out.println("210 " + (move[1] + 1) + " " + (move[0] + 1));
        		            out.flush();
                		}
                	} else {
                		color = "white";
                	}
                }
                if (parts[0].equals("220")) {
                	state = Nogo.play(state, color.equals("black") ? "white" : "black", Integer.valueOf(parts[2]) - 1, Integer.valueOf(parts[1]) - 1);
                	Nogo.print(state);
                	List<int[]> moves = Nogo.getLegalMoves(state, color);
            		if (moves.size() > 0) {
            			int[] move = moves.get(random.nextInt(moves.size()));
    			    	state = Nogo.play(state, color, move[0], move[1]);
    			    	Nogo.print(state);
    			    	out.print("210 " + (move[1] + 1) + " " + (move[0] + 1) + "\r\n");
    			    	System.out.println("210 " + (move[1] + 1) + " " + (move[0] + 1));
    		            out.flush();
            		}
                }
            }
        } catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (socket != null) {
                socket.close();
            }            
        }
    }

}
