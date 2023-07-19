require: import.sc

init:
    bind("postProcess", function($context) {
        $dialer.setNoInputTimeout(20000);
    });

theme: /

    state: Start
        q!: start
        q!: * (привет/сначала/~начало) *
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
    
    state: TMP
        q!: tmp
        a: address?
        
        state: TMP
            q: *
            script:
                var res = new RegExp('234..');
                $reactions.answer('2346699999'.replace(res, '1'));
            # a: {{toPrettySting()}}
            go!: /TMP

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