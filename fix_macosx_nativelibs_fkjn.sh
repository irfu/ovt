#!/bin/sh

#otool -L libvtkViewsInfovis-6.2.dylib |grep VTKBuild |awk '{print $1}'
#install_name_tool -change /Users/frejon/OVT/ovt/VTKBuild/lib/libvtkImagingCore-6.2.1.dylib @executable_path/natives/libvtkViewsInfovis-6.2.dylib libvtkViewsInfovis-6.2.dylib


APP_PATH=/Users/frejon/OVT/ovt/dist_static
VTK_BUILD=/Users/frejon/OVT/ovt/VTKBuild/lib
JAVA_PATH=$APP_PATH/shit
FULL_FRMW_PATH=$APP_PATH/natives/MacOS
RELATIVE_PATH=@executable_path/../java/app/natives



[ ! -d "$VTK_BUILD" ] && echo "VTK_BUILD points to nonexistent $VTK_BUILD" && exit 1
[ ! -d "$APP_PATH" ] && echo "APP_PATH points to nonexistent $APP_PATH" && exit 1

cd $FULL_FRMW_PATH



cd $VTK_BUILD #get all VTK libs
#LIB_LIST=$(ls *.*lib)
dylibjnilib=$(ls *.*lib)
cd $FULL_FRMW_PATH #now in natives folder

#read -p "Press any key to continue... " -n1 -s

for LIB in $dylibjnilib  #; do echo $f;done
do


		[ ! -e "$VTK_BUILD/$LIB" ] && echo "Cannot find $VTK_BUILD/$LIB" && continue

		#read -p "Press any key to continue... " -n1 -s

		/bin/echo -n "Processing $LIB... "
		cp $VTK_BUILD/$LIB $FULL_FRMW_PATH/
		#(cd $FULL_FRMW_PATH && ln -s $LIB_NAME $LIB_NAM1 && ln -s $LIB_NAM1 $LIB_NAM2) #don't know the use of this
	  #install_name_tool -id @executable_path/$FRAMEWORK_PATH/$LIB_NAM1 $FULL_FRMW_PATH/$LIB_NAME #changes id of file
	  install_name_tool -id $RELATIVE_PATH/$LIB $LIB #changes id of file in natives folder

	  #UPD_LIBS=`otool -L $FULL_FRMW_PATH/$LIB_NAME |grep -v "@executable_path" | grep .${VEMA}.dylib | awk '{print $1}'`
	  UPD_LIBS=`otool -L $LIB |grep VTKBuild |awk '{print $1}'`

		for LU in $UPD_LIBS
		do
			LU_base=$(basename $LU)
			install_name_tool -change $LU $RELATIVE_PATH/$LU_base $LIB
		done
		echo Done.
done

echo "all vtk libs done, check libovt-3.0.jnilib"

read -p "Press any key to continue... " -n1 -s

[ ! -e "$FULL_FRMW_PATH/libovt-3.0.jnilib" ] && echo "Cannot find $FULL_FRMW_PATH/libovt-3.0.jnilib" && exit 1
install_name_tool -id $RELATIVE_PATH/libovt-3.0.jnilib $FULL_FRMW_PATH/libovt-3.0.jnilib #changes id of libovt in natives folder
echo "Done."
	#LIB_NAME=${PREF}${LIB}.${VEMA}.${VEMI}.dylib
	#LIB_NAM1=${PREF}${LIB}.${VEMA}.dylib
	#LIB_NAM2=${PREF}${LIB}.dylib



#/bin/echo -n "Linking JNI libs... "
#for JL in $JAVA_LIBS
#do
#	LIB_NAME=${PREF}${JL}Java
#	[ -e "$JAVA_PATH/$LIB_NAME.jnilib" ] && rm $JAVA_PATH/$LIB_NAME.jnilib
#	( cd $JAVA_PATH && ln -s ../../Frameworks/VTK.framework/VTK/$LIB_NAME.dylib $LIB_NAME.jnilib )
#done
#echo Done.
