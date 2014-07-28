package org.caoz.repayment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;

public class RepaymentDtl {

	final Repayment repayment;
	final int currTerm;// 当前期数
	final BigDecimal currPrincipal;// 当期本金
	final BigDecimal currInverest; // 当期利息
	final BigDecimal remainPrincipal;// 剩余本金

	final Date currStartDate;// 当期开始日期
	final Date currEndDate;// 当期结束日期

	boolean repayOff = false;// 还清

	Date lastRepayDate;// 上次还款日期
	BigDecimal surplusPrincipal;// 当期剩余本金
	BigDecimal surplusInverest;// 当期剩余利息
	BigDecimal surplusPan;// 当期剩余罚息
	BigDecimal surplusBreak;// 当期剩余违约利息

	BigDecimal realRepayPrincipal;// 实际还款本金
	BigDecimal realRepayNormalInverest;// 实际正常还款金额
	BigDecimal realRepayBreakInverest;// 实际违约利息还款金额
	BigDecimal realRepayPanInverest;// 实际罚息还款金额

	BigDecimal currToleranceAmount; // 当期容差金额

	List<RepaymentDtlVo> dtlVos = new ArrayList<RepaymentDtlVo>();

	RepaymentDtl(Repayment repayment, int currTerm, BigDecimal currPrincipal, BigDecimal currInverest, Date currStartDate, BigDecimal remainPrincipal) {
		this.repayment = repayment;
		this.currTerm = currTerm;
		this.currPrincipal = this.surplusPrincipal = currPrincipal;
		this.currInverest = this.surplusInverest = currInverest;
		this.surplusBreak = BigDecimal.ZERO;
		this.surplusPan = BigDecimal.ZERO;
		realRepayPrincipal = BigDecimal.ZERO;
		realRepayNormalInverest = BigDecimal.ZERO;
		realRepayBreakInverest = BigDecimal.ZERO;
		realRepayPanInverest = BigDecimal.ZERO;
		currToleranceAmount = BigDecimal.ZERO;
		
		this.currStartDate = this.lastRepayDate = currStartDate;
		this.currEndDate = DateUtils.addDays(this.currStartDate, repayment.interval);
		this.remainPrincipal = remainPrincipal;
	}

	void repay(final RepaymentVo repaymentVo) {

		// 计算金额
		Date repayDate = repaymentVo.repayDate;
		if (repayDate.after(currEndDate)) {// 逾期
			surplusPan = surplusPan.add(currPrincipal.multiply(new BigDecimal(RepaymentUtil.diffDay(repayDate, lastRepayDate.after(currEndDate) ? lastRepayDate : currEndDate)).multiply(new BigDecimal(repayment.pRate))));
		} else {
			int nDay = RepaymentUtil.diffDay(repayDate.after(currStartDate) ? repayDate : currStartDate, lastRepayDate);// 正常利息天数
			BigDecimal inverest = currInverest.multiply(new BigDecimal(nDay).divide(new BigDecimal(repayment.interval)));// 正常利息
			BigDecimal amount = repaymentVo.repaySurplusAmount.subtract(inverest);
			if (amount.compareTo(BigDecimal.ZERO) > 0) {
				BigDecimal breakInverest = amount.min(currInverest.subtract(inverest));
				surplusBreak = surplusBreak.add(breakInverest);
				surplusInverest = surplusInverest.subtract(breakInverest);
			}
		}
		lastRepayDate = repayDate.after(lastRepayDate) ? repayDate : lastRepayDate;

		// 还款
		repayPan(repaymentVo);
		repayBreak(repaymentVo);
		repayInverest(repaymentVo);
		repayPrincipal(repaymentVo);
	}

	void repayPan(final RepaymentVo repaymentVo) {
		if (surplusPan.compareTo(BigDecimal.ZERO) > 0 && repaymentVo.repaySurplusAmount.compareTo(BigDecimal.ZERO) > 0) {
			BigDecimal repayPan = repaymentVo.repaySurplusAmount.min(surplusPan);
			dtlVos.add(new RepaymentDtlVo(repaymentVo, this, repayPan, "罚息"));
			repaymentVo.repaySurplusAmount = repaymentVo.repaySurplusAmount.subtract(repayPan);
			surplusPan = surplusPan.subtract(repayPan);
			realRepayPanInverest = realRepayPanInverest.add(repayPan);

			if (surplusPan.compareTo(RepaymentUtil.toleranceLimit) < 0) {
				currToleranceAmount = currToleranceAmount.add(surplusPan);
				repaymentVo.toleranceAmount = repaymentVo.toleranceAmount.add(surplusPan);
				repaymentVo.toleranceType = "罚息";
				surplusPan = BigDecimal.ZERO;
			}
		}
	}

