package com.syndicated_loan.syndicated_loan.common.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.syndicated_loan.syndicated_loan.common.exception.BusinessException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * BaseServiceの抽象実装クラス。
 * エンティティの基本的なCRUD操作を提供する抽象クラスです。
 * 
 * @param <T>  操作対象のエンティティの型
 * @param <ID> エンティティのID型
 * @param <D>  エンティティに対応するDTO型
 * @param <R>  エンティティに対応するJpaRepository型
 */
public abstract class AbstractBaseService<T, ID, D, R extends JpaRepository<T, ID>>
        implements BaseService<T, ID, D> {

    /** エンティティの永続化を担当するリポジトリ */
    protected final R repository;

    /**
     * コンストラクタ
     * 
     * @param repository エンティティの永続化を担当するリポジトリ
     */
    protected AbstractBaseService(R repository) {
        this.repository = repository;
    }

    protected R getRepository() {
        return repository;
    }

    /**
     * エンティティを新規作成します。
     * 
     * @param dto 作成するエンティティの情報を含むDTO
     * @return 作成されたエンティティのDTO
     * @throws BusinessException DTOからエンティティへの変換に失敗した場合
     */
    @Override
    @Transactional
    public D create(D dto) {
        T entity = toEntity(dto);
        T savedEntity = repository.save(entity);
        return toDto(savedEntity);
    }

    /**
     * 指定されたIDのエンティティを検索します。
     * 
     * @param id 検索対象のエンティティのID
     * @return 見つかったエンティティのDTO、存在しない場合は空のOptional
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<D> findById(ID id) {
        return repository.findById(id).map(this::toDto);
    }

    /**
     * 全てのエンティティを取得します。
     * 
     * @return 全エンティティのDTOのリスト
     */
    @Override
    @Transactional(readOnly = true)
    public List<D> findAll() {
        return repository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 指定されたIDのエンティティを更新します。
     * 
     * @param id  更新対象のエンティティのID
     * @param dto 更新する内容を含むDTO
     * @return 更新されたエンティティのDTO
     * @throws BusinessException エンティティが存在しない場合、またはDTOからエンティティへの変換に失敗した場合
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
     * 指定されたIDのエンティティを削除します。
     * 
     * @param id 削除対象のエンティティのID
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
     * エンティティにIDを設定
     * 
     * @param entity エンティティ
     * @param id     設定するID
     */
    protected abstract void setEntityId(T entity, ID id);
}
