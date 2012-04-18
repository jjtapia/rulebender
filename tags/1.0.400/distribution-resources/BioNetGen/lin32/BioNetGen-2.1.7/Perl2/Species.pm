package Species;

use Class::Struct;
use FindBin;
use lib $FindBin::Bin;
use SpeciesGraph;
use strict;

struct Species=> {
  SpeciesGraph=>  'SpeciesGraph',
  Concentration=> '$',
  Index=>         '$',		  
  RulesApplied=>  '$',
};

sub toXML{
  my $spec= shift;
  my $indent= shift;
  my $id= (@_) ? shift : "S".$spec->Index;

  my $type="Species";
  my $attributes="";

  # Attributes
  # concentration
  $attributes.= " concentration=\"".$spec->Concentration."\"";
  # name
  $attributes.= " name=\"".$spec->SpeciesGraph->toString()."\"";

  # Objects contained
  my $string= $spec->SpeciesGraph->toXML($indent,$type,$id,$attributes);

  return($string);
}

1;
