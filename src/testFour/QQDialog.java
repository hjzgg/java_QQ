package testFour;

import java.awt.BorderLayout;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

class MyPanel extends JPanel{
	private int componentsHeight = 0;
	
	public int getComponentsHeight() {
		return componentsHeight;
	}

	public void setComponentsHeight(int componentsHeight) {
		this.componentsHeight = componentsHeight;
	}

	public void Mylayout(){
		
		if(componentsHeight >= 430)
			getComponent(getComponentCount()-1).setPreferredSize(new Dimension(300, 0));
		else 
			getComponent(getComponentCount()-1).setPreferredSize(new Dimension(300, 430-componentsHeight));
		updateUI();
	}
	
}

class MyLabel extends JLabel{
	private String groupName;
	private Map<String, QQFrame> QQmap = QQDialog.getMap();
	private Map<String, String> nameToIP = QQDialog.getNameToIP();
	private Set<String> QQset = QQDialog.getSet();
	private JPanel friendPanel;
	public void myInit(){
		
		setOpaque(true);//该组件绘制其边界内的所有像素。否则该组件可能不绘制其某些或所有像素，从而允许其下面的像素透视出来
		                //因为增添了mouseEntered事件响应和mouseExited事件响应
		setBackground(new Color(255, 255, 255));
		setPreferredSize(new Dimension(250, 50));
		addMouseListener(new MouseAdapter() {
		 	Color oldC = getBackground();
		 	public void mouseEntered(MouseEvent e) {
		 		 setBackground(new Color(0, 255, 0));
		 	}
		 	
		 	public void mouseExited(MouseEvent e) {
		 		 setBackground(oldC);
		 	}
		 	
		 	public void mouseClicked(MouseEvent e) {
		 		 //打开通信窗口
		 		 if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2){
		 			    String NameAndIP = getText();
				 	    String name = NameAndIP.substring(0, NameAndIP.indexOf(':'));
			 			if(QQmap .get(nameToIP.get(name)) != null){//这个好友的窗口已经存在！
			        		QQmap.get(nameToIP.get(name)).requestFocus();
			        	    return ;
		 				}
		 			    QQFrame fm = null;
						try{
							 String[] ipStr = nameToIP.get(name).split("\\.");//将字符串中的数字分离
					         byte[] ipBuf = new byte[4];//存储IP的byte数组
					         for(int i = 0; i < 4; i++){
					             ipBuf[i] = (byte)(Integer.parseInt(ipStr[i])&0xff);
					         }
							 fm = new QQFrame(name, ipBuf);
							 QQmap.put(nameToIP.get(name), fm);//name 和 窗口的映射！
						}catch(RuntimeException ex){
							JOptionPane.showMessageDialog(null, ex.getMessage(), "错误提示！", JOptionPane.OK_CANCEL_OPTION);
							return ;
						}
		 		 }
		 		 else if(e.getButton() == MouseEvent.BUTTON3){
		 			  JPopupMenu pm = new JPopupMenu("胡峻峥");
		 			  JMenuItem del = new JMenuItem("删除");
		 			  del.setFont(new Font("华文行楷", Font.ITALIC, 20));
		 			  del.setForeground(new Color(255, 0, 0));
		 			  del.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								int choose = JOptionPane.showConfirmDialog(null, "确定删除！", "删除对话框", JOptionPane.YES_NO_OPTION);
		 				  		if(choose == JOptionPane.OK_OPTION){
		 				  			friendPanel.remove(getParent());
		 				  			String NameAndIP = getText();
					 			 	String ip = NameAndIP.substring(NameAndIP.indexOf(':') + 1);
					 			 	String name = NameAndIP.substring(0, NameAndIP.indexOf(':'));
		 				  			QQset.remove(ip);
		 				  			QQFrame fm = QQmap.get(ip);
		 				  			if(fm != null) fm.dispose();
		 				  			nameToIP.remove(name);
		 				  			QQmap.remove(ip);
		 				  		
		 				  			MyPanel QQP = (MyPanel) friendPanel.getParent();
		 				  			QQP.setComponentsHeight(QQP.getComponentsHeight()-60);
		 				  			QQP.Mylayout();
		 				  			String[] sql = {"delete from QQTable where groupName=" + "\'" + groupName + "\' and" + " name=\'" + name + "\' and" + " ip=\'" + ip + "\'"};
		 				  			QQDialog.operateDB(sql);
		 				  		}
							}
					  });
		 			  
		 			  JMenuItem edit = new JMenuItem("编辑");
		 			  edit.addActionListener(new ActionListener() {
						
						  public void actionPerformed(ActionEvent e) {
								//得到之前的数据！
								String content = getText();
								String oldName = content.substring(0, content.indexOf(':'));
								String oldIP = content.substring(content.indexOf(':')+1);
								//从新设置到文本框中
								InputDialog id = new InputDialog(oldIP, oldName, groupName, true);
								if(id.key == InputDialog.CANCELBTN || id.key == 0) return;
								
								String newName = id.getNameText();
								String newIP = id.getIPText();
								String newGroup = id.getGROUPText();
								
								setText(newName + ":" + newIP);
								
								QQFrame fm = QQmap.get(oldIP);
								if(fm != null){
									try{
										fm.setSendIAD(newIP);
										
										QQmap.put(newIP, fm);
										fm.setTitle("我的好友 " + newName);
										QQmap.remove(oldIP);
										QQset.remove(oldIP);
										QQset.add(newIP);
										nameToIP.remove(oldName);
										nameToIP.put(newName, newIP);
										
									}catch(RuntimeException ex){
										JOptionPane.showMessageDialog(null, ex.getMessage(), "修改之后的IP！", JOptionPane.OK_OPTION);
									}
								}
								
								if(!oldName.equals(newName) || !oldIP.equals(newIP) || !groupName.equals(newGroup)){
									String[] sql = {
										"delete from QQTable where groupName=" + "\'" + groupName + "\' and" + " name=\'" + oldName + "\' and" + " ip=\'" + oldIP + "\'",
										"insert into QQTable values(" + "\'" + newGroup + "\'," + "\'" + newName + "\'," + "\'" + newIP + "\')"
									};
									QQDialog.operateDB(sql);
								}
								if(!groupName.equals(newGroup)){
									JPanel TarPan = QQDialog.getBtnToPanel().get(newGroup);
									
									friendPanel.remove(getParent());
									MyPanel QQP = (MyPanel) friendPanel.getParent();
									QQP.setComponentsHeight(QQP.getComponentsHeight()-60);
									QQP.Mylayout();
									
									TarPan.add(getParent());
									if(TarPan.isVisible()){
										QQP.setComponentsHeight(QQP.getComponentsHeight()+60);
										QQP.Mylayout();
									}
								}
							}
					  });
		 			  edit.setFont(new Font("华文行楷", Font.ITALIC, 20));
		 			  edit.setForeground(new Color(255, 0, 255));
		 			  pm.setBorderPainted(true);
		 			  pm.setBackground(new Color(125, 0, 125));
		 			  pm.add(del);  pm.add(edit);
		 			  pm.show(MyLabel.this, e.getX(), e.getY());
		 			  
		 			  
		 		 }
		 	}
		 	
	 });
	}
	
	public MyLabel(String arg0, Icon arg1, int arg2, String groupName) {
		super(arg0, arg1, arg2);
		this.groupName = groupName;
		friendPanel = QQDialog.getBtnToPanel().get(groupName);
		myInit();
	}

	public MyLabel(String text, String groupName) {
		super(text);
		this.groupName = groupName;
		friendPanel = QQDialog.getBtnToPanel().get(groupName);
		myInit();
	}

	public String getGroupName() {
		return groupName;
	}

}

