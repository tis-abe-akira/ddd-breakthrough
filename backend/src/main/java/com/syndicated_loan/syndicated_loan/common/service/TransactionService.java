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
 * トランザクション関連の操作を提供する抽象サービスクラス。
 * 各種取引（ドローダウン、支払い等）の基本機能を実装します。
 *
 * @param <T> トランザクションエンティティの型
 * @param <D> トランザクションDTOの型
 * @param <R> トランザクションリポジトリの型
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public abstract class TransactionService<T extends Transaction, D extends TransactionDto, R extends TransactionRepository<T>>
        extends AbstractBaseService<T, Long, D, R> {

    /**
     * AmountPie（金額配分）のサービス
     */
    protected final AmountPieService amountPieService;

    /**
     * ポジション（貸出ポジション）のサービス
     */
    protected final PositionService positionService;

    /**
     * 投資家のサービス
     */
    protected final InvestorService investorService;

    /**
     * コンストラクタ
     *
     * @param repository       リポジトリ
     * @param amountPieService AmountPieサービス
     * @param positionService  ポジションサービス
     * @param investorService  投資家サービス
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

    /**
     * エンティティにIDを設定します
     *
     * @param entity エンティティ
     * @param id     設定するID
     */
    @Override
    protected void setEntityId(T entity, Long id) {
        entity.setId(id);
    }

    /**
     * 基本的なエンティティプロパティを設定します
     *
     * @param entity 設定対象のエンティティ
     * @param dto    設定元のDTO
     * @throws BusinessException Position/AmountPieが見つからない場合
     */
    protected void setBaseProperties(T entity, D dto) {
        // 関連するPositionの設定
        Position relatedPosition = positionService.findById(dto.getRelatedPositionId())
                .map(positionService::toEntity)
                .orElseThrow(() -> new BusinessException("Position not found", "POSITION_NOT_FOUND"));
        entity.setRelatedPosition(relatedPosition);

        // AmountPieの設定
        if (dto.getAmountPieId() != null) {
            AmountPie amountPie = amountPieService.findById(dto.getAmountPieId())
                    .map(amountPieService::toEntity)
                    .orElseThrow(() -> new BusinessException("AmountPie not found", "AMOUNT_PIE_NOT_FOUND"));
            entity.setAmountPie(amountPie);
        }
    }

    /**
     * 基本的なDTOプロパティを設定します
     *
     * @param dto    設定対象のDTO
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
     * ポジションの参照情報を生成します
     *
     * @param position ポジション
     * @return 生成された参照情報
     */
    private String generatePositionReference(Position position) {
        return String.format("%s-%d", position.getType(), position.getId());
    }

    /**
     * 指定されたポジションに関連するトランザクションを検索します
     *
     * @param positionId ポジションID
     * @return トランザクションDTOのリスト
     * @throws BusinessException ポジションが見つからない場合
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
     * 指定されたポジションと日付範囲に関連するトランザクションを検索します
     *
     * @param positionId ポジションID
     * @param startDate  開始日
     * @param endDate    終了日
     * @return トランザクションDTOのリスト
     * @throws BusinessException ポジションが見つからない場合
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
     * 指定されたタイプのトランザクションを検索します
     *
     * @param type トランザクションタイプ
     * @return トランザクションDTOのリスト
     */
    public List<D> findByType(String type) {
        return repository.findByType(type).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * 指定されたポジションとタイプに関連するトランザクションを検索します
     *
     * @param positionId ポジションID
     * @param type       トランザクションタイプ
     * @return トランザクションDTOのリスト
     * @throws BusinessException ポジションが見つからない場合
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
     * トランザクションのAmountPieを更新します
     *
     * @param transactionId トランザクションID
     * @param amountPieDto  更新するAmountPie情報
     * @return 更新されたトランザクションDTO
     * @throws BusinessException トランザクションが見つからない場合
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
     * 投資家の現在の投資額を更新するメソッド
     * 
     * @param amountPie  金額ピース
     * @param multiplier 乗数（増加の場合は正、減少の場合は負）
     * @throws BusinessException 投資家が見つからない場合
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
