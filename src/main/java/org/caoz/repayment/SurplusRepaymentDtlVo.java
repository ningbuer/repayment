package org.caoz.repayment;

import java.math.BigDecimal;

public class SurplusRepaymentDtlVo {

	SurplusRepaymentVo sVo;
	RepaymentDtl dtl;

	BigDecimal surplusPan;
	BigDecimal surplusBreak;
	BigDecimal surplusInverest;
	BigDecimal surplusPrincipal;

	SurplusRepaymentDtlVo(SurplusRepaymentVo sVo, RepaymentDtl dtl, BigDecimal surplusPan, BigDecimal surplusBreak, BigDecimal surplusInverest, BigDecimal surplusPrincipal) {
		this.sVo = sVo;
		this.dtl = dtl;
		this.sVo.sDtlVos.add(this);

		this.surplusPan = surplusPan;
		this.surplusBreak = surplusBreak;
		this.surplusInverest = surplusInverest;
		this.surplusPrincipal = surplusPrincipal;
	}

	public BigDecimal getSurplusTotalAmount() {
		return new BigDecimal(0d).add(surplusPan).add(surplusBreak).add(surplusInverest).add(surplusPrincipal);
	}

	public int getTerm(){
		return dtl.currTerm;
	}
	
	
	public String toString(){
		return String.format("日期为%s时,期数%s,剩余本金:%s,剩余利息:%s,剩余违约利息:%s,剩余罚息:%s\r\n",RepaymentUtil.sdf.format(sVo.repayDate) , getTerm() , surplusPrincipal,surplusInverest,surplusBreak,surplusPan);
	}
	
}
