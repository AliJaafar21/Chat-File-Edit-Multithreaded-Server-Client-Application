import java.awt.Color;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JTextPane;
import java.awt.Font;

@SuppressWarnings("serial")
public class chatWindow extends JFrame {
	
	chatClient chatClient;
	public static Color[] colors = new Color[] {Color.black, Color.blue, Color.green, Color.magenta, Color.orange, Color.pink};
	private JPanel contentPane;
	JButton Send_btn;
	public JTextField selfMessage_textField;
	public JTextPane messages_textPane;
	public StyledDocument messages_StyledDoc;
	public Style normalStyle;
	public Style serverStyle;
	private JScrollPane scrollPane;
	
	public chatWindow(chatClient chatClient) {
		this.chatClient = chatClient;
		setAlwaysOnTop(true);
		setResizable(false);
		setTitle("Chat - " + chatClient.clientName);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		selfMessage_textField = new JTextField();
		selfMessage_textField.setBounds(5, 220, 327, 36);
		contentPane.add(selfMessage_textField);

		Send_btn = new JButton("Send");
		Send_btn.setBounds(342, 227, 89, 23);
		contentPane.add(Send_btn);
		
		scrollPane = new JScrollPane();
		scrollPane.setBounds(-3, 0, 434, 209);
		contentPane.add(scrollPane);
		
		messages_textPane = new JTextPane();
		messages_textPane.setFont(new Font("Consolas", Font.BOLD, 14));
		scrollPane.setViewportView(messages_textPane);
		messages_textPane.setEditable(false);
		
		// creating two different styles
		messages_StyledDoc = messages_textPane.getStyledDocument();
		normalStyle = messages_textPane.addStyle("normalStyle", null);
		serverStyle = messages_textPane.addStyle("serverStyle", null);
		StyleConstants.setForeground(serverStyle, Color.red);
		
		// when Send is clicked, message is sent
		Send_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String message = selfMessage_textField.getText();
				if (!message.isBlank()) {
					// mention in which color the message should be displayed
					message = chatClient.ColorIndex + message;
					// send the message to the server
					chatClient.outToServer.println(message);
				}
				// clear the text field where message was written
				selfMessage_textField.setText("");
			}
		});
		
		// when Enter is pressed, message is sent
		selfMessage_textField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					String message = selfMessage_textField.getText();
					if (!message.isBlank()) {
						// mention in which color the message should be displayed
						message = chatClient.ColorIndex + message;
						// send the message to the server
						chatClient.outToServer.println(message);
					}
					// clear the text field where message was written
					selfMessage_textField.setText("");
				}
			}
		});

	}

	public void setColor(int colorIndex) {
		StyleConstants.setForeground(normalStyle, colors[colorIndex]);
	}

	public static int getColorsLength() {
		return colors.length;
	}

}
