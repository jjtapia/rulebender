package Observable;

use Class::Struct;
use FindBin;
use lib $FindBin::Bin;
use BNGUtils;
use BNGModel;
use SpeciesGraph;

struct Observable => {
  Name=>      '$',		   
  Patterns=>  '@',
  Weights=>   '@',
  Type=>'$'
};


sub readString{
  my $obs=shift;
  my $string= shift;
  my $model= shift;
  my $err="";

  my $plist= $model->ParamList;
  my $clist= $model->CompartmentList;
  my $mtlist= $model->MoleculeTypesList;

  # Check if first token is an index
  if ($string=~ s/^\s*\d+\s+//){
    # This index will be ignored
  }

  # Check if next token is observable type
  if ($string=~ s/^\s*(Molecules|Species)\s*//){
    $obs->Type($1);
  } else {
    $obs->Type('Molecules');
  }

  # Next token is observable Name
  if ($string=~ s/^\s*([A-Za-z0-9_]+)\s+//){
    my $name=$1;
    $obs->Name($name);
  } else {
    my ($name)= split(' ',$string);
    return("Invalid observable name $name: may contain only alphnumeric and underscore");
  }

  # Define parameter with name of the Observable
  if ($plist->set($obs->Name,"0", 1, "Observable",$obs)){
  	my $name= $obs->Name;
    return("Observable name $name matches previously defined Observable or Parameter");
  }

  # Remaining entries are patterns
  my $sep='^\s+|^\s*,\s*';
  *patterns= $obs->Patterns;
  while($string){
    my $g= SpeciesGraph->new();
    $err= $g->readString(\$string,$clist,0,$sep,$mtlist);
    if ($err){$err= "While reading observable ".$obs->Name.": $err"; return($err);}
    $string=~ s/$sep//;
    if (($obs->Type eq 'Species') && ($g->Quantifier eq '')){ 
      $g->MatchOnce(1);
    }
    push @patterns, $g;
  }
  
  return($err);
}

sub toString{
  my $obs=shift;
  my $string="";

  $string.= $obs->Type.' '.$obs->Name;
  for my $patt (@{$obs->Patterns}){
    $string.= " ".$patt->toString();
  }

  return($string);
}

sub toStringSSC{
  my $obs=shift;
  my $string="";

  for my $patt (@{$obs->Patterns}){
    ( my $tempstring, my $trash) = $patt->toStringSSC();
    $string.= " ".$tempstring;
  }

  return($string);
}

sub toMatlabString
{
   my $obs = shift;
   
   # create linear sum of terms that contribute to the observable
   my @terms = ();
   for ( my $idx=1; $idx < @{$obs->Weights}; $idx++ )
   {  
      if ( defined $obs->Weights->[$idx] )
      {
         my $term;
         if ( $obs->Weights->[$idx] == 1 )
         {  $term = "x($idx)";  }
         else
         {  $term = $obs->Weights->[$idx] . "*x($idx)";  }
         push @terms, $term;
      }
   }   
   return '(' . join( '+', @terms ) . ')';
}


sub toMexString
{
   my $obs = shift;
   
   # create linear sum of terms that contribute to the observable
   my @terms = ();
   for ( my $idx=1; $idx < @{$obs->Weights}; $idx++ )
   {  
      my $idx0 = $idx - 1;
      if ( defined $obs->Weights->[$idx] )
      {
         my $term;
         if ( $obs->Weights->[$idx] == 1 )
         {  $term = "NV_Ith_S(species,$idx0)";  }
         else
         {  $term = "RCONST(" . $obs->Weights->[$idx] . ")*NV_Ith_S(species,$idx0)";  }
         push @terms, $term;
      }
   }
   
   if ( @terms )
   {   return join( ' +', @terms );   }
   else
   {   return "RCONST(0.0)";  }
}


sub toXML{
  my $obs= shift;
  my $indent= shift;
  my $index= shift;

  my $id= "O".$index;

  my $string=$indent."<Observable";

  # Attributes
  # id
  $string.= " id=\"".$id."\"";
  # name
  if ($obs->Name){
    $string.= " name=\"".$obs->Name."\"";
  }
  # type
  if ($obs->Type){
    $string.= " type=\"".$obs->Type."\"";
  }

  # Objects contained
  my $indent2= "  ".$indent;
  my $ostring=$indent2."<ListOfPatterns>\n";
  my $ipatt=1;
  for my $patt (@{$obs->Patterns}){
    my $indent3= "  ".$indent2;
    my $pid= $id."_P".$ipatt;
    $ostring.= $patt->toXML($indent3,"Pattern",$pid,"");
    ++$ipatt;
  }
  $ostring.=$indent2."</ListOfPatterns>\n";

  # Termination
  if ($ostring){
    $string.=">\n"; # terminate tag opening
    $string.= $ostring;
    $string.=$indent."</Observable>\n";
  } else {
    $string.="/>\n"; # short tag termination
  }
}

