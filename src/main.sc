require: slotfilling/slotFilling.sc
  module = sys.zb-common
  
require: patterns.sc
  module = sys.zb-common
require: address/address.sc
  module = sys.zb-common
require: city/city.sc
  module = sys.zb-common
  
require: chaotic.sc
require: stepByStep.sc
require: patterns.sc

require: replacesYandex.yaml
  var = replacesYandex
  name = replacesYandex

require: functions.js
require: replacements.js

init:
    bind("postProcess", function($context) {
        $dialer.setNoInputTimeout(20000);
    });


theme: /

    state: Start
        q!: start
        q!: * (привет/сначала/~начало) *
        script: $client.api = "dadata"
        a: Здравствуйте.
        go!: /Address/Ask
        go!: /TMP

    state: NoMatch
        event!: noMatch
        a: No match. Вы говорите: {{$request.query}}

    state: TMP
        q!: tmp
        a: текст на замену
        # a: {{"Нур-суsdfлтан".toLowerCase().replace(replacesYandex.pp, "астана")}}
        # a: {{toPrettyString($nlp.match("улица 2 тверская ясмская дом 4 корпус 2 строение 1", "/Address/Ask/Get"))}}
        # a: {{toPrettyString($nlp.match("проспект Рахимжана Кошкарбаева", ))}}
                # a: {{toPrettyString($nlp.match("проспект Рахимжана Кошкарбаева", ["(~улица|~переулок|~проспект|проект|~проезд|~бульвар|шоссе|микрорайон|~площадь|аллеи|аллея|~съезд|~набережная|~канал|километр|тупик)"]))}}

        state: TMP
            q: * 
            # script:
            #     var ents = $caila.entitiesLookup("казахстан город кызылорда улица Шугыла Бау-Бакша Сериктестиги дом 2", true).entities
            #     return ents.forEach(function(entityElem) {
            #         if (entityElem.entity) return true;
            #     });
                
            # a: {{toPrettyString(isBauBaksha("казахстан город кызылорда улица Шугыла Бау-Бакша Сериктестиги дом 2"))}}
            a: {{chaoticAddressReplace($request.query)}}
            go!: /TMP

        state: NoMatch
            event: noMatch
            a: это не адрес
            go!: /TMP

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
        a: Хорошо, тестируем распознавание. Говори, а я буду повторять.
        
        state: Get
            q: *
            a: Вы сказали: {{$request.query}}
            go: /TestRecognition