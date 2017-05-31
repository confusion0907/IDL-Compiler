package GUI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.Set;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class PopupTextPane extends JTextPane implements ActionListener,MouseListener{
	private static final long serialVersionUID = 1L;
	private JPopupMenu popupMenu;
	private JMenuItem cutMenu,copyMenu,pasteMenu,selectAllMenu;
	private UndoableEdit u;
	
	PopupTextPane()
	{
		super();
		popupMenu = new JPopupMenu();
		cutMenu = new JMenuItem("剪切");
		copyMenu = new JMenuItem("复制");
		pasteMenu = new JMenuItem("粘贴");
		selectAllMenu = new JMenuItem("全选");
		
		cutMenu.setAccelerator(KeyStroke.getKeyStroke('X', InputEvent.CTRL_MASK));
		copyMenu.setAccelerator(KeyStroke.getKeyStroke('C', InputEvent.CTRL_MASK));
		pasteMenu.setAccelerator(KeyStroke.getKeyStroke('V', InputEvent.CTRL_MASK));
		selectAllMenu.setAccelerator(KeyStroke.getKeyStroke('A', InputEvent.CTRL_MASK));
		
		cutMenu.addActionListener(this);
		copyMenu.addActionListener(this);
		pasteMenu.addActionListener(this);
		selectAllMenu.addActionListener(this);
		
		popupMenu.add(cutMenu);
		popupMenu.add(copyMenu);
		popupMenu.add(pasteMenu);
		popupMenu.add(new JSeparator());
		popupMenu.add(selectAllMenu);
		
		this.add(popupMenu);
		this.addMouseListener(this);
		//this.addKeyListener(this);
		SyntaxHighlighter s = new SyntaxHighlighter(this);
		this.getDocument().addDocumentListener(s);
		u = new UndoableEdit(this);
	}
	
	public boolean getScrollableTracksViewportWidth()
	{
		setSize(new Dimension(getParent().getSize()));
		return false;
	}	  
	
	public void setSize(Dimension d)
	{
		if(d.width<getParent().getSize().width)
			d.width=getParent().getSize().width;
		super.setSize(d);
	}

	public void actionPerformed(ActionEvent e) 
	{ 
		if(e.getSource()==copyMenu)
			this.copy();
		if(e.getSource()==pasteMenu)
			this.paste();
		if(e.getSource()==cutMenu)
			this.cut();
		if(e.getSource()==selectAllMenu)
			this.selectAll();
	}

	public void mousePressed(MouseEvent e) 
	{
		popupMenuTrigger(e);
	}

	public void mouseReleased(MouseEvent e) 
	{
		popupMenuTrigger(e);
	}

	public void mouseClicked(MouseEvent e) {} 
	public void mouseEntered(MouseEvent e) {} 
	public void mouseExited(MouseEvent e) {}

	private void popupMenuTrigger(MouseEvent e)
	{
		if(e.isPopupTrigger())
		{
			this.requestFocusInWindow();
			cutMenu.setEnabled(isAbleToCopyAndCut());
			copyMenu.setEnabled(isAbleToCopyAndCut());
			pasteMenu.setEnabled(isAbleToPaste());
			selectAllMenu.setEnabled(isAbleToSelectAll());
			popupMenu.show(this,e.getX()+3, e.getY()+3);
		}
	}

	private boolean isAbleToSelectAll()
	{ 
		return !("".equalsIgnoreCase(this.getText())||(null==this.getText()));
	}

	private boolean isAbleToCopyAndCut()
	{
		return (this.getSelectionStart()!=this.getSelectionEnd());
	}

	private boolean isAbleToPaste()
	{
		Transferable content = this.getToolkit().getSystemClipboard().getContents(this);
		try {
			return (content.getTransferData(DataFlavor.stringFlavor) instanceof String);
		} catch (UnsupportedFlavorException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
	}

	public UndoableEdit getU() {
		return u;
	}

	/*
	public void keyPressed(KeyEvent arg0) {
		
	}

	public void keyReleased(KeyEvent arg0) {
		try {
			this.getU().setSetup(false);
			if(arg0.getKeyChar() == '<' && this.getText(this.getCaretPosition()-1, 1).equals("<") && !this.getText(this.getCaretPosition(), 1).equals(">"))
			{
				int position = this.getCaretPosition();
				String text = this.getText();
				String result = "";
				if(position != text.length())
					result = text.substring(0, position+1)+">"+text.substring(position+1, text.length());
				else
					result = text + ">";
				this.setText(result);
				this.setCaretPosition(position);
				this.setCaretPosition(position);
			}
			else if(arg0.getKeyChar() == '\n')
			{
				int position = this.getCaretPosition();
				int currentLine = this.getDocument().getDefaultRootElement().getElementIndex(position)-1;
				String []temp = this.getText().replaceAll("\r", "").split("\n");
				if(temp.length <= currentLine || currentLine < 0)
					return;
				if(KeyWords.getInstance().getLabelwords().contains(temp[currentLine]))
				{
					String result = "";
					for(int i = 0 ; i < temp.length ; i++)
					{
						if(i != currentLine && i != temp.length-1)
							result = result + temp[i] + "\r\n";
						else if(i == currentLine)
							result = result + temp[i] + "\r\n" + "\r\n%%";
						else
							result = result + temp[i];
							
					}
					this.setText(result);
					this.setCaretPosition(position);
				}
			}
			this.getU().setSetup(true);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	public void keyTyped(KeyEvent arg0) {
	}
	*/
}

class SyntaxHighlighter implements DocumentListener
{
	private Set<String> keywords;
	private Set<String> labelwords;
	private Set<String> functionwords;
	private Style keywordStyle;
	private Style labelStyle1;
	private Style labelStyle2;
	private Style functionStyle;
	private Style normalStyle;
	private char last = ' ';
	private boolean end = false;
	private PopupTextPane textPane;
	
	public SyntaxHighlighter(PopupTextPane editor)
	{
		this.textPane = editor;
		keywordStyle = ((StyledDocument)editor.getDocument()).addStyle("Keyword_Style", null);
		labelStyle1 = ((StyledDocument)editor.getDocument()).addStyle("Keyword_Style", null);
		labelStyle2 = ((StyledDocument)editor.getDocument()).addStyle("Keyword_Style", null);
		functionStyle = ((StyledDocument)editor.getDocument()).addStyle("Keyword_Style", null);
		normalStyle = ((StyledDocument)editor.getDocument()).addStyle("Keyword_Style", null);
		StyleConstants.setForeground(normalStyle, Color.BLACK);
		StyleConstants.setForeground(keywordStyle, new Color(210,54,106));
		StyleConstants.setForeground(labelStyle1, new Color(51,166,138));
		//StyleConstants.setItalic(labelStyle1, true);
		StyleConstants.setForeground(labelStyle2, new Color(149,63,204));
		//StyleConstants.setItalic(labelStyle2, true);
		StyleConstants.setForeground(functionStyle, new Color(204,136,63));
		StyleConstants.setBold(functionStyle, true);
		StyleConstants.setBold(keywordStyle, true);
		
		KeyWords key = KeyWords.getInstance();
		keywords = key.getKeywords();
		labelwords = key.getLabelwords();
		functionwords = key.getFunctionwords();
	}
	
	public void changedUpdate(DocumentEvent e) 
	{
		textPane.getU().bufferUpdate();
	}
	
	public void insertUpdate(DocumentEvent e)
	{
		if(!(textPane.getU().isundo || textPane.getU().isredo))
			textPane.getU().bufferUpdate();
		try{
			colouring((StyledDocument) e.getDocument(), e.getOffset(), e.getLength());
		} catch (BadLocationException e1) {
				e1.printStackTrace();
		}
	}
	
	public void removeUpdate(DocumentEvent e) 
	{
		if(!(textPane.getU().isundo || textPane.getU().isredo))
			textPane.getU().bufferUpdate();
		try{
			colouring((StyledDocument) e.getDocument(), e.getOffset(), 0);
		} catch (BadLocationException e1){
			e1.printStackTrace();
		}
	}
	
	public char getCharAt(Document doc, int pos) throws BadLocationException
	{
		return doc.getText(pos, 1).charAt(0);
	}
	
	public boolean isWordCharacter(Document doc, int pos) throws BadLocationException
	{
		char ch = getCharAt(doc, pos);
		if(last == '>' && end)
			return false;
		else if(Character.isLetter(ch) || Character.isDigit(ch) || ch == '%' || ch == '<' || ch == '>' || ch == ':')
		{
			last = ch;
			return true;
		}
		return false;
	}
	
	public int indexOfWordStart(Document doc, int pos) throws BadLocationException
	{
		last = ' ';
		end = false;
		for(; pos > 0 && isWordCharacter(doc, pos - 1); --pos);
		return pos;
	}
	
	public int indexOfWordEnd(Document doc, int pos) throws BadLocationException
	{
		last = ' ';
		end = true;
		for(; isWordCharacter(doc, pos); ++pos);
		return pos;
	}
	
	public void colouring(StyledDocument doc, int pos, int len) throws BadLocationException
	{
		int start = indexOfWordStart(doc, pos);
		int end = indexOfWordEnd(doc, pos + len);
		
		char ch;
		while(start < end)
		{
			ch = getCharAt(doc, start);
			
			if(Character.isLetter(ch) || ch == '%' || ch == '<')
				start = colouringWord(doc, start);
			else
			{
				SwingUtilities.invokeLater(new ColouringTask(doc, start, 1, normalStyle));
				++start;
			}
		}
	}
	
	public int colouringWord(StyledDocument doc, int pos) throws BadLocationException
	{
		int wordEnd = indexOfWordEnd(doc, pos);
		String word = doc.getText(pos, wordEnd - pos);
		
		if(word.contains(">") || word.contains("<"))
		{
			while(getCharAt(doc, pos) != '<')
				pos++;
		}
		
		word = doc.getText(pos, wordEnd - pos);
		
		if(keywords.contains(word))
			SwingUtilities.invokeLater(new ColouringTask(doc, pos, wordEnd - pos, keywordStyle));
		else if(labelwords.contains(word))
		{
			String []stmp = word.split(":");
			SwingUtilities.invokeLater(new ColouringTask(doc, pos, stmp[0].length(), labelStyle1));
			if(stmp.length == 2)
				SwingUtilities.invokeLater(new ColouringTask(doc, pos + stmp[0].length(), stmp[1].length() + 1, labelStyle2));
		}
		else if(functionwords.contains(word))
		{
			char ctmp = getCharAt(doc,pos+word.length());
			if(ctmp == '(')
				SwingUtilities.invokeLater(new ColouringTask(doc, pos, word.length(), functionStyle));
		}
		else
			SwingUtilities.invokeLater(new ColouringTask(doc, pos, wordEnd - pos, normalStyle));
		
		return wordEnd;
	}
}

class ColouringTask implements Runnable
{
	private StyledDocument doc;
	private Style style;
	private int pos;
	private int len;
	
	public ColouringTask(StyledDocument doc, int pos, int len, Style style)
	{
		this.doc = doc;
		this.pos = pos;
		this.len = len;
		this.style = style;
	}
	
	public void run()
	{
		try{
			doc.setCharacterAttributes(pos, len, style, true);
		} catch (Exception e) {}
	}
}