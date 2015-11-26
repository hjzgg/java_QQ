package testFour;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Calendar;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

public class Upload implements Runnable {
	private Socket st;
	public Upload(Socket st){
		this.st = st;
	}
	
	public void run() {
		try{
			byte[] bt = null;
			int len = 0;
			String name = null;
			InputStream is = st.getInputStream();
			bt = new byte[1024];
			
			len = is.read(bt);
		    String ip = new String(bt, 0, len);
			QQFrame fm = null;
			Map<String, QQFrame>mp = QQDialog.getMap();
			Set<String>  set = QQDialog.getSet();
			fm = mp.get(ip);
			if(set.contains(ip) && fm == null){//如果存在该好友，但是不存在对应的对话窗口
				 //自动产生对话窗口
				 Map<String, String> nameToIP = QQDialog.getNameToIP();
				 String[] ipStr = ip.split("\\.");//将字符串中的数字分离
		         byte[] ipBuf = new byte[4];//存储IP的byte数组
		         for(int i = 0; i < 4; i++){
		             ipBuf[i] = (byte)(Integer.parseInt(ipStr[i])&0xff);
		         }
		         for(String ss : nameToIP.keySet())
		        	 if( ip.equals(nameToIP.get(ss)) ){
		        		   name = ss;
		        		   break;
		        	 }
		         fm = new QQFrame(name, ipBuf);
				 mp.put(ip, fm);
			}
			if(fm == null)//对方不存在该好友！
				return ;
			
			len = is.read(bt);
		    String fileName = new String(bt, 0, len);
			
			int choose = JOptionPane.showConfirmDialog(null, "接受或不接受", "文件传输提示", JOptionPane.YES_NO_OPTION);
			if(choose == JOptionPane.NO_OPTION){
				bt = new String(Calendar.getInstance().getTime().toString() + ":文件接受失败！").getBytes();
				QQ.setTextPane(fm.getReceive(), bt, bt.length, QQ.FILE, QQ.FILEX);
				return ;
			}
			FileOutputStream fos = new FileOutputStream( new File(fileName) );
			while( (len = is.read(bt)) != -1 ){//先将流文件变成byte[], 然后利用套接字的输出流发送给客户端
				 fos.write(bt, 0, len);
				 fos.flush();
			}
			bt = new String(Calendar.getInstance().getTime().toString() + ":文件接受成功！").getBytes();
			QQ.setTextPane(fm.getReceive(), bt, bt.length, QQ.FILE, QQ.FILEX);
		} catch(FileNotFoundException e){
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			try{
				st.close();
			} catch(IOException e){
				e.printStackTrace();
			}
		}
	}

}
