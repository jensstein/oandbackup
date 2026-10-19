#!/system/bin/sh


utilbox=toybox

suspend=false

while [[ $1 == --* ]]; do

  if [[ $1 == --suspend ]]; then
    shift
    suspend=true
    continue
  fi

  break

done

if [[ -z $1 ]]; then
  if [[ -n $ANDROID_ROOT ]]; then
    packages=$(pm list packages -3 -e | sort | sed s/package://)
    cat $0 | bash -s $(echo $packages) 2>/dev/null
  else
    # remote testing via adb
    (
      #echo "packages='\
      #        com.google.android.apps.tachyon             \
      #        com.google.android.gm                       \
      #        com.google.android.inputmethod.latin        \
      #        com.google.ar.core                          \
      #        com.inspiredandroid.linuxcontrolcenterpro   \
      #        com.justtradenative                         \
      #        com.kiwibrowser.browser                     \
      #        com.noinnion.android.greader.readerpro      \
      #        com.whatsapp                                \
      #        de.ralphsapps.snorecontrol                  \
      #        fr.mathis.invisiblewidget                   \
      #        io.github.forkmaintainers.iceraven          \
      #        jp.co.korg.kaossilator.android              \
      #        me.zhanghai.android.files                   \
      #        net.cozic.joplin                            \
      #        org.forkclient.messenger.beta               \
      #        org.kde.kdeconnect_tp                       \
      #        org.mmin.handycalc                          \
      #        '"
      echo 'packages=$(pm list packages -3 -e | sort | sed s/package://)'
      echo 'bash -s $(echo $packages)'
      cat test-package-pids.sh
    ) | adb shell su
    exit
  fi
fi

while [[ -n $1 ]]; do

  package=$1
  shift

  echo "-- $package"

  #userid=$1
  #shift

  #pm list packages -3 -e -U $package | sed s/package:// | sed s/uid:// | head -n 1 | read pkg userid
  read pkg userid < <(pm list packages -U $package | sed s/package:// | sed s/uid:// | head -n 1)  #| read pkg userid
  #echo p=$pkg u=$userid
  if [[ -z $userid ]]; then echo "--- pkg $pkg user $userid"; continue; fi

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
        $utilbox echo $process $pid $user
      done
  fi

done
