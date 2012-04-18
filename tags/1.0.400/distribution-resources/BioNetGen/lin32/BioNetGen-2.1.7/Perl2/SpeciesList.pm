# $Id: SpeciesList.pm,v 1.14 2007/02/20 17:37:01 faeder Exp $

package SpeciesList;

use Class::Struct;
use FindBin;
use lib $FindBin::Bin;
use Species;
use SpeciesGraph;
use MoleculeTypesList;
use BNGUtils;
use ParamList;

struct SpeciesList=> {
  Array=> '@',
  Hash=>  '%',
  Hash_exact=> '%'
};

sub sort {
  my $slist= shift;

  print "Sorting species list\n";
  my @newarr= sort {$a->SpeciesGraph->StringExact cmp $b->SpeciesGraph->StringExact} @{$slist->Array};
  $slist->Array(\@newarr);
  my $ispec=1;
  for my $spec (@{$slist->Array}){
    $spec->Index($ispec);
    ++$ispec;
  }

  return($slist);
}

# Returns pointer to matching species in $slist or null if no match found
sub lookup
{
    my $slist = shift;
    my $sg = shift;
    my $check_iso = (@_) ? shift : 1;

    *hash  = $slist->Hash;
    *array = $slist->Array;

    if( $sg->IsCanonical ) { $check_iso=0; }

    my $sstring= $sg->StringID;

    my $spec = '';
    if ( defined( $hash{$sstring} ) )
    {
        # Since string is not completely canonical, need to check isomorphism with other list members
        # Determine whether the graph is isomorphic to any on the current list
        if ($check_iso)
        {
            my $found_iso=0;
            for my $spec2 (@{$hash{$sstring}})
            {
	            if ($sg->isomorphicTo($spec2->SpeciesGraph))
	            {
                    $spec=$spec2;
                    $found_iso=1;
                    last;
                }
            }
            #if (!$found_iso){
            #print $sg->toString(),"not uniquely defined by ",$sstring,"\n";
            #}
        }
        else
        {
            #print "Not checking isomorphism\n";
            $spec = $hash{$sstring}->[0];
        }
    }
    return ($spec);
}

sub lookup_bystring {
  my $slist=shift;
  my $sstring=shift;

  return($slist->Hash_exact->{$sstring});
}

