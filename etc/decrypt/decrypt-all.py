#!python

'''
Quickly implemented AES-GCM-NoPadding decryption tool that can decrypt Neo Backup encrypted files.
Feel free to use, study and extend it.
'''
import sys
import os
import shutil
import json
from Crypto.Cipher import AES
from hashlib import pbkdf2_hmac

# don't change

PBKDF_ITERATIONS = 2020
KEY_LEN = 32
FALLBACK_SALT = b'oandbackupx'
EXT = ".enc"

def decrypt(backup, password, files):

    try:
        with open(backup + '.properties', 'r') as f:

            try:

                properties = json.load(f)
                iv_bytes = b''.join(map(lambda i: int(i).to_bytes(1, 'big', signed=True), properties['iv']))

                encryption = properties.pop('cipherType', 'none')

                if encryption == "AES/GCM/NoPadding":

                    decrypted_backup = backup + "-DECRYPTED"

                    for file in sorted(files):

                        path = os.path.join(backup, file)

                        if file.endswith(EXT):

                            encrypted_path = path

                            with open(encrypted_path, 'rb') as input_file:

                                print("decrypt", encrypted_path)

                                os.makedirs(decrypted_backup, exist_ok=True)

                                key = pbkdf2_hmac(hash_name='sha256',
                                                  password=password,
                                                  salt=FALLBACK_SALT,
                                                  iterations=PBKDF_ITERATIONS,
                                                  dklen=KEY_LEN)
                                cipher = AES.new(key, AES.MODE_GCM, nonce=iv_bytes)

                                encrypted_content = input_file.read()

                                ciphertext = encrypted_content[:-16]
                                tag = encrypted_content[-16:]
                                decrypted_content = cipher.decrypt_and_verify(ciphertext, tag)

                                basename = os.path.splitext(file)[0]
                                decrypted_path = os.path.join(decrypted_backup, basename)

                                with open(decrypted_path, 'wb') as output_file:
                                    output_file.write(decrypted_content)

                        else:

                            print("copy   ", path)

                            os.makedirs(decrypted_backup, exist_ok=True)
                            shutil.copy(path, decrypted_backup)

                    with open(decrypted_backup + '.properties', 'w') as f:

                        properties.pop('cipherType', '')        # already done, just to be sure
                        json.dump(obj=properties, fp=f, indent=4)

                else:
                    print("unknown ciperType:", ciperType)

            except Exception as e:
                print("ERROR:", e)

    except:
        pass # no properties file

    print()

#print(sys.argv)

try:
    password  = sys.argv[1].encode('utf-8')
    directory = sys.argv[2]
except:
    print("usage: ", sys.argv[0], "PASSWORD", "BACKUPDIRECTORY")
    exit(1)

print("password: ", password.decode('utf-8'))
print("directory:", directory)

for folder, dirs, files in os.walk(directory):
    for file in files:
        if file.endswith(EXT):
            print(folder, "\t", files)
            decrypt(folder, password, files)
