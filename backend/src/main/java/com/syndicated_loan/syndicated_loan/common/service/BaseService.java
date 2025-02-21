package com.syndicated_loan.syndicated_loan.common.service;

import java.util.List;
import java.util.Optional;

/**
 * 基本的なCRUD操作を定義するジェネリックなサービスインターフェース。
 * このインターフェースは、エンティティに対する標準的なデータベース操作を提供します。
 * 全てのサービスクラスの基底インターフェースとして機能します。
 *
 * @param <T> 操作対象のエンティティの型
 * @param <ID> エンティティのID型
 * @param <D> エンティティに対応するDTO型
 */
public interface BaseService<T, ID, D> {
    /**
     * 新しいエンティティを作成します。
     * 
     * @param dto 作成するエンティティの情報を含むDTO
     * @return 作成されたエンティティのDTO
     * @throws BusinessException DTOの内容が不正な場合、または永続化に失敗した場合
     */
    D create(D dto);

    /**
     * 指定されたIDのエンティティを検索します。
     * 
     * @param id 検索対象のエンティティのID
     * @return エンティティのDTO。存在しない場合は空のOptional
     * @throws IllegalArgumentException IDがnullの場合
     */
    Optional<D> findById(ID id);

    /**
     * 全てのエンティティを取得します。
     * データ量が多い場合は、ページネーションの使用を検討してください。
     * 
     * @return 全エンティティのDTOのリスト
     */
    List<D> findAll();

    /**
     * 指定されたIDのエンティティを更新します。
     * 
     * @param id 更新対象のエンティティのID
     * @param dto 更新内容を含むDTO
     * @return 更新されたエンティティのDTO
     * @throws BusinessException エンティティが存在しない場合、またはDTOの内容が不正な場合
     * @throws IllegalArgumentException IDがnullの場合
     */
    D update(ID id, D dto);

    /**
     * 指定されたIDのエンティティを削除します。
     * 
     * @param id 削除対象のエンティティのID
     * @throws BusinessException エンティティが存在しない場合
     * @throws IllegalArgumentException IDがnullの場合
     */
    void delete(ID id);

    /**
     * DTOをエンティティに変換します。
     * この操作は、データの整合性チェックも行います。
     * 
     * @param dto 変換元のDTO
     * @return 変換されたエンティティ
     * @throws BusinessException DTOの内容が不正な場合
     * @throws IllegalArgumentException DTOがnullの場合
     */
    T toEntity(D dto);

    /**
     * エンティティをDTOに変換します。
     * この操作は、クライアントに返すデータの整形も行います。
     * 
     * @param entity 変換元のエンティティ
     * @return 変換されたDTO
     * @throws IllegalArgumentException エンティティがnullの場合
     */
    D toDto(T entity);
}
