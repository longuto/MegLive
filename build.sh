#!/usr/bin/env bash

set -x
set -e

rm -rf ../MegIDCardQuality-eclipse/IDCardLib/bin
rm -rf ../MegIDCardQuality-eclipse/IDCardLib/gen
rm -rf ../MegIDCardQuality-eclipse/IDCardProject/bin
rm -rf ../MegIDCardQuality-eclipse/IDCardProject/gen

rm -rf ../MegIDCardQuality-eclipse/IDCardLib/libs/*
cp -r ./IDCardLib/libs/* ../MegIDCardQuality-eclipse/IDCardLib/libs/
rm -rf ../MegIDCardQuality-eclipse/IDCardLib/res/*
cp -r ./IDCardLib/src/main/res/* ../MegIDCardQuality-eclipse/IDCardLib/res/
rm -rf ../MegIDCardQuality-eclipse/IDCardLib/src/*
cp -r ./IDCardLib/src/main/java/* ../MegIDCardQuality-eclipse/IDCardLib/src/
rm -rf ../MegIDCardQuality-eclipse/IDCardProject/res/*
cp -r ./app/src/main/res/* ../MegIDCardQuality-eclipse/IDCardProject/res/
rm -rf ../MegIDCardQuality-eclipse/IDCardProject/src/*
cp -r ./app/src/main/java/* ../MegIDCardQuality-eclipse/IDCardProject/src/