sub update{
  my $obs= shift;
  *species=shift;

  my $err="";
  
  my @sgs=();
  for $spec (@species){
    if (!($spec->RulesApplied)){
      push @sgs, $spec->SpeciesGraph;
    }
  }
  my $ind_max= $#species+1;
  $obs->Weights->[$ind_max]= $obs->Weights->[$ind_max];

  # Loop over patterns to generate matches; update weight at index of each match.
  for my $patt (@{$obs->Patterns}){
    for my $sg (@sgs){
      @matches=  $patt->isomorphicToSubgraph($sg);
      my $n_match= scalar(@matches);
      next unless $n_match;
      if ($patt->Quantifier){
	my $test= $n_match.$patt->Quantifier;
	my $result= eval $test;
	#print "($test) $result\n";
	next unless $result;
      }
      my $index= $sg->Species->Index;
      ($obs->Type eq 'Species') && ($n_match=1);
      $obs->Weights->[$index]+= $n_match;
    }
  }

  return($err);
}

my $print_match=0;

sub getWeightVector{
  my $obs=shift;
  my @wv=();

  for my $i (1..$#{$obs->Weights}){
    my $w= $obs->Weights->[$i];
    if ($w){
      push @wv, $w;
    } else {
      push @wv, 0;
    }
  }
  return(@wv);
}

sub toGroupString{
  my $obs=shift;
  my $slist=shift;
  my $out= sprintf "%-20s ", $obs->Name;

  my $i=-1;
  my $first=1;
  my $n_elt=0;
  for my $w (@{$obs->Weights}){
    ++$i;
    next unless $w;
    ++$n_elt;
    if ($first){
      $first=0;
    } else {
      $out.=",";
    }
    if ($w==1){
      $out.= "$i";
    } else {
      $out.= "$w*$i";
    }
  }
  
  if ($print_match){
    print $obs->Patterns->[0]->toString(),": ";
    my $i=-1;
    for my $w (@{$obs->Weights}){
      ++$i;
      next unless $w;
      my $sstring= $slist->Array->[$i-1]->SpeciesGraph->toString();
      for my $nw (1..$w){
	print "$sstring, ";
      }
    }
    print "\n";
  }

  #printf "Group %s contains %d elements.\n", $obs->Name, $n_elt;
  return $out;
}

# Returns number of nonzero elements in the Group
sub sizeOfGroup{
  my $obs= shift;

  my $n_elt=0;
  for my $w (@{$obs->Weights}){
    next unless $w;
    ++$n_elt;
  }
  return($n_elt);
}

sub printGroup{
  my $obs=shift;
  my $fh= shift;
  my $species= shift;

  printf $fh "%s ", $obs->Name;
  
  my $first=1;
  my $n_elt=0;
  *weights= $obs->Weights;
  for my $spec (@$species){
    my $i= $spec->Index;
    my $w= $weights[$i];
    next unless $w;
    ++$n_elt;
    if ($first){
      $first=0;
    } else {
      print $fh ",";
    }
    if ($w==1){
      print $fh $i;
    } else {
      print $fh "$w*$i";
    }
  }
  print $fh "\n";
  return("");
}

sub toMathMLString{
  my $obs=shift;
  my $string="";

  $string.= "<math xmlns=\"http://www.w3.org/1998/Math/MathML\">\n";
  my $n_elt= $obs->sizeOfGroup();

  $string.= "  <apply>\n";
  $string.= "    <plus/>\n";
  if ($n_elt==1){
    $string.=   sprintf "      <cn> 0 </cn>\n";
  }

  my $i=-1;
  for my $w (@{$obs->Weights}){
    ++$i;
    next unless $w;
    if ($w==1){
      $string.=   sprintf "    <ci> S%d </ci>\n", $i;
    } else {
      $string.=   "    <apply>\n";
      $string.=   "      <times/>\n";
      $string.=   sprintf "      <cn> %s </cn>\n", $w;
      $string.=   sprintf "      <ci> S%d </ci>\n", $i;
      $string.=   "    </apply>\n";
    }
  }
  # Include zero entry if no nonzero weights
  if ($n_elt==0){
    $string.= "    <cn> 0 </cn>\n";
  }

  $string.= "  </apply>\n";
  $string.= "</math>\n";

  return ($string,"");
}

1;
