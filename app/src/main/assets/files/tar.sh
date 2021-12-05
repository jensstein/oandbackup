command=$1
shift

utilbox=$(which toybox busybox | (read line ; echo "$line"))

to=-
exclude=

while [[ $1 == --* ]]; do

  if [[ $1 == --to ]]; then
    shift
    to=$1
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

  $utilbox tar -c -f "$to" -C "$dir" $exclude .

  exit $?
fi

