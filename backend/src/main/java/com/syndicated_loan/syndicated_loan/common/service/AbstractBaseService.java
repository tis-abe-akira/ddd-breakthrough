package com.syndicated_loan.syndicated_loan.common.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.syndicated_loan.syndicated_loan.common.exception.BusinessException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * BaseServiceインターフェースの抽象実装クラス。
 * 基本的なCRUD操作の共通実装を提供します。
 * 
 * @param <T>  エンティティの型
 * @param <ID> IDの型
 * @param <D>  DTOの型
 * @param <R>  リポジトリの型
 */
public abstract class AbstractBaseService<T, ID, D, R extends JpaRepository<T, ID>>
        implements BaseService<T, ID, D> {

    /**
     * エンティティに対する操作を行うリポジトリ
     */
    protected final R repository;

    /**
     * コンストラクタ
     * 
     * @param repository 使用するリポジトリ
     */
    protected AbstractBaseService(R repository) {
        this.repository = repository;
    }

    /**
     * リポジトリを取得します
     * 
     * @return 使用中のリポジトリ
     */
    protected R getRepository() {
        return repository;
    }

    /**
     * 新しいエンティティを作成します
     * 
     * @param dto 作成するエンティティの情報を含むDTO
     * @return 作成されたエンティティのDTO
     */
    @Override
    @Transactional
    public D create(D dto) {
        T entity = toEntity(dto);
        T savedEntity = repository.save(entity);
        return toDto(savedEntity);
    }

    /**
     * IDによりエンティティを検索します
     * 
     * @param id 検索するエンティティのID
     * @return エンティティのDTO（オプショナル）
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<D> findById(ID id) {
        return repository.findById(id).map(this::toDto);
    }

    /**
     * すべてのエンティティを取得します
     * 
     * @return エンティティのDTOのリスト
     */
    @Override
    @Transactional(readOnly = true)
    public List<D> findAll() {
        return repository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * エンティティを更新します
     * 
     * @param id  更新するエンティティのID
     * @param dto 更新情報を含むDTO
     * @return 更新されたエンティティのDTO
     * @throws BusinessException エンティティが存在しない場合
     */
    @Override
    @Transactional
    public D update(ID id, D dto) {
        if (!repository.existsById(id)) {
            throw new BusinessException("Entity not found with id: " + id, "ENTITY_NOT_FOUND");
        }
        T entity = toEntity(dto);
        setEntityId(entity, id);
        T savedEntity = repository.save(entity);
        return toDto(savedEntity);
    }

    /**
     * エンティティを削除します
     * 
     * @param id 削除するエンティティのID
     * @throws BusinessException エンティティが存在しない場合
     */
    @Override
    @Transactional
    public void delete(ID id) {
        if (!repository.existsById(id)) {
            throw new BusinessException("Entity not found with id: " + id, "ENTITY_NOT_FOUND");
        }
        repository.deleteById(id);
    }

    /**
     * エンティティにIDを設定します
     * 
     * @param entity IDを設定するエンティティ
     * @param id     設定するID
     */
    protected abstract void setEntityId(T entity, ID id);
}
