use super::*;

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
