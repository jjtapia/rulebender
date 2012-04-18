# $Id: RxnList.pm,v 1.6 2006/09/28 02:21:03 faeder Exp $

package RxnList;

use Class::Struct;
use FindBin;
use lib $FindBin::Bin;
use Rxn;
use SpeciesList;

struct RxnList => {
  Array       => '@',
  Hash        => '%',
  SpeciesList => 'SpeciesList',
  AsProduct   => '%'             # Number of times a species appears as product,
                                 # key is pointer to Species
};

sub resetHash {
  my $rlist = shift;

  undef( %{ $rlist->Hash } );
}

sub add {
  my $rlist    = shift;
  my $rxn      = shift;
  my $add_zero = shift;


  my $n_add = 0;

  # Don't add reaction with RateLaw of type Zero
  my $add_rxn;
  if ( ( $rxn->RateLaw->Type eq "Zero" ) && ( !$add_zero ) ) {
    $add_rxn = 0;
  }
  else {
    $add_rxn = 1;
  }

  *hash  = $rlist->Hash;
  *array = $rlist->Array;

  # Modify the string returned by this call to affect when reactions are
  # combined.
  my $rstring = $rxn->stringID();
  my ( $r, $p ) = split( ' ', $rstring );

  # Check for identical reactants and products
  if ( $r eq $p ) {
    $add_rxn = 0;
  }
  elsif ( defined( $hash{$rstring} ) ) {
    for my $rxn2 ( @{ $hash{$rstring} } ) {

     # NOTE: This algorithm guarantees that all rxns on list have same priority.
      if ( $rxn->Priority == $rxn2->Priority ) {

        # Reaction with same rate law as previous reaction is combined with it
        if ( $rxn->RateLaw == $rxn2->RateLaw ) {
          $rxn2->StatFactor( $rxn2->StatFactor + $rxn->StatFactor );
          $add_rxn = 0;

          # Exit from loop since rxn is now handled.
          last;
        }
      }
      elsif ( $rxn->Priority < $rxn2->Priority ) {

        # New reaction has lower priority so it's not added and we're done
        $add_rxn = 0;
        last;
      }
      else {

      # New reaction has higher priority, causing previous reaction to be deleted
      # NOTE: All reactions with the lower priority will be deleted by looping over $rxn2
      # Find and delete old entry from Array
        $rlist->remove($rxn2);
        --$n_add;
      }
    }    # END loop over previous reactions
  }

  # Add new entry
  *phash = $rlist->AsProduct;
  if ($add_rxn) {
    push @array, $rxn;
    push @{ $hash{$rstring} }, $rxn;
    for my $spec ( @{ $rxn->Products } ) {
      ++$phash{$spec};
    }
    ++$n_add;
  }
  
  return ($n_add);
}

sub remove {
  my $rlist = shift;
  my $rxn   = shift;

  *hash  = $rlist->Hash;
  *array = $rlist->Array;

  # Remove entry from Array
  for my $i ( 0 .. $#array ) {
    if ( $rxn == $array[$i] ) {

      printf "Deleting rxn %s\n", $rxn->toString();
      splice( @array, $i, 1 );
      last;
    }
  }

  # Remove entry from Hash
  *harray = $rlist->Hash->{ $rxn->stringID() };
  for my $i ( 0 .. $#harray ) {
    if ( $rxn == $harray[$i] ) {

      #printf "Deleting rxn from hash %s\n", $rxn->toString();
      splice( @harray, $i, 1 );
      last;
    }
  }

# Delete species that depend only on this reaction for production
# Species with non-zero concentration must exist
# Species with zero concentration must appear as product in at least one reaction
#  *phash = $rlist->AsProduct;
#  for my $spec ( @{ $rxn->Products } ) {
#    if ( ( --$phash{$spec} ) == 0 ) {
#
#      # Remove species from SpeciesList if it has zero concentration
#      $rlist->SpeciesList->remove($spec);
#    }
#  }

  return;
}

sub readString {
  my $rlist  = shift;
  my $string = shift;
  my $slist  = shift;
  my $plist  = shift;

  my @tokens = split( ' ', $string );
  my $rxn = Rxn->new();
  my $err;

  *species = $slist->Array;
  my $nspec = scalar(@species);

  # Check if token is an index
  if ( $tokens[0] =~ /^\d+$/ ) {
    my $index = shift(@tokens);    # This index will be ignored
  }

  # Next token is list of reacant indices
  my @ptrs;
  if (@tokens) {
    @ptrs = ();
    my @inds = split( ',', shift(@tokens) );
    for my $index (@inds) {
      push @ptrs, $species[ $index - 1 ];
      if ( $index < 0 || ( $index > $nspec ) ) {
        return ("Index $index to species in reaction out of range");
      }
    }
  }
  else {
    return ("Incomplete reactantion at reactants");
  }
  $rxn->Reactants( [@ptrs] );

  # Next token is list of reacant indices
  my @ptrs;
  if (@tokens) {
    @ptrs = ();
    my @inds = split( ',', shift(@tokens) );
    for my $index (@inds) {
      push @ptrs, $species[ $index - 1 ];
      if ( $index < 0 || ( $index > $nspec ) ) {
        return ("Index $index to species in reaction out of range");
      }
    }
  }
  else {
    return ("Incomplete reaction at products");
  }
  $rxn->Products( [@ptrs] );

# Next token is rate law
# This will create a separate RateLaw object for each reaction.  When reaction
# rules are used to generate Rxns, only one RateLaw is used per RxnRule.
# Information about which rule created the reacion is lost because of this.  Also,
# the statistical factor gets folded into the rate law rather than being part of the
# reaction, since it is not possible to separate the contributions to the overall
# weight.
  my $rl;
  if (@tokens) {
    my $rlstring = join( ' ', @tokens );

    #print "rlstring=$rlstring\n";
    ( $rl, $err ) = RateLaw::newRateLawNet( \$rlstring, $plist );
    if ($err) { return ($err); }
  }
  else {
    return ("Incomplete reaction at rate law");
  }
  $rxn->RateLaw($rl);

  # Set remaining attributes of rxn
  $rxn->StatFactor(1);
  $rxn->Priority(0);

  # Create new Rxn entry in RxnList
  my $n_add = $rlist->add($rxn);

  return ("");
}

sub writeBNGL {
  my $rlist = shift;
  my $text  = (@_) ? shift : 0;
  my $plist = (@_) ? shift : "";
  my $out   = "";

  $out .= "begin reactions";
  if ($text) {
    $out .= "_text";
  }
  $out .= "\n";
  my $irxn = 1;
  for my $rxn ( @{ $rlist->Array } ) {
    $out .= sprintf "%5d %s\n", $irxn, $rxn->toString( $text, $plist );
    ++$irxn;
  }
  $out .= "end reactions";
  if ($text) {
    $out .= "_text";
  }
  $out .= "\n";
  return ($out);
}

sub print {
  my $rlist   = shift;
  my $fh      = shift;
  my $i_start = (@_) ? shift : 0;

  print $fh "begin reactions\n";
  *rarray = $rlist->Array;
  for my $i ( $i_start .. $#rarray ) {
    my $rxn = $rarray[$i];
    printf $fh "%5d %s %s\n", $i - $i_start + 1, $rxn->toString();
  }
  print $fh "end reactions\n";
  return ("");
}

# Need join function to merge two lists.  Could make use of reaction
# precedence here if the rate law was suppressed from the string used to
# compare reactions

1;
