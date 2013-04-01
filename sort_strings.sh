#!/bin/sh
for i in src/ch/fhnw/lernstickwelcome/Bundle*
do
	sort $i>tmp
	mv tmp $i
done
