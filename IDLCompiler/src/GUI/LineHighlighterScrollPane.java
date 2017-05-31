package GUI;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.BadLocationException;

public class LineHighlighterScrollPane extends JScrollPane 
{
	private static final long serialVersionUID = 1L;
	private PopupTextPane pane;
	
	public LineHighlighterScrollPane(PopupTextPane pane)
	{
		super(pane);
		this.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		this.pane = pane;
	}
	
	public void paint(Graphics g)
	{
		super.paint(g);
		if(pane.getSelectedText() == null)
		{
			int fontHeight = g.getFontMetrics(pane.getFont()).getHeight();
			int fontDesc = g.getFontMetrics(pane.getFont()).getDescent();
			
			int currentLine = -1;
			try{
				currentLine = pane.modelToView(pane.getCaretPosition()).y - this.getViewport().getViewPosition().y - fontDesc;
			}
			catch (BadLocationException e1){
				e1.printStackTrace();
			}
			g.setColor(new Color(255,255,0,40));
			g.fillRect(3, currentLine, this.getWidth()-20, fontHeight+2);
		}
	}
}