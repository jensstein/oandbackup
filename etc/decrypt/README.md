
# experimental(!) script to decrypt all backups

    DISCLAIMER: without any warranty!
    first create a backup of the backups!!!!!
    you have been warned... 😊

I used this for testing ... so only a few tests were made.

The original script is from Nils:
<https://github.com/Tiefkuehlpizze/OABXDecrypt/blob/main/misc/decrypt.py>

The python script scans the given directory for backups and creates decrypted
backups with the postfix `-DECRYPTED`.

Those will be shown as `<date time> - DECRYPTED` in the backup list.

The decrypted backups have the same date, and NB traditionally used the
backup date as ID to distiguish the backups.
So, it's possible, that NB reacts weird in some areas.
If you want to stay safe, move the DECRYPTED backups to it's own backup directory and use this in NB.


Linux (or any unix environment, maybe also Mac):

The provided (bash/zsh/...) script file (decrypt-all) checks for
an installed package and virtual environment manager:

    uv          https://docs.astral.sh/uv/getting-started/installation/
                (very fast package manager)

    pipenv      debian: apt install pipenv
                or https://pipenv.pypa.io/en/latest/installation.html

Otherwise it assumes the requirements in requirements.txt are already met
and simply runs the python script.


Usage:

copy this folder to your workstation

decrypt-all PASSWORD BACKUPDIRECTORY

e.g. either

```sh
cd <path-to-backup-root>
<path-to-script-directory>/decrypt-all <the-password>
```

or

```shell
<path-to-script-directory>/decrypt-all <the-password> <path-to-backup-root>
```


There are too many ways and complications runing python scripts on Windows.
So you are on your own. But it's not that difficult.
You might use WSL to run it in an unix environment.
Or use a Linux virtual machine.
Or use a native way to run a command line python script.
The only requirement is the pycryptodome library (or whatever is listed in requirements.txt).
