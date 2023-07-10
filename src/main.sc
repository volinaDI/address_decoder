theme: /
require: slotfilling/slotFilling.sc
  module = sys.zb-common
  
require: patterns.sc
  module = sys.zb-common
require: address/address.sc
  module = sys.zb-common
require: city/city.sc
  module = sys.zb-common
  
require: topics/chaotic.sc
require: topics/stepByStep.sc
require: patterns.sc


require: functions/functions.js
require: functions/crutchReplace.js

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

    state: NoMatch
        event!: noMatch
        a: Простите, я не поняла. Вы говорите: {{$request.query}}

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