#!bin/bash
# Remove *.so.1 files and replace them with links.

cd ~/OVT/ovt/dist_static/natives/Linux/

filename_list=$(ls *.so.1 );

for filename in ${filename_list[@]}
do
	echo "Removing" $filename
	rm $filename
	ln -s ${filename%.1} $filename       
	echo "Creating link "${filename%.1} to $filename
done
