package GUI;

import java.util.Vector;

public class UndoableEdit {
	private boolean setup;
	private boolean undo;
	private boolean redo;
	public boolean isundo;
	public boolean isredo;
	private int redoSize;
	private Vector<String> buffer;
	private Vector<Integer> position;
	private int index;
	private PopupTextPane textPane;
	
	public UndoableEdit(PopupTextPane textPane)
	{
		this.setup = true;
		this.undo = false;
		this.redo = false;
		this.isundo = false;
		this.isredo = false;
		this.redoSize = 0;
		this.buffer = new Vector<String>(20);
		this.position = new Vector<Integer>(20);
		for(int i = 0 ; i < 20 ; i ++)
		{
			this.buffer.add("");
			this.position.add(0);
		}
		this.index = 0;
		this.textPane = textPane;
	}
	
	public boolean canUndo()
	{
		return undo;
	}
	
	public boolean canRedo()
	{
		return redo;
	}
	
	public void undo()
	{
		--index;
		isundo = true;
		textPane.setText(buffer.get(index));
		try {
			textPane.setCaretPosition(position.get(index)+1);
		} catch(java.lang.IllegalArgumentException e) {
			try {
			textPane.setCaretPosition(position.get(index)-1);
			} catch(java.lang.IllegalArgumentException e1) {
				textPane.setCaretPosition(position.get(index));
			}
		}
	}
	
	public void redo()
	{
		++index;
		isredo = true;
		textPane.setText(buffer.get(index));
		try {
			textPane.setCaretPosition(position.get(index)+1);
		} catch(java.lang.IllegalArgumentException e) {
			try {
			textPane.setCaretPosition(position.get(index)-1);
			} catch(java.lang.IllegalArgumentException e1) {
				textPane.setCaretPosition(position.get(index));
			}
		}
	}
	
	public void bufferUpdate()
	{
		if(!setup)
			return;
		
		if(isundo || isredo)
		{
			isundo = false;
			isredo = false;
		}
		else if(!textPane.getText().equals(buffer.get(index)))
		{
			++index;
			if(index > 19)
			{
				buffer.remove(0);
				position.remove(0);
				buffer.add("");
				position.add(0);
				index = 19;
			}
			buffer.set(index, textPane.getText());
			position.set(index, textPane.getCaretPosition());
			redoSize = index;
		}
		
		if(index >= redoSize)
			redo = false;
		else
			redo = true;
		if(index <= 0)
			undo = false;
		else
			undo = true;
	}

	public void setSetup(boolean setup) {
		this.setup = setup;
	}
}
