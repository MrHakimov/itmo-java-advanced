cd "$(dirname "$0")" || exit

java -jar junit-platform-console-standalone-1.6.2.jar --class-path out --scan-class-path

echo "Exit code: $?"
exit $?
