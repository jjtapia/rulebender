package rulebender.simulationjournaling;

import java.io.*;
import java.net.URL;
import java.util.Hashtable;
import java.awt.FontMetrics;

/**
 * This class controls the abbreviation of strings to fit them to a given 
 * length. It provides methods for various types of strings, specifically
 * names, phone numbers, and e-mail addresses, in addition to simple
 * string truncation.
 *
 * @version 1.0
 * @author David Nation
 * @author <a href="http://jheer.org">Jeffrey Heer</a> prefuse(AT)jheer.org
 */
public class StringAbbreviator {

    private static final String SUFFIX = "suffix";
	private static final String PREFIX = "prefix";
	
	public static final int NAME     = 0;
    public static final int PHONE    = 1;
    public static final int EMAIL    = 2;
    public static final int TRUNCATE = 3;
    public static final int FILE     = 4;

    public static final String STR_NAME     = "name";
    public static final String STR_PHONE    = "phone";
    public static final String STR_EMAIL    = "email";
    public static final String STR_TRUNCATE = "truncate";
    public static final String STR_FILE     = "file";

    protected URL datadir;
    protected String datafile;
    protected Hashtable<String, String> abbrevT;

    protected static Hashtable<String, String> prefixSuffixT = new Hashtable<String, String>();
    
    static {
        prefixSuffixT.put( "mr",    PREFIX );
        prefixSuffixT.put( "mr.",   PREFIX );
        prefixSuffixT.put( "dr",    PREFIX );
        prefixSuffixT.put( "dr.",   PREFIX );
        prefixSuffixT.put( "lt",    PREFIX );
        prefixSuffixT.put( "lt.",   PREFIX );
        prefixSuffixT.put( "gen",   PREFIX );
        prefixSuffixT.put( "gen.",  PREFIX );
        prefixSuffixT.put( "sgt",   PREFIX );
        prefixSuffixT.put( "sgt.",  PREFIX );
        prefixSuffixT.put( "cmdr",  PREFIX );
        prefixSuffixT.put( "cmdr.", PREFIX );
        prefixSuffixT.put( "cpt",   PREFIX );
        prefixSuffixT.put( "cpt.",  PREFIX );
        prefixSuffixT.put( "ii",    SUFFIX );
        prefixSuffixT.put( "iii",   SUFFIX );
        prefixSuffixT.put( "iv",    SUFFIX );
        prefixSuffixT.put( "jr",    SUFFIX );
        prefixSuffixT.put( "jr.",   SUFFIX );
        prefixSuffixT.put( "sr",    SUFFIX );
        prefixSuffixT.put( "sr.",   SUFFIX );
    } 
    
    private static StringAbbreviator s_abbrev = new StringAbbreviator(null, null);

    /** 
     * Construct an abbreviator.
     * @param docBase Document base for applet or application.
     * @param abbrfile File name which contains abbreviation pairs.
     */
    public StringAbbreviator(URL docBase, String abbrfile) {
		datadir = docBase;
		datafile = abbrfile;
    } //StringAbbreviator (constructor)
    
    public static StringAbbreviator getInstance() {
    	return s_abbrev;
    } //StringAbbreviator (constructor)

    public String abbreviate(String str, int type, FontMetrics fm, int width) {
		switch ( type ) {
			case NAME:
				if (fm.stringWidth(str) > width) {
					str = abbreviateName(str, false);
				} //if
				
				if (fm.stringWidth(str) > width) {
					str = abbreviateName(str, true);
				} //if
				
				break;
		
			case PHONE:
				if (fm.stringWidth(str) > width) {
					str = abbreviatePhone(str, 8);
				} //if
				
				if (fm.stringWidth(str) > width) {
					str = abbreviatePhone(str, 4);
				} //if
				
				break;
		
			case EMAIL:
				if (fm.stringWidth(str) > width) {
					str = abbreviateEmail(str);
				} //if
				
				break;
		
			case FILE:
				if (fm.stringWidth(str) > width) {
					str = abbreviate(str, false);
				} //if
				
				if (fm.stringWidth(str) > width) {
					str = abbreviate(str, true);
				} //if
				
				break;
		
			case TRUNCATE:
			default:
				int lastblank = 0, nchars = 0, cumx = 0;
				
				while ( cumx < width &&  nchars < str.length() ) {
					if ( Character.isWhitespace(str.charAt(nchars)) ) {
						lastblank = nchars;
					} //if
			
					cumx += fm.charWidth(str.charAt(nchars));
					nchars++;
				} //while
		    
		    if ( nchars < str.length() && lastblank > 0 ) { 
		    	nchars = lastblank; 
		    } //if
		    
		    str = ( nchars > 0 ? str.substring(0, nchars) : str );
		} //switch
		
		return str;
    } //abbreviate
    
