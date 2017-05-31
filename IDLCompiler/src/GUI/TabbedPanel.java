package GUI;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.undo.UndoManager;

public class TabbedPanel extends JPanel 
{
	private static final long serialVersionUID = 1L;
	private final JTabbedPane pane;
	private HashMap<JPanel, String> pathDic;
	private Vector<JPanel> contentlist;
	private Vector<PopupTextPane> textlist;
	private Vector<UndoManager> um;
	public TabbedPanel(final JTabbedPane pane,HashMap<JPanel, String> pathDic,Vector<JPanel> contentlist,Vector<PopupTextPane> textlist,Vector<UndoManager> um)
	{
		super(new FlowLayout(FlowLayout.LEFT,0,0));
		this.pathDic = pathDic;
		this.contentlist = contentlist;
		this.textlist = textlist;
		this.um = um;
		if(pane==null)
			throw new NullPointerException("TabbedPane is null");
		this.pane=pane;
		setOpaque(false);
		JLabel label = new JLabel(){
			private static final long serialVersionUID = 1L;
			public String getText()
			{
				int i = pane.indexOfTabComponent(TabbedPanel.this);
				if(i != -1)
					return pane.getTitleAt(i);
				return null;
			}
		};
		add(label);
		label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 2));
		add(new TabButton());
		setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 0));
	}
	
	private class TabButton extends JButton
	{
		private static final long serialVersionUID = 1L;
		public TabButton()
		{
			int size = 10;
			setPreferredSize(new Dimension(size, size));
			setToolTipText("关闭");
			setUI(new BasicButtonUI());
			setContentAreaFilled(false);
			setFocusable(false);
			setBorder(BorderFactory.createEtchedBorder());
			setBorderPainted(false);
			setRolloverEnabled(true);
			
			addMouseListener(new MouseAdapter(){
				public void mouseEntered(MouseEvent e){
					Component component = e.getComponent();
					if(component instanceof AbstractButton)
					{
						AbstractButton button = (AbstractButton)component;
						button.setBorderPainted(false);
					}
				}
				public void mouseExited(MouseEvent e){
					Component component = e.getComponent();
					if(component instanceof AbstractButton){
						AbstractButton button = (AbstractButton) component;
						button.setBorderPainted(false);
					}
				}
			});
			
			addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent evt){
					int i = pane.indexOfTabComponent(TabbedPanel.this);
					pathDic.remove(contentlist.get(i));
					contentlist.remove(i);
					textlist.remove(i);
					um.remove(i);
					if(i != -1)
						pane.remove(i);
				}
			});
		}
		
		public void updateUI(){}
		protected void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D)g.create();
			if(getModel().isPressed()){
				g2.translate(1, 1);
			}
			g2.setStroke(new BasicStroke(1.0f));
			g2.setColor(Color.GRAY);
			
			if(getModel().isRollover()){
				g2.setColor(Color.RED);
			}
			int delta = 6;
			g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
			g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
			g2.dispose();
		}
	}
}
