extern crate tempfile;

use super::*;
use std::os::unix::fs::MetadataExt;
use std::path::PathBuf;

struct MockIdRetriever {
    mock_uid: u32,
    mock_gid: u32,
    mock_error: Option<String>
}
impl IdRetrieverModel for MockIdRetriever {
    #[allow(unused_variables)]
    fn get_uid(&self, name: &str) -> Result<u32, OabError> {
        if let Some(ref error) = self.mock_error {
            return Err(OabError {message: error.to_string()});
        }
        Ok(self.mock_uid)
    }
    #[allow(unused_variables)]
    fn get_gid(&self, name: &str) -> Result<u32, OabError> {
        if let Some(ref error) = self.mock_error {
            return Err(OabError {message: error.to_string()});
        }
        Ok(self.mock_gid)
    }
}

#[test]
fn test_parse_id_input() {
    let mock_id_retriever = get_mocked_id_retriever(0, 0, None);
    match parse_id_input(&mock_id_retriever, "1000:100") {
        Ok((uid, gid_option)) => {
            assert_eq!(1000, uid);
            assert_eq!(100, gid_option.unwrap());
        }
        Err(_err) => return assert!(false, "error parsing input")
    };
}

#[test]
fn test_parse_id_input_only_uid() {
    let mock_id_retriever = get_mocked_id_retriever(0, 0, None);
    match parse_id_input(&mock_id_retriever, "1000") {
        Ok((uid, gid_option)) => {
            assert_eq!(1000, uid);
            assert_eq!(None, gid_option)
        }
        Err(_err) => return assert!(false, "error parsing input")
    };
}

#[test]
fn test_parse_id_input_parse_username() {
    let mock_id_retriever = get_mocked_id_retriever(1000, 1000, None);
    match parse_id_input(&mock_id_retriever, "user1000") {
        Ok((uid, gid_option)) => {
            assert_eq!(1000, uid);
            assert_eq!(gid_option, None);
        }
        Err(_err) => assert!(false, "shouldn't fail")
    };
}

#[test]
fn test_parse_id_input_parse_groupname() {
    let mock_id_retriever = get_mocked_id_retriever(0, 1000, None);
    match parse_id_input(&mock_id_retriever, "1000:group1000") {
        Ok((uid, gid_option)) => {
            assert_eq!(1000, uid);
            assert_eq!(1000, gid_option.unwrap());
        }
        Err(_err) => assert!(false, "shouldn't fail")
    };
}

#[test]
fn test_parse_id_input_username_groupname() {
    let mock_id_retriever = get_mocked_id_retriever(1000, 1000, None);
    match parse_id_input(&mock_id_retriever, "user1000:group1000") {
        Ok((uid, gid_option)) => {
            assert_eq!(1000, uid);
            assert_eq!(1000, gid_option.unwrap())
        }
        Err(_err) => assert!(false, "shouldn't fail")
    }
}

#[test]
fn test_parse_id_input_error() {
    let mock_id_retriever = get_mocked_id_retriever(150, 0,
        Some("no such user".to_string()));
    match parse_id_input(&mock_id_retriever, "user1000") {
        Ok((_uid, _gid_option)) => assert!(false, "shouldn't succeed"),
        Err(err) => assert_eq!("no such user", err.description())
    }
}

fn get_mocked_id_retriever(uid: u32, gid: u32, error: Option<String>) ->
        MockIdRetriever {
    MockIdRetriever {
        mock_uid: uid,
        mock_gid: gid,
        mock_error: error
    }
}

#[test]
fn test_get_owner_ids() {
    if !can_create_file_file() {
        return;
    }
    let file = tempfile::NamedTempFile::new().unwrap();
    let metadata = file.as_file().metadata().unwrap();
    let uid_tempfile = metadata.uid();
    let gid_tempfile = metadata.gid();
    match get_owner_ids(file.path()) {
        Ok((uid, gid)) => {
            assert_eq!(uid, uid_tempfile);
            assert_eq!(gid, gid_tempfile);
        }
        Err(_err) => assert!(false, "shouldn't fail")
    }
}

#[test]
fn test_get_owner_ids_no_such_file() {
    if !can_create_file_file() {
        return;
    }
    let path: PathBuf;
    {
        // create a temporary file and make it go out of scope immediately
        // while copying its path.  this gives us a path to a file which
        // probably doesn't exist when we try to query its metadata (which
        // is what we want to test for). of course, another file with the
        // same path could be created in the meantime and a bug could prevent
        // the file from being removed when the variable goes out of scope.
        let file = tempfile::NamedTempFile::new().unwrap();
        path = file.path().to_owned();
    }
    match get_owner_ids(&path.as_path()) {
        Ok(_) => assert!(false, "should fail"),
        Err(err) => assert_eq!(err.kind(), std::io::ErrorKind::NotFound)
    }
}

#[test]
fn test_get_permissions() {
    let dir = tempfile::tempdir().unwrap();
    let file1 = tempfile::NamedTempFile::new_in(dir.path()).unwrap();
    let file2 = tempfile::NamedTempFile::new_in(dir.path()).unwrap();
    let file3 = tempfile::NamedTempFile::new_in(dir.path()).unwrap();

    let permissions = fs::metadata(&file1).unwrap();

    let results = match get_permissions(dir.path()) {
        Ok(results) => results,
        Err(_) => return assert!(false, "shouldn't fail")
    };
    assert_eq!(results.len(), 3);
    assert_eq!(results[0].path, file1.path());
    assert_eq!(results[0].permissions, permissions.mode());
    assert_eq!(results[1].path, file2.path());
    assert_eq!(results[1].permissions, permissions.mode());
    assert_eq!(results[2].path, file3.path());
    assert_eq!(results[2].permissions, permissions.mode());
}

fn can_create_file_file() -> bool {
    match tempfile::tempfile() {
        Ok(_) => true,
        Err(_) => false
    }
}
