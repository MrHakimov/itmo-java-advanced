DIR=$(cd `dirname $0` && pwd)
MODULE_NAME=ru/ifmo/rain/hakimov
HW_NAME=walk

cd $DIR/../out/production/FileWalker/ru/ifmo/rain/hakimov/walk/ || exit
rm -f *.class

cd $DIR/$MODULE_NAME/$HW_NAME
javac *.java

mv *.class $DIR/../out/production/FileWalker/ru/ifmo/rain/hakimov/walk/.

cd $DIR/../out/production/FileWalker
java -cp . -p . -m info.kgeorgiy.java.advanced.walk RecursiveWalk ru.ifmo.rain.hakimov.walk.RecursiveWalk
