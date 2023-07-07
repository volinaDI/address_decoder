theme: /StepByStep
    
    state: AskCountry
        a: Назовите пожалуйста только страну
        script: $session.stepByStepCounter = 0;
        
        state: Get
            q: *
            script:
                # $session.stepByStepCounter = 0;
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
                if: $session.stepByStepCounter > 2
                    a: К сожалению не удалось распознать этот адрес. Попробуем ещё раз?
                    go!: /Address/Ask
                script:
                    $session.stepByStepCounter += 1;
                a: Назовите пожалуйста только страну. Как можно более отчётливо
                go: /StepByStep/AskCountry

    state: AskCity
        a: Назовите пожалуйста только город или населённый пункт
        script: $session.stepByStepCounter = 0;
        
        state: Get
            q: *
            script:
                # $session.stepByStepCounter = 0;
                $temp.dadataResponse = parseAddressDadata($request.query.replace(/[Нн][уо]р[\- ]?султан[^\s]/, "Астана"));
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
                    $session.region = $session.dadataResult.region;
                    $session.regionType = $session.dadataResult.region_type_full;
                    delete $session.dadataResult;
                go!: /StepByStep/AskStreet

            state: Incorrect
                q: * $no *
                event: speechNotRecognized || fromState = "/StepByStep/AskCity"
                if: $session.stepByStepCounter > 2
                    a: К сожалению не удалось распознать этот адрес. Попробуем ещё раз?
                    go!: /Address/Ask
                script:
                    $session.stepByStepCounter += 1;
                a: Назовите пожалуйста только населённый пункт. Как можно более отчётливо
                go: /StepByStep/AskCity
                
    state: AskStreet
        a: Назовите пожалуйста только улицу
        script: $session.stepByStepCounter = 0;
        
        state: Get
            q: $streetName [$streetType]
            q: [$streetType] $streetName
            script:
                # $session.stepByStepCounter = 0;
                $temp.dadataResponse = parseAddressDadata($request.query + " " + $session.city + " " + $session.cityType);
                $session.dadataResult = dadataParseResponse($temp.dadataResponse);
            if: $session.dadataResult && $session.dadataResult.street && $session.dadataResult.streetType
                a: {{$session.dadataResult.streetType}} {{$session.dadataResult.street}}, правильно?
            else:
                a: По моим данным в названном вами городе нет такой улицы. Вы сказали - {{$parseTree._streetType}} {{$parseTree._streetName}}. Правильно?
                script:
                    $session.tryStreet = $parseTree._streetName;
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
                if: $session.stepByStepCounter > 2
                    a: К сожалению не удалось распознать этот адрес. Попробуем ещё раз?
                    go!: /Address/Ask
                script: $session.stepByStepCounter++;
                a: Назовите пожалуйста только улицу. Как можно более отчётливо
                go: /StepByStep/AskStreet
            
    state: AskHouseNumber
        a: Назовите пожалуйста только номер дома
        script: $session.stepByStepCounter = 0;

        state: Get
            q: $customHouse
            script:
                $session.house = $request.query.toLowerCase().replace(/[Дд]ом /, "").replace(/номер /, "").replace(/курс /, "корпус ");
            a: Полный номер дома - {{$session.house}}. Это правильно?
            
            state: Correct
                q: * $yes *
                # a: Итак, полный адрес {{$session.country}}, {{$session.regionType}} {{$session.region}}, {{$session.cityType}} {{$session.city}}, {{$session.streetType}} {{$session.street}}, дом {{$session.house}}
                a: Итак, полный адрес {{$session.country}},  {{$session.cityType}} {{$session.city}}, {{$session.streetType}} {{$session.street}}, дом {{$session.house}}
                script:
                    # addLineTable($session.firstRequest, [$session.country, $session.cityType, $session.city, $session.streetType, $session.street, "дом", $session.house].join(" "));
                    addFullLineTable($session.firstRequest, [$session.country, $session.cityType, $session.city, $session.streetType, $session.street, "дом", $session.house].join(" "),
                    $session.country,
                    # $session.region + " (" + $session.regionType + ")",
                    $session.city + " (" + $session.cityType + ")",
                    $session.streetType ? $session.street + " (" + $session.streetType + ")" : $session.street,
                    "№" + $session.house)
                go!: /Address/Ask
                
            state: Incorrect
                event: speechNotRecognized
                event: speechNotRecognized || fromState = "/StepByStep/AskHouseNumber"
                event: noMatch
                event: noMatch || fromState = "/StepByStep/AskHouseNumber"
                if: $session.stepByStepCounter > 2
                    a: К сожалению не удалось распознать этот адрес. Попробуем ещё раз?
                    go!: /Address/Ask
                script: $session.stepByStepCounter++;
                a: Назовите пожалуйста только полный номер дома. Если у дома есть корпус или строение, назовите их тоже.
                go: /StepByStep/AskHouseNumber