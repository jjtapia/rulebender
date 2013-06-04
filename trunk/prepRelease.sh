#!/bin/bash
# This is a quick and dirty script for prepping the release files for RB.  It can be 
# improved or a more robust build system that works with eclipse builder (and is windows
# friendly) can be used in the long run, but for now this saves a ton of time.
# 
# Build Step 1: Make sure that BNG and NFSim are at most recent version.
# Build Step 2: Make sure that BNGParser.jar is at most recent version.
# Build Step 3: Prepare the eclipse files for release through the plugin development environment.
# Build Step 4: Put the files in the right place (see below) and 
# Build Step 5: run this script.

# The release that eclipse generates should be in rbReleaseDir ("RB-Release" here) in the 
# root rulebender trunk, and the script should also be run in the root (e.g. parent of
# rbReleaseDir).

# This script does the following:
# 1. Remove the jre folders from the eclipse output.
# 2. Create a 'zip' directory and move all of the eclipse outputs to that directory
#    while renaming the package names to RuleBender-<version>-<platform>
# 3. Copy the simulation resources (BNG, NFSim, and SampleModels) to each of the packages,
#    and delete the irrelevant binaries for each platform.
# 4. COPY the CREDITS.txt, RB-README.txt, and LICENSE.txt files. 

# version number only used for file names.  All other version branding is done in
# the .project file.
version="2.0.320"
rbReleaseDir="RB-Release"
bngdirname="BioNetGen-2.2.4"

echo "Removing JREs"

# get rid of the jre
rm -rf $rbReleaseDir/linux.gtk.x86/RuleBender/jre/
rm -rf $rbReleaseDir/linux.gtk.x86_64/RuleBender/jre/
rm -rf $rbReleaseDir/macosx.cocoa.x86/RuleBender/jre/
rm -rf $rbReleaseDir/macosx.cocoa.x86_64/RuleBender/jre/
rm -rf $rbReleaseDir/win32.win32.x86/RuleBender/jre/
rm -rf $rbReleaseDir/win32.win32.x86_64/RuleBender/jre/

echo "Creating zip dir"
# make an output folder so you don't eff up the dirs
mkdir $rbReleaseDir/zips

echo "Moving release files to zip dir and renaming to platform and version names"
# rename and move all of the RuleBender dirs
mv $rbReleaseDir/linux.gtk.x86/RuleBender/ $rbReleaseDir/zips/RuleBender-$version-lin32/
mv $rbReleaseDir/linux.gtk.x86_64/RuleBender/ $rbReleaseDir/zips/RuleBender-$version-lin64/
mv $rbReleaseDir/macosx.cocoa.x86/RuleBender/ $rbReleaseDir/zips/RuleBender-$version-osx32/
mv $rbReleaseDir/macosx.cocoa.x86_64/RuleBender/ $rbReleaseDir/zips/RuleBender-$version-osx64/
mv $rbReleaseDir/win32.win32.x86/RuleBender/ $rbReleaseDir/zips/RuleBender-$version-win32/
mv $rbReleaseDir/win32.win32.x86_64/RuleBender/ $rbReleaseDir/zips/RuleBender-$version-win64/

# remove unnecessary directories.
rm -rf $rbReleaseDir/linux.gtk.x86/
rm -rf $rbReleaseDir/linux.gtk.x86_64/
rm -rf $rbReleaseDir/macosx.cocoa.x86/
rm -rf $rbReleaseDir/macosx.cocoa.x86_64/
rm -rf $rbReleaseDir/win32.win32.x86/
rm -rf $rbReleaseDir/win32.win32.x86_64/

