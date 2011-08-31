package rulebender.old.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.projection.ProjectionDocument;
import org.eclipse.jface.text.projection.ProjectionDocumentManager;
import org.eclipse.jface.text.source.AbstractRulerColumn;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import prefuse.visual.VisualItem;


public class BNGTextArea
{
	
/*
	Document doc = new Document("abcdefg"); 
	Document backdoc = new Document("");//  @jve:decl-index=0:
	ProjectionDocument prodoc;
	
	BlockInfo[] blockinfo = new BlockInfo[BNGEditor.getBlocknum()];
	
	ProjectionDocumentManager manager;
	
	
	public BNGTextArea() 
	{
		initialize();
	}

	private void initialize() {
		
		for(int i=0; i<BNGEditor.getBlocknum(); i++)
			blockinfo[i] = new BlockInfo();
	
		manager = new ProjectionDocumentManager();
		prodoc = (ProjectionDocument) manager.createSlaveDocument(doc);

		try
		{
			prodoc.addMasterDocumentRange(0, doc.getLength());
		}
		catch (BadLocationException e) 
		{
		}
	}
		
	void UpdateProdoc(boolean popneeded, int offset)
	{
		int temp;//line span
		for(int i = 0; i<BNGEditor.getBlocknum(); i++)
		{
			if(blockinfo[i].statechange)
			{
				try{
					blockinfo[i].statechange = false;
					temp = doc.getLineOfOffset(blockinfo[i].caretend1) - doc.getLineOfOffset(blockinfo[i].caretbegin1);
					
					
						try 
						{
							prodoc.addMasterDocumentRange(blockinfo[i].caretbegin2, blockinfo[i].caretend2-blockinfo[i].caretbegin2);
							if(popneeded)
							{
								TextChanges textchange = textchanges.pop();
								textchanges.push(new TextChanges(-1,-1,"",true,blockinfo[i].caretbegin2-offset,blockinfo[i].caretend2-offset,false,i));
								if(Prodoc2Doc(textchange.start)>blockinfo[i].caretbegin1)
									textchange.start = textchange.start + blockinfo[i].caretend2 - blockinfo[i].caretbegin2;
								textchanges.push(textchange);
							}
							else
								textchanges.push(new TextChanges(-1,-1,"",true,blockinfo[i].caretbegin2,blockinfo[i].caretend2,false,i));
						}
						
						catch (BadLocationException e) 
						{
								
						}
				
						for(int j =0;j<BNGEditor.getBlocknum();j++)
						{
							if(blockinfo[j].valid)
							{
								if(blockinfo[j].line>blockinfo[i].line)
								{
									blockinfo[j].line = blockinfo[j].line+temp;
									blockinfo[j].lineend = blockinfo[j].lineend+temp;
								}
							}
							else
							{
								if(blockinfo[j].line>blockinfo[i].line)
									blockinfo[j].line = blockinfo[j].line+temp;
								if(blockinfo[j].lineend>blockinfo[i].line)
									blockinfo[j].lineend = blockinfo[j].lineend+temp;	
							}
						}
				}
				catch(BadLocationException e1){}	
			}
		}
	}
	
	class BlockInfo
	{
		int caretbegin1;//doc
		int caretbegin2;//doc
		int caretend1;//doc
		int caretend2;//doc
		int line;//prodoc
		int lineend;//prodoc
		boolean valid;
		boolean folded;
		boolean statechange;
		
		BlockInfo()
		{
			caretbegin1 = -1;
			caretbegin2 = -1;
			caretend1 = -1;
			caretend2 = -1;
			line = -1;
			lineend = -1;
			valid = false;
			folded = false;
			statechange = false;
		}
	}
	
	void dealwithblockinfo(int in1,int in2, String in3)
	{
		Matcher m;
		boolean found, research = false;
		int tcaret1,tcaret2;
		ArrayList<Integer> researchitem = new ArrayList<Integer>();
		int[] lines = getlinenums(in1, in2, in3);
		for(int i = lines[0]; i<=lines[2]; i++)
			for(int j = 0; j<BNGEditor.getBlocknum(); j++)
			{
				if(i == blockinfo[j].line && !blockinfo[j].folded)
				{
					research = true;
					blockinfo[j].caretbegin1 = -1;
					blockinfo[j].caretbegin2 = -1;
					blockinfo[j].line = -1;
					researchitem.add(j*2);
					blockinfo[j].valid = false;
				}
				if(i == blockinfo[j].lineend && !blockinfo[j].folded)
				{
					research = true;
					blockinfo[j].caretend1 = -1;
					blockinfo[j].caretend2 = -1;
					blockinfo[j].lineend = -1;
					researchitem.add(j*2+1);
					blockinfo[j].valid = false;
				}
			}
		
		String tempstr;
		int tempint = Prodoc2Doc(in1), strlength = in3.length();
		for(int j = 0; j<BNGEditor.getBlocknum(); j++)
		{
			if(blockinfo[j].caretbegin1>tempint)
			{
				blockinfo[j].caretbegin1 = blockinfo[j].caretbegin1 - strlength + in2;
				blockinfo[j].caretbegin2 = blockinfo[j].caretbegin2 - strlength + in2;
				blockinfo[j].line = blockinfo[j].line+lines[1]-lines[2];
			}
			if(blockinfo[j].caretend1>tempint)
			{
				blockinfo[j].caretend1 = blockinfo[j].caretend1 - strlength + in2;
				blockinfo[j].caretend2 = blockinfo[j].caretend2 - strlength + in2;
				blockinfo[j].lineend = blockinfo[j].lineend+lines[1]-lines[2];
			}
		}
		tempstr = " ";
		for(int i = lines[0]; i<=lines[1]; i++)
			tempstr = tempstr + styledTextArea.getLine(i) + styledTextArea.getLineDelimiter();
		
		for(int i=0;i<BNGEditor.getBlocknum()*2;i++)
		{
			m = keywords.get(i).matcher(tempstr);
			if(i%2 == 0)
			{
				found = true;
				tcaret1 = -1; tcaret2 = -1;
				int fstart = 0;
				while(found)
				{
					found = m.find(fstart);
					if(found)
					{
						fstart = m.end()-1;
						tcaret1 = Prodoc2Doc(m.start(1)-1 + styledTextArea.getOffsetAtLine(lines[0]));
						tcaret2 = tcaret1 + m.group(1).length();
					}
					if(found && (tcaret1 < blockinfo[i/2].caretbegin1 || blockinfo[i/2].caretbegin1 == -1) && validateCaret(tcaret1))
					{
						if(blockinfo[i/2].folded)
						{
							blockinfo[i/2].folded = false;
							blockinfo[i/2].statechange = true;
							TextChanges textchange = textchanges.peek();
							int offset = 0;
							if(Doc2Prodoc(blockinfo[i/2].caretbegin1)>textchange.start)
								offset = textchange.length - textchange.replacedText.length();
							UpdateProdoc(true,offset);
						}
						blockinfo[i/2].caretbegin1 = tcaret1;
						blockinfo[i/2].caretbegin2 = tcaret2;
						blockinfo[i/2].line = styledTextArea.getLineAtOffset(Doc2Prodoc(tcaret1));
						validateblock(i/2);
						found = false;
					}
					arc1.redraw();
					arc2.redraw();
				}
			}
			else
			{
				tcaret1 = -1; tcaret2 = -1;
				found = true;
				int fstart = 0;
				while(found)
				{
					found = m.find(fstart);
					if(found)
						fstart = m.end()-1;
					if(found && validateCaret(Prodoc2Doc(m.start(1)-1 + styledTextArea.getOffsetAtLine(lines[0]))))
					{
						tcaret1 = Prodoc2Doc(m.start(1)-1 + styledTextArea.getOffsetAtLine(lines[0]));
						tcaret2 = tcaret1 + m.group(1).length();
					}
				}
				if(tcaret1 > blockinfo[i/2].caretend1)
				{
					if(blockinfo[i/2].folded)
					{
						blockinfo[i/2].folded = false;
						blockinfo[i/2].statechange = true;
						TextChanges textchange = textchanges.peek();
						int offset = 0;
						if(Doc2Prodoc(blockinfo[i/2].caretbegin1)>textchange.start)
							offset = textchange.length - textchange.replacedText.length();
						UpdateProdoc(true,offset);
					}
					blockinfo[i/2].caretend1  = tcaret1;
					blockinfo[i/2].caretend2 = tcaret2;
					blockinfo[i/2].lineend = styledTextArea.getLineAtOffset(Doc2Prodoc(tcaret1));
					validateblock(i/2);
					arc1.redraw();
					arc2.redraw();
				}
			}
		}
	
		
	int Doc2Prodoc(int in)//caret
	{
		ArrayList<Integer> temp = new ArrayList<Integer>();
		ArrayList<Integer> temp2 = new ArrayList<Integer>();
		int min; int temp3;
		for(int i=0;i<BNGEditor.getBlocknum();i++)//add valid
			if(blockinfo[i].valid)
				temp.add(i);
		if(temp.size() == 0)
			return in;
		while(temp.size()!=0)//sort
		{
			min = temp.get(0);temp3 = 0;
			for(int i=0;i<temp.size();i++)
				if(blockinfo[temp.get(i)].caretbegin1<blockinfo[min].caretbegin1)
				{
					min = temp.get(i);
					temp3 = i;
				}
			temp2.add(min);
			temp.remove(temp3);
		}
		int tempint = in;
		for(int i = 0; i<temp2.size(); i++)
		{
			if(in>blockinfo[temp2.get(i)].caretbegin2 && in< blockinfo[temp2.get(i)].caretend2 && blockinfo[temp2.get(i)].folded)
				return -1;
			if(in >= blockinfo[temp2.get(i)].caretend2 && blockinfo[temp2.get(i)].folded)
				tempint = tempint - ( blockinfo[temp2.get(i)].caretend2 - blockinfo[temp2.get(i)].caretbegin2 );
		}
		return tempint;
	}
	
	void validateblock(int blockindex)
	{
		if(blockinfo[blockindex].caretbegin2<blockinfo[blockindex].caretend1 && blockinfo[blockindex].caretbegin1!=-1)
		{
			blockinfo[blockindex].valid = true;
		}
		else
		{
			blockinfo[blockindex].valid = false;
		}
		
		for(int i=0;i<BNGEditor.getBlocknum();i++)
			if( (blockinfo[i].caretbegin2!=-1 && blockinfo[blockindex].caretbegin2 > blockinfo[i].caretbegin2 && blockinfo[blockindex].caretbegin2 < blockinfo[i].caretend1) ||
				(blockinfo[i].caretbegin2!=-1 && blockinfo[blockindex].caretend1 > blockinfo[i].caretbegin2 && blockinfo[blockindex].caretend1 < blockinfo[i].caretend1	) )
			{
				blockinfo[blockindex].valid = false;
				blockinfo[i].valid = false;
				if(blockinfo[i].folded)
				{
					blockinfo[i].folded = false;
					blockinfo[i].statechange = true;
					TextChanges textchange = textchanges.peek();
					int offset = 0;
					if(Doc2Prodoc(blockinfo[i].caretbegin1)>textchange.start)
						offset = textchange.length - textchange.replacedText.length();
					UpdateProdoc(true,offset);
				}
			}
		
		for(int i=0;i<BNGEditor.getBlocknum();i++)
			if( (blockinfo[blockindex].caretbegin2!=-1 && blockinfo[i].caretbegin2 > blockinfo[blockindex].caretbegin2 && blockinfo[i].caretbegin2 < blockinfo[blockindex].caretend1) ||
				(blockinfo[blockindex].caretbegin2!=-1 && blockinfo[i].caretend1 > blockinfo[blockindex].caretbegin2 && blockinfo[i].caretend1 < blockinfo[blockindex].caretend1	) )
			{
				blockinfo[blockindex].valid = false;
				blockinfo[i].valid = false;
				if(blockinfo[i].folded)
				{
					blockinfo[i].folded = false;
					blockinfo[i].statechange = true;
					TextChanges textchange = textchanges.peek();
					int offset = 0;
					if(Doc2Prodoc(blockinfo[i].caretbegin1)>textchange.start)
						offset = textchange.length - textchange.replacedText.length();
					UpdateProdoc(true,offset);
				}
			}
	}
	*/
}