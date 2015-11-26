package testFour;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Label;
import java.awt.TextArea;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.filechooser.FileFilter;



public class QQFrame extends Frame{
	 private TextArea taSend = new TextArea();
	 private JTextPane taReceive = new JTextPane();
	 private JScrollPane p = new JScrollPane(taReceive);
	 private JPanel pSend = new JPanel();
	 private JPanel pReceive = new JPanel();
	 private Label laSend = new Label("发送端.....");
	 private Label laReceive = new Label("接收端.....");
	 private JButton FileBtn = new JButton("传输文件");
	 private JButton PicuterBtn = new JButton("发送图片");
	 private InetAddress sendIAD = null;//当前窗口所对应的好友所在机器的IP对象
	 private String QQname = null;//该对话框所对应的好友的姓名
	 private Socket st =null;
	 private String text;
	 private DatagramSocket ds = null;
	
	 public QQFrame(String name, byte[] ipBuf) throws RuntimeException{
		 try {
			sendIAD = InetAddress.getByAddress(ipBuf);
		 } catch (UnknownHostException e3) {
			throw new RuntimeException("IP错误（不存在或无发链接）！");
		 }
		 
		 ds = QQReceive.getUdpSocket();
		 if(ds == null)  throw new RuntimeException("udp Socket出错！");
		 
		 QQname = name;
		 text = "";
		 setSize(600, 600);
		 setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		 pSend.setLayout(new FlowLayout(FlowLayout.LEFT));
		 pSend.add(laSend);
		 pSend.add(FileBtn);
		 pSend.add(PicuterBtn);
		 pReceive.setLayout(new FlowLayout(FlowLayout.LEFT));
		 pReceive.add(laReceive);
		 
		 taReceive.setForeground(new Color(255, 0, 255));
		 add(pReceive);
		 add(p);
		 add(pSend);
		 add(taSend);
		 setTitle("我的好友 " + QQname);
		 
		 
		 taSend.setPreferredSize(new Dimension(0, 200));
		 taReceive.setPreferredSize(new Dimension(0, 400));
		 
		 taSend.setFont(new Font("仿宋", Font.PLAIN, 20));
		 taReceive.setFont(new Font("黑体", Font.PLAIN, 25));
		 
		 taReceive.setEditable(false);//不能进行文本的编辑，但是可以进行访问
		 
		 taSend.addKeyListener(new KeyAdapter() {
			
			 public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER){
					 text = taSend.getText();
					 if(text == null) return;
					 text += "\n\n";
					 byte[] bt = text.getBytes();
					 DatagramPacket dp = null;
					 try {
						 //向指定的ip和端口发送数据~！
						 //先说明一下数据是谁发送过来的！
						 byte[] ip = InetAddress.getLocalHost().getHostAddress().getBytes();
						 dp = new DatagramPacket(ip, ip.length, sendIAD, QQReceive.getPort());
						 ds.send(dp);
						 try {
							Thread.sleep(100);
						 } catch (InterruptedException e1) {
						 }
						 
						 dp = new DatagramPacket("PARAGRAPH".getBytes(), "PARAGRAPH".getBytes().length, sendIAD, QQReceive.getPort());
						 ds.send(dp);
						 
						 try {
								Thread.sleep(100);
						 } catch (InterruptedException e1) {
							 
						 }
						 
						 dp = new DatagramPacket(bt, bt.length, sendIAD, QQReceive.getPort());
						 ds.send(dp);
					 } catch (IOException e1) {
						e1.printStackTrace();
					 }
					 
					 synchronized(QQ.class){//发送端向接收窗口添加数据时 要 和 接收端向接收窗口添加数据时同步！
						 byte[] x = null;
						 try {
							x = new String(InetAddress.getLocalHost().getHostName() + " : ").getBytes();
						 } catch (UnknownHostException e1) {
							e1.printStackTrace();
						 }
						 QQ.setTextPane(taReceive, x, x.length, QQ.PARAGRAPH, QQ.SEND);
						 x = text.getBytes();
						 QQ.setTextPane(taReceive, x, x.length, QQ.PARAGRAPH, QQ.SEND*3);
						 taSend.requestFocus();
					 }
					 
					 taSend.setText("");//发送端清空
					 e.consume();//不让这个回车字符在输入端显示！
					 return ;
				}
			}
			 
		 });
		 
		 addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {
				Map<String, QQFrame>mp = QQDialog.getMap();
				mp.remove(sendIAD.getHostAddress());
				dispose();
			}
			 
		 });
		 
		 FileBtn.addMouseListener(new MouseAdapter() {//文件传输
				public void mouseClicked(MouseEvent e) {
					 JFileChooser jfc = new JFileChooser();
					 jfc.showOpenDialog(null);
					 File fl = jfc.getSelectedFile();
					 if(fl == null) return ;
					 try {
						st =  new Socket();//尝试连接对方
						st.connect(new InetSocketAddress(sendIAD, ServerSocketQQ.getPort()), 1000);
					 } catch (IOException e2) {
						st = null;
						JOptionPane.showMessageDialog(null, "请求超时或连接错误！", "ServerSocket", JOptionPane.OK_CANCEL_OPTION);
					 }
					 if(st != null){
						try {
							byte[] bt = new byte[1024];
							InputStream is = st.getInputStream(); 
							OutputStream os = st.getOutputStream();
						    //先说明一下是谁发送过来的！
							byte[] ip = InetAddress.getLocalHost().getHostAddress().getBytes();
							os.write(ip);
							os.flush();
							
							try {
								Thread.sleep(100);
							} catch (InterruptedException e1) {
							 
							}
							
							//向对方首先发送文件名， 然后发送文件内容！
							os.write(fl.getName().getBytes());
							os.flush();
							
							try {
								Thread.sleep(100);
							} catch (InterruptedException e1) {
							 
							}
							
							int len;
							InputStream fis = new FileInputStream(fl);
							while( (len = fis.read(bt)) != -1){
								os.write(bt, 0, len);
								os.flush();
							}
							//st.shutdownOutput();//输出流结束，并标记一下，使服务端知道客户端输出已经结束了！
							st.close();
							bt = new String(Calendar.getInstance().getTime().toString() + ":文件已传输！").getBytes();
							QQ.setTextPane(taReceive, bt, bt.length, QQ.FILE, QQ.FILEX);
						
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					 }
				}
		 });
		 
		 PicuterBtn.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					JFileChooser jfc = new JFileChooser();
					jfc.setFileFilter(new PicuterFilter());//设置当前的文件过滤器！
					jfc.setAcceptAllFileFilterUsed(false);//设置所有文件过滤器不使用！
					//jfc.addChoosableFileFilter(new Filter());
					jfc.showOpenDialog(null);
					
				   //将输入流按照下面方式处理， 根据Iterator<ImageReader> itImage是否能
				   //成功的返回一个ImageReader对象确认该流文件是否是一个图片文件！
				   //并ImageReader类中的getFormatName（）得到文件的格式！
				   //通过最后可以通过ImageIcon的byte[]构造函数建立ImageIcon对象！
				   //最后将图片显示在面板上！
					File fl = jfc.getSelectedFile();
					if(fl == null) return ;
					try{
							 InputStream is = new FileInputStream(fl);
							 ImageInputStream iis = ImageIO.createImageInputStream(is);
							 Iterator<ImageReader> itImage = ImageIO.getImageReaders(iis);
							 if(itImage.hasNext()){
								  ImageReader reader = itImage.next();
								  byte[] imageByte = new byte[1024*64];
								  int len = iis.read(imageByte);
								  if(len > 64 * 1000){
									  JOptionPane.showMessageDialog(new Frame(), "图片过大！请采用文件传输！");
									  return ;
								  }
								  DatagramPacket dp = null;
								  //先说明一下数据是谁发送过来的！
								  byte[] ip = InetAddress.getLocalHost().getHostAddress().getBytes();
								  dp = new DatagramPacket(ip, ip.length, sendIAD, QQReceive.getPort());
								  ds.send(dp);
								  
								  try {
										Thread.sleep(100);
								  } catch (InterruptedException e1) {
								  }
								  
								  dp = new DatagramPacket("PICUTER".getBytes(), "PICUTER".getBytes().length, sendIAD, QQReceive.getPort());
								  ds.send(dp);
								  
								  try {
										Thread.sleep(100);
								  } catch (InterruptedException e1) {
								  }
								  
								  dp = new DatagramPacket(imageByte, len, sendIAD, QQReceive.getPort());
								  ds.send(dp);
								  synchronized(QQ.class){
									  byte[] name = null;
									  name = new String(InetAddress.getLocalHost().getHostName() + " : ").getBytes();
									  QQ.setTextPane(taReceive, name, name.length, QQ.PARAGRAPH, QQ.SEND);
									  QQ.setTextPane(taReceive, imageByte, len, QQ.PICUTER, 0);
								  }
							 }
							 else throw new NoPicuterException("不是一张图片！");
					}catch(IOException ex){
						ex.printStackTrace();
					}
				}
		 });
		 
		 setVisible(true);
	 }
	 
	 public void setSendIAD(String ip) throws RuntimeException{
		 String[] ipStr = ip.split("\\.");//将字符串中的数字分离
         byte[] ipBuf = new byte[4];//存储IP的byte数组
         for(int i = 0; i < 4; i++){
             ipBuf[i] = (byte)(Integer.parseInt(ipStr[i])&0xff);
         }
         try {
 			sendIAD = InetAddress.getByAddress(ipBuf);
 		 } catch (UnknownHostException e3) {
 			throw new RuntimeException("IP错误（不存在或无发链接）！");
 		 }
	 }
	 
	 public DatagramSocket getDs(){
		 return ds;
	 }
	 
	 public JTextPane getReceive(){
		 return taReceive;
	 }
	 
}

class PicuterFilter extends FileFilter {
	 
	public boolean accept(File file){
		   return(file.isDirectory() || file.getName().endsWith(".gif") 
				  || file.getName().endsWith(".png") || file.getName().endsWith(".bmp")
				  || file.getName().endsWith(".jpg") );
		   /* 返回要显示的文件类型 */
		   /*
		    *   File.isDirectory()测试此抽象路径名表示的文件是否是一个目录
		   */
	  }
	  
	  public String getDescription() {
		  return("Picuter Files(*.gif, *.png, *.jpg, *.bmp)");                  //返回显示文件类型的描述
	  }
}

class NoPicuterException extends IOException{
	public NoPicuterException(String x){
		super(x);
	}
}
