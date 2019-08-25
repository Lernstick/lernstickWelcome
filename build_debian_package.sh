#!/bin/sh
dpkg-buildpackage -sa
fakeroot debian/rules clean
