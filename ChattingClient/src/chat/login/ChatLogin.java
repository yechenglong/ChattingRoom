package chat.login;

import java.awt.Color;
import java.awt.Font;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import chat.resign.ChatResign;
import chat.client.ChatClientUI;
import chat.function.ServerChatBean;
import chat.util.ServerChatUtil;

public class ChatLogin extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField textField;
	private JPasswordField passwordField;
	private ObjectOutputStream oos;
	private Socket client;
	public String u_name;
	public String u_pwd;
	public JButton btnNewButton = new JButton();
	public JLabel lblNewLabel = new JLabel();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					// 启动登陆界面
					ChatLogin frame = new ChatLogin();
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
	public ChatLogin() {
		setTitle("Landing chattingroom\n");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(350, 250, 450, 300);
		contentPane = new JPanel() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.drawImage(new ImageIcon(
						"images\\\u767B\u9646\u754C\u9762.jpg").getImage(), 0,
						0, getWidth(), getHeight(), null);
			}
		};
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		textField = new JTextField();
		textField.setBounds(128, 109, 104, 21);
		textField.setOpaque(false);
		contentPane.add(textField);
		textField.setColumns(10);

		passwordField = new JPasswordField();
		passwordField.setForeground(Color.BLACK);
		passwordField.setEchoChar('*');
		passwordField.setOpaque(false);
		passwordField.setBounds(128, 153, 104, 21);
		contentPane.add(passwordField);

		btnNewButton.setFont(new Font("宋体", Font.BOLD, 12));
		btnNewButton.setText("\u767B\u5F55");
		btnNewButton.setBounds(246, 216, 61, 25);
		getRootPane().setDefaultButton(btnNewButton);
		contentPane.add(btnNewButton);

		final JButton btnNewButton_1 = new JButton();
		btnNewButton_1.setFont(new Font("宋体", Font.BOLD, 12));
		btnNewButton_1.setText("\u6CE8\u518C");
		btnNewButton_1.setBounds(316, 216, 61, 25);
		contentPane.add(btnNewButton_1);

		// 提示信息
		lblNewLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
		lblNewLabel.setBounds(20, 220, 212, 21);
		lblNewLabel.setForeground(Color.red);
		getContentPane().add(lblNewLabel);
		
		JLabel label = new JLabel("\u6635\u79F0\uFF1A");
		label.setFont(new Font("幼圆", Font.BOLD, 16));
		label.setBounds(83, 109, 57, 21);
		contentPane.add(label);
		
		JLabel label_1 = new JLabel("\u5BC6\u7801\uFF1A");
		label_1.setFont(new Font("幼圆", Font.BOLD, 16));
		label_1.setBounds(83, 150, 57, 26);
		contentPane.add(label_1);

		// 监听登陆按钮
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				u_name = textField.getText();
				u_pwd = new String(passwordField.getPassword());
				if (u_name.length() != 0) {					
					try {
						lblNewLabel.setText("正在查询！");
						
						//请求登录
						client = new Socket("127.0.0.1", 8520);
						new loginThread(client).start();
						
						ServerChatBean clientBean = new ServerChatBean();
						clientBean.setType(6);
						clientBean.setName(u_name);
						String time = ServerChatUtil.getTimer();
						clientBean.setTimer(time);
						clientBean.setInfo("");
						clientBean.setPassword(u_pwd);
						
						try {
							oos = new ObjectOutputStream(client.getOutputStream());
							oos.writeObject(clientBean);
							oos.flush();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
					} catch (UnknownHostException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					} catch (IOException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
				} else {
					lblNewLabel.setText("您输入的昵称不存在！");
					textField.setText("");
					passwordField.setText("");
					textField.requestFocus();
				}
			}
		});

		//注册按钮监听
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnNewButton_1.setEnabled(false);
				ChatResign frame = new ChatResign();
				frame.setVisible(true);// 显示注册界面
				setVisible(false);// 隐藏掉登陆界面
			}
		});
	}

	protected void errorTip(String str) {
		// TODO Auto-generated method stub
		JOptionPane.showMessageDialog(contentPane, str, "Error Message",
				JOptionPane.ERROR_MESSAGE);
		textField.setText("");
		passwordField.setText("");
		textField.requestFocus();
	}
	
	class loginThread extends Thread{
		
		private Socket client;
		private ServerChatBean bean;
		private ObjectInputStream ois;
		
		public loginThread(Socket client) {
			this.client = client;
		}
		
		public void run() {
			try {
				while(true) {
					ois = new ObjectInputStream(client.getInputStream());
					bean = (ServerChatBean) ois.readObject();
					
					switch (bean.getType()) {
						case -2: { 
							return;//返回
							}
						default: {
							break;
						}
					}
					
					// 判断用户名是否能够登录
					if (bean.getInfo().equals("用户名已注册且密码正确!")) {
						
						//发送返回请求
						ServerChatBean clientBean = new ServerChatBean();
						clientBean.setType(-2);
						String time = ServerChatUtil.getTimer();
						clientBean.setTimer(time);
						
						try {
							oos = new ObjectOutputStream(client.getOutputStream());
							oos.writeObject(clientBean);
							oos.flush();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
						//这里创建新的Socket连接，因为请求登录的连接在查询成功返回后会关闭
						Socket client = new Socket("127.0.0.1", 8520);
						btnNewButton.setEnabled(false);
						ChatClientUI frame = new ChatClientUI(u_name,
								client);
						frame.setVisible(true);// 显示聊天界面
						setVisible(false);// 隐藏掉登陆界面
						
					}
					if(bean.getInfo().equals("用户名已注册但密码不正确!")){
						//发送返回请求
						ServerChatBean clientBean = new ServerChatBean();
						clientBean.setType(-2);
						String time = ServerChatUtil.getTimer();
						clientBean.setTimer(time);
						
						try {
							oos = new ObjectOutputStream(client.getOutputStream());
							oos.writeObject(clientBean);
							oos.flush();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
						lblNewLabel.setText("用户名已注册但密码不正确!");
						textField.setText("");
						passwordField.setText("");
						textField.requestFocus();																
					}
					if(bean.getInfo().equals("用户名尚未注册!")){
						//发送返回请求
						ServerChatBean clientBean = new ServerChatBean();
						clientBean.setType(-2);
						String time = ServerChatUtil.getTimer();               
						clientBean.setTimer(time);
						
						try {
							oos = new ObjectOutputStream(client.getOutputStream());
							oos.writeObject(clientBean);
							oos.flush();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
						lblNewLabel.setText("用户名尚未注册!");
						textField.setText("");
						passwordField.setText("");
						textField.requestFocus();																
					}
				}
					
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			finally {
				close();//发送返回请求后关闭
			}
		}
			
		private void close() {
			if (oos != null) {
				try {
					oos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (ois != null) {
				try {
					ois.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (client != null) {
				try {
					client.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}				
	}
	
}
