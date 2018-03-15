./gradlew assemblerelease
mv build/out*/apk*/re*/*.apk unsigned.apk
java -jar uber-apk-signer.jar -a . --out oandbackup.apk
rm *unsig*
