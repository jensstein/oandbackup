#!/usr/bin/env bash

set -xe

FLAVOUR=arm64

function start_test() {
	test_c=
	if test ! -z $1; then
		test_c="-e class $1"
	fi
	./gradlew test${FLAVOUR^}DebugUnitTest assemble${FLAVOUR^}Debug assemble${FLAVOUR^}DebugAndroidTest
	adb push build/outputs/apk/${FLAVOUR}/debug/oandbackup-${FLAVOUR}-debug.apk /data/local/tmp/dk.jens.backup
	adb shell pm install -t -r "/data/local/tmp/dk.jens.backup"
	adb push build/outputs/apk/androidTest/${FLAVOUR}/debug/oandbackup-${FLAVOUR}-debug-androidTest.apk /data/local/tmp/dk.jens.backup.test
	adb shell pm install -t -r "/data/local/tmp/dk.jens.backup.test"

	adb shell am instrument -w -r -e debug false ${test_c} dk.jens.backup.test/android.support.test.runner.AndroidJUnitRunner
}

function init() {
	adb uninstall dk.jens.backup || true
	start_test "dk.jens.backup.TestHelper"
}

action=
while [ $# -gt 0 ]; do
	case "$1" in
	"--test-class")
		test_class="$2"
		shift
		;;
	"--flavour")
		FLAVOUR="$2"
		shift
		;;
	"--clean")
		./gradlew clean
		;;
	"-h"|"--help")
		printf "usage: $0 [-h] [--test-class CLASS] [--flavour FLAVOUR] [--clean]"
		exit 0
		;;
	*)
		printf "unknown option $1\n" $1
		exit 1
		;;
	esac
	shift
done

init

start_test "$test_class"

