# $Id: Expression.pm,v 1.7 2007/02/20 17:39:38 faeder Exp $

# updated by msneddon, 2009/11/04
#   -added if statement as built in function
#   -added binary logical operators, <,>.<=,>=,==,!=,~=,&&,||
#    to the basic functional parser, the toString function, and 
#    to the evaluate function

#   -todo: add binary operators to method toMathMLString function
#   -todo: add the unary operator not: '!'

package Expression;

use Class::Struct;
use FindBin;
use lib $FindBin::Bin;
use Param;
use ParamList;

struct Expression => {
  Type =>
    '$',    # Valid types are 'NUM', 'VAR', 'FUN', '+', '-', '*', '/', '^', '**',
            # '>','<','>=','<=','==','!=','~=','&&','||'
  Arglist => '@',
  Err     => '$',
};

%functions = (
  "exp"  => { FPTR => sub { exp( $_[0] ) },  NARGS => 1 },
  "cos"  => { FPTR => sub { cos( $_[0] ) },  NARGS => 1 },
  "sin"  => { FPTR => sub { sin( $_[0] ) },  NARGS => 1 },
  "log"  => { FPTR => sub { log( $_[0] ) },  NARGS => 1 },
  "abs"  => { FPTR => sub { abs( $_[0] ) },  NARGS => 1 },
  "int"  => { FPTR => sub { int( $_[0] ) },  NARGS => 1 },
  "sqrt" => { FPTR => sub { sqrt( $_[0] ) }, NARGS => 1 },
  "if" => { FPTR => sub { if($_[0]) { $_[1] } else { $_[2] } }, NARGS => 3 }, #added line, msneddon
);

my $MAX_LEVEL = 100;    # Prevent infinite loop due to dependency loops

