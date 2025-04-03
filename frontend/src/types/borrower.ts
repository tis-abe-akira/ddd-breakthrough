export interface Borrower {
  id?: number;
  name: string;
  creditRating: string;
  financialStatements?: string;
  contactInformation?: string;
  companyType?: string;
  industry?: string;
  version?: number;
}