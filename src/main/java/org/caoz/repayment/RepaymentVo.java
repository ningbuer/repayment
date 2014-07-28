package org.caoz.repayment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RepaymentVo {

	final BigDecimal repayTotalAmount;// 还款总金额
	final Date repayDate;

	BigDecimal repaySurplusAmount;// 还款剩余金额
	BigDecimal toleranceAmount = new BigDecimal(0d);// 本次还款容差金额
	String toleranceType = "";// 本次还款容差类型

	List<RepaymentDtlVo> dtlVos = new ArrayList<RepaymentDtlVo>();

	public RepaymentVo(BigDecimal repayAmount, Date repayDate) {
		this.repayTotalAmount = this.repaySurplusAmount = repayAmount;
		this.repayDate = repayDate;
	}

	public String toString() {
		return String.format("还款金额为%s,详情为%s,容差金额为 %s,容差类型为%s", repayTotalAmount, dtlVos, toleranceAmount, toleranceType);
	}

}