class MyButton extends JButton {
	
	private JPanel groupPanel;
	private static MyPanel QQP;//滚动面板
	
	public static void setMyScollPane(MyPanel p){
		 QQP = p;
	} 
	
	public MyButton(JPanel p1) {
		groupPanel = p1;
		addMouseListener(new MouseAdapter() {
			
			public void mouseClicked(MouseEvent e) {//将好友面板进行收缩的效果！
				if(e.getButton() == MouseEvent.BUTTON1){//鼠标左键
						String groupName = getText();
						if(groupName.endsWith(">>")){
							groupPanel.setVisible(true);
							QQP.setComponentsHeight(QQP.getComponentsHeight() + groupPanel.getComponentCount()*60);
							setText(groupName.replace(">>", "<<"));
						}
						else{
							groupPanel.setVisible(false);
							QQP.setComponentsHeight(QQP.getComponentsHeight() - groupPanel.getComponentCount()*60);
							setText(groupName.replace("<<", ">>"));
						}
						QQP.Mylayout();
				} else if(e.getButton() == MouseEvent.BUTTON3){//鼠标右键, 编辑/删除分组
					  JPopupMenu pm = new JPopupMenu("胡峻峥");
		 			  JMenuItem del = new JMenuItem("删除");
		 			  del.setFont(new Font("华文行楷", Font.ITALIC, 20));
		 			  del.setForeground(new Color(255, 0, 0));
		 			  del.addActionListener(new ActionListener() {
		 				  	
							public void actionPerformed(ActionEvent e) {
								int choose = JOptionPane.showConfirmDialog(null, "确定删除该分组及分组中的好友？", "删除分组", JOptionPane.YES_NO_OPTION);
		 				  		if(choose == JOptionPane.OK_OPTION){
		 				  			String groupName = getText();
		 				  			groupName = groupName.substring(0, groupName.length()-2);
		 				  			JPanel friendPanel = QQDialog.getBtnToPanel().get(groupName);
		 				  			MyPanel QQP = (MyPanel) friendPanel.getParent();
		 				  			Set<String> QQset = QQDialog.getSet();
		 				  			Map<String, QQFrame> QQmap = QQDialog.getMap(); 
		 				  			Map<String, String> nameToIP = QQDialog.getNameToIP();
		 				  			Set<String> groupSet = QQDialog.getGroupSet();
		 				  			for(int i=0; i<friendPanel.getComponentCount(); ++i){
				 				  			JPanel tmp = (JPanel) friendPanel.getComponent(i);
				 				  			String NameAndIP = ((MyLabel)tmp.getComponent(0)).getText();
							 			 	String ip = NameAndIP.substring(NameAndIP.indexOf(':') + 1);
							 			 	String name = NameAndIP.substring(0, NameAndIP.indexOf(':'));
				 				  			QQset.remove(ip);
				 				  			QQFrame fm = QQmap.get(ip);
				 				  			if(fm != null) fm.dispose();
				 				  			nameToIP.remove(name);
				 				  			QQmap.remove(ip);
		 				  			}
		 				  			int h;
		 				  			if(friendPanel.isVisible()) h=30+friendPanel.getComponentCount()*60;
		 				  			else h = 30;
		 				  			//移除
		 				  			QQP.remove(MyButton.this.getParent());
		 				  			QQP.remove(friendPanel);
		 				  			groupSet.remove(groupName);
		 				  			QQP.setComponentsHeight(QQP.getComponentsHeight()-h);
		 				  			QQP.Mylayout();
		 				  			String[] sql = {"delete from QQTable where groupName=" + "\'" + groupName + "\'"};
		 				  			QQDialog.operateDB(sql);
		 				  		}
							}
					  });
		 			  
		 			  JMenuItem edit = new JMenuItem("编辑");
		 			  edit.addActionListener(new ActionListener() {
						
						  public void actionPerformed(ActionEvent e) {
								//得到之前的数据！
								String oldGroupName = getText();
								String tail = oldGroupName.substring(oldGroupName.length()-2, oldGroupName.length());
								oldGroupName = oldGroupName.substring(0, oldGroupName.length()-2);
								String newGroupName = (String)JOptionPane.showInputDialog(null, "新分组名:", "分组名修改", JOptionPane.INFORMATION_MESSAGE, null, null, oldGroupName);
								if(newGroupName == null || oldGroupName.equals(newGroupName)) return;
								Set<String> groupSet = QQDialog.getGroupSet();
								groupSet.remove(oldGroupName);
								groupSet.add(newGroupName);
								QQDialog.getBtnToPanel().put(newGroupName, ((JPanel)QQDialog.getBtnToPanel().remove(oldGroupName)));
								setText(newGroupName+tail);
							}
					  });
		 			  edit.setFont(new Font("华文行楷", Font.ITALIC, 20));
		 			  edit.setForeground(new Color(255, 0, 255));
		 			  pm.setBorderPainted(true);
		 			  pm.setBackground(new Color(125, 0, 125));
		 			  pm.add(del);  pm.add(edit);
		 			  pm.show(MyButton.this, e.getX(), e.getY());
				}
			}
		});
	}
}

