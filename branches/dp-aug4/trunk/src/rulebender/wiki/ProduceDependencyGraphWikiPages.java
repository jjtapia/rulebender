package rulebender.wiki;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ProduceDependencyGraphWikiPages
{
	public static String outDir = "/Users/adammatthewsmith/Documents/workspace/rulebender-wiki/";
	public static String imageDir = outDir+"imgs/arch/";
	
	
	public static void main(String[] args)
	{
		
		File imgDirFile = new File(imageDir);
		
		for(String fileName : imgDirFile.list())
		{
			if(fileName.contains(".png"))
			{
				 createWikiPageForImageName(fileName);
				System.out.println("  * [PackageDependency" + convertDotSepToCamelCase(fileName.substring(0, fileName.indexOf(".png"))) + " " + fileName.substring(0, fileName.indexOf(".png")) + "]");
			}
		}
	}
	
	private static void createWikiPageForImageName(String fileName)
	{
		// Create the file
		//System.out.println("Analyzing " + fileName);
		
		// Added the "PackageDependency" prefix so I can easily manage all of these files.
		String wikiPage = outDir + "PackageDependency" + convertDotSepToCamelCase(fileName.substring(0, fileName.indexOf(".png"))) + ".wiki";
		
		File wikiFile = new File(wikiPage);
		
		try
		{
			wikiFile.createNewFile();
		}
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Write the content;
		FileWriter fstream = null;
		try {
			fstream = new FileWriter(wikiFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		BufferedWriter out = new BufferedWriter(fstream);
		
		
		try 
		{
			// /*
				out.write("#summary package dependency for " + fileName.substring(0, fileName.indexOf(".png")) + "\n");
				out.write("= Package dependency graph for " + fileName.substring(0, fileName.indexOf(".png")) + "=\n");
				out.write("For class level and interactive graphs, use the CDA tool (http://www.dependency-analyzer.org/) on the !RuleBender class files.\n");
				out.write("http://rulebender.googlecode.com/svn/wiki/imgs/arch/" + fileName + "\n");
			
			// */
			
			//Close the output stream
			out.close();
			
			System.out.println("  * [PackageDependency" + convertDotSepToCamelCase(fileName.substring(0, fileName.indexOf(".png"))) + " " + fileName.substring(0, fileName.indexOf(".png")) + "]");
			
			/*
			System.out.println("Writing to " + wikiPage);
			System.out.println("#summary package dependency for " + fileName.substring(0, fileName.indexOf(".png")));
			System.out.println("= Package dependency graph for " + fileName.substring(0, fileName.indexOf(".png")) + "=");
			System.out.println("http://rulebender.googlecode.com/svn/wiki/imgs/arch/" + fileName);
			System.out.println();
			// */
		}
		
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	private static String convertDotSepToCamelCase(String s)
	{
		String toReturn = "";
		
		for(int i = 0; i < s.length(); i++)
		{
			if(s.charAt(i) == '.')
			{
				i++;
				toReturn += (s.charAt(i)+"").toUpperCase();
			}
			else if(i == 0)
			{
				toReturn += (s.charAt(i)+"").toUpperCase();
			}
			else
			{
				toReturn += s.charAt(i);
			}
		}
		
		return toReturn;
	}
}