package GUI;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultCaret;
import javax.swing.undo.UndoManager;

public class MainFrame extends JFrame
{
	private static final long serialVersionUID = 1L;
	private JMenu[] menus;
	private JMenuItem[] fileItems;
	private JMenuItem[] editItems;
	private JMenuItem[] compileItems;
	private JButton newItems;
	private JButton openItems;
	private JButton saveItems;
	private JButton saveAsItems;
	private JButton copyItems;
	private JButton pasteItems;
	private JButton undoItems;
	private JButton redoItems;
	private JButton compilerItems;
	private JButton compilerAsItems;
	private JButton applyItems;
	private JTabbedPane tb;
	private JTextArea textArea;
	private HashMap<JPanel, String> pathDic = new HashMap<JPanel, String>();
	private Vector<JPanel> contentlist = new Vector<JPanel>();
	private Vector<PopupTextPane> textlist = new Vector<PopupTextPane>();
	private Vector<UndoManager> um = new Vector<UndoManager>();
	
	public MainFrame()
	{
		super();
		setTitle("IDL模板编辑器");
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.setSize(700, 600);
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		
		this.addWindowListener(new windowHandler());
		menus = new JMenu[] { new JMenu("文件"), new JMenu("编辑"), new JMenu("编译") };
		fileItems = new JMenuItem[] { new JMenuItem("新建"), new JMenuItem("打开"), new JMenuItem("保存"),
				new JMenuItem("另存为"), new JMenuItem("退出") };
		editItems = new JMenuItem[] { new JMenuItem("复制"), new JMenuItem("粘贴"), new JMenuItem("撤销"), new JMenuItem("恢复") };
		compileItems = new JMenuItem[] { new JMenuItem("使用选中模板编译idl文件"), new JMenuItem("使用当前模板编译idl文件"), new JMenuItem("应用模板") };
		ImageIcon icon0 = new ImageIcon(this.getClass().getClassLoader().getResource("image/plus.png"));
		Image image0 = icon0.getImage().getScaledInstance(15,15,Image.SCALE_FAST);
		icon0 = new ImageIcon(image0);
		ImageIcon icon1 = new ImageIcon(this.getClass().getClassLoader().getResource("image/open.png"));
		Image image1 = icon1.getImage().getScaledInstance(15,15,Image.SCALE_FAST);
		icon1 = new ImageIcon(image1);
		ImageIcon icon2 = new ImageIcon(this.getClass().getClassLoader().getResource("image/save.png"));
		Image image2 = icon2.getImage().getScaledInstance(15,15,Image.SCALE_FAST);
		icon2 = new ImageIcon(image2);
		ImageIcon icon3 = new ImageIcon(this.getClass().getClassLoader().getResource("image/save-as.png"));
		Image image3 = icon3.getImage().getScaledInstance(15,15,Image.SCALE_FAST);
		icon3 = new ImageIcon(image3);
		ImageIcon icon4 = new ImageIcon(this.getClass().getClassLoader().getResource("image/copy.png"));
		Image image4 = icon4.getImage().getScaledInstance(15,15,Image.SCALE_FAST);
		icon4 = new ImageIcon(image4);
		ImageIcon icon5 = new ImageIcon(this.getClass().getClassLoader().getResource("image/paste.png"));
		Image image5 = icon5.getImage().getScaledInstance(15,15,Image.SCALE_FAST);
		icon5 = new ImageIcon(image5);
		ImageIcon icon6 = new ImageIcon(this.getClass().getClassLoader().getResource("image/undo.png"));
		Image image6 = icon6.getImage().getScaledInstance(15,15,Image.SCALE_FAST);
		icon6 = new ImageIcon(image6);
		ImageIcon icon7 = new ImageIcon(this.getClass().getClassLoader().getResource("image/redo.png"));
		Image image7 = icon7.getImage().getScaledInstance(15,15,Image.SCALE_FAST);
		icon7 = new ImageIcon(image7);
		ImageIcon icon8 = new ImageIcon(this.getClass().getClassLoader().getResource("image/compile.png"));
		Image image8 = icon8.getImage().getScaledInstance(15,15,Image.SCALE_FAST);
		icon8 = new ImageIcon(image8);
		ImageIcon icon9 = new ImageIcon(this.getClass().getClassLoader().getResource("image/compile-as.png"));
		Image image9 = icon9.getImage().getScaledInstance(15,15,Image.SCALE_FAST);
		icon9 = new ImageIcon(image9);
		ImageIcon icon10 = new ImageIcon(this.getClass().getClassLoader().getResource("image/apply.png"));
		Image image10 = icon10.getImage().getScaledInstance(15,15,Image.SCALE_FAST);
		icon10 = new ImageIcon(image10);
		newItems = new JButton(icon0);
		openItems = new JButton(icon1);
		saveItems = new JButton(icon2);
		saveAsItems = new JButton(icon3);
		copyItems = new JButton(icon4);
		pasteItems = new JButton(icon5);
		undoItems = new JButton(icon6);
		redoItems = new JButton(icon7);
		compilerItems = new JButton(icon8);
		compilerAsItems = new JButton(icon9);
		applyItems = new JButton(icon10);
		newItems.setToolTipText("新建");
		openItems.setToolTipText("打开");
		saveItems.setToolTipText("保存");
		saveAsItems.setToolTipText("另存为");
		copyItems.setToolTipText("复制");
		pasteItems.setToolTipText("粘贴");
		undoItems.setToolTipText("撤销");
		redoItems.setToolTipText("恢复");
		compilerItems.setToolTipText("编译(选中模板)");
		compilerAsItems.setToolTipText("编译(当前模板)");
		applyItems.setToolTipText("应用模板");
		JMenuBar jm = initMenus();
		JToolBar jt = initTools();
		this.setJMenuBar(jm);
		JPanel jp = new JPanel();
		jp.setLayout(new BorderLayout());
		jp.add("West",jt);
		tb = new JTabbedPane();
		textArea = new JTextArea();
		textArea.setForeground(Color.RED);
		textArea.setEditable(false);
		textArea.addMouseListener(new MouseAdapter(){
			public void mouseEntered(MouseEvent mouseEvent){
				textArea.setCursor(new Cursor(Cursor.TEXT_CURSOR));
			}
			
			public void mouseExited(MouseEvent mouseEvent){
				textArea.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		});
		textArea.getCaret().addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e){
				textArea.getCaret().setVisible(true);
			}
		});
		JScrollPane jsp = new JScrollPane(textArea);
		jsp.setPreferredSize(new Dimension(500,100));
		jsp.setBorder(new LineBorder(new Color(217,217,217),1));
		DefaultCaret caret = (DefaultCaret)textArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		JPanel jp2 = new JPanel();
		jp2.setLayout(new BorderLayout(1,5));
		jp2.add("Center",tb);
		jp2.add("South", jsp);
		jp.add("Center",jp2);
		this.setContentPane(jp);
		jp.setVisible(true);
		this.setVisible(true);
	}
	
	public static void main(String[] args) 
	{
		new MainFrame();
	}
	
	private Point caculateLocation(int width,int height) 
	{
        Point ownerLocation = getLocation();
        Dimension ownerSize = getSize();
        double x = 0.5 * ownerSize.getWidth()+ownerLocation.getX() - 0.5 * width;
        double y = 0.5 * ownerSize.getHeight()+ownerLocation.getY() - 0.5 * height;
        int screenWidth = (int) Toolkit.getDefaultToolkit().getScreenSize().width;
        int screenHeight = (int) Toolkit.getDefaultToolkit().getScreenSize().height;
        
        if (x < 1) 
            x = 1;
        if (y < 1) 
            y = 1;
        if (x > screenWidth - width) 
            x = screenWidth - width;
        if (y > screenHeight - height) 
            y = screenHeight - height;
        return new Point((int) x, (int) y);
    }
	
	private JToolBar initTools() 
	{
		JToolBar toolBar = new JToolBar(JToolBar.VERTICAL);
		toolBar.setFloatable(false);
		toolBar.add(newItems);
		newItems.setFocusable(false);
		newItems.setActionCommand("新建");
		newItems.addActionListener(new buttonActionListener());
		toolBar.add(openItems);
		openItems.setFocusable(false);
		openItems.setActionCommand("打开");
		openItems.addActionListener(new buttonActionListener());
		toolBar.add(saveItems);
		saveItems.setFocusable(false);
		saveItems.setActionCommand("保存");
		saveItems.addActionListener(new buttonActionListener());
		toolBar.add(saveAsItems);
		saveAsItems.setFocusable(false);
		saveAsItems.setActionCommand("另存为");
		saveAsItems.addActionListener(new buttonActionListener());
		toolBar.addSeparator();
		toolBar.add(copyItems);
		copyItems.setFocusable(false);
		copyItems.setActionCommand("复制");
		copyItems.addActionListener(new buttonActionListener());
		toolBar.add(pasteItems);
		pasteItems.setFocusable(false);
		pasteItems.setActionCommand("粘贴");
		pasteItems.addActionListener(new buttonActionListener());
		toolBar.add(undoItems);
		undoItems.setFocusable(false);
		undoItems.setActionCommand("撤销");
		undoItems.addActionListener(new buttonActionListener());
		toolBar.add(redoItems);
		redoItems.setFocusable(false);
		redoItems.setActionCommand("恢复");
		redoItems.addActionListener(new buttonActionListener());
		toolBar.addSeparator();
		toolBar.add(compilerItems);
		compilerItems.setFocusable(false);
		compilerItems.setActionCommand("使用选中模板编译idl文件");
		compilerItems.addActionListener(new buttonActionListener());
		toolBar.add(compilerAsItems);
		compilerAsItems.setFocusable(false);
		compilerAsItems.setActionCommand("使用当前模板编译idl文件");
		compilerAsItems.addActionListener(new buttonActionListener());
		toolBar.add(applyItems);
		applyItems.setFocusable(false);
		applyItems.setActionCommand("应用模板");
		applyItems.addActionListener(new buttonActionListener());
		return toolBar;
	}
	
	private JMenuBar initMenus() 
	{
		JMenuBar menuBar = new JMenuBar();

		for (JMenuItem jMItem : fileItems) {
			if (jMItem.getText().equals("退出")) {
				menus[0].addSeparator();
			}
			menus[0].add(jMItem);
			jMItem.setActionCommand(jMItem.getText());
			jMItem.addActionListener(new buttonActionListener());
		}
		for (JMenuItem jItem : editItems) {
			menus[1].add(jItem);
			jItem.setActionCommand(jItem.getText());
			jItem.addActionListener(new buttonActionListener());
		}
		for (JMenuItem jItem : compileItems) {
			menus[2].add(jItem);
			jItem.setActionCommand(jItem.getText());
			jItem.addActionListener(new buttonActionListener());
		}
		for (JMenu jMenu : menus) {
			menuBar.add(jMenu);
		}
		
		return menuBar;
	}
	
	class windowHandler implements WindowListener
	{
		public void windowOpened(WindowEvent e) {}

		public void windowClosing(WindowEvent e) 
		{
			JButton jb1 = new JButton("确定");
			JButton jb2 = new JButton("取消");
			JLabel jl = new JLabel("确定退出吗?",JLabel.CENTER);
			
			JDialog jd = new JDialog((JFrame)e.getSource(),"",true);
			jd.setSize(200, 100);
			jd.setLocation(caculateLocation(200,100));
			JPanel jp = new JPanel();
			jp.setLayout(new GridLayout(1,2,3,4));
			jd.add(jl,"Center");
			jd.add(jp, "South");
			jp.add(jb1);
			jp.add(jb2);
			jb1.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					System.exit(0);
				}
			});
			jb2.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					jd.dispose();
				}
			});
			jd.setVisible(true);
			jl.setVisible(true);
			jb1.setVisible(true);
			jb2.setVisible(true);
		}

		public void windowClosed(WindowEvent e) {}

		public void windowIconified(WindowEvent e) {}

		public void windowDeiconified(WindowEvent e) {}
		
		public void windowActivated(WindowEvent e) {}

		public void windowDeactivated(WindowEvent e) {}
	}
	
	class buttonActionListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			switch(command)
			{
			case "新建":
				JPanel jp = new JPanel();
				LineNr ln = new LineNr();
				jp.setLayout(new BorderLayout());
				jp.add("West", ln);
				jp.add("Center", ln.scrollPane);
				jp.setBorder(null);
				pathDic.put(jp, null);
				contentlist.add(jp);
				textlist.add(ln.pane);
				tb.addTab("new tab", jp);
				tb.setTabComponentAt(contentlist.size()-1,new TabbedPanel(tb, pathDic, contentlist, textlist, um));
				tb.setSelectedIndex(contentlist.size()-1);
				um.add(ln.pane.getU());
				break;
			case "打开":
				JFileChooser chooser = new JFileChooser();
				chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
				chooser.showOpenDialog(tb);
				if(chooser.getSelectedFile() == null)
					break;
				File f = chooser.getSelectedFile();
				String name = f.getName();
				String type = name.substring(name.lastIndexOf(".")+1,name.length());
				if(OPenFile(f,type))
				{
					tb.setTabComponentAt(contentlist.size()-1,new TabbedPanel(tb, pathDic, contentlist, textlist, um));
					tb.setSelectedIndex(contentlist.size() - 1);
				}
				break;
			case "保存":
				JFileChooser chooser2 = new JFileChooser();
				chooser2.setCurrentDirectory(new File(System.getProperty("user.dir")));
				chooser2.setFileFilter(new FileNameExtensionFilter("txt file", "txt"));
				if(tb.getSelectedComponent() == null)
				{
					JOptionPane.showMessageDialog(tb,"未选择需保存的模板文件");
					break;
				}
				String str = textlist.get(tb.getSelectedIndex()).getText();
				String path = pathDic.get(contentlist.get(tb.getSelectedIndex()));
				File savefile;
				if (path == null) 
				{
					chooser2.showSaveDialog(tb);
					if(!chooser2.getSelectedFile().getName().contains(".txt"))
					{
						File temp = chooser2.getSelectedFile();
						chooser2.setSelectedFile(new File(temp.getPath()+".txt"));
						if(temp.exists())
							temp.delete();
					}
					savefile = chooser2.getSelectedFile();
					if(savefile==null)
						break;
					path = savefile.getAbsolutePath();
					pathDic.put(contentlist.get(tb.getSelectedIndex()), path);
					tb.setTitleAt(tb.getSelectedIndex(), savefile.getName());
					saveFile(savefile, str);
				} else {
					savefile = new File(path);
					saveFile(savefile, str);
				}
				String title = savefile.getName();
				tb.setTitleAt(tb.getSelectedIndex(), title);
				break;
			case "另存为":
				JFileChooser chooser3 = new JFileChooser();
				chooser3.setCurrentDirectory(new File(System.getProperty("user.dir")));
				chooser3.setFileFilter(new FileNameExtensionFilter("txt file", "txt"));
				if(tb.getSelectedComponent() == null)
				{
					JOptionPane.showMessageDialog(tb,"未选择需保存的模板文件");
					break;
				}
				String str2 = textlist.get(tb.getSelectedIndex()).getText();
				chooser3.showSaveDialog(tb);
				
				if(!chooser3.getSelectedFile().getName().contains(".txt"))
				{
					File temp = chooser3.getSelectedFile();
					chooser3.setSelectedFile(new File(temp.getPath()+".txt"));
					if(temp.exists())
						temp.delete();
				}
				
				File saveasfile = chooser3.getSelectedFile();
				if(saveasfile==null)
					break;
				path = saveasfile.getAbsolutePath();
				pathDic.put(contentlist.get(tb.getSelectedIndex()), path);
				tb.setTitleAt(tb.getSelectedIndex(), saveasfile.getName());
				saveFile(saveasfile, str2);
				String t = saveasfile.getName();
				tb.setTitleAt(tb.getSelectedIndex(), t);
				break;
			case "退出":
				JButton jb1 = new JButton("确定");
				JButton jb2 = new JButton("取消");
				JLabel jl = new JLabel("确定退出吗?",JLabel.CENTER);
				
				JDialog jd = new JDialog();
				jd.setSize(200, 100);
				jd.setLocation(caculateLocation(200,100));
				JPanel jp1 = new JPanel();
				jp1.setLayout(new GridLayout(1,2,3,4));
				jd.add(jl,"Center");
				jd.add(jp1, "South");
				jp1.add(jb1);
				jp1.add(jb2);
				jb1.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						System.exit(0);
					}
				});
				jb2.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						jd.dispose();
					}
				});
				jd.setVisible(true);
				jl.setVisible(true);
				jb1.setVisible(true);
				jb2.setVisible(true);
				break;
			case "复制":
				copy();
				break;
			case "粘贴":
				paste();
				break;
			case "撤销":
				if(tb.getSelectedComponent() == null)
					break;
				if(um.get(tb.getSelectedIndex()).canUndo())
					um.get(tb.getSelectedIndex()).undo();
				break;
			case "恢复":
				if(tb.getSelectedComponent() == null)
					break;
				if(um.get(tb.getSelectedIndex()).canRedo())
					um.get(tb.getSelectedIndex()).redo();
				break;
			case "使用选中模板编译idl文件":
				if(tb.getSelectedComponent() == null)
				{
					JOptionPane.showMessageDialog(tb,"未选择模板");
					break;
				}
				
				JFileChooser chooser4 = new JFileChooser();
				chooser4.setCurrentDirectory(new File(System.getProperty("user.dir")));
				boolean judge = false;
				chooser4.setFileFilter(new FileNameExtensionFilter("txt file", "txt"));
				String str3 = textlist.get(tb.getSelectedIndex()).getText();
				String path2 = pathDic.get(contentlist.get(tb.getSelectedIndex()));
				File savefile2;
				if (path2 == null) 
				{
					JOptionPane.showMessageDialog(tb,"请先保存当前模板文件");
					chooser4.showSaveDialog(tb);
					
					savefile2 = chooser4.getSelectedFile();
					if(savefile2==null)
					{
						JOptionPane.showMessageDialog(tb,"当前模板文件保存失败，请重新执行编译");
						break;
					}
					
					if(!chooser4.getSelectedFile().getName().contains(".txt"))
					{
						File temp = chooser4.getSelectedFile();
						chooser4.setSelectedFile(new File(temp.getPath()+".txt"));
						if(temp.exists())
							temp.delete();
					}
					path2 = savefile2.getAbsolutePath();
					pathDic.put(contentlist.get(tb.getSelectedIndex()), path2);
					tb.setTitleAt(tb.getSelectedIndex(), savefile2.getName());
					saveFile(savefile2, str3);
					judge = true;
				} else {
					savefile2 = new File(path2);
					saveFile(savefile2, str3);
					judge = true;
				}
				
				if(judge == false)
				{
					JOptionPane.showMessageDialog(tb,"当前模板文件保存失败，请重新执行编译");
					break;
				}
				
				path2 = System.getProperty("user.dir")+"\\jacorb-3.8\\bin";
				if(!display(path2))
				{
					JOptionPane.showMessageDialog(tb,"编译器路径有误，应用模板失败");
					break;
				}
				File savefile3 = new File(path2+"\\template.txt");
				saveFile(savefile3,str3);
				
				JDialog jd0 = new JDialog();
				jd0.setLayout(new BorderLayout());
				JPanel ipath = new JPanel();
				JPanel opath = new JPanel();
				ipath.setLayout(new GridLayout(2,1));
				opath.setLayout(new GridLayout(2,1));
				JLabel ilabel = new JLabel("请选择需要编译的IDL文件:");
				JPanel tmp = new JPanel();
				tmp.setLayout(new FlowLayout());
				JTextField ipath_ = new JTextField();
				ipath_.setEditable(false);
				ipath_.setPreferredSize(new Dimension(220, 30));
				JButton open = new JButton("打开");
				open.setPreferredSize(new Dimension(70, 30));
				open.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						chooser4.setFileFilter(new FileNameExtensionFilter("idl file", "idl"));
						chooser4.showOpenDialog(tb);
						if(chooser4.getSelectedFile() == null)
						{
							JOptionPane.showMessageDialog(tb,"未选择需编译的idl文件");
						}
						File file = chooser4.getSelectedFile();
						String name2 = file.getName();
						String type2 = name2.substring(name2.lastIndexOf(".")+1,name2.length());
						if(!type2.equals("idl"))
							JOptionPane.showMessageDialog(tb,"选中文件非idl文件");
						else
							ipath_.setText(file.getAbsolutePath());
					}
				});
				tmp.add(ipath_);
				tmp.add(open);
				JLabel olabel = new JLabel("请填写输出文件路径:");
				JTextField opath_ = new JTextField(System.getProperty("user.dir"));
				ipath.add(ilabel);
				ipath.add(tmp);
				opath.add(olabel);
				opath.add(opath_);
				JPanel center = new JPanel();
				center.setLayout(new GridLayout(2,1));
				center.add(ipath);
				center.add(opath);
				jd0.add("Center", center);
				JButton correct = new JButton("确定");
				correct.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						if(ipath_.getText().equals("") || !ipath_.getText().endsWith(".idl"))
							JOptionPane.showMessageDialog(tb,"未选择需编译的idl文件");
						else if(!display(opath_.getText()))
							JOptionPane.showMessageDialog(tb,"输出文件路径不合法");
						else
						{
							jd0.dispose();
							File file = new File(ipath_.getText());
							String path3 = file.getAbsolutePath();
							String name2 = file.getName();
							
							JLabel jl_ = new JLabel("确定使用当前模板编译文件"+name2+"吗?",JLabel.CENTER);
							JButton jb11 = new JButton("确定");
							JButton jb22 = new JButton("取消");
							
							JDialog jd1 = new JDialog();
							jd1.setSize(300, 100);
							jd1.setLocation(caculateLocation(300,100));
							JPanel jp2 = new JPanel();
							jp2.setLayout(new GridLayout(1,2,3,4));
							jd1.add(jl_,"Center");
							jd1.add(jp2, "South");
							jp2.add(jb11);
							jp2.add(jb22);
							jb11.addActionListener(new ActionListener(){
								public void actionPerformed(ActionEvent e) 
								{
									jd1.dispose();
									String cmd = "cmd /c idl -d "+opath_.getText()+" "+path3;
									String []a = cmd.split(" ");
									Runtime run = Runtime.getRuntime();
									try{
										String path2 = System.getProperty("user.dir")+"\\jacorb-3.8\\bin";
										Process p = run.exec(a,null,new File(path2));
										InputStream stderr = p.getErrorStream();
										InputStreamReader isr = new InputStreamReader(stderr);
										BufferedReader inBr = new BufferedReader(isr);
										String lineStr = null;
										String text = "";
										while((lineStr = inBr.readLine()) != null)
											text = text+"\n"+lineStr;
										
										if(!text.equals(""))
											textArea.setText(text.substring(1));
										else
											textArea.setText("编译成功");
										inBr.close();
									}catch(Exception e1){
										e1.printStackTrace();
									}
								}
							});
							jb22.addActionListener(new ActionListener(){
								public void actionPerformed(ActionEvent e) {
									jd1.dispose();
								}
							});
							jd1.setVisible(true);
							jl_.setVisible(true);
							jb11.setVisible(true);
							jb22.setVisible(true);
						}
					}
				});
				JButton cancel = new JButton("取消");
				cancel.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						jd0.dispose();
					}
				});
				JPanel button = new JPanel();
				button.setLayout(new GridLayout(1,2));
				button.add(correct);
				button.add(cancel);
				jd0.add("South", button);
				jd0.setSize(320, 210);
				jd0.setLocation(caculateLocation(320,210));
				jd0.setVisible(true);
				break;
			case "使用当前模板编译idl文件":
				JDialog jd1 = new JDialog();
				jd1.setLayout(new BorderLayout());
				JPanel ipath2 = new JPanel();
				JPanel opath2 = new JPanel();
				ipath2.setLayout(new GridLayout(2,1));
				opath2.setLayout(new GridLayout(2,1));
				JLabel ilabel2 = new JLabel("请选择需要编译的IDL文件:");
				JPanel tmp2 = new JPanel();
				tmp2.setLayout(new FlowLayout());
				JTextField ipath_2 = new JTextField();
				ipath_2.setEditable(false);
				ipath_2.setPreferredSize(new Dimension(220, 30));
				JButton open2 = new JButton("打开");
				open2.setPreferredSize(new Dimension(70, 30));
				open2.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						JFileChooser chooser4 = new JFileChooser();
						chooser4.setFileFilter(new FileNameExtensionFilter("idl file", "idl"));
						chooser4.showOpenDialog(tb);
						if(chooser4.getSelectedFile() == null)
						{
							JOptionPane.showMessageDialog(tb,"未选择需编译的idl文件");
						}
						File file = chooser4.getSelectedFile();
						String name2 = file.getName();
						String type2 = name2.substring(name2.lastIndexOf(".")+1,name2.length());
						if(!type2.equals("idl"))
							JOptionPane.showMessageDialog(tb,"选中文件非idl文件");
						else
							ipath_2.setText(file.getAbsolutePath());
					}
				});
				tmp2.add(ipath_2);
				tmp2.add(open2);
				JLabel olabel2 = new JLabel("请填写输出文件路径:");
				JTextField opath_2 = new JTextField(System.getProperty("user.dir"));
				ipath2.add(ilabel2);
				ipath2.add(tmp2);
				opath2.add(olabel2);
				opath2.add(opath_2);
				JPanel center2 = new JPanel();
				center2.setLayout(new GridLayout(2,1));
				center2.add(ipath2);
				center2.add(opath2);
				jd1.add("Center", center2);
				JButton correct2 = new JButton("确定");
				correct2.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						if(ipath_2.getText().equals("") || !ipath_2.getText().endsWith(".idl"))
							JOptionPane.showMessageDialog(tb,"未选择需编译的idl文件");
						else if(!display(opath_2.getText()))
							JOptionPane.showMessageDialog(tb,"输出文件路径不合法");
						else
						{
							jd1.dispose();
							File file = new File(ipath_2.getText());
							String path3 = file.getAbsolutePath();
							String name2 = file.getName();
							
							JLabel jl_ = new JLabel("确定使用当前模板编译文件"+name2+"吗?",JLabel.CENTER);
							JButton jb11 = new JButton("确定");
							JButton jb22 = new JButton("取消");
							
							JDialog jd1 = new JDialog();
							jd1.setSize(300, 100);
							jd1.setLocation(caculateLocation(300,100));
							JPanel jp2 = new JPanel();
							jp2.setLayout(new GridLayout(1,2,3,4));
							jd1.add(jl_,"Center");
							jd1.add(jp2, "South");
							jp2.add(jb11);
							jp2.add(jb22);
							jb11.addActionListener(new ActionListener(){
								public void actionPerformed(ActionEvent e) 
								{
									jd1.dispose();
									String cmd = "cmd /c idl -d "+opath_2.getText()+" "+path3;
									String []a = cmd.split(" ");
									Runtime run = Runtime.getRuntime();
									try{
										String path2 = System.getProperty("user.dir")+"\\jacorb-3.8\\bin";
										if(!display(path2))
											JOptionPane.showMessageDialog(tb,"编译器路径有误");
											
										Process p = run.exec(a,null,new File(path2));
										InputStream stderr = p.getErrorStream();
										InputStreamReader isr = new InputStreamReader(stderr);
										BufferedReader inBr = new BufferedReader(isr);
										String lineStr = null;
										String text = "";
										while((lineStr = inBr.readLine()) != null)
											text = text+"\n"+lineStr;
										
										if(!text.equals(""))
											textArea.setText(text.substring(1));
										else
											textArea.setText("编译成功");
										inBr.close();
									}catch(Exception e1){
										textArea.setText("编译失败");
										//e1.printStackTrace();
									}
								}
							});
							jb22.addActionListener(new ActionListener(){
								public void actionPerformed(ActionEvent e) {
									jd1.dispose();
								}
							});
							jd1.setVisible(true);
							jl_.setVisible(true);
							jb11.setVisible(true);
							jb22.setVisible(true);
						}
					}
				});
				JButton cancel2 = new JButton("取消");
				cancel2.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						jd1.dispose();
					}
				});
				JPanel button2 = new JPanel();
				button2.setLayout(new GridLayout(1,2));
				button2.add(correct2);
				button2.add(cancel2);
				jd1.add("South", button2);
				jd1.setSize(320, 210);
				jd1.setLocation(caculateLocation(320,210));
				jd1.setVisible(true);
				break;
			case "应用模板":
				if(tb.getSelectedComponent() == null)
				{
					JOptionPane.showMessageDialog(tb,"未选择模板");
					break;
				}
				
				JFileChooser chooser5 = new JFileChooser();
				chooser5.setCurrentDirectory(new File(System.getProperty("user.dir")));
				boolean judge2 = false;
				chooser5.setFileFilter(new FileNameExtensionFilter("txt file", "txt"));
				String str4 = textlist.get(tb.getSelectedIndex()).getText();
				String path3 = pathDic.get(contentlist.get(tb.getSelectedIndex()));
				File savefile4;
				if (path3 == null) 
				{
					JOptionPane.showMessageDialog(tb,"请先保存当前模板文件");
					chooser5.showSaveDialog(tb);
					
					savefile4 = chooser5.getSelectedFile();
					if(savefile4==null)
					{
						JOptionPane.showMessageDialog(tb,"当前模板文件保存失败，请重新执行编译");
						break;
					}
					
					if(!chooser5.getSelectedFile().getName().contains(".txt"))
					{
						File temp = chooser5.getSelectedFile();
						chooser5.setSelectedFile(new File(temp.getPath()+".txt"));
						if(temp.exists())
							temp.delete();
					}
					path3 = savefile4.getAbsolutePath();
					pathDic.put(contentlist.get(tb.getSelectedIndex()), path3);
					tb.setTitleAt(tb.getSelectedIndex(), savefile4.getName());
					saveFile(savefile4, str4);
					judge2 = true;
				} else {
					savefile4 = new File(path3);
					saveFile(savefile4, str4);
					judge2 = true;
				}
				
				if(judge2 == false)
				{
					JOptionPane.showMessageDialog(tb,"应用模板失败");
					break;
				}
				
				path3 = System.getProperty("user.dir")+"\\jacorb-3.8\\bin";
				File savefile5 = new File(path3+"\\template.txt");
				if(!display(path3))
				{
					JOptionPane.showMessageDialog(tb,"编译器路径有误，应用模板失败");
					break;
				}
				saveFile(savefile5,str4);
				textArea.setText("应用模板成功");
				break;
			default:
				break;
			}
		}
	}
	
	private boolean OPenFile(File f,String type) {
		try {
			if(!type.equals("txt"))
			{
				JOptionPane.showMessageDialog(this,"不可识别的模板文件");
				return false;
			}
			@SuppressWarnings("resource")
			BufferedReader reader = new BufferedReader(new FileReader(f));
			StringBuilder strb = new StringBuilder();
			String title = f.getName();
			String path = f.getCanonicalPath();

			while (reader.ready()) {
				strb.append(reader.readLine()).append("\n");
			}

			JPanel jp = new JPanel();
			LineNr ln = new LineNr();
			jp.setLayout(new BorderLayout());
			jp.add("West", ln);
			jp.add("Center", ln.scrollPane);
			jp.setBorder(null);
			pathDic.put(jp, path);
			contentlist.add(jp);
			textlist.add(ln.pane);
			tb.addTab(title, jp);
			ln.pane.setText(strb.toString());
			UndoManager u = new UndoManager();
			ln.pane.getDocument().addUndoableEditListener(new UndoableEditListener(){
				public void undoableEditHappened(UndoableEditEvent e) {
					u.addEdit(e.getEdit());
					}
			});
			um.add(u);
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private void saveFile(File f, String str) 
	{
		try {
			str = str.replaceAll("\n", "\r\n");
			BufferedWriter writer = new BufferedWriter(new FileWriter(f));
			writer.write(str);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void copy()
	{
		if(tb.getSelectedComponent() == null)
			return;
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		String text = textlist.get(tb.getSelectedIndex()).getSelectedText();
		StringSelection selection = new StringSelection(text);
		clipboard.setContents(selection, null);
	}
	
	public void paste()
	{
		if(tb.getSelectedComponent() == null)
			return;
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable contents = clipboard.getContents(this);
		DataFlavor flavor = DataFlavor.stringFlavor;
		if(contents.isDataFlavorSupported(flavor))
		{
			try{
				String str = (String)contents.getTransferData(flavor);
				textlist.get(tb.getSelectedIndex()).replaceSelection(str);
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}
	
	boolean display(String name)
	{
		File fileObj = new File(name);
		if(fileObj.isDirectory())
			return true;
		else
			return false;
	}
}