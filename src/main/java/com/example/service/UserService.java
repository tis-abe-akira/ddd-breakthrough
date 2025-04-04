package com.example.service;

import com.example.entity.User;
import com.example.exception.DuplicateUserException;
import com.example.exception.ServiceException;
import com.example.exception.ValidationException;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ユーザー関連のビジネスロジックを提供するサービスクラス。
 * ユーザーの登録、検索、更新、削除などの操作を行う。
 * <p>
 * このサービスはトランザクション管理を行い、データの整合性を保証する。
 * また、各種例外ハンドリングによりビジネスルールの検証を行う。
 * </p>
 * 
 * @author システム開発チーム
 * @version 1.0
 */
@Service
@Transactional
public class UserService {

    /**
     * ユーザー情報にアクセスするためのリポジトリ
     */
    private final UserRepository userRepository;

    /**
     * コンストラクタを通じてリポジトリを注入する。
     *
     * @param userRepository ユーザーリポジトリ
     */
    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 指定されたIDのユーザーを取得する。
     *
     * @param userId 取得するユーザーのID
     * @return 見つかったユーザー。存在しない場合はnull
     * @throws ServiceException データベースアクセス時にエラーが発生した場合
     */
    @Transactional(readOnly = true)
    public User findById(Long userId) {
        // ...existing code...
    }

    /**
     * 新しいユーザーを登録する。
     * <p>
     * ユーザー情報のバリデーションを行い、メールアドレスの重複チェックを実施する。
     * </p>
     *
     * @param user 登録するユーザー情報
     * @return 登録されたユーザー（IDが設定された状態）
     * @throws ValidationException    ユーザー情報が不正な場合
     * @throws DuplicateUserException 同じメールアドレスのユーザーが既に存在する場合
     */
    @Transactional
    public User registerUser(User user) {
        // ...existing code...
    }

    /**
     * 指定されたメールアドレスのユーザーを検索する。
     *
     * @param email 検索するメールアドレス
     * @return 見つかったユーザー。存在しない場合はnull
     */
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        // ...existing code...
    }
}
