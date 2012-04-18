# $Id: Rxn.pm,v 1.9 2007/07/06 04:46:32 faeder Exp $

package Rxn;

use Class::Struct;
use FindBin;
use lib $FindBin::Bin;
use BNGUtils;
use SpeciesGraph;

struct Rxn => {
  Reactants      => '@',          # Array of reactant Species
  Products       => '@',          # Array of product Species
  RateLaw        => 'RateLaw',
  StatFactor     => '$',	       
  Priority       => '$',
  RxnRule        => '$',
  Compartments   => '@',          # List of reaction compartments (usually one)
};

sub toString{
  my $rxn= shift;
  my $text= (@_) ? shift : 0;
  my $plist= (@_) ? shift : 0;

  *reac= $rxn->Reactants;
  *prod= $rxn->Products;
  
  my $string;
  
  if ($text)
  {
    my @rstrings=();
    my @pstrings=();
    for my $r (@reac)
    {  push @rstrings, $r->SpeciesGraph->toString();  }
    for my $p (@prod)
    {  push @pstrings, $p->SpeciesGraph->toString();  }
    $string= join(' + ', @rstrings)." -> ".join(' + ', @pstrings);
  }
  else
  { 
    $string= $rxn->stringID(); 
  }

  $string .= ' ';

  # scale reaction rate by compartment volume
  my $volume_scale = $rxn->volume_scale($plist);

  # get ratelaw string
  $string .= $rxn->RateLaw->toString( $volume_scale * $rxn->StatFactor, 1, $plist );

  if ($rxn->RxnRule)
  {  $string.= " #".$rxn->RxnRule->Name;  }

  return $string;
}


sub toMexString
{
    my $rxn   = shift;
    my $plist = shift;

    my $reactants = $rxn->Reactants;
 
    my $statFactor = $rxn->StatFactor;
    my $volume_string = $rxn->volume_scale_mex($plist);

    # build multiplier string
    my @mult_factors = ();
    if ( ($statFactor ne '1')  and  ($statFactor ne '')  )
    {   push @mult_factors, $statFactor;   }
  
    if ( ($volume_string ne '1')  and  ($statFactor ne '') )
    {   push @mult_factors, $volume_string;   }
      
    my $multiplier_string;
    if ( scalar @mult_factors )
    {   $multiplier_string = join( '*', @mult_factors );   }
    else
    {   $multiplier_string = '1';   }

    # get ratelaw string
    return $rxn->RateLaw->toMexString( $multiplier_string, $reactants, $plist );
}


# Used to compare whether reactions are identical (only in terms of species
# involved)
sub stringID
{
    my $rxn = shift;
    my $string = '';
  
    # Prior to 2.1.7, a reaction with zero reactants or zero products produced an
    # empty string for the respective field. Network2 does not recognize the null
    # string, leading to a parsing error. To resolve this issue, a null reactant (or product)
    # indicated by the index "0" will be output for the reactant (resp. product) field
    # if a reaction has zero reactants (or products).  --Justin, 29oct2010
  
    # QUESTION: will this effect sorting or other uses of this method?
  
    # get reactant indices
    my @rstrings=();
    if ( scalar @{$rxn->Reactants} )
    {   foreach my $r (@{$rxn->Reactants}) { push @rstrings, $r->Index; }   }
    else
    {   push @rstrings, "0";   }
  
    # get product indices       
    my @pstrings=();
    if ( scalar @{$rxn->Products} )
    {   foreach my $p (@{$rxn->Products}) { push @pstrings, $p->Index; };   }
    else
    {   push @pstrings, "0";   }
  
    # sort reactants and products (if ratelaw is elementary or zero-order)
    my $type= $rxn->RateLaw->Type;
    if ( ($type eq 'Ele') or ($type eq 'Zero') )
    {
        @rstrings = sort {$a<=>$b} @rstrings;
        @pstrings = sort {$a<=>$b} @pstrings;
    }
    
    $string .= join(',', @rstrings) . " " . join(',', @pstrings);

    return ($string);
}




