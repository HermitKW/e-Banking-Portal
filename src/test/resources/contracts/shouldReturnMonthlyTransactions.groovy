import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "GET monthly transactions returns a page with per-page FX totals in the target currency"
    request {
        method GET()
        urlPath('/api/v1/transactions') {
            queryParameters {
                parameter 'yearMonth': '2020-10'
                parameter 'targetCurrency': 'CHF'
            }
        }
        headers {
            header 'Authorization': 'Bearer test-token'
        }
    }
    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body([
                yearMonth     : '2020-10',
                targetCurrency: 'CHF',
                fxRateAsOf    : '2026-06-03T09:00:00Z',
                page          : 0,
                size          : 50,
                totalElements : 1,
                totalPages    : 1,
                pageTotals    : [
                        totalCredit: '110.00',
                        totalDebit : '-55.00',
                        currency   : 'CHF'
                ],
                transactions  : [[
                        id                    : 't1',
                        amount                : '100.00',
                        currency              : 'GBP',
                        amountInTargetCurrency: '110.00',
                        iban                  : 'CH93-0000-0000-0000-0000-0',
                        valueDate             : '2020-10-01',
                        type                  : 'CREDIT',
                        description           : 'salary'
                ]]
        ])
        bodyMatchers {
            jsonPath('$.fxRateAsOf', byType())
            jsonPath('$.transactions', byType())
        }
    }
}
