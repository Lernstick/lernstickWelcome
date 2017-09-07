#!/bin/sh
echo "checking strings..."
for BUNDLE in src/main/resources/ch/fhnw/lernstickwelcome/Bundle*
do
	echo "processing bundle ${BUNDLE}"
	while read LINE
	do
		KEY=$(echo ${LINE} | awk -F= '{ print $1 }')
                # search in java source files
		find -name "*.java" | xargs grep -q "\"${KEY}\""
		if [ $? != 0 ]
		then
                        # search in FXML files
                        find -name "*.fxml" | xargs grep -q "\"%${KEY}\""
        		if [ $? != 0 ]
                	then
                                # search in applications.xml
                                grep -q ">${KEY}<" src/main/resources/applications.xml
                		if [ $? != 0 ]
                        	then
                                        echo "KEY \"${KEY}\" not found"
                                fi
                        fi
		fi
	done <${BUNDLE}
done
