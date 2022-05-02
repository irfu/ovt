#!/bin/bash
# Remove *.so.1 files and replace them with links to corresponding *.so files.

cd ~/OVT/ovt/dist_static/natives/Linux/

filename_list=$(ls *.so.1 );

for filename in ${filename_list[@]}
do
    # ${string/%substring/replacement}    If $substring matches back end of $string, substitute $replacement for $substring
	echo "Removing" $filename
	rm $filename
	ln -s ${filename%.1} $filename
	echo "Creating link "${filename%.1} to $filename
done
