import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// inspired by https://cs.lmu.edu/~ray/notes/javanetexamples/#chat
public class chatServer {

	// All client names, so we can check for duplicates upon registration.
	private static ArrayList<String> takenNames = new ArrayList<>();

	// The set of all the print writers for all the clients, used for broadcast.
	private static Set<PrintWriter> writers = new HashSet<>();

	// The set of all files currently opened
	private static Set<Integer> openedFiles = new HashSet<>();
	
	// Key is file ID | Value is a string of 2 characters : first character is number of votes, second character is number of YES votes
	private static HashMap<Integer, String> deleteVotes = new HashMap<>();
	
	private static int colorIndex = 0;

	private static int fileID = 0;
	
	private static Object lock1 = new Object();

	ServerSocket serverSocket;
	serverWindow serverWindow;

	public chatServer(serverWindow serverWindow) {
		this.serverWindow = serverWindow;
		ExecutorService pool = Executors.newFixedThreadPool(500);
		try {
			serverSocket = new ServerSocket(12345);
			System.out.println("Chat Server Started!");
			serverWindow.console.append(" Chat Server Started!\n");
			System.out.println("Listening on port number " + serverSocket.getLocalPort());
			serverWindow.console.append(" Listening on port number " + serverSocket.getLocalPort() + "\n");
			while (true) {
				pool.execute(new Handler(serverWindow, serverSocket.accept()));
			}
		} catch (java.net.BindException e) {
			System.out.println(" Server Already Running !\n");
		} catch (Exception e) { e.printStackTrace();}
	}

	// Class for handling Clients
	private static class Handler implements Runnable {
		private String name;
		serverWindow serverWindow;
		private Socket clientSocket;
		private Scanner inFromClient;
		private PrintWriter outToClient;

		// Constructor
		public Handler(serverWindow serverWindow, Socket clientSocket) {
			System.out.println("New Conncetion Established !");
			serverWindow.console.append(" New Conncetion Established !\n");
			this.serverWindow = serverWindow;
			this.clientSocket = clientSocket;
		}

