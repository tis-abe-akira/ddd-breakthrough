package com.syndicated_loan.syndicated_loan.common.service;

import java.util.List;
import java.util.Optional;

/**
 * 基本的なCRUD操作を定義するジェネリックなサービスインターフェース
 * @param <T> エンティティの型
 * @param <ID> IDの型
 * @param <D> DTOの型
 */
public interface BaseService<T, ID, D> {
    /**
     * エンティティを作成
     * @param dto 作成するエンティティのDTO
     * @return 作成されたエンティティのDTO
     */
    D create(D dto);

    /**
     * IDによるエンティティの取得
     * @param id エンティティのID
     * @return エンティティのDTO（存在しない場合はEmpty）
     */
    Optional<D> findById(ID id);

    /**
     * 全エンティティの取得
     * @return エンティティのDTOのリスト
     */
    List<D> findAll();

    /**
     * エンティティの更新
     * @param id 更新対象のエンティティのID
     * @param dto 更新内容を含むDTO
     * @return 更新されたエンティティのDTO
     */
    D update(ID id, D dto);

    /**
     * エンティティの削除
     * @param id 削除対象のエンティティのID
     */
    void delete(ID id);

    /**
     * DTOからエンティティへの変換
     * @param dto 変換元のDTO
     * @return 変換されたエンティティ
     */
    T toEntity(D dto);

    /**
     * エンティティからDTOへの変換
     * @param entity 変換元のエンティティ
     * @return 変換されたDTO
     */
    D toDto(T entity);
}
