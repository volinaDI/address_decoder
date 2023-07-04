require: slotfilling/slotFilling.sc
  module = sys.zb-common
  
require: patterns.sc
  module = sys.zb-common
require: address/address.sc
  module = sys.zb-common
  
require: patterns.sc
require: stepByStep.sc

require: functions.js
theme: /

    state: Start
        q!: $regex</start>
        if: !$client.api
            go!: /AskSelectAPI
        go!: /AddressParsing/AskAddress
        # q: * $yes * || toState = "/SuperChaotic"
        # q: * $no * || toState = "/StepByStep"
        
        

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
        
        state: NoMatch
            a: Что тестируем, Дадата или Яндекс?
            go: /AskSelectAPI
            
            
    state: NoMatch
        event!: noMatch
        a: No match. Запрос пользователя: {{$request.query}}

    state: TMP
        q!: tmp
        script:
            var text = "ыыыпыпы";
            $temp.ans = toPrettyString(parseAddressYandex(text));
        a: {{$temp.ans}}
    
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
        a: Назовите адрес
        
        state: GetAdress
            q: *
            script:
                var apiResponse = getResponseYandex($request.query);
                if (apiResponse) var res = parseYandexRes(apiResponse);
                else $reactions.answer("Не удалось получить ответ сервиса");
                $analytics.setComment(res);
                
                if (res) {
                    if(res) $reactions.answer(res[0].GeoObject.metaDataProperty.GeocoderMetaData.Address.formatted);
                }
                else $reactions.answer("Не нашлось такого адреса");
            go!: /AddressParsing/AskAddress
