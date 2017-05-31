package GUI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import javax.swing.JPanel;
import javax.swing.border.MatteBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public class LineNr extends JPanel
{
	private static final long serialVersionUID = 1L;
	public PopupTextPane pane;
	public LineHighlighterScrollPane scrollPane;

	public LineNr()
	{
		super();
		setPreferredSize(new Dimension(27, 30));
		
		pane = new PopupTextPane()
		{
			private static final long serialVersionUID = 1L;
			public void paint(Graphics g)
			{
				super.paint(g);
				LineNr.this.repaint();
			}
		};
		pane.setBorder(new MatteBorder(0, 5, 0, 0, Color.WHITE));
		scrollPane = new LineHighlighterScrollPane(pane);
		pane.addCaretListener(new CaretListener(){
			public void caretUpdate(CaretEvent arg0) {
				scrollPane.repaint();
			}
		});
		scrollPane.setBorder(new MatteBorder(0, 1, 0, 0, new Color(230,230,230)));
		this.setBackground(Color.WHITE);
		this.setBorder(null);
	}
	
	public void paint(Graphics g)
	{
		super.paint(g);
		int start = pane.viewToModel(scrollPane.getViewport().getViewPosition()); // starting pos in document
		int end = pane.viewToModel(new Point(scrollPane.getViewport().getViewPosition().x + pane.getWidth(),scrollPane.getViewport().getViewPosition().y + pane.getHeight()));
		
		Document doc = pane.getDocument();
		int startline = doc.getDefaultRootElement().getElementIndex(start) + 1;
		int endline = doc.getDefaultRootElement().getElementIndex(end) + 1;

		int fontHeight = g.getFontMetrics(pane.getFont()).getHeight();
		int fontDesc = g.getFontMetrics(pane.getFont()).getDescent();
		int starting_y = -1;

		try{
			starting_y = pane.modelToView(start).y - scrollPane.getViewport().getViewPosition().y + fontHeight - fontDesc;
		}
		catch (BadLocationException e1){
			e1.printStackTrace();
		}

		g.setColor(Color.GRAY);
		for (int line = startline, y = starting_y; line <= endline; y += fontHeight, line++)
		{
			g.drawString(Integer.toString(line), 23-6*(Integer.toString(line).length()), y);
		}
		
		scrollPane.repaint();
	}
}