{
  my $string_sav;
  my %variables;
  my $allowForward = 0;

  sub setAllowForward {
    my $expr = shift;
    $allowForward = shift;

    #print "allowForward=$allowForward\n";
  }

  sub readString {
    my $expr      = shift;
    my $sptr      = shift;
    my $plist     = (@_) ? shift : "";
    my $end_chars = (@_) ? shift : "";
    my $level     = (@_) ? shift : 0;
    my $err       = "";
    my $ops_bi    = '\*\*|[+\-*/^]|>=|<=|[<>]|==|!=|~=|&&|\|\|';  #edited, msneddon
    my $ops_un    = '[+-]';

    if ( !$level ) {
      $string_sav = $$sptr;
      %variables  = ();
    }

    # parse string into form expr op expr op ...
    # a+b*(c+d)
    # -5.0e+3/4
    my $last_read       = "";
    my $expr_new        = "";
    my @list            = ();
    my $expect_op       = 0;
    my $assign_var_name = "";
    while ( $$sptr ne "" ) {

      #print "string=$$sptr\n";
      if ( $expect_op == 1 ) {

        # OPERATOR
        if ( $$sptr =~ s/^\s*($ops_bi)// ) {
          my $express = Expression->new();
          $express->Type($1);
          push @list, $express;
          $expect_op = 0;
          next;
        }

        # Assignment using '='.  Valid syntax is VAR = EXPRSSION
        elsif ( $$sptr =~ s/^\s*=// ) {

          # Check that only preceding argument is variable
          my $var             = $list[$#list];
          my $vname           = $var->Arglist->[0];
          my $var_count_start = $variables{$vname};
          if ( $#list > 0 ) {
            return (
"Invalid assignment syntax (VAR = EXPRESSION) in $string_sav at $$sptr"
            );
          }

          if ( $var->Type ne 'VAR' ) {
            return (
"Attempted assignment to non-variable type in $string_sav at $$sptr."
            );
          }

          # Read remainder as expression
          my $rhs = Expression->new();
          $err = $rhs->readString( $sptr, $plist, $end_chars, $level + 1 );
          $err && return ($err);

          # Perform assignment of expression to variable
          # Evaluate rhs if lhs VAR occurs on rhs
          if ( $variables{$vname} > $var_count_start ) {
            $plist->set( $vname, $rhs->evaluate($plist) );
          }

          # otherwise, set variable to rhs.
          else {
            $plist->set( $vname, $rhs );
          }

          #print "Evaluates to ", $expr->evaluate($plist),"\n";
          #print "Prints    to ", $expr->toString($plist),"\n";
          last;
        }

        # Look for end characters
        elsif ( $end_chars && ( $$sptr =~ /^\s*${end_chars}/ ) ) {
          last;
        }
        else {
          $$sptr =~ s/^\s*//;

          #last unless ($$sptr);
          last;
          return ("Expecting operator in expression $string_sav at $$sptr");
        }
      }

      # Chop leading whitespace
      $$sptr =~ s/^\s*//;

      # NUMBER
      if ( my $express = getNumber($sptr) ) {

        #printf "Read NUM %s\n", $express->evaluate();
        push @list, $express;
        $expect_op = 1;
        next;
        0;
      }

      # FUNCTION
      if ( $$sptr =~ s/^($ops_un)?\s*([A-Za-z0-9_]+)\s*\(// ) {
        if ( my $op = $1 ) {

          # (optional) UNARY OP at start of expression, as in -a + b, or -a^2
          my $express_u = Expression->new();
          $express_u->Type($1);
          push @list, $express_u;
        }
        my $name  = $2;
        my @fargs = ();
        my $type  = "";
        my $nargs;
        my ($param, $err);
        if ($plist) { 
          ($param, $err)= $plist->lookup($name); 
        }
        if ( defined( $functions{$name} ) ) {
          $type  = "B";
          $nargs = $functions{$name}->{NARGS};
        }
        elsif ( $param && ( $param->Type eq "Observable" ) ) {
          $type = "O";
          # number of args may be zero or one.
        }
        elsif ( $param && ( $param->Type eq "Function" ) ) {
          $type  = "F";
          $nargs = scalar( @{ $param->Ref->Args } );
        }
        else {
          return (
"Function $name is not a built-in function, Observable, or defined Function"
          );
        }

        # Read arguments to function
        while (1) {
          my $express = Expression->new();
          $err = $express->readString( $sptr, $plist, ',\)', $level + 1 );
          $err && return ($err);
          if ($express->Type){ push @fargs, $express;}
          if ( $$sptr =~ s/^\)// ) {
            last;
          }
          elsif ( $$sptr =~ s/^,// ) {
            next;
          }
        }

        # Check Argument list for consistency with function
        if ( $type eq "O" ) {
          my $nargs= scalar(@fargs);
          if  ($nargs>1){
            return ("Observables $name is called with too many arguments");
          }
          elsif ($nargs==1){
            # Arugument must be VAR
            if ($fargs[0]->Type ne "VAR"){
              return("Argument to observable must be a variable");
            }
            # Argument to Observable must be Local type
            (my $lv) = $plist->lookup($fargs[0]->Arglist->[0]);
            if ($lv->Type ne "Local"){
              return("Argument to observable must be a local variable");
            }
          }
        }
        else {
          if ( $nargs != scalar(@fargs) ) {
            return ("Incorrect number of arguments to function $name");
          }
        }
        my $express = Expression->new();
        $express->Type('FUN');
        $express->Arglist( [ $name, @fargs ] );
        push @list, $express;
        $expect_op = 1;
        next;
      }

      # VARIABLE
      elsif ( $$sptr =~ s/^($ops_un)?\s*([A-Za-z0-9_]+)// ) {
        if ( my $op = $1 ) {

          # (optional) UNARY OP at start of expression, as in -a + b, or -a^2
          my $express_u = Expression->new();
          $express_u->Type($1);
          push @list, $express_u;
        }
        my $name = $2;

        # Validate against ParamList, if present
        if ($plist) {

          # Create and set variable if next token is '='
          # otherwise create referenced variable but leave its Expr unset
          if ( !( $$sptr =~ /^\s*=/ ) ) {
            my ( $param, $err ) = $plist->lookup($name);
            if ( !$param ) {
              if ($allowForward) {
                $plist->set($name);
              }
              else {
                return ("Can't reference undefined parameter $name");
              }
            }
          }
        }
        else {
          return ("No parameter list provided");
        }
        my $express = Expression->new();
        $express->Type('VAR');
        $express->Arglist( [$name] );
        ++$variables{$name};
        push @list, $express;
        $expect_op = 1;
        next;
      }

      # Get expression enclosed in parenthesis
      elsif ( $$sptr =~ s/^($ops_un)?\s*\(// ) {
        if ( my $op = $1 ) {

          # (optional) UNARY OP at start of expression, as in -a + b, or -a^2
          my $express_u = Expression->new();
          $express_u->Type($1);
          push @list, $express_u;
        }
        my $express = Expression->new();
        $err = $express->readString( $sptr, $plist, '\)', $level + 1 );
        $err && return ($err);
        if ( !( $$sptr =~ s/^\s*\)// ) ) {
          return ("Missing end parenthesis in $string_sav at $$sptr");
        }

        #printf "express=%s %s\n", $express->toString($plist), $$sptr;
        push @list, $express;
        $expect_op = 1;
        next;
      }
      elsif ( $end_chars && ( $$sptr =~ /^\s*[${end_chars}]/)){
          last;
      }
      # ERROR
      else {
        return ("Expecting operator argument in $string_sav at $$sptr");
      }
    }

    # Transform list into expression preserving operator precedence
    if (@list){ $expr->copy( arrayToExpression(@list) );}

    return ($err);
  }
}

{

  sub depends {
    my $expr    = shift;
    my $plist   = shift;
    my $varname = shift;
    my $level   = (@_) ? shift : 0;
    my $dep     = (@_) ? shift : {};

    my $retval = "";
    my $err    = "";

    my $type = $expr->Type;
    if ( $type eq 'NUM' ) {
    }
    elsif ( $type eq 'VAR' ) {

      #printf "type=$type %s\n", $expr->toString($plist);
      my $vname = $expr->Arglist->[0];

      #print "$varname $vname\n";
      if ( $$dep{$vname} ) {
        $err = sprintf "Cycle in parameter $vname looking for dep in %s",
          $varname;
        print "$err\n";
        $retval = $vname;
      }
      else {

        #++$$dep{$vname};
        if ( $varname eq $vname ) {
          $retval = $vname;
        }
        elsif ($plist) {
          my $param;
          ( $param, $err ) = $plist->lookup($vname);
          my %newdep = %{$dep};
          $newdep{$nvame} = 1;
          ( my $ret, $err ) = ($param ne "") ?
            ( $param->Expr->depends( $plist, $varname, $level + 1, \%newdep ) ) :
            ("","");
          if ($ret) {
            $retval = $param->Name . '->' . $ret;
          }
        }
      }
    }
    else {
      my @arglist = @{ $expr->Arglist };

      # Skip function name if this is a function
      if ( $type eq 'FUN' ) { shift(@arglist); }
      for my $e (@arglist) {
        ( $retval, $err ) =
          ( $e->depends( $plist, $varname, $level + 1, $dep ) );
        last if $retval;
      }
    }

    #print "level=$level $retval $err\n";
    return ( $retval, $err );
  }
}

sub copy {
  my $edest   = shift;
  my $esource = shift;

  $edest->Type( $esource->Type );
  $edest->Arglist( [ @{ $esource->Arglist } ] );
  return ($edest);
}

sub evaluate {
  my $expr  = shift;
  my $plist = (@_) ? shift : "";
  my $level = shift;

  ( $level > $MAX_LEVEL ) && die "Max recursion depth $MAX_LEVEL exceeded.";

  my $val;
  my $alist = $expr->Arglist;
  my $type  = $expr->Type;
  if ( $type eq 'NUM' ) {
    $val = $$alist[0];
  }
  elsif ( $type eq 'VAR' ) {
    if ( !$plist ) {
      $expr->Err("Variable is referenced but not ParamList is provided.");
      return ("");
    }

    $val = $plist->evaluate( $$alist[0], $level + 1 );
    if ($err) {
      $expr->Err($err);
      return ("");
    }
  }
  elsif ( $type eq 'FUN' ) {
    my @args  = @{ $expr->Arglist };
    my $name  = shift(@args);
    my $f     = $functions{$name}->{FPTR};
    my @fargs = ();
    for my $arg (@args) {
      push @fargs, $arg->evaluate($plist);
    }
    $val = $f->(@fargs);
  }
  elsif ( $type eq '+' ) {
    for my $e (@$alist) {
      $x = $e->evaluate( $plist, $level + 1 );
      $val += $x;
    }
  }
  elsif ( $type eq '*' ) {
    $val = 1;
    for my $e (@$alist) {
      $val *= $e->evaluate( $plist, $level + 1 );
    }
  }
  elsif ( $type eq '-' ) {
    if ( $#$alist == 0 ) {
      $val = -$$alist[0]->evaluate( $plist, $level + 1 );
    }
    else {
      $val =
        $$alist[0]->evaluate( $plist, $level + 1 ) -
        $$alist[1]->evaluate( $plist, $level + 1 );
    }
  }
  elsif ( $type eq '/' ) {
    if ( $#$alist == 0 ) {
      $val = 1 / $$alist[0]->evaluate( $plist, $level + 1 );
    }
    else {
      $val =
        $$alist[0]->evaluate( $plist, $level + 1 ) /
        $$alist[1]->evaluate( $plist, $level + 1 );
    }
  }
  elsif ( $type eq '^' ) {
    $val =
      ( $$alist[0]->evaluate( $plist, $level + 1 ) )
      **( $$alist[1]->evaluate( $plist, $level + 1 ) );
  }
  elsif ( $type eq '**' ) {
    $val =
      ( $$alist[0]->evaluate( $plist, $level + 1 ) )
      **( $$alist[1]->evaluate( $plist, $level + 1 ) );
  }

  #BEGIN added, msneddon
  elsif ( $type eq '<' ) {
    if(( $$alist[0]->evaluate( $plist, $level + 1 ) )
      < ( $$alist[1]->evaluate( $plist, $level + 1 ) ))
    { $val = 1; }
    else {$val = 0; }
  }
  elsif ( $type eq '>' ) {
    if(( $$alist[0]->evaluate( $plist, $level + 1 ) )
      > ( $$alist[1]->evaluate( $plist, $level + 1 ) ))
    { $val = 1; }
    else {$val = 0; }
  }
  elsif ( $type eq '>=' ) {
    if(( $$alist[0]->evaluate( $plist, $level + 1 ) )
      >= ( $$alist[1]->evaluate( $plist, $level + 1 ) ))
    { $val = 1; }
    else {$val = 0; }
  }
  elsif ( $type eq '<=' ) {
    if(( $$alist[0]->evaluate( $plist, $level + 1 ) )
      <= ( $$alist[1]->evaluate( $plist, $level + 1 ) ))
    { $val = 1; }
    else {$val = 0; }
  }
  elsif ( $type eq '==' ) {
    if(( $$alist[0]->evaluate( $plist, $level + 1 ) )
      == ( $$alist[1]->evaluate( $plist, $level + 1 ) ))
    { $val = 1; }
    else {$val = 0; }
  }
  elsif ( $type eq '!=' ) {
    if(( $$alist[0]->evaluate( $plist, $level + 1 ) )
      != ( $$alist[1]->evaluate( $plist, $level + 1 ) ))
    { $val = 1; }
    else {$val = 0; }
  }
  elsif ( $type eq '~=' ) {
    if(( $$alist[0]->evaluate( $plist, $level + 1 ) )
      != ( $$alist[1]->evaluate( $plist, $level + 1 ) ))
    { $val = 1; }
    else {$val = 0; }
  }
  elsif ( $type eq '&&' ) {
    if(( $$alist[0]->evaluate( $plist, $level + 1 ) )
      && ( $$alist[1]->evaluate( $plist, $level + 1 ) ))
    { $val = 1; }
    else {$val = 0; }
  }
  elsif ( $type eq '||' ) {
    if(( $$alist[0]->evaluate( $plist, $level + 1 ) )
      || ( $$alist[1]->evaluate( $plist, $level + 1 ) ))
    { $val = 1; }
    else {$val = 0; }
  }
  #END added, msneddon


  else {
    $expr->Err( "Unrecognized Expression type " . $type );
    print "Warning when evaluating function: Unrecognized Expression type: $type\n"; #added, msneddon
    return ("");
  }

  return ($val);
}

sub toString {
  my $expr  = shift;
  my $plist = (@_) ? shift : "";
  my $level = (@_) ? shift : 0;

  ( $level > $MAX_LEVEL ) && die "Max recursion depth $MAX_LEVEL exceeded.";

  my $string = "";
  my $type   = $expr->Type;
  if ( $type eq 'NUM' ) {
    $string = $expr->Arglist->[0];

    #print "NUM=$string\n";
  }
  elsif ( $type eq 'VAR' ) {
    $string = $expr->Arglist->[0];

    #$string= $expr->evaluate($plist);
    #print "VAR=$string\n";
  }
  elsif ( $type eq 'FUN' ) {
    my @sarr = ();
    for my $i ( 1 .. $#{ $expr->Arglist } ) {
      push @sarr, $expr->Arglist->[$i]->toString( $plist, $level + 1 );
    }
    $string = $expr->Arglist->[0] . '(' . join( ',', @sarr ) . ')';
  }
  else {
    my @sarr = ();
    for my $e ( @{ $expr->Arglist } ) {
      push @sarr, $e->toString( $plist, $level + 1 );
    }
    if ( $#sarr > 0 ) {
      $string = join( $type, @sarr );
    }
    else {
      $string = $type . $sarr[0];
    }

    # enclose in brackets if not at top level
    #    print "level=$level\n";
    if ($level) {
      $string = '(' . $string . ')';
    }

    #printf "%s=$string\n", $expr->Type;
  }

  #BEGIN edit, msneddon
  # for outputting to XML, we need to make sure we put in some special
  # characters and operators to match the muParser library and to allow
  # the XML parser to work.<" with "&lt;", ">" with "&gt;", and
  #"&" with "&amp
  #print "before XML replacement: $string\n";
  $string =~ s/</&lt\;/;
  $string =~ s/>/&gt\;/;
  $string =~ s/&&/and/;
  $string =~ s/\|\|/or/;
  #print "after XML replacement: $string\n";
  #END edit, msneddon


  return ($string);
}


sub toMexString
{
    my $expr  = shift;
    my $plist = (@_) ? shift : "";
    my $level = (@_) ? shift : 0;

    ( $level > $MAX_LEVEL ) && die "Max recursion depth $MAX_LEVEL exceeded.";

    my $string = '';
    my $type   = $expr->Type;
    
    if ( $type eq 'NUM' )
    {
        $string = $expr->Arglist->[0];
    }
    elsif ( $type eq 'VAR' )
    {
        $string = $expr->Arglist->[0];
    }
    elsif ( $type eq 'FUN' )
    {
        my @sarr = ();
        foreach my $i ( 1 .. $#{ $expr->Arglist } )
        {
            my $sarr = $expr->Arglist->[$i]->toMexString( $plist, $level + 1 );
            $sarr =~ s/^(\d+)$/$1.0/;
            push @sarr, $sarr;
        }
        push @sarr, 'expressions', 'observables';
        $string = $expr->Arglist->[0] . '(' . join( ',', @sarr ) . ')';
    }
    elsif ( ($type eq '**') or ($type eq '^') )
    {
        my @sarr = ();
        foreach my $e ( @{ $expr->Arglist } )
        {
            my $sarr = $e->toMexString( $plist, $level + 1 );
            $sarr =~ s/^(\d+)$/$1.0/;
            push @sarr, $sarr;
        }
    
        if ( @sarr == 2 )
        {
            $string = 'pow(' . $sarr[0] . ',' . $sarr[1] . ')';
        }
        else
        {
            #error!
        }
    }    
    else
    {
        my @sarr = ();
        foreach my $e ( @{ $expr->Arglist } )
        {
            my $sarr = $e->toMexString( $plist, $level + 1 );
            $sarr =~ s/^(\d+)$/$1.0/;
            push @sarr, $sarr;
        }
        
        if ( $#sarr > 0 )
        {
            $string = join( $type, @sarr );
        }
        else
        {
            $string = $type . $sarr[0];
        }

        # enclose in brackets (always. just to be safe)
        $string = '(' . $string . ')';
    }

    return ($string);
}



sub toMatlabString
{
   my $expr  = shift;
   my $plist = (@_) ? shift : '';
   my $level = (@_) ? shift : 0;

   ( $level > $MAX_LEVEL ) && die "Max recursion depth $MAX_LEVEL exceeded.";

   my $string = '';
   my $type = $expr->Type;
      
   if ( $type eq 'NUM' )
   {  $string = $expr->Arglist->[0];  }
   elsif ( $type eq 'VAR' )
   {
      my ($pref, $err) = $plist->lookup( $expr->Arglist->[0] );
      if    ( $pref->Type eq 'Constant'  or  $pref->Type eq 'ConstantExpression' )
      {  $string = $pref->Name;  }
      elsif ( $pref->Type eq 'Observable' )
      {  $string = $pref->Ref->toMatlabString( $plist );  }
      elsif ( $pref->Type eq 'Local' )
      {  $string = $pref->Expr->toMatlabString( $plist );  }       
      else
      {  $string = '';  }
   }
   elsif ( $type eq 'FUN' )
   {
      my ($fref, $err) = $plist->lookup( $expr->Arglist->[0] );
      $string = $fref->Ref->toMatlabString( [@{$expr->Arglist}[1..$#{$expr->Arglist}]],
                                            $plist );
      #my @sarr = ();
      #for my $i ( 1 .. $#{ $expr->Arglist } )
      #{  push @sarr, $expr->Arglist->[$i]->toMatlabString( $plist, $level + 1 );  }
      #$string = $expr->Arglist->[0] . '(' . join( ',', @sarr ) . ')';
   }
   else
   {
      my @sarr = ();
      foreach my $e ( @{ $expr->Arglist } )
      {  push @sarr, $e->toMatlabString( $plist, $level + 1 );  }
      if ( $#sarr > 0 )
      {  $string = join( $type, @sarr );  }
      else
      {  $string = $type . $sarr[0];  }

      # enclose in brackets if not at top level
      if ($level)
      {  $string = '(' . $string . ')';  }
   }

   return $string;
}


{
  my %ophash = (
    '+'  => 'plus',
    '-'  => 'minus',
    '*'  => 'times',
    '/'  => 'divide',
    '**' => 'power',
    '^'  => 'power',
  );

  sub toMathMLString {
    my $expr   = shift;
    my $plist  = (@_) ? shift : "";
    my $indent = (@_) ? shift : "";
    my $level  = (@_) ? shift : 0;

    ( $level > $MAX_LEVEL ) && die "Max recursion depth $MAX_LEVEL exceeded.";

    my $string  = "";
    my $indentp = $indent;
    if ( $level == 0 ) {
      $string .=
        $indent . "<math xmlns=\"http://www.w3.org/1998/Math/MathML\">\n";
      $indentp .= "  ";
    }

    my $type = $expr->Type;
    if ( $type eq 'NUM' ) {
      $string .= sprintf "%s<cn> %s </cn>\n", $indentp, $expr->Arglist->[0];
    }
    elsif ( $type eq 'VAR' ) {
      $string .= sprintf "%s<ci> %s </ci>\n", $indentp, $expr->Arglist->[0];
    }
    elsif ( $type eq 'FUN' ) {
      $string .= $indentp . "<apply>\n";
      my $indentpp = $indentp . "  ";
      my @arglist  = @{ $expr->Arglist };
      $string .= sprintf "%s<%s/>\n", $indentpp, shift(@arglist);
      for my $e (@arglist) {
        $string .= $e->toMathMLString( $plist, $indentpp, $level + 1 );
      }
      $string .= $indentp . "</apply>\n";
    }
    else {
      $string .= $indentp . "<apply>\n";
      my $indentpp = $indentp . "  ";
      $string .= sprintf "%s<%s/>\n", $indentpp, $ophash{ $expr->Type };
      for my $e ( @{ $expr->Arglist } ) {
        $string .= $e->toMathMLString( $plist, $indentpp, $level + 1 );
      }
      $string .= $indentp . "</apply>\n";
    }

    if ( $level == 0 ) {
      $string .= $indent . "</math>\n";
    }
    return ($string);
  }
}

# Convert an array of type EXPR OP EXPR OP ... to a single Expression.
sub arrayToExpression {
  my @earr = @_;

  # list of optypes in order of precedence
  my @operators = ( '\*\*|\^', '[*/]', '[+-]','[<>]|==|!=|~=|>=|<=','&&|\|\|'); #edited, msneddon
  my $optype = shift @operators;
  while ($optype) {
    my $i = 0;

    # Consolidate EXPR OP EXPR into EXPR
    while ( $i < $#earr ) {
      my $expr = $earr[$i];
      if ( $expr->Type =~ /$optype/ && !( @{ $expr->Arglist } ) ) {
        if ( $i > 0 ) {
          $expr->Arglist->[0] = $earr[ $i - 1 ];
          $expr->Arglist->[1] = $earr[ $i + 1 ];
          splice @earr, $i - 1, 3, $expr;
          next;
        }
        else {

          # Handle leading unary op, as in -a + b
          $expr->Arglist->[0] = $earr[ $i + 1 ];
          splice @earr, $i, 2, $expr;
          ++$i;
          next;
        }
      }
      ++$i;
    }

    #print "expression after $optype= ";
    #foreach $expr (@earr){
    # printf " %s", $expr->toString();
    #}
    #print "\n";
    # Finished with current optype
    $optype = shift @operators;
  }

  #printf "final expression= %s\n", $earr[0]->toString();

  return ( $earr[0] );
}

sub getNumber {
  my $string = shift;
  my $number = "";

  # Decimal part
  if ( $$string =~ s/^([+-]?\d+)([.]?\d*)// ) {
    $number = $1;
    if ($2 eq '.'){
      # pad number ending in decimal point
      $number .= ".0";
    } else {
      $number .= $2;
    }
  }
  elsif ( $$string =~ s/^([+-]?[.]\d+)// ) {
    $number = $1;
  }
  else {
    return ("");
  }

  # Exponent part
  if ( $$string =~ s/^([DEFGdefg][+-]?\d+)// ) {
    $number .= $1;
  }
  elsif ( $$string =~ /^[A-Za-z_]/ ) {

    # String is non a number; restore value of string
    $$string = $number . $$string;

    #print "gothere: $$string\n";
    return ("");
  }
  my $express = Expression->new();
  $express->Type('NUM');
  $express->Arglist( [$number] );
  return ($express);
}

# Returns name of VAR if expression is an existing VAR or
# creates a new VAR with name derived from $basename and 
# returns name of new VAR containing expression.
sub getName {
  my $expr     = shift;
  my $plist    = shift;
  my $basename = (@_) ? shift : "k";
  my $name;

  if ( $expr->Type eq 'VAR' ) {
    $name = $expr->Arglist->[0];

    #printf "Found existing parameter %s\n", $name;
  }
  else {
    # Find unused name
    my $index = 1;
    while (1) {
      my ( $param, $err ) = $plist->lookup( $basename . $index );
      last unless $param;
      ++$index;
    }
    $name = $basename . $index;
    $plist->set( $name, $expr );

    #printf "Creating new parameter %s\n", $name;
  }

  return ($name);
}

# Return a hash of all the variable names referenced in the current expression.
 
sub getVariables {
  my $expr    = shift;
  my $plist   = shift;
  my $level   = (@_) ? shift : 0;
  my $rethash = (@_) ? shift : "";
#  use Data::Dumper;

  ( $level > $MAX_LEVEL ) && die "Max recursion depth $MAX_LEVEL exceeded.";

  if ( $level == 0 ) {
    $rethash = {};
  }

  my $type = $expr->Type;
  if ( $type eq 'NUM' ) {
  }
  elsif ( $type eq 'VAR' ) {
    my ( $param, $err ) = $plist->lookup( $expr->Arglist->[0] );
    if ($err) { die $err }
    ;    # Shouldn't be an undefined variable name here
    $rethash->{ $param->Type }->{ $param->Name }= $param;
  }
  elsif ( $type eq 'FUN' ) {
    my ( $param, $err ) = $plist->lookup( $expr->Arglist->[0] );
    if ($err){
      # function is a built-in      
    }
    else {
      $rethash->{$param->Type}->{ $param->Name }= $param;
    }
    for my $i ( 1 .. $#{ $expr->Arglist } ) {
      $expr->Arglist->[$i]->getVariables( $plist, $level + 1, $rethash );
    }
  }
  else {
    for my $e ( @{ $expr->Arglist } ) {
      $e->getVariables( $plist, $level + 1, $rethash );
    }
  }

  ( $level > 0 ) && return ();

#  print Dumper($rethash);
  return ($rethash);
}

1;
