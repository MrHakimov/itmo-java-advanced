DIR=$(cd `dirname $0` && pwd)
MODULE_NAME=ru/ifmo/rain/hakimov
HW_NAME=arrayset
MODIFICATION=NavigableSet
MAIN_CLASS=ArraySet
PROJECT=ArraySet

cd $DIR/../out/production/$PROJECT/$MODULE_NAME/$HW_NAME || exit
rm -f *.class

cd $DIR/$MODULE_NAME/$HW_NAME
javac *.java

mv *.class $DIR/../out/production/$PROJECT/$MODULE_NAME/$HW_NAME/.

cd $DIR/../out/production/$PROJECT
java -cp . -p . -m info.kgeorgiy.java.advanced.$HW_NAME $MODIFICATION ru.ifmo.rain.hakimov.$HW_NAME.$MAIN_CLASS
