package com.example.controller;

/**
 * ユーザー関連のREST APIを提供するコントローラクラス。
 * ユーザーの検索、登録、更新、削除などのエンドポイントを定義する。
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    /**
     * 全ユーザーの一覧を取得する。
     *
     * @return ユーザー一覧
     */
    @GetMapping
    public List<UserDto> getAllUsers() {
        // ...existing code...
    }

    /**
     * 指定されたIDのユーザーを取得する。
     *
     * @param id 取得するユーザーのID
     * @return ユーザー情報
     * @throws ResourceNotFoundException 指定されたIDのユーザーが存在しない場合
     */
    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable Long id) {
        // ...existing code...
    }
}
