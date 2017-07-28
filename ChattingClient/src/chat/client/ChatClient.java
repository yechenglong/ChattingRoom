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
			// 不停的从服务器接收信息
			while (true) {
				ois = new ObjectInputStream(clientSocket.getInputStream());
				bean = (ServerChatBean) ois.readObject();
				switch (bean.getType()) {
				case 0: {
					// 更新列表
					onlines.clear();
					HashSet<String> clients = bean.getClients();
					Iterator<String> it = clients.iterator();
					while (it.hasNext()) {
						String ele = it.next();
						if (name.equals(ele)) {
							onlines.add(ele + "(我)");
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
					//下线
					return;
				}
				case 1: {
					//聊天
					String info = bean.getTimer() + "  " + bean.getName()
							+ " 对 " + bean.getClients() + "说:\r\n";
					if (info.contains(name) ) {
						info = info.replace(name, "我");
					}
					ui.aau.play();
					ui.textArea.append(info+bean.getInfo() + "\r\n");
					ui.textArea.selectAll();
					break;
				}
				case 2: {
					// 由于等待目标客户确认是否接收文件是个阻塞状态，所以这里用线程处理
					new Thread(){
						public void run() {
							//显示是否接收文件对话框
							int result = JOptionPane.showConfirmDialog(
									ui.getContentPane(), bean.getInfo());
							switch(result){
							case 0:{  //接收文件
								JFileChooser chooser = new JFileChooser();
								chooser.setDialogTitle("保存文件框"); // 标题哦...
								//默认文件名称还有放在当前目录下
								chooser.setSelectedFile(new File(bean
										.getFileName()));
								chooser.showDialog(ui.getContentPane(), "保存"); // 这是按钮的名字..
								//保存路径
								String saveFilePath =chooser.getSelectedFile().toString();
							
								//创建客户ChatBean
								ServerChatBean clientBean = new ServerChatBean();
								clientBean.setType(3);
								clientBean.setName(name);  //接收文件的客户名字
								clientBean.setTimer(ServerChatUtil.getTimer());
								clientBean.setFileName(saveFilePath);
								clientBean.setInfo("确定接收文件");

								// 判断要发送给谁
								HashSet<String> set = new HashSet<String>();
								set.add(bean.getName());
								clientBean.setClients(set);  //文件来源
								clientBean.setTo(bean.getClients());//给这些客户发送文件
								
								
								
								// 创建新的tcp socket 接收数据, 这是额外增加的功能, 大家请留意...
								try {
									ServerSocket ss = new ServerSocket(0); // 0可以获取空闲的端口号
									
									InetAddress addr = InetAddress.getLocalHost();
									String ip = null;
									ip=addr.getHostAddress().toString();//获得本机IP

									clientBean.setIp(ip);
									clientBean.setPort(ss.getLocalPort());
									ChatClientUI.sendMessage(clientBean); // 先通过服务器告诉发送方, 你可以直接发送文件到我这里了...
									
									ui.isReceiveFile=true;
									//等待文件来源的客户，输送文件....目标客户从网络上读取文件，并写在本地上
									Socket sk = ss.accept();
									ui.textArea.append(ServerChatUtil.getTimer() + "  " + bean.getFileName()
											+ "文件保存中.\r\n");
									DataInputStream dis = new DataInputStream(  //从网络上读取文件
											new BufferedInputStream(sk.getInputStream()));
									DataOutputStream dos = new DataOutputStream(  //写在本地上
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
											ui.lblNewLabel.setText("下载进度:" + count
													+ "/" + bean.getSize() + "  整体" + index
													+ "%");
										}else{
											ui.lblNewLabel.setText("下载进度:" + count
													+ "/" + bean.getSize() +"  整体:"+new Double(new Double(count).doubleValue()/new Double(bean.getSize()).doubleValue()*100).intValue()+"%");
											if(count==bean.getSize()){
												ui.progressBar.setValue(100);
											}
										}
			
									}
									
									//给文件来源客户发条提示，文件保存完毕
									PrintWriter out = new PrintWriter(sk.getOutputStream(),true);
									out.println(ServerChatUtil.getTimer() + " 发送给"+name+"的文件[" + bean.getFileName()+"]"
											+ "文件保存完毕.\r\n");
									out.flush();
									dos.flush();
									dos.close();
									out.close();
									dis.close();
									sk.close();
									ss.close();
									ui.textArea.append(ServerChatUtil.getTimer() + "  " + bean.getFileName()
											+ "文件保存完毕.存放位置为:"+saveFilePath+"\r\n");
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
								clientBean.setName(name);  //接收文件的客户名字
								clientBean.setTimer(ServerChatUtil.getTimer());
								clientBean.setFileName(bean.getFileName());
								clientBean.setInfo(ServerChatUtil.getTimer() + "  "
										+ name + "取消接收文件["
										+ bean.getFileName() + "]");


								// 判断要发送给谁
								HashSet<String> set = new HashSet<String>();
								set.add(bean.getName());
								clientBean.setClients(set);  //文件来源
								clientBean.setTo(bean.getClients());//给这些客户发送文件
								
								ChatClientUI.sendMessage(clientBean);
							 	
								break;
							
							}
						}
						};	
					}.start();
					break;
				}
				case 3: {  //目标客户愿意接收文件，源客户开始读取本地文件并发送到网络上
					ui.textArea.append(bean.getTimer() + "  "+ bean.getName() + "确定接收文件" + ",文件传送中..\r\n");
					new Thread(){
						public void run() {
							
							try {
								ui.isSendFile = true;
								//创建要接收文件的客户套接字								
								
								Socket s = new Socket(bean.getIp(),bean.getPort());
								DataInputStream dis = new DataInputStream(
										new FileInputStream(ui.filePath));  //本地读取该客户刚才选中的文件
								DataOutputStream dos = new DataOutputStream(
										new BufferedOutputStream(s
												.getOutputStream()));  //网络写出文件
								
							
								int size = dis.available();
								
								int count = 0;  //读取次数
								int num = size / 100;
								int index = 0;
								while (count < size) {
									
									int t = dis.read();
									dos.write(t);
									count++;  //每次只读取一个字节

									if(num>0){
										if (count % num == 0 && index < 100) {
											ui.progressBar.setValue(++index);

										}
										ui.lblNewLabel.setText("上传进度:" + count + "/"
														+ size + "  整体" + index
														+ "%");
									}else{
										ui.lblNewLabel.setText("上传进度:" + count + "/"
												+ size +"  整体:"+new Double(new Double(count).doubleValue()/new Double(size).doubleValue()*100).intValue()+"%"
												);
										if(count==size){
											ui.progressBar.setValue(100);
										}
									}
								}
								dos.flush();
								dis.close();
							  //读取目标客户的提示保存完毕的信息...
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
