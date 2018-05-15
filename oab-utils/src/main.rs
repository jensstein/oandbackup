extern crate clap;

use std::error::Error;
use std::fs;
use std::os::unix::fs::MetadataExt;
use std::os::unix::fs::PermissionsExt;
use std::path::Path;

use clap::{Arg, App, SubCommand};

// https://blog.rust-lang.org/2016/05/13/rustup.html
// https://users.rust-lang.org/t/announcing-cargo-apk/5501

struct OwnerId {
    uid: u32,
    gid: u32
}

fn get_owner_ids(path: &str) -> OwnerId {
    let ids = match fs::metadata(Path::new(path)) {
        Err(e) => {
            eprintln!("error getting owner ids for {}: {}", path,
                e.description());
            std::process::exit(1);
        }
        Ok(metadata) => OwnerId{
            uid: metadata.uid(), gid: metadata.gid()
        }
    };
    ids
}

fn set_permissions(p: &str) -> bool {
    let path = Path::new(p);
    let mut perms = match fs::metadata(path) {
        Err(e) => {
            eprintln!("error getting permissions for {:?}: {}", path,
                e.description());
            return false
        }
        Ok(metadata) => metadata.permissions()
    };
    let mode = 0o664;
    if perms.mode() & 0o777 != mode {
        perms.set_mode(mode);
        match std::fs::set_permissions(path, perms) {
                Err(e) => {
                    eprintln!("unable to set mode {:o} on path {:?}: {}",
                        mode, path, e.description());
                    return false;
                }
                Ok(_) => return true
        };
    }
    false
}

fn main() {
    // https://github.com/kbknapp/clap-rs
    let args = App::new("oab-utils")
        .subcommand(SubCommand::with_name("owner")
            .arg(Arg::with_name("input")
                .help("file to get info from")
                .required(true)))
        .subcommand(SubCommand::with_name("set-permissions")
            .arg(Arg::with_name("input")
                .required(true))
        )
        .get_matches();
    if let Some(args) = args.subcommand_matches("owner") {
        let input = args.value_of("input").unwrap();
        let ids = get_owner_ids(input);
        println!("uid: {}\ngid: {}", ids.uid, ids.gid);
    } else if let Some(args) = args.subcommand_matches("set-permissions") {
        let input = args.value_of("input").unwrap();
        if !set_permissions(input) {
            std::process::exit(1);
        };
    }
}
