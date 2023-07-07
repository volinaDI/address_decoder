theme: /StepByStep
    
    state: AskCountry
        a: Назовите пожалуйста только страну
        
        state: Get
            q: *
            script:
                $temp.dadataResponse = parseAddressDadata($request.query);
                $session.dadataResult = dadataParseResponse($temp.dadataResponse);
            if: $session.dadataResult && $session.dadataResult.country
                a: Страна {{$session.dadataResult.country}}, правильно?
            else: 
                go!: Incorrect
                
            state: Correct
                q: * $yes *
                script:
                    $session.country = $session.dadataResult.country;
                    delete $session.dadataResult;
                go!: /StepByStep/AskCity
            
            state: Incorrect
                q: * $no *
                event: speechNotRecognized || fromState = "/StepByStep/AskCountry"
                a: Назовите пожалуйста только страну. Как можно более отчётливо
                go: /StepByStep/AskCountry

    state: AskCity
        a: Назовите пожалуйста только город или населённый пункт
        
        state: Get
            q: *
            script:
                $temp.dadataResponse = parseAddressDadata($request.query);
                $session.dadataResult = dadataParseResponse($temp.dadataResponse);
            if: $session.dadataResult && $session.dadataResult.city && $session.dadataResult.cityType
                a: Населённый пункт - {{$session.dadataResult.cityType}} {{$session.dadataResult.city}}, правильно?
            else: 
                go!: Incorrect
            
            state: Correct
                q: * $yes *
                script:
                    $session.city = $session.dadataResult.city;
                    $session.cityType = $session.dadataResult.cityType;
                    delete $session.dadataResult;
                go!: /StepByStep/AskStreet

            state: Incorrect
                q: * $no *
                event: speechNotRecognized || fromState = "/StepByStep/AskCity"
                a: Назовите пожалуйста только населённый пункт. Как можно более отчётливо
                go: /StepByStep/AskCity
                
    state: AskStreet
        a: Назовите пожалуйста только улицу
        
        state: Get
            q: $streetName [$streetType]
            q: [$streetType] $streetName
            script:
                $temp.dadataResponse = parseAddressDadata($request.query + " " + $session.city + " " + $session.cityType);
                $session.dadataResult = dadataParseResponse($temp.dadataResponse);
            if: $session.dadataResult && $session.dadataResult.street && $session.dadataResult.streetType
                a: {{$session.dadataResult.streetType}} {{$session.dadataResult.street}}, правильно?
            else:
                a: По моим данным в названном вами городе нет такой улицы. Вы сказали - {{$parseTree._streetType}} {{$parseTree._addressWordsRegexp}}. Правильно?
                script:
                    $session.tryStreet = $parseTree._addressWordsRegexp;
                    $session.tryStreetType = $parseTree._streetType;
    
            state: Correct
                q: * $yes *
                script:
                    $session.street = $session.dadataResult.street ? $session.dadataResult.street : $session.tryStreet ;
                    $session.streetType = $session.dadataResult.streetType ? $session.dadataResult.streetType : $session.tryStreetType;
                    delete $session.dadataResult;
                go!: /StepByStep/AskHouseNumber

            state: Incorrect
                q: * $no *
                event: speechNotRecognized || fromState = "/StepByStep/AskStreet"
                event: noMatch || fromState = "/StepByStep/AskStreet"
                a: Назовите пожалуйста только улицу. Как можно более отчётливо
                go: /StepByStep/AskStreet
            
    state: AskHouseNumber
        a: Назовите пожалуйста только номер дома
        
        state: Get
            q: $customHouse
            script:
                $session.house = $request.query.replace(/дом /, "").replace(/номер /, "")
            # a: Номер дома - {{$session.house}}
            a: Итак, полный адрес {{$session.country}} {{$session.cityType}} {{$session.city}} {{$session.streetType}} {{$session.street}} {{$session.house}}
            go!: /Address/Ask
            
        state: Incorrect
            event: speechNotRecognized
            event: noMatch
            # a: 
            go!: /StepByStep/AskHouseNumber