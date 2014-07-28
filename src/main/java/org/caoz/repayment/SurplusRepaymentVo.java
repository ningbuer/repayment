package org.caoz.repayment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class SurplusRepaymentVo {

	Date repayDate;

	public List<SurplusRepaymentDtlVo> sDtlVos = new ArrayList<>();

	public SurplusRepaymentVo(Date repayDate) {
		this.repayDate = repayDate;
	}

	/**
	 * 获取当前时间下应还总金额
	 * 
	 * @return
	 */
	public BigDecimal getSurplusTotalAmount() {
		BigDecimal amount = new BigDecimal(0.0d);
		for (Iterator<SurplusRepaymentDtlVo> iter = sDtlVos.iterator(); iter.hasNext();) {
			amount = amount.add(iter.next().getSurplusTotalAmount());
		}
		return amount;
	}

	public String toString() {
		return String.format("日期:%s,剩余还款详情:\r\n%s", RepaymentUtil.sdf.format(repayDate), sDtlVos);
	}

}
