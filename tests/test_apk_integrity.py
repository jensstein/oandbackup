#!/usr/bin/env python3

import os
import unittest
import zipfile

BUILD_DIR = os.getenv("GRADLE_BUILD_DIR", "build-dir-not-set")

flavor_asset_dict = {
    "arm": ["assets/armeabi-v7a/oab-utils"],
    "arm64": ["assets/arm64-v8a/oab-utils"],
    "x86": ["assets/x86/oab-utils"],
    "x86_64": ["assets/x86_64/oab-utils"],
    "universal": ["assets/armeabi-v7a/oab-utils",
        "assets/arm64-v8a/oab-utils", "assets/x86/oab-utils",
        "assets/x86_64/oab-utils"]
}

class ApkTest(unittest.TestCase):
    def test_assets(self):
        self.assertEqual(os.path.exists(BUILD_DIR), True)
        for root, dirs, files in os.walk(BUILD_DIR):
            for filename in files:
                if filename[-4:] == ".apk":
                    self.assert_apk_asset_contains(os.path.join(root,
                        filename))

    def assert_apk_asset_contains(self, filename):
        for flavor, assets in flavor_asset_dict.items():
            if os.sep + flavor + os.sep in filename:
                with zipfile.ZipFile(filename, "r") as z:
                    for asset in assets:
                        self.assertEqual(asset in z.namelist(), True,
                            "checking flavor {} failed for asset {}".format(
                            flavor, asset))
