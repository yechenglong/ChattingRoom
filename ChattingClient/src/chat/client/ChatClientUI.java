package chat.client;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import chat.function.ServerChatBean;
import chat.util.ServerChatUtil;


class CellRenderer extends JLabel implements ListCellRenderer {
	CellRenderer() {
		setOpaque(true);
	}

	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {

		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));// ������Ϊ5�Ŀհױ߿�

		if (value != null) {
			setText(value.toString());
			setIcon(new ImageIcon("images//1.jpg"));
		}
		if (isSelected) {
			setBackground(new Color(255, 255, 153));// ���ñ���ɫ
			setForeground(Color.black);
		} else {
			// ����ѡȡ��ȡ��ѡȡ��ǰ���뱳����ɫ.
			setBackground(Color.white); // ���ñ���ɫ
			setForeground(Color.black);
		}
		setEnabled(list.isEnabled());
		setFont(new Font("sdf", Font.ROMAN_BASELINE, 13));
		setOpaque(true);
		return this;
	}
}


class UUListModel extends AbstractListModel{
	
	private Vector vs;
	
	public UUListModel(Vector vs){
		this.vs = vs;
	}

	@Override
	public Object getElementAt(int index) {
		// TODO Auto-generated method stub
		return vs.get(index);
	}

	@Override
	public int getSize() {
		// TODO Auto-generated method stub
		return vs.size();
	}
	
}


public class ChatClientUI extends JFrame {

	private static final long serialVersionUID = 6129126482250125466L;

	public static JPanel contentPane;
	public static Socket clientSocket;
	public static ObjectOutputStream oos;
	public static ObjectInputStream ois;
	public static String name;
	public JTextArea textArea;
	public AbstractListModel listmodel;
	public JList list;
	public String filePath;
	public JLabel lblNewLabel;
	public JProgressBar progressBar;
	public static Vector onlines;
	public boolean isSendFile = false;
	public boolean isReceiveFile = false;

	// ����
	public static File file, file2;
	public static URL cb, cb2;
	public AudioClip aau;
	public AudioClip aau2;

	/**
	 * Create the frame.
	 */

	public ChatClientUI(String u_name, Socket client) {
		// ��ֵ
		name = u_name;
		clientSocket = client;
		onlines = new Vector();
		
		SwingUtilities.updateComponentTreeUI(this);

		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnsupportedLookAndFeelException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		setTitle(name);
		setResizable(false);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(200, 100, 688, 510);
		contentPane = new JPanel() {
			private static final long serialVersionUID = 1L;
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.drawImage(new ImageIcon("images\\������1.jpg").getImage(), 0, 0,
						getWidth(), getHeight(), null);
			}

		};
		setContentPane(contentPane);
		contentPane.setLayout(null);

