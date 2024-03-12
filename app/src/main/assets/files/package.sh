#!/system/bin/sh

command=$1
shift

utilbox=$1
shift

suspend=false

while [[ $1 == --* ]]; do

  if [[ $1 == --suspend ]]; then
    shift
    suspend=true
    continue
  fi

  break

done


if [[ $command == "pre-backup" ]]; then

  package=$1
  shift

  userid=$1
  shift


  #am force-stop $package
  #am kill $package

  if $suspend; then
    pm suspend $package >/dev/null
    $utilbox sleep 3
  fi

  pids=$(
    (
      $utilbox ps -A -o PID -u $userid | $utilbox tail -n +2
      $utilbox ls -l /proc/*/fd/* 2>/dev/null |
          $utilbox grep -E "/data/data/|/media/" |
          #$utilbox grep -E "/data/|/media/" |  #TODO add an option for wider catching
          $utilbox grep -F /$package/ |
          $utilbox cut -s -d / -f 3
    ) |
    $utilbox sort -u -n
  )

  #echo "pids=( $pids )"

  if [[ -n $pids ]]; then
    $utilbox ps -A -w -o USER,PID,NAME -p $pids |
      while read -r user pid process; do
        if [[ $user    != u0_*                        ]]; then continue; fi
        if [[ $process == android.process.media       ]]; then continue; fi
        if [[ $process == com.android.externalstorage ]]; then continue; fi
        $utilbox kill -STOP $pid && echo $pid
      done
  fi

  exit
fi

if [[ $command == "post-backup" ]]; then

  package=$1
  shift

  userid=$1
  shift

  # kill background processes, which should be restarted by the system when necessary
  am kill $package

  if [[ -n $* ]]; then $utilbox kill -CONT "$@"; fi

  if $suspend; then
    pm unsuspend $package
  fi

  exit
fi

if [[ $command == "pre-restore" ]]; then

  package=$1
  shift

  userid=$1
  shift

  # kill app and all of its services without canceling it's scheduled alarms and jobs.
  # if command does not exist (or fails) stop everything instead
  am stop-app $package || am force-stop $package

  exit
fi

if [[ $command == "post-restore" ]]; then

  package=$1
  shift

  userid=$1
  shift

  exit
fi
