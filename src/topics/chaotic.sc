theme: /Address
    
    state: Ask
        a: Назовите адрес
        script:
            delete $session.house;
            delete $session.street;
            delete $session.streetType;
            delete $session.city;
            delete $session.cityType;
            delete $session.country;
            delete $session.yandexOk;
            delete $session.dadataRes;
            delete $session.addressAnswer;
        
        state: Get
            q: * {[$addressCity/$City] * ($addressStreet * $customHouse)} *
            q: * {($addressCity/$City) * ($addressStreet * [$customHouse])} *
            q: * {башкортостан * (сквер/худайбер*/худойбер*)} *
            script:
                delete $session.dadataRes;
                delete $session.addressAnswer;
                $session.query = $request.query.replace(/номер /, "");
                $session.query = chaoticAddressReplace($session.query);
                $session.firstRequest = $request.query;
                // если Тинькофф, надо пошаманить с числами
                if ($injector.ASRmodel[$request.botId] === "tinkoff") $session.query = numeralsToNumbers($session.query);
                // dadata
                $session.dadataResponse = parseAddressDadata($session.query);
                $temp.dadataOk = true;
            # dadata не отвечает
            if: !$session.dadataResponse
                a: Произошла техническая ошибка. Нет доступа к базе данных
                a: Перезвоните пожалуйста.
                script: $response.replies.push({"type": "hangup"});
            else:
                script:
                    $session.dadataRes = dadataParseResponse($session.dadataResponse);
                    // проверка страны на вменяемость
                    if (["Казахстан", "Россия", "Беларусь", "Армения", "Молдова, Республика"].indexOf($session.dadataRes.country) === -1) {
                        $temp.dadataOk = false;
                        $temp.incorrectCountry = true;
                    }
                    // если Казахстан, используем яндекс
                    if ($session.dadataRes.country === "Казахстан") {
                        // костыль улица Шугыла Бау-Бакша Сериктестиги
                        if (isBauBaksha($session.query)) {
                            $session.street = "Шугыла Бау-Бакша Сериктестиги";
                            $session.streetType = "улица";
                            $session.city = "Кызылорда";
                            $session.cityType = "город";
                            $session.country = "Казахстан";
                            if ($parseTree._customHouse) $session.house = $parseTree._customHouse.replace(/[Дд]ом /, "").replace(/номер /, "");
                            $session.yandexOk = true;
                        } else {
                            $temp.yandexRes = parseYandexGeoObject(getResponseYandex($session.query));
                            if ($temp.yandexRes) $temp.yandexComponents = yandexComponents($temp.yandexRes);
                            if ($temp.yandexComponents) {
                                $session.country = $temp.yandexComponents.country;
                                $session.city = $temp.yandexComponents.city;    
                                $session.cityType = $temp.yandexComponents.cityType;
                                $session.street = $temp.yandexComponents.street;
                                $session.streetType = $temp.yandexComponents.streetType;
                            }
                            if ($parseTree._customHouse) $session.house = $parseTree._customHouse.replace(/[Дд]ом /, "").replace(/номер /, "");
                        }
                    }
                    // формулируем ответ
                    $session.addressAnswer = formAddreessToSay($session.dadataRes);
                    if ($session.street && $session.streetType && $session.house) {
                        $session.addressAnswer = $session.country + ", " + $session.cityType + " " + $session.city + ", " + $session.streetType + " " + $session.street +  ", дом "+ $session.house;
                        $session.yandexOk = true;
                        $temp.dadataOk = false;
                    } else if ($session.street && $session.streetType && !$session.house) {
                        $session.addressAnswer = $session.country + ", " + $session.cityType + " " + $session.city + ", " + $session.streetType + " " + $session.street;
                        $session.yandexOk = true;
                        $temp.yandexOnlyStreet = true;
                        $temp.dadataOk = false;
                    }
                    if (!$session.dadataRes.street || !$session.dadataRes.house) {
                        $temp.dadataOk = false;
                    }
                if: $temp.dadataOk || ($session.yandexOk && !$temp.yandexOnlyStreet)
                    a: {{$session.addressAnswer}}. Это правильный адрес?
                else:
                    if: $temp.incorrectCountry 
                        a: Давайте я попробую записать адрес по частям.
                        go!: /StepByStep/AskCountry
                    if: $session.dadataRes.country && !$session.dadataRes.city && !$session.yandexOk
                        go!: OnlyCountry
                    if: $session.dadataRes.city && !$session.dadataRes.street && !$session.yandexOk
                        go!: OnlyCity
                    if: ($session.dadataRes.city && $session.dadataRes.street && !$session.dadataRes.house && !$session.yandexOk) || $temp.yandexOnlyStreet
                        go!: OnlyStreet
                    elseif:!$session.yandexOk 
                        a: Произошла техническая ошибка. Нет доступа к базе данных
                        a: Перезвоните пожалуйста.
                        script: $response.replies.push({"type": "hangup"});

            state: Yes
                q: * $yes * 
                script: 
                    // заполнение таблицы
                    if ($session.yandexOk) {
                        addFullLineTable($session.firstRequest, $session.country + ", " + $session.cityType + " " + $session.city + ", " + $session.streetType + " " + $session.street +  ", дом "+ $session.house,
                        $session.country,
                        $session.city + " (" + $session.cityType + ")",
                        $session.street + " (" + $session.streetType + ")",
                        "№" + $session.house);
                    } else {
                        addFullLineTable($session.firstRequest, $session.dadataResponse.result,
                        $session.dadataRes.country,
                        $session.dadataRes.city + " (" + $session.dadataRes.cityType + ")",
                        $session.dadataRes.street + " (" + $session.dadataRes.streetType + ")",
                        "№" + $session.dadataRes.house + ($session.dadataRes.houseAdd ? " " + $session.dadataRes.houseAdd : ""))
                    }
                    delete $session.firstRequest;
                go!: /Address/Ask
            
            state: No
                q: * $no *
                a: Очень жаль. Давайте я попробую записать адрес по частям.
                go!: /StepByStep/AskCountry
                
            state: NoMatch
                event: noMatch
                event: speechNotRecognized
                a: Простите - не расслышала. {{$session.addressAnswer}} - это правильный адрес?
                go: /Address/Ask/Get
                
            state: OnlyCountry
                a: Я поняла только часть адреса. Страна {{$session.addressAnswer}} - это правильно?
                
                state: Correct
                    q: * $yes *
                    script:
                        $session.country = $session.dadataRes.country;
                    a: Хорошо, помогите мне пожалуйста записать полный адрес
                    go!: /StepByStep/AskCity

                state: Incorrect
                    q: *
                    event: speechNotRecognized
                    a: Очень жаль. Давайте я попробую записать адрес по частям.
                    script: delete $session.dadataRes;
                    go!: /StepByStep/AskCountry            

            state: OnlyCity
                a: Я поняла только часть адреса. {{$session.addressAnswer}} - это правильно?
                
                state: Correct
                    q: * $yes *
                    script:
                        $session.country = $session.dadataRes.country;
                        $session.city = $session.dadataRes.city;
                        $session.cityType = $session.dadataRes.cityType;
                    a: Хорошо, помогите мне пожалуйста записать полный адрес
                    go!: /StepByStep/AskStreet
                    
                state: Incorrect
                    q: *
                    event: speechNotRecognized
                    a: Очень жаль. Давайте я попробую записать адрес по частям.
                    script: delete $session.dadataRes;
                    go!: /StepByStep/AskCountry
                
            state: OnlyStreet
                a: Я поняла только часть адреса. {{$session.addressAnswer}} - это правильно?
                
                state: Correct
                    q: * $yes *
                    script:
                        $session.country = $session.country ? $session.country : $session.dadataRes.country;
                        $session.city = $session.city ? $session.city : $session.dadataRes.city;
                        $session.cityType = $session.cityType ? $session.cityType : $session.dadataRes.cityType;
                        $session.street = $session.street ? $session.street : $session.dadataRes.street;
                        $session.streetType = $session.streetType ? $session.streetType : $session.dadataRes.streetType;
                    a: Хорошо, помогите мне пожалуйста записать полный адрес
                    go!: /StepByStep/AskHouseNumber
                    
                state: Incorrect
                    q: *
                    event: speechNotRecognized
                    a: Очень жаль. Давайте я попробую записать адрес по частям.
                    script: delete $session.dadataRes;
                    go!: /StepByStep/AskCountry
        
        state: NoMatch
            event: noMatch
            event: speechNotRecognized
            a: Это не похоже на адрес, попробуйте ещё раз. Назовите пожалуйста реально существующий адрес - без указания квартиры, этажа и почтового индекса.
            go!: /Address/Ask
