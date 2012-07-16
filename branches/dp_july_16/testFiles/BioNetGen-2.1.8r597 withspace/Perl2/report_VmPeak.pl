#!/usr/bin/perl

use strict;
use warnings;

# turn off buffering
select STDOUT;
$| = 1;

# get pid and time interval
my ($pid, $dt) = @ARGV;

# define command for retrieving VmPeak
my $get_VmPeak = "cat /proc/${pid}/status | grep Vm";

# report VmPeak loop
while (1)
{
    # check memory before process has a chance to close
    ($VmPeak) = (`$get_VmPeak` =~ /^VmPeak:\s*(\d+) kB/);

    # exit loop if we couldn't find VmPeak for $pid
    last unless ( defined $VmPeak );

    # report VmPeak
    print STDOUT $VmPeak;
 
    # wait a little before checking again
    sleep $dt;
}

exit(0);       
