package editor;

import java.awt.Dimension;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.awt.Color;
import javax.swing.JFrame;

import networkviewer.NetworkViewer;
import networkviewer.VisualRule;

import editor.BNGEditor;
import editor.contactmap.Bond;
import editor.contactmap.Molecule;
import editor.contactmap.PotentialBond;
import editor.contactmap.Rule;

import prefuse.Display;
import prefuse.Visualization;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.visual.AggregateItem;
import prefuse.visual.AggregateTable;
import prefuse.visual.EdgeItem;
import prefuse.visual.VisualItem;


public class Test {
	
	ArrayList<Molecule> molecules = new ArrayList<Molecule>();
	ArrayList<Bond> bonds = new ArrayList<Bond>();
	ArrayList<Rule> rules = new ArrayList<Rule>();
	
	private boolean ruleparsevalid;
	private ArrayList<PotentialBond> pbonds = new ArrayList<PotentialBond>();
	private boolean flexiblestate = false;
	
	public Test(String molestr, String rulestr, boolean moleculetype)
	{
		
	}
	
	public static void main(String[] args)
	{
		System.out.println("This worked.");
	}
}
