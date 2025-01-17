package BNGModel;

# pragmas
#use strict;
#use warnings;



# BNGOutput contains BNGModel methods related to third-party output
#  e.g. writeXML, writeSBML, writeMfile, writeMexfile, toSSC...
#  Note that .NET and .BNGL writer methods are contained in THIS file.
use BNGOutput;

# BNGAction contains BNGModel action methods
#  e.g. simulate, simulate_pla, simulate_nf, parameter_scan, generate_hybrid_model...
#  Note that some core actions are contained here: generate_network, setParameter, etc.
use BNGAction;

# Perl Modules
use Class::Struct;
use FindBin;
use lib $FindBin::Bin;
use File::Spec;

# BNG Modules
use BNGUtils;
use MoleculeTypesList;
use ParamList;
use Function;
use Compartment;
use CompartmentList;
use SpeciesList;
use RxnRule;
use EnergyPattern;
use Observable;
use PopulationList;




# Global package variables
my $NO_EXEC = 0;  # Prevents execution of functions to allow file syntax checking
my $HAVE_PS = 0;  # Set to 0 for MS Windows systems with no ps command - disables
                  #  reporting of memory usage at each iteration of network generation




# Structure containing BioNetGen model data
struct BNGModel =>
{
	Name                => '$',
	Time                => '$',
	Concentrations      => '@',
	MoleculeTypesList   => 'MoleculeTypesList',
	SpeciesList         => 'SpeciesList',
	SeedSpeciesList     => 'SpeciesList',
	RxnList             => 'RxnList',
	RxnRules            => '@',
	ParamList           => 'ParamList',
	Observables         => '@',
	EnergyPatterns      => '@',  # energyBNG: Holds a list of energy patterns  --Justin
	CompartmentList     => 'CompartmentList',    # list of reaction compartments (volumes and surfaces)
	PopulationTypesList => 'MoleculeTypesList',  # list of population molecule types
	PopulationList      => 'PopulationList',     # list of population species
	SubstanceUnits      => '$',
	UpdateNet           => '$',  # This variable is set to force update of NET file before simulation.
	Version             => '@',  # Indicates set of version requirements- output to BNGL and NET files
	Options             => '%',  # Options used to control behavior of model and associated methods
    Params              => '%',  # run-time parameters (not to be saved)
};



###
###
###



# Initialize BNGModel data structures
sub initialize
{
    my $model = shift @_;

    # Initialize ParameterList
	my $plist = ParamList->new;
	$model->ParamList($plist);

	# Initialize MoleculeTypesList
	my $mtlist = MoleculeTypesList->new( StrictTyping=>0 );
    $model->MoleculeTypesList($mtlist);

    # Create the null type "0"
    #my $nulltype = MoleculeType->new( Name=>'0', PopulationType=>0 );
    #$mtlist->add($nulltype);
			
	# Initialize PopulationTypesList
	$model->PopulationTypesList( MoleculeTypesList->new( StrictTyping=>0 ) );
			
	# Initialize PopulationList
	$model->PopulationList( PopulationList->new() );

	# Initialize CompartmentList
	my $clist = CompartmentList->new();
	$model->CompartmentList($clist);

	# Initialize SpeciesList
	my $slist = SpeciesList->new();
	$model->SpeciesList($slist);

    # Create NullSpecies
    #my $null_string = '$0()';
    #my $null_species = SpeciesGraph->new();
    #$null_species->readString( \$null_string, $clist, 1, '', $mtlist);
    #$slist->add( $null_species, 1.0 );

    # Initialize RxnList
    my $rlist = RxnList->new();
    $model->RxnList($rlist);

	# Initialize SubstanceUnits (Number is default)
	$model->SubstanceUnits("Number");
}



###
###
###



