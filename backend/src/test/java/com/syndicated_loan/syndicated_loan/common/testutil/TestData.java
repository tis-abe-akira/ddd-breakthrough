package com.syndicated_loan.syndicated_loan.common.testutil;

import com.syndicated_loan.syndicated_loan.common.dto.*;
import lombok.Value;

@Value
public class TestData {
    BorrowerDto borrower;
    InvestorDto leadBank1;
    InvestorDto leadBank2;
    InvestorDto member1;
    InvestorDto member2;
    SyndicateDto syndicate1;
    SyndicateDto syndicate2;
    SharePieDto sharePie1;
    SharePieDto sharePie2;
    FacilityDto facility1;
    FacilityDto facility2;
    FacilityInvestmentDto facilityInvestment1;
    FacilityInvestmentDto facilityInvestment2;
}
