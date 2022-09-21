import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import javax.swing.JTextPane;
import javax.swing.JScrollPane;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

@SuppressWarnings("serial")
public class ContentsOfTextFile extends JFrame {
	public JPanel contentPane;
	JTextPane textPane;
	FilesWindow filesWindow;
	int fileID;
	String fileContent;

	public ContentsOfTextFile(FilesWindow filesWindow, int fileID, String fileName, String fileContent) {
		
		this.filesWindow = filesWindow;
		this.fileID = fileID;
		this.fileContent = fileContent;
		setResizable(false);
		setAlwaysOnTop(true);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 600, 400);
		setTitle(fileName);

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JButton exit_btn = new JButton("Exit");
		exit_btn.setBounds(10, 327, 89, 23);
		contentPane.add(exit_btn);
		
		JButton save_btn = new JButton("Save");
		save_btn.setBounds(485, 327, 89, 23);
		contentPane.add(save_btn);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(0, 0, 584, 316);
		contentPane.add(scrollPane);
		
		textPane = new JTextPane();
		scrollPane.setViewportView(textPane);
		textPane.setText(fileContent);
		
		exit_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				filesWindow.ChatClient.outToServer.println("JAKS::FILESDATABASE::REQUEST::CLOSE" + fileID);
				dispose();
			}
		});
		
		save_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				filesWindow.dbm.editFile(fileID, textPane.getText());
			
			}
		});
	}
}
