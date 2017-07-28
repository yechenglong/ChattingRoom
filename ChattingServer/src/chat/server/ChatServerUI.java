package chat.server;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import javax.swing.JTextArea;

import chat.util.ServerChatUtil;

public class ChatServerUI extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	public JTextArea textArea;
	public JTextArea textArea_1;
	public ChatServer chatServer;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ChatServerUI frame = new ChatServerUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public ChatServerUI() {
		super("The backstage server");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel(){
		private static final long serialVersionUID = 1L;

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.drawImage(new ImageIcon(
					"images\\后台.jpg").getImage(), 0,
					0, getWidth(), getHeight(), null);
		}
	};
		
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JButton button = new JButton("\u542F\u52A8\u670D\u52A1");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//启动后台服务
				chatServer = new ChatServer(ChatServerUI.this);
			}
		});
		button.setBounds(337, 170, 93, 23);
		contentPane.add(button);
		
		JButton button_1 = new JButton("\u5173\u95ED\u670D\u52A1");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chatServer.stageSend("后台服务器暂时关闭，退出后无法重新登录！");
				//关闭后台服务
				chatServer.closeServer();
				chatServer.println("关闭服务器成功：端口8520");
				textArea.selectAll();
			}
		});
		button_1.setBounds(337, 203, 93, 23);
		contentPane.add(button_1);
		
		JButton button_2 = new JButton("\u53D1\u9001\u4FE1\u606F");
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String info = textArea_1.getText();
				String time = ServerChatUtil.getTimer();
				if (info.equals("")) {
					JOptionPane.showMessageDialog(getContentPane(), "不能发送空信息");
					return;
				}

				chatServer.stageSend(info);
				// 自己发的内容也要现实在自己的屏幕上面
				textArea.append(time+" server:\r\n" + info+ "\r\n");
				textArea.selectAll();
				textArea_1.setText(null);
				textArea_1.requestFocus();
			}
		});
		button_2.setBounds(337, 236, 93, 23);
		contentPane.add(button_2);
		
		// 连接信息显示区域
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(5, 5, 335, 158);
		getContentPane().add(scrollPane);

		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setLineWrap(true);//激活自动换行功能 
		textArea.setWrapStyleWord(true);//激活断行不断字功能 
		textArea.setFont(new Font("sdf", Font.BOLD, 13));
		scrollPane.setViewportView(textArea);

		// 打字区域
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(5, 175, 325, 85);
		getContentPane().add(scrollPane_1);

		textArea_1 = new JTextArea();
		textArea_1.setLineWrap(true);//激活自动换行功能 
		textArea_1.setWrapStyleWord(true);//激活断行不断字功能 
		textArea_1.setFont(new Font("sdf", Font.BOLD, 13));
		scrollPane_1.setViewportView(textArea_1);
		
		this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                int a = JOptionPane.showConfirmDialog(null, "确定关闭吗？", "温馨提示",
                        JOptionPane.YES_NO_OPTION);
                if (a == 1) {
                    chatServer.closeServer();
                    System.exit(0); // 关闭
                }
            }
        });
	}
}