		// ������Ϣ��ʾ����
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 10, 410, 300);
		getContentPane().add(scrollPane);

		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setLineWrap(true);//�����Զ����й��� 
		textArea.setWrapStyleWord(true);//������в����ֹ��� 
		textArea.setFont(new Font("sdf", Font.BOLD, 13));
		scrollPane.setViewportView(textArea);

		// ��������
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(10, 347, 411, 97);
		getContentPane().add(scrollPane_1);

		final JTextArea textArea_1 = new JTextArea();
		textArea_1.setLineWrap(true);//�����Զ����й��� 
		textArea_1.setWrapStyleWord(true);//������в����ֹ��� 
		scrollPane_1.setViewportView(textArea_1);

		// �رհ�ť
		final JButton btnNewButton = new JButton("\u5173\u95ED");
		btnNewButton.setBounds(214, 448, 60, 30);
		getContentPane().add(btnNewButton);

		// ���Ͱ�ť
		JButton btnNewButton_1 = new JButton("\u53D1\u9001");
		btnNewButton_1.setBounds(313, 448, 60, 30);
		getRootPane().setDefaultButton(btnNewButton_1);
		getContentPane().add(btnNewButton_1);

		// ���߿ͻ��б�
		listmodel = new UUListModel(onlines) ;
		list = new JList(listmodel);
		list.setCellRenderer(new CellRenderer());
		list.setOpaque(false);
		Border etch = BorderFactory.createEtchedBorder();
		list.setBorder(BorderFactory.createTitledBorder(etch, "["+u_name+"]"
				+ " ���߿ͻ�", TitledBorder.LEADING, TitledBorder.TOP, new Font(
				"sdf", Font.BOLD, 16), Color.gray));

		JScrollPane scrollPane_2 = new JScrollPane(list);
		scrollPane_2.setBounds(430, 10, 245, 375);
		scrollPane_2.setOpaque(false);
		scrollPane_2.getViewport().setOpaque(false);
		getContentPane().add(scrollPane_2);

		// �ļ�������
		progressBar = new JProgressBar();
		progressBar.setBounds(430, 390, 245, 15);
		progressBar.setMinimum(1);
		progressBar.setMaximum(100);
		getContentPane().add(progressBar);

		// �ļ�������ʾ
		lblNewLabel = new JLabel(
				"\u6587\u4EF6\u4F20\u9001\u4FE1\u606F\u680F:");
		lblNewLabel.setFont(new Font("SimSun", Font.PLAIN, 12));
		lblNewLabel.setBackground(Color.WHITE);
		lblNewLabel.setBounds(430, 410, 245, 15);
		getContentPane().add(lblNewLabel);

		try {
			oos = new ObjectOutputStream(clientSocket.getOutputStream());
			// ��¼���߿ͻ�����Ϣ��chatbean�У������͸�������
			ServerChatBean bean = new ServerChatBean();
			bean.setType(0);
			bean.setName(name);
			bean.setTimer(ServerChatUtil.getTimer());
			oos.writeObject(bean);
			oos.flush();

			// ��Ϣ��ʾ����
			file = new File("sounds\\��ŷ.wav");
			cb = file.toURL();
			aau = Applet.newAudioClip(cb);
			// ������ʾ����
			file2 = new File("sounds\\��.wav");
			cb2 = file2.toURL();
			aau2 = Applet.newAudioClip(cb2);

			// �����ͻ������߳�
			new ChatClient(ChatClientUI.this,name,clientSocket,onlines).start();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// ���Ͱ�ť
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String info = textArea_1.getText();
				List to = list.getSelectedValuesList();

				if (to.size() < 1) {
					JOptionPane.showMessageDialog(getContentPane(), "��ѡ���������");
					return;
				}
				if (to.toString().contains(name+"(��)")) {
					JOptionPane
							.showMessageDialog(getContentPane(), "�������Լ�������Ϣ");
					return;
				}
				if (info.equals("")) {
					JOptionPane.showMessageDialog(getContentPane(), "���ܷ��Ϳ���Ϣ");
					return;
				}

				ServerChatBean clientBean = new ServerChatBean();
				clientBean.setType(1);
				clientBean.setName(name);
				String time = ServerChatUtil.getTimer();
				clientBean.setTimer(time);
				clientBean.setInfo(info);
				HashSet set = new HashSet();
				set.addAll(to);
				clientBean.setClients(set);

				// �Լ���������ҲҪ��ʵ���Լ�����Ļ����
				textArea.append(time + " �Ҷ�" + to + "˵:\r\n" + info + "\r\n");

				sendMessage(clientBean);
				textArea_1.setText(null);
				textArea_1.requestFocus();
			}
		});

		// �رհ�ť
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(isSendFile || isReceiveFile){
					JOptionPane.showMessageDialog(contentPane,
							"���ڴ����ļ��У��������뿪...",
							"Error Message", JOptionPane.ERROR_MESSAGE);
				}else{
				btnNewButton.setEnabled(false);
				ServerChatBean clientBean = new ServerChatBean();
				clientBean.setType(-1);
				clientBean.setName(name);
				clientBean.setTimer(ServerChatUtil.getTimer());
				sendMessage(clientBean);
				}
			}
		});

		// �뿪
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				// TODO Auto-generated method stub
				if(isSendFile || isReceiveFile){
					JOptionPane.showMessageDialog(contentPane,
							"���ڴ����ļ��У��������뿪...",
							"Error Message", JOptionPane.ERROR_MESSAGE);
				}else{
				int result = JOptionPane.showConfirmDialog(getContentPane(),
						"��ȷ��Ҫ�뿪������");
				if (result == 0) {
					ServerChatBean clientBean = new ServerChatBean();
					clientBean.setType(-1);
					clientBean.setName(name);
					clientBean.setTimer(ServerChatUtil.getTimer());
					sendMessage(clientBean);
				}
				}
			}
		});

		// �б����
		list.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				List to = list.getSelectedValuesList();
				if (e.getClickCount() == 2) {
					
					if (to.toString().contains(name+"(��)")) {
						JOptionPane
								.showMessageDialog(getContentPane(), "�������Լ������ļ�");
						return;
					}
					
					// ˫�����ļ��ļ�ѡ���
					JFileChooser chooser = new JFileChooser();
					chooser.setDialogTitle("ѡ���ļ���"); // ����Ŷ...
					chooser.showDialog(getContentPane(), "ѡ��"); // ���ǰ�ť������..

					// �ж��Ƿ�ѡ�����ļ�
					if (chooser.getSelectedFile() != null) {
						// ��ȡ·��
						filePath = chooser.getSelectedFile().getPath();
						File file = new File(filePath);
						// �ļ�Ϊ��
						if (file.length() == 0) {
							JOptionPane.showMessageDialog(getContentPane(),
									filePath + "�ļ�Ϊ��,��������.");
							return;
						}

						ServerChatBean clientBean = new ServerChatBean();
						clientBean.setType(2);// �������ļ�
						clientBean.setSize(new Long(file.length()).intValue());
						clientBean.setName(name);
						clientBean.setTimer(ServerChatUtil.getTimer());
						clientBean.setFileName(file.getName()); // ��¼�ļ�������
						clientBean.setInfo("�������ļ�");

						// �ж�Ҫ���͸�˭
						HashSet<String> set = new HashSet<String>();
						set.addAll(list.getSelectedValuesList());
						clientBean.setClients(set);
						sendMessage(clientBean);
					}
				}
			}
		});

	}

	public static void sendMessage(ServerChatBean clientBean) {
		try {
			oos = new ObjectOutputStream(clientSocket.getOutputStream());
			oos.writeObject(clientBean);
			oos.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void  main(String[] args) {
	
		
	}

}