class InputDialog extends Dialog{
	private JLabel ipLabel = new JLabel("IP地址:");
	private JLabel nameLabel = new JLabel("姓名:");
	private JLabel groupLabel = new JLabel("分组:");
	private JButton okBtn = new JButton("确定");
	private JButton cleBtn = new JButton("取消");
	private JTextField ipText = new JTextField(20);
	private JTextField nameText = new JTextField(20);
	private Choice groupChoice = new Choice();
	private static final int OKBTN = 1;
	public static final int CANCELBTN = 2;
	public int key = 0;
	private String IP = null, NAME = null, GROUP = null; 
	private boolean flag = false;
	void initDialog(String selectName){
	    
		JPanel p = null;
		setModal(true);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		add(p = new JPanel());
		p.setLayout(new FlowLayout(FlowLayout.CENTER));
		ipLabel.setPreferredSize(new Dimension(50, 12));
		p.add(ipLabel); p.add(ipText);
		
		
		add(p = new JPanel());
		p.setLayout(new FlowLayout(FlowLayout.CENTER));
		nameLabel.setPreferredSize(new Dimension(50, 12));
		p.add(nameLabel); p.add(nameText);
		
		Set groupSet = QQDialog.getGroupSet();
		Iterator it = groupSet.iterator();
		while(it.hasNext())
			groupChoice.add((String)it.next());
		groupChoice.select(selectName);
		add(p = new JPanel());
		p.setLayout(new FlowLayout(FlowLayout.CENTER));
		groupLabel.setPreferredSize(new Dimension(50, 12));
		groupChoice.setPreferredSize(new Dimension(225, 12));
		p.add(groupLabel); p.add(groupChoice); 
		
		add(p = new JPanel());
		p.setLayout(new FlowLayout(FlowLayout.CENTER));
		p.add(okBtn); p.add(cleBtn);
		
		
		addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					 key = 0;
					 dispose();
				}
		});
		
		addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					 requestFocus();
				}
		});
		
		okBtn.addMouseListener(new MouseAdapter() {
				private Set<String> QQset = QQDialog.getSet();

				public void mouseClicked(MouseEvent e) {
					 key = InputDialog.OKBTN;
					 if("".equals(ipText.getText()) || "".equals(nameText.getText()) || !checkIP(ipText.getText()) )
							 JOptionPane.showMessageDialog(null, "信息不全或者IP填写错误!");
					 else{
						 String ip = ipText.getText();
						 if(!flag && QQset.contains(ip)){
							 JOptionPane.showMessageDialog(null, "好友已存在！", "QQ好友", JOptionPane.OK_OPTION);
							 return ;
						 }
						 IP = ipText.getText();
						 NAME = nameText.getText();
						 GROUP = groupChoice.getSelectedItem();
						 dispose();//释放Dialog资源
					 }
				}
		});
		
		cleBtn.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					key = InputDialog.CANCELBTN;
					dispose();
				}
		});
		setSize(300, 200);
		setResizable(false);
		setLocation(200, 200);
		setVisible(true);
	}
	
	public InputDialog(String ip, String name, String groupName, boolean flag){
		super(new Frame());
		ipText.setText(ip);
		nameText.setText(name);
		this.flag = flag;
		initDialog(groupName);
	}
	
	public InputDialog(){
		super(new Frame());
		initDialog("我的好友");
	}
		
	public boolean checkIP(String ip){
		int i, begin = 0, end;
		for(i = 1; i <= 4; ++i){
			end = ip.indexOf('.', begin);
			if(end == -1) return false;
			int p = Integer.valueOf(ip.substring(begin, end));
			if(p < 0 || p > 255)  return false;
		}
		return true;
	}
	
	public String getGROUPText() {
		return GROUP;
	}

	public String getIPText(){
		return IP;
	}
	
	public String getNameText(){
		return NAME;
	}
	
}

