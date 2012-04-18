# $Id: Param.pm,v 1.3 2006/09/26 03:36:00 faeder Exp $

package Param;

use Class::Struct;
use FindBin;
use lib $FindBin::Bin;
use ParamList;
use Expression;

struct Param => {
  Name=> '$',
  Expr=>'Expression',
  Type=> '$', # See allowedTypes below
  Ref=> '$' # For Observables and Functions
};

my %allowedTypes=(Constant=>1, # A number or an expression involving only numbers
            		ConstantExpression=>1, # An expression involving at least on other Constant or ConstantExpression
		            Observable=>1, # A variable defined by the name of an Observable - may take a single argument
		            Function=>1, # A function that takes variable arguments
                  Local=>1); # local arguments to functions
sub evaluate
{
   my $param = shift;
   my $plist = (@_) ? shift : '';

   if ($param->Expr)
   {  return $param->Expr->evaluate($plist);  }
   return '';
}


sub setType{
  my $param= shift;
  my $type= shift;
  my $ref= (@_) ? shift : "";

  if (!defined($allowedTypes{$type})){
    die("Type $type not recognized in setType");
  }

  $param->Type($type);
  if ($ref) { param->Ref($ref); }
  return("");
}


sub toString{
  my $param = shift;
  my $plist = (@_) ? shift : '';
  
  if ($param->Expr)
  {  return $param->Expr->toString($plist);  }
  else
  {  return '';  }
}


sub toMexString
{
    my $param = shift;
    my $plist = (@_) ? shift : '';
  
    if ($param->Type eq 'Constant')
    {   return $param->Name;   }
    else
    {
        if ($param->Expr)
        {   return $param->Expr->toMexString($plist);  }
        else
        {   return '';  }
    }
}


sub toMatlabString{
  my $param = shift;
  my $plist = (@_) ? shift : '';
  
  my $type = $param->Type;
  
  if ($type eq 'Constant'  or  $type eq 'ConstantExpression' )
  {  return $param->Expr->toMatlabString($plist);  }
  elsif ($type eq 'Function')
  {  return $param->Ref->toMatlabString($plist);  }
  elsif ($type eq 'Observable')
  {  return $param->Ref->toMatlabString($plist);  }  
  elsif ($type eq 'Local') 
  {  return $param->Expr->toMatlabString($plist);  }
  else
  {  return '';  }
}


sub toXMLReference{
  my $param= shift;
  my $head= shift;
  my $id= shift;
  my $plist= (@_) ? shift : "";
  
  if ($head eq ""){ $head= "Reference";}
  my $string="<$head";

  # Attributes
  # id
  if ($id ne ""){
    $string.= " id=\"".$param->Name."\"";
  }
  # name 
  $string.= " name=\"".$param->Name."\"";
  # type
  $string.= " type=\"".$param->Type."\"";
  
  $string.= "/>\n";
  
  return($string);  
}


sub toMathMLString{
  my $param= shift;
  my $plist= (@_) ? shift : "";
  my $indent= (@_) ? shift : "";

  if ($param->Expr){
    return($param->Expr->toMathMLString($plist,$indent));
  }
  return("");
}

1;
