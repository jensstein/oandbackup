use super::*;

#[test]
fn test_parse_id_input() {
    let id = match parse_id_input("1000:100") {
        Ok((uid, gid_option)) => (uid, gid_option.unwrap()),
        Err(_err) => return assert!(false, "error parsing input")
    };
    assert_eq!((1000, 100), id);
}

#[test]
fn test_parse_id_input_only_uid() {
    let id = match parse_id_input("1000") {
        Ok((uid, gid_option)) => (uid, gid_option),
        Err(_err) => return assert!(false, "error parsing input")
    };
    assert_eq!((1000, None), id);
}
