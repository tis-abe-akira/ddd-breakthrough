package com.example.entity;

/**
 * ユーザー情報を表すエンティティクラス。
 * システム内でのユーザー管理に使用される。
 * <p>
 * このクラスはユーザーの基本情報（ID、ユーザー名、メールアドレス）を保持し、
 * ユーザー関連の基本的な操作を提供する。
 * </p>
 * 
 * @author システム開発チーム
 * @version 1.0
 */
public class User {

    /**
     * ユーザーID
     * データベース上の主キーとして使用される
     */
    private Long id;

    /**
     * ユーザー名
     * システム内でユーザーを識別するための表示名
     */
    private String username;

    /**
     * メールアドレス
     * ユーザー連絡先およびログイン認証に使用される
     */
    private String email;

    /**
     * デフォルトコンストラクタ
     */
    public User() {
        // デフォルトコンストラクタ
    }

    /**
     * ユーザー情報を指定して初期化するコンストラクタ
     *
     * @param username ユーザー名
     * @param email    メールアドレス
     */
    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }

    /**
     * ユーザー情報を取得する。
     *
     * @return ユーザー情報の文字列表現
     */
    public String getUserInfo() {
        return username + " (" + email + ")";
    }

    /**
     * 指定されたロールを持っているか確認する。
     *
     * @param role 確認するロール
     * @return ロールを持っている場合はtrue、それ以外はfalse
     * @throws IllegalArgumentException roleがnullの場合
     */
    public boolean hasRole(String role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        // ...existing code...
    }

    // 以下はgetter/setterメソッド

    /**
     * ユーザーIDを取得する。
     * 
     * @return ユーザーID
     */
    public Long getId() {
        return id;
    }

    /**
     * ユーザーIDを設定する。
     * 
     * @param id 設定するユーザーID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * ユーザー名を取得する。
     * 
     * @return ユーザー名
     */
    public String getUsername() {
        return username;
    }

    /**
     * ユーザー名を設定する。
     * 
     * @param username 設定するユーザー名
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * メールアドレスを取得する。
     * 
     * @return メールアドレス
     */
    public String getEmail() {
        return email;
    }

    /**
     * メールアドレスを設定する。
     * 
     * @param email 設定するメールアドレス
     */
    public void setEmail(String email) {
        this.email = email;
    }
}