    public String abbreviateName(String str, FontMetrics fm, int width) {
    	return abbreviate(str, NAME, fm, width);
    } //abbreviateName
    
	public String abbreviateEmail(String str, FontMetrics fm, int width) {
		return abbreviate(str, EMAIL, fm, width);
	} //abbreviateEmail

    protected String abbreviate(String inString, boolean initial) {
		// If abbreviation file has not been read in yet, read it in.
		if (abbrevT == null) {
		    readAbbrFile();
		} //if
	
		String allowableLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		StringReader in = new StringReader(inString);
		StreamTokenizer p = new StreamTokenizer(in);
		p.wordChars('&', '&');
		p.wordChars('@', '@');
		p.ordinaryChar(',');
		p.ordinaryChar('.');
		p.ordinaryChar('-');
		p.ordinaryChar(':');
		
		int c;
		String newVal = null;
		StringBuffer outString = new StringBuffer();
		boolean firstTime = true;
		
		try {
			out:
				while (true) {
					c = p.nextToken();
					switch (c) {
					case StreamTokenizer.TT_EOF:
						break out;
						
					case StreamTokenizer.TT_EOL:
						System.err.println("warning: unexpected EOL token"); break;
						
					case StreamTokenizer.TT_NUMBER:
						if (firstTime) {
							firstTime = false;
						} else if (!initial) {
							outString.append(" ");
						} //if-else
						
						outString.append(new Integer((int)(p.nval)));
						break;
						
					case StreamTokenizer.TT_WORD:
						if (firstTime) {
							firstTime = false;
						} else if (!initial) {
							outString.append(" ");
						} //if-else
						
						if (initial) {
							String tmps = p.sval.substring(0,1);			
							if ( allowableLetters.indexOf(tmps) >= 0 ) {
								outString.append(tmps);
							} //if
						} else {
							if ((newVal = (String) abbrevT.get(p.sval.toLowerCase())) != null) {
								outString.append(newVal);
							} else {
								outString.append(p.sval);
							} //if-else
						} //if-else
						break;
						
					case ',':
						outString.append((char) c);
						break;
						
					case ':':
						outString.append((char) c);
						break;
						
					case '.':
						if (!initial) {
							outString.append((char) c);
						} //if
						
						break;
						
					case '-':
						if (!initial) {
							outString.append((char) c);
						} //if
						break;
						
					default:
						if (!initial) {
							outString.append((char) c);
						} //if
						break;
						
				} //switch
					
			} //while
		
		} catch (IOException e) {
		    e.printStackTrace();
		} //try-catch
		
		return outString.toString();
    } //abbreviate

    protected String abbreviatePhone(String inString, int maxChars) {
		if ((inString != null) && (inString.length() > maxChars)) {
		    return inString.substring(inString.length() - maxChars);
		} else {
			return inString;
		} //if-else
    } //abbrevatePhone

    protected String abbreviateEmail(String inString) {
		if ((inString != null) && (inString.indexOf('@') > 0)) {
		    return inString.substring(0, inString.indexOf('@'));
		} else {
		    return inString;
		} //if-else
    } //abbrevaiteEmail