# Returns reference to Species object either newly created or found in $slist
# Should check if species already exists in list before adding
sub add {
  my $slist= shift;
  my $sg= shift;
  my $conc= (@_) ? shift : 0;

  *hash= $slist->Hash;
  *hash_exact= $slist->Hash_exact;
  *array= $slist->Array;

  # Create new species from SpeciesGraph
  $spec=Species->new;
  push @array, $spec;
  push @{$hash{$sg->StringID}}, $spec;
  $hash_exact{$sg->StringExact}= $spec; # Can only be one entry
  $spec->SpeciesGraph($sg);
  $spec->Concentration($conc);
  $spec->Index($#array+1);
  $spec->RulesApplied(0);
  # Put ref to species in SpeciesGraph to bind it
  $sg->Species($spec);

  return($spec);
}

sub remove{
  my $slist=shift;
  my $spec= shift;

  #print "Removing species ", $spec->Index," ",$spec->SpeciesGraph->toString(),"\n";

  # Remove from Array
  splice(@{$slist->Array}, $spec->Index-1,1);
  
  # Remove from Hash 
  *harray= $slist->Hash->{$spec->SpeciesGraph->StringID};
  for my $i (0..$#harray){
    if ($spec==$harray[$i]){
      splice(@harray,$i,1);
      if (!@harray){
	undef $slist->Hash->{$spec->SpeciesGraph->StringID};
      }
      last;
    }
  }

  # Remove from Hash_exact
  undef $slist->Hash_exact->{$spec->SpeciesGraph->StringExact};

  return;
}


# Read entry from input species block

sub readString
{
  my $slist=shift;
  my $string= shift;
  my $plist= shift;
  my $clist= shift;
  my $mtlist= (@_) ? shift : "";
  my $AllowNewTypes= (@_) ? shift : 0;

  my $conc, $sg, $err;

  my $name='';
#  if ($string=~ s/^\s*([^:].*)[:]\s*//){
#    # Check if first token is a name for the species (first occurence of a ':')
#    $name= $1;
#    #print "(user) name=$name\n";
#  }
#  # Check if token is an index (ignored)
#  elsif ($string=~ s/^\s*(\d+)\s+//){
#  }

  if ( $string =~ s/^\s*(\d+)\s+// ) {}

  # Read species string
  $sg = SpeciesGraph->new;
  $string =~ s/^\s+//;
  $err = $sg->readString( \$string, $clist, 1, '^\s+', $mtlist, $AllowNewTypes );
  if ($err) { return ($err); }

  # Check if isomorphic to existing species
  if ( my $sg_prev=$slist->lookup($sg) )
  {
      my $sstring = $sg->StringExact;
      my $index = $sg_prev->Index;
      return ( "Species $sstring isomorphic to previously defined species index $index" );
  }

  # Read species concentration as math expression (set to 0 if not present)
  # Set species concentration to number or variable name
  if ( $string=~ /\S+/ )
  {
    # Read expression
    my $expr= Expression->new();
    if ( my $err = $expr->readString( \$string, $plist ) ) { return ('', $err) }
    if ( $expr->Type eq 'NUM' )
    {
        $conc = $expr->evaluate();
    }
    else
    {
        $conc = $expr->getName( $plist, 'InitialConc' );
    }
  }
  else
  {
      $conc = 0;
  }

  # Create new Species entry in SpeciesList
  my $spec = $slist->add($sg, $conc);

  return ('');
}

sub writeBNGL{
  my $slist= shift;
  my $conc= (@_) ? shift: "";
  my $plist= (@_) ? shift : "";
  my $print_names= (@_) ? shift : 0;
  my $out="";

  # Determine length of longest species string
  my $maxlen=0;
  for my $spec (@{$slist->Array}){
    my $len= length($spec->SpeciesGraph->Name.$spec->SpeciesGraph->StringExact)+1;
    $maxlen= ($len> $maxlen) ? $len : $maxlen;
  }

  $out.="begin species\n";
  for my $spec (@{$slist->Array}){
    $out.= sprintf "%5d ", $spec->Index;
    my $sname;
    #my $sexact= $spec->SpeciesGraph->StringExact;
    my $sexact= $spec->SpeciesGraph->toString(0);
    if (my $name=$spec->SpeciesGraph->Name){
      if ($sexact=~ /[:]/){
	$sname.= $name.$sexact;
      } else {
	$sname.= $name.':'.$sexact;
      }	
    } else {
      $sname=$sexact;
    }
    $out.= sprintf "%-${maxlen}s", $sname;
    my $c;
    if (@$conc){
      $c= $$conc[$spec->Index-1];
    } else {
      $c= $spec->Concentration;
    }
    $out.= sprintf " %s", $c;
    # $out.= sprintf " %d", $spec->RulesApplied;
    $out.= "\n";
  }
  $out.="end species\n";
  return($out);
}

sub writeSSC{
  my $slist= shift;
  my $conc= (@_) ? shift: "";
  my $plist= (@_) ? shift : "";
  my $print_names= (@_) ? shift : 0;
  my $string="";

  # Determine length of longest species string. Not sure, what it does
  my $maxlen=0;
  for my $spec (@{$slist->Array}){
          my $len= length($spec->SpeciesGraph->Name.$spec->SpeciesGraph->StringExact)+1;
          $maxlen= ($len> $maxlen) ? $len : $maxlen;
  }

  for my $spec (@{$slist->Array}){
          my $sname;
          my $sexact= $spec->SpeciesGraph->toStringSSCMol();
          $sname=$sexact;
          $string .= "new $sname at ";
          my $c;
          $c= $spec->Concentration;
          $string .= $c;
          $string.= "\n";
  }

  return($string);
}


sub print{
  my $slist= shift;
  my $fh= shift;
  my $i_start= (@_) ? shift : 0;

  print $fh "begin species\n";
  *sarray= $slist->Array;
  for my $i ($i_start..$#sarray){
    my $spec= $sarray[$i];
    printf $fh "%5d %s %s\n", $i-$i_start+1, $spec->SpeciesGraph->StringExact, $spec->Concentration;
  }
  print $fh "end species\n";
  return("");
}

sub toXML{
  my $slist= shift;
  my $indent=shift;

  my $string=$indent."<ListOfSpecies>\n";
  
  for my $spec (@{$slist->Array}){
    $string.= $spec->toXML("  ".$indent);
  }

  $string.= $indent."</ListOfSpecies>\n";
  return($string);
}

1;
