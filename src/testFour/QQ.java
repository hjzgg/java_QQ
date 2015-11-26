package testFour;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class QQ {
	public static final int PICUTER = 1; 
	public static final int FILE = 2; 
	public static final int PARAGRAPH = 3; 
	public static final int SEND = 1; 
	public static final int RECEIVE = 2; 
	public static final int FILEX = 3;
	
	public static int getUnusedPort() throws BindException{
		int port;
		for(port = 10000 ; port <= 65535; ++port){
			ServerSocket ss = null; 
			try {
				ss = new ServerSocket(port);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(ss != null) break;
		}
		if(port > 65535)
			throw new BindException("没有可用的端口！");
		return port;
	}
	
	public synchronized static void setTextPane(JTextPane tp, byte[] bt, int len, int flag, int tag){
		 ImageIcon myIcon = null;
		 if(tag == SEND)
			 myIcon = new ImageIcon("ff.jpg");
		 else if(tag == RECEIVE)
			 myIcon = new ImageIcon("gg.jpg");
		 else if(tag == FILEX)
			 myIcon = new ImageIcon("kk.jpg");
		 
		 SimpleAttributeSet set = new SimpleAttributeSet();
		 Document doc = tp.getStyledDocument();
		 
		 if(tag == SEND || tag == RECEIVE){
			  tp.setCaretPosition(doc.getLength());
			  JLabel fileLabel = new JLabel("", myIcon, JLabel.CENTER);
			  tp.insertComponent(fileLabel);
		 }
		 
		 if(flag == PARAGRAPH){
			 FontMetrics fm = tp.getFontMetrics(tp.getFont());
			 int paneWidth = tp.getWidth();
			 String text = new String(bt, 0, len);
			 
			 if( tag/3 == SEND ){
				 StyleConstants.setForeground(set, new Color(0,255,0));//设置文字颜色
				 StyleConstants.setFontSize(set, 15);//设置字体大小
			 }
			 else if( tag/3 == RECEIVE){
				 StyleConstants.setForeground(set, new Color(0,0,0));//设置文字颜色
				 StyleConstants.setFontSize(set, 15);//设置字体大小
			 }
			 
			 try
			  {
				  for(int i = 0, cnt = 0; i < text.length(); ++i){
					  if((cnt += fm.charWidth(text.charAt(i))) >= paneWidth){
						  cnt = 0;
						  doc.insertString(doc.getLength(), "\n", set);
						  continue;
					  }
					  doc.insertString(doc.getLength(), String.valueOf(text.charAt(i)), set);
				  }
				  doc.insertString(doc.getLength(), "\n", set);
				  
				  tp.setCaretPosition(doc.getLength());//最简单的设置滚动条的位置到最后输出文本的地方
				  									   //就是将JTextPane中的插入符的位置移动到文本的末端！
			  }
			  catch (BadLocationException e)
			  {
			  }
			 
		 } else if(flag == PICUTER) {
			
			 try{
					 InputStream is = new ByteArrayInputStream(bt, 0, len);
					 ImageInputStream iis = ImageIO.createImageInputStream(is);
					 Iterator<ImageReader> itImage = ImageIO.getImageReaders(iis);
					 if(itImage.hasNext()){
						 ImageReader reader = itImage.next();
						 ImageIcon ii = new ImageIcon( bt, reader.getFormatName() );
						 tp.setCaretPosition( doc.getLength() );
						 tp.insertComponent( new PicuterPanel(ii) );
						 doc.insertString(doc.getLength(), "\n", set);
					 }
			 }catch(IOException ex){
				     ex.printStackTrace();
			 }catch(BadLocationException e1){
				   
			 }
			 
		 } else if(flag == FILE) {
			  try{
				  tp.setCaretPosition(doc.getLength());
				  JLabel fileLabel = new JLabel(new String(bt, 0, len), myIcon, JLabel.LEFT);
				  tp.insertComponent(fileLabel);
				  doc.insertString(doc.getLength(), "\n", set);
			  }catch (BadLocationException e) {
				  e.printStackTrace();
			}
		 }
	}
}

class PicuterPanel extends JPanel{
	private ImageIcon ii;
	public PicuterPanel(ImageIcon ii){
		this.ii = ii;
		setPreferredSize(new Dimension(200, 300));
		setBackground(new Color(255, 255, 255));
	}
	
	protected void paintComponent(Graphics g) {
		  super.paintComponent(g);
		  g.drawImage(ii.getImage(), 0, 0, 200, 300, 0, 0, ii.getIconWidth(), ii.getIconHeight(), this);
	}
}
