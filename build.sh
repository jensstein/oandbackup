#!/usr/bin/env bash

set -e

function build {
	cargo_options=""
	gradle_mode=""
	if test $1 = "release"; then
		cargo_options="--release"
		gradle_mode="build"
	elif test $1 = "debug"; then
		cargo_options=""
		gradle_mode="assembleDebug"
	fi

	cd $(realpath $(dirname $0))

	cd oab-utils
	set -x
	cargo test
	cargo build $cargo_options --target armv7-linux-androideabi
	mkdir -p ../assets
	cp -v target/armv7-linux-androideabi/release/oab-utils ../assets

	cd ../
	./gradlew $gradle_mode
	set +x
}

if test $# -eq 0; then
	printf "usage: $0 {debug|release}\n"
	exit 1
fi

while test $# -gt 0; do
	case $1 in
	"release" | "debug")
		build $1
		;;
	*)
		printf "unknown option $1\n"
		exit 1
		;;
	esac
	shift
done
