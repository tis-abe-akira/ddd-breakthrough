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

@Slf4j
@Service
@Transactional(readOnly = true)
public abstract class TransactionService<T extends Transaction, D extends TransactionDto, R extends TransactionRepository<T>>
        extends AbstractBaseService<T, Long, D, R> {

    // privateからprotectedに変更
    protected final AmountPieService amountPieService;
    protected final PositionService positionService;
    protected final InvestorService investorService;

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

    protected void setBaseDtoProperties(D dto, T entity) {
        dto.setPositionType(entity.getRelatedPosition().getType());
        dto.setPositionReference(generatePositionReference(entity.getRelatedPosition()));
        if (entity.getAmountPie() != null) {
            dto.setAmountPie(amountPieService.toDto(entity.getAmountPie()));
        }
    }

    // ポジションの参照情報を生成
    private String generatePositionReference(Position position) {
        return String.format("%s-%d", position.getType(), position.getId());
    }

    // 追加の検索メソッド
    public List<D> findByRelatedPosition(Long positionId) {
        Position position = positionService.findById(positionId)
                .map(positionService::toEntity)
                .orElseThrow(() -> new BusinessException("Position not found", "POSITION_NOT_FOUND"));
        return repository.findByRelatedPosition(position).stream()
                .map(this::toDto)
                .toList();
    }

    public List<D> findByRelatedPositionAndDateBetween(
            Long positionId, LocalDateTime startDate, LocalDateTime endDate) {
        Position position = positionService.findById(positionId)
                .map(positionService::toEntity)
                .orElseThrow(() -> new BusinessException("Position not found", "POSITION_NOT_FOUND"));
        return repository.findByRelatedPositionAndDateBetween(position, startDate, endDate).stream()
                .map(this::toDto)
                .toList();
    }

    public List<D> findByType(String type) {
        return repository.findByType(type).stream()
                .map(this::toDto)
                .toList();
    }

    public List<D> findByRelatedPositionAndType(Long positionId, String type) {
        Position position = positionService.findById(positionId)
                .map(positionService::toEntity)
                .orElseThrow(() -> new BusinessException("Position not found", "POSITION_NOT_FOUND"));
        return repository.findByRelatedPositionAndType(position, type).stream()
                .map(this::toDto)
                .toList();
    }

    // AmountPieの更新
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
     * @param amountPie 金額ピース
     * @param multiplier 乗数
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
