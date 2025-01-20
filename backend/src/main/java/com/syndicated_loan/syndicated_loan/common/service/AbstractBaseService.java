package com.syndicated_loan.syndicated_loan.common.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.syndicated_loan.syndicated_loan.common.exception.BusinessException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * BaseServiceの抽象実装クラス
 * 
 * @param <T>  エンティティの型
 * @param <ID> IDの型
 * @param <D>  DTOの型
 * @param <R>  リポジトリの型
 */
public abstract class AbstractBaseService<T, ID, D, R extends JpaRepository<T, ID>>
        implements BaseService<T, ID, D> {

    protected final R repository;

    protected AbstractBaseService(R repository) {
        this.repository = repository;
    }

    protected R getRepository() {
        return repository;
    }

    @Override
    @Transactional
    public D create(D dto) {
        T entity = toEntity(dto);
        T savedEntity = repository.save(entity);
        return toDto(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<D> findById(ID id) {
        return repository.findById(id).map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<D> findAll() {
        return repository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

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
