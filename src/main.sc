require: slotfilling/slotFilling.sc
  module = sys.zb-common
  
require: patterns.sc
  module = sys.zb-common
require: address/address.sc
  module = sys.zb-common
  
require: chaotic.sc
require: stepByStep.sc

require: replacesYandex.yaml
  var = replacesYandex
  name = replacesYandex

require: functions.js
theme: /

    state: Start
        q!: start
        q!: * (привет/сначала/~начало) *
        script: $client.api = "dadata"
        #     go!: /AskSelectAPI
        # if: $client.api === "yandex"
        #     go!: /Yandex/AskAddress
        a: Привет
        go!: /TestRecognition

    state: AskSelectAPI
        q!: [change/switch] api
        a: Чтобы тестировать интеграцию Дадата скажите - дата. Чтобы попробовать Яндекс скажите - Яндекс.
        
        state: GetAPI
            q: * ($one/[да] ~дата/да да) * : dadata
            q: * ($two/яндекс) * : yandex
            script: $client.api = $parseTree._Root;
            a: Хорошо, тестируем {{ $parseTree._Root === "dadata" ? "Дадата" : "Яндекс"}}
            go!: /Start
            
        state: NoMatch
            a: Что тестируем, Дадата или Яндекс?
            go: /AskSelectAPI
            
    state: NoMatch
        event!: noMatch
        a: No match. Вы говорите пользователя: {{$request.query}}

    state: TMP
        q!: tmp
        a: пиши адрес

        state: TMP
            q: * $Address
            a: это адрес
            a: {{toPrettyString($parseTree)}}
            go!: /TMP

        state: NoMatch
            event: noMatch
            a: это не адрес

    state: Reset
        q!: reset
        q!: * {(~сброс/~сбросить/~сбрасывать) * ~настройка} *
        script:
            $client = {};
            $session = {};
        a: Сброс настроек выполнен.
        
    state: NewSession
        q!: (reset/new) (session/s)
        q!: ns
        script: $jsapi.stopSession();
        a: Новая сессия, ура

    state: TestRecognition || modal = true
        q!: * {(тест*/тестиров*) распозн*} *
        a: Тестируем распознавание. Говори, а я буду записывать
        
        state: Get
            q: *
            # a: Записала запрос: {{$request.query}}
            a: Записала запрос.
            script: 
                if ($injector.ASRmodel[$request.botId] === "tinkoff") {
                    // $session.query = numeralsToNumbers($request.query);
                    justAsr(numeralsToNumbers($request.query));
                } else {
                    justAsr($request.query);
                }
            a: Давай дальше
            go: /TestRecognition
            
        state: NoMatch
            event: speechNotRecognized
            a: плохо слышно. повтори - пожалуйста
            go: /TestRecognition