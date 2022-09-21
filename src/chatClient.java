import java.io.File;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Scanner;
import javax.swing.SwingWorker;

public class chatClient {
	Socket clientSocket;
	Scanner inFromServer;
	PrintWriter outToServer;
	String clientName;
	int ColorIndex;
	welcomeWindow welcome;
	chatWindow chat;
	FilesWindow filesWindow;

	// it all starts here ! now go to the constructor of ChatClient
	public static void main(String[] args) {
		chatClient client = new chatClient();
	}
	
	/**
	 * ChatClient Constructor
	 * 	Creates welcome window and displays it
	 * 	Connects to the Server
	 */
	public chatClient() {
		// create and display the welcome window
		// pass this (chatClient Object) to the constructor of chatWindow to prevent the use of static member variables and methods
		welcome = new welcomeWindow(this);
		// centers the window
		welcome.setLocationRelativeTo(null);
		welcome.setVisible(true);
		try {
			clientSocket = new Socket("localhost", 12345);
			inFromServer = new Scanner(clientSocket.getInputStream());
			outToServer = new PrintWriter(clientSocket.getOutputStream(), true);
		} catch (Exception e) {
			// Couldn't Connect to Server
			welcome.displayConnectionErrorMessage();
			System.exit(0);
		}
		// Now nothing happens until the user presses join the server which calls sendName()
		// go to welcomeWindow.java 
	}
	
	/**
	 * This method is called from welcome (welcomeWindow object) when user presses "Join the Server" button
	 * registers user name at the server
	 * displays error if name if blank
	 * displays error if name is already taken
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public void sendName() throws SQLException, ClassNotFoundException {
		clientName = clientName.replaceAll("\\s", ""); // remove spaces
		outToServer.println(clientName);
		String serverResponse = inFromServer.nextLine();
		if (serverResponse.equals("JAKS::BLANKNAME")) {
			welcome.displayBlankNameErrorMessage();
		} else if (serverResponse.equals("JAKS::NAMEALREADYTAKEN")) {
			welcome.displayNameAlreadyTakenErrorMessage();
		} else if (serverResponse.equals("JAKS::NAMEACCEPTED")) {
			// user's name accepted! Get rid of welcome window
			welcome.dispose(); 
			ColorIndex = Integer.parseInt(inFromServer.nextLine());
			//System.out.println(ColorIndex);
			// Now user can chat !
			this.ON();
		}
	}

	private void ON() throws SQLException, ClassNotFoundException {
		chat = new chatWindow(this);
		filesWindow = new FilesWindow(this);
		chat.setVisible(true);
		filesWindow.setVisible(true);

		// if we don't deal with updating GUI in another thread with SwingWorker the GUI will halt
		// inspired by https://www.javacodegeeks.com/2012/12/multi-threading-in-java-swing-with-swingworker.html
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				while (inFromServer.hasNextLine()) {
					String line = inFromServer.nextLine();
					System.out.println(line);
					
					// special message from server displayed in red
					if (line.startsWith("JAKS::NOTIFICATION")) {
						chat.messages_StyledDoc.insertString(chat.messages_StyledDoc.getLength(), " " + line.substring(18) + '\n', chat.serverStyle);
					} 
					
					// Files Database Related Message
					else if (line.startsWith("JAKS::FILESDATABASE::RESPONSE")) {
						
						// Open File Response
						if (line.startsWith("JAKS::FILESDATABASE::RESPONSE::OPEN")) {
							// got an ack from server to open file
							int fileID = Integer.parseInt(line.substring(35, line.indexOf(',')));
							String content = filesWindow.dbm.getContent(fileID);
							System.out.println(content);
							String fileName = line.substring(line.indexOf(",") + 1);
							filesWindow.displayContent(fileID, fileName, content);
							
						// New File Response
						} else if (line.startsWith("JAKS::FILESDATABASE::RESPONSE::NEW")) {

							filesWindow.dbm.newFile(Integer.parseInt(line.substring(34, line.indexOf(","))), line.substring(line.indexOf(",") + 1));
							filesWindow.ChatClient.outToServer.println("JAKS::FILESDATABASE::REQUEST::REFRESH");
						
						// Upload File Response
						} else if (line.startsWith("JAKS::FILESDATABASE::RESPONSE::UPLOAD")) {
							int fileId = Integer.parseInt(line.substring(37, line.indexOf('<')));
							String path = line.substring(line.indexOf('<') + 1, line.indexOf('>'));
							String fileName = path.substring(path.lastIndexOf('\\') + 1, path.lastIndexOf('.'));
							System.out.println(fileName);
							System.out.println("path " + path);
							File file = new File(path);
							Scanner scanner = new Scanner(file);
							String content = "";
							while (scanner.hasNextLine()) {
								content = content + scanner.nextLine() + '\n';
							}
							scanner.close();
							filesWindow.dbm.newFile(fileId, fileName);
							filesWindow.dbm.editFile(fileId, content);
							filesWindow.ChatClient.outToServer.println("JAKS::FILESDATABASE::REQUEST::REFRESH");
						
						// Vote for Deletion
						} else if (line.startsWith("JAKS::FILESDATABASE::RESPONSE::VOTEDELETE")) {
							int firstLessThan = line.indexOf('<');
							int firstGreaterThan = line.indexOf('>');
							String nameOfUserWhoRequestedDeletion = line.substring(firstLessThan + 1, firstGreaterThan);
							
							int secondLessThan = line.lastIndexOf('<');
							int secondGreaterThan = line.lastIndexOf('>');
							int fileId = Integer.parseInt(line.substring(secondLessThan + 1, secondGreaterThan));
							
							if (!nameOfUserWhoRequestedDeletion.equals(clientName)) {
								//System.out.println("in");
								// you are not the one who requested the deletion, you should vote for deletion
								filesWindow.VoteDelete(nameOfUserWhoRequestedDeletion, fileId);
							}
						
						// Requesting deleting a file more than once
						} else if (line.startsWith("JAKS::FILESDATABASE::RESPONSE::ALREADYMARKEDFORDELETION")) {
							filesWindow.displayAlreadyMarkedForDeletionNotification(Integer.parseInt(line.substring(55)));
						
						// all users agreed on deleting the file
						} else if (line.startsWith("JAKS::FILESDATABASE::RESPONSE::CONFIRMDELETE")) {
							int id = Integer.parseInt(line.substring(44));
							filesWindow.dbm.deleteFile(id);
							filesWindow.ChatClient.outToServer.println("JAKS::FILESDATABASE::REQUEST::REFRESH");
						}
						
						// refresh is needed to update the files list
						else if (line.startsWith("JAKS::FILESDATABASE::RESPONSE::REFRESH")) {
							System.out.println("REFERSHING!");
							filesWindow.updateFilesList();
						}
						
						// trying to open an already opened file
						else if (line.startsWith("JAKS::FILESDATABASE::RESPONSE::FILEALREADYOPENED")) {
							filesWindow.displayFileAlreadyOpenedNotification();
						}
					}
					
					// Chat Messages
					else {
						//change the color
						chat.setColor(Integer.parseInt(line.substring(0, 1)));
						
						//remove colorIndex from beginning of message
						line = line.substring(1);
						
						// when you send a message you get on your screen "You: your message ..." instead of "Your Name: your message ..."
						if (line.startsWith(clientName)) {
							line = line.replaceFirst(clientName, "You");
						}	
						chat.messages_StyledDoc.insertString(chat.messages_StyledDoc.getLength(), " " + line + '\n', chat.normalStyle);
					}
				}
				return null;
			}
		};
		worker.execute();
	}
}
