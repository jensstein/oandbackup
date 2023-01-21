


# .ERROR.

* renames properties files or folders xxx that are wrong in some way to .ERROR.xxx
  wrong is
+ damaged format = json cannot be read, e.g. files with length 0 or truncated
+ folder without properties
+ properties without folder
- if only folder or properties is renamed (e.g. with (1) attached), it will also be renamed to .ERROR.xxx. While this could be detected, I refused to do so. The case
  the.package.name (1)
  should work, the others should never have happend. Tell me if you have lots of properties+folders where only one is renamed by SAF (= xxx (1))

