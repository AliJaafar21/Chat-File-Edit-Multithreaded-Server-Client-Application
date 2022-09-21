import java.awt.Color;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.SwingWorker;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.JScrollPane;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

@SuppressWarnings("serial")
public class serverWindow extends JFrame {

	private JPanel contentPane;
	private chatServer ChatServer;
	JTextArea console;
	public StyledDocument serverStatus_StyledDocument;
	public Style normalStyle;
	
	public static void main(String[] args) throws ClassNotFoundException {
		Class.forName("org.sqlite.JDBC");
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					serverWindow frame = new serverWindow();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public serverWindow() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				if(e.getID() == WindowEvent.WINDOW_CLOSING) {
					chatServer.serverTerminated();
				}
			}
		});
		setTitle("Server");
		setResizable(false);
		setAlwaysOnTop(true);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 515, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JButton Run_Btn = new JButton("Run Server");
		Run_Btn.setBounds(10, 121, 147, 23);
		contentPane.add(Run_Btn);
		
		JButton Stop_btn = new JButton("Terminate Server");
		Stop_btn.setBounds(10, 178, 147, 23);
		contentPane.add(Stop_btn);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(177, 11, 307, 239);
		contentPane.add(scrollPane);
		
		console = new JTextArea();
		scrollPane.setViewportView(console);
		console.setEditable(false);
		
		JLabel lblNewLabel = new JLabel("SERVER STATUS:");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setFont(new Font("Simplified Arabic Fixed", Font.PLAIN, 16));
		lblNewLabel.setBounds(10, 11, 157, 53);
		contentPane.add(lblNewLabel);
		
		JTextPane serverStatus = new JTextPane();
		serverStatus.setEditable(false);
		serverStatus.setBounds(43, 70, 78, 23);
		serverStatus.setFont(new Font("Calibri", Font.BOLD, 12));

		serverStatus_StyledDocument = serverStatus.getStyledDocument();
		normalStyle =serverStatus.addStyle("normalStyle", null);
		StyleConstants.setForeground(normalStyle, Color.red);
		SimpleAttributeSet center = new SimpleAttributeSet();
		StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
		serverStatus_StyledDocument.setParagraphAttributes(0, serverStatus_StyledDocument.getLength(), center, false);
		try {
			serverStatus_StyledDocument.insertString(serverStatus_StyledDocument.getLength(), "Not Running", normalStyle);
		} catch (BadLocationException e1) { e1.printStackTrace();}
		contentPane.add(serverStatus);
		
		// clicking the button RunServer will instantiate a chatServer
		Run_Btn.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				Run_Btn.setEnabled(false);
				StyleConstants.setForeground(normalStyle, Color.green);
				serverStatus.setText("");
				DBM.createNewDatabase();
				try { 
					serverStatus_StyledDocument.insertString(serverStatus_StyledDocument.getLength(), "Running", normalStyle);
				} catch (BadLocationException e1) { e1.printStackTrace();}
				
				SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
					@Override
					protected Void doInBackground() throws Exception {
						ChatServer = new chatServer(getServerWindow());
						return null;
					}
				};
				worker.execute();
			}
		});
	
		Stop_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chatServer.serverTerminated();
				System.exit(0);
			}
		});
	}
	
	private serverWindow getServerWindow() {
		return this;
	}
}
