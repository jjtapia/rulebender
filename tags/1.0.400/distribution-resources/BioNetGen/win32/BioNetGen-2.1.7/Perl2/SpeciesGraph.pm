package SpeciesGraph;

use Class::Struct;
use FindBin;
use lib $FindBin::Bin;
use Molecule;
use Component;
use Compartment;
use BNGUtils;
use Map;
use MoleculeTypesList;
use HNauty;

struct SpeciesGraph => {
	Name  => '$',
	Label => '$',   # Local variable to assign to this Object (analogous to Label atrribute of Moleules
	                # and Components, not to be confused with graph label)
	Compartment => 'Compartment',
	Molecules   => '@',
	Edges       => '@',    # an array whose only element is an array of edge definitions (why a 2d array?)
	Adjacency   => '%',
	StringID    => '$',    # This is for the Canonical label.
	StringExact => '$',    # This is also related to canonical labeling (what's the difference?)  --justin
	Quantifier  => '$',
	Species     => 'Species', #Set only if this SpeciesGraph is bound to a particular Species
	MatchOnce   => '$',       # Map this pattern at most 1 time to a given species
	Fixed       => '$',       # Concentration of species referred to by this pattern is constant.
	IsCanonical => '$'
};

my $SpeciesLabel = "Auto";    # Allowed values are Auto, HNauty, Quasi

sub setSpeciesLabel {
	my $label = shift;
	my %valid = ( 'Auto' => 1, 'HNauty' => 1, 'Quasi' => 1 );
	if ( defined( $valid{$label} ) ) {
		$SpeciesLabel = $label;
		print "SpeciesLabel method set to $label.\n";
	}
	else {
		return ("Invalid value for SpeciesLabel function: $label");
	}
	return ("");
}

