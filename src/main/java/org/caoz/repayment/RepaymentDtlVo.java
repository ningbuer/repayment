package org.caoz.repayment;

import java.math.BigDecimal;

public class RepaymentDtlVo {

	final RepaymentVo repaymentVo;
	final RepaymentDtl dtl;
	final BigDecimal repayAmount;
	final String type;

	RepaymentDtlVo(RepaymentVo repaymentVo, RepaymentDtl dtl, BigDecimal repayAmount, String type) {
		this.repaymentVo = repaymentVo;
		this.dtl = dtl;
		this.repayAmount = repayAmount;
		this.type = type;

		this.repaymentVo.dtlVos.add(this);
	}

	public int getTerm() {
		return dtl.currTerm;
	}

	public BigDecimal getRepayAmount() {
		return repayAmount;
	}

	public String getType() {
		return type;
	}

	public String toString() {
		return String.format("当前还款第%s期,金额为%s元,类型为%s", getTerm(), repayAmount, type);
	}

}
