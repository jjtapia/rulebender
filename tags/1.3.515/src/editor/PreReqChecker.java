package editor;

import java.io.IOException;

public class PreReqChecker 
{
	
	public static void checkForPerl()
	{
		String instruction = "perl --version";
		
		// run the process
		try 
		{
			Runtime.getRuntime().exec(instruction);
		}
		catch (IOException e) 
		{
			BNGEditor.displayOutput("\n***Warning: It appears that Perl is not installed on your system.\nPlease install Perl if you want to run simulations.\n");
			//e.printStackTrace();
		}	
	}
}

