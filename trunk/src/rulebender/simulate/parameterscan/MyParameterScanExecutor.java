package rulebender.simulate.parameterscan;

public class MyParameterScanExecutor 
{
	
	private String m_name;
	private String m_filePath;
	private String m_bngPath;
	private ParameterScanData m_data;
	private String m_resultsPath;
	
	private String m_prefix;

	private float m_delta;
	
	public MyParameterScanExecutor(String name, String filePath,
								   String bngPath, String scriptFullPath, 
								   ParameterScanData data, String resultsPath)
	{
		m_name = name;
		m_filePath = filePath;
		m_bngPath = bngPath;
		m_data = data;
		m_resultsPath = resultsPath;
		
		// set up the prefix.
		m_prefix=filePath.substring(filePath.lastIndexOf(System.getProperty("file.separator")), filePath.indexOf(".bngl"));
		m_prefix +="_" + data.getName();
		
		if (data.isLogScale())
		{
			  data.setMinValue((float) Math.log(data.getMinValue()));
			  data.setMaxValue((float) Math.log(data.getMaxValue()));
		}
		
		m_delta= (data.getMaxValue()-data.getMinValue())/(data.getPointsToScan());
	}

	public void run()
	{
		// Read in the file
		
	}
	/*
	// Read file 
	open(IN,$file) || die "Couldn't open file $file: $?\n";
	my $script="";
	while(<IN>){
	  $script.=$_;
	  # Skip actions
	  last if (/^\s*end\s*model\s*$/);
	}

	if (-d $prefix){
	  system("rm -r $prefix");
	#  die "Directory $prefix exists.  Remove before running this script.";
	}

	mkdir $prefix;
	chdir $prefix;

	# Create input file scanning variable
	$fname= sprintf "${prefix}.bngl", $run;
	open(BNGL,">$fname") || die "Couldn't write to $fname";
	print BNGL $script;
	print BNGL "generate_network({overwrite=>1});\n";
	my $val= $var_min;
	for my $run (1..$n_pts){
	  my $srun= sprintf "%05d", $run;
	  if ($run>1){
	    print BNGL "resetConcentrations()\n";
	  }
	  my $x= $val;
	  if ($log){ $x= exp($val);}
	  printf BNGL "setParameter($var,$x);\n";
	  
	  my $opt= "suffix=>\"$srun\",t_end=>$t_end,n_steps=>$n_steps";
	  if ($steady_state){
	    $opt.=",steady_state=>1";
	  }
	  printf BNGL "simulate_ode({$opt});\n";
	  $val+=$delta;
	}  
	close(BNGL);

	#my $outdir = "/Users/mr_smith22586/Documents/workspace/BNGModelsTest/egfr_net/egfr_net/results/parascan-13-Dec-11_11-21-52/egf_0_000";
	my $outdir = ".";

	# Run BioNetGen on file
	print "Running BioNetGen on $fname\n";

	my $exec= "${BNGPATH}/BNG2.pl";

	print "Executing: $exec -outdir $outdir $fname \n";

	print "Prefix: ${prefix}";

	system("$exec -outdir $outdir $fname > $prefix.log");

	# Process output
	$ofile="../$prefix.scan";
	open(OUT,">$ofile") || die "Couldn't open $ofile";
	my $val= $var_min;
	for my $run (1..$n_pts)
	{
	  # Get data from gdat file
	  $file= sprintf "${prefix}_%05d.gdat", $run;
	  print "Extracting data from $file\n";
	  open(IN,"$file") || die "Couldn't open $file";
	  if ($run==1)
	  {
	     my $head= <IN>;
	     $head=~ s/^\s*\#//;
	     my @heads= split(' ',$head);
	     shift(@heads);
	     printf OUT "# %+14s", $var;
	     for my $head (@heads){
	     printf OUT " %+14s", $head;
	  }
	     
	  print OUT "\n";

	}
	 
	 while(<IN>){$last=$_};
	  my @dat= split(' ',$last);
	  my $time= shift(@dat);
	  my $x= ($log)? exp($val) : $val;
	  printf OUT "%16.8e %s\n", $x, join(' ',@dat);
	  close(IN);
	  $val+=$delta;
	}  
	close(OUT);
*/
}
