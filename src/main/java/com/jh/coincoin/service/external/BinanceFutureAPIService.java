package com.jh.coincoin.service.external;

import com.jh.coincoin.model.type.BinanceType.BinanceURL;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Created by dale on 2024-09-11.
 */

// 아마 이 클레스는 추후 주문 api등을 활용할듯
@Service
@RequiredArgsConstructor
public class BinanceFutureAPIService {

    private final RestClient restClient;


}
