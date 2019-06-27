#!/bin/bash

make clean 1> /dev/null
#make 1> /dev/null
make
rm main.tdo main.lof 2> /dev/null
evince main.pdf 2> /dev/null
