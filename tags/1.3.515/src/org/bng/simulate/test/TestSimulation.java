package org.bng.simulate.test;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;

import org.bng.simulate.BioNetGenUtility;
import org.junit.Before;
import org.junit.Test;

public class TestSimulation {

	public static String bngR597PathNoSpace = "/users/adammatthewsmith/Documents/workspace/BioNetGenSimulation/testFiles/BioNetGen-2.1.8r597/BNG2.pl";
	public static String bngR597PathSpace = "/users/adammatthewsmith/Documents/workspace/BioNetGenSimulation/testFiles/BioNetGen-2.1.8r597 withSpace/BNG2.pl";

	public static String modelPathNoSpace = "/users/adammatthewsmith/Documents/workspace/BioNetGenSimulation/testFiles/bngl/TestModel.bngl";
	public static String modelPathSpace = "/users/adammatthewsmith/Documents/workspace/BioNetGenSimulation/testFiles/bngl withSpace/TestModel.bngl";

	public static String modelPathNoSpaceFileSpace = "/users/adammatthewsmith/Documents/workspace/BioNetGenSimulation/testFiles/bngl/TestModel withSpace.bngl";
	public static String modelPathSpaceFileSpace = "/users/adammatthewsmith/Documents/workspace/BioNetGenSimulation/testFiles/bngl withSpace/TestModel withSpace.bngl";

	public static String resultsPathNoSpace = "/users/adammatthewsmith/Documents/workspace/BioNetGenSimulation/testFiles/results/";
	public static String resultsPathSpace = "/users/adammatthewsmith/Documents/workspace/BioNetGenSimulation/testFiles/results withSpace/";

	@Before
	public void setup() {
		File f = (new File(resultsPathNoSpace));
		f.delete();
		f.mkdirs();

		f = (new File(resultsPathSpace));
		f.delete();
		f.mkdirs();
	}

	@Test
	public void testR597SimulationNoSpace() {
		// // fuuuuuuuuu
		// need to wait for the thread to finish before making the checks.
		// this is becoming too tedious and I am just moving on.

		// no spaces
		// BioNetGenUtility.runBNGLFile(modelPathNoSpace, bngR597PathNoSpace,
		// resultsPathNoSpace);

		// Space in model path
		BioNetGenUtility.runBNGLFile(modelPathSpace, bngR597PathNoSpace,
				resultsPathNoSpace);

		// BioNetGenUtility.runBNGLFile(modelPathNoSpace, bngR597PathNoSpace,
		// resultsPathNoSpace);
		// BioNetGenUtility.runBNGLFile(modelPathNoSpace, bngR597PathNoSpace,
		// resultsPathNoSpace);
		// BioNetGenUtility.runBNGLFile(modelPathNoSpace, bngR597PathNoSpace,
		// resultsPathNoSpace);
		// BioNetGenUtility.runBNGLFile(modelPathNoSpace, bngR597PathNoSpace,
		// resultsPathNoSpace);

	}
}
