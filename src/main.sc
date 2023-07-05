require: slotfilling/slotFilling.sc
  module = sys.zb-common
  
require: patterns.sc
  module = sys.zb-common
require: address/address.sc
  module = sys.zb-common
  
require: patterns.sc
require: stepByStep.sc

require: replacesYandex.yaml
  var = replacesYandex
  name = replacesYandex

require: functions.js
theme: /

    state: Start
        q!: $regex</start>
        q!: * (привет/сначала) *
        if: !$client.api
            go!: /AskSelectAPI
        go!: /AddressParsing/AskAddress

    state: SuperChaotic
        q!: (арбуз/ch/ср)
        a: Какой адрес вас интересует?
        
        state: GetAddress
            q: *
            a: {{toPrettyString(getFullAddress($request.query))}}

    state: AskSelectAPI
        a: Чтобы тестировать интеграцию Дадата скажите - дата. Чтобы попробовать Яндекс скажите - Яндекс.
        
        state: GetAPI
            q: * ($one/[да] ~дата) * : dadata
            q: * ($two/яндекс) * : ya
            script: $client.api = $parseTree._Root;
            a: Хорошо, тестируем {{ $parseTree._Root === "dadata" ? "Дадата" : "Яндекс"}}
            go!: /AddressParsing/AskAddress
            
        state: NoMatch
            a: Что тестируем, Дадата или Яндекс?
            go: /AskSelectAPI
            
            
    state: NoMatch
        event!: noMatch
        a: No match. Запрос пользователя: {{$request.query}}

    state: TMP
        q!: tmp
        a: {{replaceFromDict('3', replacesYandex)}}
        
    
    state: Reset
        q!: reset
        q!: * {(~сброс/~сбросить/~сбрасывать) * ~настройка} *
        script:
            $client = {};
            $session = {};
        a: Сброс настроек выполнен.
        
    state: NewSession
        q!: (reset/new) session
        script: $jsapi.stopSession();
        a: Новая сессия, ура

theme: /AddressParsing
    
    state: AskAddress
        q: * $yes * ||fromState = "/AddressParsing/AskAddress/GetAddress"
        a: Назовите адрес
        
        state: GetAddress
            q: *
            script: $temp.apiResponse = getResponseYandex($request.query);
            if: !$temp.apiResponse
                a: Не удалось получить ответ сервиса.
            else:
                script:
                    $temp.res = parseYandexRes($temp.apiResponse);
                    $session.query = $request.query;
                    $analytics.setComment(toPrettyString($temp.apiResponse));
                if: !$temp.res
                    a: Не нашлось такого адреса.
                    if: replaceFromDict($request.query, replacesYandex) != $request.query
                        go!: AddressWithReplace
                else:
                    a: {{$temp.res[0].GeoObject.metaDataProperty.GeocoderMetaData.Address.formatted}}
                    a: Это правильный ответ?

            state: AddressWithReplace
                q: * $no *
                a: После замены получилось - {{replaceFromDict($session.query, replacesYandex)}}
                go!: /AddressParsing/AskAddress

            