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
        q!: $regex</start>
        q!: * (привет/сначала/~начало) *
        if: !$client.api
            go!: /AskSelectAPI
        if: $client.api === "yandex"
            go!: /Yandex/AskAddress
        go!: /Dadata/AskAddress

    state: AskSelectAPI
        q!: [change/switch] api
        a: Чтобы тестировать интеграцию Дадата скажите - дата. Чтобы попробовать Яндекс скажите - Яндекс.
        
        state: GetAPI
            q: * ($one/[да] ~дата) * : dadata
            q: * ($two/яндекс) * : ya
            script: $client.api = $parseTree._Root;
            a: Хорошо, тестируем {{ $parseTree._Root === "dadata" ? "Дадата" : "Яндекс"}}
            go!: /Start
            
        state: NoMatch
            a: Что тестируем, Дадата или Яндекс?
            go: /AskSelectAPI
            
    state: NoMatch
        event!: noMatch
        a: No match. Запрос пользователя: {{$request.query}}

    state: TMP
        q!: tmp
        script:
            addLineTable("шарикоподшипниковская улица дом шесть корпус два строение три", "г Москва, ул Шарикоподшипниковская");
        #     $temp.text = "москва большой спасоглинищевский переулок дом четыре дробь два а";
        #     var res = $caila.entitiesLookup($temp.text, true);
        #     res.entities.forEach(function(elem) {
        #         if (elem.entity === "zb.number") {
        #             $temp.text = $temp.text.replace(elem.text, parseInt(elem.value))
        #         }
        #     })
        #     $temp.text = $temp.text.replace("дробь", "/")
        a: {{$injector.ASRmodel[$request.botId] ? true : false}}

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