sub volume_scale
# non-neg_realnum = $rxn->volume_scale($plist)
# calculate volume scaling factor for compartment reactions
#
# NOTE: in future voluming scaling should be handled by an expression,
#  preferrably in the RateLaw class.

# Handle volume-dependent rate constants for compartment reactions.
#   justinshogg@gmail.com  23feb2009
#
# we assume state variables are species counts (not concentrations) and
# that user has chosen consistent units for reaction constants which are
# independent of compartment volumes.
#
# for bi-molecular reactions, the reaction compartment is the 3-D volume [V]
# unless all reactants are at a 2-D surface [S].
#
#  rxn type                adjustment
#  ----------------------------------------------------
#   S                      none
#   V                      none
#   S + S                  /S
#   S + V                  /V
#   V + V                  /V 
#   S + S + S              /S/S
#   S + S + V              /S/V
#   S + V + V              /V/V
#   V + V + V              /V/V            etc...

{
   my $rxn = shift;
   my $plist = shift;
   
   # initialize volume_scale to 1.
   my $volume_scale = 1;

   # get all the defined compartments
   my @defined_compartments = ( grep {defined $_} @{$rxn->Compartments} );
   unless ( scalar(@defined_compartments) )
   {  return $volume_scale;  }
   
   # divide into surfaces and volumes
   my @surfaces = ( grep {$_->SpatialDimensions==2} @defined_compartments );
   my @volumes  = ( grep {$_->SpatialDimensions==3} @defined_compartments );

   # Pick and toss an anchor reactant.  If there's a surface reactant, toss it.
   # Otherwise toss a volume.
   (@surfaces) ? shift @surfaces : shift @volumes;

   # TODO: handle case without plist
   if ( $plist )
   {
       # divide by the product of the remaining reactants
       foreach my $comp ((@surfaces, @volumes))
       {
           $volume_scale /= $comp->Size->evaluate($plist);
       }
   }
     
   return $volume_scale;
}

sub volume_scale_mex
# non-neg_realnum = $rxn->volume_scale($plist)
# calculate volume scaling factor for compartment reactions
#
# NOTE: in future voluming scaling should be handled by an expression,
#  preferrably in the RateLaw class.

# Handle volume-dependent rate constants for compartment reactions.
#   justinshogg@gmail.com  23feb2009
#
# we assume state variables are species counts (not concentrations) and
# that user has chosen consistent units for reaction constants which are
# independent of compartment volumes.
#
# for bi-molecular reactions, the reaction compartment is the 3-D volume [V]
# unless all reactants are at a 2-D surface [S].
#
#  rxn type                adjustment
#  ----------------------------------------------------
#   S                      none
#   V                      none
#   S + S                  /S
#   S + V                  /V
#   V + V                  /V 
#   S + S + S              /S/S
#   S + S + V              /S/V
#   S + V + V              /V/V
#   V + V + V              /V/V            etc...

{
   my $rxn = shift;
   my $plist = shift;
   
   # initialize volume_scale to 1.
   #my $volume_expr = new Expression;

   # get all the defined compartments
   my @defined_compartments = ( grep {defined $_} @{$rxn->Compartments} );
   unless ( scalar(@defined_compartments) )
   {  return $volume_scale;  }
   
   # divide into surfaces and volumes
   my @surfaces = ( grep {$_->SpatialDimensions==2} @defined_compartments );
   my @volumes  = ( grep {$_->SpatialDimensions==3} @defined_compartments );

   # Pick and toss an anchor reactant.  If there's a surface reactant, toss it.
   # Otherwise toss a volume.
   (@surfaces) ? shift @surfaces : shift @volumes;

   my @volume_pars = ();
   foreach my $comp ((@surfaces, @volumes))
   {
       push @volume_pars, $comp->Size->toMexString($plist);
   }
 
 
 
   
   my $vol_string;
   if ( @volume_pars )
   {
       $vol_string = '(1.0/' . join( '/', @volume_pars ) . ")";
       #$volume_expr->readString( \$expr_string, $plist );
   }
   else
   {   
       $vol_string = '1';
       #$volume_expr->readString( \$expr_string, $plist );
   }

   #return $volume_expr;
   return $vol_string;
}

1;