# Read bionetgen data in blocks enclosed by begin param end param
# lines.  Prevents overwriting of variables possible with eval.
#
# To do:
# 1.  Receive a valid list of parameter names to be read
# 2.  Check syntax of lines- this is currently done when parameter is
#     handled.  Some basic checks could be done here.
#
# Lines between begin and end commands are put into arrays with the name given
#  by the block name
{

	my ($filename, $line_number, $file_dat);
	my (@filenames, @line_numbers, @file_dats);
	my $level = -1;
	my $MAX_LEVEL = 5;    # Sets maximum level of allowed recursion
	my %bngdata;
    my $t_start;
    my $stdout_handle;


	sub readFile
	{
        # get arguments
		my $model  = shift @_;
		my $params = @_ ? shift @_ : {};

        # a place for error messages
        my $err;

        # increment level
        ++$level;
        # save previous work!
        if ( $level > 0 )
        {   # Do this now, so we can exit and recover the previous level in a uniform manner
            push @filenames, $filename;
            push @file_dats, $file_dat;
            push @line_numbers, $line_number;
        }

	    if ( $level > $MAX_LEVEL )
        {   # done
		    $err = errgen( "Recursion level exceeds maximum of $MAX_LEVEL" );
		    goto EXIT;
		}
 
        # get the filename
        my $filename = defined $params->{file} ? $params->{file} : undef;
		unless ( defined $filename )
        {   # Filename argument is mandatory
            $err = errgen( "File parameter is required for method BNGModel::readFile" );
            goto EXIT;
        }


        # Top level stuff
        if ($level==0)
        {   
            # get start time
            $t_start = cpu_time(0);

            # Initialize_model
      		$model->initialize();

            # set model update flag
            $model->UpdateNet(1);

            # set model name
            if ( $params->{basename} )
            {   # set model name to basename
                $model->Name( $params->{basename} );
            }
            else
            {   # determine model basename from filename
                my ($vol, $dir, $fn) = File::Spec->splitpath( $filename );  
                    
                my $basename;
                # file = basename.ext
                if ( $fn =~ /^(.+)\.([^\.]+)$/ )
                {   $basename = $1;   }
                # file = basename
                elsif ( $fn =~ /^([^\.]+)$/ )
                {   $basename = $1;   }
                # file = ???
                else
                {   $basename = $fn;  }

                $model->Name( $basename );  
            }
 
            # set model parameters
            $model->Params( $params );

            # set output directory
            unless ( defined $model->Params->{output_dir} )
            {   # default is current directory
                $model->setOutputDir();
            } 

            # set up log file, if needed
            if ( $model->Params->{logging} )
            {  
                # generate logfile name
                my $logfilename = $model->getOutputPrefix() . ".log" ;
                # remember where to find STDOUT
                unless( open $stdout_handle, ">&STDOUT" )
                {  $err = "Problem finding handle for STDOUT: $!";  goto EXIT;  }       
                # redirect STDOUT to logfile
                unless ( open STDOUT, '>', $logfilename )
                {  $err = "Problem redirecting STDOUT to logfile: $!";;  goto EXIT;  }
            }

            # turn off output buffering on STDOUT
            select STDOUT;  $| = 1;

            # Say Hello to the user
            printf "BioNetGen version %s\n", BNGversion();   
        }




        # Read BNG model data		
        print "Reading from file $filename (level $level)\n";
		unless( open FILE, '<', $filename )
        {   
            $err = "Couldn't read from file $filename: $!";
            goto EXIT;
        }
        # read all lines of the file into an array at $file_dat
        $file_dat = [<FILE>];
        # close file
        close FILE;


		# Read data from file into data hash
		$line_number = 0;
		my $begin_model = 0;
		my $in_model    = 1;
		while ( my $string = get_line() )
        {
            # chop leading spaces
            $string =~ s/^\s+//;

			if ( $string =~ /^begin\s+model\s*$/ )
            {
		    	++$begin_model;
				if ( $begin_model > 1 )
                {
					$err = errgen("Only one model definition allowed per file");
					goto EXIT;
				}
				$in_model = 1;
				next;
			}
			elsif ( $string =~ /^end\s+model\s*$/ )
            {
				unless ($in_model)
                {
				    $err = errgen("end model encountered without enclosing begin model");
					goto EXIT;
				}
				$in_model = 0;
				next;
			}

			# Process multi-line block
			if ( $string =~ s/^begin\s*// )
            {
                # get block name
				my $name = $string;
				# Remove trailing white space
				$name =~ s/\s*$//;
				# Remove repeated white space
				$name =~ s/\s+/ /g;

				unless ($in_model or ($name eq 'actions'))
                {
                    $err = errgen("$name cannot be defined outside of a model");
				    goto EXIT;
				}

				# Read block data
				my $block_dat;
				( $block_dat, $err ) = read_block_array($name);
				if ($err) {  goto EXIT;  }
				$bngdata{$name} = 1;




				### Read Parameters Block
				if ( $name eq 'parameters' )
                {
					# Read model parameters
                    my $plast = $model->ParamList->getNumParams();
                    my ($entry, $lno);
					foreach my $line ( @$block_dat )
                    {
						($entry, $lno) = @$line;
						$err = $model->ParamList->readString($entry);
						if ($err) {  $err = errgen( $err, $lno );  goto EXIT;  }
					}
                    # check parameter list
					if ( $err = $model->ParamList->check() )
                    {
						$err = errgen( $err, $lno );
						goto EXIT;
					}
                    # sort parameters
					if ( $err = $model->ParamList->sort() )    
                    {
						$err = errgen( $err, $lno );
						goto EXIT;
					}
                    # update user
					printf "Read %d $name.\n", $model->ParamList->getNumParams() - $plast;
				}

					
				### Read Functions Block
				elsif ( $name eq 'functions' )
				{
					# Model functions
					my $nread = 0;
                    my ($entry, $lno);
					foreach my $line ( @$block_dat )
                    {
						($entry, $lno) = @{$line};
						my $fun = Function->new();
						$err = $fun->readString( $entry, $model );
						if ($err) {  $err = errgen( $err, $lno );  goto EXIT;  }
						++$nread;
					}
					
				    # check paramlist for unresolved dependency, etc
				    #   GIVE warning here, don't terminate!
					if ( $err = $model->ParamList->check() )
					{
						$err = errgen( $err, $lno );
						print "Warning: $err\n"
						     ."  (if parameter is defined in a subsequent block,\n"
						     ."  then this warning can be safely ignored.)\n";
					}							
					# update user
					printf "Read %d ${name}.\n", $nread;
				}
				
	
				### Read Molecule Types block
				elsif ( $name =~ /^molecule[ _]types$/ )
				{
					# read MoleculeTypes
					$model->MoleculeTypesList->StrictTyping(1);
					foreach my $line ( @$block_dat )
					{
						my ( $entry, $lno ) = @$line;
						$err = $model->MoleculeTypesList->readString($entry);
						if ($err) {  $err = errgen( $err, $lno );  goto EXIT;  }
					}
                    # update user
					printf "Read %d molecule types.\n", $model->MoleculeTypesList->getNumMolTypes();
				}


				### Read Population Types block
				elsif ( $name eq 'population types' )
				{
					# read PopulationTypes
                    $model->PopulationTypesList->StrictTyping(1);
				    foreach my $line ( @$block_dat )
					{
						my ( $entry, $lno ) = @$line;
						$err = $model->PopulationTypesList->readString($entry);
						if ($err) {  $err = errgen( $err, $lno );  goto EXIT;  }
					}
                    # update user
					printf "Read %d population types.\n", $model->PopulationTypesList->getNumMolTypes();
				}


				### Read Population Maps block
				elsif ( $name eq 'population maps' )
				{
                    unless ( $model->MoleculeTypesList->StrictTyping )
                    {
						$err = errgen("A $name block cannot be defined unless molecule types are defined explicitly");
						goto EXIT;        
                    }
					# read Population Maps
					foreach my $line ( @$block_dat )
					{
						my ($entry, $lno) = @$line;
						$err = $model->PopulationList->readString($entry,$model);
						if ($err) {  $err = errgen( $err, $lno );  goto EXIT;  }
					}
                    # update user
					printf "Read %d population maps.\n", $model->PopulationList->getNumPopulations;
				}
				
					
				### Read Compartments Block
				elsif ( $name eq 'compartments' )
				{
                    # Read Compartments
                    my ($entry, $lno);
					foreach my $line ( @$block_dat )
                    {
						($entry, $lno) = @$line;
  						$model->CompartmentList->readString( $entry, $model->ParamList );
						if ($err) {  $err = errgen( $err, $lno );  goto EXIT;  }
					}
                    # validate compartments
					if ( $err = $model->CompartmentList->validate() )
                    {
						$err = errgen( $err, $lno );
						goto EXIT;
					}
                    # update user
					printf "Read %d compartments.\n", $model->CompartmentList->getNumCompartments;
				}
				
					
				### Read Species/Seed Species Block
				elsif ( ($name eq 'species') or ($name =~ /^seed[ _]species$/) )
				{
                    # read Species
					foreach my $line ( @$block_dat )
					{
						my ($entry, $lno) = @$line;
						$err = $model->SpeciesList->readString( $entry, $model->ParamList,
								                                $model->CompartmentList,
								                                $model->MoleculeTypesList );
						if ($err) {  $err = errgen( $err, $lno );  goto EXIT;  }							
					}
                    # update user
					printf "Read %d species.\n", $model->SpeciesList->getNumSpecies();
				}
				

				### Read Reaction Rules Block
				elsif ( $name =~ /reaction[ _]rules/ )
				{
					# Read reaction rules
					my $nerr = 0;
					my $rrules = [];
                    $model->RxnRules( $rrules );
					foreach my $line ( @$block_dat )
                    {
						my ($entry, $lno) = @$line;
						(my $rrs, $err) = RxnRule::newRxnRule( $entry, $model );
						if ($err)
                        {   # some error encountered
							$err = errgen( $err, $lno );
							printf "ERROR: $err\n";
							++$nerr;
						}
						else
                        {   # rule is ok
							push @$rrules, $rrs;
                            # give names, if not defined
							unless ( $rrs->[0]->Name )
                            {   $rrs->[0]->Name( 'Rule' . scalar @$rrules );   }
							if ( @$rrs > 1 )
                            {
								unless ($rrs->[1]->Name)
                                {   $rrs->[1]->Name( 'Rule' . scalar @$rrules . 'r' );   }
							}
						}
					}
					if ($nerr)
                    {
						$err = "Reaction rule list could not be read because of errors";
						goto EXIT;
					}
                    # update user
					printf "Read %d reaction rule(s).\n", scalar @{$model->RxnRules};
				}
				

				### Read Reactions Block
				elsif ( $name eq 'reactions' )
				{
					# Reactions (when reading NET file)
					my $rlist = RxnList->new;
					foreach my $line ( @{$block_dat} )
                    {
						my ( $entry, $lno ) = @{$line};
						$err = $rlist->readString( $entry,
						                           $model->SpeciesList,
                                                   $model->ParamList    );
						if ($err) {  $err = errgen( $err, $lno );  goto EXIT;  }
					}
					printf "Read %d reaction(s).\n", scalar( @{$block_dat} );
					$model->RxnList($rlist);
				}


				### Read Groups Block
				elsif ( $name eq 'groups' )
                {
					# Groups (when reading NET file) must come after Observables
					unless ( @{$model->Observables} )
                    {
						$err = errgen("Observables must be defined before Groups", $line_number );
						goto EXIT;
					}

                    # Read Groups
					my $iobs   = 0;
					foreach my $line ( @$block_dat )
                    {
						my ( $entry, $lno ) = @$line;
                
                        # split into tokens (note: using ' ' is different than / /, see perlfunc)
						my @tokens = split ' ', $entry;

						# Skip first entry if it's an index
						if ( $tokens[0] =~ /^\d+$/ ) {  shift @tokens;  }

						if ( $iobs >= @{$model->Observables} )
                        {   # more groups than observables!
							$err = errgen( "More groups than observables", $lno );
							goto EXIT;
						}

                        # get observable
						my $obs = $model->Observables->[$iobs];

						# Check that Observable and Group names match
                        my $group_name = @tokens ? shift @tokens : '';
						unless ( $group_name eq $obs->Name )
                        {
							$err = errgen("Group named $tokens[0] is not compatible with any observable", $lno );
							goto EXIT;
						}						

                        # get group weights
						my @group_weights = split /,/, $tokens[0];

						# Zero the weights (TODO..)
						@{$obs->Weights} = (0) x @{$obs->Weights};
						my ($weight, $species_idx);
						foreach my $component (@group_weights)
                        {
							if ( $component =~ m/^(\d+)\*(\d+)$/ )
                            {
								$weight = $1;
                                $species_idx = $2;
							}
							elsif ( $component =~ m/^(\d+)$/ )
                            {
								$weight = 1;
                                $species_idx = $1;
							}
							else
                            {
								$err = errgen( "Invalid group entry: $component", $lno );
								goto EXIT;
							}
							$obs->Weights->[$species_idx] += $weight;
						}
						++$iobs;
					}
                    # update user
					printf "Read %d group(s).\n", $iobs;
				}

				
				### Read Observables Block
				elsif ( $name eq 'observables' )
				{
                    # Read observables
                    my ($entry, $lno );
					foreach my $line ( @$block_dat )
					{
						($entry, $lno ) = @$line;
						my $obs = Observable->new();
						$err = $obs->readString($entry, $model);
                        if ($err)
						{
							$err = errgen( $err, $lno );
							goto EXIT;
						}
						push @{$model->Observables}, $obs;
					}	

				    # check paramlist for unresolved dependency, etc
				    #   GIVE warning here, don't terminate!				    
					if ( $err = $model->ParamList->check() )
					{
						$err = errgen( $err, $lno );
						print "Warning: $err\n"
						     ."  (if parameter is defined in a subsequent block,\n"
						     ."  then this warning can be safely ignored.)\n";
					}					
				    # update user			
					printf "Read %d observable(s).\n", scalar @{$model->Observables};
				}

				
				### Read Energy Patterns Block
				elsif ( $name eq 'energy patterns' )
				{
                    # check if this is an energyBNG model!
                    unless ( $model->Options->{energyBNG}  )
                    {
                        $err = errgen("$name cannot be defined unless the energyBNG option is true");
                        goto EXIT;
                    }
                    # read energy patterns
					foreach my $line ( @$block_dat )
					{
						my ($entry, $lno) = @$line;
						my $epatt = EnergyPattern->new();
						$err = $epatt->readString( $entry, $model );
						if ($err) {  $err = errgen( $err, $lno );  goto EXIT;  }
						push @{$model->EnergyPatterns}, $epatt;
					}
					# update 
					printf "Read %d energy patterns(s).\n", scalar @{$model->EnergyPatterns};  
				
				}					

				
				### Read Actions Block
				elsif ( $name eq 'actions' )
				{
					unless ($model->Params->{allow_actions})
                    {
						unless ($model->Params->{action_skip_warn})
                        {   send_warning( err_gen("Skipping actions") );   }
						next;
					}
                    # Read actions
					foreach my $line ( @$block_dat )
                    {
						my ($entry, $lno) = @$line;
						# Remove (and ignore) leading index from line
						$entry =~ s/^\d+\s+//;
                        # Get action and options
						my ($action, $options);
						if ( $entry =~ /^\s*(\w+)\s*\((.*)\);?\s*$/ )
                        {
                            $action  = $1;
                            $options = $2; 
                        }
                        else                        
                        {
							$err = "Line $entry does not appear to contain a command";
							$err = errgen( $err, $lno );
						}

                        # TODO: validate action                        
                        # TODO: validate option syntax

                        # Perform self-consistency checks before operations are performed on model
						if ( $err = $model->ParamList->check() )
                        {
							$err = errgen($err);
							goto EXIT;
						}

                        # build command						
                        my $command = '$model->' . $action . '(' . $options . ');';
                        # execute action
						my $t_start = cpu_time(0);
						$err = eval $command;
                        if ($@)   {  $err = errgen($@, $lno);   goto EXIT;  }
						if ($err) {  $err = errgen($err, $lno); goto EXIT;  }

                        # report time (TODO: maybe more to action?)						
						my $t_interval = cpu_time(0) - $t_start;
						if ( $t_interval > 0.0 )
                        {
							printf "CPU TIME: %s %.1f s.\n", $1, $t_interval;
						}
					}
				}
				

				### Try to read any other Block type (probably an error)
				else
                {   # warn user
					send_warning( errgen("Could not process block type $name") );
				}
			}

			elsif ( $string =~ /^\s*(setOption)\s*\((.*)\);?\s*$/ )
			{   # special action: setOption(opts)
                my $action = $1;
                my $options = $2;
				
                # Perform self-consistency checks before operations are performed on model
				if ( $err = $model->ParamList->check() )
                {  $err = errgen($err);  goto EXIT;  }

				# call to methods associated with $model
                my $command = '$model->' . $action . '(' . $options . ');';
				$err = eval $command;
				if ($@)   {  $err = errgen($@);    goto EXIT;  }
				if ($err) {  $err = errgen($err);  goto EXIT;  }
			}

			elsif ( $string =~ s/^\s*(parameter|param|par)\s+//i )
			{   # Define a parameter outside of the Parameter block
				unless ($in_model)
                {
					$err = errgen("Parameter cannot be defined outside of a model");
					goto EXIT;
				}
                # read parameter
				$err = $model->ParamList->readString($string);
                if ($err) {  $err = errgen($err);  goto EXIT;  }
			}

			elsif ( $string =~ /^\s*(\w+)\s*\((.*)\);?\s*$/ )
			{   # execute an Action:  "action(options)"
                my $action = $1;
                my $options = $2;
				
                unless ($model->Params->{allow_actions})
                {
					unless ($model->Params->{action_skip_warn})
                    {   send_warning( errgen("Skipping actions") );   }
					next;
				}

                # Perform self-consistency checks before operations are performed on model
				if ( $err = $model->ParamList->check() )
                {
					$err = errgen($err);
					goto EXIT;
				}

				# call to methods associated with $model
                my $command = '$model->' . $action . '(' . $options . ');';
				my $t_start = cpu_time(0);    # Set cpu clock offset
				$err = eval $command;
				if ($@)   {  $err = errgen($@);    goto EXIT;  }
				if ($err) {  $err = errgen($err);  goto EXIT;  }
				my $t_interval = cpu_time(0) - $t_start;
				if ( $t_interval > 0.0 )
                {
					printf "CPU TIME: %s %.1f s.\n", $1, $t_interval;
				}
			}
			else
			{   # Try to execute general PERL code (Dangerous!!)
				if ( $model->Params->{allow_perl} )
                {
				    # General Perl code
				    eval $string;
				    if ($@) {  $err = errgen($@);  goto EXIT;  }
				}
                else
                {
					send_warning( errgen("Unidentified input! Will not attempt to execute as Perl.") );
                    next;
                }
			}
		}




      EXIT:
        unless ($err)
        {   
            # if we're back at level 0, perform any required actions
            if ($level == 0)
            {
      		    if ( $model->Params->{write_xml} )
                {  $model->writeXML();   }

		        if ( $model->Params->{generate_network} )
                {  $model->generate_network( {overwrite=>1} );  }

		        if ( $model->Params->{write_mfile} )
                {  $model->writeMfile();  }

		        if ( $model->Params->{write_sbml} )
                {  $model->writeSBML();   }
            }

            # indicate that we're finished
            print "Finished processing file $filename.\n";          
            if ($level == 0)
            {   # write time info
                printf "CPU TIME: total %.1f s.\n", cpu_time(0) - $t_start;
                # restore STDOUT
                if ($params->{logging})
                {
                    unless( open STDOUT, ">&", $stdout_handle )
                    {  $err = "Problem restoring STDOUT: $!\n";  } 
                }
            }
        }


        # RECURSION BOOKKEEPING..
		if ($level > 0)
		{   # retrieve previous filename, file_dat and line_number
			$filename = pop @filenames;
			$file_dat = pop @file_dats;
			$line_number = pop @line_numbers;
		}	

        # drop a level
        --$level;
        # return with any error messages	
		return $err;
	}

    ###
    ###

	sub read_block_array
	{
		my $name  = shift;
		my @array = ();

		my $got_end = 0;
		while ( $_ = get_line() )
		{
			# Look for end of block or errors
			if ( s/^\s*end\s+// )
			{
				my $ename = $_;
				$ename =~ s/\s*$//;
				$ename =~ s/\s+/ /g;
				if ( $ename ne $name )
				{
					return ( [], errgen("end $ename does not match begin $name") );
				}
				else
				{
					$got_end = 1;
					#print "end at $line_number\n";
					last;
				}
			}
			elsif ( /^\s*begin\s*/ )
			{
				return ( [], errgen("begin block before end of previous block $name") );
			}

			# Add declarations from current line
			push @array, [ ( $_, $line_number ) ];

			#print "$_ $line_number\n";
		}
		if ( !$got_end )
		{
			return ( [], errgen("begin $name has no matching end $name") );
		}

		return ( [@array] );
	}

    ###
    ###

	sub errgen
	{
		my $err = shift @_;
		my $lno = @_ ? shift : $line_number;
		$err =~ s/[*]/\*/g;
		my $reterr = sprintf "%s\n  at line $lno", $err;
        if (defined $filename) { $reterr .= " of file '$filename'"; }
		return $reterr;
	}

    ###
    ###

	sub get_line
	{
		my $line;
		while ( $line = shift @$file_dat )
		{
			++$line_number;
			chomp $line;                         # remove newline character
			$line =~ s/\#.*$//;                  # remove comments
			unless ( $line =~ /\S+/ ) { next; }  # skip blank lines
			while ( $line =~ s/\\\s*$// )
			{   # line continuations "\"
                unless (scalar @$file_dat) { last; }  # end if there are no more lines
				++$line_number;
				my $nline = shift @$file_dat;
				chomp $nline;
				$nline =~ s/\#.*$//;                  # remove comments
				$line .= $nline;                      # append to previous line	
			}
			last;
		}
		return $line;
	}

    # END of readFile method
}



###
###
###



# Syntax
#   setOption(name,value,name,value,...) Set option value pairs
# First call will cause initialization with default values.
sub setOption
{
	my $model = shift @_;
	my $err   = '';

	# Process options
	while (@_)
    {
		my $arg = shift @_;
		@_ || return ("No value specified for option $arg");
		my $val = shift @_;

		if ( $arg eq "SpeciesLabel" ) {

			# SpeciesLabel method can only be changed prior to reading species.
			# Otherwise, inconsistent behavior could arise from changing the
			# labeling method.
			if ( $model->SeedSpeciesList ) {
				return ( "SpeciesLabel attribute can only be changed prior to reading of species.");
			}
			( $err = SpeciesGraph::setSpeciesLabel($val) )  and  return ($err);
			$model->Options->{$arg} = $val;
		}
		elsif ( $arg eq "energyBNG" )
		{   # enable energy mode
		    if ( scalar @{$model->RxnRules} )
            {   return ( "energyBNG mode can only be changed prior to reading ReactionRules.");  }
		    $model->Options->{$arg} = $val;
		}
		else {
			return "Unrecognized option $arg in setOption";
		}
	}

	return '';
}



###
###
###



sub substanceUnits
{
	my $model = shift;
	my $units = shift;

	my $ucommand = "";
	if ( $units =~ /^conc/i ) {
		$ucommand = "Concentration";
	}
	elsif ( $units =~ /^num/i ) {
		$ucommand = "Number";
	}
	else {
		return "Invalid argument to subtanceUnits $units: valid arguments are Number and Concentration";
	}

	print "SubstanceUnits set to $ucommand.\n";
	$model->SubstanceUnits($ucommand);
	return '';
}



###
###
###



sub setVolume
{
	my $model            = shift @_;
	my $compartment_name = shift @_;
	my $value            = shift @_;

	my $err = $model->CompartmentList->setVolume( $compartment_name, $value );
	return ($err);
}



###
###
###



# Set the concentration of a species to specified value.
# Value may be a number or a parameter.
sub setConcentration
{
	my $model = shift @_;
	my $sname = shift @_;
	my $value = shift @_;
    
	return '' if $NO_EXEC;

	my $plist = $model->ParamList;
	my $err;

	# SpeciesGraph specified by $sname
	my $sg = SpeciesGraph->new;
	$err = $sg->readString( \$sname, $model->CompartmentList, 1, '', $model->MoleculeTypesList );
	if ($err) { return $err; }

	# Should check that this SG specifies a complete species, otherwise
	# may match a number of species.

	# Find matching species
	my $spec;
	unless ( $spec = $model->SpeciesList->lookup($sg) )
	{
		$err = sprintf "Species %s not found in SpeciesList", $sg->toString();
		return $err;
	}

	# Read expression
	my $expr    = Expression->new();
	my $estring = $value;
	if ( my $err = $expr->readString( \$estring, $plist ) )
	{
		return ( '', $err );
	}
	my $conc = $expr->evaluate($plist);


	# Set concentration in Species object
	$spec->Concentration($conc);

	# Set concentration in Concentrations array if defined
	if ( @{$model->Concentrations} )
	{
		$model->Concentrations->[$spec->Index - 1] = $conc;
	}

	# Set flag to update netfile when it's used
	$model->UpdateNet(1);

	printf "Set concentration of species %s to value %s\n", $spec->SpeciesGraph->StringExact, $conc;
	return '';
}



###
###
###



sub setParameter
{
	my $model = shift @_;
	my $pname = shift @_;
	my $value = shift @_;

	return '' if $NO_EXEC;

	my $plist = $model->ParamList;
	my ($param, $err);

	# Error if parameter doesn't exist
	( $param, $err ) = $plist->lookup($pname);
	if ($err) { return ($err) }

	# Read expression
	my $expr    = Expression->new();
	my $estring = "$pname=$value";
	if ( $err = $expr->readString( \$estring, $plist ) ) { return $err; }

	# Set flag to update netfile when it's used
	$model->UpdateNet(1);

	printf "Set parameter %s to value %s\n", $pname, $expr->evaluate($plist);
	return '';
}



###
###
###



sub saveConcentrations
{
	my $model = shift @_;

	if (@{$model->Concentrations})
    {
    	my $i = 0;
		foreach my $spec ( @{$model->SpeciesList->Array} )
        {
			$spec->Concentration( $model->Concentrations->[$i] );
			++$i;
		}
	}
	return '';
}



###
###
###



sub resetConcentrations
{
	my $model = shift @_;

	return '' if $NO_EXEC;

	$model->Concentrations( [] );
	return '';
}



###
###
###



sub setModelName
{
	my $model = shift @_;
	my $name  = shift @_;

	$model->Name($name);
	return '';
}


###
###
###



# Function to require the version conform to specified requirement
# Syntax: version(string);
# string = major[.minor][.dist][+-]
#
# major, minor, and dist. indicate the major, minor, and distribution number
# respectively against which the BioNetGen version numbers will be compared.
# + indicates version should be the specified version or later (default)
# - indicates version should be the specified version or earlier
sub version
{
	my $model   = shift @_;
	my $vstring = shift @_;

	return '' if $NO_EXEC;

	if (@_)
    {   return "Version called with too many arguments.";   }

	# Determine whether specified version is upper or lower bound
	my $crit = 1;
	if ( $vstring =~ s/([+-])$// ) {
		$crit = $1 . "1";
	}

	# Process requested version
	my @version;
	@version = ();
	my $sstring = $vstring;
	while ( $sstring =~ s/^(\d+)[.]?// ) {
		push @version, $1;
	}
	if ($sstring) {
		return ("String $vstring is an invalid version number specification.");
	}

	# Convert version to 15 digit number
	my $r_number = sprintf "%05d%05d%05d", @version;

	# Determine current version of BNG
	my $bng_string = BNGversion();
	my ( $major, $minor, $rel ) = split( '\.', $bng_string );

    # Increment release if '+' appended to increment development version to next release.
	if ( $rel =~ s/[+]$// ) {
		++$rel;
	}
	my $bng_number = sprintf "%05d%05d%05d", $major, $minor, $rel;

	if ( $crit > 0 ) {
		if ( $bng_number < $r_number ) {
			return ( "This file requires BioNetGen version $vstring or later.  Active version is $bng_string." );
		}
	}
	else {
		if ( $bng_number > $r_number ) {
			return ( "This file requires BioNetGen version $vstring or earlier. Active version is $bng_string." );
		}
	}

	# Add current version requirement to the model
	push @{ $model->Version }, $vstring;

	return '';
}



###
###
###



sub quit
{
    # quick exit. no cleanup. no error messages
    # This is useful when the user desires to exit before
    #  performing a set of actions and it would be tedious to 
    #  comment out all those actions.
    exit(0);
}



###
###
###



sub writeSimpleBNGL
{
	my $model = shift @_;


	return '' if $NO_EXEC;

	# Species
	my $out = $model->SpeciesList->writeBNGL( $model->Concentrations, $model->ParamList );

	# Reactions
	$out   .= $model->RxnList->writeBNGL( '', $model->ParamList );

    return $out;
}



###
###
###



# write Model to BNGL format
#  (also writes reaction network, if it exists)
sub writeBNGL
{
	my $model  = shift @_;
	my $params = @_ ? shift @_ : {};
	my $out    = '';
	
    use strict;
    
	my %vars = (
		'prefix'       => $model->getOutputPrefix(),
		'TextReaction' => 0,
		'NETfile'      => 0
	);
	my %vars_pass = ();

	foreach my $key ( keys %$params )
	{
		if ( defined( $vars{$key} ) )
		{
			$vars{$key} = $params->{$key};
			if ( defined $vars_pass{$key} )
			{
				$vars_pass{$key} = $params->{$key};
			}
		}
		elsif ( defined $vars_pass{$key} )
		{
			$vars_pass{$key} = $params->{$key};
		}
		else
		{
			die "Unrecognized parameter $key in writeBNGL";
		}
	}

	return '' if $NO_EXEC;


    # !!! Begin writing file !!!

	# Header
	my $version = BNGversion();
	$out .= "# Created by BioNetGen $version\n";

	# Version requirements
	foreach my $vstring ( @{$model->Version} )
	{   $out .= "version(\"$vstring\");\n";   }

	# Options
	while ( my ($opt,$val) = each %{$model->Options} )
	{
        next if ( $opt eq 'prefix' );
        next if ( $opt eq 'suffix' );
        $out .= "setOption(\"$opt\",\"$val\");\n";
    }

	# Units
	$out .= sprintf "substanceUnits(\"%s\");\n", $model->SubstanceUnits;


    # Begin Model
    $out .= "\nbegin model\n"  unless ( $vars{NETfile} );


	# Parameters
	$out .= $model->ParamList->writeBNGL( $vars{NETfile} );


	# Compartments
	if ( defined $model->CompartmentList  and  @{$model->CompartmentList->Array} )
	{   $out .= $model->CompartmentList->toString( $model->ParamList );   }


	# MoleculeTypes
	$out .= $model->MoleculeTypesList->writeBNGL( {NETfile=>$vars{NETfile}} );


	# Observables	
	if ( @{$model->Observables} )
	{
	    # find max length of observable name
	    my $max_length = 0;
		foreach my $obs ( @{$model->Observables} )
		{
		    $max_length = ( length $obs->Name > $max_length ) ? length $obs->Name : $max_length;
		}
	    
	
		$out .= "begin observables\n";
        #$out .= "\n"  unless ( $vars{NETfile} );
		my $io = 1;
		foreach my $obs ( @{$model->Observables} )
		{
		    if ( $vars{NETfile} )
		    {
			    $out .= sprintf "%5d %s\n", $io, $obs->toString();
			}
			else
			{
			    $out .= sprintf "  %s\n", $obs->toString($max_length);
			}
			++$io;
		}
        #$out .= "\n"  unless ( $vars{NETfile} );		
		$out .= "end observables\n";
	}


	# Energy Patterns
	if ( @{$model->EnergyPatterns} )
	{
		$out .= "begin energy patterns\n";
		#$out .= "\n"  unless ( $vars{NETfile} );		
		my $io = 1;
		foreach my $epatt ( @{ $model->EnergyPatterns } )
		{
		    if ( $vars{NETfile} )
		    {
			    $out .=  sprintf "%5d %s\n", $io, $epatt->toString($model->ParamList);
			}
			else
			{
			    $out .=  sprintf "  %s\n", $epatt->toString($model->ParamList);
			}
			++$io;
		}
		#$out .= "\n"  unless ( $vars{NETfile} );		
		$out .= "end energy patterns\n";
	}


	# Functions
	$out .= $model->ParamList->writeFunctions( {NETfile=>$vars{NETfile}} );
	
	
	# Species
  	$out .= $model->SpeciesList->writeBNGL( $model->Concentrations, $model->ParamList, 0, {NETfile=>$vars{NETfile}} );


	# Reaction rules
	$out .= "begin reaction rules\n";
    {
        my $irxn = 1;
	    foreach my $rset ( @{$model->RxnRules} )
	    {
            $out .= "\n"  unless ($vars{NETfile}  or  $irxn == 1);
		    my $rreverse = ( @$rset > 1 ) ? $rset->[1] : undef;

            # write BNGL rule
		    $out .= sprintf "  %s\n", $rset->[0]->toString($rreverse);
		
		    # write actions if this is a NETfile
		    if ( $vars{NETfile} )
	        {
		        $out .= $rset->[0]->listActions();
		        if ( defined $rreverse )
		        {
		    	    $out .= "  # Reverse\n";
		    	    $out .= $rset->[1]->listActions();
		        }    
		    }
            ++$irxn;
	    }
    }
	$out .= "end reaction rules\n";


    # only write network blocks if this is a NETfile!
	if ( $vars{NETfile} )
	{
	    # Reactions	
	    if ( $vars{TextReaction} )
	    {
		    print "Writing full species names in reactions.\n";
	    }
	
	    $out .= $model->RxnList->writeBNGL( $vars{TextReaction}, $model->ParamList );


	    # Groups
	    if ( @{$model->Observables} )
	    {
		    $out .= "begin groups\n";
		    my $io = 1;
		    foreach my $obs ( @{$model->Observables} )
		    {
			    $out .= sprintf "%5d %s\n", $io, $obs->toGroupString( $model->SpeciesList );
			    ++$io;
		    }
		    $out .= "end groups\n";
	    }
	}


    # End Model
    $out .= "end model\n"  unless ( $vars{NETfile} );

	return $out;
}



###
###
###



# write Reaction Network to .NET file
sub writeNET
{
	my $model  = shift @_;
	my $params = @_ ? shift @_ : {};

	my %vars = (
		'simple' => 0,
		'prefix' => $model->getOutputPrefix(),
		'suffix' => ''
	);
	my %vars_pass = (
		'TextReaction' => '',
		'NETfile'      => '1'
	);

	foreach my $key ( keys %$params ) {
		if ( defined( $vars{$key} ) ) {
			$vars{$key} = $params->{$key};
			if ( defined( $vars_pass{$key} ) ) {
				$vars_pass{$key} = $params->{$key};
			}
		}
		elsif ( defined( $vars_pass{$key} ) ) {
			$vars_pass{$key} = $params->{$key};
		}
		else {
			die "Unrecognized parameter $key in writeNET";
		}
	}

	return '' if $NO_EXEC;

	my $suffix = $vars{suffix};
	my $prefix = $vars{prefix};
	my $simple = $vars{simple};
	if ( $suffix ne "" ) {
		$prefix .= "_${suffix}";
	}
	my $file = "${prefix}.net";

	unless ( $model->RxnList )
	{
	    return ( "writeNET() has nothing to do: no reactions in current model.\n"
	            ."  Did you remember to call generate_network() before attempting to\n"
	            ."  write network output?");
	}

	# Print network to file
    my $FH;
	open $FH, '>', $file  or return "Couldn't write to $file: $!\n";
	if ($simple) {
		print $FH $model->writeSimpleBNGL();
	}
	else {
		print $FH $model->writeBNGL( \%vars_pass );
	}
	close $FH;
	print "Wrote network to $file.\n";
	$model->UpdateNet(0);

	return '';
}



###
###
###



# Add equilibrate option, which uses additional parameters
# t_equil and spec_nonequil.  If spec_nonequil is set, these
# species are not used in equilibration of the network and are only
# added after equilibration is performed. Network generation should
# re-commence after equilibration is performed if spec_nonequil has
# been set.

sub generate_network
{
	my $model  = shift @_;
	my $params = shift @_;

    # default parameters
	my %vars = (
		'max_iter'   => '100',
		'max_agg'    => '1e99',
		'max_stoich' => '',
		'check_iso'  => '1',
		'prefix'     => $model->getOutputPrefix(),
		'overwrite'  => 0,
		'print_iter' => 0,
		'verbose'    => 0
	);

	
    # default parameters passed to called methods
	my %vars_pass = (
        'TextReaction' => '',
		'prefix'       => $model->getOutputPrefix()
	);


    # get user parameters
	foreach my $key ( keys %$params )
    {
		if ( exists $vars{$key} )
        {
			$vars{$key} = $params->{$key};
			if ( exists $vars_pass{$key} )
            {
				$vars_pass{$key} = $params->{$key};
			}
		}
		elsif ( exists $vars_pass{$key} )
        {
			$vars_pass{$key} = $params->{$key};
		}
		else
        {
			return "Unrecognized parameter $key in generate_network";
		}
	}

	return '' if $NO_EXEC;


    # check verbose option
    my $verbose = $vars{verbose};

    # Check if existing net file has been created since last modification time of .bngl file
	my $prefix    = $vars{prefix};
	my $overwrite = $vars{overwrite};
	if ( -e "$prefix.net"  and  -e "$prefix.bngl" )
    {
		if ($overwrite)
        {
			send_warning("Removing old network file $prefix.net.");
			unlink "$prefix.net";
		}
		elsif ( -M "$prefix.net" < -M "$prefix.bngl" )
        {
			send_warning("$prefix.net is newer than $prefix.bngl so reading NET file.");
			my $err = $model->readFile( { file => "$prefix.net" } );
			return $err;
		}
		else
        {
			return "Previously generated $prefix.net exists.  Set overwrite=>1 option to overwrite.";
		}
	}

	unless ( defined $model->SpeciesList )
    {   return "No species defined in call to generate_network";   }
	my $slist = $model->SpeciesList;

	unless ( defined $model->RxnList )
    {
		$model->RxnList( RxnList->new );
		$model->RxnList->SpeciesList($slist);
	}
	my $rlist = $model->RxnList;

    
	# Initialize observables
	foreach my $obs ( @{$model->Observables} )
	{
        $obs->update( $slist->Array );
    }	
    # Set ObservablesApplied attribute to everything in SpeciesList
    foreach my $spec ( @{$slist->Array} )
    {
        $spec->ObservablesApplied(1);
    }


    # Initialize energy patterns (for energyBNG only)
    if ( $model->Options->{energyBNG} )
	{
	    foreach my $epatt ( @{$model->EnergyPatterns} )
	    {
	        (my $err) = $epatt->updateSpecies( $slist->Array );
	    }
	}
	

	unless ( defined $model->RxnRules )
    {   return "No reaction_rules defined in call to generate_network";   }

	my $nspec       = scalar @{$slist->Array};
	my $nrxn        = scalar @{$rlist->Array};
	my @rule_timing = ();
	my @rule_nrxn   = ();
	
	# update user with initial report
	report_iter( 0, $nspec, $nrxn );

    # now perform network generation steps
	foreach my $niter ( 1 .. $vars{max_iter} )
	{
		my $t_start_iter = cpu_time(0);
		my @species = @{$slist->Array};

		# Apply reaction rules
		my $irule = 0;
		my ($n_new, $t_off, $n_new_tot);
		$n_new_tot = 0;
		# NOTE: each element of @{$model->RxnRules} is an array of reactions.
		#  If a rule is unidirectional, then the array has a single element.
		#  If a rule is bidirectional, then the array has two elements (forward and reverse)

		foreach my $rset ( @{$model->RxnRules} )
		{
			if ($verbose) { printf "Rule %d:\n", $irule + 1; }
			$n_new = 0;
			$t_off = cpu_time(0);
			my $dir = 0;

			foreach my $rr (@$rset)
			{

				if ($verbose)
                {
					if ($dir == 0) { print "  forward:\n"; }
					else           { print "  reverse:\n"; }
				}
             
				# change by Justin for compartments
				# added plist
				$n_new += $rr->applyRule( $slist, $rlist,
					                      $model->ParamList,
					                      \@species,
					                      {  max_agg    => $vars{max_agg},
						                     check_iso  => $vars{check_iso},
						                     max_stoich => $vars{max_stoich},
						                     verbose    => $vars{verbose},
					                      }
				                        );
				++$dir;
			}
		
			my $time = cpu_time(0) - $t_off;
			$rule_timing[$irule] += $time;
			$rule_nrxn[$irule]   += $n_new;

			if ($verbose)
            {   printf "Result: %5d new reactions %.2e CPU s\n", $n_new, $time;   }

			$n_new_tot += $n_new;
			++$irule;
		}


        # update RulesApplied for species processed in this interation
		foreach my $spec (@species)
		{
			$spec->RulesApplied($niter)  unless ( $spec->RulesApplied );
		}

		# Update observables
        foreach my $obs ( @{$model->Observables} )
	    {
            $obs->update( $slist->Array );
        }
        # Set ObservablesApplied attribute to everything in SpeciesList
        foreach my $spec ( @{$slist->Array} )
        {
            $spec->ObservablesApplied(1);
        }

		# update energy patterns (for energyBNG only)
		if ( $model->Options->{energyBNG} )
		{
		    foreach my $epatt ( @{$model->EnergyPatterns} )
		    {
		        (my $err) = $epatt->updateSpecies( $slist->Array );
		    }
		}
	
	    # Finalize ratelaws for each new Rxn (energyBNG only)
        if ( $model->Options->{energyBNG} )
	    {
    	    foreach my $rxn_set ( keys %{$rlist->Hash} )
    	    {
    	        foreach my $rxn ( @{$rlist->Hash->{$rxn_set}} )
    	        {
    	            (my $err) = $rxn->updateEnergyRatelaw( $model );
    	        }
    	    }
        }

		$nspec = scalar @{$slist->Array};
		$nrxn  = scalar @{$rlist->Array};
		report_iter( $niter, $nspec, $nrxn, $t_start_iter );


		# Free memory associated with RxnList hash
		$rlist->resetHash;

		# Stop iteration if no new species were generated
		#printf "nspec=$nspec last= %d\n", scalar(@species);
		last if ( $nspec == scalar @species );

		# Print network after current iteration to netfile
		if ( $vars{print_iter} )
        {
			$vars_pass{prefix} = "${prefix}_${niter}";
			if ( my $err = $model->writeNET(\%vars_pass) ) { return $err; }
			$vars_pass{prefix} = $prefix;
		}
	}
        
        		
	# Print rule timing information
	printf "Cumulative CPU time for each rule\n";
	my $t_tot = 0;
    my $n_tot = 0;
	foreach my $irule ( 0 .. $#{$model->RxnRules} )
	{
		my $eff = ( $rule_nrxn[$irule] ) ? $rule_timing[$irule] / $rule_nrxn[$irule] : 0.0;
		printf "Rule %3d: %5d reactions %.2e CPU s %.2e CPU s/rxn\n",
		           $irule + 1, $rule_nrxn[$irule], $rule_timing[$irule], $eff;
		$t_tot += $rule_timing[$irule];
		$n_tot += $rule_nrxn[$irule];
	}
	my $eff = ($n_tot) ? $t_tot / $n_tot : 0.0;
	printf "Total   : %5d reactions %.2e CPU s %.2e CPU s/rxn\n", $n_tot, $t_tot, $eff;

	# Print result to netfile
	if ( my $err = $model->writeNET(\%vars_pass) ) { return $err; }

	return '';


    ###
    ###
    ###


    sub report_iter
    {
        my $niter = shift @_;
        my $nspec = shift @_;
        my $nrxn  = shift @_;
        my $t_start_iter = @_ ? shift @_ : undef;

	    printf "Iteration %3d: %5d species %6d rxns", $niter, $nspec, $nrxn;
	    my $t_cpu = defined $t_start_iter ? cpu_time(0) - $t_start_iter : 0;
	    printf "  %.2e CPU s", $t_cpu;
	    if ($HAVE_PS) {
		    my ( $rhead, $vhead, $rmem, $vmem ) = split ' ', `ps -o rss,vsz -p $$`;
		    printf " %.2e (%.2e) Mb real (virtual) memory.",
                    $t_cpu, $rmem / 1000, $vmem / 1000;
	    }
	    printf "\n";
	    return;
    }

    # end generate network

}



###
###
###



sub findExec
{
	use Config;
	my $prog = shift @_;

	my $exec = BNGpath( "bin", $prog );

	# First look for generic binary in BNGpath
	if ( -x $exec )
    {   return ($exec);   }

	my $arch = $Config{myarchname};

	# Currently recognized values of $arch are
	# i686-linux, ppc-darwin, MSWin32

	# Then look for os specific binary
	$exec .= "_${arch}";

	if ( $arch =~ /MSWin32/ )
    {   $exec .= ".exe";   }

	if ( -x $exec )
    {
		return ($exec);
	}
	else
    {
		print "findExec: $exec not found.\n";
		return '';
	}
}



###
###
###



# get output prefix = outputDir/modelName[_outputSuffix]
sub getOutputPrefix
{
    my $model = shift;

    my $file_prefix = $model->Name;
    if ( $model->Params->{suffix} )
    {   $file_prefix .= '_' . $model->Params->{output_suffix};   }

    return File::Spec->catfile( ($model->getOutputDir()), $file_prefix );
}

###
###

# returns 0 if everything is ok, otherwise 1.
sub setOutputDir
{
    my $model = shift;
    my $dir   = (@_) ? shift : undef;

    unless (defined $dir)
    {   # default to current directory
        $dir = File::Spec->curdir();
    }

    # set output directory
    $model->Params->{output_dir} = $dir;
}

###
###

# get the output directory
sub getOutputDir
{
    my $model = shift;
    unless ( defined $model->Params->{output_dir} )
    {   # output directory not defined, set to default
        $model->setOutputDir();
    }
    return $model->Params->{output_dir};
}



###
###
###

1;

