#!/usr/bin/perl

use FindBin;
use lib $FindBin::Bin;
use IO::Handle;
use BNGModel;
use SpeciesList;
use BNGUtils;
use strict;

my %params           = ();
my $generate_network = 0;
my $logging          = 0;

while ( $ARGV[0] =~ /^-/ ) {
  $_ = shift;
  if (/^-check$/) {
    $params{no_exec} = 1;
  }
  elsif (/^-log$/) {
    $logging = 1;
  }
  elsif (/^-v$/){
    printf "BioNetGen version %s\n", BNGversion();
    exit();    
  }
  elsif (/^-xml$/) {
    $params{write_xml} = 1;
  }
  elsif (/^-mfile$/) {
    $params{write_mfile} = 1;
  }
  elsif (/^-sbml$/) {
    $params{write_sbml} = 1;
  }
  else {
    exit_error("Unrecognized command line option $_");
  }
}

for my $file (@ARGV)
{
  # create BNGMOdel object
  my $model = BNGModel->new();
  
  my $t_start= cpu_time(0);
  $params{file} = $file;

  # Open logfile, if specified
  if ($logging) {
    # Default logfile name is base name of first bngl file plus .log suffix
    my $lbase = $file;
    $lbase =~ s/[.]([^.]+)$//;
    open OUTPUT, '>', "${lbase}.log" or die $!;
    STDOUT->fdopen( \*OUTPUT, 'w' ) or die $!;
  }
  # turn off output buffering on STDOUT
  ( select(*STDOUT), $| = 1 )[0];

  printf "BioNetGen version %s\n", BNGversion();

  if ( my $err = $model->readFile( \%params ) ) { exit_error($err); }
  printf "CPU TIME: total %.1f s.\n", cpu_time(0) - $t_start;
}


