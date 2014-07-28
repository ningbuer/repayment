package org.caoz.repayment;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.Test;

public class RepaymentTest {

	@Test
	public void singleRepaymentTest() throws Exception {
		/**
		 *  借款1000元 ,30天后还清,到期需要还1010元
		 */
		Date payDate = new Date();
		Repayment repayment = new Repayment(new BigDecimal(1000), 1, (0.01 / 30) * 30, payDate, 30);
		SurplusRepaymentVo sVo = repayment.getSurplusRepaymentVo(DateUtils.addDays(payDate, 30));
		BigDecimal amount = sVo.getSurplusTotalAmount();
		Assert.assertEquals(amount.doubleValue(), 1010.00d, 0.01);

	}

}
