package rulebender.prefuse.collinsbubbleset.lib;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import prefuse.util.ColorLib;

public class Colors {

	private HashMap<String,Integer> colors = new HashMap<String,Integer>();
    private HashMap<String,String[]> palettes = new HashMap<String,String[]>();
       
    private static Colors c = new Colors();
   
    private void readColours(String filename) {
    	InputStream fileInputStream;
		try {
			System.err.println("reading colours from " + filename);
	    	fileInputStream = new FileInputStream(new File(filename));
			readColours(fileInputStream);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private void readColours(InputStream is) {
    	try {
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line = br.readLine();
			while (line != null) {
				// skip comments
				if (line.startsWith("//")) {
					line = br.readLine();
					continue;
				}
				// parse line
				String[] parts = line.split(",");
				String name = parts[0];
				String type = parts[1];
				
				if (type.equals("rgbai")) {
					colors.put(name,ColorLib.rgba(Integer.parseInt(parts[2]),Integer.parseInt(parts[3]),Integer.parseInt(parts[4]),Integer.parseInt(parts[5])));
				}
				if (type.equals("rgbaf")) {
					colors.put(name,ColorLib.rgba(Float.parseFloat(parts[2]),Float.parseFloat(parts[3]),Float.parseFloat(parts[4]),Float.parseFloat(parts[5])));
				} 
				if (type.equals("i")) {
					colors.put(name,Integer.parseInt(parts[2]));
				}
				if (type.equals("p")) {
					ArrayList<String> paletteColours = new ArrayList<String>();
					for (int i = 2; i < parts.length; i++) {
						paletteColours.add(parts[i]);
					}
					palettes.put(name, paletteColours.toArray(new String[paletteColours.size()]));
				}
				line = br.readLine();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
 
    public Colors(String filename) {
    	super();
    	readColours(filename);
    }
    
    public Colors() {
    	super();
    	// load from local colours repository
        readColours(getClass().getResourceAsStream("colours.csv"));
    }
    
    public Set getColorNames() {
    	return colors.keySet();
    }
    
    public int getColor(String name) {
    	return colors.get(name);
    }
    
    public static Colors getInstance() {
    	return c;
    }
    
    public int[] getPalette(String name) {
    	String[] colorNames = palettes.get(name);
    	int[] colors = new int[colorNames.length];
    	for (int i = 0; i < colorNames.length; i++) {
    		colors[i] = getColor(colorNames[i]);
    	}
    	return colors;
    }
    
    public void addColor(String name, int color) {
    	colors.put(name, color);
    }
     
}
