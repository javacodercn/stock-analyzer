SELECT * FROM `stock-analyzer`.company where pe > 0 and pe < 3000
and roe >1000
-- 负债
and debt >0 and debt< 5000
and  market_capital > 10000
and gross_profit_ratio > 3000
-- 净利润
and npr > 1000