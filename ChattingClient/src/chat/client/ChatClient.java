package chat.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import chat.function.ServerChatBean;
import chat.util.ServerChatUtil;

public class ChatClient extends Thread {
	
	private Socket clientSocket;
	private String name;
	private Vector onlines;
	private ObjectInputStream ois;
	private ChatClientUI ui;
	public static ServerChatBean  bean;

	public ChatClient(ChatClientUI ui,String name,Socket clientSocket,Vector onlines){
		this.ui = ui;
		this.name = name;
		this.clientSocket = clientSocket;
		this.onlines = onlines;
	}	
	
	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		try {
			// ��ͣ�Ĵӷ�����������Ϣ
			while (true) {
				ois = new ObjectInputStream(clientSocket.getInputStream());
				bean = (ServerChatBean) ois.readObject();
				switch (bean.getType()) {
				case 0: {
					// �����б�
					onlines.clear();
					HashSet<String> clients = bean.getClients();
					Iterator<String> it = clients.iterator();
					while (it.hasNext()) {
						String ele = it.next();
						if (name.equals(ele)) {
							onlines.add(ele + "(��)");
						} else {
							onlines.add(ele);
						}
					}

					ui.listmodel = new UUListModel(onlines);
					ui.list.setModel(ui.listmodel);
					ui.aau2.play();
					ui.textArea.append(bean.getInfo() + "\r\n");
					ui.textArea.selectAll();
					break;
				}
				case -1: {
					//����
					return;
				}
				case 1: {
					//����
					String info = bean.getTimer() + "  " + bean.getName()
							+ " �� " + bean.getClients() + "˵:\r\n";
					if (info.contains(name) ) {
						info = info.replace(name, "��");
					}
					ui.aau.play();
					ui.textArea.append(info+bean.getInfo() + "\r\n");
					ui.textArea.selectAll();
					break;
				}
				case 2: {
					// ���ڵȴ�Ŀ��ͻ�ȷ���Ƿ�����ļ��Ǹ�����״̬�������������̴߳���
					new Thread(){
						public void run() {
							//��ʾ�Ƿ�����ļ��Ի���
							int result = JOptionPane.showConfirmDialog(
									ui.getContentPane(), bean.getInfo());
							switch(result){
							case 0:{  //�����ļ�
								JFileChooser chooser = new JFileChooser();
								chooser.setDialogTitle("�����ļ���"); // ����Ŷ...
								//Ĭ���ļ����ƻ��з��ڵ�ǰĿ¼��
								chooser.setSelectedFile(new File(bean
										.getFileName()));
								chooser.showDialog(ui.getContentPane(), "����"); // ���ǰ�ť������..
								//����·��
								String saveFilePath =chooser.getSelectedFile().toString();
							
								//�����ͻ�ChatBean
								ServerChatBean clientBean = new ServerChatBean();
								clientBean.setType(3);
								clientBean.setName(name);  //�����ļ��Ŀͻ�����
								clientBean.setTimer(ServerChatUtil.getTimer());
								clientBean.setFileName(saveFilePath);
								clientBean.setInfo("ȷ�������ļ�");

								// �ж�Ҫ���͸�˭
								HashSet<String> set = new HashSet<String>();
								set.add(bean.getName());
								clientBean.setClients(set);  //�ļ���Դ
								clientBean.setTo(bean.getClients());//����Щ�ͻ������ļ�
								
								
								
								// �����µ�tcp socket ��������, ���Ƕ������ӵĹ���, ���������...
								try {
									ServerSocket ss = new ServerSocket(0); // 0���Ի�ȡ���еĶ˿ں�
									
									InetAddress addr = InetAddress.getLocalHost();
									String ip = null;
									ip=addr.getHostAddress().toString();//��ñ���IP

									clientBean.setIp(ip);
									clientBean.setPort(ss.getLocalPort());
									ChatClientUI.sendMessage(clientBean); // ��ͨ�����������߷��ͷ�, �����ֱ�ӷ����ļ�����������...
									
									ui.isReceiveFile=true;
									//�ȴ��ļ���Դ�Ŀͻ��������ļ�....Ŀ��ͻ��������϶�ȡ�ļ�����д�ڱ�����
									Socket sk = ss.accept();
									ui.textArea.append(ServerChatUtil.getTimer() + "  " + bean.getFileName()
											+ "�ļ�������.\r\n");
									DataInputStream dis = new DataInputStream(  //�������϶�ȡ�ļ�
											new BufferedInputStream(sk.getInputStream()));
									DataOutputStream dos = new DataOutputStream(  //д�ڱ�����
											new BufferedOutputStream(new FileOutputStream(
													saveFilePath)));
			
									int count = 0;
									int num = bean.getSize() / 100;
									int index = 0;
									while (count < bean.getSize()) {
										int t = dis.read();
										dos.write(t);
										count++;
										
										if(num>0){
											if (count % num == 0 && index < 100) {
												ui.progressBar.setValue(++index);
											}
											ui.lblNewLabel.setText("���ؽ���:" + count
													+ "/" + bean.getSize() + "  ����" + index
													+ "%");
										}else{
											ui.lblNewLabel.setText("���ؽ���:" + count
													+ "/" + bean.getSize() +"  ����:"+new Double(new Double(count).doubleValue()/new Double(bean.getSize()).doubleValue()*100).intValue()+"%");
											if(count==bean.getSize()){
												ui.progressBar.setValue(100);
											}
										}
			
									}
									
									//���ļ���Դ�ͻ�������ʾ���ļ��������
									PrintWriter out = new PrintWriter(sk.getOutputStream(),true);
									out.println(ServerChatUtil.getTimer() + " ���͸�"+name+"���ļ�[" + bean.getFileName()+"]"
											+ "�ļ��������.\r\n");
									out.flush();
									dos.flush();
									dos.close();
									out.close();
									dis.close();
									sk.close();
									ss.close();
									ui.textArea.append(ServerChatUtil.getTimer() + "  " + bean.getFileName()
											+ "�ļ��������.���λ��Ϊ:"+saveFilePath+"\r\n");
									ui.isReceiveFile = false;
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
								break;
							}
							default: {
								ServerChatBean clientBean = new ServerChatBean();
								clientBean.setType(4);
								clientBean.setName(name);  //�����ļ��Ŀͻ�����
								clientBean.setTimer(ServerChatUtil.getTimer());
								clientBean.setFileName(bean.getFileName());
								clientBean.setInfo(ServerChatUtil.getTimer() + "  "
										+ name + "ȡ�������ļ�["
										+ bean.getFileName() + "]");


								// �ж�Ҫ���͸�˭
								HashSet<String> set = new HashSet<String>();
								set.add(bean.getName());
								clientBean.setClients(set);  //�ļ���Դ
								clientBean.setTo(bean.getClients());//����Щ�ͻ������ļ�
								
								ChatClientUI.sendMessage(clientBean);
							 	
								break;
							
							}
						}
						};	
					}.start();
					break;
				}
				case 3: {  //Ŀ��ͻ�Ը������ļ���Դ�ͻ���ʼ��ȡ�����ļ������͵�������
					ui.textArea.append(bean.getTimer() + "  "+ bean.getName() + "ȷ�������ļ�" + ",�ļ�������..\r\n");
					new Thread(){
						public void run() {
							
							try {
								ui.isSendFile = true;
								//����Ҫ�����ļ��Ŀͻ��׽���								
								
								Socket s = new Socket(bean.getIp(),bean.getPort());
								DataInputStream dis = new DataInputStream(
										new FileInputStream(ui.filePath));  //���ض�ȡ�ÿͻ��ղ�ѡ�е��ļ�
								DataOutputStream dos = new DataOutputStream(
										new BufferedOutputStream(s
												.getOutputStream()));  //����д���ļ�
								
							
								int size = dis.available();
								
								int count = 0;  //��ȡ����
								int num = size / 100;
								int index = 0;
								while (count < size) {
									
									int t = dis.read();
									dos.write(t);
									count++;  //ÿ��ֻ��ȡһ���ֽ�

									if(num>0){
										if (count % num == 0 && index < 100) {
											ui.progressBar.setValue(++index);

										}
										ui.lblNewLabel.setText("�ϴ�����:" + count + "/"
														+ size + "  ����" + index
														+ "%");
									}else{
										ui.lblNewLabel.setText("�ϴ�����:" + count + "/"
												+ size +"  ����:"+new Double(new Double(count).doubleValue()/new Double(size).doubleValue()*100).intValue()+"%"
												);
										if(count==size){
											ui.progressBar.setValue(100);
										}
									}
								}
								dos.flush();
								dis.close();
							  //��ȡĿ��ͻ�����ʾ������ϵ���Ϣ...
							    BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
							    ui.textArea.append( br.readLine() + "\r\n");
							    ui.isSendFile = false;
								br.close();
							    s.close();
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						
						};
					}.start();
					break;
				}
				case 4: {
					ui.textArea.append(bean.getInfo() + "\r\n");
					break;
				}			
				default: {
					break;
				}
				}

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (clientSocket != null) {
				try {
					clientSocket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			System.exit(0);
		}
	}


}
