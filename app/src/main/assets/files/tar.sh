#!/system/bin/sh

command=$1
shift

utilbox=$1
shift

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
    exclude="$exclude -X $1"
    shift
    continue
  fi

  break

done


if [[ $command == "create" ]]; then

  dir=$1
  shift

  #cd $dir && (
  #  ($utilbox ls -1A | $utilbox tar -c -f "$archive" $exclude -T -) || (dd if=/dev/zero bs=1024c count=1 2>/dev/null)
  #)
  $utilbox tar -c -f "$archive" -C "$dir" $exclude .

  exit $?

elif [[ $command == "extract" ]]; then

  dir=$1
  shift

  $utilbox tar -x -f "$archive" -C "$dir" $exclude

  exit $?
fi