public class QQDialog extends Frame{
		private MyPanel QQP = new MyPanel();
		private JPanel funP = new JPanel();
		private JButton add = new JButton("添加好友");
		private JButton newGroup = new JButton("新建分组");
		private JButton serach = new JButton("查询好友");
		private JScrollPane jsp = new JScrollPane(QQP);
		private static Set<String>QQset;//IP的集合
		private static Map<String, QQFrame>QQmap;//IP到对通信窗口的映射
		private static Map<String, String>nameToIP;//姓名到IP的映射
		private static Set<String>groupSet;//分组的集合

		private static Map<String, JPanel>btnToPanel;//分组菜单到面板的映射
		private int groupCnt = 0;
		 
		ImageIcon[] ii = new ImageIcon[3];
		
		public static Map<String, QQFrame> getMap(){
			return QQmap;
		}
		
		public static Set<String> getGroupSet() {
			return groupSet;
		}
		
		public static Set<String> getSet(){
			return QQset;
		}
		
		public static Map<String, String> getNameToIP(){
			return nameToIP;
		}
		public static Map<String, JPanel> getBtnToPanel(){
			return btnToPanel;
		}
		
		private void initPara(){
			QQset = new TreeSet<String>();
			QQmap = new TreeMap<String, QQFrame>();
			nameToIP = new TreeMap<String, String>();
			btnToPanel = new TreeMap<String, JPanel>();
			groupSet = new TreeSet<String>();
			MyButton.setMyScollPane(QQP);
			
			ii[0] = new ImageIcon("ff.jpg");
			ii[1] = new ImageIcon("gg.jpg");
			ii[2] = new ImageIcon("kk.jpg");
		}
		