    protected String abbreviateName(String inString, boolean lastOnly) {
		StringReader in = new StringReader(inString);
		StreamTokenizer p = new StreamTokenizer(in);
		p.wordChars('&', '&');
		p.wordChars('@', '@');
		p.wordChars(':', ':');
		p.ordinaryChar(',');
		p.ordinaryChar('-');
		
		int c;
		String fieldName = null;
		String lastNameHold = null;
		String lastInitialHold = null;
		StringBuffer outString = new StringBuffer();
		try {
			
			out:
				while (true) {
					c = p.nextToken();
					
					switch (c) {
						case StreamTokenizer.TT_EOF:
							break out;
						
						case StreamTokenizer.TT_EOL:
							System.err.println("warning: unexpected EOL token"); break;
						
						case StreamTokenizer.TT_NUMBER:
							break;
						
						case ',':
							break out;
						
						case StreamTokenizer.TT_WORD:
							if (p.sval.endsWith(":")) {
								outString.append(p.sval + " ");
							
							} else if (prefixSuffixT.get(p.sval.toLowerCase()) == null) {
								if (!lastOnly) {
									if (lastInitialHold != null) {
										outString.append(lastInitialHold);
									} //if
									
									lastInitialHold = p.sval.substring(0,1)+". ";
								} //if
								
								lastNameHold = p.sval;
							} //if-else
							
							break;
							
						default:
							break;
					} //switch
		    } //while
		
		    outString.append(lastNameHold);
		    
		} catch (IOException e) {
		    e.printStackTrace();
		} //try-catch
		
		return outString.toString();
    } //abbreviateName

    /**
     * Reads in the abbreviation file.
     */
    public void readAbbrFile() {
		abbrevT = new Hashtable();
		try {  // open directory data file
		    URL theURL = new URL(datadir, datafile);
		    BufferedReader d = new BufferedReader(new InputStreamReader(theURL.openStream()));
		    parseAbbrFile(d, abbrevT);
		
		} catch (Exception e) {
		    // No abbrev file found, but that's OK, we'll just go on without it -- jheer
		} //try-catch
    } //readAbbrFile

    /**
     * Parses an abbreviation mapping coming in from an input stream.
     * @param in the BufferedReader from which to read the mappings.
     * @param h the Hashtable in which to store the mappings.
     */
    public static void parseAbbrFile(BufferedReader in, Hashtable h) {
		StreamTokenizer p = new StreamTokenizer(in);
		//key values are separated by white spaces or '='
		p.whitespaceChars('=', '=');
		p.wordChars('&', '&');	// allow & as an abbreviation
		p.wordChars('/', '/');
		p.slashStarComments(true);
		p.slashSlashComments(true);
		p.commentChar('#');
		
		int c;
		String key = null;
		boolean errflag = false;
		boolean expecting_value = false;
		try {
			out:
				while (true) {
					c = p.nextToken();
					switch (c) {
						case StreamTokenizer.TT_EOF:
							break out;
						
						case StreamTokenizer.TT_EOL:
							System.err.println("warning: unexpected EOL token"); break;
						
						case StreamTokenizer.TT_NUMBER:
							if (expecting_value) {
								h.put(key, new Double(p.nval));
								expecting_value = false;
							} else {
								errflag = true;
								break out;
							} //if-else
							break;
						
						case StreamTokenizer.TT_WORD:
							if (expecting_value) {
								h.put(key, p.sval);
								expecting_value = false;
							} else {
								expecting_value = true;
								key = p.sval.toLowerCase();
							} //if-else
							break;
						
						default:
							errflag = true;
							break out;
					} //switch
				} //while
		    
			if (errflag) {
				System.out.println("Error encountered around '"+key+"'");
			} //if
			
		} catch (IOException e) {
		    e.printStackTrace();
		} //try-catch
    } //parseAbbrFile

    /** 
     * Set the abbreviation file name.
     * @param fileName Abbreviation file name.
     */
    public void setAbbrFile(String fileName) {
		datafile = fileName;
		readAbbrFile();
    } //setAbbrFile

} //StringAbbreviator