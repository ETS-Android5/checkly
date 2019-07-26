package com.shinmashita.checkly.answers;

public class answers {
    private int _answer_id;
    private String _answer_value;

    public answers(String _answer_value) {
        this._answer_value = _answer_value;
    }

    public void set_answer_id(int _answer_id) {
        this._answer_id = _answer_id;
    }

    public void set_answer_value(String _answer_value) {
        this._answer_value = _answer_value;
    }

    public int get_answer_id() {
        return _answer_id;
    }

    public String get_answer_value() {
        return _answer_value;
    }
}
