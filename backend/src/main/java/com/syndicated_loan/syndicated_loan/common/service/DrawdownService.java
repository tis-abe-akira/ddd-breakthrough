package com.syndicated_loan.syndicated_loan.common.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.syndicated_loan.syndicated_loan.common.dto.DrawdownDto;
import com.syndicated_loan.syndicated_loan.common.dto.LoanDto;
import com.syndicated_loan.syndicated_loan.common.dto.AmountPieDto;
import com.syndicated_loan.syndicated_loan.common.entity.AmountPie;
import com.syndicated_loan.syndicated_loan.common.entity.Drawdown;
import com.syndicated_loan.syndicated_loan.common.entity.Facility;
import com.syndicated_loan.syndicated_loan.common.entity.Loan;
import com.syndicated_loan.syndicated_loan.common.repository.DrawdownRepository;
import com.syndicated_loan.syndicated_loan.common.exception.BusinessException;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;

/**
 * ドローダウン（融資引き出し）操作を提供するサービスクラス。
 * 
 * <p>
 * このサービスは、ファシリティからの資金引き出しに関する処理を管理します。
 * ドローダウン時にはファシリティの利用可能額を減少させるとともに、新たな
 * ローンエンティティを生成します。また、投資家ごとのAmountPie（金額配分）も
 * 作成・管理します。
 * </p>
 * 
 * <p>
 * ドローダウンの実行処理や金額の更新、特定条件での検索機能も提供します。
 * 返済スケジュールの自動生成やファシリティ利用率の計算も行います。
 * </p>
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class DrawdownService
        extends TransactionService<Drawdown, DrawdownDto, DrawdownRepository> {

    private final FacilityService facilityService;
    private final LoanService loanService; // 追加！

    public DrawdownService(
            DrawdownRepository repository,
            AmountPieService amountPieService,
            PositionService positionService,
            FacilityService facilityService,
            LoanService loanService, // 追加！
            InvestorService investorService) {
        super(repository, amountPieService, positionService, investorService);
        this.facilityService = facilityService;
        this.loanService = loanService; // 追加！

    }

    @Override
    public Drawdown toEntity(DrawdownDto dto) {
        Drawdown entity = new Drawdown();
        entity.setId(dto.getId());
        entity.setType("DRAWDOWN");
        entity.setDate(dto.getDate());
        entity.setAmount(dto.getDrawdownAmount());

        // Facilityの設定
        Facility facility = facilityService.findById(dto.getRelatedFacilityId())
                .map(facilityService::toEntity)
                .orElseThrow(() -> new BusinessException("Facility not found", "FACILITY_NOT_FOUND"));
        entity.setRelatedFacility(facility);

        // Loanエンティティの作成と永続化
        LoanDto loanDto = LoanDto.builder()
                .amount(dto.getDrawdownAmount())
                .totalAmount(dto.getDrawdownAmount())
                .borrowerId(facility.getBorrower().getId())
                .facilityId(facility.getId())
                .startDate(LocalDate.now())
                .endDate(facility.getEndDate())
                .term((int) ChronoUnit.MONTHS.between(LocalDate.now(), facility.getEndDate())) // termを追加！
                .interestRate(facility.getInterestRate())
                .build();

        // Loanを永続化して関連付け
        LoanDto savedLoan = loanService.create(loanDto);
        entity.setRelatedPosition(loanService.toEntity(savedLoan));

        // AmountPieの設定
        if (dto.getAmountPieId() != null) {
            AmountPie amountPie = amountPieService.findById(dto.getAmountPieId())
                    .map(amountPieService::toEntity)
                    .orElseThrow(() -> new BusinessException("AmountPie not found", "AMOUNT_PIE_NOT_FOUND"));
            entity.setAmountPie(amountPie);
        }

        entity.setDrawdownAmount(dto.getDrawdownAmount());
        entity.setVersion(dto.getVersion());
        return entity;
    }

    @Override
    public DrawdownDto toDto(Drawdown entity) {
        DrawdownDto dto = DrawdownDto.builder()
                .id(entity.getId())
                .type(entity.getType())
                .date(entity.getDate())
                .processedDate(entity.getProcessedDate())
                .amount(entity.getAmount())
                .relatedPositionId(entity.getRelatedPosition().getId())
                .amountPieId(entity.getAmountPie() != null ? entity.getAmountPie().getId() : null)
                .relatedFacilityId(entity.getRelatedFacility().getId())
                .drawdownAmount(entity.getDrawdownAmount())
                .version(entity.getVersion())
                .build();

        // レスポンス用の追加情報
        setBaseDtoProperties(dto, entity);
        dto.setRelatedFacility(facilityService.toDto(entity.getRelatedFacility()));

        // AmountPieの情報も設定
        if (entity.getAmountPie() != null) {
            dto.setAmountPie(amountPieService.toDto(entity.getAmountPie()));
        }

        // 残額と利用率の計算
        Facility facility = entity.getRelatedFacility();
        dto.setRemainingFacilityAmount(facility.getAvailableAmount());
        if (facility.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal utilizationRate = BigDecimal.ONE
                    .subtract(facility.getAvailableAmount()
                            .divide(facility.getTotalAmount(), 4, RoundingMode.HALF_UP))
                    .multiply(new BigDecimal("100"));
            dto.setUtilizationRate(utilizationRate);
        }

        return dto;
    }

    /**
     * 配下のファシリティに関連するドローダウンを探し出す術なり！
     * 
     * <p>
     * 指定されたファシリティIDに紐づくドローダウンを一挙に取得せよ。
     * 見つからぬ場合は敵将を討ち取れぬが如く例外を投げるぞ！
     * </p>
     *
     * @param facilityId 探索すべきファシリティのID
     * @return そのファシリティに関連するドローダウンのリスト
     * @throws BusinessException ファシリティが見つからぬ場合に発せられる
     */
    public List<DrawdownDto> findByRelatedFacility(Long facilityId) {
        Facility facility = facilityService.findById(facilityId)
                .map(facilityService::toEntity)
                .orElseThrow(() -> new BusinessException("Facility not found", "FACILITY_NOT_FOUND"));
        return repository.findByRelatedFacility(facility).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * 指定された額よりも大なる金額のドローダウンを探し出す！
     * 
     * <p>
     * 大軍を率いるが如く、大きな金額のドローダウンのみを抽出する。
     * 武将の器の大きさを見極めるが如し！
     * </p>
     *
     * @param amount 比較すべき基準額
     * @return 指定された金額を上回るドローダウンのリスト
     */
    public List<DrawdownDto> findByDrawdownAmountGreaterThan(BigDecimal amount) {
        return repository.findByDrawdownAmountGreaterThan(amount).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * 特定のファシリティに紐づき、かつ指定額を超える大金のドローダウンを探し出す！
     * 
     * <p>
     * 大名の所領と財力を兼ね備えた重要なドローダウンを発見せよ。
     * 二つの条件を満たす精鋭の如き取引のみを取得する。
     * </p>
     *
     * @param facilityId 探索すべきファシリティのID
     * @param amount     比較すべき基準額
     * @return 条件を満たすドローダウンのリスト
     * @throws BusinessException ファシリティが見つからぬ場合に発せられる
     */
    public List<DrawdownDto> findByRelatedFacilityAndDrawdownAmountGreaterThan(Long facilityId, BigDecimal amount) {
        Facility facility = facilityService.findById(facilityId)
                .map(facilityService::toEntity)
                .orElseThrow(() -> new BusinessException("Facility not found", "FACILITY_NOT_FOUND"));
        return repository.findByRelatedFacilityAndDrawdownAmountGreaterThan(facility, amount).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * ドローダウンを実行し、資金を引き出す決戦の時！
     * 
     * <p>
     * 戦を決するが如く、ドローダウンを実行し、ファシリティの利用可能額を減少させる。
     * また、投資家の現在の投資額を増加させ、ドローダウンの状態を「実行済み」と記す。
     * 利用可能額が足りぬ場合は撤退のごとく例外を投げる！
     * </p>
     *
     * @param drawdownId 実行すべきドローダウンのID
     * @return 実行後のドローダウン情報
     * @throws BusinessException ドローダウンが見つからぬ場合や、利用可能額が不足する場合に発せられる
     */
    @Transactional
    public DrawdownDto executeDrawdown(Long drawdownId) {
        Drawdown drawdown = repository.findById(drawdownId)
                .orElseThrow(() -> new BusinessException("Drawdown not found", "DRAWDOWN_NOT_FOUND"));

        Facility facility = drawdown.getRelatedFacility();
        BigDecimal newAvailableAmount = facility.getAvailableAmount().subtract(drawdown.getDrawdownAmount());

        if (newAvailableAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Insufficient available amount", "INSUFFICIENT_AVAILABLE_AMOUNT");
        }

        // ファシリティの利用可能額を更新
        facilityService.updateAvailableAmount(facility.getId(), newAvailableAmount);

        // 投資家の現在の投資額を更新（増額）
        // AmountPieエンティティをDTOに変換してから渡す
        AmountPieDto amountPieDto = amountPieService.toDto(drawdown.getAmountPie());
        updateInvestorCurrentInvestments(amountPieDto, BigDecimal.ONE);

        // ドローダウンのステータスを更新
        drawdown.setStatus("EXECUTED");
        drawdown.setProcessedDate(java.time.LocalDateTime.now());

        return toDto(repository.save(drawdown));
    }

    /**
     * ドローダウン金額を変更する、戦略の練り直しのごとし！
     * 
     * <p>
     * 戦の前に兵力を再配置するが如く、実行前のドローダウン金額を更新する。
     * 既に実行済みの場合は、過ぎたる戦を覆すことなかれと例外を投げる。
     * また、新たな金額は正の数かつファシリティの利用可能額以下であるべし！
     * </p>
     *
     * @param drawdownId 更新すべきドローダウンのID
     * @param newAmount  新しく設定すべきドローダウン金額
     * @return 更新されたドローダウン情報
     * @throws BusinessException ドローダウンが見つからぬ場合、既に実行済みの場合、
     *                           金額が不正な場合、または利用可能額を超える場合に発せられる
     */
    @Transactional
    public DrawdownDto updateDrawdownAmount(Long drawdownId, BigDecimal newAmount) {
        Drawdown drawdown = repository.findById(drawdownId)
                .orElseThrow(() -> new BusinessException("Drawdown not found", "DRAWDOWN_NOT_FOUND"));

        if ("EXECUTED".equals(drawdown.getStatus())) {
            throw new BusinessException("Cannot update executed drawdown", "DRAWDOWN_ALREADY_EXECUTED");
        }

        if (newAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Drawdown amount must be positive", "INVALID_DRAWDOWN_AMOUNT");
        }

        Facility facility = drawdown.getRelatedFacility();
        if (newAmount.compareTo(facility.getAvailableAmount()) > 0) {
            throw new BusinessException("Drawdown amount exceeds available amount", "INSUFFICIENT_AVAILABLE_AMOUNT");
        }

        drawdown.setDrawdownAmount(newAmount);
        drawdown.setAmount(newAmount); // 取引金額も更新

        return toDto(repository.save(drawdown));
    }

    @Override
    @Transactional
    public DrawdownDto create(DrawdownDto dto) {
        // AmountPieの生成
        if (dto.getAmountPie() != null) {
            var amountPie = amountPieService.create(dto.getAmountPie());
            dto.setAmountPieId(amountPie.getId());
        }

        // 基底クラスのcreateを呼び出し
        DrawdownDto createdDto = super.create(dto);

        // 返済スケジュールの生成
        Drawdown drawdown = repository.findById(createdDto.getId())
                .orElseThrow(() -> new BusinessException("Drawdown not found", "DRAWDOWN_NOT_FOUND"));
        loanService.generateRepaymentSchedules((Loan) drawdown.getRelatedPosition());

        return createdDto;
    }

    @Override
    @Transactional
    public DrawdownDto update(Long id, DrawdownDto dto) {
        // AmountPieの更新
        if (dto.getAmountPie() != null) {
            // 既存のAmountPieを取得
            Drawdown existingDrawdown = repository.findById(id)
                    .orElseThrow(() -> new BusinessException("Drawdown not found", "DRAWDOWN_NOT_FOUND"));

            if (existingDrawdown.getAmountPie() != null) {
                // 既存のAmountPieを更新
                var updatedAmountPie = amountPieService.update(
                        existingDrawdown.getAmountPie().getId(),
                        dto.getAmountPie());
                dto.setAmountPieId(updatedAmountPie.getId());
            } else {
                // 新しいAmountPieを作成
                var newAmountPie = amountPieService.create(dto.getAmountPie());
                dto.setAmountPieId(newAmountPie.getId());
            }
        }

        // 基底クラスのupdateを呼び出し
        return super.update(id, dto);
    }
}
