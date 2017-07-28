package chat.resign;

import java.awt.Color;
import java.awt.Font;
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
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import chat.function.ServerChatBean;
import chat.login.ChatLogin;
import chat.util.ServerChatUtil;

public class ChatResign extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField textField;
	private JPasswordField passwordField;
	private JPasswordField passwordField_1;
	private JLabel lblNewLabel;
	private JLabel label;
	private JLabel label_1;
	private JLabel label_2;
	public JButton btnNewButton_1 = new JButton();
	private ObjectOutputStream oos;
	private Socket client;
	
	public String u_name;
	public String u_pwd;
	public String u_pwd_ag;

	public ChatResign() {
		setTitle("Registerring chattingroom\n");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(350, 250, 450, 300);
		contentPane = new JPanel() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.drawImage(new ImageIcon("images\\\u6CE8\u518C\u754C\u9762.jpg").getImage(), 0,0, getWidth(), getHeight(), null);
			}
		};
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		textField = new JTextField();
		textField.setBounds(148, 42, 104, 21);
		textField.setOpaque(false);
		contentPane.add(textField);
		textField.setColumns(10);

		passwordField = new JPasswordField();
		passwordField.setEchoChar('*');
		passwordField.setOpaque(false);
		passwordField.setBounds(177, 89, 104, 21);
		contentPane.add(passwordField);

		passwordField_1 = new JPasswordField();
		passwordField_1.setBounds(177, 132, 104, 21);
		passwordField_1.setOpaque(false);
		contentPane.add(passwordField_1);

		//ע�ᰴť
		btnNewButton_1.setFont(new Font("����", Font.BOLD, 12));
		btnNewButton_1.setText("\u6CE8\u518C");
		btnNewButton_1.setBounds(327, 211, 61, 25);
		getRootPane().setDefaultButton(btnNewButton_1);
		contentPane.add(btnNewButton_1);

		//���ذ�ť
		final JButton btnNewButton = new JButton("\u8FD4\u56DE");
		btnNewButton.setFont(new Font("����", Font.BOLD, 12));
		btnNewButton.setBounds(250, 211, 61, 25);
		contentPane.add(btnNewButton);

		//��ʾ��Ϣ
		lblNewLabel = new JLabel();
		lblNewLabel.setBounds(55, 218, 185, 20);
		lblNewLabel.setForeground(Color.red);
		contentPane.add(lblNewLabel);
		
		label = new JLabel("\u6635\u79F0\uFF1A");
		label.setFont(new Font("��Բ", Font.BOLD, 16));
		label.setBounds(104, 43, 51, 19);
		contentPane.add(label);
		
		label_1 = new JLabel("\u7528\u6237\u5BC6\u7801\uFF1A");
		label_1.setFont(new Font("��Բ", Font.BOLD, 16));
		label_1.setBounds(95, 90, 85, 19);
		contentPane.add(label_1);
		
		label_2 = new JLabel("\u786E\u8BA4\u5BC6\u7801\uFF1A");
		label_2.setFont(new Font("��Բ", Font.BOLD, 16));
		label_2.setBounds(95, 133, 85, 19);
		contentPane.add(label_2);
		
		//���ذ�ť����
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (client != null) {
					//���ͷ������� 
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
					
				//	reg.interrupt();
				}
				btnNewButton.setEnabled(false);
				//���ص�½����
				ChatLogin frame = new ChatLogin();
				frame.setVisible(true);
				setVisible(false);
			}
		});
		
		//ע�ᰴť����
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
			u_name = textField.getText();
			u_pwd = new String(passwordField.getPassword());
			u_pwd_ag = new String(passwordField_1.getPassword());
				
				try {				
					if (u_name.length() != 0) {																
						if (u_pwd.length() != 0) {
							if (u_pwd.equals(u_pwd_ag)) {
								
								//����ע��
								client = new Socket("127.0.0.1", 8520);								
								new resignThread(client).start();
								
								ServerChatBean clientBean = new ServerChatBean();
								clientBean.setType(5);
								clientBean.setName(u_name);
								String time = ServerChatUtil.getTimer();
								clientBean.setTimer(time);
								clientBean.setInfo("");
								clientBean.setPassword(u_pwd_ag);
								
								try {
									oos = new ObjectOutputStream(client.getOutputStream());
									oos.writeObject(clientBean);
									oos.flush();
								} catch (IOException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
								
								textField.setText("");
								passwordField.setText("");
								passwordField_1.setText("");								

							} else {
								lblNewLabel.setText("���벻һ�£�");
								textField.setText("");
								passwordField.setText("");
								passwordField_1.setText("");
							}
						} else {
							lblNewLabel.setText("����Ϊ�գ�");
							textField.setText("");
							passwordField.setText("");
							passwordField_1.setText("");
						}
						
					} else {
						lblNewLabel.setText("�û�������Ϊ�գ�");
						textField.setText("");
						passwordField.setText("");
						passwordField_1.setText("");
					}
					
				} catch (UnknownHostException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}			
			}
		});
	}
	
	class resignThread extends Thread{
		
		private Socket client;
		private ServerChatBean bean;
		private ObjectInputStream ois;
		
		public resignThread(Socket client) {
			this.client = client;
		}
		
		public void run() {
			try {	
				while(true) {
					ois = new ObjectInputStream(client.getInputStream());
					bean = (ServerChatBean) ois.readObject();
					
					switch (bean.getType()) {
						case -2: {
							return;//���� 
							}
						default: {
							break;
						}
					}
					
					// �ж��û����Ƿ�����ͨ�û����Ѵ���
					if (bean.getInfo().equals("�û����Ѵ���!")) {
						lblNewLabel.setText("�û����Ѵ���!");
						
						textField.setText("");
						passwordField.setText("");
						passwordField_1.setText("");
						btnNewButton_1.setEnabled(false);
						
					}
					if(bean.getInfo().equals("ע��ɹ�!")){
						lblNewLabel.setText("ע��ɹ�!");
						
						textField.setText("");
						passwordField.setText("");
						passwordField_1.setText("");
						btnNewButton_1.setEnabled(false);
																
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
				close();//���ͷ�������
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