echo "Copying simulation resources"
# copy the simulation stuff into the directory. 
cp -r distributionResources/Simulation/* $rbReleaseDir/zips/RuleBender-$version-lin32/
cp -r distributionResources/Simulation/* $rbReleaseDir/zips/RuleBender-$version-lin64/
cp -r distributionResources/Simulation/* $rbReleaseDir/zips/RuleBender-$version-osx32/
cp -r distributionResources/Simulation/* $rbReleaseDir/zips/RuleBender-$version-osx64/
cp -r distributionResources/Simulation/* $rbReleaseDir/zips/RuleBender-$version-win32/
cp -r distributionResources/Simulation/* $rbReleaseDir/zips/RuleBender-$version-win64/

echo "Removing unnecessary binaries from lin32"
# remove the useless exes
# linux 32
rm $rbReleaseDir/zips/RuleBender-$version-lin32/$bngdirname/bin/cyggcc_s-1.dll
rm $rbReleaseDir/zips/RuleBender-$version-lin32/$bngdirname/bin/cygstdc++-6.dll
rm $rbReleaseDir/zips/RuleBender-$version-lin32/$bngdirname/bin/cygwin1.dll
rm $rbReleaseDir/zips/RuleBender-$version-lin32/$bngdirname/bin/msvcirt.dll
rm $rbReleaseDir/zips/RuleBender-$version-lin32/$bngdirname/bin/NFsim_i386-darwin
rm $rbReleaseDir/zips/RuleBender-$version-lin32/$bngdirname/bin/NFsim_i686-cygwin
rm $rbReleaseDir/zips/RuleBender-$version-lin32/$bngdirname/bin/NFsim_x86_64-linux
rm $rbReleaseDir/zips/RuleBender-$version-lin32/$bngdirname/bin/NFsim_MSWin32.exe
rm $rbReleaseDir/zips/RuleBender-$version-lin32/$bngdirname/bin/run_network_i386-darwin
rm $rbReleaseDir/zips/RuleBender-$version-lin32/$bngdirname/bin/run_network_i686-cygwin
rm $rbReleaseDir/zips/RuleBender-$version-lin32/$bngdirname/bin/run_network_MSWin32.exe
rm $rbReleaseDir/zips/RuleBender-$version-lin32/$bngdirname/bin/run_network_x86_64-linux

echo "Removing unnecessary binaries from lin64"
#linux 64
rm $rbReleaseDir/zips/RuleBender-$version-lin64/$bngdirname/bin/cyggcc_s-1.dll
rm $rbReleaseDir/zips/RuleBender-$version-lin64/$bngdirname/bin/cygstdc++-6.dll
rm $rbReleaseDir/zips/RuleBender-$version-lin64/$bngdirname/bin/cygwin1.dll
rm $rbReleaseDir/zips/RuleBender-$version-lin64/$bngdirname/bin/msvcirt.dll
rm $rbReleaseDir/zips/RuleBender-$version-lin64/$bngdirname/bin/NFsim_i386-darwin
rm $rbReleaseDir/zips/RuleBender-$version-lin64/$bngdirname/bin/NFsim_i686-cygwin
rm $rbReleaseDir/zips/RuleBender-$version-lin64/$bngdirname/bin/NFsim_i686-linux
rm $rbReleaseDir/zips/RuleBender-$version-lin64/$bngdirname/bin/NFsim_MSWin32.exe
rm $rbReleaseDir/zips/RuleBender-$version-lin64/$bngdirname/bin/run_network_i386-darwin
rm $rbReleaseDir/zips/RuleBender-$version-lin64/$bngdirname/bin/run_network_i686-cygwin
rm $rbReleaseDir/zips/RuleBender-$version-lin64/$bngdirname/bin/run_network_MSWin32.exe
rm $rbReleaseDir/zips/RuleBender-$version-lin64/$bngdirname/bin/run_network_i686-linux

echo "Removing unnecessary binaries from osx32"
# osx 32
rm $rbReleaseDir/zips/RuleBender-$version-osx32/$bngdirname/bin/cyggcc_s-1.dll
rm $rbReleaseDir/zips/RuleBender-$version-osx32/$bngdirname/bin/cygstdc++-6.dll
rm $rbReleaseDir/zips/RuleBender-$version-osx32/$bngdirname/bin/cygwin1.dll
rm $rbReleaseDir/zips/RuleBender-$version-osx32/$bngdirname/bin/msvcirt.dll
rm $rbReleaseDir/zips/RuleBender-$version-osx32/$bngdirname/bin/NFsim_x86_64-linux
rm $rbReleaseDir/zips/RuleBender-$version-osx32/$bngdirname/bin/NFsim_i686-cygwin
rm $rbReleaseDir/zips/RuleBender-$version-osx32/$bngdirname/bin/NFsim_i686-linux
rm $rbReleaseDir/zips/RuleBender-$version-osx32/$bngdirname/bin/NFsim_MSWin32.exe
rm $rbReleaseDir/zips/RuleBender-$version-osx32/$bngdirname/bin/run_network_x86_64-linux
rm $rbReleaseDir/zips/RuleBender-$version-osx32/$bngdirname/bin/run_network_i686-cygwin
rm $rbReleaseDir/zips/RuleBender-$version-osx32/$bngdirname/bin/run_network_MSWin32.exe
rm $rbReleaseDir/zips/RuleBender-$version-osx32/$bngdirname/bin/run_network_i686-linux

echo "Removing unnecessary binaries from osx64"
# osx 64
rm $rbReleaseDir/zips/RuleBender-$version-osx64/$bngdirname/bin/cyggcc_s-1.dll
rm $rbReleaseDir/zips/RuleBender-$version-osx64/$bngdirname/bin/cygstdc++-6.dll
rm $rbReleaseDir/zips/RuleBender-$version-osx64/$bngdirname/bin/cygwin1.dll
rm $rbReleaseDir/zips/RuleBender-$version-osx64/$bngdirname/bin/msvcirt.dll
rm $rbReleaseDir/zips/RuleBender-$version-osx64/$bngdirname/bin/NFsim_x86_64-linux
rm $rbReleaseDir/zips/RuleBender-$version-osx64/$bngdirname/bin/NFsim_i686-cygwin
rm $rbReleaseDir/zips/RuleBender-$version-osx64/$bngdirname/bin/NFsim_i686-linux
rm $rbReleaseDir/zips/RuleBender-$version-osx64/$bngdirname/bin/NFsim_MSWin32.exe
rm $rbReleaseDir/zips/RuleBender-$version-osx64/$bngdirname/bin/run_network_x86_64-linux
rm $rbReleaseDir/zips/RuleBender-$version-osx64/$bngdirname/bin/run_network_i686-cygwin
rm $rbReleaseDir/zips/RuleBender-$version-osx64/$bngdirname/bin/run_network_MSWin32.exe
rm $rbReleaseDir/zips/RuleBender-$version-osx64/$bngdirname/bin/run_network_i686-linux

echo "Removing unnecessary binaries from win32"
#windows 32
rm $rbReleaseDir/zips/RuleBender-$version-win32/$bngdirname/bin/NFsim_x86_64-linux
rm $rbReleaseDir/zips/RuleBender-$version-win32/$bngdirname/bin/NFsim_i686-linux
rm $rbReleaseDir/zips/RuleBender-$version-win32/$bngdirname/bin/NFsim_i386-darwin 
rm $rbReleaseDir/zips/RuleBender-$version-win32/$bngdirname/bin/run_network_x86_64-linux
rm $rbReleaseDir/zips/RuleBender-$version-win32/$bngdirname/bin/run_network_i386-darwin
rm $rbReleaseDir/zips/RuleBender-$version-win32/$bngdirname/bin/run_network_i686-linux

echo "Removing unnecessary binaries from win64"
#windows 64
rm $rbReleaseDir/zips/RuleBender-$version-win64/$bngdirname/bin/NFsim_x86_64-linux
rm $rbReleaseDir/zips/RuleBender-$version-win64/$bngdirname/bin/NFsim_i686-linux
rm $rbReleaseDir/zips/RuleBender-$version-win64/$bngdirname/bin/NFsim_i386-darwin
rm $rbReleaseDir/zips/RuleBender-$version-win64/$bngdirname/bin/run_network_x86_64-linux
rm $rbReleaseDir/zips/RuleBender-$version-win64/$bngdirname/bin/run_network_i386-darwin
rm $rbReleaseDir/zips/RuleBender-$version-win64/$bngdirname/bin/run_network_i686-linux

# Copy the RB-README.txt, LICENSE.txt, and CREDITS.txt files
echo "Copying RB-README.txt, LICENSE.txt, and CREDITS.txt"
cp distributionResources/*.txt $rbReleaseDir/zips/RuleBender-$version-lin32/
cp distributionResources/*.txt $rbReleaseDir/zips/RuleBender-$version-lin64/
cp distributionResources/*.txt $rbReleaseDir/zips/RuleBender-$version-osx32/
cp distributionResources/*.txt $rbReleaseDir/zips/RuleBender-$version-osx64/
cp distributionResources/*.txt $rbReleaseDir/zips/RuleBender-$version-win32/
cp distributionResources/*.txt $rbReleaseDir/zips/RuleBender-$version-win64/


#cd to zips dir to avoid more dirs in zip
cd $rbReleaseDir/zips/

# remove the any svn files
find . -type d -name .svn -exec rm -rf '{}' \;

# zip that garbage
zip -r RuleBender-$version-lin32.zip RuleBender-$version-lin32
zip -r RuleBender-$version-lin64.zip RuleBender-$version-lin64
zip -r RuleBender-$version-osx32.zip RuleBender-$version-osx32
zip -r RuleBender-$version-osx64.zip RuleBender-$version-osx64
zip -r RuleBender-$version-win32.zip RuleBender-$version-win32
zip -r RuleBender-$version-win64.zip RuleBender-$version-win64