		private void initDatabase(){
			initPara();
			Connection con = null;
			Statement sta = null;
			ResultSet rt = null;
			Statement stax = null;
			ResultSet rtx = null;
			try {
				Class.forName("org.sqlite.JDBC");
				con = DriverManager.getConnection("jdbc:sqlite:QQDatabase.db3");
				sta = con.createStatement();
				rt = sta.executeQuery("SELECT groupName FROM QQTable GROUP BY groupName");
				 
				while(rt.next()){
					String groupName = rt.getString("groupName");
					JPanel pan = new JPanel();
					pan.setLayout(new BoxLayout(pan, BoxLayout.Y_AXIS));
					
					MyButton btn = new MyButton(pan);
					JPanel pbtn = new JPanel();
					btn.setText(groupName+">>");
					pbtn.setLayout(new BorderLayout());
					pbtn.add(btn, "Center");
					pbtn.setPreferredSize(new Dimension(300, 30));//按钮的高度
					QQP.add(pbtn);
					++groupCnt;//第几个分组
					groupSet.add(groupName);
					btnToPanel.put(groupName, pan);//分组按钮到面板的映射
					stax = con.createStatement();
					rtx = stax.executeQuery("SELECT * FROM QQTable WHERE groupName=" + "\'" + groupName + "\'");//该分组的查询
					while(rtx.next()){
						JPanel tmp = new JPanel();
						tmp.setLayout(new FlowLayout(FlowLayout.CENTER));
						tmp.setBackground(new Color(205, 21, 0));
						tmp.setPreferredSize(new Dimension(300, 60));//标签的高度
						String name = rtx.getString("name");
						String ip = rtx.getString("ip");
						
						nameToIP.put(name, ip);
						QQset.add(ip);
						
						int k;
						Random rd = new Random();
						while((k=rd.nextInt())<0);
						k %= 3;
						MyLabel lab = new MyLabel(name + ":" + ip, ii[k], MyLabel.LEFT, groupName);
						tmp.add(lab);
						pan.add(tmp);
					}
					pan.setVisible(false);
					QQP.add(pan);
				}
				QQP.add(new JPanel());//末尾面板
				QQP.setComponentsHeight(groupCnt*30);
				QQP.Mylayout();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				try{
					if(rt !=null) rt.close();
					if(rtx !=null) rt.close();
					if(sta != null) sta.close();
					if(stax != null) sta.close();
					if(con != null) con.close();
				} catch(SQLException e){
					e.printStackTrace();
				}
			}
			
		}
		