		@Override
		public void run() {
			try {
				inFromClient = new Scanner(clientSocket.getInputStream());
				outToClient = new PrintWriter(clientSocket.getOutputStream(), true);

				// Keep requesting a name until we get a unique one.
				while (true) {
					name = inFromClient.nextLine();
					synchronized (takenNames) {
						if (name.isBlank()) {
							outToClient.println("JAKS::BLANKNAME");
						} else if (takenNames.contains(name)) {
							outToClient.println("JAKS::NAMEALREADYTAKEN");
						} else {
							takenNames.add(name);
							System.out.println("Welcome : " + name);
							serverWindow.console.append(" " + name + " has joined.\n");
							break;
						}
					}
				}

				outToClient.println("JAKS::NAMEACCEPTED");

				// send the new client a color index that will be used to color his/her messages
				// on all screens
				outToClient.println(colorIndex);
				colorIndex += 1;
				if (colorIndex >= chatWindow.getColorsLength()) {
					colorIndex = 0;
				}

				// Now that a successful name has been chosen, add the socket's print writer to
				// the set of all writers
				// so this client can receive broadcast messages.
				// But BEFORE THAT, let everyone else know that the new person has joined!
				for (PrintWriter writer : writers) {
					writer.println("JAKS::NOTIFICATION" + name + " has joined");
				}
				writers.add(outToClient);

				// inform the new client of Active Users
				String activeUsers = "JAKS::NOTIFICATION";
				if (takenNames.size() == 1) {
					activeUsers += "Welcome, you are the first user to join the chat!";
				} else {
					activeUsers = activeUsers + "Active Users:" + takenNames.toString();
					activeUsers = activeUsers.replace('[', '\0');
					// remove (,) (the name of the user) and (])
					activeUsers = activeUsers.substring(0, activeUsers.length() - name.length() - 3);
				}
				outToClient.println(activeUsers);
				System.out.println(activeUsers);

				// Accept messages from this client and broadcast them.
				while (true) {
					String input = inFromClient.nextLine();
					serverWindow.console.append(input + '\n');
					
					// FILES Related Messages
					if (input.startsWith("JAKS::FILESDATABASE::REQUEST")) {
						
						// Open File Request
						if (input.startsWith("JAKS::FILESDATABASE::REQUEST::OPEN")) {
							synchronized (openedFiles) {
								int requestedFileID = Integer.parseInt(input.substring(34, input.indexOf(',')));
								System.out.println(requestedFileID);
								String fileName = input.substring(input.indexOf(',') + 1);
								if (openedFiles.contains(requestedFileID)) {
									outToClient.println("JAKS::FILESDATABASE::RESPONSE::FILEALREADYOPENED");
								} else {
									openedFiles.add(requestedFileID);
									outToClient.println("JAKS::FILESDATABASE::RESPONSE::OPEN" + requestedFileID + "," + fileName);
								}
							}
							
						// New File Request
						} else if (input.startsWith("JAKS::FILESDATABASE::REQUEST::NEW")) {
							synchronized (lock1) {
								fileID++;
								System.out.println(fileID);
								outToClient.println("JAKS::FILESDATABASE::RESPONSE::NEW" + fileID + "," + input.substring(33));
							}
						
						// Upload File Request
						} else if (input.startsWith("JAKS::FILESDATABASE::REQUEST::UPLOAD")) {
							synchronized (lock1) {
								fileID++;
								System.out.println("SENDING RESPONSE");
								outToClient.println("JAKS::FILESDATABASE::RESPONSE::UPLOAD" + fileID + "<" + input.substring(36) + ">");
							}
						
						// Close File Request
						} else if (input.startsWith("JAKS::FILESDATABASE::REQUEST::CLOSE")) {
							synchronized (openedFiles) {
								int ID_of_File_to_Close = Integer.parseInt(input.substring(35));
								openedFiles.remove(ID_of_File_to_Close);
							}
						
						// Delete File Request
						} else if (input.startsWith("JAKS::FILESDATABASE::REQUEST::DELETE")) {
							int ID_of_File_to_Delete = Integer.parseInt(input.substring(36));
							if (takenNames.size() > 1) {
								if (!deleteVotes.containsKey(ID_of_File_to_Delete)) {
									deleteVotes.put(ID_of_File_to_Delete, "1" + "," + "1");
									for (PrintWriter writer : writers) {
										writer.println("JAKS::FILESDATABASE::RESPONSE::VOTEDELETE" + "<" + name + ">" + "<" + ID_of_File_to_Delete + ">");
									}
								} else {
									outToClient.println("JAKS::FILESDATABASE::RESPONSE::ALREADYMARKEDFORDELETION" + ID_of_File_to_Delete);
								}
							} else {
								// 1 user only so no voting
								outToClient.println("JAKS::FILESDATABASE::RESPONSE::CONFIRMDELETE" + ID_of_File_to_Delete);
								deleteVotes.remove(ID_of_File_to_Delete);
							}
						
						// Yes Vote Received
						} else if (input.startsWith("JAKS::FILESDATABASE::REQUEST::VOTEYES")) {
							synchronized (deleteVotes) {
								int ID = Integer.parseInt(input.substring(37));
								String s = deleteVotes.get(ID);
								int votesCount = Integer.parseInt(String.valueOf(s.charAt(0))) + 1;
								int YesCount = Integer.parseInt(String.valueOf(s.charAt(2))) + 1;
								s = votesCount + "," + YesCount;
								if (votesCount == YesCount && votesCount == takenNames.size()) {
										outToClient.println("JAKS::FILESDATABASE::RESPONSE::CONFIRMDELETE" + ID);
									deleteVotes.remove(ID);
								} else if (votesCount == takenNames.size()) {
									deleteVotes.remove(ID);
								} else {
									s = votesCount + "," + YesCount;
									deleteVotes.replace(ID, s);
								}
							}
						
						// No Vote Received
						} else if (input.startsWith("JAKS::FILESDATABASE::REQUEST::VOTENO")) {
							synchronized (deleteVotes) {
								int ID = Integer.parseInt(input.substring(36));
								String s = deleteVotes.get(ID);
								int votesCount = Integer.parseInt(String.valueOf(s.charAt(0))) + 1;
								s = votesCount + "," + s.charAt(2);
								if (votesCount == takenNames.size()) {
									deleteVotes.remove(ID);
								} else {
									deleteVotes.replace(ID, s);
								}
							}
						} else if (input.startsWith("JAKS::FILESDATABASE::REQUEST::REFRESH")) {
							for (PrintWriter writer : writers) {
								writer.println("JAKS::FILESDATABASE::RESPONSE::REFRESH");
							}
						}
					
					
					// chat messages
					} else {
						System.out.println(name + ": " + input);
						serverWindow.console.append(" " + name + ": " + input.substring(1) + "\n");
						int colorIndex = Integer.parseInt(input.substring(0, 1));
						input = input.substring(1);
						for (PrintWriter writer : writers) {
							writer.println(colorIndex + name + ": " + input);
						}
					}
				}
				
			// user left
			} catch (java.util.NoSuchElementException e) {
				writers.remove(outToClient);
				takenNames.remove(name);
				System.out.println(name + " has left");
				serverWindow.console.append(" " + name + " has left\n");
				for (PrintWriter writer : writers) {
					writer.println("JAKS::NOTIFICATION" + name + " has left");
				}
			} catch (Exception e) {
				System.out.println(e);
			}

		}

	}

	public static void serverTerminated() {
		for (PrintWriter writer : writers) {
			writer.println("JAKS::NOTIFICATION" + "Server has terminated. Please close the chat window.");
		}
	}
}
