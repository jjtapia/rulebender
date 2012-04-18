package Function;

use Class::Struct;
use FindBin;
use lib $FindBin::Bin;
use ParamList;
use Expression;
use strict;

struct Function => {
  Name => '$',
  Args => '@',
  Expr => 'Expression',
};

sub readString
{
  my $fun    = shift;
  my $string = shift;
  my $model  = shift;
  my $err    = "";

  my $plist = $model->ParamList;

  # Check if first token is an index
  if ( $string =~ s/^\s*\d+\s+// ) {
    # This index will be ignored
  }

  # Next token is function Name
  
  if ( $string =~ s/^\s*([A-Za-z0-9_]+)\s*// ) {
    my $name = $1;
    $fun->Name($name);
  }
  else {
    my ($name) = split( ' ', $string );
    return ("Invalid function name $name: may contain only alphnumeric and underscore");
  }

  # Process arguments to function (if any)
  my @Args = ();
  if ( $string =~ s/^[(]\s*// ) {
    while (1) {
      if ( $string =~ s/^\s*([A-Za-z0-9_]+)\s*// ) {
        my $arg = $1;
        # Define argument as an allowed local variable in $plist
        if ( $plist->set( $arg, "0", 1, "Local" ) ) {
          my $name = $fun->Name;
          return ("Local argument $arg to Function $name matches previously defined variable");
        }
        #printf "Added argument %s to function %s\n", $arg, $fun->Name;
        push @Args, $arg;
      }
      elsif ( $string =~ s/^[,]\s*// ) {
        next;
      }
      elsif ( $string =~ s/^[)]\s*// ) {
        last;
      }
      else {
        my $name= $fun->Name;
        return ("Unrecognized argument at $string in declaration of function $name.");
      }
    }
    $fun->Args( [@Args] );
  }

  # Remove '=' if present
  $string=~ s/[=]\s*//;

  # Read expression defining function.  Function arguments are "local" variables
  my $expr = Expression->new();
  if ( my $err = $expr->readString( \$string, $plist ) ) { return ($err) }
  $string && return ("Syntax error at $string");
  $fun->Expr($expr);

  # Define parameter with name of the Function
  if ( $plist->set( $fun->Name, $expr, 1, "Function", $fun ) ) {
    my $name = $fun->Name;
    return ("Function name $name matches previously defined variable");
  }

  $fun->unsetArgs($plist);

  return '';
}

sub toString
{
   my $fun = shift;
   my $plist = (@_) ? shift : '';
   my $string = '';

   $string = $fun->Name . '(' . join(',', @{$fun->Args}) . ')';

   if ( $fun->Expr )
   {  $string .= ' '.$fun->Expr->toString($plist);  }

   return $string;
}


sub toMexString
{
   my $fun = shift;
   my $plist = (@_) ? shift : '';
   my $string = '';

   my @args = ( @{$fun->Args}, 'N_Vector expressions', 'N_Vector observables' );
   $string = $fun->Name . ' ( ' . join(', ', @args) . ' )';

   #if ( $fun->Expr )
   #{  $string .= ' '.$fun->Expr->toMexString($plist);  }

   return $string;
}


sub setArgs{
  my $fun= shift;
  my $plist= shift;
  
  for my $arg (@{$fun->Args}){
    $plist->set( $arg, "0", 1, "Local" ); 
  }
  return("");
}

sub unsetArgs{
  my $fun= shift;
  my $plist= shift;
  
   # Delete ParamList entries for Local arguments
  for my $arg (@{$fun->Args}){
    $plist->deleteLocal($arg);
  }
  return("");
}

sub toXML {
  my $fun = shift;
  my $plist = (@_) ? shift : "";
  my $indent= shift;

  $fun->setArgs($plist);

  my $indent2= "  ".$indent;
  my $indent3= "  ".$indent2;
  my $string=$indent."<Function";

  # Attributes
  # id
  $string.= " id=\"".$fun->Name."\"";
  $string.= ">\n";

  # Arguments
  if ($#{$fun->Args}>=0){
    $string.= $indent2."<ListOfArguments>\n";
    for my $arg (@{$fun->Args}){
      $string.= $indent3."<Argument";
      $string.= " id="."\"".$arg."\"";
      $string.= "/>\n"
    }
    $string.= $indent2."</ListOfArguments>\n";
  }

  # References
  $string.= $indent2."<ListOfReferences>\n";
  my $vhash= $fun->Expr->getVariables($plist);
  
  for my $type (keys %{$vhash}){
    for my $var (keys %{$vhash->{$type}}){
      #print "$type $var\n";
      $string.= $indent3.($vhash->{$type}->{$var})->toXMLReference("Reference", "", $plist),"\n";
    }    
  }
  $string.= $indent2."</ListOfReferences>\n";
    
  $string.= $indent2."<Expression> ";
  $string.= $fun->Expr->toString($plist);
  $string.= " </Expression>\n";

  $string.= $indent."</Function>\n";

  $fun->unsetArgs($plist);

  return ($string);
  
}


sub toMatlabString{
   my $fun = shift;
   my $in_args = shift;
   my $plist = (@_) ? shift : '';

   my $string = '';
   
   # set local arguments
   if (@$in_args)
   {
      for (my $ii = 0; $ii < @{$in_args}; $ii++ )
      {  $plist->set( $fun->Args->[$ii], $in_args->[$ii], 1, 'Local' );  }
   }

   if ( $fun->Expr )
   {  $string .= $fun->Expr->toMatlabString($plist); }

   # delete local arguments
   if (@$in_args)
   {
      for (my $ii = 0; $ii < @{$in_args}; $ii++ )
      {  $plist->deleteLocal( $fun->Args->[$ii] );  }
   }
      
   return $string;
}

1;
