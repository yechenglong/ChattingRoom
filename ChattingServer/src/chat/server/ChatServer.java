package chat.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import chat.function.ServerChatBean;
import chat.function.ServerClientBean;
import chat.util.ServerChatUtil;

public class ChatServer extends Thread{
	private ChatServerUI ui;
	private static ServerSocket ss;
	public static HashMap<String, ServerClientBean> onlines;
	
	public ChatServer(ChatServerUI ui) {
        this.ui = ui;
        this.start();
    }
	
	public void run() {
		try {
			ss = new ServerSocket(8520);
			println("�����������ɹ����˿�8520");
			onlines = new HashMap<String, ServerClientBean>();
			while(true){
				println("�ȴ�����");
				Socket client = ss.accept();
			    println(ServerChatUtil.getTimer()+"\r\n"+client.toString()+" ���ӳɹ�" );
			    // ���������������߳�
			    new ChatClientInput(client).start();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			println("����������ʧ�ܣ��˿�8520");
            println(e.toString());
		    e.printStackTrace();
		}
	}
	
	public void println(String s) {
        if (s != null) {
            this.ui.textArea.setText(this.ui.textArea.getText() + s + "\n");
            this.ui.textArea.selectAll();
            System.out.println(s + "\n");
        }
    }

    public void closeServer() {
        try {
            if (ss != null)
                ss.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void stageSend(String info){
		
    	ServerChatBean serverBean = new ServerChatBean();
		serverBean.setType(0);
		String time = ServerChatUtil.getTimer();
		serverBean.setInfo(time+" server:\r\n"+info);
		// server֪ͨ���пͻ�
		HashSet<String> set = new HashSet<String>();
		// �ͻ��ǳ�
		set.addAll(onlines.keySet());
		serverBean.setClients(set);
    	
		Collection<ServerClientBean> clients = onlines.values();
		Iterator<ServerClientBean> it = clients.iterator();
		ObjectOutputStream oos;
		while (it.hasNext()) {
			Socket c = it.next().getSocket();
			try {
				oos = new ObjectOutputStream(c.getOutputStream());
				oos.writeObject(serverBean);
				oos.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }

	 class ChatClientInput extends Thread {
		private Socket client;
		private ServerChatBean bean;
		private ObjectInputStream ois;
		private ObjectOutputStream oos;

		public ChatClientInput(Socket client) {
			this.client = client;
		}

		@Override
		public void run() {
			try {
				// ��ͣ�Ĵӿͻ��˽�����Ϣ
				while (true) {
					// ��ȡ�ӿͻ��˽��յ���chatbean��Ϣ
					ois = new ObjectInputStream(client.getInputStream());
					bean = (ServerChatBean)ois.readObject();
					
					// ����chatbean�У�type������һ������
					switch (bean.getType()) {
					// �����߸���
					case 0: { // ����
						// ��¼���߿ͻ����û����Ͷ˿���clientbean��
						ServerClientBean cbean = new ServerClientBean();
						cbean.setName(bean.getName());
						cbean.setSocket(client);
						// ��������û�
						onlines.put(bean.getName(), cbean);
						// ������������chatbean�������͸��ͻ���
						ServerChatBean serverBean = new ServerChatBean();
						serverBean.setType(0);
						serverBean.setInfo(bean.getTimer() + "  "
								+ bean.getName() + "������");
						//��̨��ʾ
						ui.textArea.setText(ui.textArea.getText() +bean.getTimer() + "  "
								+ bean.getName() + "������"+"\r\n");
						ui.textArea.selectAll();
						// ֪ͨ���пͻ���������
						HashSet<String> set = new HashSet<String>();
						// �ͻ��ǳ�
						set.addAll(onlines.keySet());
						serverBean.setClients(set);
						sendAll(serverBean);
						break;
					}
					case -1: { // ����
						// ������������chatbean�������͸��ͻ���
						ServerChatBean serverBean = new ServerChatBean();
						serverBean.setType(-1);

						try {
							oos = new ObjectOutputStream(
									client.getOutputStream());
							oos.writeObject(serverBean);
							oos.flush();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						onlines.remove(bean.getName());
						
						// ��ʣ�µ������û����������뿪��֪ͨ
						ServerChatBean serverBean2 = new ServerChatBean();
						serverBean2.setInfo(bean.getTimer() + "  "
								+ bean.getName() + " " + " ������");
						//��̨��ʾ
						ui.textArea.setText(ui.textArea.getText() +bean.getTimer() + "  "
								+ bean.getName() + " ������"+"\r\n");
						ui.textArea.selectAll();
						serverBean2.setType(0);
						HashSet<String> set = new HashSet<String>();
						set.addAll(onlines.keySet());
						serverBean2.setClients(set);

						sendAll(serverBean2);
						return;
					}
					case 1: { // ����
						
//						 ������������chatbean�������͸��ͻ���
						ServerChatBean serverBean = new ServerChatBean();

						serverBean.setType(1);
						serverBean.setClients(bean.getClients());
						serverBean.setInfo(bean.getInfo());
						serverBean.setName(bean.getName());
						serverBean.setTimer(bean.getTimer());
						// ��ѡ�еĿͻ���������
						sendMessage(serverBean);
						break;
					}
					case 2: { // ��������ļ�
						// ������������chatbean�������͸��ͻ���
						ServerChatBean serverBean = new ServerChatBean();
						String info = bean.getTimer() + "  " + bean.getName()
								+ "���㴫���ļ�,�Ƿ���Ҫ����";

						serverBean.setType(2);
						serverBean.setClients(bean.getClients()); // ���Ƿ��͵�Ŀ�ĵ�
						serverBean.setFileName(bean.getFileName()); // �ļ�����
						serverBean.setSize(bean.getSize()); // �ļ���С
						serverBean.setInfo(info);
						serverBean.setName(bean.getName()); // ��Դ
						serverBean.setTimer(bean.getTimer());
						// ��ѡ�еĿͻ���������
						sendMessage(serverBean);

						break;
					}
					case 3: { // ȷ�������ļ�
						ServerChatBean serverBean = new ServerChatBean();

						serverBean.setType(3);
						serverBean.setClients(bean.getClients()); // �ļ���Դ
						serverBean.setTo(bean.getTo()); // �ļ�Ŀ�ĵ�
						serverBean.setFileName(bean.getFileName()); // �ļ�����
						serverBean.setIp(bean.getIp());
						serverBean.setPort(bean.getPort());
						serverBean.setName(bean.getName()); // ���յĿͻ�����
						serverBean.setTimer(bean.getTimer());
						// ֪ͨ�ļ���Դ�Ŀͻ����Է�ȷ�������ļ�
						sendMessage(serverBean);
						break;
					}
					case 4: {// ȡ�������ļ�
						ServerChatBean serverBean = new ServerChatBean();

						serverBean.setType(4);
						serverBean.setClients(bean.getClients()); // �ļ���Դ
						serverBean.setTo(bean.getTo()); // �ļ�Ŀ�ĵ�
						serverBean.setFileName(bean.getFileName());
						serverBean.setInfo(bean.getInfo());
						serverBean.setName(bean.getName());// ���յĿͻ�����
						serverBean.setTimer(bean.getTimer());
						sendMessage(serverBean);

						break;
					}				
					case 5: {
						//����ע��
						ServerChatBean serverBean = new ServerChatBean();
					
						String u_name = bean.getName();
						String u_pwd = bean.getPassword();
						
						Properties userPro = new Properties();
						File file = new File("Server.properties");
						ServerChatUtil.loadPro(userPro, file);	

						// �ж��û����Ƿ�����ͨ�û����Ѵ���
						if (userPro.containsKey(u_name)) {
							serverBean.setInfo("�û����Ѵ���!");
							//sendMessage(serverBean);
							try {
								oos = new ObjectOutputStream(client.getOutputStream());
								oos.writeObject(serverBean);
								oos.flush();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} else {
							userPro.setProperty(u_name, u_pwd);
							serverBean.setInfo("ע��ɹ�!");
							//sendMessage(serverBean);
							try {
								oos = new ObjectOutputStream(client.getOutputStream());
								oos.writeObject(serverBean);
								oos.flush();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						try {
							userPro.store(new FileOutputStream(file),
									"Copyright (c) Chguancheng");
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						}
						break;
					}
					
					case 6: {
						//�����¼
						ServerChatBean serverBean = new ServerChatBean();
					
						String u_name = bean.getName();
						String u_pwd = bean.getPassword();
						
						Properties userPro = new Properties();
						File file = new File("Server.properties");
						ServerChatUtil.loadPro(userPro, file);	

						// �ж��û����Ƿ��Ѿ�ע��
						if (userPro.containsKey(u_name)) {
							
							if(u_pwd.equals(userPro.getProperty(u_name))){
								serverBean.setInfo("�û�����ע����������ȷ!");
								try {
									oos = new ObjectOutputStream(client.getOutputStream());
									oos.writeObject(serverBean);
									oos.flush();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
							} else {
								serverBean.setInfo("�û�����ע�ᵫ���벻��ȷ!");
								try {
									oos = new ObjectOutputStream(client.getOutputStream());
									oos.writeObject(serverBean);
									oos.flush();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}							
							
						} else {
							serverBean.setInfo("�û�����δע��!");
							try {
								oos = new ObjectOutputStream(client.getOutputStream());
								oos.writeObject(serverBean);
								oos.flush();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						break;
					}
					
					case -2: {
						//ע�ἰ��¼�̴߳���
						// ������������chatbean�������͸��ͻ���
						ServerChatBean serverBean = new ServerChatBean();
						serverBean.setType(-2);
						serverBean.setInfo(bean.getInfo());

						try {
							oos = new ObjectOutputStream(
									client.getOutputStream());
							oos.writeObject(serverBean);
							oos.flush();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return;
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
				close();
			}
		}

		// ��ѡ�е��û���������
		private void sendMessage(ServerChatBean serverBean) {
			// ����ȡ�����е�values
			Set<String> cbs = onlines.keySet();
			Iterator<String> it = cbs.iterator();
			// ѡ�пͻ�
			HashSet<String> clients = serverBean.getClients();
			while (it.hasNext()) {
				// ���߿ͻ�
				String client = it.next();
				// ѡ�еĿͻ����������ߵģ��ͷ���serverbean
				if (clients.contains(client)) {
					Socket c = onlines.get(client).getSocket();
					ObjectOutputStream oos;
					try {
						oos = new ObjectOutputStream(c.getOutputStream());
						oos.writeObject(serverBean);
						oos.flush();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
		}

		// �����е��û���������
		public void sendAll(ServerChatBean serverBean) {
			Collection<ServerClientBean> clients = onlines.values();
			Iterator<ServerClientBean> it = clients.iterator();
			ObjectOutputStream oos;
			while (it.hasNext()) {
				Socket c = it.next().getSocket();
				try {
					oos = new ObjectOutputStream(c.getOutputStream());
					oos.writeObject(serverBean);
					oos.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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

