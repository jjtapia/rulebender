# $Id: RateLaw.pm,v 1.14 2007/07/06 04:47:47 faeder Exp $

package RateLaw;

use Class::Struct;
use FindBin;
use lib $FindBin::Bin;
use BNGUtils;
use SpeciesGraph;

struct RateLaw => {
  Type      => '$',
  Constants => '@',
  Factor    => '$'
};

# Two basic formats are possible
#  [stat_factor*]rate_constant_forward
#   or
#  [stat_factor*]rate_law_type(rate_constant1,...,rate_constantN)
my $n_RateLaw = 0;

sub newRateLaw {
  my $strptr   = shift;
  my $plist    = shift;
  my $basename = (@_) ? shift : "rateLaw";

  my $string_left = $$strptr;
  my ( $name, $rate_law_type, $rate_fac );
  my @rate_constants = ();
  my ( $param, $err );

  $rate_fac = 1;

  # Handle legacy Sat and MM RateLaw types
  if ($string_left =~ s/^(Sat|MM|Hill)\(//){
    $rate_law_type= $1;
    # Validate remaining rate constants
    my $found_end=0;
    while ( $string_left =~ s/^([A-Za-z0-9_]+)\s*// ) {
      my $rc = $1;

      #print "rc=$rc\n";
      ( $param, $err ) = $plist->lookup($rc);
      if ($err) { return ( "", $err ); }
      push @rate_constants, $rc;
      next if ($string_left =~ s/^,\s*//);
      if ($string_left =~ s/\)//){
        $found_end=1;
      }
    }
    if ( !$found_end) {
      return ( "", "RateLaw not terminated at $string_left" );
    }
  }
  elsif ( $string_left =~ /\S+/ ) {

    # Handle expression for rate constant of elementary reaction
    # Read expression
    my $expr = Expression->new();
    my $err = $expr->readString( \$string_left, $plist, "," );
    if ($err) { return ( "", $err ) }
    my $name = $expr->getName( $plist, $basename );
    ( my $param, my $err ) = $plist->lookup($name);
    if ( $param->Type =~ /^Constant/ ) {
      $rate_law_type = "Ele";
    }
    else {
      $rate_law_type = "Function";
    }
    push @rate_constants, $name;
  }
  else {

    # No rateLaw is specified: assume elementary with rate constant of 1
    $rate_law_type = "Ele";

    # New parameter initialized to default value of 1
    my $expr = Expression->new();
    my $str  = "1";
    $expr->readString( \$str, $plist );
    my $name = $expr->getName( $plist, $basename );
    printf "Creating new variable $name with value 1\n";
    push @rate_constants, $name;
  }

  # Remove leading whitespace
  $string_left =~ s/^\s*//;

  # Done processing input string
  $$strptr = $string_left;

  # Create new RateLaw object
  my $rl;
  $rl = RateLaw->new();
  $rl->Type($rate_law_type);
  $rl->Constants( [@rate_constants] );
  $rl->Factor($rate_fac);

  # Validate rate law type
  if ( $err = $rl->validate ) {
    return ( "", $err );
  }

  ++$n_RateLaw;
  return ($rl);
}

# Two basic formats are possible
#  [stat_factor*]rate_constant
#   or
#  [stat_factor*]rate_law_name rate_constant1 rate_constant2 ... rate_constantN

sub newRateLawNet {
  my $strptr = shift;
  my $plist  = shift;

  my $string_left = $$strptr;
  my $name, $rate_law_type, $rate_fac;
  my @rate_constants = ();
  my $param, $err;

  # Find statistical factor for rate law
  if ( $string_left =~ s/^([^*\s,]*)\*// ) {
    my $value;
    ( $param, $err ) = $plist->lookup($1);
    if ($param) {
      $value = $param->evaluate($plist);

      #$value= $1;
      #print "val=$value\n";
    }
    else {
      if ( !isReal($1) ) {
        return ( "",
          "Coefficient for rate law ($1) must be a parameter or a real number"
        );
      }
      $value = $1;
    }
    $rate_fac = $value;
  }
  else {
    $rate_fac = 1;
  }

  #print "rf=$rate_fac\n";

  # Get rate law type or name
  if ( $string_left =~ s/^([A-Za-z0-9_]+)\s*// ) {
    $name = $1;

    #print "name=$name|$string_left\n";
  }
  else {
    return ( "", "Invalid rate law specification in $string_left" );
  }

  if ( $string_left =~ /^\S+/ ) {

    # Handle rate law type
    $rate_law_type = $name;

    # Validate remaining rate constants
    while ( $string_left =~ s/^([A-Za-z0-9_]+)\s*// ) {
      my $rc = $1;

      #print "rc=$rc\n";
      ( $param, $err ) = $plist->lookup($rc);
      if ($err) { return ( "", $err ); }
      push @rate_constants, $rc;
    }
    if ( $string_left =~ /\S+/ ) {
      return ( "", "Invalid rate law syntax in $string_left" );
    }
  }
  else {

    # Handle single rate constant for elementary reaction
    $rate_law_type = "Ele";

    # Rate constant
    if ( $name eq '0' ) {

      # Allow reactions with zero rate for purpose of deleting
      # existing reaction
      $rate_law_type = "Zero";
    }
    else {
      ( $param, $err ) = $plist->lookup($name);
      if ($err) { return ( "", $err ); }
      push @rate_constants, $name;
    }
  }

  # Remove leading whitespace
  $string_left =~ s/^\s*//;

  # Done processing input string
  $$strptr = $string_left;

  # Create new RateLaw object
  my $rl;
  $rl = RateLaw->new();
  $rl->Type($rate_law_type);
  $rl->Constants( [@rate_constants] );
  $rl->Factor($rate_fac);

  # Validate rate law type
  if ( $err = $rl->validate ) {
    return ( "", $err );
  }

  return ($rl);
}

sub toString {
  my $rl         = shift;
  my $statFactor = (@_) ? shift : 1;
  my $netfile    = (@_) ? shift : 0;
  my $plist      = (@_) ? shift : '';

  my $string = '';

  $statFactor *= $rl->Factor;
  if ( $statFactor != 1 ) { $string .= "$statFactor*"; }

  my $type = $rl->Type;
  my $rcs  = $rl->Constants->[0];
  if ( $type eq 'Ele' ) {
    $string .= sprintf "%s", $rcs;
  }
  elsif ( $type eq 'Function' ) {
    $string.= $rcs;
#    if ($plist) {
#      # In a netfile, need to pass function call not variable name
#      ( my $param, my $err ) = $plist->lookup($rcs);
#      my $fcall = $param->toString($plist);
#      if ($netfile) {
#        $fcall =~ s/\(/ /;
#        $fcall =~ s/,/ /g;
#        $fcall =~ s/\)/ /;
#      }
#      $string .= $fcall;
#    }
  }
  else {
    if ($netfile){
      $string.= $type." ".join(' ', @{$rl->Constants});
    } else {
      $string.= $type."(".join(',', @{$rl->Constants}).")";
    }

  }

  return $string;
}


sub toMexString
{
    my $rl         = shift;
    my $rxn_mult   = shift;
    my $reactants  = shift;
    my $plist      = shift;

    my @rl_terms = ();

    if ( ($rxn_mult ne '1')  and  ($rxn_mult ne '') )
    {   push @rl_terms, $rxn_mult;   }
    
    if ( ($rl->Factor ne '1')  and  ($rl->Factor ne '') )
    {   push @rl_terms, $rl->Factor;  }
    
    my $type = $rl->Type;
    my $rate_constants = $rl->Constants;

    if ( $type eq 'Ele' )
    {
        # look up parameter
        (my $const) = $plist->lookup( $rate_constants->[0] );
    
        # get rate constant
        push @rl_terms, $const->toMexString($plist);
      
        # get reactant species    
        foreach my $reactant ( @$reactants )
        {
            #look up species
            my $species_idx = $reactant->Index - 1;
            push @rl_terms, "NV_Ith_S(species,$species_idx)";             
        }
    }
    elsif ( $type eq 'Function' )
    {
        # look up parameter
        (my $fcn_param) = $plist->lookup( $rate_constants->[0] ); 
        
        my $fcn = $fcn_param->Ref;
        my @fcn_args = @{$fcn->Args};
        push @fcn_args, 'expressions', 'observables';
        push @rl_terms, $fcn->Name . '(' . join(',', @fcn_args) . ')';

        # get reactant species  
        foreach my $reactant ( @$reactants )
        {
            #look up species
            my $species_idx = $reactant->Index - 1;
            push @rl_terms, "NV_Ith_S(species,$species_idx)";             
        }
    }
    else
    {
        #$string.= $type."(".join(',', @{$rl->Constants}).")";
        return  "other ratelaws not implemented";
    }

    # build ratelaw string
    return join( '*', @rl_terms );
}



sub toXML {
  my $rl     = shift;
  my $indent = shift;
  my $id     = (@_) ? shift : "RateLaw" . $n_RateLaw;

  if ( $rl->Type eq "Function" ) {
    return ( $rl->toXMLFunction( $indent, $id ) );
  }

  my $string = $indent . "<RateLaw";

  # Attributes
  # id
  $string .= " id=\"" . $id . "\"";

  # type
  $string .= " type=\"" . $rl->Type . "\"";

  # StatFactor
  if ( $rl->Factor != 1 ) {
    $string .= " statFactor=\"" . $rl->Factor . "\"";
  }

  # Objects contained
  my $ostring = "";
  my $indent2 = "  " . $indent;

  $ostring .= $indent2 . "<ListOfRateConstants>\n";
  for my $rc ( @{ $rl->Constants } ) {
    my $indent3 = "  " . $indent2;
    ### How to handle references to named parameters?
    $ostring .= $indent3 . "<RateConstant";
    $ostring .= " value=\"" . $rc . "\"";
    $ostring .= "/>\n";
  }
  $ostring .= $indent2 . "</ListOfRateConstants>\n";

  # Termination
  if ($ostring) {
    $string .= ">\n";                      # terminate tag opening
    $string .= $ostring;
    $string .= $indent . "</RateLaw>\n";
  }
  else {
    $string .= "/>\n";                     # short tag termination
  }
  return ($string);
}

sub toXMLFunction {
  my $rl     = shift;
  my $indent = shift;
  my $id     = (@_) ? shift : "RateLaw" . $n_RateLaw;

  my $string = $indent . "<RateLaw";

  # Attributes
  # id
  $string .= " id=\"" . $id . "\"";

  # type
  $string .= " type=\"" . $rl->Type . "\"";

  # reference
  $string .= " name=\"" . $rl->Constants->[0] . "\"";

  $string .= ">\n";

  my $ostring = "";

  # Termination
  # TODO This is a hack; the termination in RxnRule.pm.
  #$string.=$indent."</RateLaw>\n";

  return ($string);
}

# WARNING: Checking here is minimal

sub validate {
  my $rl = shift;

  *rate_constants = $rl->Constants;
  my $type = $rl->Type;

  if ( $type eq "Ele" ) {
  }
  elsif ( $type eq "Sat" ) {
    if ( scalar(@rate_constants) < 1 ) {
      return ("Saturation reactions require at least 1 rate constant");
    }
  }
  elsif ( $type eq "MM" ) {
    if ( scalar(@rate_constants) != 2 ) {
      return ("Michaelis-Menton reactions require exactly 2 rate constants");
    }
  }
  elsif ( $type eq "Hill" ) {
    if ( scalar(@rate_constants) != 3 ) {
      return ("Hill reactions require exactly 3 rate constants");
    }
  }
  elsif ( $type eq "Zero" ) {
    if ( scalar(@rate_constants) != 0 ) {
      return ("Zero reactions do not accept rate constants");
    }
  }
  elsif ( $type eq "Function" ) {

    # Validate local arguments here ?
  }
  else {
    return ("Unrecogized RateLaw type $type");
  }

  return (0);
}

sub toMatlabString {
  my $rl = shift;
  my $reactants = shift;
  my $statFactor = (@_) ? shift : 1;
  my $compartments = (@_) ? shift : 1;
  my $rr = (@_) ? shift : undef;
  
  my $string = '';

  # apply statFactor (accounts for symmetry) and match multiplicity)
  $statFactor *= $rl->Factor;
  if ($statFactor != 1)
  {  $string  .=  $statFactor;  }

  # calculate volume correction
  # TODO: get rid of this hack!
  if ( scalar @$compartments )
  {   
     # divide into surfaces and volumes
     my @surfaces = ( grep {$_->SpatialDimensions==2} @$compartments );
     my @volumes  = ( grep {$_->SpatialDimensions==3} @$compartments );

     # Pick and toss an anchor reactant.  If there's a surface reactant, toss it.
     # Otherwise toss a volume.
     (@surfaces) ? shift @surfaces : shift @volumes;

     my @compartments = (@surfaces, @volumes);

     if ( scalar @compartments )
     {
        $string .=  $string  ?  "/("  :  "1/(";
        #$string .= join '*', map {$_->Size->toMatlabString( $BNGModel::PARAM_LIST)} @compartments;
        $string .= join '*', map {$_->Size->toString()} @compartments;
        $string .= ")";
     }
  }

  # add multiplication symbol if there is a pre-multiplier.
  if ($string) {  $string .= '*';  }
  
  # handle elementary reactions
  if ($rl->Type eq 'Ele')
  {
     $string .= $rl->Constants->[0];
     for my $reac (@$reactants)
     {  $string.= "*x(".$reac->Index.")";  }
  }
  elsif ($rl->Type eq 'Sat'){
  		return('',"Sat rate law not currently supported");
  }
  elsif ($rl->Type eq 'MM'){
  		return('',"MM rate law not currently supported");
  }
  elsif ($rl->Type eq 'Hill'){
  		return('',"Hill rate law not currently supported");
  }
  # handle function reactions
  elsif ( $rl->Type eq 'Function' )
  {
     my $plist = $BNGModel::PARAM_LIST;
     my ($name, $err) = $plist->lookup( $rl->Constants->[0] );
     my $fcn_ref = $name->Ref;

     # temporarily set reference tags to the reactants species
     my $rrefs = $rr->RRefs;
     my @args = ();
     foreach my $arg ( keys %{$rr->RRefs} )
     {
        my $target = $rr->RRefs->{$arg};
        next unless ( $target =~ /^\d+$/ );
        my $value = 'x('. $reactants->[$target]->Index . ')';
        $plist->set( $arg, $value , 1, 'Local' );   
        push @args, $arg;   
     }

     $string .= $fcn_ref->toMatlabString( [], $plist );

     # unset reference tags
     foreach my $arg ( @args )
     {  $plist->deleteLocal($arg);  }
  }
  else
  {  return('',"Unrecogized RateLaw type $type");  }

  return $string, '';
}


##
##


sub toLatexString {
  my $rl         = shift;
  my $reactants  = shift;
  my $statFactor = shift;
  my $plist      = shift;
  my $string     = "";

  my @k    = @{ $rl->Constants };
  my $type = $rl->Type;

  $statFactor *= $rl->Factor;

  my %pindex = ();
  for my $index ( 0 .. $#{ $plist->Array } ) {
    my $pname = $plist->Array->[$index]->Name;
    $pindex{$pname} = $index + 1;
  }

  if ( $type eq "Ele" ) {
    if ( $statFactor != 1 ) {
      $string .= "$statFactor ";
    }
    $string .= "p_{" . $pindex{ $k[0] } . "}";
    for my $reac (@$reactants) {
      $string .= " x_{" . $reac->Index . "}";
    }
  }
  else {
    return ( "", "Unrecogized RateLaw type $type" );
  }
  return ( $string, "" );
}

sub toMathMLString {
  my $rl         = shift;
  my $rindices   = shift;
  my $pindices   = shift;
  my $statFactor = (@_) ? shift : 1;
  my $string     = "";

  my @k    = @{ $rl->Constants };
  my $type = $rl->Type;

  $string .= "<math xmlns=\"http://www.w3.org/1998/Math/MathML\">\n";

  $statFactor *= $rl->Factor;

  if ( $type eq "Ele" ) {

    # $statFactor*$k[0]*reactant1*...*reactantN
    $string .= "  <apply>\n    <times/>\n";
    if ( $statFactor != 1 ) {
      $string .= "    <cn> $statFactor </cn>\n";
    }
    $string .= sprintf "    <ci> %s </ci>\n", $k[0];
    for my $i (@$rindices) {
      $string .= sprintf "    <ci> S%d </ci>\n", $i;
    }
    $string .= "  </apply>\n";
  }
  elsif ( $type eq "Sat" ) {

# NOTE: THIS CODE IS NOT TESTED!
# One parameter:
#    rate = $statFactor*$k[0]
# Two or more parameters (N denominator terms, N+1 parameters)
#    rate = $statFactor*$k[0]*reactant1*...*reactantN/(($k[1]+reactant1)*...*($k[N]+reactantN)
    if ( $#k == 0 ) {
      if ( $statFactor != 1 ) {
        $string .= "    <apply>\n";
        $string .= "      <times/>\n";
        $string .= "      <cn> $statFactor </cn>\n";
        $string .= sprintf "      <ci> %s </ci>\n", $k[0];
        $string .= "    </apply>\n";
      }
      else {
        $string .= sprintf "      <ci> %s </ci>\n", $k[0];
      }
    }
    else {
      $string .= "  <apply>\n";
      $string .= "    <divide/>\n";

      $string .= "    <apply>\n";
      $string .= "      <times/>\n";
      if ( $statFactor != 1 ) {
        $string .= "      <cn> $statFactor </cn>\n";
      }
      $string .= sprintf "      <ci> %s </ci>\n", $k[0];
      for my $i (@$rindices) {
        $string .= sprintf "      <ci> S%d </ci>\n", $i;
      }
      $string .= "    </apply>\n";
      my $indentp = "    ";
      if ( $#k > 1 ) {
        $string  .= $indentp . "<apply>\n";
        $string  .= $indentp . "<times/>\n";
        $indentp .= "  ";
      }
      for my $ik ( 1 .. $#k ) {
        $string .= $indentp . "<apply>\n";
        $string .= $indentp . "<plus/>\n";
        $string .= sprintf "%s  <ci> %s </ci>\n", $indentp, $k[$ik];
        $string .= sprintf "%s  <ci> S%d </ci>\n", $indentp,
          $$rindices[ $ik - 1 ];
        $string .= $indentp . "</apply>\n";
      }
      if ( $#k > 1 ) {
        $indentp =~ s/  $//;
        $string .= $indentp . "</apply>\n";    # end times
      }
      $string .= "  </apply>\n";               # end divide
    }
  }
  elsif ( $type eq "MM" ) {
    return ( "", "Michaelis-Menton type reactions are not curently handled." );
  }
  else {
    return ( "", "Unrecogized RateLaw type $type" );
  }

  $string .= "</math>\n";

  return ( $string, "" );
}

1;
