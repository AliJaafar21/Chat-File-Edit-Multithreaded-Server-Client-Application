import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

@SuppressWarnings("serial")
public class welcomeWindow extends JFrame {
	
	chatClient chatClient;
	private JPanel contentPane;
	JTextField textField;
	JButton joinTheServer_btn;
	
	public welcomeWindow(chatClient chatClient) {
		setAlwaysOnTop(true);
		setResizable(false);
		this.chatClient = chatClient;

		setTitle("Welcome");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 452, 231);
		contentPane = new JPanel();
		setContentPane(contentPane);
		contentPane.setLayout(null);

		joinTheServer_btn = new JButton("Join the Server");
		joinTheServer_btn.setBounds(163, 118, 120, 38);
		contentPane.add(joinTheServer_btn);

		textField = new JTextField();
		textField.setBounds(90, 67, 257, 40);
		contentPane.add(textField);
		textField.setColumns(1);

		JLabel lblNewLabel = new JLabel("Enter Your Name: ");
		lblNewLabel.setForeground(Color.BLUE);
		lblNewLabel.setBounds(90, 27, 141, 29);
		contentPane.add(lblNewLabel);
		
		// clicking Join the Server makes you join the server
		joinTheServer_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chatClient.clientName = textField.getText();
				textField.setText("");
				try {
					chatClient.sendName();
				} catch (Exception e1) { e1.printStackTrace();}
			}
		});
		
		// pressing enter makes you join the server
		textField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					chatClient.clientName = textField.getText();
					textField.setText("");
					try {
						chatClient.sendName();
					} catch (Exception e1) { e1.printStackTrace();}
				}
			}
		});
	}
	
	// Connection Error Message
	public void displayConnectionErrorMessage() {
		JOptionPane.showMessageDialog(contentPane, "Couldn't Connect to the server ! \n Make sure the server is running !", "Connection Error", JOptionPane.ERROR_MESSAGE);
	}
	
	// Blank Name Error Message
	public void displayBlankNameErrorMessage() {
		JOptionPane.showMessageDialog(contentPane, "Name Can't be Blank!", "Error", JOptionPane.ERROR_MESSAGE);
	}
	
	// Name Already Taken Error Message
	public void displayNameAlreadyTakenErrorMessage() {
		JOptionPane.showMessageDialog(contentPane, "Name Already Taken", "Error", JOptionPane.ERROR_MESSAGE);
	}

}
