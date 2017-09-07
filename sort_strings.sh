#!/bin/sh
echo "sorting strings..."
for i in src/main/resources/ch/fhnw/lernstickwelcome/Bundle*
do
	sort $i>tmp
	mv tmp $i
done