		public static void operateDB(String[] sql){
			Connection con = null;
			Statement sta = null;
			try {
				Class.forName("org.sqlite.JDBC");
				con = DriverManager.getConnection("jdbc:sqlite:QQDatabase.db3");
				sta = con.createStatement();
				for(int i=0; i<sql.length; ++i)
					sta.executeUpdate(sql[i]);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				try{
					if(sta != null) sta.close();
					if(con != null) con.close();
				} catch(SQLException e){
					e.printStackTrace();
				}
			}
			
		}
		
		public QQDialog(){
			
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			jsp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			jsp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			jsp.setPreferredSize(new Dimension(300, 450));
			QQP.setLayout(new BoxLayout(QQP, BoxLayout.Y_AXIS));
			initDatabase();
			add(jsp);
			add(funP);
			funP.setLayout(new FlowLayout(FlowLayout.CENTER));
			funP.add(add);
			funP.add(serach);
			funP.add(newGroup);
			
			add.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						InputDialog dlg = new InputDialog();
						if(dlg.key == InputDialog.CANCELBTN || dlg.key== 0) return;
						Random rd = new Random();
						int index = Math.abs(rd.nextInt()) % 3;
						MyLabel ll = new MyLabel(dlg.getNameText() + ":" + dlg.getIPText(), ii[index], JLabel.LEFT, dlg.getGROUPText());
						QQset.add(dlg.getIPText());//将新增加的好友IP添加到集合中！
						nameToIP.put(dlg.getNameText(), dlg.getIPText());
						JPanel tmp = new JPanel();
						JPanel pan = btnToPanel.get(dlg.getGROUPText());//得到改好友要添加到的面板
						tmp.setLayout(new FlowLayout(FlowLayout.CENTER));
						tmp.setPreferredSize(new Dimension(300, 60));
						tmp.setBackground(new Color(205, 21, 0));
					    tmp.add(ll);
						/*
						 *  BoxLayout布局简直是蛋疼的要命，一个面板X是BoxLayout布局，如果该面板添加一个面板Y的话
						 *  那么Y就会填充X面板！如果在添加一个面板Z， 那么Y, Z就会一起布满X面板！但是可以设置Y,Z面板
						 *  的比例！ 如果X添加的是一个按钮或者标签时，还不能控制其大小.....无语了！
						 *  
						 *  下面的我的做法将标签添加到面板tmp中，然后再将tmp添加中QQP面板中！这样就可以控制标签的大小了！
						 *  再添加新的面板的时候，要设置一下之前面板的PreferredSize！保证每一个标签的距离适中！
						 *  也就是保证所有的添加的面板的高度之和 == QQP.getHeight();
						 * */
						
						pan.add(tmp);
						if(pan.isVisible()){
							QQP.setComponentsHeight(QQP.getComponentsHeight()+60);
							QQP.Mylayout();
							//JScrollBar jsb = jsp.getVerticalScrollBar();
							//终于搞好了，将垂直滚动条自动的移动到最低端
							//jsp.getViewport().setViewPosition(new Point(0, jsp.getVerticalScrollBar().getMaximum()));
						}
						
						//数据库的更新：
						String[] sql = {"insert into QQTable values(" + "\'" + dlg.getGROUPText() + "\'," + "\'" + dlg.getNameText() + "\'," + "\'" + dlg.getIPText() + "\')"};
						operateDB(sql);
					}
			});
			
			serach.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						  String name = JOptionPane.showInputDialog(null, "好友姓名", "好友查询", JOptionPane.OK_OPTION);
						  if(name == null)  return ;
						  
						  String ip = nameToIP.get(name);
						  
						  if(ip == null)
							  	JOptionPane.showMessageDialog(null, "好友不存在！", "查询结果", JOptionPane.OK_OPTION);
						  else{
							  QQFrame fm = QQmap.get(ip);
							  if(fm == null){
								  try{
									     String[] ipStr = ip.split("\\.");//将字符串中的数字分离
								         byte[] ipBuf = new byte[4];//存储IP的byte数组
								         for(int i = 0; i < 4; i++){
								             ipBuf[i] = (byte)(Integer.parseInt(ipStr[i])&0xff);
								         }
										 fm = new QQFrame(name, ipBuf);
									}catch(RuntimeException ex){
										 JOptionPane.showMessageDialog(null, ex.getMessage(), "Socket错误！", JOptionPane.OK_CANCEL_OPTION);
										 return ;
									}
								  QQmap.put(ip, fm);
							  }
							  else fm.requestFocus();
						  }
					}
			});
			
			newGroup.addMouseListener(new MouseAdapter() {

				public void mouseClicked(MouseEvent e) {
					String groupName = JOptionPane.showInputDialog(null, "分组名称", "新建分组", JOptionPane.OK_OPTION);
					if(groupName == null)  return ;
					if(groupSet.contains(groupName)){
						JOptionPane.showMessageDialog(null, "分组已存在！", "错误提示", JOptionPane.OK_CANCEL_OPTION);
						return ;
					}
					
					JPanel pan = new JPanel();
					pan.setLayout(new BoxLayout(pan, BoxLayout.Y_AXIS));
					pan.setVisible(false);
					MyButton btn = new MyButton(pan);
					JPanel pbtn = new JPanel();
					btn.setText(groupName+">>");
					pbtn.setLayout(new BorderLayout());
					pbtn.add(btn, "Center");
					pbtn.setPreferredSize(new Dimension(300, 30));//按钮的高度
					QQP.add(pbtn, QQP.getComponentCount()-1);
					QQP.add(pan, QQP.getComponentCount()-1);
					++groupCnt;//第几个分组
					groupSet.add(groupName);//添加到分组的集合
					QQDialog.getBtnToPanel().put(groupName, pan);
					QQP.setComponentsHeight(QQP.getComponentsHeight()+30);
					QQP.Mylayout();
				}
				
			});
			
			addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent e) {
						if(qr == null) System.out.println("hehe");
						qr.setFlag();//udp服务线程停止
						ss.setFlag();//tcp服务线程停止
						System.exit(0);
					}
			});
			
			QQP.setBackground(new Color(255, 0, 255));
			funP.setBackground(new Color(255, 255, 0));
			setResizable(false);
			setSize(300, 500);
			setLocation(500, 200);
			setVisible(true);
		}
		
		private static ServerSocketQQ ss = null;
		private static QQReceive qr = null;
		
		public static void main(String[] args){

 			ss = new ServerSocketQQ();
 			new Thread(ss).start();
 			if(ServerSocketQQ.getPort() < 1) return;
 			
 			qr = new QQReceive();
 			new Thread(qr).start();
 			if(QQReceive.getPort() < 1)  return ;
 			
			new QQDialog();
		}
}
