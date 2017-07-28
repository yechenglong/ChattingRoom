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
			println("启动服务器成功：端口8520");
			onlines = new HashMap<String, ServerClientBean>();
			while(true){
				println("等待连接");
				Socket client = ss.accept();
			    println(ServerChatUtil.getTimer()+"\r\n"+client.toString()+" 连接成功" );
			    // 启动服务器接收线程
			    new ChatClientInput(client).start();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			println("启动服务器失败：端口8520");
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
		// server通知所有客户
		HashSet<String> set = new HashSet<String>();
		// 客户昵称
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
				// 不停的从客户端接收信息
				while (true) {
					// 读取从客户端接收到的chatbean信息
					ois = new ObjectInputStream(client.getInputStream());
					bean = (ServerChatBean)ois.readObject();
					
					// 分析chatbean中，type是那样一种类型
					switch (bean.getType()) {
					// 上下线更新
					case 0: { // 上线
						// 记录上线客户的用户名和端口在clientbean中
						ServerClientBean cbean = new ServerClientBean();
						cbean.setName(bean.getName());
						cbean.setSocket(client);
						// 添加在线用户
						onlines.put(bean.getName(), cbean);
						// 创建服务器的chatbean，并发送给客户端
						ServerChatBean serverBean = new ServerChatBean();
						serverBean.setType(0);
						serverBean.setInfo(bean.getTimer() + "  "
								+ bean.getName() + "上线了");
						//后台显示
						ui.textArea.setText(ui.textArea.getText() +bean.getTimer() + "  "
								+ bean.getName() + "上线了"+"\r\n");
						ui.textArea.selectAll();
						// 通知所有客户有人上线
						HashSet<String> set = new HashSet<String>();
						// 客户昵称
						set.addAll(onlines.keySet());
						serverBean.setClients(set);
						sendAll(serverBean);
						break;
					}
					case -1: { // 下线
						// 创建服务器的chatbean，并发送给客户端
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
						
						// 向剩下的在线用户发送有人离开的通知
						ServerChatBean serverBean2 = new ServerChatBean();
						serverBean2.setInfo(bean.getTimer() + "  "
								+ bean.getName() + " " + " 下线了");
						//后台显示
						ui.textArea.setText(ui.textArea.getText() +bean.getTimer() + "  "
								+ bean.getName() + " 下线了"+"\r\n");
						ui.textArea.selectAll();
						serverBean2.setType(0);
						HashSet<String> set = new HashSet<String>();
						set.addAll(onlines.keySet());
						serverBean2.setClients(set);

						sendAll(serverBean2);
						return;
					}
					case 1: { // 聊天
						
//						 创建服务器的chatbean，并发送给客户端
						ServerChatBean serverBean = new ServerChatBean();

						serverBean.setType(1);
						serverBean.setClients(bean.getClients());
						serverBean.setInfo(bean.getInfo());
						serverBean.setName(bean.getName());
						serverBean.setTimer(bean.getTimer());
						// 向选中的客户发送数据
						sendMessage(serverBean);
						break;
					}
					case 2: { // 请求接受文件
						// 创建服务器的chatbean，并发送给客户端
						ServerChatBean serverBean = new ServerChatBean();
						String info = bean.getTimer() + "  " + bean.getName()
								+ "向你传送文件,是否需要接受";

						serverBean.setType(2);
						serverBean.setClients(bean.getClients()); // 这是发送的目的地
						serverBean.setFileName(bean.getFileName()); // 文件名称
						serverBean.setSize(bean.getSize()); // 文件大小
						serverBean.setInfo(info);
						serverBean.setName(bean.getName()); // 来源
						serverBean.setTimer(bean.getTimer());
						// 向选中的客户发送数据
						sendMessage(serverBean);

						break;
					}
					case 3: { // 确定接收文件
						ServerChatBean serverBean = new ServerChatBean();

						serverBean.setType(3);
						serverBean.setClients(bean.getClients()); // 文件来源
						serverBean.setTo(bean.getTo()); // 文件目的地
						serverBean.setFileName(bean.getFileName()); // 文件名称
						serverBean.setIp(bean.getIp());
						serverBean.setPort(bean.getPort());
						serverBean.setName(bean.getName()); // 接收的客户名称
						serverBean.setTimer(bean.getTimer());
						// 通知文件来源的客户，对方确定接收文件
						sendMessage(serverBean);
						break;
					}
					case 4: {// 取消接收文件
						ServerChatBean serverBean = new ServerChatBean();

						serverBean.setType(4);
						serverBean.setClients(bean.getClients()); // 文件来源
						serverBean.setTo(bean.getTo()); // 文件目的地
						serverBean.setFileName(bean.getFileName());
						serverBean.setInfo(bean.getInfo());
						serverBean.setName(bean.getName());// 接收的客户名称
						serverBean.setTimer(bean.getTimer());
						sendMessage(serverBean);

						break;
					}				
					case 5: {
						//请求注册
						ServerChatBean serverBean = new ServerChatBean();
					
						String u_name = bean.getName();
						String u_pwd = bean.getPassword();
						
						Properties userPro = new Properties();
						File file = new File("Server.properties");
						ServerChatUtil.loadPro(userPro, file);	

						// 判断用户名是否在普通用户中已存在
						if (userPro.containsKey(u_name)) {
							serverBean.setInfo("用户名已存在!");
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
							serverBean.setInfo("注册成功!");
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
						//请求登录
						ServerChatBean serverBean = new ServerChatBean();
					
						String u_name = bean.getName();
						String u_pwd = bean.getPassword();
						
						Properties userPro = new Properties();
						File file = new File("Server.properties");
						ServerChatUtil.loadPro(userPro, file);	

						// 判断用户名是否已经注册
						if (userPro.containsKey(u_name)) {
							
							if(u_pwd.equals(userPro.getProperty(u_name))){
								serverBean.setInfo("用户名已注册且密码正确!");
								try {
									oos = new ObjectOutputStream(client.getOutputStream());
									oos.writeObject(serverBean);
									oos.flush();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
							} else {
								serverBean.setInfo("用户名已注册但密码不正确!");
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
							serverBean.setInfo("用户名尚未注册!");
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
						//注册及登录线程处理
						// 创建服务器的chatbean，并发送给客户端
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

		// 向选中的用户发送数据
		private void sendMessage(ServerChatBean serverBean) {
			// 首先取得所有的values
			Set<String> cbs = onlines.keySet();
			Iterator<String> it = cbs.iterator();
			// 选中客户
			HashSet<String> clients = serverBean.getClients();
			while (it.hasNext()) {
				// 在线客户
				String client = it.next();
				// 选中的客户中若是在线的，就发送serverbean
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

		// 向所有的用户发送数据
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

