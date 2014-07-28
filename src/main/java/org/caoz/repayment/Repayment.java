package org.caoz.repayment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;

public class Repayment {

	final double rate;// 利率
	final BigDecimal totalPrincipal;// 还款总本金
	final int totalTerm;// 总期数
	final BigDecimal eachRepayment;// 每期还款金额

	final Date startDate;// 开始时间
	final int interval;// 间隔天数

	double nRate = 0.01d / 30;
	double bRate = 0.01d / 30;
	double pRate = 0.015d / 30;

	List<RepaymentDtl> dtls = new ArrayList<RepaymentDtl>();

	List<RepaymentVo> repaymentVos = new ArrayList<>();

	public Repayment(BigDecimal totalPrincipal, int totalTerm, double rate, Date startDate, int interval) {
		this.totalPrincipal = totalPrincipal;
		this.totalTerm = totalTerm;
		this.rate = rate;
		this.startDate = DateUtils.truncate(startDate, Calendar.DAY_OF_MONTH);
		this.interval = interval;
		this.eachRepayment = totalPrincipal.multiply(new BigDecimal(1).add(new BigDecimal(rate)).pow(totalTerm).multiply(new BigDecimal(rate))).divide(new BigDecimal(1).add(new BigDecimal(rate)).pow(totalTerm).subtract(new BigDecimal(1)));
		
		BigDecimal principal = new BigDecimal(totalPrincipal.doubleValue());
		for (int i = 1; i <= totalTerm; i++) {
			if (i != totalTerm) {
				BigDecimal inverest = principal.multiply(new BigDecimal(rate));
				BigDecimal currPrincipal = eachRepayment.subtract(inverest);
				dtls.add(new RepaymentDtl(this, i, currPrincipal, inverest, DateUtils.addDays(this.startDate, (i - 1) * interval), principal = principal.subtract(currPrincipal)));
			} else {
				BigDecimal currPrincipal = principal;
				BigDecimal inverest = this.eachRepayment.subtract(currPrincipal);
				dtls.add(new RepaymentDtl(this, i, currPrincipal, inverest, DateUtils.addDays(this.startDate, (i - 1) * interval), BigDecimal.ZERO));
			}
		}
	}

	public void repay(RepaymentVo vo) {
		repaymentVos.add(vo);

		// 先还罚息
		for (Iterator<RepaymentDtl> iter = dtls.iterator(); iter.hasNext();) {
			RepaymentDtl dtl = iter.next();
			if (!dtl.repayOff) {
				dtl.repayPan(vo);
			}
		}

		//
		for (Iterator<RepaymentDtl> iter = dtls.iterator(); iter.hasNext();) {
			RepaymentDtl dtl = iter.next();
			if (!dtl.repayOff) {
				dtl.repay(vo);
			}
		}
	}

	public boolean isRepayOff() {
		boolean off = true;
		for (Iterator<RepaymentDtl> iter = dtls.iterator(); iter.hasNext();) {
			RepaymentDtl dtl = iter.next();
			off = off && dtl.repayOff;
		}
		return off;
	}

	public SurplusRepaymentVo getSurplusRepaymentVo(Date date) {
		SurplusRepaymentVo sVo = new SurplusRepaymentVo(date);
		for (Iterator<RepaymentDtl> iter = dtls.iterator(); iter.hasNext();) {
			RepaymentDtl dtl = iter.next();
			// if (!dtl.repayOff)
			dtl.getRepayAmount(sVo);
		}
		return sVo;
	}

	// 获取第几期的还款金额
	public SurplusRepaymentVo getSurplusRepaymentVo(Date date, int number) {
		SurplusRepaymentVo sVo = new SurplusRepaymentVo(date);
		dtls.get(number - 1).getRepayAmount(sVo);
		return sVo;
	}

}
