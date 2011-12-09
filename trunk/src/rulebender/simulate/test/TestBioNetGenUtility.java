package rulebender.simulate.test;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import rulebender.simulate.BioNetGenUtility;
import rulebender.simulate.parameterscan.ParameterScanData;
import rulebender.simulate.parameterscan.ParameterScanScriptCreator;


public class TestBioNetGenUtility 
{
	
	private static String bngFullPath = "distributionResources/BioNetGen/mac64/BioNetGen-2.1.8/Perl2/BNG2.pl";
	private static String scanVarFullPath ="distributionResources/BioNetGen/mac64/BioNetGen-2.1.8/Perl2/scan_var.pl";		

	@Test
	public void testScriptWriterFileLocation()
	{
		String resultsPath = "~/.rulebender-test/";
		String modifiedPerlScriptName = "testScript.pl";
	
		File testDir = new File(resultsPath);
		
		testDir.mkdirs();
		
		// Create the perl script.
		ParameterScanScriptCreator.produceAndWriteScript(resultsPath, modifiedPerlScriptName, bngFullPath, scanVarFullPath);
		
		File script = new File(resultsPath + modifiedPerlScriptName);
		
		assertTrue(testDir.exists());
		assertTrue(script.exists());
		
		testDir.delete();
	}
}
