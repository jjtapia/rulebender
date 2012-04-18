# $Id: ParamList.pm,v 1.11 2007/08/24 15:10:13 faeder Exp $

package ParamList;

use Class::Struct;
use FindBin;
use lib $FindBin::Bin;
use Param;
use Expression;
use BNGUtils("isReal","send_warning");

struct ParamList => {
  Array=> '@',
  Hash=> '%',
  Unchecked=>'@'		     
};

sub lookup{
  my $plist= shift;
  my $name= shift;
  my $param;
  
  if (defined($plist->Hash->{$name})){
    return($plist->Hash->{$name},"");
  } else {
    return('',"Parameter $name not defined");
  }
}

sub evaluate{
  my $plist=shift;
  my $string= shift;

  if (defined($plist->Hash->{$string})){
    return($plist->Hash->{$string}->evaluate($plist));
  }
  elsif (isReal($string)){
    return($string);
  } else {
    return("$string is not a valid number or parameter.");
  }
}

sub readString{
  my $plist=shift;
  my $string= shift;
  my $allow_undefined= (@_) ? shift : 0;

  my $sptr= \$string;
  my ($name, $val);

  # Remove leading index and whitespace
  $$sptr=~ s/^\s*\d+\s+//;

  # Convert non assignment format to assignment
  if (!($$sptr=~ /^\s*\S+\s*=/)){
    if (!($$sptr=~ s/^\s*(\S+)\s+/$1=/)){
      return("Invalid parameter declaration $$sptr: format is [index] name[=]Expression");
    }
  }

  # Read expression
  my $expr= Expression->new();
  $allow_undefined=1;
  $expr->setAllowForward($allow_undefined);
  if (my $err=$expr->readString($sptr,$plist)){return($err)}
  $$sptr && return("Syntax error at $$sptr");
  $expr->setAllowForward(0);

  return("");
}

# By default, allows previously defined variable to be overwritten.  Use $no_overwrite=1 to stop.
sub set{
  my $plist=shift;
  my $name= shift;
  my $rhs= (@_) ? shift : "";
  my $no_overwrite= (@_) ? shift : 0;
  my $type= (@_) ? shift : ""; # Overrides derived type of rhs
  my $ref= (@_) ? shift : ""; # Reference to Function or Observable

  # Find existing parameter
  my ($param,$err)= $plist->lookup($name);
  # or add new parameter to array
  if ($err){
    #print "Adding parameter $name\n";
    $param= Param->new(Name=>$name);
    if ($type ne "Local"){push @{$plist->Array}, $param;}
    $plist->Hash->{$name}= $param;
    # Add parameter to list of parameters to be checked
    if ($type ne "Local"){push @{$plist->Unchecked}, $param;}
    # Return leaving param unset if no rhs
    if ($rhs eq ""){
      return("");
    }
  }
  
  if ($param->Expr ne ""){
    if ($no_overwrite){
      return("Changing value of previously defined variable $name is not allowed");
    } else {
      send_warning("Changing value of previously defined variable $name");
    }
  }

  # Handle scalar (string) argument (probably from setParameter)
  if (ref(\$rhs) eq "SCALAR"){
    my $expr= Expression->new(Type=>'NUM',Arglist=>[$rhs]);
    $rhs=$expr;
  } 

  # Set Param->Expression
  $param->Expr($rhs);
  # Set Param->Type
  if ($type ne ""){
    $param->setType($type);
    # Set reference
    if ($ref){
      $param->Ref($ref);
    }
  }
  elsif ($rhs->Type eq 'NUM'){
    $param->setType("Constant");
  }
  else {
    # Get hash of variables reference in Expr
    my $vhash= $param->Expr->getVariables($plist);
    if ($vhash->{Observable} || $vhash->{Function}){
      $param->setType("Function");
      my $fun= Function->new();
      $fun->Name($param->Name);
      $fun->Expr($param->Expr);
      my @args= keys %{$vhash->{Local}};
      $fun->Args([@args]);
      $param->Ref($fun);
    }
    elsif ($vhash->{Constant} || $vhash->{ConstantExpression}){
      $param->setType("ConstantExpression");
    }
    else {
      # Expression contains only number arguments
      $param->setType("Constant");
    }
  }

  return("");
}
  
sub toString{
  my $plist=shift;
  my $out="";

  for my $param (@{$plist->Array}){
    $out.= sprintf "Parameter %s=%s\n", $param->Name, $param->toString($plist);
  }
  return($out);
}