	private void repayBreak(final RepaymentVo repaymentVo) {
		if (surplusBreak.compareTo(BigDecimal.ZERO) > 0 && repaymentVo.repaySurplusAmount.compareTo(BigDecimal.ZERO) > 0) {// 还违约利息
			BigDecimal repayBreak = repaymentVo.repaySurplusAmount.min(surplusBreak);
			dtlVos.add(new RepaymentDtlVo(repaymentVo, this, repayBreak, "利息"));
			repaymentVo.repaySurplusAmount = repaymentVo.repaySurplusAmount.subtract(repayBreak);
			surplusBreak = surplusBreak.subtract(repayBreak);
			realRepayBreakInverest = realRepayBreakInverest.add(repayBreak);

			if (surplusBreak.compareTo(RepaymentUtil.toleranceLimit) < 0) {
				currToleranceAmount = currToleranceAmount.add(surplusBreak);
				repaymentVo.toleranceAmount = repaymentVo.toleranceAmount.add(surplusBreak);
				repaymentVo.toleranceType = "违约利息";
				surplusBreak = BigDecimal.ZERO;
			}
		}
	}

	private void repayInverest(final RepaymentVo repaymentVo) {
		if (surplusInverest.compareTo(BigDecimal.ZERO) > 0 && repaymentVo.repaySurplusAmount.compareTo(BigDecimal.ZERO) > 0) {// 还利息
			BigDecimal repayInverest = repaymentVo.repaySurplusAmount.min(surplusInverest);
			dtlVos.add(new RepaymentDtlVo(repaymentVo, this, repayInverest, "利息"));
			repaymentVo.repaySurplusAmount = repaymentVo.repaySurplusAmount.subtract(repayInverest);
			surplusInverest = surplusInverest.subtract(repayInverest);
			realRepayNormalInverest = realRepayNormalInverest.add(repayInverest);

			if (surplusInverest.compareTo(RepaymentUtil.toleranceLimit) < 0) {
				currToleranceAmount = currToleranceAmount.add(surplusInverest);
				repaymentVo.toleranceAmount = repaymentVo.toleranceAmount.add(surplusInverest);
				repaymentVo.toleranceType = "利息";
				surplusInverest = BigDecimal.ZERO;
			}
		}
	}

	private void repayPrincipal(final RepaymentVo repaymentVo) {
		if (surplusPrincipal.compareTo(BigDecimal.ZERO) > 0 && repaymentVo.repaySurplusAmount.compareTo(BigDecimal.ZERO) > 0) {// 还本金
			BigDecimal repayPrincipal = repaymentVo.repaySurplusAmount.min(surplusPrincipal);
			dtlVos.add(new RepaymentDtlVo(repaymentVo, this, repayPrincipal, "本金"));
			repaymentVo.repaySurplusAmount = repaymentVo.repaySurplusAmount.subtract(repayPrincipal);
			surplusPrincipal = surplusPrincipal.subtract(repayPrincipal);
			realRepayPrincipal = realRepayPrincipal.add(repayPrincipal);

			if (surplusPrincipal.compareTo(RepaymentUtil.toleranceLimit) < 0) {
				currToleranceAmount = currToleranceAmount.add(surplusPrincipal);
				repaymentVo.toleranceAmount = repaymentVo.toleranceAmount.add(surplusPrincipal);
				repaymentVo.toleranceType = "本金";
				surplusPrincipal = BigDecimal.ZERO;
				repayOff = true;
			}
		}
	}

	void getRepayAmount(final SurplusRepaymentVo sVo) {
		Date date = sVo.repayDate;

		BigDecimal surplusPanTemp = new BigDecimal(surplusPan.doubleValue());
		BigDecimal surplusBreakTemp = new BigDecimal(surplusBreak.doubleValue());
		BigDecimal surplusInverestTemp = new BigDecimal(surplusInverest.doubleValue());
		BigDecimal surplusPrincipalTemp = new BigDecimal(surplusPrincipal.doubleValue());

		if (date.after(currEndDate)) {
			surplusPanTemp = surplusPanTemp.add(currPrincipal.multiply(new BigDecimal(RepaymentUtil.diffDay(date, lastRepayDate.after(currEndDate) ? lastRepayDate : currEndDate)).multiply(new BigDecimal(repayment.pRate))));
		} else {
			int nDay = RepaymentUtil.diffDay(date.after(currStartDate) ? date : currStartDate, lastRepayDate);// 正常利息天数
			BigDecimal inverest = currInverest.multiply(new BigDecimal(nDay).divide(new BigDecimal(repayment.interval), 4, BigDecimal.ROUND_HALF_UP));// 正常利息
			surplusBreakTemp = surplusBreakTemp.add(surplusInverestTemp.subtract(inverest));
			surplusInverestTemp = inverest;
		}
		new SurplusRepaymentDtlVo(sVo, this, surplusPanTemp, surplusBreakTemp, surplusInverestTemp, surplusPrincipalTemp);
	}
}
