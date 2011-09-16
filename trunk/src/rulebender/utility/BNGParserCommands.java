package rulebender.utility;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RuleReturnScope;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.AngleBracketTemplateLexer;

import bngparser.dataType.ChangeableChannelTokenStream;
import bngparser.grammars.BNGGrammar;
import bngparser.grammars.BNGLexer;
import bngparser.grammars.BNGGrammar.prog_return;

public class BNGParserCommands 
{
	public static String EGFR_NET = "testModels/egfr_net.bngl";
	public static String TOY = "testModels/toy-jim.bngl";
	public static String FCERI_JI = "testModels/fceri_ji.bngl";
	public static String SIMPLE = "testModels/Simple.bngl";
	
	public static void test(String fileName)
	{
		prog_return ast = null;
		try {
			ast = getASTForFileName(fileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RecognitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("AST:" + (ast == null ? " null" : ast.toString()));
	}
	
	
	public static prog_return getASTForFileName(String fileName) throws IOException,RecognitionException
	{	
		BNGGrammar parser = getParserForFileName(fileName);
		//TODO Make this xml a part of the jar.
		StringTemplateGroup template = new StringTemplateGroup(new FileReader("/Users/mr_smith22586/Documents/workspace/rulebender/resources/xml.stg"),AngleBracketTemplateLexer.class);
		parser.setTemplateLib(template);
		return parser.prog();
	}
	
	public static void writeXMlTranslationForFileName(String fileName) throws RecognitionException, IOException 
	{
		BNGGrammar parser = getParserForFileName(fileName);
		
		StringTemplateGroup template = new StringTemplateGroup(new FileReader("xml.stg"),AngleBracketTemplateLexer.class);
		parser.setTemplateLib(template);
		RuleReturnScope r = parser.prog();
		
		FileWriter writer = new FileWriter("output.xml");
		writer.write(r.getTemplate().toString());
		writer.close();
	}
	
	private static BNGGrammar getParserForFileName(String fileName) throws IOException
	{
		CharStream input = new ANTLRFileStream(fileName);
		BNGLexer bnglexer = new BNGLexer(input);
		ChangeableChannelTokenStream tokens = new ChangeableChannelTokenStream(bnglexer);
		 
		return new BNGGrammar(tokens);
	}
}
