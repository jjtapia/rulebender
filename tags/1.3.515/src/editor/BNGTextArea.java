package editor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import link.LinkHub;
import link.LinkedViewsReceiverInterface;

import networkviewer.cmap.VisualRule;

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

public class BNGTextArea extends Composite implements
		LinkedViewsReceiverInterface {
	private StyledText styledTextArea = null;
	Document doc = new Document("abcdefg");
	Document backdoc = new Document("");// @jve:decl-index=0:
	CompositeRuler ruler; // @jve:decl-index=0:
	LineNumberRuler arc1; // @jve:decl-index=0:
	CodeFoldRuler arc2;// @jve:decl-index=0:
	ProjectionDocument prodoc;
	SourceViewer sv;
	BlockInfo[] blockinfo = new BlockInfo[BNGEditor.getBlocknum()];
	ProjectionDocumentManager manager;
	ArrayList<Pattern> keywords = BNGEditor.getKeywords(); // @jve:decl-index=0:
	Menu popmenu = new Menu(this);
	Control rulercontrol;
	Stack<TextChanges> textchanges = new Stack<TextChanges>(); // @jve:decl-index=0:
	int UndoLimit = 500;
	private boolean ignoreUndo = false;
	private boolean undobycollapse = false;
	Shell findShell;
	Text findtext1;
	StyleRange[] orisr;
	private boolean resetHighlightneeded = false;
	Shell replaceShell;
	Text replacetext1;
	Text replacetext2;
	Button replacebutton1;
	Button replacebutton2;
	Button replacebutton3;
	Button findbutton1;
	Button findbutton2;

	CTabItem tabitem;

	private static Color color_PaleGreen4 = new Color(null, 84, 139, 84);
	private static Color color_purple = new Color(null, 128, 0, 128);
	private static Color color_DarkGoldenrod1 = new Color(null, 255, 185, 15);

	public BNGTextArea(Composite parent, CTabItem tabitem, int style) {
		super(parent, style);
		this.tabitem = tabitem;

		LinkHub.getLinkHub().registerLinkedViewsListener(this);

		initialize();
	}

	private void initialize() {
		setLayout(new FillLayout());

		for (int i = 0; i < BNGEditor.getBlocknum(); i++)
			blockinfo[i] = new BlockInfo();
		ruler = new CompositeRuler();
		arc1 = new LineNumberRuler(16);
		arc2 = new CodeFoldRuler(14);
		ruler.addDecorator(0, arc1);
		ruler.addDecorator(1, arc2);

		manager = new ProjectionDocumentManager();
		prodoc = (ProjectionDocument) manager.createSlaveDocument(doc);

		try {
			prodoc.addMasterDocumentRange(0, doc.getLength());
		} catch (BadLocationException e) {
		}

		sv = new SourceViewer(this, ruler, SWT.V_SCROLL | SWT.H_SCROLL
				| SWT.BORDER);
		sv.setDocument(prodoc);
		sv.showAnnotations(true);

		// add text change listener for SourceViewer
		sv.addTextListener(new ITextListener() {

			public void textChanged(TextEvent event) {

				if ((event.getText() == null || event.getText().equals(""))
						&& event.getReplacedText() == null) {
					return;
				}

				// first open
				if (event.getReplacedText() != null
						&& event.getReplacedText().equals("abcdefg")) {
					return;
				}

				// empty selection
				if (event.getText() == null
						|| (event.getText().equals("") && event
								.getReplacedText().equals(""))) {
					return;
				}

				String filename = tabitem.getText();
				if (!filename.startsWith("*")) {
					tabitem.setText("*" + filename);
				}
			}

		});

		// add selection listener for SourceViewer
		sv.addPostSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				redrawRulers();
				resetHighlights();

				// set background color for all the text which is the same as
				// the selected one
				String fileContent = styledTextArea.getText();
				Point p = sv.getSelectedRange();

				if (p.y == 0)
					return;

				// get selected text
				String selected = styledTextArea.getSelectionText();

				// whitespace
				if (selected.equals(" ")) {
					return;
				}

				// multiple line
				if (selected.contains("\n")) {
					return;
				}

				int selectOffset = 0;
				int subStringOffset = 0;
				while (selectOffset != -1) {
					// find the new text position
					selectOffset = fileContent.substring(subStringOffset)
							.indexOf(selected);

					if (selectOffset != -1) {
						// create a style range
						final StyleRange sr = new StyleRange();
						sr.background = color_DarkGoldenrod1;
						sr.start = subStringOffset + selectOffset;
						sr.length = p.y;
						sv.getTextWidget().setStyleRange(sr);
					}

					// update the substring offset
					subStringOffset += selectOffset + p.y;
				}

			}

		});

		styledTextArea = sv.getTextWidget();
		styledTextArea.setMenu(BNGEditor.getPopmenu());
		// Windows
		if (ConfigurationManager.getConfigurationManager().getOSType() == 1)
			styledTextArea.setFont(new Font(Display.getCurrent(), "SEGOE UI",
					10, SWT.NONE));
		// OSX
		else if (ConfigurationManager.getConfigurationManager().getOSType() == 2)
			styledTextArea.setFont(new Font(Display.getCurrent(), "Monaco", 12,
					SWT.NONE));
		// Other
		else
			styledTextArea.setFont(new Font(Display.getCurrent(), "Monospace",
					12, SWT.NONE));
		styledTextArea.addExtendedModifyListener(new ExtendedModifyListener() {
			public void modifyText(ExtendedModifyEvent arg0) {
				if (undobycollapse) {
					arc1.redraw();
					arc2.redraw();
					return;
				}
				if (!ignoreUndo) {
					textchanges.push(new TextChanges(arg0.start, arg0.length,
							arg0.replacedText, false, -1, -1, false, -1));
					if (textchanges.size() > UndoLimit)
						textchanges.remove(0);
				}
				if (IsinCollapse(arg0.start, arg0.length, arg0.replacedText)) {
					arc1.redraw();
					arc2.redraw();
				} else {
					backdoc.set(doc.get());
					dealwithblockinfo(arg0.start, arg0.length,
							arg0.replacedText);
				}
			}
		});

		rulercontrol = arc2.getControl();
		rulercontrol.addMouseListener(new MouseListener() {
			public void mouseDoubleClick(MouseEvent arg0) {
			}

			public void mouseDown(MouseEvent arg0) {
			}

			public void mouseUp(MouseEvent arg0) {
				dealwithfold(arc2.toDocumentLineNumber(arg0.y));
			}
		});

		styledTextArea.addMouseListener(new MouseListener() {
			public void mouseDoubleClick(MouseEvent arg0) {
			}

			public void mouseDown(MouseEvent arg0) {
				if (resetHighlightneeded) {
					resetHighlights();
					resetHighlightneeded = false;
				}
			}

			public void mouseUp(MouseEvent arg0) {
				backdoc.set(doc.get());
			}
		});

		// resetBlockInfo();
	}

	/**
	 * This is the ruler that displays the line numbers.
	 */
	class LineNumberRuler extends AbstractRulerColumn {
		int digit = 2;
		int limit = 100;

		LineNumberRuler(int width) {
			this.setWidth(width);
			this.setTextInset(0);
			// this.setFont(new
			// Font(Display.getCurrent(),"Courier New",9,SWT.NONE));
			this.setDefaultBackground(new Color(Display.getCurrent(), 230, 230,
					230));
		}

		protected String computeText(int line) {
			int temp = getActualLineNum(line);
			if (temp >= limit) {
				digit++;
				limit = limit * 10;
				this.setWidth(8 * digit);
			}
			String tempstr = String.valueOf(temp);
			while (tempstr.length() != digit)
				tempstr = " " + tempstr;
			return tempstr;
		}
	}

	/**
	 * This is the ruler that displays the + and - for code folding.
	 */
	class CodeFoldRuler extends AbstractRulerColumn {
		CodeFoldRuler(int width) {
			this.setWidth(width);
			this.setDefaultBackground(new Color(Display.getCurrent(), 240, 240,
					240));
			this.setFont(new Font(Display.getCurrent(), "Courier New", 12,
					SWT.BOLD | SWT.COLOR_BLACK));
		}

		protected String computeText(int line) {
			for (int i = 0; i < BNGEditor.getBlocknum(); i++)
				if (line == blockinfo[i].line && blockinfo[i].valid) {
					if (blockinfo[i].folded) {
						return "+";
					} else {
						return "-";
					}
				}
			return "";
		}
	}

	int getActualLineNum(int line) {
		int temp = line;
		for (int i = 0; i < BNGEditor.getBlocknum(); i++)
			if (line > blockinfo[i].line && blockinfo[i].valid
					&& blockinfo[i].folded)
				temp = temp + blockinfo[i].lineend - blockinfo[i].line;
		return temp + 1;
	}

	void dealwithfold(int in) {
		boolean temp = false;
		for (int i = 0; i < BNGEditor.getBlocknum(); i++)
			if (in == blockinfo[i].line && blockinfo[i].valid) {
				blockinfo[i].folded = !blockinfo[i].folded;
				blockinfo[i].statechange = true;
				backdoc.set(doc.get());
				temp = true;
			}
		if (temp)
			UpdateProdoc(false, 0);
	}

	boolean IsinCollapse(int in1, int in2, String in3) {
		int[] temp = getlinenums(in1, in2, in3);
		for (int i = temp[0]; i <= temp[2]; i++)
			for (int j = 0; j < BNGEditor.getBlocknum(); j++)
				if (blockinfo[j].line == i && blockinfo[j].valid
						&& blockinfo[j].folded) {
					undobycollapse = true;
					blockinfo[j].folded = false;
					blockinfo[j].statechange = true;
				}
		if (undobycollapse) {
			Undo();
			doc.set(backdoc.get());
			for (int k = 0; k < BNGEditor.getBlocknum(); k++)
				if (blockinfo[k].statechange == false && blockinfo[k].folded)
					try {
						prodoc.removeMasterDocumentRange(
								blockinfo[k].caretbegin2,
								blockinfo[k].caretend2
										- blockinfo[k].caretbegin2);
					} catch (BadLocationException e) {
					}
			UpdateProdoc(false, 0);
			undobycollapse = false;
			return true;
		}
		return false;
	}

	void UpdateProdoc(boolean popneeded, int offset) {
		int temp;// line span
		for (int i = 0; i < BNGEditor.getBlocknum(); i++) {
			if (blockinfo[i].statechange) {
				try {
					blockinfo[i].statechange = false;
					temp = doc.getLineOfOffset(blockinfo[i].caretend1)
							- doc.getLineOfOffset(blockinfo[i].caretbegin1);
					if (blockinfo[i].folded) {
						try {
							prodoc.removeMasterDocumentRange(
									blockinfo[i].caretbegin2,
									blockinfo[i].caretend2
											- blockinfo[i].caretbegin2);
							if (popneeded) {
								TextChanges textchange = textchanges.pop();
								textchanges.push(new TextChanges(-1, -1, "",
										true,
										blockinfo[i].caretbegin2 - offset,
										blockinfo[i].caretend2 - offset, true,
										i));
								if (Prodoc2Doc(textchange.start) > blockinfo[i].caretbegin1)
									textchange.start = textchange.start
											- blockinfo[i].caretend2
											+ blockinfo[i].caretbegin2;
								textchanges.push(textchange);
							} else
								textchanges.push(new TextChanges(-1, -1, "",
										true, blockinfo[i].caretbegin2,
										blockinfo[i].caretend2, true, i));
						} catch (BadLocationException e) {
						}
						for (int j = 0; j < BNGEditor.getBlocknum(); j++) {
							if (blockinfo[j].valid) {
								if (blockinfo[j].line > blockinfo[i].line) {
									blockinfo[j].line = blockinfo[j].line
											- temp;
									blockinfo[j].lineend = blockinfo[j].lineend
											- temp;
								}
							} else {
								if (blockinfo[j].line > blockinfo[i].line)
									blockinfo[j].line = blockinfo[j].line
											- temp;
								if (blockinfo[j].lineend > blockinfo[i].line)
									blockinfo[j].lineend = blockinfo[j].lineend
											- temp;
							}
						}
					} else {
						try {
							prodoc.addMasterDocumentRange(
									blockinfo[i].caretbegin2,
									blockinfo[i].caretend2
											- blockinfo[i].caretbegin2);
							if (popneeded) {
								TextChanges textchange = textchanges.pop();
								textchanges.push(new TextChanges(-1, -1, "",
										true,
										blockinfo[i].caretbegin2 - offset,
										blockinfo[i].caretend2 - offset, false,
										i));
								if (Prodoc2Doc(textchange.start) > blockinfo[i].caretbegin1)
									textchange.start = textchange.start
											+ blockinfo[i].caretend2
											- blockinfo[i].caretbegin2;
								textchanges.push(textchange);
							} else
								textchanges.push(new TextChanges(-1, -1, "",
										true, blockinfo[i].caretbegin2,
										blockinfo[i].caretend2, false, i));
						} catch (BadLocationException e) {
						}
						for (int j = 0; j < BNGEditor.getBlocknum(); j++) {
							if (blockinfo[j].valid) {
								if (blockinfo[j].line > blockinfo[i].line) {
									blockinfo[j].line = blockinfo[j].line
											+ temp;
									blockinfo[j].lineend = blockinfo[j].lineend
											+ temp;
								}
							} else {
								if (blockinfo[j].line > blockinfo[i].line)
									blockinfo[j].line = blockinfo[j].line
											+ temp;
								if (blockinfo[j].lineend > blockinfo[i].line)
									blockinfo[j].lineend = blockinfo[j].lineend
											+ temp;
							}
						}
					}
				} catch (BadLocationException e1) {
				}
			}
		}
		SyntaxHighlight(0, styledTextArea.getText().length(), "", true);
		arc1.redraw();
		arc2.redraw();
	}

	void SyntaxHighlight(int in1, int in2, String in3, boolean global) {
		int[] temp = getlinenums(in1, in2, in3);
		String tempstr;
		Matcher m;
		boolean found;
		ArrayList<StyleRange> ranges = new ArrayList<StyleRange>();
		if (!global) {
			tempstr = "";
			int linecount = styledTextArea.getLineCount();
			for (int i = temp[0]; i <= temp[1] && i <= linecount - 1; i++)
				tempstr = tempstr + styledTextArea.getLine(i)
						+ styledTextArea.getLineDelimiter();
		} else
			tempstr = styledTextArea.getText()
					+ styledTextArea.getLineDelimiter();
		tempstr = " " + tempstr;

		for (int i = 0; i < keywords.size(); i++) {
			found = true;
			m = keywords.get(i).matcher(tempstr);
			int fstart = 0;
			while (found) {
				found = m.find(fstart);
				if (found) {
					fstart = m.end() - 1;
					if (i != BNGEditor.getBlocknum() * 2) {
						if (i == BNGEditor.getBlocknum() * 3) {
							// molecule names
							ranges.add(new StyleRange(m.start(1) - 1
									+ styledTextArea.getOffsetAtLine(temp[0]),
									m.group(1).length(), Display.getCurrent()
											.getSystemColor(SWT.COLOR_BLUE),
									null, SWT.NULL));
						} else
							// keywords
							ranges.add(new StyleRange(m.start(1) - 1
									+ styledTextArea.getOffsetAtLine(temp[0]),
									m.group(1).length(), color_purple, null,
									SWT.NULL));
					}
					// comment
					else {
						ranges.add(new StyleRange(m.start(1) - 1
								+ styledTextArea.getOffsetAtLine(temp[0]), m
								.group(1).length(), color_PaleGreen4, null,
								SWT.NULL));
					}
					/*
					 * if(i!=BNGEditor.blocknum*2) ranges.add(new
					 * StyleRange(m.start(1)-1 +
					 * textarea.getOffsetAtLine(temp[0]), m.group(1).length(),
					 * Display.getCurrent().getSystemColor(SWT.COLOR_BLUE),null,
					 * SWT.BOLD)); else ranges.add(new StyleRange(m.start(1)-1 +
					 * textarea.getOffsetAtLine(temp[0]), m.group(1).length(),
					 * Display
					 * .getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY),null,
					 * SWT.NULL));
					 */
				}
			}
		}

		ArrayList<StyleRange> tempArray = new ArrayList<StyleRange>();
		int int1, min;
		while (ranges.size() != 0)// sort
		{
			int1 = 0;
			min = ranges.get(0).start;
			for (int i = 1; i < ranges.size(); i++) {
				if (ranges.get(i).start < min) {
					int1 = i;
					min = ranges.get(i).start;
				}
			}
			if (tempArray.size() == 0
					|| ranges.get(int1).start >= tempArray
							.get(tempArray.size() - 1).start
							+ tempArray.get(tempArray.size() - 1).length)
				tempArray.add(ranges.get(int1));
			ranges.remove(int1);
		}
		ranges = tempArray;
		if (global)
			styledTextArea.replaceStyleRanges(0, tempstr.length()
					- styledTextArea.getLineDelimiter().length() - 1,
					(StyleRange[]) ranges.toArray(new StyleRange[0]));
		else
			styledTextArea.replaceStyleRanges(
					styledTextArea.getOffsetAtLine(temp[0]), tempstr.length()
							- styledTextArea.getLineDelimiter().length() - 1,
					(StyleRange[]) ranges.toArray(new StyleRange[0]));
	}

	int[] getlinenums(int in1, int in2, String in3) {
		int[] lines = new int[3]; // 0: start 1:newend 2:oldend
		if (in3.length() == 0) {
			lines[0] = styledTextArea.getLineAtOffset(in1);
			lines[1] = styledTextArea.getLineAtOffset(in1 + in2);
			lines[2] = lines[0];
		} else {
			Document tempdoc = new Document();
			String tempstr = "";
			if (in1 - 1 >= 0)
				tempstr = tempstr + styledTextArea.getText(0, in1 - 1);
			tempstr = tempstr + in3;
			if (in1 + in2 <= styledTextArea.getCharCount() - 1)
				tempstr = tempstr
						+ styledTextArea.getText(in1 + in2,
								styledTextArea.getCharCount() - 1);
			tempdoc.set(tempstr);
			try {
				lines[0] = tempdoc.getLineOfOffset(in1);
				lines[1] = styledTextArea.getLineAtOffset(in1 + in2);
				lines[2] = tempdoc.getLineOfOffset(in1 + in3.length());
			} catch (BadLocationException e) {
			}
		}
		// System.out.println(lines[0]+" "+lines[1]+" "+lines[2]);
		return lines;
	}

	class TextChanges// To be used together with Undo
	{
		int start;
		int length;
		String replacedText;
		boolean fold;
		int foldstart;
		int foldend;
		boolean expand;
		int blockindex;

		public TextChanges(int start, int length, String replacedText,
				boolean fold, int foldstart, int foldend, boolean expand,
				int blockindex) {
			this.start = start;
			this.length = length;
			this.replacedText = replacedText;
			this.fold = fold;
			this.foldstart = foldstart;
			this.foldend = foldend;
			;
			this.expand = expand;
			this.blockindex = blockindex;
		}
	}

	void Undo() {
		if (!textchanges.empty()) {
			TextChanges textchange = (TextChanges) textchanges.pop();
			if (textchange.fold) {
				try {
					int temp = doc.getLineOfOffset(textchange.foldend)
							- doc.getLineOfOffset(textchange.foldstart);
					if (!textchange.expand) {
						try {
							prodoc.removeMasterDocumentRange(
									textchange.foldstart, textchange.foldend
											- textchange.foldstart);
							blockinfo[textchange.blockindex].folded = true;
						} catch (BadLocationException e) {
						}
						for (int j = 0; j < BNGEditor.getBlocknum(); j++) {
							if (blockinfo[j].valid) {
								if (blockinfo[j].caretbegin1 > textchange.foldstart) {
									blockinfo[j].line = blockinfo[j].line
											- temp;
									blockinfo[j].lineend = blockinfo[j].lineend
											- temp;
								}
							} else {
								if (blockinfo[j].caretbegin1 > textchange.foldstart)
									blockinfo[j].line = blockinfo[j].line
											- temp;
								if (blockinfo[j].caretend1 > textchange.foldstart)
									blockinfo[j].lineend = blockinfo[j].lineend
											- temp;
							}
						}
					} else {
						try {
							prodoc.addMasterDocumentRange(textchange.foldstart,
									textchange.foldend - textchange.foldstart);
							blockinfo[textchange.blockindex].folded = false;
						} catch (BadLocationException e) {
						}
						for (int j = 0; j < BNGEditor.getBlocknum(); j++) {
							if (blockinfo[j].valid) {
								if (blockinfo[j].caretbegin1 > textchange.foldstart) {
									blockinfo[j].line = blockinfo[j].line
											+ temp;
									blockinfo[j].lineend = blockinfo[j].lineend
											+ temp;
								}
							} else {
								if (blockinfo[j].caretbegin1 > textchange.foldstart)
									blockinfo[j].line = blockinfo[j].line
											+ temp;
								if (blockinfo[j].caretend1 > textchange.foldstart)
									blockinfo[j].lineend = blockinfo[j].lineend
											+ temp;
							}
						}
					}
				} catch (BadLocationException ee) {
				}
				SyntaxHighlight(0, styledTextArea.getText().length(), "", true);
				arc1.redraw();
				arc2.redraw();
				return;
			}
			ignoreUndo = true;
			styledTextArea.replaceTextRange(textchange.start,
					textchange.length, textchange.replacedText);
			styledTextArea.setCaretOffset(textchange.start);
			int textchangeLineIndex = styledTextArea
					.getLineAtOffset(textchange.start);
			int topIndex = sv.getTopIndex();
			int bottomIndex = sv.getBottomIndex();
			// change top index if the changed text if not in display area
			if (textchangeLineIndex < topIndex
					|| textchangeLineIndex > bottomIndex) {
				styledTextArea.setTopIndex(textchangeLineIndex
						- (bottomIndex - topIndex) / 2);
			}
			ignoreUndo = false;
		}
	}

	class BlockInfo {
		int caretbegin1;// doc
		int caretbegin2;// doc
		int caretend1;// doc
		int caretend2;// doc
		int line;// prodoc
		int lineend;// prodoc
		boolean valid;
		boolean folded;
		boolean statechange;

		BlockInfo() {
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

	void dealwithblockinfo(int in1, int in2, String in3) {
		Matcher m;
		boolean found, research = false;
		int tcaret1, tcaret2;
		ArrayList<Integer> researchitem = new ArrayList<Integer>();
		int[] lines = getlinenums(in1, in2, in3);
		for (int i = lines[0]; i <= lines[2]; i++)
			for (int j = 0; j < BNGEditor.getBlocknum(); j++) {
				if (i == blockinfo[j].line && !blockinfo[j].folded) {
					research = true;
					blockinfo[j].caretbegin1 = -1;
					blockinfo[j].caretbegin2 = -1;
					blockinfo[j].line = -1;
					researchitem.add(j * 2);
					blockinfo[j].valid = false;
				}
				if (i == blockinfo[j].lineend && !blockinfo[j].folded) {
					research = true;
					blockinfo[j].caretend1 = -1;
					blockinfo[j].caretend2 = -1;
					blockinfo[j].lineend = -1;
					researchitem.add(j * 2 + 1);
					blockinfo[j].valid = false;
				}
			}

		String tempstr;
		int tempint = Prodoc2Doc(in1), strlength = in3.length();
		for (int j = 0; j < BNGEditor.getBlocknum(); j++) {
			if (blockinfo[j].caretbegin1 > tempint) {
				blockinfo[j].caretbegin1 = blockinfo[j].caretbegin1 - strlength
						+ in2;
				blockinfo[j].caretbegin2 = blockinfo[j].caretbegin2 - strlength
						+ in2;
				blockinfo[j].line = blockinfo[j].line + lines[1] - lines[2];
			}
			if (blockinfo[j].caretend1 > tempint) {
				blockinfo[j].caretend1 = blockinfo[j].caretend1 - strlength
						+ in2;
				blockinfo[j].caretend2 = blockinfo[j].caretend2 - strlength
						+ in2;
				blockinfo[j].lineend = blockinfo[j].lineend + lines[1]
						- lines[2];
			}
		}
		tempstr = " ";
		for (int i = lines[0]; i <= lines[1]; i++)
			tempstr = tempstr + styledTextArea.getLine(i)
					+ styledTextArea.getLineDelimiter();

		for (int i = 0; i < BNGEditor.getBlocknum() * 2; i++) {
			m = keywords.get(i).matcher(tempstr);
			if (i % 2 == 0) {
				found = true;
				tcaret1 = -1;
				tcaret2 = -1;
				int fstart = 0;
				while (found) {
					found = m.find(fstart);
					if (found) {
						fstart = m.end() - 1;
						tcaret1 = Prodoc2Doc(m.start(1) - 1
								+ styledTextArea.getOffsetAtLine(lines[0]));
						tcaret2 = tcaret1 + m.group(1).length();
					}
					if (found
							&& (tcaret1 < blockinfo[i / 2].caretbegin1 || blockinfo[i / 2].caretbegin1 == -1)
							&& validateCaret(tcaret1)) {
						if (blockinfo[i / 2].folded) {
							blockinfo[i / 2].folded = false;
							blockinfo[i / 2].statechange = true;
							TextChanges textchange = textchanges.peek();
							int offset = 0;
							if (Doc2Prodoc(blockinfo[i / 2].caretbegin1) > textchange.start)
								offset = textchange.length
										- textchange.replacedText.length();
							UpdateProdoc(true, offset);
						}
						blockinfo[i / 2].caretbegin1 = tcaret1;
						blockinfo[i / 2].caretbegin2 = tcaret2;
						blockinfo[i / 2].line = styledTextArea
								.getLineAtOffset(Doc2Prodoc(tcaret1));
						validateblock(i / 2);
						found = false;
					}
					arc1.redraw();
					arc2.redraw();
				}
			} else {
				tcaret1 = -1;
				tcaret2 = -1;
				found = true;
				int fstart = 0;
				while (found) {
					found = m.find(fstart);
					if (found)
						fstart = m.end() - 1;
					if (found
							&& validateCaret(Prodoc2Doc(m.start(1) - 1
									+ styledTextArea.getOffsetAtLine(lines[0])))) {
						tcaret1 = Prodoc2Doc(m.start(1) - 1
								+ styledTextArea.getOffsetAtLine(lines[0]));
						tcaret2 = tcaret1 + m.group(1).length();
					}
				}
				if (tcaret1 > blockinfo[i / 2].caretend1) {
					if (blockinfo[i / 2].folded) {
						blockinfo[i / 2].folded = false;
						blockinfo[i / 2].statechange = true;
						TextChanges textchange = textchanges.peek();
						int offset = 0;
						if (Doc2Prodoc(blockinfo[i / 2].caretbegin1) > textchange.start)
							offset = textchange.length
									- textchange.replacedText.length();
						UpdateProdoc(true, offset);
					}
					blockinfo[i / 2].caretend1 = tcaret1;
					blockinfo[i / 2].caretend2 = tcaret2;
					blockinfo[i / 2].lineend = styledTextArea
							.getLineAtOffset(Doc2Prodoc(tcaret1));
					validateblock(i / 2);
					arc1.redraw();
					arc2.redraw();
				}
			}
		}
		SyntaxHighlight(in1, in2, in3, false);
		if (research) {
			for (int k = 0; k < researchitem.size(); k++) {
				tempstr = " " + doc.get();
				m = keywords.get(researchitem.get(k)).matcher(tempstr);
				if (researchitem.get(k) % 2 == 0) {
					found = true;
					int fstart = 0;
					while (found) {
						found = m.find(fstart);
						if (found)
							fstart = m.end() - 1;
						if (found && validateCaret(m.start(1) - 1)) {
							tcaret1 = m.start(1) - 1;
							tcaret2 = tcaret1 + m.group(1).length();
							blockinfo[researchitem.get(k) / 2].caretbegin1 = tcaret1;
							blockinfo[researchitem.get(k) / 2].caretbegin2 = tcaret2;
							blockinfo[researchitem.get(k) / 2].line = styledTextArea
									.getLineAtOffset(Doc2Prodoc(tcaret1));
							found = false;
						}
					}
				} else {
					tcaret1 = -1;
					tcaret2 = -1;
					found = true;
					int fstart = 0;
					while (found) {
						found = m.find(fstart);
						if (found)
							fstart = m.end() - 1;
						if (found && validateCaret(m.start(1) - 1)) {
							tcaret1 = m.start(1) - 1;
							tcaret2 = tcaret1 + m.group(1).length();
						}
					}
					if (tcaret1 > blockinfo[researchitem.get(k) / 2].caretend1) {
						blockinfo[researchitem.get(k) / 2].caretend1 = tcaret1;
						blockinfo[researchitem.get(k) / 2].caretend2 = tcaret2;
						blockinfo[researchitem.get(k) / 2].lineend = styledTextArea
								.getLineAtOffset(Doc2Prodoc(tcaret1));
					}
				}

			}
			for (int k = 0; k < BNGEditor.getBlocknum(); k++)
				validateblock(k);
			arc1.redraw();
		}
		arc2.redraw();
	}

	boolean validateCaret(int caretin) {
		try {
			int linenum = doc.getLineOfOffset(caretin);
			int caretinline = caretin - doc.getLineOffset(linenum);
			String tempstr = doc.get(doc.getLineOffset(linenum),
					doc.getLineLength(linenum))
					+ doc.getDefaultLineDelimiter();
			Matcher m = BNGEditor.getKeywords()
					.get(BNGEditor.getBlocknum() * 2).matcher(tempstr);
			boolean found = m.find();
			if (found && m.start(1) < caretinline)
				return false;
		} catch (BadLocationException e) {
		}
		return true;
	}

	int Prodoc2Doc(int in)// caret
	{
		ArrayList<Integer> temp = new ArrayList<Integer>();
		ArrayList<Integer> temp2 = new ArrayList<Integer>();
		int min;
		int temp3;
		for (int i = 0; i < BNGEditor.getBlocknum(); i++)
			// add valid
			if (blockinfo[i].valid)
				temp.add(i);
		if (temp.size() == 0)
			return in;
		while (temp.size() != 0)// sort
		{
			min = temp.get(0);
			temp3 = 0;
			for (int i = 0; i < temp.size(); i++)
				if (blockinfo[temp.get(i)].caretbegin1 < blockinfo[min].caretbegin1) {
					min = temp.get(i);
					temp3 = i;
				}
			temp2.add(min);
			temp.remove(temp3);
		}
		for (int i = 0; i < temp2.size(); i++)
			if (in > blockinfo[temp2.get(i)].caretbegin2
					&& blockinfo[temp2.get(i)].folded)
				in = in - blockinfo[temp2.get(i)].caretbegin2
						+ blockinfo[temp2.get(i)].caretend2;
		return in;
	}

	int Doc2Prodoc(int in)// caret
	{
		ArrayList<Integer> temp = new ArrayList<Integer>();
		ArrayList<Integer> temp2 = new ArrayList<Integer>();
		int min;
		int temp3;
		for (int i = 0; i < BNGEditor.getBlocknum(); i++)
			// add valid
			if (blockinfo[i].valid)
				temp.add(i);
		if (temp.size() == 0)
			return in;
		while (temp.size() != 0)// sort
		{
			min = temp.get(0);
			temp3 = 0;
			for (int i = 0; i < temp.size(); i++)
				if (blockinfo[temp.get(i)].caretbegin1 < blockinfo[min].caretbegin1) {
					min = temp.get(i);
					temp3 = i;
				}
			temp2.add(min);
			temp.remove(temp3);
		}
		int tempint = in;
		for (int i = 0; i < temp2.size(); i++) {
			if (in > blockinfo[temp2.get(i)].caretbegin2
					&& in < blockinfo[temp2.get(i)].caretend2
					&& blockinfo[temp2.get(i)].folded)
				return -1;
			if (in >= blockinfo[temp2.get(i)].caretend2
					&& blockinfo[temp2.get(i)].folded)
				tempint = tempint
						- (blockinfo[temp2.get(i)].caretend2 - blockinfo[temp2
								.get(i)].caretbegin2);
		}
		return tempint;
	}

	void validateblock(int blockindex) {
		if (blockinfo[blockindex].caretbegin2 < blockinfo[blockindex].caretend1
				&& blockinfo[blockindex].caretbegin1 != -1) {
			blockinfo[blockindex].valid = true;
		} else {
			blockinfo[blockindex].valid = false;
		}

		for (int i = 0; i < BNGEditor.getBlocknum(); i++)
			if ((blockinfo[i].caretbegin2 != -1
					&& blockinfo[blockindex].caretbegin2 > blockinfo[i].caretbegin2 && blockinfo[blockindex].caretbegin2 < blockinfo[i].caretend1)
					|| (blockinfo[i].caretbegin2 != -1
							&& blockinfo[blockindex].caretend1 > blockinfo[i].caretbegin2 && blockinfo[blockindex].caretend1 < blockinfo[i].caretend1)) {
				blockinfo[blockindex].valid = false;
				blockinfo[i].valid = false;
				if (blockinfo[i].folded) {
					blockinfo[i].folded = false;
					blockinfo[i].statechange = true;
					TextChanges textchange = textchanges.peek();
					int offset = 0;
					if (Doc2Prodoc(blockinfo[i].caretbegin1) > textchange.start)
						offset = textchange.length
								- textchange.replacedText.length();
					UpdateProdoc(true, offset);
				}
			}

		for (int i = 0; i < BNGEditor.getBlocknum(); i++)
			if ((blockinfo[blockindex].caretbegin2 != -1
					&& blockinfo[i].caretbegin2 > blockinfo[blockindex].caretbegin2 && blockinfo[i].caretbegin2 < blockinfo[blockindex].caretend1)
					|| (blockinfo[blockindex].caretbegin2 != -1
							&& blockinfo[i].caretend1 > blockinfo[blockindex].caretbegin2 && blockinfo[i].caretend1 < blockinfo[blockindex].caretend1)) {
				blockinfo[blockindex].valid = false;
				blockinfo[i].valid = false;
				if (blockinfo[i].folded) {
					blockinfo[i].folded = false;
					blockinfo[i].statechange = true;
					TextChanges textchange = textchanges.peek();
					int offset = 0;
					if (Doc2Prodoc(blockinfo[i].caretbegin1) > textchange.start)
						offset = textchange.length
								- textchange.replacedText.length();
					UpdateProdoc(true, offset);
				}
			}
	}

	void cut() {
		styledTextArea.cut();
	}

	void copy() {
		styledTextArea.copy();
	}

	void paste() {
		styledTextArea.paste();
	}

	void selectall() {
		boolean updateneeded = false;
		for (int i = 0; i < BNGEditor.getBlocknum(); i++)
			if (blockinfo[i].folded) {
				updateneeded = true;
				blockinfo[i].folded = false;
				blockinfo[i].statechange = true;
			}
		if (updateneeded)
			UpdateProdoc(false, 0);
		styledTextArea.selectAll();
	}

	void deleteall() {
		selectall();
		for (int i = 0; i < BNGEditor.getBlocknum(); i++)
			blockinfo[i] = new BlockInfo();
		doc.set(" ");
		try {
			prodoc.addMasterDocumentRange(0, doc.getLength());
		} catch (BadLocationException e) {
		}
		styledTextArea.setText("");
	}

	public void find() {
		if (styledTextArea.getCaretOffset() == styledTextArea.getText()
				.length())
			styledTextArea.setSelection(0, 0);
		findShell = new Shell(BNGEditor.getMainEditorShell(), SWT.DIALOG_TRIM);
		findShell.setText("Find...");
		// set grid layout
		findShell.setLayout(new GridLayout(3, true));

		findShell.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent arg0) {
				resetHighlightneeded = true;
			}
		});

		// find label
		Label findlabel1 = new Label(findShell, SWT.NONE);
		findlabel1.setText("  Find :");

		// find text
		findtext1 = new Text(findShell, SWT.BORDER);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		findtext1.setLayoutData(gridData);

		// add listener for find text
		findtext1.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (findtext1.getText().length() == 0) {
					findbutton1.setEnabled(false);
					findbutton2.setEnabled(false);
				} else {
					findbutton1.setEnabled(true);
					findbutton2.setEnabled(true);
				}
			}
		});

		// find button
		findbutton1 = new Button(findShell, SWT.NONE);
		findbutton1.setText("Find");
		gridData = new GridData();
		gridData.widthHint = 80;
		findbutton1.setLayoutData(gridData);

		// add listener for find button
		findbutton1.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				redrawRulers();
				resetHighlights();
				int a = styledTextArea.getCaretOffset();
				int b = 0, StartFindPos = a;
				String strA = styledTextArea.getText();
				String strB = findtext1.getText();

				StyleRange sr = new StyleRange();
				if (a >= 0) {
					a = strA.indexOf(strB, StartFindPos);
					b = strB.length();
					StartFindPos = a + b;
					if (a == -1) {
						MessageBox mb = new MessageBox(findShell,
								SWT.ICON_INFORMATION);
						mb.setMessage("End of Search !");
						mb.setText("End of Search");
						mb.open();
						styledTextArea.setSelection(0, 0);
					} else {
						styledTextArea.setSelection(a, StartFindPos);
						sr.background = new Color(Display.getCurrent(), 51,
								170, 255);
						sr.start = a;
						sr.length = StartFindPos - a;
						styledTextArea.setStyleRange(sr);
					}
				}
			}
		});
		findbutton1.setEnabled(false);

		// find all button
		findbutton2 = new Button(findShell, SWT.NONE);
		findbutton2.setText("Find All");
		findbutton2.setLayoutData(gridData);

		// add listener for find all button
		findbutton2.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				resetHighlights();
				StyleRange sr;
				int a = 0, b = 0, StartFindPos = 0;
				String strA = styledTextArea.getText();
				String strB = findtext1.getText();
				a = 0;
				StartFindPos = 0;
				styledTextArea.setSelection(0, 0);
				while (true) {
					redrawRulers();
					a = strA.indexOf(strB, StartFindPos);
					b = strB.length();
					StartFindPos = a + b;
					if (a == -1) {
						MessageBox mb = new MessageBox(findShell,
								SWT.ICON_INFORMATION);
						mb.setMessage("End of Search !");
						mb.setText("End of Search");
						mb.open();
						break;
					} else {
						sr = new StyleRange();
						styledTextArea.setSelection(a, StartFindPos);
						sr.background = color_DarkGoldenrod1;
						sr.start = a;
						sr.length = StartFindPos - a;
						styledTextArea.setStyleRange(sr);
					}
				}
			}
		});
		findbutton2.setEnabled(false);

		// cancel button
		Button findbutton3 = new Button(findShell, SWT.NONE);
		findbutton3.setText("Cancel");
		findbutton3.setLayoutData(gridData);

		// add listener for cancel button
		findbutton3.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				findShell.dispose();
			}
		});

		findShell.pack();
		findShell.open();
	}

	void resetHighlights() {
		StyleRange[] srall = styledTextArea.getStyleRanges();
		for (int i = 0; i < srall.length; i++)
			if (srall[i].background != null) {
				srall[i].background = null;
				styledTextArea.setStyleRange(srall[i]);
				SyntaxHighlight(srall[i].start, srall[i].length, "", false);
			}
	}

	public void replace() {
		if (styledTextArea.getCaretOffset() == styledTextArea.getText()
				.length())
			styledTextArea.setSelection(0, 0);
		replaceShell = new Shell(BNGEditor.getMainEditorShell(),
				SWT.DIALOG_TRIM);
		replaceShell.setText("Replace...");
		// set grid layout
		replaceShell.setLayout(new GridLayout(3, true));

		// add listener for replace shell
		replaceShell.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent arg0) {
				resetHighlightneeded = true;
			}
		});

		// find label
		Label replacelabel1 = new Label(replaceShell, SWT.NONE);
		replacelabel1.setText("Find:");

		// find text
		replacetext1 = new Text(replaceShell, SWT.BORDER);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		replacetext1.setLayoutData(gridData);

		// add listener for find text
		replacetext1.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (replacetext1.getText().length() == 0) {
					replacebutton1.setEnabled(false);
					replacebutton2.setEnabled(false);
					replacebutton3.setEnabled(false);
				} else if (replacetext2.getText().length() != 0) {
					replacebutton1.setEnabled(true);
					replacebutton2.setEnabled(true);
					replacebutton3.setEnabled(true);
				} else
					replacebutton1.setEnabled(true);
			}
		});

		// replace label
		Label replacelabel2 = new Label(replaceShell, SWT.NONE);
		replacelabel2.setText("Replace:");

		// replace text
		replacetext2 = new Text(replaceShell, SWT.BORDER);
		replacetext2.setLayoutData(gridData);

		// add listener for replace text
		replacetext2.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (replacetext2.getText().length() == 0) {
					replacebutton2.setEnabled(false);
					replacebutton3.setEnabled(false);
				} else if (replacetext1.getText().length() != 0) {
					replacebutton2.setEnabled(true);
					replacebutton3.setEnabled(true);
				}
			}
		});

		// empty label to control layout
		new Label(replaceShell, SWT.NONE).setText("");

		// find button
		replacebutton1 = new Button(replaceShell, SWT.NONE);
		replacebutton1.setText("Find");
		gridData = new GridData();
		gridData.widthHint = 100;
		replacebutton1.setLayoutData(gridData);

		// add listener for find button
		replacebutton1.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				resetHighlights();
				int a = styledTextArea.getCaretOffset();
				int b = 0, StartFindPos = a;
				String strA = styledTextArea.getText();
				String strB = replacetext1.getText();
				StyleRange sr = new StyleRange();

				if (a >= 0) {
					a = strA.indexOf(strB, StartFindPos);
					b = strB.length();
					StartFindPos = a + b;
					if (a == -1) {
						MessageBox mb = new MessageBox(replaceShell,
								SWT.ICON_INFORMATION);
						mb.setMessage("End of Search !");
						mb.setText("End of Search");
						mb.open();
						styledTextArea.setSelection(0, 0);
					} else {
						styledTextArea.setSelection(a, StartFindPos);
						sr.background = new Color(Display.getCurrent(), 51,
								170, 255);
						sr.start = a;
						sr.length = StartFindPos - a;
						styledTextArea.setStyleRange(sr);
					}
				}
			}
		});
		replacebutton1.setEnabled(false);

		// replace button
		replacebutton2 = new Button(replaceShell, SWT.NONE);
		replacebutton2.setText("Replace");
		replacebutton2.setLayoutData(gridData);

		// add listener for replace button
		replacebutton2.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int a = styledTextArea.getCaretOffset(), b = 0, StartFindPos = a;
				String strA = styledTextArea.getText();
				String strB = replacetext1.getText();

				if (styledTextArea.getSelectionText() != null) {
					if (styledTextArea.getSelectionText().equals(strB)) {
						styledTextArea.replaceTextRange(
								styledTextArea.getCaretOffset() - strB.length(),
								strB.length(), replacetext2.getText());
						return;
					}
				}
				resetHighlights();
				if (a >= 0) {
					a = strA.indexOf(strB, StartFindPos);
					b = strB.length();
					StartFindPos = a + b;
					if (a == -1) {
						MessageBox mb = new MessageBox(replaceShell,
								SWT.ICON_INFORMATION);
						mb.setMessage("End of Search !");
						mb.setText("End of Search");
						mb.open();
						styledTextArea.setSelection(0, 0);
						a = 0;
						StartFindPos = 0;
					} else {
						styledTextArea.setSelection(a, StartFindPos);
					}
				}
				if (StartFindPos - a != 0)
					styledTextArea.replaceTextRange(a, b,
							replacetext2.getText());
			}
		});
		replacebutton2.setEnabled(false);

		// empty label to control layout
		new Label(replaceShell, SWT.NONE).setText("");

		// replace all button
		replacebutton3 = new Button(replaceShell, SWT.NONE);
		replacebutton3.setText("Replace All");
		replacebutton3.setLayoutData(gridData);

		// add listener for replace all button
		replacebutton3.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int a = 0, b = 0, StartFindPos = 0;
				String strRepleaceAll = replacetext2.getText();
				while (a > -1) {
					String strA = styledTextArea.getText();
					String strB = replacetext1.getText();
					a = strA.indexOf(strB, StartFindPos);
					if (a == -1) {
						break;
					}
					b = strB.length();
					StartFindPos = a + b;
					styledTextArea.setSelection(a, StartFindPos);
					styledTextArea.replaceTextRange(a, b, strRepleaceAll);
				}
				MessageBox mb = new MessageBox(replaceShell,
						SWT.ICON_INFORMATION);
				mb.setMessage("Replace complete !");
				mb.setText("Replace All");
				mb.open();
				styledTextArea.setSelection(0, 0);
			}
		});
		replacebutton3.setEnabled(false);

		// cancel button
		Button replacebutton4 = new Button(replaceShell, SWT.NONE);
		replacebutton4.setText("Cancel");
		replacebutton4.setLayoutData(gridData);

		// add listener for cancel button
		replacebutton4.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				replaceShell.dispose();
			}
		});
		replaceShell.pack();
		replaceShell.open();
	}

	/**
	 * 
	 * @param in
	 */
	void selectErrorLine(int in) {

		ArrayList<Integer> temp = new ArrayList<Integer>();
		ArrayList<Integer> temp2 = new ArrayList<Integer>();

		int line = in;
		int min;
		int temp3;

		// For each block info object,
		// if the block is valid is folded,
		// then add it to the temp arraylist.
		for (int i = 0; i < BNGEditor.getBlocknum(); i++)
			// add valid
			if (blockinfo[i].valid && blockinfo[i].folded)
				temp.add(i);

		// If there were no valid and folded blocks.
		if (temp.size() == 0) {
			// if the line passed in is not the last line, but inside of
			// the document.
			if (line < styledTextArea.getLineCount() - 1) {
				// Set the selection to be from the offset at the line
				// to the offset of the next line minus the newline character
				styledTextArea.setSelection(
						styledTextArea.getOffsetAtLine(line),
						styledTextArea.getOffsetAtLine(line + 1)
								- styledTextArea.getLineDelimiter().length());
			}

			// if the line passed in the last line (or after)
			else {
				// set the selection to be from the offset of the last line
				// to the last character in the text area.
				styledTextArea.setSelection(
						styledTextArea.getOffsetAtLine(line),
						styledTextArea.getOffsetAtLine(line)
								+ styledTextArea.getCharCount());
			}

			arc1.redraw();
			arc2.redraw();
			return;
		}

		/*-** If the code reaches here, then there was either an invalid block,
		  -** or a block that was folded.
		 *
		 */

		while (temp.size() != 0)// sort
		{
			min = temp.get(0);
			temp3 = 0;
			for (int i = 0; i < temp.size(); i++)
				if (blockinfo[temp.get(i)].caretbegin1 < blockinfo[min].caretbegin1) {
					min = temp.get(i);
					temp3 = i;
				}
			temp2.add(min);
			temp.remove(temp3);
		}
		while (temp2.size() != 0) {
			if (line > blockinfo[temp2.get(0)].line) {
				if (line + blockinfo[temp2.get(0)].line
						- blockinfo[temp2.get(0)].lineend > blockinfo[temp2
							.get(0)].line) {
					line = line + blockinfo[temp2.get(0)].line
							- blockinfo[temp2.get(0)].lineend;
					temp2.remove(0);
				} else {
					blockinfo[temp2.get(0)].folded = false;
					blockinfo[temp2.get(0)].statechange = true;
					UpdateProdoc(false, 0);
					styledTextArea.setSelection(
							styledTextArea.getOffsetAtLine(line),
							styledTextArea.getOffsetAtLine(line + 1)
									- styledTextArea.getLineDelimiter()
											.length());
					arc1.redraw();
					arc2.redraw();
					return;
				}
			} else {
				styledTextArea.setSelection(
						styledTextArea.getOffsetAtLine(line),
						styledTextArea.getOffsetAtLine(line + 1)
								- styledTextArea.getLineDelimiter().length());
				arc1.redraw();
				arc2.redraw();
				return;
			}
		}
		styledTextArea.setSelection(styledTextArea.getOffsetAtLine(line),
				styledTextArea.getOffsetAtLine(line + 1)
						- styledTextArea.getLineDelimiter().length());
		arc1.redraw();
		arc2.redraw();
		return;
	}

	// TODO start here with testing
	private void findFromRegExp(String regExp) {
		resetHighlights();

		StyleRange sr;

		String text = styledTextArea.getText();

		int caretPosition = styledTextArea.getCaretOffset();

		styledTextArea.setRedraw(false);

		Pattern p = Pattern.compile(regExp);
		Matcher m = p.matcher(text);

		while (m.find()) {
			sr = new StyleRange();
			styledTextArea.setSelection(m.start(), m.end());
			sr.background = color_DarkGoldenrod1;
			sr.start = m.start();
			sr.length = m.end() - m.start();
			styledTextArea.setStyleRange(sr);
		}

		styledTextArea.setCaretOffset(caretPosition);
		styledTextArea.setRedraw(true);
	}

	private void findFromString(String string) {

		// System.out.println("Trying to find \"" + string + "\"");

		resetHighlights();

		StyleRange sr;
		int a = 0, b = 0, startFindPos = 0;

		String text = styledTextArea.getText();

		int caretPosition = styledTextArea.getCaretOffset();

		styledTextArea.setRedraw(false);

		while (true) {
			a = text.indexOf(string, startFindPos);
			b = string.length();

			startFindPos = a + b;

			if (a == -1) {
				break;
			}

			else {
				sr = new StyleRange();
				styledTextArea.setSelection(a, startFindPos);
				sr.background = color_DarkGoldenrod1;
				sr.start = a;
				sr.length = startFindPos - a;
				styledTextArea.setStyleRange(sr);
			}
		}

		styledTextArea.setSelection(caretPosition, caretPosition);
		// textarea.setCaretOffset(caretPosition);

		styledTextArea.setRedraw(true);
	}

	/**
	 * Given the ruleText, select the rule in the text.
	 * 
	 * @param ruleText
	 */
	public boolean selectRuleForText(String ruleText, boolean resetHighlights) {
		if (resetHighlights)
			resetHighlights();

		StyleRange sr;

		// Remove the rates.
		if (ruleText.contains(")"))
			;
		ruleText = ruleText.substring(0, ruleText.lastIndexOf(")") + 1);

		// System.out.println("Rule Received in Text: " + ruleText);

		String text = styledTextArea.getText();

		Pattern p = producePatternForRuleString(ruleText);
		Matcher m = p.matcher(text);

		if (m.find()) {
			sr = new StyleRange();
			styledTextArea.setSelection(m.start(), m.end());
			sr.background = color_DarkGoldenrod1;
			sr.start = m.start();
			sr.length = m.end() - m.start();
			styledTextArea.setStyleRange(sr);
			return true;
		} else {
			return false;
		}
	}

	private Pattern producePatternForRuleString(String rule) {
		// Start the regular expression as the rule
		String regexp = rule;// .replace(" ", "");

		if (rule.contains("\n"))
			;
		{
			regexp = regexp.replace("\n", "");
		}

		String delimiter = styledTextArea.getLineDelimiter();

		// put an optional pair of backslashes between every character.
		// This has to happen before the other special characters are escaped.
		regexp = regexp.replace("", "\\s*\\\\?\\s*" + delimiter + "?");

		// This makes the rule match for either forward or bidirectional.
		// regexp = regexp.replace("<", "<?");

		// Escape the parentheses
		regexp = regexp.replace("(", "\\(");
		regexp = regexp.replace(")", "\\)");

		// Escape the +
		regexp = regexp.replace("+", "\\+");

		// Escape the !
		regexp = regexp.replace("!", "\\!");

		// Escape the ~
		// regexp = regexp.replace("~", "\\~");

		// Escape the .
		regexp = regexp.replace(".", "\\.");

		Pattern p = Pattern.compile(regexp);

		// DEBUG
		// System.out.println("\tRule: " + rule);
		// System.out.println("\tRegex: " + regexp);

		return p;
	}

	/**
	 * Called when a rule is selected in the influence graph.
	 */
	public void ruleSelectedInInfluenceGraph(VisualItem rule) {
		resetHighlights();

		// rules will only have x -> y from the influence graph,
		// but could be of the form x -> y, x <-> y, or y <-> x
		// in the text.

		String ruleText = (String) rule.get("rulename");
		// ruleText = ruleText.substring(0,ruleText.lastIndexOf(")")+1);

		// First try the forward rule and return if it has a match.
		if (selectRuleForText(ruleText, true))
			return;

		// Then try the bidirectional version of the forward rule.
		// Return on a match.
		String forward_bi = ruleText.replace("->", "<->");
		if (selectRuleForText(forward_bi, true))
			return;

		// Finally, try the bidirectional reverse rule.
		String reverse_bi = forward_bi.substring(forward_bi.indexOf('>') + 1,
				forward_bi.length())
				+ "<->"
				+ forward_bi.substring(0, forward_bi.indexOf('<'));
		selectRuleForText(reverse_bi, true);
	}

	/**
	 * When a Molecule is selected, this is called.
	 */
	public void moleculeSelectedInContactMap(VisualItem molecule) {
		String moleculeName = ((String) molecule.get("molecule")).trim();
		findFromRegExp(moleculeName);
	}

	/**
	 * Called when a rule is selected in the contact map.
	 */
	public void ruleSelectedInContactMap(VisualRule rule) {
		selectRuleForText(rule.getName(), true);
	}

	public void redrawRulers() {
		arc1.redraw();
		arc2.redraw();
	}

	public StyledText getStyledTextArea() {
		return styledTextArea;
	}

	public void clearSelectionFromInfluenceGraph() {
		resetHighlights();
		styledTextArea.setSelection(styledTextArea.getCaretOffset(),
				styledTextArea.getCaretOffset());
	}

	public void componentSelectedInContactMap(VisualItem componentItem) {
		// TODO only the component from that molecule.
		String name = ((String) componentItem.get(VisualItem.LABEL)).trim();
		String moleculeName = ((String) componentItem.get("molecule")).trim();

		System.out.println(name + " is a part of " + moleculeName);
		System.out
				.println(moleculeName + "\\(^[\\)]" + name + "^[\\)]" + "\\)");

		findFromRegExp(moleculeName + "\\s*\\((\\s|[^\\)])*" + name
				+ "([^\\)]|\\s)*\\)");

		// findFromRegExp(moleculeName + "\\(^[\\)]" +name +"^[\\)]" + "\\)" );
	}

	public void edgeSelectedInContactMap(VisualItem edge) {
		resetHighlights();
		ArrayList rules = (ArrayList<VisualRule>) edge.get("rules");

		// TODO Hub edges do not have rules. I should get the source nodes
		// and see if they have rules.
		// there is a "type" value of "hub" for hub nodes.
		// and a "rules" arraylist.
		if (rules == null)
			return;

		Iterator<VisualRule> ruleIt = rules.iterator();

		while (ruleIt.hasNext()) {
			selectRuleForText(ruleIt.next().getName(), false);
		}
	}

	public void stateSelectedInContactMap(VisualItem stateItem) {
		// TODO Get the molecule that the component is in. This will take
		// ignoring other components and states in the text. It might not be
		// necessary since biologically there may not be components with the
		// same name in different molecules, but it is possible in the language.
		String component = ((String) stateItem.get("component")).trim();
		String state = ((String) stateItem.get(VisualItem.LABEL)).trim();
		findFromRegExp(component + "~" + state);
	}

	public void hubSelectedInContactMap(VisualItem hubItem) {
		// TODO
		ArrayList rules = (ArrayList<VisualRule>) hubItem.get("rules");

		// TODO Hub edges do not have rules. I should get the source nodes
		// and see if they have rules.
		// there is a "type" value of "hub" for hub nodes.
		// and a "rules" arraylist.
		if (rules == null)
			return;

		Iterator<VisualRule> ruleIt = rules.iterator();

		while (ruleIt.hasNext()) {
			selectRuleForText(ruleIt.next().getName(), false);
		}
	}

	public void compartmentSelectedInContactMap(VisualItem compartmentItem) {
		String name = ((String) compartmentItem.get("compartment")).trim();
		findFromString(name);
	}

	public void clearSelectionFromContactMap() {
		resetHighlights();
		styledTextArea.setSelection(styledTextArea.getCaretOffset(),
				styledTextArea.getCaretOffset());
	}

	// All locally handled.
	public void moleculeSelectedInText(String moleculeText) { /* Local */
	}

	public void ruleSelectedInText(String ruleText) { /* Local */
	}

	public void clearSelectionFromText() {/* Local */
	}

	public void componentSelectedInText(String componentText) {/* Local */
	}

	public void stateSelectedInText(String stateText) {/* Local */
	}

	public void compartmentSelectedInText(VisualItem compartment) {/* Local */
	}
}