package BNGModel;

use Class::Struct;
use FindBin;
use lib $FindBin::Bin;
use MoleculeTypesList;
use ParamList;
use Function;
use Compartment;
use CompartmentList;
use SpeciesList;
use RxnRule;
use Observable;
use BNGUtils;

#use strict;

my $NO_EXEC = 0; # Prevents execution of functions to allow file syntax checking
my $HAVE_PS = 0
  ; # Set to 0 for MS Windows systems with no ps command - disables reporting of
    # memory usage at each iteration of network generation

# Set up global variable for referencing the ParameterList object
$BNGModel::PARAM_LIST = undef;
# Set up global timer variable (for timing routines)
$BNGModel::TIMER = 0;

# Structure containing all BioNetGen model data
struct BNGModel => {
	Name              => '$',
	Time              => '$',
	Concentrations    => '@',
	MoleculeTypesList => 'MoleculeTypesList',
	SpeciesList       => 'SpeciesList',
	SeedSpeciesList   => 'SpeciesList',
	RxnList           => 'RxnList',
	RxnRules          => '@',
	ParamList         => 'ParamList',
	Observables       => '@',
	CompartmentList   => 'CompartmentList',
	SubstanceUnits    => '$',
	UpdateNet => '$',  # This variable is set to force update of NET file before
	                   # simulation
	Version =>
	  '@', # Indicates set of version requirements- output to BNGL and NET files
	Options =>
	  '%'    # Options used to control behavior of model and associated methods
};

# Read bionetgen data in blocks enclosed by begin param end param
# lines.  Prevents overwriting of variables possible with eval.

# To do:
# 1.  Receive a valid list of parameter names to be read
# 2.  Check syntax of lines- this is currently done when parameter is
#     handled.  Some basic checks could be done here.

# Lines between begin and end commands are put into arrays with the name given by the
# block name

