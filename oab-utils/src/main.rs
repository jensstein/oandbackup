extern crate clap;

use std::error::Error;
use std::fs;
use std::os::unix::fs::MetadataExt;
use std::os::unix::fs::PermissionsExt;
use std::path::Path;

use clap::{Arg, App, AppSettings, SubCommand};

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

fn set_permissions(p: &str, mode: u32) -> bool {
    let path = Path::new(p);
    let mut perms = match fs::metadata(path) {
        Err(e) => {
            eprintln!("error getting permissions for {:?}: {}", path,
                e.description());
            return false
        }
        Ok(metadata) => metadata.permissions()
    };
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
        .setting(AppSettings::ArgRequiredElseHelp)
        .subcommand(SubCommand::with_name("owner")
            .arg(Arg::with_name("input")
                .help("file to get info from")
                .required(true)))
        .subcommand(SubCommand::with_name("set-permissions")
            .arg(Arg::with_name("mode")
                .help("mode to set, in octal (e.g. 644)")
                .required(true))
            .arg(Arg::with_name("input")
                .required(true))
        )
        .get_matches();
    match args.subcommand() {
        ("owner", Some(args)) => {
            let input = args.value_of("input").unwrap();
            let ids = get_owner_ids(input);
            println!("uid: {}\ngid: {}", ids.uid, ids.gid);
        },
        ("set-permissions", Some(args)) => {
            let input = args.value_of("input").unwrap();
            let mode_str = args.value_of("mode").unwrap();
            let mode = match u32::from_str_radix(mode_str, 8) {
                Err(e) => {
                    eprintln!("error parsing input {}: {}", mode_str, e.description());
                    std::process::exit(1);
                }
                Ok(value) => value
            };
            if !set_permissions(input, mode) {
                std::process::exit(1);
            };
        },
        ("", None) => {
            eprintln!("no commands specified");
            std::process::exit(1);
        },
        _ => unreachable!()
    }
}
