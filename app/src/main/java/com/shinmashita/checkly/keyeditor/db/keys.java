package com.shinmashita.checkly.keyeditor.db;

public class keys {
    private int _id;
    private String _keyValue;

    public keys(String _keyValue) {
        this._keyValue = _keyValue;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public void set_keyValue(String _keyValue) {
        this._keyValue = _keyValue;
    }

    public int get_id() {
        return _id;
    }

    public String get_keyValue() {
        return _keyValue;
    }
}
