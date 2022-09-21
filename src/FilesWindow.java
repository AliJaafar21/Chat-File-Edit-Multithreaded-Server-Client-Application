import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.event.ListSelectionEvent;
import javax.swing.JScrollPane;

@SuppressWarnings("serial")
public class FilesWindow extends JFrame {
	private JPanel contentPane;
	chatClient ChatClient;
	JList<String> files_list;
	DefaultListModel<String> listModel;
	private JTextField nameOfTheNewFile;
	JButton open_btn;
	JButton delete_btn;
	JButton upload_btn;
	JButton download_btn;
	JButton refresh_btn;
	JButton new_btn;
	DBM dbm;
	ArrayList<String> filesNames;
	ArrayList<Integer> filesIDs;
	private JScrollPane scrollPane;


	public FilesWindow(chatClient ChatClient) throws SQLException, ClassNotFoundException {
		
		Class.forName("org.sqlite.JDBC");
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setAlwaysOnTop(true);
		setTitle("Files - " + ChatClient.clientName);
		setBounds(100, 100, 384, 322);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		open_btn = new JButton("Open");
		open_btn.setEnabled(false);
		open_btn.setBounds(254, 11, 89, 23);
		contentPane.add(open_btn);
		
		delete_btn = new JButton("Delete");
		delete_btn.setEnabled(false);
		delete_btn.setBounds(254, 45, 89, 23);
		contentPane.add(delete_btn);
		
		upload_btn = new JButton("Upload");
		upload_btn.setBounds(254, 79, 89, 23);
		contentPane.add(upload_btn);
		
		download_btn = new JButton("Download");
		download_btn.setEnabled(false);
		download_btn.setBounds(254, 145, 104, 31);
		contentPane.add(download_btn);
		
		refresh_btn = new JButton("Refresh");
		refresh_btn.setBounds(254, 187, 89, 23);
		contentPane.add(refresh_btn);
		
		new_btn = new JButton("New");
		new_btn.setEnabled(false);
		new_btn.setBounds(209, 249, 89, 23);
		contentPane.add(new_btn);
		
		nameOfTheNewFile = new JTextField();
		nameOfTheNewFile.setBounds(10, 250, 171, 20);
		nameOfTheNewFile.setText("New File Name");
		nameOfTheNewFile.setHorizontalAlignment(SwingConstants.CENTER);
		contentPane.add(nameOfTheNewFile);
		nameOfTheNewFile.setColumns(10);
	
		listModel = new DefaultListModel<>();
		
		scrollPane = new JScrollPane();
		scrollPane.setBounds(0, 0, 228, 218);
		contentPane.add(scrollPane);
		files_list = new JList<>(listModel);
		scrollPane.setViewportView(files_list);
		
		files_list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				open_btn.setEnabled(true);
				delete_btn.setEnabled(true);
				download_btn.setEnabled(true);
				
			}
		});
		
		this.ChatClient = ChatClient;
		dbm = new DBM();
		
		nameOfTheNewFile.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				nameOfTheNewFile.setText("");
				new_btn.setEnabled(true);
			}
		});
		
		open_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ChatClient.outToServer.println("JAKS::FILESDATABASE::REQUEST::OPEN" + filesIDs.get(files_list.getSelectedIndex()) + "," + files_list.getSelectedValue());
			}
		});
		
		delete_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ChatClient.outToServer.println("JAKS::FILESDATABASE::REQUEST::DELETE" + filesIDs.get(files_list.getSelectedIndex()));
			}
		});
		
		upload_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				FileNameExtensionFilter filter = new FileNameExtensionFilter("TEXT FILES", "txt", "text");
				fileChooser.setAcceptAllFileFilterUsed(false);
				fileChooser.setFileFilter(filter);
				int response = fileChooser.showOpenDialog(rootPane);
				if (response == JFileChooser.APPROVE_OPTION) {
					String path = fileChooser.getSelectedFile().getAbsolutePath();
					System.out.println(path);
					ChatClient.outToServer.println("JAKS::FILESDATABASE::REQUEST::UPLOAD" + path);
				}
			}
		});

		download_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileName = files_list.getSelectedValue();
				int fileID = filesIDs.get(files_list.getSelectedIndex());
				String fileContent = "";
				try {
					fileContent = dbm.getContent(fileID);
				} catch (SQLException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				JFileChooser directoryChooser = new JFileChooser();
				directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int result = directoryChooser.showOpenDialog(rootPane);
				 if (result == JFileChooser.APPROVE_OPTION) {
					 System.out.println(directoryChooser.getSelectedFile());
					 File file = new File(directoryChooser.getSelectedFile() + "\\" + fileName + ".txt");
					 System.out.println(file.getAbsolutePath());
					  try {
						FileWriter fileWriter = new FileWriter(file);
						fileWriter.write(fileContent);
						fileWriter.close();
					} catch (IOException e1) { e1.printStackTrace();}    
				}
			}
		});
		
		refresh_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					updateFilesList();
				} catch (SQLException e1) { e1.printStackTrace();
				}
			}
		});
		
		new_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new_btn.setEnabled(false);
				ChatClient.outToServer.println("JAKS::FILESDATABASE::REQUEST::NEW" + nameOfTheNewFile.getText());
				nameOfTheNewFile.setText("New File Name");
			}	
		});
		
		updateFilesList();	
	}
	
	public void updateFilesList() throws SQLException {
				files_list.clearSelection();
				listModel.removeAllElements();
				open_btn.setEnabled(false);
				delete_btn.setEnabled(false);
				download_btn.setEnabled(false);
				filesNames = dbm.getFileNames();
				filesIDs = dbm.getFileIds();
				
			
				for (String fileName : filesNames) {
					System.out.println(fileName);
					listModel.addElement(fileName);
					files_list.ensureIndexIsVisible(listModel.getSize());
				}
	}

	public void VoteDelete(String user, int fileID) {
		String fileName = filesNames.get(filesIDs.indexOf(fileID));
		int response = JOptionPane.showConfirmDialog(contentPane, user + " wants to delete the file " + fileName, "Delete Vote", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (response == JOptionPane.YES_OPTION) {
			ChatClient.outToServer.println("JAKS::FILESDATABASE::REQUEST::VOTEYES" + fileID);
		}
		else {
			ChatClient.outToServer.println("JAKS::FILESDATABASE::REQUEST::VOTENO" + fileID);
		}
	}

	public void displayAlreadyMarkedForDeletionNotification(int fileID) {
		String fileName = filesNames.get(filesIDs.indexOf(fileID));
		JOptionPane.showMessageDialog(contentPane, "File " + fileName + " already marked for deletion!");
	}

	public void displayContent(int fileID, String fileName, String content) {
		ContentsOfTextFile window = new ContentsOfTextFile(this, fileID, fileName, content);
		window.setVisible(true);
	}

	public void displayFileAlreadyOpenedNotification() {
		JOptionPane.showMessageDialog(contentPane, "File already open !");
	}
}