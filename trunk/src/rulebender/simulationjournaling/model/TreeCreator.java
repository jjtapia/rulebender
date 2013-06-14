package rulebender.simulationjournaling.model;

import java.util.Date;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.data.Tree;
import prefuse.data.io.DataIOException;
import prefuse.data.parser.DataParseException;
import prefuse.data.parser.DataParser;
import prefuse.data.parser.ParserFactory;

public class TreeCreator {

	private InputSource m_xml_source;
	private String m_xml;
    private ParserFactory m_pf;
	
	public TreeCreator() {
		m_xml = null;
		m_pf = ParserFactory.getDefaultFactory();
	} //TreeCreator (constructor)
	
	public TreeCreator(String xml) {
		m_xml = xml;
		m_pf = ParserFactory.getDefaultFactory();
	} //TreeCreator (constructor)
	
	public TreeCreator(InputSource xml) {
		m_xml_source = xml;
		m_pf = ParserFactory.getDefaultFactory();
	} //TreeCreator (constructor)
	
    public Graph readGraph(InputSource inputSource) throws DataIOException {
    	m_xml_source = inputSource;
    	
        try {       
            TreeMLHandler handler = new TreeMLHandler();
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(m_xml_source, handler);
            return handler.getTree();
        } catch (Exception e) {
            throw new DataIOException(e);
        } //try-catch
    } //readGraph
	
	/**
     * String tokens used in the TreeML format.
     */
    public static interface Tokens {
        public static final String TREE   = "tree";
        public static final String BRANCH = "branch";
        public static final String LEAF   = "leaf";
        public static final String ATTR   = "attribute";
        public static final String NAME   = "name";
        public static final String VALUE  = "value";
        public static final String TYPE   = "type";
        
        public static final String DECLS  = "declarations";
        public static final String DECL   = "attributeDecl";
        
        public static final String INT = "Int";
        public static final String INTEGER = "Integer";
        public static final String LONG = "Long";
        public static final String FLOAT = "Float";
        public static final String REAL = "Real";
        public static final String STRING = "String";
        public static final String DATE = "Date";
        public static final String CATEGORY = "Category";
        
        // prefuse-specific allowed types
        public static final String BOOLEAN = "Boolean";
        public static final String DOUBLE = "Double";
    }
    
    /**
     * A SAX Parser for TreeML data files.
     */
    public class TreeMLHandler extends DefaultHandler implements Tokens {
        
        private Table m_nodes = null;
        private Tree m_tree = null;
        
        private Node m_activeNode = null;
        private boolean m_inSchema = true;
        
        public void startDocument() {
            m_tree = new Tree();
            m_nodes = m_tree.getNodeTable();
        } //startDocument
        
        private void schemaCheck() {
            if (m_inSchema) {
                m_inSchema = false;
            } //if
        } //schemaCheck
        
        public void endElement(String namespaceURI, String localName, String qName) {
            if (qName.equals(BRANCH) || qName.equals(LEAF)) {
                m_activeNode = m_activeNode.getParent();
            } //if
        } //endElement
        
        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {           
            if (qName.equals(DECL)) {
                if (!m_inSchema) {
                    throw new RuntimeException("All declarations must be done before nodes begin");
                } //if
                
                String name = atts.getValue(NAME);
                String type = atts.getValue(TYPE);
                Class t = parseType(type);
                m_nodes.addColumn(name, t);
            
            } else if (qName.equals(BRANCH) || qName.equals(LEAF)) {
                schemaCheck();
                
                // parse a node element
                Node n;
                if (m_activeNode == null) {
                    n = m_tree.addRoot();
                } else {
                    n = m_tree.addChild(m_activeNode);
                } //if-else
                m_activeNode = n;
                
            } else if (qName.equals(ATTR)) {
                // parse an attribute
                parseAttribute(atts);
            } //if-else
        } //startElement
        
        protected void parseAttribute(Attributes atts) {
            String alName, name = null, value = null;
            
            for (int i = 0; i < atts.getLength(); i++) {
                alName = atts.getQName(i);
                if (alName.equals(NAME)) {
                    name = atts.getValue(i);
                } else if (alName.equals(VALUE)) {
                    value = atts.getValue(i);
                } //if-else
            } //for
            
            if (name == null || value == null) {
                System.err.println("Attribute under-specified");
                return;
            } //if

            try {
                Object val = parse(value, m_nodes.getColumnType(name));
                m_activeNode.set(name, val);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } //try-catch
        } //parseAttribute
        
        protected Object parse(String s, Class type) throws DataParseException {
            DataParser dp = m_pf.getParser(type);
            return dp.parse(s);
        } //parse
        
        protected Class parseType(String type) {
            type = Character.toUpperCase(type.charAt(0)) + type.substring(1).toLowerCase();
            if ( type.equals(INT) || type.equals(INTEGER) ) {
                return int.class;
            } else if ( type.equals(LONG) ) {
                return long.class;
            } else if ( type.equals(FLOAT) ) {
                return float.class;
            } else if ( type.equals(DOUBLE) || type.equals(REAL)) {
                return double.class;
            } else if ( type.equals(BOOLEAN) ) {
                return boolean.class;
            } else if ( type.equals(STRING) ) {
                return String.class;
            } else if ( type.equals(DATE) ) {
                return Date.class;
            } else {
                throw new RuntimeException("Unrecognized data type: "+type);
            } //if-else
        } //parseType
        
        public Tree getTree() {
            return m_tree;
        } //getTree
        
    } //TreeMLHandler (inner class)
	
} //TreeCreator (class)