# COINCOIN (feat.slack)

## 설명

----
이 서비스는 코인들의 **지표값 알람, 자동매매** 서비스이다.\
현재 거래소에서는 가격의 대한 실시간 알람은 있지만 지표의 대한 알람은 없다고 확인된다.
따라서 해당 서비스가 필요하다고 생각되어 만들었다.

## 아키텍처
* **서버 구성**
> **coincoin** : 메인 서버\
> **candle-collector** : 캔들 수집 서버
* 인프라
>`EC2`,`RDS`,`DockerHub`
* 배포과정
> 1.`prod` 브랜치로 merge 및 push\
> 2.githubAction 빌드 (config repository에서 prod properties pull)\
> 3.DockerHub image push\
> 4.ec2에서 image pull 및 docker start
## 슬랙 명령어

----
명령어는 Slack의 Slash Command로 구성

1.도움말\
`/help`\
어떤 기능이 있는지 알려주는 명령어

2.새로운 코인 추가하기\
`/set_symbol [코인 심볼]` ex) /set_symbol BTCUSDT\
새롭게 추적하고 싶은 코인을 추적코인에 추가해준다.\
**최대 추적 가능한 코인의 갯수는 5개이다.**

3.추적 코인 제거하기\
`/delete_symbol [코인 심볼]` ex) /delete_symbol BTCUSDT\
코인 추적 리스트에서 제거한다.

4.매매 전략 생성\
`/create_trade_strategy` ex) /create_trade_strategy\
새로운 매매 전략을 생성한다. 커스텀 하고싶은 주문전략과 매수 전략이 있다면 먼저 생성하고 매매전략을 만든다.

5.주문 전략 생성\
`/create_order_strategy` ex) /create_order_strategy\
새로운 주문 전략을 생성한다.

6.매수 전략 생성\
`/create_buy_strategy` ex) /create_buy_strategy\
새로운 매수 전략을 생성한다. 매수 전략에는 손익비 전략이 포함되어있다.\
```
손절과 익절의 비율을 정하는 전략

TPYE : PEAK_RETIO
DESC : 전저/전고점과 진입가를 1비중으로 N배율 만큼 손/익절가를 설정
EX) 롱 진입평단:100 설정비율 1:2 손절라인:90 -> 익절:120 

TPYE : FIXED_RETIO
DESC : 설정된 값으로 손/익절가를 설정 (실제 캔들의 %. 레버리지 적용x)
EX) 롱 진입평단:100 설정비율 0.5:1 -> 손절:99.5 익절:101
```

7.자동매매 시작/종료\
`/auto_trade [on, off]` ex) /auto_trade on\
자동매매 활동 여부. 서버가 시작할 당시에는 기본 **off**로 설정됨
