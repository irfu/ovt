#!bin/bash
#remove *.so.1 files, replace with links.
cd ~/OVT/ovt/dist_static/natives/Linux/

shit=$(ls *.so.1 );

for number in ${shit[@]}
    do
	echo "removing" $number
	rm $number
	#ln -s target link
	ln -s ${number%.1} $number       
	echo "creating link "${number%.1} to $number

    done



