#!/bin/sh
for ICON in $(find src/ch/fhnw/lernstickwelcome/icons -type f -name *.png)
do
	NAME=$(basename ${ICON})
#	echo "processing icon ${NAME}"
	find -name "*.java" | xargs grep -q "${NAME}"
	if [ $? != 0 ]
	then
		echo "icon \"${NAME}\" not found"
	fi
done
