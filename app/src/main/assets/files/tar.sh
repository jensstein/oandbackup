command=$1
shift

utilbox=$(which toybox busybox | (read line ; echo "$line"))

archive=-
exclude=

while [[ $1 == --* ]]; do

  if [[ $1 == --archive ]]; then
    shift
    archive=$1
    shift
    continue
  fi

  if [[ $1 == --exclude ]]; then
    shift
    exclude=" -X $1"
    shift
    continue
  fi

  break

done


if [[ $command == "create" ]]; then

  dir=$1
  shift

  $utilbox tar -c -f "$archive" -C "$dir" $exclude .

  exit $?

elif [[ $command == "extract" ]]; then

  dir=$1
  shift

  $utilbox tar -x -f "$archive" -C "$dir" $exclude

  exit $?
fi