{
	my $file, $line_number, $file_dat;
	my $level = 0, @files, @lnos, @fdats;
	my $MAX_LEVEL = 5;    # Sets maximum level of allowed recursion
	my %bngdata;

	sub readFile {
		my $model = shift;
		my $params = (@_) ? shift : "";

		# Internal control parameters
		my $fname            = "";
		my $prefix           = "";
		my $write_xml        = 0;
		my $write_mfile      = 0;
		my $write_sbml       = 0;
		my $generate_network = 0;
		my $allow_actions    = 1;
		my $action_skip_warn = 0;

		# Process optional parameters
		if ($params) {
			for my $param ( keys %$params ) {
				if ( $param eq "no_exec" ) {
					$NO_EXEC = $params->{no_exec};
				}
				elsif ( $param eq "file" ) {
					$fname = $params->{file};
				}
				elsif ( $param eq "prefix" ) {
					$prefix = $params->{prefix};
				}
				elsif ( $param eq "write_xml" ) {
					$write_xml     = 1;
					$allow_actions = 0;
				}
				elsif ( $param eq "write_mfile" ) {
					$write_mfile      = 1;
					$allow_actions    = 0;
					$generate_network = 1;
				}
				elsif ( $param eq "write_sbml" ) {
					$write_sbml       = 1;
					$allow_actions    = 0;
					$generate_network = 1;
				}
				else {
					send_warning("Parameter $param ignored");
				}
			}
		}

		if ( $fname eq "" ) {
			return ("Parameter file must be specified in readFile");
		}

		my $err;

	  READ:
		while (1) {

			# Read BNG model data
			print "readFile::Reading from file $fname\n";
			if ( !open( FH, $fname ) ) {
				return ("Couldn't read from file $fname: $!");
			}
			$file_dat = [<FH>];
			close(FH);

			$file = $fname;
			push @files, $file;
			$level = $#files;
			if ( $level == 0 ) {
				%bngdata     = ();
				$line_number = 0;
			}
			else {
				push @lnos, $line_number;
			}
			push @fdats, $file_dat;

			#print "level=$level lno=$line_number file=$file\n";
			if ( $level > $MAX_LEVEL ) {
				$err = "Recursion level exceeds maximum of $MAX_LEVEL";
				last READ;
			}

			$line_number = 0;
			if ( $prefix ne "" ) {
				print "Setting model name to $prefix\n";
				$model->Name($prefix);
				$model->UpdateNet(1);
			}
			else {

				# Set name of model based on file name
				my $name = $file;

				# Strip suffix
				$name =~ s/[.]([^.]+)$//;
				my $type = $1;
				$model->Name($name);
			}

			# Initialize parameter list
			my $plist = ParamList->new;
			$model->ParamList($plist);

 # Set Global Variable PARAM_LIST
 # (added to allow universal access to parameters. This setup will have problems
 #  if the active BNGmodel is changed.)
			$BNGModel::PARAM_LIST = $plist;

			# Initialize MoleculeTypesList
			my $mtlist = MoleculeTypesList->new;
			$model->MoleculeTypesList($mtlist);

			# Initialize CompartmentList
			my $clist = CompartmentList->new();
			$model->CompartmentList($clist);

			# Initialize SubstanceUnits
			$model->SubstanceUnits("Number");

			# Read data from file into data hash
			my $begin_model = 0;
			my $in_model    = 1;
			while ( $_ = get_line() ) {
				if (/^\s*begin\s+model\s*$/) {
					++$begin_model;
					if ( $begin_model > 1 ) {
						$err =
						  errgen("Only one model definition allowed per file");
						last READ;
					}
					$in_model = 1;
					next;
				}
				elsif (/^\s*end\s+model\s*$/) {
					if ( !$in_model ) {
						$err = errgen(
"end model encountered without enclosing begin model"
						);
						last READ;
					}
					$in_model = 0;
					next;
				}

				# Process multi-line block
				if (s/^\s*begin\s*//) {
					$name = $_;

					# Remove trailing white space
					$name =~ s/\s*$//;

					# Remove repeated white space
					$name =~ s/\s+/ /g;

					# Read block data
					my $block_dat;
					( $block_dat, $err ) = read_block_array($name);
					if ($err) { last READ; }
					$bngdata{$name} = 1;

					# Process block data
					if ( ( $name eq "parameters" ) ) {
						my $plast = $#{ $plist->Array };
						if ( !$in_model ) {
							$err = errgen(
								"$name cannot be defined outside of a model");
							last READ;
						}

						# Model parameters
						my $lno;
						my $plist = $model->ParamList;
						for my $line ( @{$block_dat} ) {
							( my $entry, $lno ) = @{$line};
							if ( $err = $plist->readString($entry) ) {
								$err = errgen( $err, $lno );
								last READ;
							}
						}
						if ( $err = $plist->check() ) {
							$err = errgen( $err, $lno );
							last READ;
						}
						if ( $err = $plist->sort() ) {
							$err = errgen( $err, $lno );
							last READ;
						}
						printf "Read %d ${name}.\n",
						  $#{ $plist->Array } - $plast;
					}
					elsif ( ( $name eq "functions" ) ) {
						my $nread = 0;
						if ( !$in_model ) {
							$err = errgen(
								"$name cannot be defined outside of a model");
							last READ;
						}

						# Model functions
						my $lno;
						my $plist = $model->ParamList;
						for my $line ( @{$block_dat} ) {
							( my $entry, $lno ) = @{$line};
							my $fun = Function->new();
							if ( $err = $fun->readString( $entry, $model ) ) {
								$err = errgen( $err, $lno );
								last READ;
							}
							++$nread;
						}
						printf "Read %d ${name}.\n", $nread;
					}
					elsif ( $name =~ /^molecule[_ ]types$/ ) {
						if ( !$in_model ) {
							$err = errgen(
								"$name cannot be defined outside of a model");
							last READ;
						}

						# MoleculeTypes
						my $mtlist = $model->MoleculeTypesList;
						for my $line ( @{$block_dat} ) {
							my ( $entry, $lno ) = @{$line};
							if ( $err = $mtlist->readString( $entry, $mtlist ) )
							{
								$err = errgen( $err, $lno );
								last READ;
							}
						}
						printf "Read %d molecule types.\n",
						  scalar( keys %{ $mtlist->MolTypes } );
					}
					elsif ( $name eq "compartments" ) {

# changes to implement:  if compartment block is defined, then we should require
# explicit declaration of species compartments.  if not defined, then
# we can skip a lot of extra processing in RxnRule
# --justin 23feb2009

						for my $line ( @{$block_dat} ) {
							my ( $entry, $lno ) = @{$line};
							if ( $err =
								$clist->readString( $entry, $model->ParamList )
							  )
							{
								$err = errgen( $err, $lno );
								last READ;
							}
						}
						if ( $err = $clist->validate() ) {
							$err = errgen( $err, $lno );
							last READ;
						}

						#print $clist->toString($plist);
						printf "Read %d compartments.\n",
						  scalar( @{ $clist->Array } );
					}
					elsif (( $name eq "species" )
						|| ( $name =~ /^seed[ _]species$/ ) )
					{
						if ( !$in_model ) {
							$err = errgen(
								"$name cannot be defined outside of a model");
							last READ;
						}

						# Species
						my $slist = SpeciesList->new;

				 # Only allow defined types if they have been explicitly defined
						my $AllowNewTypes =
						  ( scalar( keys %{ $mtlist->MolTypes } ) > 0 ) ? 0 : 1;

						#print "AllowNewTypes=$AllowNewTypes\n";
						foreach my $line ( @{$block_dat} )
						{
							my ( $entry, $lno ) = @{$line};
							if ( $err = $slist->readString( $entry, $model->ParamList,
									                        $model->CompartmentList,
									                        $model->MoleculeTypesList,
									                        $AllowNewTypes  		   ) )
							{
								$err = errgen( $err, $lno );
								last READ;
							}							
						}
						printf "Read %d species.\n",
						  scalar( @{ $slist->Array } );
						$model->SpeciesList($slist);
					}
					elsif ( $name =~ /^reaction[_ ]rules$/ ) {
						if ( !$in_model ) {
							$err = errgen(
								"$name cannot be defined outside of a model");
							last READ;
						}
						my $nerr = 0;

						# Reaction rules
						my @rrules = ();
						for my $line ( @{$block_dat} ) {
							my ( $entry, $lno ) = @{$line};

							# Error handled internally in RxnRule
							my $rrs;
							( $rrs, $err ) =
							  RxnRule::newRxnRule( $entry, $model->ParamList,
								$model->CompartmentList,
								$model->MoleculeTypesList );
							if ( $err ne "" ) {

								#last READ;
								$err = errgen( $err, $lno );
								printf STDERR "ERROR: $err\n";
								++$nerr;
							}
							else {
								push @rrules, $rrs;

				 #printf "n_rules=%d n_new=%d\n", scalar(@rrules),scalar(@$rrs);
								if ( !$rrs->[0]->Name ) {
									$rrs->[0]->Name( "Rule" . scalar(@rrules) );
								}
								if ( $#$rrs == 1 ) {
									if ( !$rrs->[1]->Name ) {
										$rrs->[1]->Name(
											"Rule" . scalar(@rrules) . "r" );
									}
								}
							}
						}
						if ($nerr) {
							$err =
"Reaction rule list could not be read because of errors";
							last READ;
						}
						$model->RxnRules( [@rrules] );
						printf "Read %d reaction rule(s).\n",
						  scalar( @{ $model->RxnRules } );
					}
					elsif ( $name eq "reactions" ) {
						if ( !$in_model ) {
							$err = errgen(
								"$name cannot be defined outside of a model");
							last READ;
						}

						# Reactions (when reading NET file)
						my $rlist = RxnList->new;
						for my $line ( @{$block_dat} ) {
							my ( $entry, $lno ) = @{$line};
							if (
								$err = $rlist->readString(
									$entry, $model->SpeciesList,
									$model->ParamList
								)
							  )
							{
								$err = errgen( $err, $lno );
								last READ;
							}
						}
						printf "Read %d reaction(s).\n",
						  scalar( @{$block_dat} );
						$model->RxnList($rlist);
					}
					elsif ( $name eq "groups" ) {
						if ( !$in_model ) {
							$err = errgen(
								"$name cannot be defined outside of a model");
							last READ;
						}

						# Groups (when reading NET file)
						# Must come after Observables
						if ( !$model->Observables ) {
							$err = errgen(
								"Observables must be defined before Groups",
								$lno );
							last READ;
						}
						my $iobs   = 0;
						my $maxobs = $#{ $model->Observables };
						for my $line ( @{$block_dat} ) {
							my ( $entry, $lno ) = @{$line};
							my @tokens = split( ' ', $entry );

							# Skip first entry if it's an index
							if ( $tokens[0] =~ /^\d+$/ ) { shift(@tokens) }

							if ( $iobs > $maxobs ) {
								$err = errgen( "More groups than observables",
									$lno );
								last READ;
							}
							my $obs = $model->Observables->[$iobs];

							# Check that Observable and Group names match
							if ( $tokens[0] ne $obs->Name ) {
								$err = errgen(
"Group named $tokens[0] is not compatible with any observable",
									$lno
								);
								last READ;
							}
							shift(@tokens);

							my @array = split( ',', $tokens[0] );
							*weights = $obs->Weights;

							# Zero the weights
							@weights = (0) x @weigths;
							my $w, $ind;
							for $elt (@array) {
								if ( $elt =~ s/^([^*]*)\*// ) {
									$w = $1;
								}
								else {
									$w = 1;
								}
								if ( $elt =~ /\D+/ ) {
									$err =
									  errgen( "Non-integer group entry: $elt",
										$lno );
									last READ;
								}
								$weights[$elt] += $w;
							}
							++$iobs;
						}
						printf "Read %d group(s).\n", scalar( @{$block_dat} );
					}
					elsif ( $name eq "observables" ) {
						if ( !$in_model ) {
							$err = errgen(
								"$name cannot be defined outside of a model");
							last READ;
						}

						# Observables
						my @observables = ();
						for my $line ( @{$block_dat} ) {
							my ( $entry, $lno ) = @{$line};
							my $obs = Observable->new();
							if ( $err = $obs->readString( $entry, $model ) ) {
								$err = errgen( $err, $lno );
								last READ;
							}
							push @observables, $obs;
						}
						$model->Observables( [@observables] );
						printf "Read %d observable(s).\n",
						  scalar( @{ $model->Observables } );
					}
					elsif ( $name eq "actions" ) {
						if ( !$allow_actions ) {
							if ( !$action_skip_warn ) {
								send_warning("Skipping actions");
								$action_skip_warn = 1;
							}
							next;
						}
						for my $line ( @{$block_dat} ) {
							my ( $entry, $lno ) = @{$line};

							# Remove (and ignore) leading index from line
							$entry =~ s/^\d+\s+//;

							my $command;
							if ( $entry =~ /^\s*([A-Za-z0-9_]+)\s*\(/ ) {
								$command = $1;
							}
							else {
								$err =
"Line $entry does not appear to contain a command";
								$err = errgen( $err, $lno );
							}

	  # Perform self-consistency checks before operations are performed on model
							if ( $err = $plist->check() ) {
								$err = errgen($err);
								last READ;
							}

							# call to methods associated with $model
							my $t_off = cpu_time(0);    # Set cpu clock offset

							$err = eval '$model->' . $entry;
							if ($err) { $err = errgen($err); last READ; }
							if ($@)   { $err = errgen($@);   last READ; }
							my $tused = cpu_time(0) - $t_off;
							if ( $tused > 0.0 ) {
								printf "CPU TIME: %s %.1f s.\n", $1, $tused;
							}
						}
					}
					else {

						# Unrecognized block name
						$err = errgen("Could not process block type $name");
						send_warning($err);
						$err = "";

						#last READ;
					}
				}
				elsif (s/^\s*(Parameter|Param)\s+//i) {
					if ( !$in_model ) {
						$err = errgen(
							"Parameter cannot be defined outside of a model");
						last READ;
					}
					my $plist = $model->ParamList;

					#printf "Reading $1 $_\n";
					if ( $err = $plist->readString($_) ) {
						$err = errgen($err);
						last READ;
					}
				}
				elsif (/^\s*([A-Za-z][^(]*)/) {
					if ( !$allow_actions ) {
						if ( !$action_skip_warn ) {
							send_warning("Skipping actions");
							$action_skip_warn = 1;
						}
						next;
					}

	  # Perform self-consistency checks before operations are performed on model
					if ( $err = $plist->check() ) {
						$err = errgen($err);
						last READ;
					}

					# call to methods associated with $model
					my $t_off = cpu_time(0);    # Set cpu clock offset
					                            #print "command: $_\n";
					$err = eval '$model->' . $_;
					if ($err) { $err = errgen($err); last READ; }
					if ($@)   { $err = errgen($@);   last READ; }
					my $tused = cpu_time(0) - $t_off;
					if ( $tused > 0.0 ) {
						printf "CPU TIME: %s %.1f s.\n", $1, $tused;
					}
				}
				else {
					if ( !$allow_actions ) {
						if ( !$action_skip_warn ) {
							send_warning("Skipping actions");
							$action_skip_warn = 1;
						}
						next;
					}

					# General Perl code
					eval $_;
					if ($@) { $err = errgen($@); last READ; }
				}
			}
			last READ;
		}    # END READ

		if ($write_xml) {
			$model->writeXML();
		}
		if ($generate_network) {
			$model->generate_network( { overwrite => 1 } );
		}
		if ($write_mfile) {
			$model->writeMfile();
		}
		if ($write_sbml) {
			$model->writeSBML();
		}

	  EXIT:
		pop @files;
		pop @fdats;
		if ($level) {
			$file        = $files[$#files];
			$file_dat    = $fdats[$#fdats];
			$line_number = pop @lnos;
			--$level;

			#print "returning to level $level file $file line $line_number\n";
		}
		print "Finished processing file $fname\n" unless $err;
		return ($err);
	}

	sub read_block_array {
		my $name  = shift;
		my @array = ();

		my $got_end = 0;
		while ( $_ = get_line() ) {

			# Look for end of block or errors
			if (s/^\s*end\s+//) {
				my $ename = $_;
				$ename =~ s/\s*$//;
				$ename =~ s/\s+/ /g;
				if ( $ename ne $name ) {
					return ( [],
						errgen("end $ename does not match begin $name") );
				}
				else {
					$got_end = 1;

					#print "end at $line_number\n";
					last;
				}
			}
			elsif (/^\s*begin\s*/) {
				return ( [],
					errgen("begin block before end of previous block $name") );
			}

			# Add declarations from current line
			push @array, [ ( $_, $line_number ) ];

			#print "$_ $line_number\n";
		}
		if ( !$got_end ) {
			return ( [], errgen("begin $name has no matching end $name") );
		}

		return ( [@array] );
	}

	sub errgen {
		my $err = shift;
		my $lno = (@_) ? shift : $line_number;
		$err =~ s/[*]/\*/g;
		my $reterr = sprintf "%s\n  at line $lno of file $file", $err;
		return ($reterr);
	}

	sub get_line {
		my $line = "";

		while ( $line = shift(@$file_dat) ) {
			++$line_number;
			chomp($line);
			$line =~ s/\#.*$//;    # remove comments
			next unless $line =~ /\S+/;    # skip blank lines
			while ( $line =~ s/\\\s*$// ) {
				++$line_number;
				my $nline = shift(@$file_dat);
				chomp($nline);
				$nline =~ s/\#.*$//;       # remove comments
				$line .= $nline;
				next unless ( !@$file_dat );
			}
			last;
		}
		return ($line);
	}
}

# Syntax
#   setOption(name,value,name,value,...) Set option value pairs
# First call will cause initialization with default values.

sub setOption {
	my $model = shift;
	my $err   = "";

	# Process options
	while (@_) {
		my $arg = shift @_;
		@_ || return ("No value specified for option $arg");
		my $val = shift @_;

		if ( $arg eq "SpeciesLabel" ) {

			# SpeciesLabel method can only be changed prior to reading species.
			# Otherwise, inconsistent behavior could arise from changing the
			# labeling method.
			if ( $model->SeedSpeciesList ) {
				return (
"SpeciesLabel attribute can only be changed prior to reading of species."
				);
			}
			( $err = SpeciesGraph::setSpeciesLabel($val) ) && return ($err);
			$model->Options->{$arg} = $val;
		}
		else {
			return "Unrecognized option $arg in setOption";
		}
	}

	return ("");
}

sub substanceUnits {
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
		return (
"Invalid argument to subtanceUnits $units: valid arguments are Number and Concentration"
		);
	}

	print "SubstanceUnits set to $ucommand.\n";
	$model->SubstanceUnits($ucommand);
	return ("");
}

sub setVolume {
	my $model            = shift;
	my $compartment_name = shift;
	my $value            = shift;

	my $err = $model->CompartmentList->setVolume( $compartment_name, $value );
	return ($err);
}

sub writeSimpleBNGL {
	my $model = shift;
	my $out   = "";

	return ("") if $NO_EXEC;

	# Species
	$out .=
	  $model->SpeciesList->writeBNGL( $model->Concentrations,
		$model->ParamList );

	# Reactions
	$out .= $model->RxnList->writeBNGL( "", $model->ParamList );

	return ($out);
}

sub writeBNGL {
	my $model  = shift;
	my $params = (@_) ? shift(@_) : "";
	my $out    = "";
	use strict;

	my %vars = (
		'prefix'       => $model->Name,
		'TextReaction' => '',
		'NETfile'      => ''
	);
	my %vars_pass = ();

	for my $key ( keys %$params ) {
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
			die "Unrecognized parameter $key in writeBNGL";
		}
	}

	return ("") if $NO_EXEC;

	# Header
	my $version = BNGversion();
	$out .= "# Created by BioNetGen $version\n";

	# Version requirements
	for my $vstring ( @{ $model->Version } ) {
		$out .= "version(\"$vstring\");\n";
	}

	# Options
	for my $opt ( keys %{ $model->Options } ) {
		$out .= sprintf "setOption(\"%s\",\"%s\");\n", $opt,
		  $model->Options->{$opt};
	}

	# Units
	$out .= sprintf "substanceUnits(\"%s\");\n", $model->SubstanceUnits;

	# Parameters
	$out .= $model->ParamList->writeBNGL( $vars{NETfile} );

	# Compartments
	if ( @{ $model->CompartmentList->Array } ) {
		$out .= $model->CompartmentList->toString( $model->ParamList );

		# Compartment volume commands
		#$out.= $model->CompartmentList->setVolumeStrings();
	}

	# MoleculeTypes
	$out .= $model->MoleculeTypesList->writeBNGL();

	# Observables
	if ( @{ $model->Observables } ) {
		$out .= "begin observables\n";
		my $io = 1;
		for my $obs ( @{ $model->Observables } ) {
			$out .= sprintf "%5d %s\n", $io, $obs->toString();
			++$io;
		}
		$out .= "end observables\n";
	}

	# Functions
	$out .= $model->ParamList->writeFunctions();

	# Species
	$out .=
	  $model->SpeciesList->writeBNGL( $model->Concentrations,
		$model->ParamList );

	# Reaction rules
	$out .= "begin reaction rules\n";
	my $irule = 1;
	for my $rset ( @{ $model->RxnRules } ) {

		#print "Rule $irule\n";
		my $id = 0;
		my $rreverse = ( $#$rset > 0 ) ? $rset->[1] : "";

		#    $out.= sprintf "%5d %s\n", $irule, $rset->[0]->toString($rreverse);
		$out .= sprintf "%s\n", $rset->[0]->toString($rreverse);
		$out .= $rset->[0]->listActions();
		if ($rreverse) {
			$out .= "# Reverse\n";
			$out .= $rset->[1]->listActions();
		}
		++$irule;
	}
	$out .= "end reaction rules\n";

	# Reactions
	if ( $vars{TextReaction} ) {
		print "Writing full species names in reactions.\n";
	}

	# added ParamList to the writeBNGL argument list  --justin
	$out .=
	  $model->RxnList->writeBNGL( $vars{TextReaction}, $model->ParamList );

	# Groups
	if ( @{ $model->Observables } ) {
		$out .= "begin groups\n";
		my $io = 1;
		for my $obs ( @{ $model->Observables } ) {
			$out .= sprintf "%5d %s\n", $io,
			  $obs->toGroupString( $model->SpeciesList );
			++$io;
		}
		$out .= "end groups\n";
	}

	return ($out);
}

# TODO: Observable outputs!
sub writeMfile {
	my $model = shift;
	my $params = (@_) ? shift(@_) : "";

	return ("") if $NO_EXEC;

	if ( !$model->RxnList ) {
		return ("No reactions in current model.");
	}

	my $plist = $model->ParamList;

	my $model_name = $model->Name;

	# Strip prefixed path
	$model_name =~ s/^.*\///;
	my $prefix =
	  ( defined( $params->{prefix} ) ) ? $params->{prefix} : $model->Name;
	my $suffix = ( defined( $params->{suffix} ) ) ? $params->{suffix} : "";
	if ( $suffix ne "" ) {
		$prefix .= "_${suffix}";
	}

	my $file = "${prefix}.m";

	open( Mfile, ">$file" ) || die "Couldn't open $file: $!\n";
	my $version = BNGversion();
	print Mfile "% M-file for model $prefix created by BioNetGen $version\n";
	print Mfile "function [t_out,obs_out,x_out]= $prefix(tend)\n\n";

	# Dimensions
	my $Nspecies   = scalar( @{ $model->SpeciesList->Array } );
	my $Nreactions = scalar( @{ $model->RxnList->Array } );
	printf Mfile "Nspecies=%d;\n",   $Nspecies;
	printf Mfile "Nreactions=%d;\n", $Nreactions;
	print Mfile "obs_out=zeros(1);";    # NOT CURRENTLY USED
	printf Mfile "\n";

	# Parameters
	print Mfile "% Parameters\n";
	for my $param ( @{ $model->ParamList->Array } ) {
		printf Mfile "%s=%s;\n", $param->Name, $param->toString($plist);
	}
	printf Mfile "\n";

	# Initial concentrations
	print Mfile "% Intial concentrations\n";
	print Mfile "x0= [";
	my $use_array = ( @{ $model->Concentrations } ) ? 1 : 0;
	my %fixed = ();
	for my $spec ( @{ $model->SpeciesList->Array } ) {
		my $conc;
		if ($use_array) {
			$conc = $model->Concentrations->[ $spec->Index - 1 ];
		}
		else {
			$conc = $spec->Concentration;
		}

		# Save indices of species with fixed concentration
		if ( $spec->SpeciesGraph->Fixed ) {
			$fixed{ $spec->Index } = 1;
		}
		printf Mfile " %s;", $conc;
	}
	print Mfile "];\n\n";

	# Stoichiometry matrix
	#my @St=();
	my %S = ();

	#print Mfile "% Stoichiometry matrix\n";
	my @fluxes = ();
	my $irxn   = 1;
	for my $rxn ( @{ $model->RxnList->Array } ) {

		# Each reactant contributes a -1
		for my $r ( @{ $rxn->Reactants } ) {
			--$S{ $r->Index }{$irxn};
		}

		# Each product contributes a +1
		for my $p ( @{ $rxn->Products } ) {
			++$S{ $p->Index }{$irxn};
		}
		my ( $flux, $err ) = $rxn->RateLaw->toMatlabString(
			$rxn->Reactants,    $rxn->StatFactor,
			$rxn->Compartments, $rxn->RxnRule
		);
		$err && return ($err);
		push @fluxes, $flux;
		++$irxn;
	}

	print Mfile "% Reaction flux function\n";
	print Mfile "function f= flux(t,x)\n";
	my $i = 1;
	for my $flux (@fluxes) {
		printf Mfile "    f(%d)=%s;\n", $i, $flux;
		++$i;
	}
	print Mfile "end\n\n";

	print Mfile "% Derivative function\n";
	print Mfile "function d= xdot(t,x)\n";
	print Mfile "    f=flux(t,x);\n";
	for my $ispec ( sort { $a <=> $b } keys %S ) {
		printf Mfile "    d(%d,1)= ", $ispec;
		if ( $fixed{$ispec} ) {
			printf Mfile "0";
		}
		else {
			for my $irxn ( sort { $a <=> $b } keys %{ $S{$ispec} } ) {
				my $s = $S{$ispec}{$irxn};
				if ( $s == 1 ) {
					$mod = "+";
				}
				elsif ( $s == -1 ) {
					$mod = "-";
				}
				elsif ( $s > 0 ) {
					$mod = "+$s*";
				}
				else {
					$mod = "+($s)*";
				}
				$s && printf Mfile " %sf(%d)", $mod, $irxn;
			}
		}
		printf Mfile ";\n";
	}
	print Mfile "end\n\n";

	#print Mfile "    d=d';\n";

	my @onames = ();
	my @snames = ();
	if ( $#{ $model->Observables } >= 0 ) {
		print Mfile "% Observables\n";
		printf Mfile "function o= obs(x)\n";
		print Mfile "  w= [\n";
		for my $obs ( @{ $model->Observables } ) {
			push @onames, $obs->Name;
			print Mfile "      ", join( ' ', $obs->getWeightVector ), ";\n";
		}
		print Mfile "     ];\n";
		print Mfile "  o= x*w';\n";
		print Mfile "end\n\n";
		for my $oname (@onames) {
			$oname =~ s/_/\\_/g;
			$oname = '\'' . $oname . '\'';
		}
		print Mfile "onames={", join( ',', @onames ), "};\n";
	}
	else {
		for my $spec ( @{ $model->SpeciesList->Array } ) {
			push @snames, $spec->SpeciesGraph->toString();
		}
		print Mfile "snames={", join( ',', @snames ), "};\n";
	}

	print Mfile
	  "\n% Integrate ODEs\n[t_out,x_out]= ode15s(\@xdot, [0 tend], x0);\n";
	if ( $#onames >= 0 ) {
		print Mfile << "EOF";
% compute observables
obs_out= obs(x_out);
% plot observables
plot(t_out,obs_out);
legend(onames);
EOF

	}
	else {
		print Mfile << "EOF";
% plot species concentrations
plot(t_out,x_out);
legend(snames);
EOF

	}
	print Mfile "end\n";
	close(Mfile);
	print "Wrote M-file $file.\n";
	return ();
}


##
##


# TODO: implement local functions!!
sub writeMexfile
{
	my $model = shift;
	my $params = (@_) ? shift(@_) : '';

	return ('') if $NO_EXEC;

	if ( !$model->RxnList )
	{   return ("No reactions in current model.");   }

	my $plist = $model->ParamList;
	
	my $model_name = $model->Name;

	# Strip prefixed path
	$model_name =~ s/^.*\///;
	my $prefix =
	  ( defined( $params->{prefix} ) ) ? $params->{prefix} : $model->Name;
	my $suffix = ( defined( $params->{suffix} ) ) ? $params->{suffix} : "";
	if ( $suffix ne "" )
	{   $prefix .= "_${suffix}";   }
	
	my $mexfile = "${prefix}_mex.c";
	my $mscript = "${prefix}_script.m";


    # configure options
    my $cvode_abstol = 1e-8;
    if ( exists $params->{'atol'} )
    {   $cvode_abstol = $params->{'atol'};  }
    
    my $cvode_reltol = 1e-8;
    if ( exists $params->{'rtol'} )
    {   $cvode_reltol = $params->{'rtol'};  }    

    my $cvode_max_steps = 500;
    if ( exists $params->{'max_steps'} )
    {   $cvode_max_steps = $params->{'max_steps'};  }  

    my $cvode_max_err_test_fails = 7;
    if ( exists $params->{'max_err_test_fails'} )
    {   $cvode_max_err_test_fails = $params->{'max_err_test_fails'};  }  

    my $cvode_max_conv_fails = 10;
    if ( exists $params->{'max_max_conv_fails'} )
    {   $cvode_max_conv_fails = $params->{'max_conv_fails'};  }  

    # Non-stiff = CV_ADAMS,CV_FUNCTIONAL;  Stiff = CV_BDF,CV_NEWTON
    my $cvode_linear_multistep = 'CV_BDF';
    if ( exists $params->{'linear_multistep'} )
    {   $cvode_linear_multistep = $params->{'linear_multistep'};  }     

    my $cvode_nonlinear_solver = 'CV_NEWTON';
    if ( exists $params->{'nonlinear_solver'} )
    {   $cvode_nonlinear_solver = $params->{'nonlinear_solver'};  }  

    # time options for mscript
    my $t_start = 0;
    if ( exists $params->{'t_start'} )
    {   $t_start = $params->{'t_start'};  }  

    my $t_end = 10;
    if ( exists $params->{'t_end'} )
    {   $t_end = $params->{'t_end'};  } 

    my $n_steps = 10;
    if ( exists $params->{'n_steps'} )
    {   $n_steps = $params->{'n_steps'};  } 


    # construct expressions
    my $Nparameters = 0;
    my $Nexpressions = 0;
    my %parameter_index = ();   
    my %expression_index = ();
	my $calc_expressions_string = '';
	my @mscript_params = ();
	my @mscript_param_names = ();	
	foreach my $param ( @{ $model->ParamList->Array } )
	{
		if ( $param->Type eq 'Constant' )
		{
		    my $param_name = $param->Name;
		    my $param_ref = "parameters[$Nparameters]";
		    my $param_idx = $Nparameters;
		    my $expr_idx  = $Nexpressions;
		    $parameter_index{$param_name} = $param_idx;		    
		    $expression_index{$param_name} = $expr_idx;		    
		    ++$Nparameters;  ++$Nexpressions;
  		    $calc_expressions_string .= "    NV_Ith_S(expressions,$expr_idx) = parameters[$param_idx];\n";
  		    
  		    push @mscript_params, $param->toString($plist);

            my $mscript_param_name = $param_name;
            $mscript_param_name =~ s/\_/\\\_/g;
  		    push @mscript_param_names, "'" . $mscript_param_name . "'";    
  		}
  		elsif ( $param->Type eq 'ConstantExpression' )
  		{    
		    my $expr_idx = $Nexpressions;
		    $expression_index{$param->Name} = $expr_idx;		    
  		    ++$Nexpressions;
  		    
  		    my $expr = $param->Expr->toMexString($plist);
  		    foreach my $expr_name ( keys %expression_index )
  		    {
  		        my $idx = $expression_index{$expr_name};
  		        $expr =~ s/(^|\W)$expr_name(\W|$)/$1NV_Ith_S\(expressions\,$idx\)$2/g;
  		    }
  		    
  		    $calc_expressions_string .= "    NV_Ith_S(expressions,$expr_idx) = $expr;\n";
  		}
	}
	my $mscript_params = join(', ', @mscript_params );
	my $mscript_param_names = join( ', ', @mscript_param_names );
	
    # construct observables
    my $Nobservables = 0;
    my %observable_index = ();
	my $calc_observables_string = '';
	my @mscript_observable_names = ();     
	foreach my $obsv ( @{ $model->Observables } )
	{
	    my $obsv_name = $obsv->Name;
	    my $obsv_idx = $Nobservables;
	    
	    my $mscript_obsv_name = $obsv_name;
	    $mscript_obsv_name =~ s/\_/\\\_/g;
	    push @mscript_observable_names, "'" . $mscript_obsv_name . "'";

		$observable_index{$obsv_name} = $obsv_idx;
		++$Nobservables;
  		$calc_observables_string .= "    NV_Ith_S(observables,$obsv_idx) = " . $obsv->toMexString() . ";\n";			    
	}
	my $mscript_observable_names = join( ', ', @mscript_observable_names );

    # construct functions
    my $user_fcn_declarations = '';
    my $user_fcn_definitions = '';
	foreach my $param ( @{ $model->ParamList->Array } )
	{
		if ( $param->Type eq 'Function' )
		{
		    my $fcn      = $param->Ref;	
		    my $fcn_name = $param->Name;

		    my @args = ();
		    foreach my $arg ( @{$fcn->Args} )
		    {   push @args, "double $arg";  }
		    
		    push @args, 'N_Vector expressions';
		    push @args, 'N_Vector observables';		    
		     
		    my $fcn_decl = "double " . $fcn->toMexString( $plist );
		    my $fcn_expr = $fcn->Expr->toMexString($plist);      

  		    foreach my $expr_name ( keys %expression_index )
  		    {
  		        my $idx = $expression_index{$expr_name};
  		        $fcn_expr =~ s/(^|\W)$expr_name(\W|$)/$1NV_Ith_S\(expressions\,$idx\)$2/g;
  		    }

	        foreach my $obsv_name ( keys %observable_index )
	        {
  		        my $idx = $observable_index{$obsv_name};	        
	            $fcn_expr =~
	                s/(^|\W)$obsv_name(|\(\))(\W|$)/$1NV_Ith_S\(observables\,$idx\)$3/g;
	        }     
		    
		    
		    $user_fcn_declarations .= "$fcn_decl;\n";
		    $user_fcn_definitions  .= "\n/* user function $fcn_name */\n$fcn_decl\n{\n    return $fcn_expr;\n}\n\n";
        }
	}

    # construct reaction ratelaws
	my $Nreactions = 0;
    my $calc_ratelaws_string = '';
    foreach my $rxn ( @{$model->RxnList->Array} )
    {
        my $ratelaw_string = $rxn->toMexString($plist);

  		foreach my $expr_name ( keys %expression_index )
  		{
  		    my $idx = $expression_index{$expr_name};
  		    $ratelaw_string =~ s/(^|\W)$expr_name(\W|$)/$1NV_Ith_S\(expressions\,$idx\)$2/g;
  		}      

	    foreach my $obsv_name ( keys %observable_index )
	    {
  		    my $idx = $observable_index{$obsv_name};	        
	        $ratelaw_string =~
	            s/(^|\W)$obsv_name(|\(\))(\W|$)/$1NV_Ith_S\(observables\,$idx\)$3/g;
	    }  
        
        $calc_ratelaws_string .= "    NV_Ith_S(ratelaws,$Nreactions) = $ratelaw_string;\n";
        ++$Nreactions;
    }


    # construct stoichiometry matrix
	my %S = ();
	my @fluxes = ();
	my $i_rxn = 1;
	for my $rxn ( @{ $model->RxnList->Array } )
	{
		# Each reactant contributes a -1
		foreach my $reactant ( @{ $rxn->Reactants } )
		{   --$S{ $reactant->Index }{$i_rxn};   }

		# Each product contributes a +1
		foreach my $product ( @{ $rxn->Products } )
		{   ++$S{ $product->Index }{$i_rxn};   }
		
		++$i_rxn;
	}

	# construct species equations
	my $Nspecies = 0;
    my $calc_derivs_string = '';
    my $mscript_species_init = '';    
    my @mscript_species_names = ();    
    foreach my $species ( @{ $model->SpeciesList->Array } )
    {
        (my $param) = $plist->lookup( $species->Concentration );
        
        my $midx = $Nspecies + 1;
        if ($param)
        {
            $mscript_species_init .= "    species_init($midx) = " . $param->toMexString($plist) . ";\n";          
        }
        else
        {
            $mscript_species_init .= "    species_init($midx) = " . $species->Concentration . ";\n";
        }       
        push @mscript_species_names, "'" . $species->SpeciesGraph->StringExact . "'";
    
        my $species_vector = $S{ $species->Index };
        my $species_string = '';
        
        foreach my $i_rxn ( keys %$species_vector )
        {
            my $i_rxn0 = $i_rxn - 1;
            my $stoich = $species_vector->{$i_rxn};
            
            if ( $stoich == 1 )
            {   $species_string .= " +NV_Ith_S(ratelaws,$i_rxn0)";   }
            elsif ( $stoich > 0 )
            {   $species_string .= " +$stoich.0*NV_Ith_S(ratelaws,$i_rxn0)";   }
            elsif ( $stoich < 0 )
            {   $species_string .= " $stoich.0*NV_Ith_S(ratelaws,$i_rxn0)";    } 
        }   
        
        # trim leading +
        $species_string =~ s/^ \+?//;
        
        # fix empty string
        if ($species_string eq '')
        {   $species_string = '0.0';   }
        
        $calc_derivs_string .= "    NV_Ith_S(Dspecies,$Nspecies) = $species_string;\n"; 
        ++$Nspecies;
	}

    my $mscript_species_names = join( ', ', @mscript_species_names);

    
    # expand expressions in species init
  	foreach my $expr_name ( keys %expression_index )
  	{
  	    next if ( exists $parameter_index{$expr_name} );
        (my $expr_ref) = $plist->lookup( $expr_name );
        my $expr = $expr_ref->toMexString($plist);
  	    $mscript_species_init =~ s/(^|\W)$expr_name(\W|$)/$1$expr$2/g;
  	}       
  	# replace parameter names with matlab indices
  	foreach my $param_name ( keys %parameter_index )
  	{
  	    my $midx = $parameter_index{$param_name} + 1;
  	    $mscript_species_init =~ s/(^|\W)$param_name(\W|$)/$1params\($midx\)$2/g;
  	}                       


    # open Mexfile and begin printing...
	open( Mexfile, ">$mexfile" ) || die "Couldn't open $mexfile: $!\n";
    print Mexfile <<"EOF";
/*   
**   $mexfile
**	 
**   Cvode-Mex implementation of BioNetGen model $prefix.
**
**   Code Adapted from templates provided by Mathworks and Sundials.
**   QUESTIONS?  Email justinshogg\@gmail.com
**
**   Requires the CVODE libraries:  sundials_cvode and sundials_nvecserial.
**   https://computation.llnl.gov/casc/sundials/main.html
**
**-----------------------------------------------------------------------------
**
**   COMPILE:
**   mex -L<path_to_cvode_libraries> -I<path_to_cvode_includes>  ...
**          -lsundials_nvecserial -lsundials_cvode -lm network_mex.c
**
**   note1: if cvode is in your library path, you can omit path specifications.
**
**   note2: if linker complains about lib stdc++, try removing "-lstdc++"
**     from the mex configuration file "gccopts.sh".  This should be in the
**     matlab bin folder.
** 
**-----------------------------------------------------------------------------
**
**   EXECUTE in MATLAB:
**   [error_status, species_out, observables_out]
**        = $prefix( parameters, species_init, timepoints )
**
**   parameters      : column vector of model parameters with Dim(PAR) elements.
**   species_init    : row vector of initial species populations with Dim(species) elements.
**   timepoints      : row vector of time points (including the initial
**                        time) where the trajectory is evaluated. (minimum 2 elements.)
**
**   error_status    : 0 if the integrator exits without error, >0 otherwise.
**   species_out     : [Dim(T)xDim(species)] array of the integrated species population trajectories
**                        (columns correspond to states, rows correspond to time).
**   observables_out : [Dim(T)xDim(obsrv)] array of the integrated observable trajectories
**                        (columns correspond to states, rows correspond to time).
*/

/* Library headers */
#include "mex.h"
#include "matrix.h"
#include <stdlib.h>
#include <math.h>
#include <cvode/cvode.h>             /* prototypes for CVODE  */
#include <nvector/nvector_serial.h>  /* serial N_Vector       */
#include <cvode/cvode_dense.h>       /* prototype for CVDense */

/* Problem Dimensions */
#define __N_PARAMETERS__   $Nparameters
#define __N_EXPRESSIONS__  $Nexpressions
#define __N_OBSERVABLES__  $Nobservables
#define __N_RATELAWS__     $Nreactions
#define __N_SPECIES__      $Nspecies

/* scalar relative tolerance */
#define __RTOL__  $cvode_reltol
/* scalar absolute tolerance */
#define __ATOL__  $cvode_abstol

/* other CVODE configuration parameters */
#define __CVODE_MAX_STEPS__            $cvode_max_steps
#define __CVODE_MAX_ERR_TEST_FAILS__   $cvode_max_err_test_fails
#define __CVODE_MAX_CONV_FAILS__       $cvode_max_conv_fails
#define __CVODE_LINEAR_MULTISTEP__     $cvode_linear_multistep
#define __CVODE_NONLINEAR_SOLVER__     $cvode_nonlinear_solver

/* core function declarations */
void  mexFunction ( int nlhs, mxArray *plhs[], int nrhs, const mxArray *prhs[] );
int   check_flag  ( void *flagvalue, char *funcname, int opt );
void  calc_expressions ( N_Vector expressions, double * parameters );
void  calc_observables ( N_Vector observables, N_Vector species );
void  calc_ratelaws    ( N_Vector ratelaws,  N_Vector species, N_Vector expressions, N_Vector observables );
int   calc_species_deriv ( realtype time, N_Vector species, N_Vector Dspecies, void * f_data );

/* USER-DEFINED function declarations */
$user_fcn_declarations

/* USER-DEFINED function definitions  */
$user_fcn_definitions

/* Calculate expressions */
void
calc_expressions ( N_Vector expressions, double * parameters )
{
$calc_expressions_string   
}

/* Calculate observables */
void
calc_observables ( N_Vector observables, N_Vector species )
{
$calc_observables_string
}

/* Calculate ratelaws */
void
calc_ratelaws ( N_Vector ratelaws, N_Vector species, N_Vector expressions, N_Vector observables )
{  
$calc_ratelaws_string
}


/* Calculate species derivates */
int
calc_species_deriv ( realtype time, N_Vector species, N_Vector Dspecies, void * f_data )
{
    int         return_val;
    N_Vector *  temp_data;
    
    N_Vector    expressions;
    N_Vector    observables;
    N_Vector    ratelaws;

    /* cast temp_data */
    temp_data = (N_Vector*)f_data;
     
    /* get ratelaws Vector */
    expressions = temp_data[0];
    observables = temp_data[1];
    ratelaws    = temp_data[2];
       
    /* calculate observables */
    calc_observables( observables, species );
    
    /* calculate ratelaws */
    calc_ratelaws( ratelaws, species, expressions, observables );
                        
    /* calculate derivates */
$calc_derivs_string

    return(0);
}


/*
**   ========
**   main MEX
**   ========
*/
void mexFunction( int nlhs, mxArray * plhs[], int nrhs, const mxArray * prhs[] )
{
    /* variables */
    double *  return_status;
    double *  species_out;
    double *  observables_out;
    double *  parameters;
    double *  species_init;
    double *  timepoints; 
    size_t    n_timepoints;
    size_t    i;
    size_t    j;

    /* intermediate data vectors */
    N_Vector  expressions;
    N_Vector  observables;
    N_Vector  ratelaws;

    /* array to hold pointers to data vectors */
    N_Vector  temp_data[3];
    
    /* CVODE specific variables */
    realtype  reltol;
    realtype  abstol;
    realtype  time;
    N_Vector  species;
    void *    cvode_mem;
    int       flag;

    /* check number of input/output arguments */
    if (nlhs != 3)
    {  mexErrMsgTxt("invalid output arguments! syntax: [return_status, species_out, observables_out] = network_mex( parameters, species_init, timepoints )");  }
    if (nrhs != 3)
    {  mexErrMsgTxt("invalid input arguments!  syntax: [return_status, species_out, observables_out] = network_mex( parameters, species_init, timepoints )");  }

    /* make sure params has correct dimensions */
    if ( (mxGetM(prhs[0]) != __N_PARAMETERS__)  ||  (mxGetN(prhs[0]) != 1) )
    {  mexErrMsgTxt("CONSTANTS must be a column vector with __N_PARAMETERS__ elements.");  }

    /* make sure species_init has correct dimensions */
    if ( (mxGetM(prhs[1]) != 1)  ||  (mxGetN(prhs[1]) != __N_SPECIES__) )
    {  mexErrMsgTxt("SPECIES_INIT must be a row vector with __N_SPECIES__ elements.");  } 

    /* make sure timepoints has correct dimensions */
    if ( (mxGetM(prhs[2]) != 1)  ||  (mxGetN(prhs[2]) < 2) )
    {  mexErrMsgTxt("TIMEPOINTS must be a row vector with 2 or more elements.");  }


    /* get pointers to input arrays */
    parameters   = mxGetPr(prhs[0]);
    species_init = mxGetPr(prhs[1]);
    timepoints   = mxGetPr(prhs[2]);

    /* get number of timepoints */
    n_timepoints = mxGetN(prhs[2]);

    /* Create an mxArray for output trajectories */
    plhs[0] = mxCreateDoubleMatrix(1, 1, mxREAL );
    plhs[1] = mxCreateDoubleMatrix(n_timepoints, __N_SPECIES__, mxREAL);
    plhs[2] = mxCreateDoubleMatrix(n_timepoints, __N_OBSERVABLES__, mxREAL);

    /* get pointers to output arrays */
    return_status   = mxGetPr(plhs[0]);
    species_out     = mxGetPr(plhs[1]);
    observables_out = mxGetPr(plhs[2]);    

   
    /* initialize intermediate data vectors */
    expressions  = NULL;
    expressions = N_VNew_Serial(__N_EXPRESSIONS__);
    if (check_flag((void *)expressions, "N_VNew_Serial", 0))
    {
        return_status[0] = 1;
        return;
    }

    observables = NULL;
    observables = N_VNew_Serial(__N_OBSERVABLES__);
    if (check_flag((void *)observables, "N_VNew_Serial", 0))
    {
        N_VDestroy_Serial(expressions);
        return_status[0] = 1;
        return;
    }

    ratelaws    = NULL; 
    ratelaws = N_VNew_Serial(__N_RATELAWS__);
    if (check_flag((void *)ratelaws, "N_VNew_Serial", 0))
    {   
        N_VDestroy_Serial(expressions);
        N_VDestroy_Serial(observables);
        return_status[0] = 1;
        return;
    }
    
    /* set up pointers to intermediate data vectors */
    temp_data[0] = expressions;
    temp_data[1] = observables;
    temp_data[2] = ratelaws;

    /* calculate expressions */
    calc_expressions( expressions, parameters );

        
    /* SOLVE model equations! */
    species   = NULL;
    cvode_mem = NULL;

    /* Set the scalar relative tolerance */
    reltol = __RTOL__;
    abstol = __ATOL__;

    /* Create serial vector for Species */
    species = N_VNew_Serial(__N_SPECIES__);
    if (check_flag((void *)species, "N_VNew_Serial", 0))
    {  
        N_VDestroy_Serial(expressions);
        N_VDestroy_Serial(observables);
        N_VDestroy_Serial(ratelaws);
        return_status[0] = 1;
        return;
    }
    for ( i = 0; i < __N_SPECIES__; i++ )
    {   NV_Ith_S(species,i) = species_init[i];   }
    
    /* write initial species populations into species_out */
    for ( i = 0; i < __N_SPECIES__; i++ )
    {   species_out[i*n_timepoints] = species_init[i];   }
    
    /* write initial observables populations into species_out */ 
    calc_observables( observables, species );  
    for ( i = 0; i < __N_OBSERVABLES__; i++ )
    {   observables_out[i*n_timepoints] = NV_Ith_S(observables,i);   }


    /*   Call CVodeCreate to create the solver memory:    
     *   CV_ADAMS or CV_BDF is the linear multistep method
     *   CV_FUNCTIONAL or CV_NEWTON is the nonlinear solver iteration
     *   A pointer to the integrator problem memory is returned and stored in cvode_mem.
     */
    cvode_mem = CVodeCreate(__CVODE_LINEAR_MULTISTEP__, __CVODE_NONLINEAR_SOLVER__);
    if (check_flag((void *)cvode_mem, "CVodeCreate", 0))
    {
        N_VDestroy_Serial(expressions);
        N_VDestroy_Serial(observables);
        N_VDestroy_Serial(ratelaws);
        N_VDestroy_Serial(species);
        return_status[0] = 1;
        return;
    }
  

    /*   Call CVodeInit to initialize the integrator memory:     
     *   cvode_mem is the pointer to the integrator memory returned by CVodeCreate
     *   rhs_func  is the user's right hand side function in y'=f(t,y)
     *   T0        is the initial time
     *   y         is the initial dependent variable vector
     */
    flag = CVodeInit(cvode_mem, calc_species_deriv, timepoints[0], species);
    if (check_flag(&flag, "CVodeInit", 1))
    {
        N_VDestroy_Serial(expressions);
        N_VDestroy_Serial(observables);
        N_VDestroy_Serial(ratelaws);
        N_VDestroy_Serial(species);
        CVodeFree(&cvode_mem);
        return_status[0] = 1;
        return;
    }
   
    /* Set scalar relative and absolute tolerances */
    flag = CVodeSStolerances(cvode_mem, reltol, abstol);
    if (check_flag(&flag, "CVodeSStolerances", 1))
    {
        N_VDestroy_Serial(expressions);
        N_VDestroy_Serial(observables);
        N_VDestroy_Serial(ratelaws);
        N_VDestroy_Serial(species);
        CVodeFree(&cvode_mem);
        return_status[0] = 1;
        return;
    }   
   
    /* pass params to rhs_func */
    flag = CVodeSetUserData(cvode_mem, &temp_data);
    if (check_flag(&flag, "CVodeSetFdata", 1))
    {
        N_VDestroy_Serial(expressions);
        N_VDestroy_Serial(observables);
        N_VDestroy_Serial(ratelaws);
        N_VDestroy_Serial(species);
        CVodeFree(&cvode_mem);
        return_status[0] = 1;
        return;
    }

    /* Call CVDense to specify the CVDENSE dense linear solver */
    flag = CVDense(cvode_mem, __N_SPECIES__);
    if (check_flag(&flag, "CVDense", 1))
    {
        N_VDestroy_Serial(expressions);
        N_VDestroy_Serial(observables);
        N_VDestroy_Serial(ratelaws);
        N_VDestroy_Serial(species);
        CVodeFree(&cvode_mem);
        return_status[0] = 1;
        return;
    }

    flag = CVodeSetMaxNumSteps(cvode_mem, __CVODE_MAX_STEPS__);
    if (check_flag(&flag, "CVodeSetMaxNumSteps", 1))
    {
        N_VDestroy_Serial(expressions);
        N_VDestroy_Serial(observables);
        N_VDestroy_Serial(ratelaws);
        N_VDestroy_Serial(species);
        CVodeFree(&cvode_mem);
        return_status[0] = 1;
        return;
    }

    flag = CVodeSetMaxErrTestFails(cvode_mem, __CVODE_MAX_ERR_TEST_FAILS__);
    if (check_flag(&flag, "CVodeSetMaxErrTestFails", 1))
    {
        N_VDestroy_Serial(expressions);
        N_VDestroy_Serial(observables);
        N_VDestroy_Serial(ratelaws);
        N_VDestroy_Serial(species);
        CVodeFree(&cvode_mem);
        return_status[0] = 1;
        return;
    }

    flag = CVodeSetMaxConvFails(cvode_mem, __CVODE_MAX_CONV_FAILS__);
    if (check_flag(&flag, "CVodeSetMaxConvFails", 1))
    {
        N_VDestroy_Serial(expressions);
        N_VDestroy_Serial(observables);
        N_VDestroy_Serial(ratelaws);
        N_VDestroy_Serial(species);
        CVodeFree(&cvode_mem);
        return_status[0] = 1;
        return;
    }

    /* integrate to each timepoint */
    for ( i=1;  i < n_timepoints;  i++ )
    {
        flag = CVode(cvode_mem, timepoints[i], species, &time, CV_NORMAL);
        if (check_flag(&flag, "CVode", 1))
        {
            N_VDestroy_Serial(expressions);
            N_VDestroy_Serial(observables);
            N_VDestroy_Serial(ratelaws);
            N_VDestroy_Serial(species);
            CVodeFree(&cvode_mem);
            return_status[0] = 1; 
            return;
        }

        /* copy species output from nvector to matlab array */
        for ( j = 0; j < __N_SPECIES__; j++ )
        {   species_out[j*n_timepoints + i] = NV_Ith_S(species,j);   }
        
        /* copy observables output from nvector to matlab array */
        calc_observables( observables, species );         
        for ( j = 0; j < __N_OBSERVABLES__; j++ )
        {   observables_out[j*n_timepoints + i] = NV_Ith_S(observables,j);   }        
    }
 
    /* Free vectors */
    N_VDestroy_Serial(expressions);
    N_VDestroy_Serial(observables);
    N_VDestroy_Serial(ratelaws);        
    N_VDestroy_Serial(species);

    /* Free integrator memory */
    CVodeFree(&cvode_mem);

    return;
}


/*  Check function return value...
 *   opt == 0 means SUNDIALS function allocates memory so check if
 *            returned NULL pointer
 *   opt == 1 means SUNDIALS function returns a flag so check if
 *            flag >= 0
 *   opt == 2 means function allocates memory so check if returned
 *            NULL pointer 
 */
int check_flag(void *flagvalue, char *funcname, int opt)
{
    int *errflag;

    /* Check if SUNDIALS function returned NULL pointer - no memory allocated */
    if (opt == 0 && flagvalue == NULL)
    {
        fprintf(stderr, "\\nSUNDIALS_ERROR: %s() failed - returned NULL pointer\\n\\n", funcname);
        return(1);
    }

    /* Check if flag < 0 */
    else if (opt == 1)
    {
        errflag = (int *) flagvalue;
        if (*errflag < 0)
        {
            fprintf(stderr, "\\nSUNDIALS_ERROR: %s() failed with flag = %d\\n\\n", funcname, *errflag);
            return(1);
        }
    }

    /* Check if function returned NULL pointer - no memory allocated */
    else if (opt == 2 && flagvalue == NULL)
    {
        fprintf(stderr, "\\nMEMORY_ERROR: %s() failed - returned NULL pointer\\n\\n", funcname);
        return(1);
    }

    return(0);
}
EOF
	close(Mexfile);

    # open Mexfile and begin printing...
	open( Mscript, ">$mscript" ) || die "Couldn't open $mscript: $!\n";
    print Mscript <<"EOF";
function [return_status, species_out, observables_out ] = ${prefix}_script( params, species_init, timepoints )
%${prefix}_script
% run simulation of ${prefix} model

if ( isempty(params) )
   params = [ $mscript_params ]';
end

if ( isempty(species_init) )
   species_init = initialize_species( params );
end

if ( isempty(timepoints) )
   timepoints = linspace($t_start,$t_end,$n_steps+1);
end

% define parameter labels
param_labels = { $mscript_param_names };

% define species labels
species_labels = { $mscript_species_names };

% define observables labels
observable_labels = { $mscript_observable_names };

% run simulation
[return_status, species_out, observables_out] = ${prefix}_mex( params, species_init, timepoints );

% plot observables
plot(timepoints,observables_out);
title('${prefix}');
axis([$t_start $t_end 0 inf]);
legend(observable_labels);


% initialize species function
function [species_init] = initialize_species( params )
$mscript_species_init
end



end
EOF
	close(Mscript);
	print "Wrote Mexfile $mexfile and M-file script $mscript.\n";
	return ();
}


##
##


sub quit
{
    exit;
}

##
##


sub writeLatex {
	my $model = shift;
	my $params = (@_) ? shift(@_) : "";

	return ("") if $NO_EXEC;

	if ( !$model->RxnList ) {
		return ("No reactions in current model.");
	}

	my $plist = $model->ParamList;

	my $model_name = $model->Name;

	# Strip prefixed path
	$model_name =~ s/^.*\///;
	my $prefix =
	  ( defined( $params->{prefix} ) ) ? $params->{prefix} : $model->Name;
	my $suffix = ( defined( $params->{suffix} ) ) ? $params->{suffix} : "";
	if ( $suffix ne "" ) {
		$prefix .= "_${suffix}";
	}

	my $file = "${prefix}.tex";

	open( Lfile, ">$file" ) || die "Couldn't open $file: $!\n";
	my $version = BNGversion();
	print Lfile
"% Latex formatted differential equations for model $prefix created by BioNetGen $version\n";

	# Document Header
	print Lfile <<'EOF';
\documentclass{article}
\begin{document}
EOF

	# Dimensions
	my $Nspecies   = scalar( @{ $model->SpeciesList->Array } );
	my $Nreactions = scalar( @{ $model->RxnList->Array } );
	print Lfile "\\section{Model Summary}\n";
	printf Lfile "The model has %d species and %d reactions.\n", $Nspecies,
	  $Nreactions;
	print Lfile "\n";

	# Stoichiometry matrix
	#my @St=();
	my %S      = ();
	my @fluxes = ();
	my $irxn   = 1;
	for my $rxn ( @{ $model->RxnList->Array } ) {

		# Each reactant contributes a -1
		for my $r ( @{ $rxn->Reactants } ) {
			--$S{ $r->Index }{$irxn};
		}

		# Each product contributes a +1
		for my $p ( @{ $rxn->Products } ) {
			++$S{ $p->Index }{$irxn};
		}
		my ( $flux, $err ) =
		  $rxn->RateLaw->toLatexString( $rxn->Reactants, $rxn->StatFactor,
			$model->ParamList );
		$err && return ($err);
		push @fluxes, $flux;
		++$irxn;
	}

	print Lfile "\\section{Differential Equations}\n";
	print Lfile "\\begin{eqnarray*}\n";
	for my $ispec ( sort { $a <=> $b } keys %S ) {

		#    print Lfile "\\begin{eqnarray*}\n";
		printf Lfile "\\dot{x_{%d}}&=& ", $ispec;
		my $nrxn = 1;
		for my $irxn ( sort { $a <=> $b } keys %{ $S{$ispec} } ) {
			my $s = $S{$ispec}{$irxn};
			if ( $s == 1 ) {
				$mod = "+";
			}
			elsif ( $s == -1 ) {
				$mod = "-";
			}
			elsif ( $s > 0 ) {
				$mod = "+$s";
			}
			else {
				$mod = "+($s)";
			}
			if ( ( $nrxn % 5 ) == 0 ) { print Lfile "\\\\ &&"; }
			if ($s) {
				printf Lfile " %s %s", $mod, $fluxes[ $irxn - 1 ];
				++$nrxn;
			}
		}

		#    print Lfile "\n\\end{eqnarray*}\n";
		if ( $nrxn == 1 ) {
			print Lfile "0";
		}
		print Lfile "\n\\\\\n";
	}
	print Lfile "\\end{eqnarray*}\n";
	print Lfile "\n";

	# Document Footer
	print Lfile <<'EOF';
\end{document}
EOF
	close(Lfile);
	print "Wrote Latex equations to  $file.\n";
	return ();
}

sub toSBMLfile {
	my $model = shift;
	my $params = (@_) ? shift(@_) : "";

	return ("") if $NO_EXEC;

	send_warning("Use writeSBML instead of toSBMLfile");
	return ( $model->writeSBML($params) );
}

sub writeSSC {

	my $model = shift;
	my $params = (@_) ? shift(@_) : "";
	return ("") if $NO_EXEC;

	my $model_name = $model->Name;

	# Strip prefixed path
	$model_name =~ s/^.*\///;
	my $prefix =
	  ( defined( $params->{prefix} ) ) ? $params->{prefix} : $model->Name;
	my $suffix = ( defined( $params->{suffix} ) ) ? $params->{suffix} : "";
	if ( $suffix ne "" ) {
		$prefix .= "_${suffix}";
	}
	my $file = "${prefix}.rxn";
	open( SSCfile, ">$file" ) || die "Couldn't open $file: $!\n";
	my $version = BNGversion();
	print SSCfile
	  "--# SSC-file for model $prefix created by BioNetGen $version\n";
	print "Writing SSC translator .rxn file.....";

	#-- Compartment default for SSC ---- look more into it
	printf SSCfile
	  "region World \n  box width 1 height 1 depth 1\nsubvolume edge 1";

	# --This part correspond to seed specie
	print SSCfile "\n\n";
	print SSCfile "--# Initial molecules and their concentrations\n";
	$sp_string =
	  $model->SpeciesList->writeSSC( $model->Concentrations,
		$model->ParamList );
	print SSCfile $sp_string;

	# --This part in SSC corrsponds to Observables
	if ( @{ $model->Observables } ) {
		print SSCfile"\n\n--# reads observables";
		print SSCfile "\n";
		for my $obs ( @{ $model->Observables } ) {
			$ob_string = $obs->toStringSSC();
			if ( $ob_string =~ /\?/ ) {
				print STDOUT
" \n WARNING: SSC does not implement ?. The observable has been commented. Please see .rxn file for more details \n";
				print STDOUT "\n See Observable\n", $obs->toString();
				$ob_string = "\n" . "--#" . "record " . $ob_string;
				print SSCfile $ob_string;
			}    #putting this string as a comment and carrying on
			else {
				print SSCfile "\nrecord ", $ob_string;
			}
		}
	}

	# --Reaction rules
	print SSCfile" \n\n--# reaction rules\n";
	for my $rset ( @{ $model->RxnRules } ) {
		my $id = 0;
		my $rreverse = ( $#$rset > 0 ) ? $rset->[1] : "";
		( my $reac1, my $errorSSC ) = $rset->[0]->toStringSSC($rreverse);
		if ( $errorSSC == 1 ) {
			print STDOUT "\nSee rule in .rxn \n",
			  $rset->[0]->toString($rreverse);
			$reac1 = "--#" . $reac1;
		}
		print SSCfile $reac1;
		print SSCfile "\n";
		if ($rreverse) {
			( my $reac2, my $errorSSC ) = $rset->[1]->toStringSSC($rreverse);
			if ( $errorSSC == 1 ) { $reac2 = "--#" . $reac2; }
			print SSCfile $reac2;
			print SSCfile "\n";
		}
	}
	print "\nWritten SSC file\n";
	return ();
}

# This subroutine writes a file which contains the information corresponding to the parameter block in BNG
sub writeSSCcfg {
	my $model = shift;
	my $params = (@_) ? shift(@_) : "";
	return ("") if $NO_EXEC;

	my $model_name = $model->Name;

	# Strip prefixed path
	$model_name =~ s/^.*\///;
	my $prefix =
	  ( defined( $params->{prefix} ) ) ? $params->{prefix} : $model->Name;
	my $suffix = ( defined( $params->{suffix} ) ) ? $params->{suffix} : "";
	if ( $suffix ne "" ) {
		$prefix .= "_${suffix}";
	}
	my $file    = "${prefix}.cfg";
	my $version = BNGversion();
	open( SSCcfgfile, ">$file" ) || die "Couldn't open $file: $!\n";
	print STDOUT "\n Writting SSC cfg file \n";
	print SSCcfgfile
	  "# SSC cfg file for model $prefix created by BioNetGen $version\n";
	print SSCcfgfile $model->ParamList->writeSSCcfg( $vars{NETfile} );
	return ();
}

# Write BNG model specification in BNG XML format
sub writeBNGXML {
	return ( writeXML(@_) );
}

sub writeXML {
	my $model = shift;
	my $params = (@_) ? shift(@_) : "";

	return ("") if $NO_EXEC;

	my $model_name = $model->Name;

	# Strip prefixed path
	$model_name =~ s/^.*\///;
	my $prefix =
	  ( defined( $params->{prefix} ) ) ? $params->{prefix} : $model->Name;
	my $suffix = ( defined( $params->{suffix} ) ) ? $params->{suffix} : "";
	my $EvaluateExpressions =
	  ( defined( $params->{EvaluateExpressions} ) )
	  ? $params->{EvaluateExpressions}
	  : 1;
	if ( $suffix ne "" ) {
		$prefix .= "_${suffix}";
	}

	my $file = "${prefix}.xml";

	open( XML, ">$file" ) || die "Couldn't open $file: $!\n";
	my $version = BNGversion();

	#HEADER
	print XML <<"EOF";
<?xml version="1.0" encoding="UTF-8"?>
<!-- Created by BioNetGen $version  -->
<sbml xmlns="http://www.sbml.org/sbml/level3" level="3" version="1">
  <model id="$model_name">
EOF

	$indent = "    ";

	# Parameters
	print XML $indent . "<ListOfParameters>\n";
	my $indent2 = "  " . $indent;
	my $plist   = $model->ParamList;
	for my $param ( @{ $plist->Array } ) {
		my $value;
		my $type;
		my $do_print = 0;
		if ( $param->Type =~ /^Constant/ ) {
			$value =
			  ($EvaluateExpressions)
			  ? $param->evaluate($plist)
			  : $param->toString($plist);
			$type = ($EvaluateExpressions) ? "Constant" : $param->Type;
			$do_print = 1;
		}
		next unless $do_print;
		printf XML "$indent2<Parameter id=\"%s\"", $param->Name;
		printf XML " type=\"%s\"",                 $type;
		printf XML " value=\"%s\"",                $value;
		printf XML "/>\n";
	}
	print XML $indent . "</ListOfParameters>\n";

	# Molecule Types
	print XML $model->MoleculeTypesList->toXML($indent);

	# Compartments
	print XML $model->CompartmentList->toXML($indent);

	# Species
	print XML $model->SpeciesList->toXML($indent);

	# Reaction rules
	my $string  = $indent . "<ListOfReactionRules>\n";
	my $indent2 = "  " . $indent;
	my $rindex  = 1;
	for my $rset ( @{ $model->RxnRules } ) {
		for my $rr ( @{$rset} ) {
			$string .= $rr->toXML( $indent2, $rindex, $plist );
			++$rindex;
		}
	}
	$string .= $indent . "</ListOfReactionRules>\n";
	print XML $string;

	# Observables
	my $string  = $indent . "<ListOfObservables>\n";
	my $indent2 = "  " . $indent;
	my $oindex  = 1;
	for my $obs ( @{ $model->Observables } ) {
		$string .= $obs->toXML( $indent2, $oindex );
		++$oindex;
	}
	$string .= $indent . "</ListOfObservables>\n";
	print XML $string;

	# Functions
	print XML $indent . "<ListOfFunctions>\n";
	my $indent2 = "  " . $indent;
	for my $param ( @{ $plist->Array } ) {
		next unless ( $param->Type eq "Function" );

		#print $param->Name,"\n";
		print XML $param->Ref->toXML( $plist, $indent2 );
	}
	print XML $indent . "</ListOfFunctions>\n";

	#FOOTER
	print XML <<"EOF";
  </model>
</sbml>
EOF
	print "Wrote BNG XML to $file.\n";
	return ();
}

sub writeSBML {
	my $model = shift;
	my $params = (@_) ? shift(@_) : "";

	return ("") if $NO_EXEC;

	if ( !$model->RxnList ) {
		return ("No reactions in current model.");
	}

	my $plist = $model->ParamList;

	my $model_name = $model->Name;

	# Strip prefixed path
	$model_name =~ s/^.*\///;
	my $prefix =
	  ( defined( $params->{prefix} ) ) ? $params->{prefix} : $model->Name;
	my $suffix = ( defined( $params->{suffix} ) ) ? $params->{suffix} : "";
	if ( $suffix ne "" ) {
		$prefix .= "_${suffix}";
	}

	my $file = "${prefix}.xml";

	open( SBML, ">$file" ) || die "Couldn't open $file: $!\n";
	my $version = BNGversion();

	#HEADER
	print SBML <<"EOF";
<?xml version="1.0" encoding="UTF-8"?>
<!-- Created by BioNetGen $version  -->
<sbml xmlns="http://www.sbml.org/sbml/level2" level="2" version="1">
  <model id="$model_name">
EOF

	# 1. Compartments (currently one dimensionsless compartment)
	print SBML <<"EOF";
    <listOfCompartments>
      <compartment id="cell" size="1"/>
    </listOfCompartments>
EOF

	# 2. Species
	print SBML "    <listOfSpecies>\n";

	my $use_array = ( @{ $model->Concentrations } ) ? 1 : 0;
	for my $spec ( @{ $model->SpeciesList->Array } ) {
		my $conc;
		if ($use_array) {
			$conc = $model->Concentrations->[ $spec->Index - 1 ];
		}
		else {
			$conc = $spec->Concentration;
		}
		if ( !isReal($conc) ) {
			$conc = $plist->evaluate($conc);
		}
		printf SBML
"      <species id=\"S%d\" compartment=\"%s\" initialConcentration=\"%s\"",
		  $spec->Index, "cell", $conc;
		if ( $spec->SpeciesGraph->Fixed ) {
			printf SBML " boundaryCondition=\"true\"";
		}
		printf SBML " name=\"%s\"", $spec->SpeciesGraph->StringExact;
		print SBML "/>\n";
	}
	print SBML "    </listOfSpecies>\n";

	# 3. Parameters
	# A. Rate constants
	print SBML "    <listOfParameters>\n";
	print SBML "      <!-- Independent variables -->\n";
	for my $param ( @{ $plist->Array } ) {
		next if ( $param->Expr->Type ne 'NUM' );
		printf SBML "      <parameter id=\"%s\" value=\"%s\"/>\n", $param->Name,
		  $param->evaluate($plist);
	}
	print SBML "      <!-- Dependent variables -->\n";
	for my $param ( @{ $plist->Array } ) {
		next if ( $param->Expr->Type eq 'NUM' );
		printf SBML "      <parameter id=\"%s\" constant=\"false\"/>\n",
		  $param->Name;
	}

	# B. Observables
	if ( $model->Observables ) {
		print SBML "      <!-- Observables -->\n";
	}
	for my $obs ( @{ $model->Observables } ) {
		printf SBML "      <parameter id=\"%s\" constant=\"false\"/>\n",
		  "Group_" . $obs->Name;
	}
	print SBML "    </listOfParameters>\n";

	# 4. Rules (for observables)
	print SBML "    <listOfRules>\n";
	print SBML "      <!-- Dependent variables -->\n";
	for my $param ( @{ $plist->Array } ) {
		next if ( $param->Expr->Type eq 'NUM' );
		printf SBML "      <assignmentRule variable=\"%s\">\n", $param->Name;

  #    print  SBML "        <notes>\n";
  #    print  SBML "          <xhtml:p>\n";
  #    printf SBML "            %s=%s\n", $param->Name,$param->toString($plist);
  #    print  SBML "          </xhtml:p>\n";
  #    print  SBML "        </notes>\n";
		printf SBML $param->toMathMLString( $plist, "        " );
		print SBML "      </assignmentRule>\n";
	}
	if ( @{ $model->Observables } ) {
		print SBML "      <!-- Observables -->\n";
		for my $obs ( @{ $model->Observables } ) {
			printf SBML "      <assignmentRule variable=\"%s\">\n",
			  "Group_" . $obs->Name;
			my ( $ostring, $err ) = $obs->toMathMLString();
			if ($err) { return ($err); }
			for my $line ( split( "\n", $ostring ) ) {
				print SBML"          $line\n";
			}
			print SBML "      </assignmentRule>\n";
		}
	}
	print SBML "    </listOfRules>\n";

	# 5. Reactions
	print SBML "    <listOfReactions>\n";
	my $index = 0;
	for $rxn ( @{ $model->RxnList->Array } ) {
		++$index;
		printf SBML "      <reaction id=\"R%d\" reversible=\"false\">\n",
		  $index;

		#Get indices of reactants
		my @rindices = ();
		for $spec ( @{ $rxn->Reactants } ) {
			push @rindices, $spec->Index;
		}
		@rindices = sort { $a <=> $b } @rindices;

		#Get indices of products
		my @pindices = ();
		for $spec ( @{ $rxn->Products } ) {
			push @pindices, $spec->Index;
		}
		@pindices = sort { $a <=> $b } @pindices;

		print SBML "        <listOfReactants>\n";
		for my $i (@rindices) {
			printf SBML "          <speciesReference species=\"S%d\"/>\n", $i;
		}
		print SBML "        </listOfReactants>\n";

		print SBML "        <listOfProducts>\n";
		for my $i (@pindices) {
			printf SBML "          <speciesReference species=\"S%d\"/>\n", $i;
		}
		print SBML "        </listOfProducts>\n";

		print SBML "        <kineticLaw>\n";
		my ( $rstring, $err ) =
		  $rxn->RateLaw->toMathMLString( \@rindices, \@pindices,
			$rxn->StatFactor );
		if ($err) { return ($err); }
		for my $line ( split( "\n", $rstring ) ) {
			print SBML"          $line\n";
		}
		print SBML "        </kineticLaw>\n";

		print SBML "      </reaction>\n";
	}
	print SBML "    </listOfReactions>\n";

	#FOOTER
	print SBML <<"EOF";
  </model>
</sbml>
EOF
	print "Wrote SBML to $file.\n";
	return ();
}

# Add equilibrate option, which uses additional parameters
# t_equil and spec_nonequil.  If spec_nonequil is set, these
# species are not used in equilibration of the network and are only
# added after equilibration is performed. Network generation should
# re-commence after equilibration is performed if spec_nonequil has
# been set.

sub generate_network {
	my $model  = shift;
	my $params = shift;

	my %vars = (
		'max_iter'   => '100',
		'max_agg'    => '1e99',
		'max_stoich' => '',
		'check_iso'  => '1',
		'prefix'     => $model->Name,
		'overwrite'  => 0,
		'print_iter' => 0,
		'verbose'    => 0
	);
	my %vars_pass = (
		'TextReaction' => '',
		'prefix'       => $model->Name
	);

	for my $key ( keys %$params ) {
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
			return "Unrecognized parameter $key in generate_network";
		}
	}

	return ("") if $NO_EXEC;

	#print "max_iter=$max_iter\n";
	#print "max_agg=$max_agg\n";
	#print "check_iso=$check_iso\n";

# Check if existing net file has been created since last modification time of .bngl file
	my $prefix    = $vars{prefix};
	my $overwrite = $vars{overwrite};
	if ( -e "$prefix.net" && -e "$prefix.bngl" ) {
		if ($overwrite) {
			send_warning("Removing old network file $prefix.net.");
			unlink("$prefix.net");
		}
		elsif ( -M "$prefix.net" < -M "$prefix.bngl" ) {
			send_warning(
				"$prefix.net is newer than $prefix.bngl so reading NET file.");
			my $err = $model->readFile( { file => "$prefix.net" } );
			return ($err);
		}
		else {
			return (
"Previously generated $prefix.net exists.  Set overwrite=>1 option to overwrite."
			);
		}
	}

	if ( !defined( $model->SpeciesList ) ) {
		return ("No species defined in call to generate_network");
	}
	my $slist = $model->SpeciesList;

	if ( !defined( $model->RxnList ) ) {
		$model->RxnList( RxnList->new );
		$model->RxnList->SpeciesList($slist);
	}
	my $rlist = $model->RxnList;

    
	# Initialize observables
	# (part of a bug fix for counting observables on an iteration limited network.
	#   see below.  --Justin, 11oct2010)
	foreach my $obs ( @{ $model->Observables } )
	{   $obs->update( $slist->Array );   }
	

	if ( !defined( $model->RxnRules ) ) {
		return ("No reaction_rules defined in call to generate_network");
	}
	*RxnRules = $model->RxnRules;

	my $nspec       = scalar( @{ $slist->Array } );
	my $nrxn        = scalar( @{ $rlist->Array } );
	my @rule_timing = ();
	my @rule_nrxn   = ();
	report_iter( 0, $nspec, $nrxn );
	for my $niter ( 1 .. $vars{max_iter} ) {
		$t_start_iter = cpu_time(0);
		my @species = @{ $slist->Array };

		# Apply reaction rules
		my $irule = 0;
		my $n_new, $t_off, $n_new_tot = 0;
		for my $rset (@RxnRules) {
			if ($verbose) { printf "Rule %d:\n", $irule + 1; }
			$n_new = 0;
			$t_off = cpu_time(0);
			my $dir = 0;
			for my $rr (@$rset) {

				#print $rr->toString(),"\n";
				if ($verbose) {
					if ( $dir == 0 ) {
						print "  forward:\n";
					}
					else {
						print "  reverse:\n";
					}
				}

				# change by Justin for compartments
				# added plist
				$n_new += $rr->applyRule(
					$slist, $rlist,
					$model->ParamList,
					\@species,
					{
						max_agg    => $vars{max_agg},
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
			if ($verbose) {
				printf "Result: %5d new reactions %.2e CPU s\n", $n_new, $time;
			}
			$n_new_tot += $n_new;
			++$irule;
		}

		#printf "Total   : %3d\n", $n_new_tot;

        # update RulesApplied for species processed in this interation
		for my $spec (@species) {
			if ( !($spec->RulesApplied) ) {
				$spec->RulesApplied($niter);
			}
		}

		# Update observables
        # BUG (reported by Jim): Species generated in the last iteration of network
        #  generation are not counted in observables IF generation was limited by the
        #  max_iter opertion.  
        #
        # FIX:  Observables are checked over everything in @{$slist->Array} since @species does not contain
        #  the most recently generated species.  In order for this to wrok, species in @species must be flagged
        #  as "RulesApplied" before observables are calculated.  Also, observables must be initialized over seed
        #  species before netgen iteration starts.
        # --Justin, 11oct2010
		for my $obs ( @{ $model->Observables } )
		{   $obs->update( $slist->Array );   }

		$nspec = scalar( @{ $slist->Array } );
		$nrxn  = scalar( @{ $rlist->Array } );
		report_iter( $niter, $nspec, $nrxn );


		# Free memory associated with RxnList hash
		$rlist->resetHash;

		# Stop iteration if no new species were generated
		#printf "nspec=$nspec last= %d\n", scalar(@species);
		last if ( $nspec == scalar(@species) );

		# Print network after current iteration to netfile
		if ( $vars{print_iter} ) {
			$vars_pass{prefix} = "${prefix}_${niter}";
			if ( $err = $model->writeNET( \%vars_pass ) ) { return ($err); }
			$vars_pass{prefix} = $prefix;
		}
	}

	# Print rule timing information
	printf "Cumulative CPU time for each rule\n";
	my $t_tot = 0, $n_tot = 0;
	for my $irule ( 0 .. $#RxnRules ) {
		my $eff =
		  ( $rule_nrxn[$irule] )
		  ? $rule_timing[$irule] / $rule_nrxn[$irule]
		  : 0.0;
		printf "Rule %3d: %5d reactions %.2e CPU s %.2e CPU s/rxn\n",
		  $irule + 1,
		  $rule_nrxn[$irule], $rule_timing[$irule], $eff;
		$t_tot += $rule_timing[$irule];
		$n_tot += $rule_nrxn[$irule];
	}
	my $eff = ($n_tot) ? $t_tot / $n_tot : 0.0;
	printf "Total   : %5d reactions %.2e CPU s %.2e CPU s/rxn\n", $n_tot,
	  $t_tot,
	  $eff;

	# Print result to netfile
	if ( $err = $model->writeNET( \%vars_pass ) ) {
		return ($err);
	}

	return ('');
}

sub report_iter {
	my $niter = shift;
	my $nspec = shift;
	my $nrxn  = shift;

	printf "Iteration %3d: %5d species %6d rxns", $niter, $nspec, $nrxn;
	my $t_cpu = ( $niter > 0 ) ? cpu_time(0) - $t_start_iter : 0;
	printf "  %.2e CPU s", $t_cpu;
	if ($HAVE_PS) {
		my ( $rhead, $vhead, $rmem, $vmem ) = split ' ', `ps -o rss,vsz -p $$`;
		printf " %.2e (%.2e) Mb real (virtual) memory.", $t_cpu, $rmem / 1000,
		  $vmem / 1000;
	}
	printf "\n";
	return;
}

sub simulate_ode {
	use IPC::Open3;

	my $model  = shift;
	my $params = shift;
	my $err;

	my $prefix =
	  ( defined( $params->{prefix} ) ) ? $params->{prefix} : $model->Name;
	my $suffix  = ( defined( $params->{suffix} ) )  ? $params->{suffix}  : "";
	my $netfile = ( defined( $params->{netfile} ) ) ? $params->{netfile} : "";
	my $method = ( defined( $params->{method} ) ) ? $params->{method} : "cvode";
	my $sparse = ( defined( $params->{sparse} ) ) ? $params->{sparse} : 0;
	my $atol   = ( defined( $params->{atol} ) )   ? $params->{atol}   : 1e-8;
	my $rtol   = ( defined( $params->{rtol} ) )   ? $params->{rtol}   : 1e-8;
	my $print_end =
	  ( defined( $params->{print_end} ) ) ? $params->{print_end} : 0;
	my $steady_state =
	  ( defined( $params->{steady_state} ) ) ? $params->{steady_state} : 0;
	my $verbose = ( defined( $params->{verbose} ) ) ? $params->{verbose} : 0;
    # Added explicit argument for simulation continuation.  --Justin
    my $continue = ( defined( $params->{continue} ) ) ? $params->{continue} : 0;

	return ("") if $NO_EXEC;

	if ( $model->ParamList->writeFunctions() ) {

		#		return (
		#"Simulation using Functions in .net file is currently not implemented."
		#		);
	}

	if ( $suffix ne "" ) {
		$prefix .= "_${suffix}";
	}

	print "Network simulation using ODEs\n";
	my $program;
	if ( !( $program = findExec("run_network") ) ) {
		return ("Could not find executable run_network");
	}
	my $command = "\"" . $program . "\"";
	$command .= " -o \"$prefix\"";

	# Specify netfile to read from existing netfile.
	# New netfile will be generated if prefix is set or
	# is UpdateNet is true.

	# Default netfile based on prefix
	my $netpre;
	if ( $netfile eq "" ) {
		$netfile = $prefix . ".net";
		$netpre  = $prefix;

		# Generate net file if not already created or if prefix is set in params
		if (   ( !-e $netfile )
			|| $model->UpdateNet
			|| ( defined( $params->{prefix} ) )
			|| ( defined( $params->{suffix} ) ) )
		{
			if ( $err = $model->writeNET( { prefix => "$netpre" } ) ) {
				return ($err);
			}
		}
	}
	else {

		# Make sure NET file has proper suffix
		$netpre = $netfile;
		$netpre =~ s/[.]([^.]+)$//;
		if ( !( $1 =~ /net/i ) ) {
			return ("File $netfile does not have net suffix");
		}
	}

	$command .= " -p $method";
	if ( $method eq "cvode" ) {

		# Set paramters related to CVODE integration
		$command .= " -a $atol";
		$command .= " -r $rtol";
		if ($sparse) {
			$command .= " -b";
		}
	}
	else {
		return ("method set to unrecognized type: $method");
	}

	# Checking of steady state
	if ($steady_state) {
		$command .= " -c";
	}

	# Printing of _end.net file
	if ($print_end) {
		$command .= " -e";
	}

	# More detailed output
	if ($verbose) {
		$command .= " -v";
	}

	# Continuation
	# NOTE: continuation must now be specified explicitly!
	if ($continue) {
		$command .= " -x";
	}

	# Set start time for trajectory
	my $t_start;
	# t_start argument is defined
	if ( defined( $params->{t_start} ) )
	{
		$t_start = $params->{t_start};		
		# if this is a continuation, check that model time equals t_start
		if ( $continue )
		{
		    unless ( defined($model->Time)  and  ($model->Time == $t_start) )
		    {
		        return ("t_start must equal current model time for continuation.");
		    }
		}
	}
	# t_start argument not defined
	else 
	{
	    if ( $continue   and   defined($model->Time) )
	    {   $t_start = $model->Time;   }
 		else
 		{   $t_start = 0.0;   }
	}

    # set the model time to t_start
    $model->Time($t_start);

  	# CHANGES: to preserve backward compatibility: only output start time if ne 0
	unless ( $t_start == 0.0 )
	{   $command.= " -i $t_start"; 	}


	# Use program to compute observables
	$command .= " -g \"$netfile\"";

	# Read network from $netfile
	$command .= " \"$netfile\"";

	if ( defined( $params->{t_end} ) ) {
		my $n_steps, $t_end;
		$t_end = $params->{t_end};
		# Extend interval for backward compatibility.  Previous versions default assumption was $t_start=0.
		if (($t_end-$t_start)<=0.0){ 
		     return ("t_end must be greater than t_start.");
		}
		$n_steps = ( defined( $params->{n_steps} ) ) ? $params->{n_steps} : 1;
		my $step_size = ( $t_end - $t_start ) / $n_steps;
		$command .= " ${step_size} ${n_steps}";
	}
	elsif ( defined( $params->{sample_times} ) ) {

		# Two sample points are given.
		*sample_times = $params->{sample_times};
		if ( $#sample_times > 1 ) {
			$command .= " " . join( " ", @sample_times );
			$t_end = $sample_times[$#sample_times];
		}
		else {
			return ("sample_times array must contain 3 or more points");
		}
	}
	else {
		return ("Either t_end or sample_times must be defined");
	}

	print "Running run_network on ", `hostname`;
	print "full command: $command\n";

	# Compute timecourses using run_network
	local ( *Reader, *Writer, *Err );
	if ( !( $pid = open3( \*Writer, \*Reader, \*Err, "$command" ) ) ) {
		return ("$command failed: $?");
	}
	my $last                 = "";
	my $steady_state_reached = 0;
	while (<Reader>) {
		print;
		$last = $_;
		if ($steady_state) {
			if (/Steady state reached/) {
				$steady_state_reached = 1;
			}
		}
	}
	my @err = <Err>;
	close Writer;
	close Reader;
	close Err;
	waitpid( $pid, 0 );

	# Check for errors in running the simulation command
	if (@err) {
		print @err;
		return ("$command\n  did not run successfully.");
	}
	if ( !( $last =~ /^Program times:/ ) ) {
		return ("$command\n  did not run successfully.");
	}

	if ($steady_state) {
		send_warning("Steady_state status= $steady_state_reached");
		if ( !$steady_state_reached ) {
			return ("Simulation did not reach steady state by t_end=$t_end");
		}
	}

	# Process output concentrations
	if ( !( $model->RxnList ) ) {
		send_warning(
			"Not updating species concnetrations because no model has been read"
		);
	}
	elsif ( -e "$prefix.cdat" ) {
		print "Updating species concentrations from $prefix.cdat\n";
		open( CDAT, "$prefix.cdat" );
		my $last = "";
		while (<CDAT>) {
			$last = $_;
		}
		close(CDAT);

		# Update Concentrations with concentrations from last line of CDAT file
		my ( $time, @conc ) = split( ' ', $last );
		*species = $model->SpeciesList->Array;
		if ( $#conc != $#species ) {
			$err =
			  sprintf
			  "Number of species in model (%d) and CDAT file (%d) differ",
			  scalar(@species), scalar(@conc);
			return ($err);
		}
		$model->Concentrations( [@conc] );
		$model->UpdateNet(1);
	}
	else {
		return ("CDAT file is missing");
	}
	$model->Time($t_end);
	#printf "t_end=%g\n", $t_end;

	return ("");
}

# Set the concentration of a species to specified value.
# Value may be a number or a parameter.
sub setConcentration {
	my $model = shift;
	my $sname = shift;
	my $value = shift;

	return ("") if $NO_EXEC;

	my $plist = $model->ParamList;
	my $err;

	#print "sname=$sname value=$value\n";

	# SpeciesGraph specified by $sname
	my $sg = SpeciesGraph->new;
	$err = $sg->readString( \$sname, $model->CompartmentList );
	if ($err) { return ($err); }

	# Should check that this SG specifies a complete species, otherwise
	# may match a number of species.

	# Find matching species
	my $spec;
	if ( !( $spec = $model->SpeciesList->lookup($sg) ) ) {
		$err = sprintf "Species %s not found in SpeciesList", $sg->toString();
		return ($err);
	}

	# Read expression
	my $expr    = Expression->new();
	my $estring = $value;
	my $conc;
	if ( my $err = $expr->readString( \$estring, $plist ) ) {
		return ( "", $err );
	}
	$conc = $expr->evaluate($plist);

	# Set concentration in Species object
	$spec->Concentration($conc);

	# Set concentration in Concentrations array if defined
	if ( @{ $model->Concentrations } ) {
		$model->Concentrations->[ $spec->Index - 1 ] = $conc;
	}

	# Set flag to update netfile when it's used
	$model->UpdateNet(1);

	printf "Set concentration of species %s to value %s\n",
	  $spec->SpeciesGraph->StringExact, $conc;

	return ("");
}

sub setParameter {
	my $model = shift;
	my $pname = shift;
	my $value = shift;

	return ("") if $NO_EXEC;

	my $plist = $model->ParamList;
	my $param, $err;

	# Error if parameter doesn't exist
	( $param, $err ) = $plist->lookup($pname);
	if ($err) { return ($err) }

	# Read expression
	my $expr    = Expression->new();
	my $estring = "$pname=$value";
	if ( $err = $expr->readString( \$estring, $plist ) ) { return ($err) }

	# Set flag to update netfile when it's used
	$model->UpdateNet(1);

	printf "Set parameter %s to value %s\n", $pname, $expr->evaluate($plist);
	return ("");
}

sub saveConcentrations {
	my $model = shift;

	my $i = 0;
	*conc = $model->Concentrations;
	if (@conc) {
		for my $spec ( @{ $model->SpeciesList->Array } ) {
			$spec->Concentration( $conc[$i] );

			#printf "%6d %s\n", $i+1, $conc[$i];
			++$i;
		}
	}
	return ("");
}

sub resetConcentrations {
	my $model = shift;

	return ("") if $NO_EXEC;

	$model->Concentrations( [] );
	return ("");
}

sub setModelName {
	my $model = shift;
	my $name  = shift;

	$model->Name($name);
	return ("");
}

sub simulate_ssa {
	use IPC::Open3;

	my $model  = shift;
	my $params = shift;
	my $err;

	my $prefix =
	  ( defined( $params->{prefix} ) ) ? $params->{prefix} : $model->Name;
	my $suffix  = ( defined( $params->{suffix} ) )  ? $params->{suffix}  : "";
	my $netfile = ( defined( $params->{netfile} ) ) ? $params->{netfile} : "";
	my $verbose = ( defined( $params->{verbose} ) ) ? $params->{verbose} : 0;
	my $print_end =
	  ( defined( $params->{print_end} ) ) ? $params->{print_end} : 0;
	my $print_net =
	  ( defined( $params->{print_net} ) ) ? $params->{print_net} : 0;
	my $seed =
	  ( defined( $params->{seed} ) )
	  ? $params->{seed}
	  : int( rand( 2**32 ) ) + 1;
	my $verbose = ( defined( $params->{verbose} ) ) ? $params->{verbose} : 0;
    # Added explicit argument for simulation continuation.  --Justin
    my $continue = ( defined( $params->{continue} ) ) ? $params->{continue} : 0;	

	return ("") if $NO_EXEC;

	if ( $model->ParamList->writeFunctions() ) {
        #return (
        #   "Simulation using Functions in .net file is currently not implemented."
        #);
	}

	if ( $suffix ne "" ) {
		$prefix .= "_${suffix}";
	}

	print "Network simulation using SSA\n";
	my $program;
	if ( !( $program = findExec("run_network") ) ) {
		return ("Could not find executable run_network");
	}
	my $command = "\"" . $program . "\"";
	$command .= " -o \"$prefix\"";

	# Default netfile based on prefix
	my $netpre;
	if ( $netfile eq "" ) {
		$netfile = $prefix . ".net";
		$netpre  = $prefix;

		# Generate net file if not already created or if prefix is set in params
		if (   ( !-e $netfile )
			|| $model->UpdateNet
			|| ( defined( $params->{prefix} ) )
			|| ( defined( $params->{suffix} ) ) )
		{
			if ( $err = $model->writeNET( { prefix => "$netpre" } ) ) {
				return ($err);
			}		
		}
	}
	else {

		# Make sure NET file has proper suffix
		$netpre = $netfile;
		$netpre =~ s/[.]([^.]+)$//;
		if ( !( $1 =~ /net/i ) ) {
			return ("File $netfile does not have net suffix");
		}
	}


	my $update_interval =
	  ( defined( $params->{update_interval} ) )
	  ? $params->{update_interval}
	  : 1;
	my $expand = ( defined( $params->{expand} ) ) ? $params{$expand} : "lazy";
	if ( $expand eq "lazy" ) {
	}
	elsif ( $expand eq "layered" ) {
	}
	else {
		return ("Unrecognized expand method $expand");
	}

	$command .= " -p ssa -h $seed";

	if ($print_net) {
		$command .= " -n";
	}

	if ($print_end) {
		$command .= " -e";
	}

	# More detailed output
	if ($verbose) {
		$command .= " -v";
	}
	
	# Continuation
	# NOTE: continuation must now be specified explicitly!
	if ($continue) {
		$command .= " -x";
	}	

	# Set start time for trajectory
	my $t_start;
	# t_start argument is defined
	if ( defined( $params->{t_start} ) )
	{
		$t_start = $params->{t_start};		
		# if this is a continuation, check that model time equals t_start
		if ( $continue )
		{
		    unless ( defined($model->Time)  and  ($model->Time == $t_start) )
		    {
		        return ("t_start must equal current model time for continuation.");
		    }
		}
	}
	# t_start argument not defined
	else 
	{
	    if ( $continue   and   defined($model->Time) )
	    {   $t_start = $model->Time;   }
 		else
 		{   $t_start = 0.0;   }
	}

    # set the model time to t_start
    $model->Time($t_start);

  	# To preserve backward compatibility: only output start time if != 0
	unless ( $t_start == 0.0 )
	{   $command.= " -i $t_start"; 	}
	

	# Use program to compute observables
	$command .= " -g \"$netfile\"";

	# Read network from $netfile
	$command .= " \"$netfile\"";

	if ( defined( $params->{n_steps} ) ) {
		my $n_steps, $t_end;
		if ( defined( $params->{t_end} ) ) {
			$t_end = $params->{t_end};
		}
		else {
			return ("Parameter t_end must be defined");
		}
		# Extend interval for backward compatibility.  Previous versions default assumption was $t_start=0.
		if (($t_end-$t_start)<=0.0){
		    return ("t_end must be greater than t_start.");
        }
		$n_steps = ( defined( $params->{n_steps} ) ) ? $params->{n_steps} : 1;
		my $step_size = ( $t_end - $t_start ) / $n_steps;
		$command .= " ${step_size} ${n_steps}";
	}
	elsif ( defined( $params->{sample_times} ) ) {

		# Two sample points are given.
		*sample_times = $params->{sample_times};
		if ( $#sample_times > 1 ) {
			$command .= " " . join( " ", @sample_times );
		}
		else {
			return ("sample_times array must contain 3 or more points");
		}
	}

	# Determine index of last rule iteration
	if ( $model->SpeciesList ) {
		my $n_iter = 0;
		for my $spec ( @{ $model->SpeciesList->Array } ) {
			my $iter = $spec->RulesApplied;
			$n_iter = ( $iter > $n_iter ) ? $iter : $n_iter;
		}

		#print "Last iteration was number $n_iter\n";
	}
	print "Running run_network on ", `hostname`;
	print "full command: $command\n";

	#print "seed=$seed\n";

	# Compute timecourses using run_network
	local ( *Reader, *Writer, *Err );
	if ( !( $pid = open3( \*Writer, \*Reader, \*Err, "$command" ) ) ) {
		return ("$command failed: $?");
	}
	my $last    = "";
	my $edgepop = 0;
	while (<Reader>) {

		# If network generation is on-the-fly, look for signal that
		# species at the edge of the network is newly populated
		if (s/^edgepop://) {

			# Can't generate new species if running from netfile
			if ( !$model->SpeciesList ) {
				++$edgepop;
				print Writer "continue\n";
				next;
			}

			my (@newspec) = split(' ');

			#print join(" ",@newspec),"\n";
			my $slist = $model->SpeciesList;
			my $rlist = $model->RxnList;

			my $species;
			++$n_iter;
			if ( $expand eq "lazy" ) {
				my @sarray, $spec;
				for my $sname (@newspec) {
					if ( !( $spec = $slist->lookup_bystring($sname) ) ) {
						return ("Couldn't find species $sname.");
					}
					push @sarray, $spec;
				}
				if ($verbose) {
					printf "Applying rules to %d species\n", scalar(@sarray);
				}
				$species = \@sarray;
			}
			else {

				# Do full next iteration of rule application
				$species = $slist->Array;
			}

			# Apply reaction rules
			my $nspec = scalar( @{ $slist->Array } );
			my $nrxn  = scalar( @{ $rlist->Array } );
			my $irule = 1;
			my $n_new, $t_off;
			for my $rset ( @{ $model->RxnRules } ) {
				if ($verbose) { $t_off = cpu_time(0); }
				$n_new = 0;
				for my $rr (@$rset) {

					#print $rr->toString(),"\n";
					# modified by Justin.  added paramList
					$n_new +=
					  $rr->applyRule( $slist, $rlist, $model->ParamList,
						$species, $params );
				}
				if ($verbose) {
					printf "Rule %3d: %3d new reactions %.2e s CPU time\n",
					  $irule,
					  $n_new, cpu_time(0) - $t_off;
				}
				++$irule;
			}

			# Update observables
			for my $obs ( @{ $model->Observables } ) {
				$obs->update($species);
			}

			# Set RulesApplied attribute
			for my $spec (@$species) {
				if ( !$spec->RulesApplied ) {
					$spec->RulesApplied($n_iter);
				}
			}

			# Print new species, reactions, and observable entries
			my $nrxn_new = scalar( @{ $rlist->Array } );
			if ( $nrxn_new > $nrxn ) {
				print Writer "read\n";
				$slist->print( *Writer, $nspec );

				#$slist->print(*STDOUT, $nspec);
				$rlist->print( *Writer, $nrxn );

				#$rlist->print(*STDOUT, $nrxn);
				print Writer "begin groups\n";
				my $i = 1;
				for my $obs ( @{ $model->Observables } ) {
					print Writer "$i ";
					$obs->printGroup( *Writer, $species );

					#$obs->printGroup(*STDOUT, $species);
					++$i;
				}
				print Writer "end groups\n";
			}
			else {
				print Writer "continue\n";
			}
		}
		else {
			print;
			$last = $_;
		}
	}
	my @err = <Err>;
	close Writer;
	close Reader;
	close Err;
	waitpid( $pid, 0 );

	# Report number of times edge species became populated
	# without network expansion
	#  if ($edgepop){
	if (1) {
		printf "Edge species became populated %d times.\n", $edgepop;
	}

	# Print final netfile
	if ( $model->SpeciesList
		&& ( $err = $model->writeNET( { prefix => "$netpre" } ) ) )
	{
		return ($err);
	}

	# Process output concentrations
	if ( !( $model->RxnList ) ) {
		send_warning(
			"Not updating species concnetrations because no model has been read"
		);
	}
	elsif ( -e "$prefix.cdat" ) {
		print "Updating species concentrations from $prefix.cdat\n";
		open( CDAT, "$prefix.cdat" );
		my $last = "";
		while (<CDAT>) {
			$last = $_;
		}
		close(CDAT);

		# Update Concentrations with concentrations from last line of CDAT file
		my ( $time, @conc ) = split( ' ', $last );
		*species = $model->SpeciesList->Array;
		if ( $#conc != $#species ) {
			$err =
			  sprintf
			  "Number of species in model (%d) and CDAT file (%d) differ",
			  scalar(@species), scalar(@conc);
			return ($err);
		}
		$model->Concentrations( [@conc] );
		$model->UpdateNet(1);
	}
	else {
		return ("CDAT file is missing");
	}

	if (@err) {
		print @err;
		return ("$command\n  did not run successfully.");
	}
	if ( !( $last =~ /^Program times:/ ) ) {
		return ("$command\n  did not run successfully.");
	}

	$model->Time($t_end);

	return ("");
}

sub simulate_nf {
	use IPC::Open3;

	my $model  = shift;
	my $params = shift;
	my $err;

	my $prefix =
	  ( defined( $params->{prefix} ) ) ? $params->{prefix} : $model->Name;
	my $suffix = ( defined( $params->{suffix} ) ) ? $params->{suffix} : "";

	my $verbose = ( defined( $params->{verbose} ) ) ? $params->{verbose} : 0;
	my $complex = ( defined( $params->{complex} ) ) ? $params->{complex} : 0;

	# Handle other command line args.
	my $otherCommandLineParameters =
	  ( defined( $params->{param} ) ) ? $params->{param} : "";

	#print "$otherCommandLineParameters\n";

	return ("") if $NO_EXEC;

	if ( $suffix ne "" ) {
		$prefix .= "_${suffix}";
	}

	print "Network simulation using NFsim\n";
	my $program;
	if ( !( $program = findExec("NFsim") ) ) {
		return ("Could not find executable NFsim");
	}
	my $command = "\"" . $program . "\"";

	# Write BNG xml file
	$model->writeXML( { prefix => $prefix } );

	# Read network from $netfile
	$command .= " -xml \"${prefix}.xml\" -o \"${prefix}.gdat\"";

	# Append the run time and output intervals
	my $t_start;
	if ( defined( $params->{t_start} ) ) {
		$t_start = $params->{t_start};
		$model->Time($t_start);
	}
	else {
		$t_start = ( defined( $model->Time ) ) ? $model->Time : 0;
	}

	if ( defined( $params->{n_steps} ) ) {
		my $n_steps, $t_end;
		$n_steps = $params->{n_steps};
		if ( $n_steps < 1 ) {
			return ("No simulation output requested: set n_steps>0");
		}
		if ( defined( $params->{t_end} ) ) {
			$t_end = $params->{t_end};
		}
		else {
			return ("Parameter t_end must be defined");
		}
		$command .= " -sim ${t_end} -oSteps ${n_steps}";
	}
	elsif ( defined( $params->{sample_times} ) ) {
		return ("sample_times not supported in this version of NFsim");
	}
	else {
		return ("No simulation output requested: set n_steps>0");
	}

	# Append the other command line arguments
	$command .= " " . $otherCommandLineParameters;

	# Turn on complex bookkeeping if requested
	# TODO: Automatic check for turning this on
	if ($complex) { $command .= " -cb"; }
	if ($verbose) { $command .= " -v"; }

	print "Running NFsim on ", `hostname`;
	print "full command: $command\n";

	# Compute timecourses using nfsim
	local ( *Reader, *Writer, *Err );
	if ( !( $pid = open3( \*Writer, \*Reader, \*Err, "$command" ) ) ) {
		return ("$command failed: $?");
	}
	my $last = "";
	while (<Reader>) {
		print;
		$last = $_;
	}
	( my @err = <Err> );

	close Writer;
	close Reader;
	close Err;
	waitpid( $pid, 0 );

	if (@err) {
		print "Error log:\n", @err;
		return ("$command\n  did not run successfully.");
	}

	return ("");
}

sub writeNET {
	my $model = shift;
	my $params = (@_) ? shift(@_) : "";

	my %vars = (
		'simple' => 0,
		'prefix' => $model->Name,
		'suffix' => ''
	);
	my %vars_pass = (
		'TextReaction' => '',
		'NETfile'      => '1'
	);

	for my $key ( keys %$params ) {
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

	return ("") if $NO_EXEC;

	my $suffix = $vars{suffix};
	my $prefix = $vars{prefix};
	my $simple = $vars{simple};
	if ( $suffix ne "" ) {
		$prefix .= "_${suffix}";
	}
	my $file = "${prefix}.net";

	if ( !( $model->RxnList ) ) {
		return ("Current model contains no reactions");
	}

	# Print network to file
	open( out, ">$file" ) || return ("Couldn't write to $file: $!\n");
	if ($simple) {
		print out $model->writeSimpleBNGL();
	}
	else {
		print out $model->writeBNGL( \%vars_pass );
	}
	close(out);
	print "Wrote network to $file.\n";
	$model->UpdateNet(0);

	return ("");
}

# Function to require the version conform to specified requirement
# Syntax: version(string);
# string= major[.minor][.dist][+-]

# major, minor, and dist. indicate the major, minor, and distribution number
# respectively against which the BioNetGen version numbers will be compared.
# + indicates version should be the specified version or later (default)
# - indicates version should be the specified version or earlier

sub version {
	my $model   = shift;
	my $vstring = shift;

	return ("") if $NO_EXEC;

	if (@_) {
		return ("Additional arguments to function version.");
	}

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
			return (
"This file requires BioNetGen version $vstring or later.  Active version is $bng_string."
			);
		}
	}
	else {
		if ( $bng_number > $r_number ) {
			return (
"This file requires BioNetGen version $vstring or earlier. Active version is $bng_string."
			);
		}
	}

	# Add current version requirement to the model
	push @{ $model->Version }, $vstring;

	return ("");
}

sub findExec {
	use Config;
	my $prog = shift;

	my $exec = BNGpath( "bin", $prog );

	# First look for generic binary in BNGpath
	if ( -x $exec ) {
		return ($exec);
	}

	my $arch = $Config{myarchname};

	# Currently recognized values of $arch are
	# i686-linux, ppc-darwin, MSWin32

	# Then look for os specific binary
	$exec .= "_" . $arch;

	if ( $arch =~ /MSWin32/ ) {
		$exec .= ".exe";
	}

	if ( -x $exec ) {
		return ($exec);
	}
	else {
		print "findExec: $exec not found.\n";
		return ("");
	}
}

sub LinearParameterSensitivity {

#This function will perform a brute force linear sensitivity analysis
#bumping one parameter at a time according to a user specified bump
#For each parameter, simulations are saved as:
#'netfile_paramname_suffix.(c)(g)dat', where netfile is the .net model file
#and paramname is the bumped parameter name, and c/gdat files have meaning as normal
	######################
	#NOT IMPLEMENTED YET!!
	#Additional files are written containing the raw sensitivity coefficients
	#for each parameter bump
	#format: 'netfile_paramname_suffix.(c)(g)sc'
	#going across rows is increasing time
	#going down columns is increasing species/observable index
	#first row is time
	#first column is species/observable index
	######################

	#Starting time assumed to be 0

#Input Hash Elements:
#REQUIRED PARAMETERS
#net_file:  the base .net model to work with; string;
#t_end:  the end simulation time; real;
#OPTIONAL PARAMETERS
#bump:  the percentage parameter bump; real; default 5%
#inp_ppert:  model input parameter perturbations; hash{pnames=>array,pvalues=>array};
#default empty
#inp_cpert:  model input concentration perturbations; hash{cnames=>array,cvalues=>array};
#default empty
#stochast:  simulate_ssa (1) or simulate_ode (0) is used; boolean; default 0 (ode)
#CANNOT HANDLE simulate_ssa CURRENTLY
#sparse:    use sparse methods for integration?; boolean; 1
#atol:  absolute tolerance for simulate_ode; real; 1e-8
#rtol:  relative tolerance for simulate_ode; real; 1e-8
#init_equil:  equilibrate the base .net model; boolean; default 1 (true)
#re_equil:  equilibrate after each parameter bump but before simulation; boolean; default 1 (true)
#n_steps:  the number of evenly spaced time points for sensitivity measures; integer;
#default 50
#suffix:  added to end of filename before extension; string; default ""

	#Variable Declaration and Initialization
	use strict;
	my $model;     #the BNG model
	my %params;    #the input parameter hash table
	my $net_file = "";
	my %inp_pert;
	my $t_end;
	my %readFileinputs;
	my %simodeinputs;
	my $simname;
	my $basemodel = BNGModel->new();
	my $plist;
	my $param_name;
	my $param_value;
	my $new_param_value;
	my $pperts;
	my $cperts;
	my $pert_names;
	my $pert_values;
	my $pert_names;
	my $pert_values;
	my $newbumpmodel = BNGModel->new();
	my $foo;
	my $i;

	#Initialize model and input parameters

	my $model  = shift;
	my $params = shift;

	#Required params
	if ( defined( $params->{net_file} ) ) {
		$net_file = $params->{net_file};
	}
	else {
		$net_file = $model->Name;
	}
	if ( defined( $params->{t_end} ) ) {
		$t_end = $params->{t_end};
	}
	else {
		return ("t_end not defined");
	}

	#Optional params
	my $bump     = ( defined( $params->{bump} ) )     ? $params->{bump}     : 5;
	my $stochast = ( defined( $params->{stochast} ) ) ? $params->{stochast} : 0;
	my $sparse   = ( defined( $params->{sparse} ) )   ? $params->{sparse}   : 1;
	my $atol = ( defined( $params->{atol} ) ) ? $params->{atol} : 1e-8;
	my $rtol = ( defined( $params->{rtol} ) ) ? $params->{rtol} : 1e-8;
	my $init_equil =
	  ( defined( $params->{init_equil} ) ) ? $params->{init_equil} : 1;
	my $t_equil = ( defined( $params->{t_equil} ) ) ? $params->{t_equil} : 1e6;
	my $re_equil = ( defined( $params->{re_equil} ) ) ? $params->{re_equil} : 1;
	my $n_steps = ( defined( $params->{n_steps} ) ) ? $params->{n_steps} : 50;
	my $suffix  = ( defined( $params->{suffix} ) )  ? $params->{suffix}  : "";

	#Run base case simulation
	%readFileinputs = ( file => "$net_file.net" );
	$basemodel->readFile( \%readFileinputs );

	#if initial equilibration is required
	if ($init_equil) {
		$simname      = "_baseequil_";
		%simodeinputs = (
			prefix       => "$net_file$simname$suffix",
			t_end        => $t_equil,
			sparse       => $sparse,
			n_steps      => $n_steps,
			steady_state => 1,
			atol         => $atol,
			rtol         => $rtol
		);
		$basemodel->simulate_ode( \%simodeinputs );
	}
	$simname      = "_basecase_";
	%simodeinputs = (
		prefix       => "$net_file$simname$suffix",
		t_end        => $t_end,
		sparse       => $sparse,
		n_steps      => $n_steps,
		steady_state => 0,
		atol         => $atol,
		rtol         => $rtol
	);

	#Implement input perturbations
	if ( defined( $params->{inp_ppert} ) ) {
		$pperts      = $params->{inp_ppert};
		$pert_names  = $pperts->{pnames};
		$pert_values = $pperts->{pvalues};
		$i           = 0;
		while ( $pert_names->[$i] ) {
			$param_name  = $pert_names->[$i];
			$param_value = $pert_values->[$i];
			$basemodel->setParameter( $param_name, $param_value );
			$i = $i + 1;
		}
	}
	if ( defined( $params->{inp_cpert} ) ) {
		$cperts      = $params->{inp_cpert};
		$pert_names  = $cperts->{cnames};
		$pert_values = $cperts->{cvalues};
		$i           = 0;
		while ( $pert_names->[$i] ) {
			$param_name  = $pert_names->[$i];
			$param_value = $pert_values->[$i];
			$basemodel->setConcentration( $param_name, $param_value );
			$i = $i + 1;
		}
	}
	$basemodel->simulate_ode( \%simodeinputs );

	$plist = $basemodel->ParamList;

	#For every parameter in the model
	for my $model_param ( @{ $plist->Array } ) {
		$param_name      = $model_param->Name;
		$param_value     = $model_param->evaluate();
		$new_param_value = $param_value * ( 1 + $bump / 100 );

		#Get fresh model and bump parameter
		$newbumpmodel->readFile( \%readFileinputs );
		$newbumpmodel->setParameter( $param_name, $new_param_value );

		#Reequilibrate
		if ($re_equil) {
			$simname = "_equil_$param_name", "_";
			%simodeinputs = (
				prefix       => "$net_file$simname$suffix",
				t_end        => $t_equil,
				sparse       => $sparse,
				n_steps      => $n_steps,
				steady_state => 1,
				atol         => $atol,
				rtol         => $rtol
			);
			$newbumpmodel->simulate_ode( \%simodeinputs );
		}

		#Implement input and run simulation
		$simname = "_$param_name", "_";
		%simodeinputs = (
			prefix       => "$net_file$simname$suffix",
			t_end        => $t_end,
			sparse       => $sparse,
			n_steps      => $n_steps,
			steady_state => 0,
			atol         => $atol,
			rtol         => $rtol
		);
		if ( defined( $params->{inp_ppert} ) ) {
			$pperts      = $params->{inp_ppert};
			$pert_names  = $pperts->{pnames};
			$pert_values = $pperts->{pvalues};
			$i           = 0;
			while ( $pert_names->[$i] ) {
				$param_name  = $pert_names->[$i];
				$param_value = $pert_values->[$i];
				$newbumpmodel->setParameter( $param_name, $param_value );
				$i = $i + 1;
			}
		}
		if ( defined( $params->{inp_cpert} ) ) {
			$cperts      = $params->{inp_cpert};
			$pert_names  = $cperts->{cnames};
			$pert_values = $cperts->{cvalues};
			$i           = 0;
			while ( $pert_names->[$i] ) {
				$param_name  = $pert_names->[$i];
				$param_value = $pert_values->[$i];
				$newbumpmodel->setConcentration( $param_name, $param_value );
				$i = $i + 1;
			}
		}
		$newbumpmodel->simulate_ode( \%simodeinputs );

		#Evaluate sensitivities and write to file

		#Get ready for next bump
		$newbumpmodel = BNGModel->new();
	}

}
1;

