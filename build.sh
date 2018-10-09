#!/usr/bin/env bash

set -e

TARGET=armv7-linux-androideabi
TARGET_ABI=armeabi-v7a

function build {
	build_mode=$1
	cargo_options=""
	gradle_mode=""
	if test $build_mode = "release"; then
		cargo_options="--release"
		gradle_mode="build"
	elif test $build_mode = "debug"; then
		cargo_options=""
		gradle_mode="assembleDebug"
	fi

	cd $(realpath $(dirname $0))

	cd oab-utils
	set -x
	cargo test
	cargo build $cargo_options --target $TARGET
	mkdir -p ../assets/$TARGET_ABI
	cp -v target/$TARGET/$build_mode/oab-utils ../assets/$TARGET_ABI

	cd ../
	./gradlew $gradle_mode
	set +x
}

if test $# -eq 0; then
	printf "usage: $0 {debug|release}\n"
	exit 1
fi

ACTION=
while test $# -gt 0; do
	case $1 in
	"--target")
		TARGET=$2
		shift
		;;
	"--abi")
	    TARGET_ABI=$2
	    shift
	    ;;
	"release" | "debug")
		ACTION="build $1"
		;;
	*)
		printf "unknown option $1\n"
		exit 1
		;;
	esac
	shift
done

$ACTION