sub readString
{
	my $sg            = shift;
	my $strptr        = shift;
	my $clist         = shift;
	my $is_species    = (@_) ? shift : 1;
	my $stop          = (@_) ? shift : '';
	my $mtlist        = (@_) ? shift : '';
	my $AllowNewTypes = (@_) ? shift : 0;

	my $string_left = $$strptr;
	my %mlabels     = ();
	my %elabels     = ();
	*molecules = $sg->Molecules;

	# NOTES:  species string parsed at these blocks:
	# (1) seed species
	# (2) reaction rule, reaction
	# (3) observables

	# remove leading whitespace
	$string_left =~ s/^\s+//;

    # Header (all characters up to ":")
    # Fixed bug here  --Justin
    # restrict pre ":" characters to word characters, whitespace, "%", "*" and "@",
	if ( $string_left =~ s/^([\w\s%@*]+)(::|:)\s*// ) {
		my $head = $1;

		# Optional species name
		# COMMENT: why is "*" allowed in a Name?
		if ( $head =~ s/^([\w\s*]+)// ) { $sg->Name($1); }

		# Optional Label (variable name)
		if ( $head =~ s/^\%(\w+)// ) { $sg->Label($1); }

		# Optional species compartment
		if ( $head =~ s/^\@(\w+)// ) {
			my $comp = $clist->lookup($1);
			if   ($comp) { $sg->Compartment($comp); }
			else         { return ("Undefined compartment $1"); }
		}
		if ($head) {
			return ("Improper syntax for SpeciesGraph header at $head");
		}
	}

	while ($string_left)
	{
		# Handle continuation and stopping
		# Continue characters
		if ( $string_left =~ s/^[.]// ) { next; }
		elsif ($stop) {

			# Stop characters
			if ( $string_left =~ /$stop/ ) {

				#printf "str_left=%s\n", $string_left;
				last;
			}
		}

		# Handle modifier syntax
		# Set Fixed attribute by prepending '$'
		if ( $string_left =~ s/^\$// ) { $sg->Fixed(1); }

		# Quantifier
		elsif ( $string_left =~ s/^(=|==|<|<=|>|>=)(\d+)// )
		{
			#printf "Quantifier for %s is %s\n", $sg->toString(), $1.$2;
			my $op = $1;
			if ( $op eq '=' ) { $op = '==' }
			$sg->Quantifier( $op . $2 );
			if ($is_species) { return ("Quantifier not allowed in species"); }
		}

		# Attributes listed in {}
		elsif ( $string_left =~ s/^[{]// )
		{
			while ( !( $string_left =~ s/^\}// ) )
			{
				my $attr  = '';
				my $value = '';

				# Get attribute name
				if ( $string_left =~ s/^([^,=\}]+)// ) { $attr = $1; }
				else {
					return ("Null attribute for SpeciesGraph at $string_left");
				}

				# Get (optional) attribute value
				if ( $string_left =~ s/^=([^,\}]+)// ) { $value = $1; }

				# Remove trailing comma
				$string_left =~ s/^,//;

				if ( $attr eq 'MatchOnce' ) {
					my $val = booleanToInt($value);
					if ( $val == -1 ) {
						return (
                            "Invalid value $value assigned to Boolean attribute $attr"
						);
					}
					$sg->MatchOnce($val);
				}
				elsif ( $attr eq 'Fixed' ) {
					my $val = booleanToInt($value);
					if ( $val == -1 ) {
						return ( "Invalid value $value assigned to Boolean attribute $attr" );
					}
					$sg->Fixed($val);
				}
				else { return ("Invalid attribute $attr for SpeciesGraph"); }
			}
		}
		else
		{
			# Read molecule
			my ( $mol, $err ) = Molecule::newMolecule( \$string_left, $clist );
			if ($err) { return ($err); }
			push @molecules, $mol;
		}
	}

	# Done processing input string

	# Check edge labels for correct definition
	# Sorts molecules and components and contructs edges and adjacency matrix.
	if ($is_species)
	{
		# get Species Compartment (undefined is a possible). --justin
		# NOTE: removed this from sortLabel routine!
		# FUTURE: check for well-defined compartment in "strict" mode.  --justin
		my $err = $sg->assignCompartment();
		return $err if ($err);

		#printf "before sort: %s\n", $sg->toString();
		if ( my $err = $sg->sortLabel() ) { return ($err); }

		#printf "after  sort: %s\n", $sg->toString();

		# Check for correct number of subgraphs
		my ($nsub) = $sg->findConnected2();
		if ( $nsub != 1 ) {

			#print "nsub=$nsub\n";
			return ("Species $$strptr is not connected");
		}
	}
	else { $sg->updateEdges(); }

	# Check that molecules match declared types
	if ($mtlist) {
		$err =
		  $mtlist->checkSpeciesGraph( $sg,
			{ IsSpecies => $is_species, AllowNewTypes => $AllowNewTypes } );
		if ($err) { return ($err); }
	}

    #print "parse result: ", $sg->toString(), "\n";

	$$strptr = $string_left;
	return '';
}

# Convert a pointer to a SpeciesGraph component or molecule to a label (used in XML output)
sub p_to_label {
	my $sg     = shift;
	my $p      = shift;
	my $string = shift;

	my @inds = split( '\.', $p );

	if ( $#inds == 0 ) {
		$string .= sprintf "_M%d", ( $inds[0] + 1 );
	}
	elsif ( $#inds == 1 ) {
		$string .= sprintf "_M%d_C%d", ( $inds[0] + 1 ), $inds[1] + 1;
	}
	else {
		die "Error in p_to_label";
	}

	return ($string);
}

sub labelHNauty {
	use strict;
	use Data::Dumper;
	my $sg = shift;
	
	# DEBUG
	#print STDERR "begin HNauty: ", $sg->toString(), "\n";

	# Construct adjacency matrix and partition
	my %adj       = ();
	my @partition = ();

	# Contains partitions for molecules
	my %mtypes = ();

	# Contains partitions for components
	my %ctypes = ();

	# Hash to convert pointers to node index
	my %pointer_index = ();

	# Bonds hashed by name
	my %bonds = ();

	my $n_mol = scalar( @{ $sg->Molecules } );
	my $imol  = 0;
	my $icomp = $n_mol;

# Loop over molecules and components to
# (1) determine pointer to index conversions
# (2) partition by Molecule and Componoent names (could add connectivity later, if it improves performance)
# (3) create edges from Molecules to its contained Components
# (4) collect bonds in a hash (precludes need to call update edges beforehand).
	for my $mol ( @{ $sg->Molecules } ) {
		my $mname = $mol->Name . "." . $mol->State . "." . $mol->Compartment;
		$pointer_index{$imol} = $imol;
		push @{ $mtypes{$mname} }, $imol;
		for my $edge ( @{ $mol->Edges } ) {
			push @{ $bonds{$edge} }, $imol;
		}
		my $jcomp = 0;
		for my $comp ( @{ $mol->Components } ) {
			my $cname =
			    $mname . "."
			  . $comp->Name . "."
			  . $comp->State . "."
			  . $comp->Compartment;
			my $p = "$imol.$jcomp";
			$pointer_index{$p} = $icomp;
			push @{ $ctypes{$cname} }, $icomp;
			$adj{$imol}{$icomp} = [0];
			$adj{$icomp}{$imol} = [0];
			for my $edge ( @{ $comp->Edges } ) {
				push @{ $bonds{$edge} }, $p;
			}
			++$icomp;
			++$jcomp;
		}
		++$imol;
	}

	# Loop over Edges to determine adjacency matrix for bonds
	for my $ename ( keys %bonds ) {
		my $earr   = $bonds{$ename};
		my $istart = $pointer_index{ $$earr[0] };
		my $iend   = $pointer_index{ $$earr[1] };
		$adj{$istart}{$iend} = [1];
		$adj{$iend}{$istart} = [1];
	}

	#print Dumper(\%adj),"\n";

	# Set up partitions; sort order molecules then components.
	for my $type ( sort keys %mtypes ) {
		push @partition, $mtypes{$type};

		#print " | ",join(' ',@{$mtypes{$type}});
	}
	for my $type ( sort keys %ctypes ) {
		push @partition, $ctypes{$type};

		#print " | ",join(' ',@{$ctypes{$type}});
	}

	#print "\n";
	#  print "molecules : ", join(' ',sort {$a cmp $b} keys %mtypes),"  ";
	#  print "components: ", join(' ',sort {$a cmp $b} keys %ctypes),"\n";

	# Call HNauty to obtain canonical ordering
	( my $perm ) = HNauty( \%adj, \%adj, \@partition );

	#print Dumper($perm),"\n";

	# Reorder Components of each Molecule according to canonical order
	my $icomp_start = $n_mol;
	for my $mol ( @{ $sg->Molecules } ) {
		my $icomp_end = $icomp_start + $#{ $mol->Components };
		my @perm_c = map { $perm->{$_} } ( $icomp_start .. $icomp_end );

		#print join(' ',@perm_c),"\n";
		my @comp_perm =
		  map { [ $perm_c[$_], $mol->Components->[$_] ] }
		  ( 0 .. $#{ $mol->Components } );
		@comp_perm = sort { $a->[0] <=> $b->[0] } @comp_perm;
		$mol->Components( [ map { $_->[1] } @comp_perm ] );
		$icomp_start = $icomp_end + 1;
	}

	# Reorder Molecules according to canonical order
	# This is probably inefficient since we have the exact mapping
	my @mol_perm =
	  map { [ $perm->{$_}, $sg->Molecules->[$_] ] }
	  ( 0 .. $#{ $sg->Molecules } );

	#map {print $_->[0];} @mol_perm;  print "\n";
	@mol_perm = sort { $a->[0] <=> $b->[0] } @mol_perm;
	$sg->Molecules( [ map { $_->[1] } @mol_perm ] );

	$sg->updateEdges();

	# Create exact string representation
	my $string = $sg->toString( 0, 1 );

	# Remove attributes from label.
	# No longer needed, argument passed to "toString" removed all attributes
	#  --Justin, 30 Oct 2010
	#$string =~ s/\$//;
	
	$sg->StringExact($string);
	$sg->StringID($string);
	$sg->IsCanonical(1);

    # DEBUG
	#print STDERR "end HNauty: canonical =  $string \n\n";

	return ("");
}

sub labelQuasi
{
	my $sg = shift;
	my $isCanonical = (@_) ? shift : 0;

	*molecules = $sg->Molecules;
	*edges     = $sg->Edges;
	*adjacency = $sg->Adjacency;

	# Sort components of each molecule
	for my $mol (@molecules)
	{
		*components = $mol->Components;
		@components = sort by_component @components;
	}

	# Sort molecules
	@molecules = sort by_molecule @molecules;

	$sg->updateEdges();

	# Create quasi-canonical string representation
	my $string = $sg->toString( 1, 1 );
	$string =~ s/^{.*}//;
	$sg->StringID($string);

	# Create exact string representation
	$string = $sg->toString( 0, 1 );

	# Remove attributes from label.
	# No longer needed, argument passed to "toString" removed all attributes
	#  --Justin, 30 Oct 2010
	#$string =~ s/\$//;
	
	$sg->StringExact($string);
	$sg->IsCanonical($isCanonical);

	return ('');
}

# Modified version using different labeling functions
sub sortLabel
{ 
	my $sg = shift;

	if ( $SpeciesLabel eq "Auto" ) {
		return ( $sg->labelQuasi(0) );
	}
	elsif ( $SpeciesLabel eq "Quasi" ) {
		return ( $sg->labelQuasi(1) );    # Equivalent to setting check_iso=>0
	}
	elsif ( $SpeciesLabel eq "HNauty" ) {
		return ( $sg->labelHNauty() );
	}
}

sub assignCompartment

 # $err = $sg->assignCompartment()
 # $err = $sg->assignCompartment( $force_comp )
 #
 # Calls inferSpeciesCompartment to determine Species Compartment and then makes
 # the assignment, if valid.  Returns an error string if there are any problems.
 # Note that the assignment is allowed to be undefined!
 #
 # If $force_comp is supplied, the SpeciesGraph is forced to that compartment,
 #  unless it is incompatible with any molecule compartments.
 #
 # Note (Justin): separated assign and infer Compartment into two methods.
{
	my $sg = shift;                                # SpeciesGraph
	my $force_comp = scalar(@_) ? shift : undef;

	# get Species Compartment (return any errors)
	my ( $species_comp, $err ) = $sg->inferSpeciesCompartment();
	return $err if ($err);

	if ( defined $force_comp and defined $species_comp ) {
		unless ( $species_comp == $force_comp ) {
			return
			  sprintf
"Attempt to force incompatible species compartment on SpeciesGraph %s.",
			  $sg->toString();
		}
	}
	elsif ( defined $force_comp and !defined($species_comp) ) {
		$species_comp = $force_comp;
	}
	elsif ( !defined $force_comp and !defined($species_comp) ) {

		# Currently nothing to do here!

# in strict mode: generate error
# return sprintf "Species Compartment for SpeciesGraph %s could not be determined.",
#                $sg->toString();
		return '';
	}

	# Assign Compartment
	$sg->Compartment($species_comp);

# Set Compartment of any Molecules without explicit Compartment definitions.
# NOTE: This handles compartment specification when syntax "@C:Species" is used.
	for my $mol ( @{ $sg->Molecules } ) {
		unless ( defined $mol->Compartment ) {
			$mol->Compartment($species_comp);
		}
	}
	return '';
}

sub inferSpeciesCompartment

# (Compartment, err) = $sg->inferCompartment()
#
# Infers Species Compartment of a SpeciesGraph.  Returns an undefined Compartment
# if the Compartment cannot be inferred or is invalid.  Sets err=1 if Species
# Compartment is invalid and err=0 otherwise.
#
# There is no check for bond validity here!  SHould be added in future.
{
	my $sg = shift;

	my $inferred_comp;    # Species Compartment inferred from molecules
	my %volumes  = ();    # molecule volume compartments found in $sg
	my %surfaces = ();    # molecule surface compartments found in $sg
	my $err = '';  # return error (set string if species compartment is invalid)

	# Gather molecule compartments
	for my $mol ( @{ $sg->Molecules } ) {
		my $comp = $mol->Compartment;

		# fixed BUG: if compartment molecule isn't explictly defined,
		# transfer the explicit species compartment (if any)  --justin
		if ( !( defined $comp ) and defined $sg->Compartment ) {
			$comp = $sg->Compartment;
		}

		next unless ( defined $comp );

		if    ( $comp->SpatialDimensions == 2 ) { $surfaces{$comp} = $comp; }
		elsif ( $comp->SpatialDimensions == 3 ) { $volumes{$comp}  = $comp; }
	}

	my $n_surfaces = scalar( keys %surfaces );
	my $n_volumes  = scalar( keys %volumes );

	# infer Species Compartment
	if ( $n_surfaces == 0 ) {
		if ( $n_volumes == 0 ) {    # no inferred compartment
			$inferred_comp = undef;
		}
		elsif ( $n_volumes == 1 ) {    # unique volume compartment is inferred
			($inferred_comp) = ( values %volumes );
		}
		else                           #( $n_volumes > 1 )
		{                              # error: multiple volumes with no surface
			$err =
			  sprintf
			  "Molecule Compartments of %s define invalid Species Compartment.",
			  $sg->toString();
			return ( undef, $err );
		}
	}
	elsif ( $n_surfaces == 1 ) {       # unique surface compartment is inferred
		($inferred_comp) = ( values %surfaces );

		# check adjacency of volumes
		foreach my $comp ( values %volumes ) {
			unless ( $inferred_comp->adjacent($comp) )
			{                          # error: volume not adjacent to surface
				$err =
				  sprintf
"Molecule Compartments of %s define invalid Species Compartment.",
				  $sg->toString();
				return ( undef, $err );
			}
		}
	}
	else                               #( $n_surfaces > 1 )
	{                                  # error: multiple surfaces
		$err =
		  sprintf "Molecule Compartments of %s include more than 1 Surface.",
		  $sg->toString();
		return ( undef, $err );
	}

# Check that inferred Species Compartment assignment is Compatible with any explicit declaration
	if ( defined $sg->Compartment and defined $inferred_comp ) {
		unless ( $inferred_comp == $sg->Compartment ) {
			$err = sprintf "Explicit Species Compartment (%s)"
			  . " is not compatible with inferred Species Compartment (%s)",
			  $sg->Compartment->Name, $inferred_comp->Name;
			return ( undef, $err );
		}
	}

	# if no compartment was inferred, then use explicit compartment (if any)
	if ( defined $sg->Compartment and !( defined $inferred_comp ) ) {
		$inferred_comp = $sg->Compartment;
	}

	# return inferred compartment
	return ( $inferred_comp, $err );
}

sub interactingSet

# bool = interactingSet ( $sg1, $sg2, .. )
#
# determine if list of species graphs forms an interacting Set.  Return 1 if
# yes, and 0 otherwise.
#
# NOTE: if all species have undefined compartment, then Return 1.  But return 0
# if only some of the species have undefined compartment.  This lets us run
# compartmentBNG in a "sloppy" mode where compartments may or may not be defined.
{
	my @sgs = @_;

	my $volume  = undef;    # volume compartment
	my $surface = undef;    # surface compartment

	# find surface and volume compartments
	my $missing = 0;        # track species without compartment
	for my $sg (@sgs) {
		my $comp = $sg->Compartment;
		unless ( defined $comp ) {    # species has undefined compartment
			$missing++;
			next;
		}

		if ( $comp->SpatialDimensions == 2 ) {   # this compartment is a surface
			if ( defined $surface )
			{    # Surface found previously.  This surface should be the same.
				return 0 unless ( $surface == $comp );
			}
			else {    # No surface found previously.
				$surface = $comp;
			}
		}
		elsif ( $comp->SpatialDimensions == 3 ) { # this compartment is a volume
			if ( defined $volume )
			{    # Volume found previously.  This volume should be the same.
				return 0 unless ( $volume == $comp );
			}
			else {    # No volume found previously.
				$volume = $comp;
			}
		}
	}

	if ( defined $surface and defined $volume )
	{                 # surface and volume must be adjacent
		return 0 unless ( $surface->adjacent($volume) );
	}

	if ( $missing and ( $missing < @sgs ) )
	{                 # mixture of reactants with and without compartment
		return 0;
	}

	# species are an interacting set!
	return 1;
}

sub find_compartment_connected

 # @compartment_connected = $sg->find_compartment_connected( $i_mol )
 #
 # find the set of molecules in $sg that are connected to the molecule $i_mol by
 # a path contained in the same compartment as $i_mol.  Returns a list of
 # indexes corresponding to the molecules in the set.
{
	my ( $sg, $i_mol ) = @_;

	# get compartment of reference molecule
	my $comp = $sg->Molecules->[$i_mol]->Compartment;
	return () unless ( defined $comp );

	# build a hash that maps each molecule in compartment to the set
	# of adjacent vertices (also in the compartment)
	my $adjacency_hash = {};
	foreach my $edge ( @{ $sg->Edges } ) {
		my ( $mol1, $mol2 ) = ( $edge =~ /^(\d+)\.\d+\s+(\d+)\.\d+/ );
		if (    $sg->Molecules->[$mol1]->Compartment == $comp
			and $sg->Molecules->[$mol2]->Compartment == $comp )
		{
			push @{ $adjacency_hash->{$mol1} }, $mol2;
			push @{ $adjacency_hash->{$mol2} }, $mol1;
		}
	}

# initialize hash map for collection of molecules in the compartment connected component
	my $compartment_connected = {};

# find the compartment connected component using a depth first search (recursive)
	&depth_first_search( $i_mol, $adjacency_hash, $compartment_connected );
	return ( keys %$compartment_connected );

	sub depth_first_search

# recursive method to find connected component of graph
#  vertex:  index of the starting vertex.
#  adjacency:  reference to hash that maps vertices to the set of adjacent vertices.
#  connected_component:  reference to a hash whose keys are vertices in the connected component.
	{
		my ( $vertex, $adjacency, $connected_component ) = @_;

		# do nothing if vertex was already found
		return if ( exists $connected_component->{$vertex} );

		# vertex is in connected component
		$connected_component->{$vertex} = 1;

		# search edges at this vertex
		foreach my $adjacent_vertex ( @{ $adjacency->{$vertex} } ) {
			&depth_first_search( $adjacent_vertex, $adjacency,
				$connected_component );
		}
	}
}

# Can be used to create a copy of a single graph or combine multiple graphs into a single graph
# UPDATE: eliminated redundant copy of molecules by treating msnew as a reference to an array.
#  --justin  17apr09   [further remark, this didn't change much.. maybe Perl was implicitly
# doing a pass by reference?
sub copy {
	my @sgs     = @_;
	my $sg_copy = SpeciesGraph->new();

	my $n_edges = 0;
	my $msnew   = [];
	my $isg     = 1;

	my $sg;
	my $mol;
	foreach $sg (@sgs) {
		foreach $mol ( @{ $sg->Molecules } ) {

			# This form of Molecule->copy adds a prefix to the edges to avoid
			# overlap of edge names between different SpeciesGraphs
			push @$msnew, $mol->copy("${isg}_");
		}
		++$isg;
	}    # END loop over SpeciesGraphs
	$sg_copy->Molecules($msnew);
	$sg_copy->updateEdges();
	return ($sg_copy);
}

sub copySubgraph {
	my $sg       = shift;
	my $subgraph = shift;
	my $nsub     = shift;
	my $sg_copy  = SpeciesGraph->new();

	my $n_edges = 0;
	my @msnew   = ();
	*molecules = $sg->Molecules;
	my %enames = ();
	for my $imol ( 0 .. $#molecules ) {
		next unless ( $$subgraph[$imol] == $nsub );
		push @msnew, $molecules[$imol]->copy();
	}
	$sg_copy->Molecules( [@msnew] );
	$sg_copy->updateEdges();
	return ($sg_copy);
}

sub stoich {
	my $sg    = shift;
	my $mname = shift;
	my $count = 0;

	for my $mol ( @{ $sg->Molecules } ) {
		next if ( $mname cmp $mol->Name );
		++$count;
	}
	return ($count);
}

sub updateEdges

# this method matches speciesGraph edges by labels, then sorts them, numbers them,
# and stores the list at @{$sg->Edges}.  An adjacency hash is also created
# and stored at @{$sg->Adjacency}.
#
# Notes:  edge array format = "m1.c1 m2.c2"
#         adjacency hash keys = {m1.c1}{m2.c2}
#         Molecules and Components should be pre-sorted before calling this routine.
#
# Q: why are edges sorted?
#
# ***********************************************************************************
# Debug status: this code was streamlined at r87, but there could be lingering bugs!!
#  --justin
#
# UPDATE: fixed bug that overlooked edges that goes to $m2 if $2m2 has index 0.
#  --justin   30 Apr 2010
# ***********************************************************************************

{
	my $sg = shift;
	my $AllowDangling = (@_) ? shift : 1;

	# Update edges
	# Loop over molecules and components again to set pointers based
	# on sorted positions
	my $ledges = {};
	my $imol = 0;
	foreach my $mol ( @{$sg->Molecules} )
	{
		my $icomp = 0;
		foreach my $comp ( @{$mol->Components} )
		{
			# Set pointers for edge entries of each component
			my $wildcard = [];
			
			foreach my $elabel ( @{ $comp->Edges } )
			{
				if ( $elabel =~ /^[*+?]$/ )
				{   push @$wildcard, $elabel;   }
				else
				{   push @{$ledges->{$elabel}}, $imol, $icomp;   }
			}

			# This information will replaced when adjacency hash is updated
			$comp->Edges($wildcard);

			$icomp++;
		}    # END loop over components

		$imol++;
	}    # END loop over molecules


	# create array to hold edges
	my $edges = [];
	$sg->Edges($edges);

	# create adjacency hash
	my $adjacency = {};
	$sg->Adjacency($adjacency);

	my $iedge = 0;
	foreach my $edge ( sort edge_sort values %$ledges )
	{
		my $p1, $p2;
		my ( $m1, $c1, $m2, $c2 ) = @$edge;

		$p1 = "$m1.$c1";
		# BUG FIXED HERE: returned false if $m2 has index 0.  replaced if($m2)
		#  with if (defined $m2)
		if (defined $m2)
		{
			$p2                    = "$m2.$c2";
			$adjacency->{$p1}{$p2} = $iedge;
			$adjacency->{$p2}{$p1} = $iedge;
			push @{ $sg->Molecules->[$m1]->Components->[$c1]->Edges }, $iedge;
			push @{ $sg->Molecules->[$m2]->Components->[$c2]->Edges }, $iedge;
			push @$edges, "$p1 $p2";
		}
		elsif ($AllowDangling)
		{
			$adjacency->{$p1} = $iedge;
			push @{ $sg->Molecules->[$m1]->Components->[$c1]->Edges }, $iedge;
			push @$edges, "$p1";
		}

		$iedge++;
	}
	
	#print "edges: ", join(',', @$edges), "\n";
	#print "update_edges: ", $sg->toString(), "\n";
	return $sg;
}





sub toString {
	# get this species graph
	my $sg = shift;
	# get arguments
	my $suppress_edge_names = (@_) ? shift : 0;   # if true, egde labels and species attributes are omitted from the string
	my $suppress_attributes = (@_) ? shift : 0;   # if true, species attributes are omitted (use this for Canonical labeling!!)
	
	# initialize string
	my $string              = '';

	# header
	# NOTE: printing name messes up use of StringExact for hashing species
	#if ($sg->Name){
	#$string.=$sg->Name;
	#}
	if ( $sg->Label ) {
		$string .= '%' . $sg->Label;
	}
	if ( $sg->Compartment ) {
		$string .= '@' . $sg->Compartment->Name;
	}
	if ($string) {
		$string .= "::";
	}

	# attributes
	# (suppression is required for generating canonical labels!)
	unless ( $suppress_edge_names  or  $suppress_attributes ) {
		my @attr = ();
		if ( $sg->MatchOnce ) {
			push @attr, "MatchOnce";
		}
		# put additional attributes here!
		if (@attr) {
			$string .= '{' . join( ',', @attr ) . '}';
		}
		# Handle fixed by prepending '$'
		if ( $sg->Fixed ) {
			$string .= '$';
		}
	}

	$imol = 0;
	for my $mol ( @{ $sg->Molecules } ) {
		if ($imol) {
			$string .= ".";
		}
		$string .= $mol->toString( $suppress_edge_names, $sg->Compartment );
		++$imol;
	}

	$string .= $sg->Quantifier;
	return ($string);
}

sub toStringSSC {
	my $sg                  = shift;
	my $suppress_edge_names = (@_) ? shift : 0;
	my $string              = "";
	$imol = 0;

	# attributes
	if ( $sg->MatchOnce ) {
		print STDOUT"\n WARNING: SSC does not implement MatchOnce. Though this rule has been translated, ";
		print STDOUT"\n          Please remove any usage of MatchOnce, as otherwise SSC will not compile the rule. ";
		print STDOUT"\n          See .rxn file for more details. ";
	}

	for my $mol ( @{ $sg->Molecules } ) {
		if ($imol) {
			$string .= "";
		}
		( my $tempstring, my $checkSameComp ) = $mol->toStringSSC();
		$string .= $tempstring;
		++$imol;
	}
	if ( $checkSameComp != 0 ) { return ( $string, $checkSameComp ); }
	return ( $string, 0 );
}

# this toString is just used in corresponding seed species block.
# As in SSC one only specifies molecules, molecules if they hava a defined states
# Or molecules with bonds.
sub toStringSSCMol {
	my $sg                  = shift;
	my $suppress_edge_names = (@_) ? shift : 0;
	my $string              = "";
	$imol = 0;
	for my $mol ( @{ $sg->Molecules } ) {
		if ($imol) {
			$string .= "";
		}
		$string .= $mol->Name;
		$string .= $mol->toStringSSCMol($suppress_edge_names)
		  ;    #Calls toStringSSCMol of Molecule.pm

		++$imol;
	}
	return ($string);
}

# NOTE: Doesn't return enclosing contained because it is assumed that this is called from specific
#       class containing a SpeciesGraph, e.g. Species, or Reactants or Products in RxnRule.

sub toXML {
	my $sg         = shift;
	my $indent     = shift;
	my $type       = shift;
	my $id         = shift;
	my $attributes = shift;

	my $string = $indent . "<$type";

	# Attributes
	# id
	$string .= " id=\"" . $id . "\"";

	# other attributes
	if ( $attributes ne "" ) { $string .= " " . $attributes; }

	# Label
	if ( $sg->Label ne "" ) {
		$string .= " label=\"" . $sg->Label . "\"";
	}

	# Compartment
	if ( $sg->Compartment ) {
		$string .= " compartment=\"" . $sg->Compartment->Name . "\"";
	}

	# add support for MatchOnce keyword
	if ( $sg->MatchOnce ) {
		$string .= ' matchOnce="1"';
	}

	# add support for Fixed
	if ( $sg->Fixed ) {
		$string .= ' Fixed="1"';
	}

	# add support for quantifiers
	# --justin  29may09
	if ( $sg->Quantifier ) {
		my ( $relation, $quantity ) =
		  ( $sg->Quantifier =~ /(=|==|<=|>=|<|>)(\d+)/ );
		$string .= ' relation="' . $relation . '" quantity="' . $quantity . '"';
	}

	# Objects contained
	my $indent2 = "  " . $indent;
	my $ostring = "";

	# Molecules
	if ( @{ $sg->Molecules } ) {
		$ostring .= $indent2 . "<ListOfMolecules>\n";
		my $index = 1;
		for my $mol ( @{ $sg->Molecules } ) {
			$ostring .= $mol->toXML( "  " . $indent2, $id, $index );
			++$index;
		}
		$ostring .= $indent2 . "</ListOfMolecules>\n";
	}

	# Bonds
	if ( @{ $sg->Edges } ) {
		my $bstring = "";
		my $index   = 1;
		my $indent3 = "  " . $indent2;
		for my $edge ( @{ $sg->Edges } ) {
			my ( $p1, $p2 ) = split( ' ', $edge );
			next
			  unless $p2 != ""
			; # Only print full bonds; half-bonds handled by BindingState variable in Components
			my $bid = sprintf "${id}_B%d", $index;
			$bstring .= $indent3 . "<Bond";
			$bstring .= " id=\"" . $bid . "\"";
			$bstring .= " site1=\"" . $sg->p_to_label( $p1, $id ) . "\"";
			$bstring .= " site2=\"" . $sg->p_to_label( $p2, $id ) . "\"";
			$bstring .= "/>\n";
			++$index;
		}
		if ($bstring) {
			$ostring .=
			    $indent2
			  . "<ListOfBonds>\n"
			  . $bstring
			  . $indent2
			  . "</ListOfBonds>\n";
		}
	}

	# Termination
	if ($ostring) {
		$string .= ">\n";                    # terminate tag opening
		$string .= $ostring;
		$string .= $indent . "</$type>\n";
	}
	else {
		$string .= "/>\n";                   # short tag termination
	}

	return ($string);
}

sub addEdge {
	my $sg    = shift;
	my $ename = shift;

	*molecules = $sg->Molecules;

	for my $p (@_) {
		my ( $im, $ic ) = split( '\.', $p );
		push @{ $molecules[$im]->Components->[$ic]->Edges }, $ename;
	}

	return $sg;
}

sub deleteEdge {
	my $sg = shift;
	my $p1 = shift;
	my $p2 = shift;

	*molecules = $sg->Molecules;
	*adjacency = $sg->Adjacency;

	return unless defined( $adjacency{$p1}{$p2} );
	my $ename = $adjacency{$p1}{$p2};

	my $ndrop = 0;
	for my $p ( $p1, $p2 ) {
		my ( $im, $ic ) = split( '\.', $p );
		*cedges = $molecules[$im]->Components->[$ic]->Edges;
		for my $ie ( 0 .. $#cedges ) {
			if ( $cedges[$ie] eq $ename ) {
				splice @cedges, $ie, 1;
				++$ndrop;
				last;
			}
		}
	}

	if ( $ndrop != 2 ) {
		exit_error("deleteEdge acted $ndrop times instead of the correct 2");
	}

	return $sg;
}

# Do breadth first search to split graph into connected subgraphs. Returns
# array of arrays containing indices of molecules in distinct connected
# subgraphs contained in the graph.
sub findConnected {
	my $sg = shift;

	*molecules = $sg->Molecules;
	*edges     = $sg->Edges;
	*adjacency = $sg->Adjacency;

	my @reached_total = (0) x @molecules;
	my @subgraphs     = ();
	while (1) {

		# Find first molecule that hasn't been included in a subgraph
		my $istart = -1;
		for my $i ( 0 .. $#reached_total ) {
			if ( $reached_total[$i] == 0 ) {
				$istart = $i;
				last;
			}
		}
		last if ( $istart < 0 );

		# Start search from first unreached molecule
		my @reached = (0) x @molecules;
		$reached[$istart] = 1;
		my @mol_new = ($istart);

		#    print "istart=$istart\n";
		while (@mol_new) {
			my @mol_next = ();

			# Loop over molecules in mol_new
			for my $imol (@mol_new) {
				my $mol = $molecules[$imol];

				# Loop over components in molecule
				for my $icomp ( 0 .. $#{ $mol->Components } ) {
					my $p = "$imol.$icomp";

					# Loop over edges from component
					for my $q ( keys %{ $adjacency{$p} } ) {
						my ( $im, $ic ) = split( '\.', $q );
						if ( !$reached[$im] ) {
							$reached[$im] = 1;
							push @mol_next, $im;
						}
					}
				}
			}
			@mol_new = @mol_next;
		}
		for my $i ( 0 .. $#reached ) {
			$reached_total[$i] |= $reached[$i];
		}

		push @subgraphs, [@reached];
	}
	return (@subgraphs);
}

# Modified version that returns n_subgraphs
# and single array containing index of subgraph
# to which molecule with each index corresponds
sub findConnected2 {
	my $sg = shift;

	*molecules = $sg->Molecules;
	*edges     = $sg->Edges;
	*adjacency = $sg->Adjacency;

	my @subgraph   = (0) x @molecules;
	my $n_subgraph = 0;
	while (1) {

		# Find first molecule that hasn't been included in a subgraph
		my $imol_start = -1;
		for my $imol ( 0 .. $#subgraph ) {
			if ( $subgraph[$imol] == 0 ) {
				$imol_start = $imol;
				last;
			}
		}
		last if ( $imol_start < 0 );
		++$n_subgraph;

		# Start search from first unreached molecule
		$subgraph[$imol_start] = $n_subgraph;
		my @mol_new = ($imol_start);
		while (@mol_new) {
			my @mol_next = ();

			# Loop over molecules in mol_new
			for my $imol (@mol_new) {
				my $mol = $molecules[$imol];

				# Loop over components in molecule
				for my $icomp ( 0 .. $#{ $mol->Components } ) {
					my $p = "$imol.$icomp";

					# Loop over edges from component
					for my $q ( keys %{ $adjacency{$p} } ) {
						my ( $im, $ic ) = split( '\.', $q );
						if ( !$subgraph[$im] ) {
							$subgraph[$im] = $n_subgraph;
							push @mol_next, $im;
						}
					}
				}
			}
			@mol_new = @mol_next;
		}

		#print join(" ",@subgraph),"\n";
	}
	return ( $n_subgraph, [@subgraph] );
}

# Determine if SpeciesGraph is isomorphic to another SpeciesGraph
# Assume that molecules have already been sorted by molecule name and component state
# using cmp_molecule and cmp_component and cmp_edge
sub isomorphicTo {
	my $sg1 = shift;
	my $sg2 = shift;

	if ( $sg1->StringID cmp $sg2->StringID ) {

		#print "Failed string test\n";
		return (0);
	}

	# Check for exact match
	if ( !( $sg1->StringExact cmp $sg2->StringExact ) ) {

		#    print "Exact match\n";
		return (1);
	}

	# If string is canonical, we're done
	#  else {
	#    return(0);
	#  }

	#  printf "Doing isomorphism check...";

	# Nested depth first search, first molecules, then components to find
	# match
	*molecules1 = $sg1->Molecules;
	*molecules2 = $sg2->Molecules;

	my @maps   = ();
	my $nmol   = $#molecules1;
	my @mused  = (0) x @molecules1;
	my @mptr   = (0) x @molecules1;
	my $im1    = 0;
	my @cptrs  = ();
	my @cuseds = ();
	*components1 = $molecules1[$im1]->Components;
	my $im2, $ic1, $ic2, $ncomp = $#components1;

	*adj1 = $sg1->Adjacency;
	*adj2 = $sg2->Adjacency;

	# depth first search over Molecules
  MITER:
	while (1) {

		# find a match at the current level
		# Currently loop is done over all possible molecules, but this could be
		# changed to loop over molecules adjacent to molecules higher level to
		# limit search.
		my $mmatch = 0;

		#   for $im2 ($mptr[$im1]..$nmol){
		for ( $im2 = $mptr[$im1] ; $im2 <= $nmol ; ++$im2 ) {
			next if $mused[$im2];    # Continue if this molecule already mapped
			next if ( $molecules1[$im1]->Name cmp $molecules2[$im2]->Name );
			next
			  if ( $molecules1[$im1]->Compartment !=
				$molecules2[$im2]->Compartment );

			# Initialize data for component match at this level
			$mptr[$im1]   = $im2;
			*components2  = $molecules2[$im2]->Components;
			$cptrs[$im1]  = [ (0) x @components1 ];
			$cuseds[$im1] = [ (0) x @components2 ];
			$ic1          = 0;
			$mmatch       = 1;
			last;
		}

		# Move up a level (to last component of molecule at previous level)
		# if no match molecules found
		if ( !$mmatch ) {
			last MITER if ( $im1 == 0 );

			# Reset molecule pointer at current level
			$mptr[$im1] = 0;
			--$im1;
			*components1 = $molecules1[$im1]->Components;
			$ncomp       = $#components1;
			$ic1         = $ncomp;
			$im2         = $mptr[$im1];
			++$cptrs[$im1][$ic1];
			$mused[ $mptr[$im1] ] = 0;
			*components2 = $molecules2[$im2]->Components;

			#      print "set im1 to $im1  $cptrs[$im1][$ic1]\n";
		}

		# Do depth first search over components of molecule 2
		*cptr  = $cptrs[$im1];
		*cused = $cuseds[$im1];
	  CITER:
		while (1) {
			$cmatch = 0;
			for ( $ic2 = $cptr[$ic1] ; $ic2 <= $ncomp ; ++$ic2 ) {
				next if $cused[$ic2];
				if ( $components1[$ic1]->compare_local( $components2[$ic2] ) ) {
					next;
				}

				#Check component edges
				my $ematch = 1;
				$p1 = "$im1.$ic1";
				$p2 = "$im2.$ic2";
			  EDGE:
				for my $q1 ( keys %{ $adj1{$p1} } ) {
					my ( $jm1, $jc1 ) = split( '\.', $q1 );
					next if ( $jm1 > $im1 );
					if ( $jm1 == $im1 ) {
						next if ( $jc1 >= $ic1 );

#	  exit_error("isomorphicTo can't handle bonds among components of same molecule");
					}
					my $q2 = "$mptr[$jm1].$cptrs[$jm1][$jc1]";
					if ( !defined( $adj2{$p2}{$q2} ) ) {

						#	  print "Failed adjacency: $p1 $q1 $p2 $q2\n";
						$ematch = 0;
						last EDGE;
					}
				}
				next unless ($ematch);

				$cptr[$ic1] = $ic2;

				# Complete mapping of this molecule if $ic1==$ncomp
				if ( $ic1 == $ncomp ) {
					$cmatch = 1;
					last;
				}
				else {

					# descend to next component
					$cused[$ic2] = 1;
					++$ic1;

					#	print "ic1=$ic1 cptr=$cptr[$ic1] ncomp=$ncomp\n";
					next CITER;
				}
			}

			# Move up a component level if no match found
			if ( !$cmatch ) {

				# Move to next molecule at current level if up exhausted
				# component search
				if ( $ic1 == 0 ) {

					# Increment molecule pointer at current level
					++$mptr[$im1];

					#	print "set im2 to $mptr[$im1] ", join(' ',@mused), "\n";
					next MITER;
				}

				# Reset component pointer at current level
				$cptr[$ic1] = 0;
				--$ic1;
				$cused[ $cptr[$ic1] ] = 0;    # Reset pointers at new level
				++$cptr[$ic1];

				#      print "set ic1 to $ic1 cptr is $cptr[$ic1]\n";
				next CITER;
			}
			last CITER;
		}    # END CITER

		# If $im1==$nmol, then graphs are isomorhpic and we can return
		if ( $im1 == $nmol ) {
			return (1);
		}

		# Move down a level in molecules (increment $im1)
		$mused[$im2] = 1;
		++$im1;
		*components1 = $molecules1[$im1]->Components;
		$ncomp       = $#components1;
	}    # END MITER

	#print "Failed full isomorphism test\n";
	return (0);
}

# Determine if SpeciesGraph is isomorphic
# to a portion of another SpeciesGraph
sub isomorphicToSubgraph {

    #DEBUG
    #print STDERR "SpeciesGraph::isomorphicToSubgraph  begin\n";
    #my $time0 = time;
   
	my $sg1       = shift;
	my $MatchOnce = $sg1->MatchOnce;

	*molecules1 = $sg1->Molecules;
	*edges1     = $sg1->Edges;
	*adj1       = $sg1->Adjacency;
	my @maps = ();

  GRAPH: while ( my $sg2 = shift ) {
		*molecules2 = $sg2->Molecules;

		# Number of molecules
		if ( scalar(@molecules1) > scalar(@molecules2) ) {
			next;
		}

		# Check that number of edges is same
		*edges2 = $sg2->Edges;
		if ( scalar(@edges1) > scalar(@edges2) ) {
			next;
		}

		# Check that in same compartment
		if ( $sg1->Compartment ) {
			next if ( $sg1->Compartment != $sg2->Compartment );
		}

		# Nested depth first search, first molecules, then components to find
		# match

		my $nmol1  = $#molecules1;
		my $nmol2  = $#molecules2;
		my @mptr   = (0) x @molecules1;
		my @mused  = (0) x @molecules2;
		my $im1    = 0;
		my @cptrs  = ();
		my @cuseds = ();
		*components1 = $molecules1[$im1]->Components;
		my $im2, $ic1, $ic2, $ncomp1 = $#components1;
		my $ci1, $ci2;

		*adj2 = $sg2->Adjacency;

		# depth first search over Molecules
	  MITER:
		while (1) {

		 # find a match at the current level
		 # Currently loop is done over all possible molecules, but this could be
		 # changed to loop over molecules adjacent to molecules higher level to
		 # limit search.
			my $mmatch = 0;
			for ( $im2 = $mptr[$im1] ; $im2 <= $nmol2 ; ++$im2 ) {
				next if $mused[$im2]; # Continue if this molecule already mapped
				my $namestring = $molecules1[$im1]->Name;
				$namestring =~ s/\*$/.*/;

				#print "namestring=$namestring\n";
				next unless ( $molecules2[$im2]->Name =~ /^${namestring}$/ );

				#next if ($molecules1[$im1]->Name cmp $molecules2[$im2]->Name);
				if ( $molecules1[$im1]->Compartment ) {
					next
					  if ( $molecules1[$im1]->Compartment !=
						$molecules2[$im2]->Compartment );
				}

	  #print "$im1 $im2: $molecules1[$im1]->Name -> $molecules2[$im2]->Name\n";;
	  # Initialize data for component match at this level
				$mptr[$im1]   = $im2;
				*components2  = $molecules2[$im2]->Components;
				$cptrs[$im1]  = [ (0) x @components1 ];
				$cuseds[$im1] = [ (0) x @components2 ];
				$ic1 = ( $ncomp1 >= 0 ) ? 0 : -1;
				$mmatch = 1;
				last;
			}

			# Move up a level (to last component of molecule at previous level)
			# if no match molecules found
			if ( !$mmatch ) {
				last MITER if ( $im1 == 0 );

				# Reset molecule pointer at current level
				$mptr[$im1] = 0;
				--$im1;
				*components1          = $molecules1[$im1]->Components;
				$ncomp1               = $#components1;
				$ic1                  = $ncomp1;
				$im2                  = $mptr[$im1];
				$mused[ $mptr[$im1] ] = 0;
				if ( $ic1 >= 0 ) {
					++$cptrs[$im1][$ic1];
				}
				else {
					++$mptr[$im1];
					next MITER;
				}
				*components2 = $molecules2[$im2]->Components;

				#print "set im1 to $im1  $cptrs[$im1][$ic1]\n";
			}

			# Do depth first search over components of molecule 2
			*cptr  = $cptrs[$im1];
			*cused = $cuseds[$im1];
		  CITER:
			while (1) {
				if ( $ncomp1 >= 0 ) {
					$ci1    = $components1[$ic1];
					$ncomp2 = $#components2;
					$cmatch = 0;
					for ( $ic2 = $cptr[$ic1] ; $ic2 <= $ncomp2 ; ++$ic2 ) {

						#      print "ic2=$ic2\n";
						next if $cused[$ic2];
						$ci2 = $components2[$ic2];

						# Component name
						next if ( $ci1->Name cmp $ci2->Name );

						# Component state only if present in sg1
						if ( defined( $ci1->State ) ) {
							if ( $ci1->State =~ /[*+?]/ ) {
								if ( $ci1->State eq '+' ) {
									next if ( $ci2->State eq '' );
								}
							}
							else {
								next if ( $ci1->State cmp $ci2->State );
							}
						}
						if ( $ci1->Compartment ) {
							next if ( $ci1->Compartment != $ci2->Compartment );
						}

						#      print " $ic1 $ic2 $ci1->Name -> $ci2->Name\n";

		 # Number of component edges must match (primarily used to look for free
		 # binding sites
		 # Number of edges
						my $diff = $#{ $ci2->Edges } - $#{ $ci1->Edges };
						if ($diff) {

							# Mismatch unless first Edge is wildcard
							my $wild = $ci1->Edges->[0];
							next unless ( $wild =~ /^[*+?]$/ );

						   # + wildcard requires $diff>=0 (= case handled above)
							if ( $wild eq '+' ) {
								next unless ( $diff > 0 );
							}
							else {

# *? (equivalent) wildcard requires $diff>-1, #c2 edges >= #c1 edges - 1 (for wildcard)
								next unless ( $diff >= -1 );
							}
						}

						#Check component edges
						my $ematch = 1;
						$p1 = "$im1.$ic1";
						$p2 = "$im2.$ic2";
					  EDGE:
						for my $q1 ( keys %{ $adj1{$p1} } ) {
							my ( $jm1, $jc1 ) = split( '\.', $q1 );
							next if ( $jm1 > $im1 );
							if ( $jm1 == $im1 ) {
								next if ( $jc1 >= $ic1 );
							}
							my $q2 = "$mptr[$jm1].$cptrs[$jm1][$jc1]";
							if ( !defined( $adj2{$p2}{$q2} ) ) {

								#print "Failed adjacency: $p1 $q1 $p2 $q2\n";
								$ematch = 0;
								last EDGE;
							}
						}
						next unless ($ematch);

						$cptr[$ic1] = $ic2;

						# Complete mapping of this molecule if $ic1==$ncomp1
						if ( $ic1 == $ncomp1 ) {
							$cmatch = 1;
							last;
						}
						else {

							# descend to next component
							$cused[$ic2] = 1;
							++$ic1;

						   #	print "ic1=$ic1 cptr=$cptr[$ic1] ncomp1=$ncomp1\n";
							next CITER;
						}
					}
				}
				else {

					# No components in pattern
					$cmatch = 1;
				}

				# Move up a component level if no match found
				if ( !$cmatch ) {

					# Move to next molecule at current level if up exhausted
					# component search
					if ( $ic1 <= 0 ) {

						# Increment molecule pointer at current level
						++$mptr[$im1];

				#print "set im2 to $mptr[$im1] $nmol1 ", join(' ',@mused), "\n";
						next MITER;
					}

					# Reset component pointer at current level
					$cptr[$ic1] = 0;
					--$ic1;
					$cused[ $cptr[$ic1] ] = 0;    # Reset pointers at new level
					++$cptr[$ic1];

					#      print "set ic1 to $ic1 cptr is $cptr[$ic1]\n";
					next CITER;
				}

				# If $im1==$nmol, then graphs are isomorhpic and we can return
				# Modify to save map for subgraph isomorphism case
				if ( $im1 == $nmol1 ) {
					my $map = Map->new;
					$map->Source($sg1);
					$map->Target($sg2);
					my %mapf = ();
					for my $im ( 0 .. $nmol1 ) {
						my $im2 = $mptr[$im];
						$mapf{$im} = $im2;
						*cptr = $cptrs[$im];

						#print "$im -> $im2\n";
						for my $ic ( 0 .. $#cptr ) {
							$mapf{"$im.$ic"} = "$im2.$cptr[$ic]";

							#print "$im.$ic -> $im2.$ic2\n";
						}
					}
					$map->MapF( {%mapf} );
					push @maps, $map;
					if ($MatchOnce) {
						next GRAPH;
					}
					if ( $ncomp1 >= 0 ) {
						++$cptr[$ic1];
						next CITER;
					}
					else {

				  # Go to next molecule if no components in the current molecule
				  #print " ncomp1=$ncomp1 im1=$im1 $mptr[$im1]\n";
						++$mptr[$im1];
						next MITER;
					}
				}
				last CITER;
			}    # END CITER

			# Move down a level in molecules (increment $im1)
			$mused[$im2] = 1;
			++$im1;
			*components1 = $molecules1[$im1]->Components;
			$ncomp1      = $#components1;

			#    print "set im1 to $im1\n";
		}    # END MITER
	}    # END loop over $sg2
	
	
	#DEBUG
	#$BNGmodel::TIMER += (time - $time0);
	#print STDERR "number of matches: ", scalar @maps, "\n";
    #print STDERR "SpeciesGraph::isomorphicToSubgraph  cumulative time: ", $BNGmodel::TIMER, "\n";
	return (@maps);
}

# Find mapping of graph onto another graph (or set of graphs) according to the following rules
# For each molecule:
# 1. Preserve molecule name and label (if defined)
# 2. Preserve component number and names
#
# In general, there may be multiple mappings.  For reaction rules it is
# necessary to select a single mapping.  This can be done be either taking the
# default mapping to be the one that takes the first possible match

# STILL NEEDS WORK

sub findMaps {
	my $sg1 = shift;

	*molecules1 = $sg1->Molecules;
	my @maps = ();

	while ( my $sg2 = shift ) {
		*molecules2 = $sg2->Molecules;

		my $nmol1 = $#molecules1;
		my $nmol2 = $#molecules2;
		my @mused = (0) x @molecules2;
		my @mptrs = (-1) x @molecules1;
		my @cptrs = (0) x @molecules1;
		my $im1, $im2, $ic1, $ic2;
		my $ci1, $ci2;

		# Loop over molecules of Source
		for ( $im1 = 0 ; $im1 <= $nmol1 ; ++$im1 ) {
			*components1 = $molecules1[$im1]->Components;
			$ncomp1      = $#components1;
			my $mmatch = 0;
		  MOL:

			# Loop over molecules of Target
			for ( $im2 = 0 ; $im2 <= $nmol2 ; ++$im2 ) {
				next if $mused[$im2]; # Continue if this molecule already mapped
				next if ( $molecules1[$im1]->Name cmp $molecules2[$im2]->Name );
				next
				  if ( $molecules1[$im1]->Label cmp $molecules2[$im2]->Label );
				*components2 = $molecules2[$im2]->Components;
				$ncomp2      = $#components2;
				next if ( $ncomp1 != $ncomp2 );
				my @cptr  = (0) x @components1;
				my @cused = (0) x @components2;

				#print "$molecules1[$im1]->Name -> $molecules2[$im2]->Name\n";
			  COMP:
				for ( $ic1 = 0 ; $ic1 <= $ncomp1 ; ++$ic1 ) {
					$ci1 = $components1[$ic1];
					my $cmatch = 0;
					for ( $ic2 = 0 ; $ic2 <= $ncomp2 ; ++$ic2 ) {

	   #printf "%s -> %s\n", $components1[$ic1]->Name, $components2[$ic2]->Name;
						next if $cused[$ic2];
						$ci2 = $components2[$ic2];

						# Component name
						next if ( $ci1->Name cmp $ci2->Name );
						next if ( $ci1->Label cmp $ci2->Label );
						$cmatch      = 1;
						$cptr[$ic1]  = $ic2;
						$cused[$ic2] = 1;
						last;
					}    # End loop over $ic2
					 # Unmatched components invalidate match for current molecule $im2
					if ( !$cmatch ) {
						next MOL;
					}
				}    # End loop over $ic1
				 # There is one-to-one mapping of components of $im1 to components of $im2
				$mmatch      = 1;
				$mptrs[$im1] = $im2;
				@cptrs[$im1] = [@cptr];
				$mused[$im2] = 1;
				last;
			} # End loop over $im2
			  # If no match for current molecule could be found, $mptrs[$im1]=-1
		}    # End loop over $im1

# Create new Map object for completed map, which is the best mapping that could be found
		my $map = Map->new;
		$map->Source($sg1);
		$map->Target($sg2);
		my %mapf = ();
		for my $im1 ( 0 .. $nmol1 ) {
			my $im2 = $mptrs[$im1];
			$mapf{$im1} = $im2;
			if ( $im2 < 0 ) {

				# Check for component label mappings
				next;
			}
			*cptr = $cptrs[$im1];
			for my $ic1 ( 0 .. $#cptr ) {
				$mapf{"$im1.$ic1"} = "$im2.$cptr[$ic1]";
			}
		}
		$map->MapF( {%mapf} );
		push @maps, $map;
	}    # End loop over target graphs

	return (@maps);
}

sub findMaps2

# (Map) = SpeciesGraph1->findMaps2(SpeciesGraph2)
#
# A simplified replacement for findMaps. Finds mapping by first labeling the
# molecules and components and building a map from object labels to the object index
# w.r.t. the species graph.  By matching labels in two species graphs,
# the forward and/or reverse map can be generated (map from pointer index to pointer index).
#
# should this be in Map module?
# REVISED by justinshogg@gmail.com 19feb2009
{

	# get species graphs
	my ( $sg1, $sg2 ) = @_;

	# for each speciesGraph, build a map from object labels to object indices
	my $labelmap1 = $sg1->buildLabelMap;
	my $labelmap2 = $sg2->buildLabelMap;

	# create and setup a new Map object
	my $map = Map->new;
	$map->Source($sg1);
	$map->Target($sg2);
	$map->MapF( buildPointerMap( $labelmap1, $labelmap2 ) );
	$map->MapR( buildPointerMap( $labelmap2, $labelmap1 ) );

	# all done, return map (in a list, for deprecated reasons)
	return ( ($map) );

	sub buildPointerMap

	  # pointermap = buildPointerMap( labelmap1, labelmap2 );
	  # DESCR: given 2 label maps, returns a map from label indices in $lmap1
	  #  to label indices in $lmap2.
	  # NOTES: the pointer map is a hash (not a Map object)
	{
		my ( $lmap1, $lmap2 ) = @_;
		my $pmap = {};
		for my $label ( keys %$lmap1 ) {

			# map label index1 to label index2, or -1 if index2 is not defined
			$pmap->{ $lmap1->{$label} } =
			  ( exists $lmap2->{$label} ) ? $lmap2->{$label} : -1;
		}
		return $pmap;
	}
}

sub buildLabelMap

  # labelmap = SpeciesGraph->buildLabelMap()
  # this outsources the labeling loop from findMaps2
  # returns a map from labels to indices
{
	my $sg       = shift;    # species graph
	my $labelmap = {};       # initialize map from labels to indices
	my %labels   = ();       # a map of labels  (up to replicate index)
	                         #  to the number of objects with that label.

	# label molecules
	for ( my $im = 0 ; $im < @{ $sg->Molecules } ; $im++ ) {
		my $mol     = $sg->Molecules->[$im];    # molecule to be labeled
		my @clabels = ();                       # list of component labels
		my $mlabel;                             # molecule label string

		# Get component labels (substitute name, if no label)
		for my $comp ( @{ $mol->Components } ) {
			if   ( my $clabel = $comp->Label ) { push @clabels, '%' . $clabel; }
			else                               { push @clabels, $comp->Name; }
		}

		# User provided label supercedes other labeling
		# It must be unique for each molecule and
		# each componentl
		if ( $mlabel = $mol->Label ) { $mlabel = '%' . $mlabel; }
		else {

			# Automatic mol label starts with molname
			$mlabel = $mol->Name;
			$mlabel .= '_' . join( '_', sort @clabels ) . '_';
			$mlabel .= ++$labels{$mlabel};
		}

		# write map:  molecule label -> molecule index
		$labelmap->{$mlabel} = $im;

		# Component labels have syntax mlabel|clabel.  If user label is
		# provided, it has precedence and mlabel is omitted. Otherwise, $clabel
		# is name of component plus a number that indicates its order in
		# occurence of identical components
		for ( my $ic = 0 ; $ic < @clabels ; $ic++ ) {
			my $clabel = $clabels[$ic];
			unless ( $clabel =~ /^\%/ ) {

				# Automatic label
				$clabel = $mlabel . '|' . $clabel . '_';
				$clabel .= ++$labels{$clabel};
			}
			$labelmap->{$clabel} = "$im.$ic";
		}
	}

	return $labelmap;
}

# Canonical order for molecules
sub cmp_molecule {
	my $a = shift;
	my $b = shift;
	my $cmp;
	if ( $cmp = ( $a->Name cmp $b->Name ) ) {
		return ($cmp);
	}
	*comp_a = $a->Components;
	*comp_b = $b->Components;
	if ( $cmp = ( $#comp_a <=> $#comp_b ) ) {
		return $cmp;
	}

	# Comparison of Compartment Names
	if ( $a->Compartment ) {
		return (1) if !( $b->Compartment );
		if ( $cmp = ( $a->Compartment->Name cmp $b->Compartment->Name ) ) {
			return ($cmp);
		}
	}
	if ( $b->Compartment ) {
		return (-1) if !( $a->Compartment );
	}

	for my $i ( 0 .. $#comp_a ) {
		if ( $cmp = cmp_component( $comp_a[$i], $comp_b[$i] ) ) {
			return $cmp;
		}
	}
	return (0);
}

sub by_molecule {
	return ( &cmp_molecule( $a, $b ) );
}

# Canonical order for components of molecules
sub cmp_component {
	my $a = shift;
	my $b = shift;

	my $cmp;

	# Lexical comparison of name
	if ( $cmp = ( $a->Name cmp $b->Name ) ) {
		return ($cmp);
	}

	# Lexical comparison of state labels
	if ( $cmp = ( $a->State cmp $b->State ) ) {
		return ($cmp);
	}

	# Comparison of number of edges
	*a_edges = $a->Edges;
	*b_edges = $b->Edges;
	if ( $cmp = ( $#b_edges <=> $#a_edges ) ) {
		return ($cmp);
	}

	# Comparison of edges
	#  for my $i (0..$#a_edges){
	#    if ($cmp=($a_edges[$i] cmp $b_edges[$i])){
	#      return($cmp);
	#    }
	#  }

	return (0);
}

sub by_component {
	return ( &cmp_component( $a, $b ) );
}

sub cmp_edge {
	my $a = shift;
	my $b = shift;
	my $cmp;

	my (@a_p) = split( '[ \.]', $a );
	my (@b_p) = split( '[ \.]', $b );

	for my $i ( 0 .. $#a_p ) {
		if ( $cmp = ( $a_p[$i] <=> $b_p[$i] ) ) {
			return ($cmp);
		}
	}

	# Getting here means edges are identical, which shouldn't happen.
	return (0);
}

sub by_edge {
	return ( &cmp_edge( $a, $b ) );
}

# copied code from cmp_edge to optimize speed
#  --justin, 17 apr09
sub edge_sort {

	# $a, $b should be array references
	my $cmp;
	foreach my $i ( 0 .. $#{$a} ) {
		if ( $cmp = ( $a->[$i] <=> $b->[$i] ) ) { return $cmp; }
	}

	# Getting here means edges are identical, which shouldn't happen.
	return 0;
}

1;
