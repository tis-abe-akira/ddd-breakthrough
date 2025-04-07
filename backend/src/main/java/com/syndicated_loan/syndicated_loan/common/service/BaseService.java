package com.syndicated_loan.syndicated_loan.common.service;

import java.util.List;
import java.util.Optional;

/**
 * 基本的なCRUD操作を定義するジェネリックなサービスインターフェース。
 * すべてのサービスクラスの基本となるインターフェースです。
 * 
 * @param <T>  エンティティの型
 * @param <ID> IDの型
 * @param <D>  DTOの型
 */
public interface BaseService<T, ID, D> {
    /**
     * 新しいエンティティを作成します
     * 
     * @param dto 作成するエンティティのDTO
     * @return 作成されたエンティティのDTO
     */
    D create(D dto);

    /**
     * IDによりエンティティを検索します
     * 
     * @param id エンティティのID
     * @return エンティティのDTO（存在しない場合はEmpty）
     */
    Optional<D> findById(ID id);

    /**
     * すべてのエンティティを取得します
     * 
     * @return エンティティのDTOのリスト
     */
    List<D> findAll();

    /**
     * エンティティを更新します
     * 
     * @param id  更新対象のエンティティのID
     * @param dto 更新内容を含むDTO
     * @return 更新されたエンティティのDTO
     */
    D update(ID id, D dto);

    /**
     * エンティティを削除します
     * 
     * @param id 削除対象のエンティティのID
     */
    void delete(ID id);

    /**
     * DTOからエンティティへ変換します
     * 
     * @param dto 変換元のDTO
     * @return 変換されたエンティティ
     */
    T toEntity(D dto);

    /**
     * エンティティからDTOへ変換します
     * 
     * @param entity 変換元のエンティティ
     * @return 変換されたDTO
     */
    D toDto(T entity);
}
