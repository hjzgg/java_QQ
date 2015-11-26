package testFour;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JOptionPane;

public class ServerSocketQQ implements Runnable{
	private ServerSocket sst = null;
	private Socket st = null;
	private static int port = 10108;
	private boolean flag = true;
	
	public void setFlag(){
		if(sst != null)
			try {
				sst.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		flag = false;
	}
	
	public static int getPort(){
		return port;
	}
	
	public ServerSocketQQ(){
		//建立服务端
		try {
			sst = new ServerSocket(port);
		} catch (IOException e) {
			port = -1;
			JOptionPane.showMessageDialog(null, "ServerSocket端口绑定错误！", "绑定错误", JOptionPane.OK_CANCEL_OPTION);
		}
		
	}
	
	public void run(){
		if(sst == null)  return ;
		while(true){
			try{
				//侦听并接受到此服务套接字的连接。此方法在进行连接之前一直阻塞。 创建新套接字 
				st = sst.accept();
				//得到客户端传输过来的流
				new Thread(new Upload(st)).start();//开启一个新的线程进行处理
		   }catch(IOException e){
			   e.printStackTrace();
		   }
		}
	 }
	
}
