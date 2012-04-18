# Objects for typing and checking Molecules
package MoleculeType;

use Class::Struct;
use FindBin;
use lib $FindBin::Bin;
use Molecule;
use ComponentType;

struct MoleculeType=> {
  Name=> '$',
  Components=> '@', # Array of ComponentTypes
};

sub readString{
  my $mtype=shift;

  my $strptr= shift;

  my $string_left=$$strptr;

  # Get molecule name
  if ($string_left=~ s/^([A-Za-z0-9_]*)//){
    my $name= $1;
    $mtype->Name($1);
  } else {
    return("Invalid MoleculeType name in $string_left");
  }

  # Get molecule state (marked by ~) edges (marked by !) and label (marked by
  # %) and components (enclosed in ())
  my @elabels=();
  my @components=();
  while($string_left){
    # Read components in parentheses
    if ($string_left=~ s/^[(]//){
      if (@components){
	return("Multiple component definitions");
      }
      while($string_left){
	# Continue characters
	if ($string_left=~ s/^[,.]//){
	  next;
	}
	# Stop characters
	elsif ($string_left=~ s/^[)]//){
	  last;
	}
        # Read
	else {
	  my $comp= ComponentType->new;
	  my $err= $comp->readString(\$string_left);
	  if ($err){ return($err);}
	  push @components, $comp;
	}
      }
    }
    # Stop characters
    elsif ($string_left=~ /^[,.]|^\s+|[<]?-[>]/){
      last;
    }
    # Stop at unrecognized syntax
    else {
      return("Invalid MoleculeType specification $$strptr");
    }
  }

  if (@components){
    $mtype->Components([@components]);
  }
  $$strptr= $string_left;

  return("");
}

sub add {
  my $mtype=shift;
  my $mol= shift;

  #print "Adding ", $mol->toString(),"\n";
  $mtype->Name($mol->Name);
  my @ctarray;
  @ctarray=();
  for my $comp (@{$mol->Components}){
    my $ctype=ComponentType->new;
    $ctype->Name($comp->Name);
    # The first entry in the States array becomes the default state value
    my $state= $comp->State;
    if ($state ne ""){
      $ctype->States(0,$state);
    }
    push @ctarray, $ctype;
  }
  $mtype->Components([@ctarray]);
  return("");
}

sub check {
  my $mtype= shift;
  my $mol = shift;
  my $params= shift;

  my $IsSpecies= (defined($params->{IsSpecies})) ? $params->{IsSpecies} : 1;
  my $AllowNewStates= (defined($params->{AllowNewStates})) ? $params->{AllowNewStates} : 1;
  my $AllowPartial= !($IsSpecies);
  my $AllowWildcard= !($IsSpecies);
  my $AllowUndefinedStates= (defined($params->{AllowUndefinedStates})) ? $params->{AllowUndefinedStates} : !($IsSpecies);
  my $InheritList= (defined($params->{InheritList})) ? $params->{InheritList} : 0;

  #print "Checking ", $mol->toString(),"\n";

  @ctypes= @{$mtype->Components};
  for my $comp (@{$mol->Components}){
    my $found=0;
    my $index=0;
    # Check for match for each component
    for my $comp_type (@ctypes){
      if ($comp->Name eq $comp_type->Name){
	my $state= $comp->State;
	if ($state eq ""){
	  # If component state is undefined, check whether component states have been declared, meaning this 
	  # component should not be stateless, unless AllowUndefinedStates is true
	  my $InheritState = ($InheritList) ? $InheritList->[$index] : 0;
	  #print "IS= $InheritState\n";
	  if (@{$comp_type->States} && !$InheritState && !$AllowUndefinedStates){
	    my $err= sprintf "State of component %s of molecule %s must be set", 
	      $comp->Name, $mol->toString(); 
	    return($err);
	  }
	} 
	elsif($state=~/^[*?+]$/){
	  if (!$AllowWildcard){
	    my $err="May not use wildcard for component state in species.";
	    return($err);
	  }
	}
	else {
	  # If component state is defined, check whether no component states have been declared, meaning this
	  # component should be stateless
	  if (!@{$comp_type->States}){
	    my $err= sprintf "Component %s of molecule %s does not accept states", 
	      $comp->Name, $mol->toString(); 
	    return($err);
	  }
	  
	  # Check that $state matches allowed states for this component
	  my $sfound=0;
	  for my $comp_state (@{$comp_type->States}){
	    if ($state eq $comp_state){
	      $sfound=1;
	      last;
	    }
	  }
	  if (!$sfound){
	    if ($AllowNewStates){
	      printf "Adding %s as allowed state of component %s of molecule %s\n", 
		$comp->State, $comp->Name, $mol->Name; 
	      push @{$comp_type->States}, $comp->State;
	    } else {
	      my $err= sprintf "Component state %s of component %s of molecule %s not defined in molecule declaration", 
		$comp->State, $comp->Name, $mol->toString(); 
	      return($err);
	    }
	  } 
	}
	# Delete component type from search array
	splice @ctypes, $index, 1;
	$found=1;
	last;
      } 
      ++$index;
    }
    if (!$found){
      my $err= sprintf "Component %s of molecule %s not found in molecule declaration", $comp->Name, $mol->toString();
      return($err);
    }
  }

  # Incomplete specification of molecule components
  if (!$AllowPartial && @ctypes){
    $names="";
    for my $ct (@ctypes){
      $names.= " ".$ct->Name;
    }
    my $err= sprintf "Component(s)${names} missing from molecule %s", $mol->toString();
    return($err);
  }

  return("");
}

sub toString{
  my $mtype= shift;
  my $string="";

  $string.= $mtype->Name;

  my @cstrings;
  @cstrings=();
  for my $comp (@{$mtype->Components}){
    push @cstrings, $comp->toString();
  }
  $string.= "(".join(",",@cstrings).")";

  return($string);
}

sub toStringSSC{
  my $mtype= shift;
  my $string="";

  $string.= $mtype->Name;

  my @cstrings;
  @cstrings=();
  for my $comp (@{$mtype->Components}){
    push @cstrings, $comp->toStringSSC();
  }
  $string.= "(".join(",",@cstrings).")";

  return($string);
}

sub toXML{
  my $mtype= shift;
  my $indent=shift;
  my $string=$indent."<MoleculeType";

  # Attributes
  # id
  $string.=" id=\"".$mtype->Name."\"";


  # Objects
  my $indent2= "  ".$indent;
  my $ostring="";
  # Component list
  if (@{$mtype->Components}){
    $ostring.=$indent2."<ListOfComponentTypes>\n";
    for my $comp (@{$mtype->Components}){
      $ostring.= $comp->toXML("  ".$indent2);
    }
    
    $ostring.=$indent2."</ListOfComponentTypes>\n";
  }

  if ($ostring){
    $string.=">\n"; # terminate tag opening
    $string.= $ostring;
    $string.=$indent."</MoleculeType>\n";
  } else {
    $string.="/>\n"; # short tag termination
  }

  return($string);
}

1;
