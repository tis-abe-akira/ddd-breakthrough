package com.syndicated_loan.syndicated_loan.common.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.syndicated_loan.syndicated_loan.common.dto.TransactionDto;
import com.syndicated_loan.syndicated_loan.common.dto.AmountPieDto;
import com.syndicated_loan.syndicated_loan.common.dto.InvestorDto;
import com.syndicated_loan.syndicated_loan.common.entity.Transaction;
import com.syndicated_loan.syndicated_loan.common.entity.AmountPie;
import com.syndicated_loan.syndicated_loan.common.entity.Position;
import com.syndicated_loan.syndicated_loan.common.repository.TransactionRepository;
import com.syndicated_loan.syndicated_loan.common.exception.BusinessException;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;

/**
 * シンジケートローンにおける取引（Transaction）を管理する基底サービスクラス。
 * ドローダウン、返済、利息支払いなど、各種取引の共通機能を提供します。
 * ポジションとの関連付け、金額配分の管理、投資家への影響の反映などを行います。
 * 
 * @param <T> 取引エンティティ型
 * @param <D> 取引DTO型
 * @param <R> 取引リポジトリ型
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public abstract class TransactionService<T extends Transaction, D extends TransactionDto, R extends TransactionRepository<T>>
        extends AbstractBaseService<T, Long, D, R> {

    protected final AmountPieService amountPieService;
    protected final PositionService positionService;
    protected final InvestorService investorService;

    /**
     * コンストラクタ
     * 
     * @param repository リポジトリインスタンス
     * @param amountPieService 金額配分サービス
     * @param positionService ポジションサービス
     * @param investorService 投資家サービス
     */
    protected TransactionService(
            R repository,
            AmountPieService amountPieService,
            PositionService positionService,
            InvestorService investorService) {
        super(repository);
        this.amountPieService = amountPieService;
        this.positionService = positionService;
        this.investorService = investorService;
    }

    @Override
    protected void setEntityId(T entity, Long id) {
        entity.setId(id);
    }

    /**
     * 取引エンティティの基本プロパティを設定します。
     * 関連するポジションとAmountPieの検証と設定を行います。
     * 
     * @param entity 設定対象のエンティティ
     * @param dto 設定元のDTO
     * @throws BusinessException 以下の場合に発生:
     *                          - ポジションが存在しない場合（POSITION_NOT_FOUND）
     *                          - AmountPieが存在しない場合（AMOUNT_PIE_NOT_FOUND）
     */
    protected void setBaseProperties(T entity, D dto) {
        Position relatedPosition = positionService.findById(dto.getRelatedPositionId())
                .map(positionService::toEntity)
                .orElseThrow(() -> new BusinessException("Position not found", "POSITION_NOT_FOUND"));
        entity.setRelatedPosition(relatedPosition);

        if (dto.getAmountPieId() != null) {
            AmountPie amountPie = amountPieService.findById(dto.getAmountPieId())
                    .map(amountPieService::toEntity)
                    .orElseThrow(() -> new BusinessException("AmountPie not found", "AMOUNT_PIE_NOT_FOUND"));
            entity.setAmountPie(amountPie);
        }
    }

    /**
     * DTOの基本プロパティを設定します。
     * ポジションの参照情報とAmountPieの情報を設定します。
     * 
     * @param dto 設定対象のDTO
     * @param entity 設定元のエンティティ
     */
    protected void setBaseDtoProperties(D dto, T entity) {
        dto.setPositionType(entity.getRelatedPosition().getType());
        dto.setPositionReference(generatePositionReference(entity.getRelatedPosition()));
        if (entity.getAmountPie() != null) {
            dto.setAmountPie(amountPieService.toDto(entity.getAmountPie()));
        }
    }

    /**
     * ポジションの参照情報を生成します。
     * 
     * @param position ポジション
     * @return 「タイプ-ID」形式の参照情報
     */
    private String generatePositionReference(Position position) {
        return String.format("%s-%d", position.getType(), position.getId());
    }

    /**
     * 指定されたポジションに関連する取引を検索します。
     * 
     * @param positionId ポジションID
     * @return 関連する取引のDTOリスト
     * @throws BusinessException ポジションが存在しない場合（POSITION_NOT_FOUND）
     */
    public List<D> findByRelatedPosition(Long positionId) {
        Position position = positionService.findById(positionId)
                .map(positionService::toEntity)
                .orElseThrow(() -> new BusinessException("Position not found", "POSITION_NOT_FOUND"));
        return repository.findByRelatedPosition(position).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * 指定されたポジションの特定期間内の取引を検索します。
     * 
     * @param positionId ポジションID
     * @param startDate 期間開始日時
     * @param endDate 期間終了日時
     * @return 条件を満たす取引のDTOリスト
     * @throws BusinessException ポジションが存在しない場合（POSITION_NOT_FOUND）
     */
    public List<D> findByRelatedPositionAndDateBetween(
            Long positionId, LocalDateTime startDate, LocalDateTime endDate) {
        Position position = positionService.findById(positionId)
                .map(positionService::toEntity)
                .orElseThrow(() -> new BusinessException("Position not found", "POSITION_NOT_FOUND"));
        return repository.findByRelatedPositionAndDateBetween(position, startDate, endDate).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * 指定されたタイプの取引を検索します。
     * 
     * @param type 取引タイプ
     * @return 指定されたタイプの取引のDTOリスト
     */
    public List<D> findByType(String type) {
        return repository.findByType(type).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * 指定されたポジションと取引タイプの取引を検索します。
     * 
     * @param positionId ポジションID
     * @param type 取引タイプ
     * @return 条件を満たす取引のDTOリスト
     * @throws BusinessException ポジションが存在しない場合（POSITION_NOT_FOUND）
     */
    public List<D> findByRelatedPositionAndType(Long positionId, String type) {
        Position position = positionService.findById(positionId)
                .map(positionService::toEntity)
                .orElseThrow(() -> new BusinessException("Position not found", "POSITION_NOT_FOUND"));
        return repository.findByRelatedPositionAndType(position, type).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * 取引のAmountPieを更新します。
     * 
     * @param transactionId 取引ID
     * @param amountPieDto 新しいAmountPieのDTO
     * @return 更新された取引のDTO
     * @throws BusinessException 取引が存在しない場合（TRANSACTION_NOT_FOUND）
     */
    @Transactional
    public D updateAmountPie(Long transactionId, AmountPieDto amountPieDto) {
        T transaction = repository.findById(transactionId)
                .orElseThrow(() -> new BusinessException("Transaction not found", "TRANSACTION_NOT_FOUND"));

        AmountPie amountPie = amountPieService.toEntity(amountPieDto);
        transaction.setAmountPie(amountPie);

        return toDto(repository.save(transaction));
    }

    /**
     * 投資家の現在の投資額を更新します。
     * AmountPieの配分に基づいて各投資家の投資額を増減させます。
     * 
     * @param amountPie 金額配分情報
     * @param multiplier 投資額の増減を示す乗数（増加:1, 減少:-1）
     * @throws BusinessException 投資家が存在しない場合（INVESTOR_NOT_FOUND）
     */
    @Transactional
    protected void updateInvestorCurrentInvestments(AmountPieDto amountPie, BigDecimal multiplier) {
        if (amountPie == null || amountPie.getAmounts() == null) {
            return;
        }

        amountPie.getAmounts().forEach((investorId, amount) -> {
            InvestorDto investor = investorService.findById(investorId)
                    .orElseThrow(() -> new BusinessException("Investor not found", "INVESTOR_NOT_FOUND"));

            BigDecimal currentAmount = investor.getCurrentInvestments();
            BigDecimal updateAmount = amount.multiply(multiplier);
            investor.setCurrentInvestments(currentAmount.add(updateAmount));

            investorService.update(investorId, investor);
        });
    }
}