# This serves an input file in SSC which contains information corresponding to our parameters block in BNG
sub writeSSCcfg{
  my $plist= shift;
  #my $NETfile= shift;
  my $out="";

  $out.="# begin parameters";
  $out.="\n";
  my $iparam=1;
  for my $param (@{$plist->Array}){
    my $type= $param->Type;
    next unless $type=~ /^Constant/;
    #$out.= sprintf "%5d", $iparam;
    $out.= " ".$param->Name;
    $out.= " = ";
    $out.= " ".$param->evaluate($plist);
    $out.= "\n";
    ++$iparam;
  }
  $out.="# end parameters";
  $out.="\n";

  return($out);
}


sub writeBNGL{
  my $plist= shift;
  my $NETfile= shift;
  my $out="";

  $out.="begin parameters";
  $out.="\n";
  my $iparam=1;
  for my $param (@{$plist->Array}){
    my $type= $param->Type;
    next unless $type=~ /^Constant/; 
    $out.= sprintf "%5d", $iparam;
    $out.= " ".$param->Name;
    if ($NETfile){
    	$out.= " ".$param->evaluate($plist);
    }	else {
    	$out.= " ".$param->toString($plist);
    }
    $out.= " # ".$type;
    $out.= "\n";
    ++$iparam;
  }
  $out.="end parameters";
  $out.="\n";

  return($out);
}

sub writeFunctions{
  my $plist= shift;
  my $out="";

  $out.="begin functions\n";
  my $iparam=1;
  for my $param (@{$plist->Array}){
		my $type= $param->Type;
    next unless $type eq "Function";
    $out.= sprintf "%5d", $iparam;
    $out.= " ".$param->Ref->toString($plist)."\n";
    ++$iparam;
  }
  $out.="end functions\n";
	# Don't output null block
  if ($iparam==1){$out="";}
 
  return($out);
}

# Delete a parameter from the ParamList by name
sub deleteLocal{
  my $plist= shift;
  my $pname= shift; 
    
  # Find parameter
  my ($param,$err)= $plist->lookup($pname);
  if ($err){return($err);}
  if ($param->Type ne "Local"){return("Parameter $pname is not a local parameter");}
  
  delete $plist->Hash->{$pname};
#  for (my $index=$#{$plist->Array}; $index>=0; --$index){
#    next unless ($param==$plist->Array->[$index]);
#    splice( @{$plist->Array}, $index, 1);
#  }
  undef $param;

  return("");
}

sub check{
  my $plist=shift;
  my $err="";
  
  for my $param (@{$plist->Unchecked}){
    # Check that variable has defined value
    #printf "Checking if parameter %s is defined.\n", $param->Name;
    if (!$param->Type){
      $err= sprintf "Parameter %s is referenced but not defined", $param->Name;
      last;
    }
  }
  $err && return($err);

  for my $param (@{$plist->Unchecked}){
    #printf "Checking parameter %s for cycles.\n", $param->Name;
    # Check that variable doesn't have cylic dependency
    (my $dep, $err)= ($param->Expr->depends($plist,$param->Name));
    if ($dep){
      $err= sprintf "Parameter %s has a dependency cycle %s", $param->Name, $param->Name.'->'.$dep;
      last;
    }
  }
  
  # Reset list of Unchecked parameters if all parameters passed checks.
  if (!$err){
    undef @{$plist->Unchecked};
    #printf "Unchecked=%d\n", scalar(@{$plist->Unchecked});
  }
  return($err);
}

{
  my $plist;
  my $err;
  sub sort{
    $plist=shift;
    $err="";
    
    $plist->Array([sort by_depends @{$plist->Array}]);
    return($err);
  }

  sub by_depends{
    (my $dep_a,$err)= $a->Expr->depends($plist,$b->Name);
    if ($err){
      #printf "$err %s %s\n", $a->Name, $b->Name;
      return(0);
    }

    if ($dep_a){
      #printf "%s depends on %s\n", $a->Name, $b->Name;
      return(1);
    }

    (my $dep_b,$err)= $b->Expr->depends($plist,$a->Name);
    if ($err){
      return(0);
    }
    if ($dep_b){
      #printf "%s depends on %s\n", $b->Name, $a->Name;
      return(-1);
    }

    #printf "%s and %s are independent\n", $a->Name, $b->Name;
    return(0);
  }
}

1;
